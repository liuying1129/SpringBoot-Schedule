package com.yklis.schedule.business.job;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import com.yklis.schedule.util.Constants;
import com.yklis.schedule.util.SpringUtils;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * 命令模式
 * 命令实现类
 * 
 * 执行该JOB前,有必要初始化数据
 * chk_con_bak.TjJiWangShi='1'、chk_valu_bak.Reserve6
 * 以免浪费时间处理很旧的数据
 * @author ying07.liu
 *
 */
public class JobLis2Redis implements Command {

    //配置容器起动时候加载log4j配置文件
    //只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
    //在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
    //如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
    //PropertyConfigurator.configure("log4jj.properties");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private JdbcTemplate jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);    
    private ShardedJedisPool shardedJedisPool = SpringUtils.getBean(ShardedJedisPool.class);

    //类成员变量,类实例化对象时被执行
    //从连接池池中获取一个Jedis对象
    //private ShardedJedis shardedJedis = shardedJedisPool.getResource();
    
    //JAVA规定，如果类中没有定义任何构造函数，JVM自动为其生成一个默认的构造函数
    //故可不需要手动写下面的构造函数
    //public JobLis2Redis(){
    //}

    @Override
    public void execute(Map<String,Object> map) {
        
        //经测试,网络中断,JOB报错,网络复通后JOB可正常运行并正常写Redis
        
        ShardedJedis shardedJedis = null;
        try{
            //从连接池池中获取一个Jedis对象  
            shardedJedis = shardedJedisPool.getResource();

            List<Map<String, Object>> list = null;
            try{
                list = jdbcTemplate.queryForList(" select * from chk_con_bak ch where isnull(TjJiWangShi,'')<>'1' ");
            }catch(Exception e){            
                logger.error("获取待导入Redis列表(病人基本信息)失败:"+e.toString());
            }
            
            for(Map<String, Object> map1 : list) {
                                 
                //Redis不接受null值
                //对于Redis而言，使用分隔符(:)来组织关键字是很常见的方法.实际上,这个关键字里的冒号没有任何特殊含义
                //Hash中field的value如果变化,会被重写
                //Hash
                //shardedJedis.hset("PatientBaseInfo_Unid:"+map1.get("unid"), "patientname", null==map1.get("patientname")?"":map1.get("patientname").toString());
                //shardedJedis.hset("PatientBaseInfo_Unid:"+map1.get("unid"), "sex", null==map1.get("sex")?"":map1.get("sex").toString());
                //shardedJedis.hset("PatientBaseInfo_Unid:"+map1.get("unid"), "age", null==map1.get("age")?"":map1.get("age").toString());
                //shardedJedis.hset("PatientBaseInfo_Unid:"+map1.get("unid"), "check_date", dateFormat.format(map1.get("check_date")));            
                     
                Map<String,String> map11 = new HashMap<>();
                
                map11.put("patientname", null==map1.get("patientname")?"":map1.get("patientname").toString());
                map11.put("sex", null==map1.get("sex")?"":map1.get("sex").toString());
                map11.put("age", null==map1.get("age")?"":map1.get("age").toString());
                map11.put("check_date", dateFormat.format(map1.get("check_date")));
                
                //猜测:hmset性能应该比hset性能更好
                shardedJedis.hmset("PatientBaseInfo_Unid:"+map1.get("unid"), map11);
                
                
                //设置过期时间begin
                Date date1 = new Date();
                if(null!=map1.get("check_date")){
                    try {
                        date1=dateFormat.parse(map1.get("check_date").toString());
                    } catch (ParseException e) {
                        logger.error("JobLis2Redis,日期转换失败:"+e.toString());
                    }
                }
                
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(date1); 
                calendar.add(Calendar.DATE, Constants.EXPIRE_DAYS);
                
                Date date2 = calendar.getTime();
                
                String date2_unix = null;
                try {
                    date2_unix = String.valueOf(dateFormat.parse(dateFormat.format(date2)).getTime() / 1000);
                } catch (ParseException e) {
                    logger.error("JobLis2Redis,失效日期转换unix时间戳失败:"+e.toString());
                }
    
                shardedJedis.expireAt("PatientBaseInfo_Unid:"+map1.get("unid"), Long.parseLong(date2_unix));
                //设置过期时间end
                
                try{
                    jdbcTemplate.update("update chk_con_bak set TjJiWangShi=1 where unid="+map1.get("unid"));
                }catch(Exception e){
                    logger.error("更新病人的有效性标志失败【unid:"+map1.get("unid")+"】:"+e.toString());
                }
            }        
            
            List<Map<String, Object>> list3 = null;
            try{
                list3 = jdbcTemplate.queryForList(" select ch.check_date,cv.* from chk_con_bak ch,chk_valu_bak cv where ch.unid=cv.pkunid and ltrim(rtrim(isnull(cv.itemvalue,'')))<>'' and isnull(cv.Reserve6,0)<>1 ");
            }catch(Exception e){            
                logger.error("获取待导入Redis列表(病人检查项目及结果)失败:"+e.toString());
            }
            
            for(Map<String, Object> map3 : list3) {
                    
                //Set
                shardedJedis.sadd("PatientItemList_Unid:"+map3.get("pkunid"), map3.get("valueid").toString());
    
                //Hash
                //shardedJedis.hset("Chk_Valu_Unid:"+map3.get("valueid"), "itemid", null==map3.get("itemid")?"":map3.get("itemid").toString());
                //shardedJedis.hset("Chk_Valu_Unid:"+map3.get("valueid"), "Name", null==map3.get("Name")?"":map3.get("Name").toString());
                //shardedJedis.hset("Chk_Valu_Unid:"+map3.get("valueid"), "english_name", null==map3.get("english_name")?"":map3.get("english_name").toString());
                //shardedJedis.hset("Chk_Valu_Unid:"+map3.get("valueid"), "itemvalue", null==map3.get("itemvalue")?"":map3.get("itemvalue").toString());
                //shardedJedis.hset("Chk_Valu_Unid:"+map3.get("valueid"), "Unit", null==map3.get("Unit")?"":map3.get("Unit").toString());
                //shardedJedis.hset("Chk_Valu_Unid:"+map3.get("valueid"), "Min_value", null==map3.get("Min_value")?"":map3.get("Min_value").toString());
                //shardedJedis.hset("Chk_Valu_Unid:"+map3.get("valueid"), "Max_value", null==map3.get("Max_value")?"":map3.get("Max_value").toString());
                
                Map<String,String> map33 = new HashMap<>();
                
                map33.put("itemid", null==map3.get("itemid")?"":map3.get("itemid").toString());
                map33.put("Name", null==map3.get("Name")?"":map3.get("Name").toString());
                map33.put("english_name", null==map3.get("english_name")?"":map3.get("english_name").toString());
                map33.put("itemvalue", null==map3.get("itemvalue")?"":map3.get("itemvalue").toString());
                map33.put("Unit", null==map3.get("Unit")?"":map3.get("Unit").toString());
                map33.put("Min_value", null==map3.get("Min_value")?"":map3.get("Min_value").toString());
                map33.put("Max_value", null==map3.get("Max_value")?"":map3.get("Max_value").toString());
                
                shardedJedis.hmset("Chk_Valu_Unid:"+map3.get("valueid"), map33);
                
                //设置过期时间begin
                Date date1 = new Date();
                if(null!=map3.get("check_date")){
                    try {
                        date1=dateFormat.parse(map3.get("check_date").toString());
                    } catch (ParseException e) {
                        logger.error("JobLis2Redis,日期转换失败:"+e.toString());
                    }
                }
                
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(date1); 
                calendar.add(Calendar.DATE, Constants.EXPIRE_DAYS);
                
                Date date2 = calendar.getTime();
                
                String date2_unix = null;
                try {
                    date2_unix = String.valueOf(dateFormat.parse(dateFormat.format(date2)).getTime() / 1000);
                } catch (ParseException e) {
                    logger.error("JobLis2Redis,失效日期转换unix时间戳失败:"+e.toString());
                }
    
                shardedJedis.expireAt("PatientItemList_Unid:"+map3.get("pkunid"), Long.parseLong(date2_unix));
                shardedJedis.expireAt("Chk_Valu_Unid:"+map3.get("valueid"), Long.parseLong(date2_unix));
                //设置过期时间end
                                
                try{
                    jdbcTemplate.update("update chk_valu_bak set Reserve6=1 where valueid="+map3.get("valueid"));
                }catch(Exception e){
                    logger.error("更新病人结果的有效性标志失败【valueid:"+map3.get("valueid")+"】:"+e.toString());
                }
            }
            
        }catch(Exception e){
            //曾经出现:redis.clients.jedis.exceptions.JedisConnectionException: java.net.SocketTimeoutException: connect timed out
            //故保险起见,还是在最外层加上try catch,以便观察未知的报错
            logger.error("JobLis2Redis.execute失败:"+e.toString());
        }finally{
            //将Jedis对象归还给连接池.否则,ShardedJedis.getResource()次数超过redis.pool.maxTotal后将报错:Could not get a resource from the pool
            shardedJedis.close();
        }
    }
}
