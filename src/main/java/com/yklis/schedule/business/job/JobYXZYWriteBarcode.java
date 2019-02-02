package com.yklis.schedule.business.job;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import com.yklis.schedule.util.SpringUtil;

/**
 * 命令模式
 * 命令实现类
 * 
 * 越秀中医:回写条码任务
 * 越秀中医扫描体检条码慢
 * 将chk_valu_his中的条码号回写到chk_con_his
 * 提高扫描速度
 * @author ying07.liu
 *
 */
public class JobYXZYWriteBarcode implements Command {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    //JAVA规定，如果类中没有定义任何构造函数，JVM自动为其生成一个默认的构造函数
    //故可不需要手动写下面的构造函数
    //public JobSPH2CJ(){       
    //}
    
    @Override
    public void execute(Map<String,Object> map) {
        
        JdbcTemplate jdbcTemplate = SpringUtil.getBean(JdbcTemplate.class);
        
        List<Map<String, Object>> list = null;
        try{
            list = jdbcTemplate.queryForList(" select Unid,TjJianYan from chk_con_his cch WITH(NOLOCK) where cch.check_date+30>getdate() ");
        }catch(Exception e){            
            logger.error("jdbcTemplate.queryForList失败:"+e.toString());
        }
        
        for(Map<String, Object> map1 : list) {

            StringBuilder sb1 = new StringBuilder();
            sb1.append(" select dbo.uf_Peis_Br_Barcode(");
            sb1.append(map1.get("Unid"));
            sb1.append(") ");
            
            String newBarcode = null;
            try{
                newBarcode = jdbcTemplate.queryForObject(sb1.toString(),String.class);
            }catch(Exception e){            
                logger.error("jdbcTemplate.queryForObject失败:"+e.toString());
            }
            
            if(null!=newBarcode&&!"".equals(newBarcode)){
                newBarcode = ","+newBarcode+",";
            }
            if(null==newBarcode){
                newBarcode = "";
            }
            
            String oldBarcode = "";
            if(null!=map1.get("TjJianYan")){
                oldBarcode = map1.get("TjJianYan").toString();
            }
                
            if(!newBarcode.equals(oldBarcode)){
                
                StringBuilder sb11 = new StringBuilder();
                sb11.append(" update chk_con_his set TjJianYan='");
                sb11.append(newBarcode);
                sb11.append("' where Unid=");
                sb11.append(map1.get("Unid"));
                
                try{
                    jdbcTemplate.update(sb11.toString());
                }catch(Exception e){            
                    logger.error("jdbcTemplate.update失败:"+e.toString());
                }
            }                                                   
        }
    }
}
