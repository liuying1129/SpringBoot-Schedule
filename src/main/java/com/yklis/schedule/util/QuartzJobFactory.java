package com.yklis.schedule.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSON;
import com.yklis.schedule.entity.CommCodeEntity;

/**
 * 命令模式
 * 命令执行类
 * 
 * 自定义任务到达触发时间就会执行execute
 * @author ying07.liu
 *
 */
//该注解表示等待该job执行完后才会执行下一次
@DisallowConcurrentExecution
public class QuartzJobFactory implements Job {
	
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		CommCodeEntity commCodeEntity = (CommCodeEntity)context.getMergedJobDataMap().get(Constants.JOB_DATA_MAP_KEY);
                
		switch(commCodeEntity.getReserve()){
		case "Class":
			
	        Class<?> ownerClass = null;
			try {
				ownerClass = Class.forName(commCodeEntity.getReserve2());
			} catch (ClassNotFoundException e) {
				logger.error("Class.forName报错:"+e.toString());
				return;
			}
	        Object owner = null;
			try {
				owner = ownerClass.newInstance();
			} 
			/*
			 * InstantiationException | IllegalAccessException
			 * 实现类JobLis2Redis,成员变量private ShardedJedis shardedJedis = shardedJedisPool.getResource();获取不到连接时的报错用上述异常类无法捕获
			 * 故使用Exception
			 */
			catch (Exception e) {
				logger.error("newInstance报错:"+e.toString());
				return;
			}
			
			Class<?>[] params = { Map.class };
			Map<String,Object> map = new HashMap<>();
			map.put("k1", "abcdefg");
			Object[] paramValues = { map };
					
	        Method method = null;
			try {
				//命令类都实现interface Command,故方法名固定为execute
				method = ownerClass.getMethod("execute",params);//commCodeEntity.getReserve4()
			} catch (NoSuchMethodException | SecurityException e) {
				logger.error("getMethod报错:"+e.toString());
				return;
			}
	        try {
				method.invoke(owner,paramValues);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.error("invoke报错:"+e.toString());
				return;
			}
	        
	        break;
		case "DataBase":
			
	        //切换数据源的变量准备工作start
			//Reserve5在DB中为NULL时返回0,getConnectionInfo不会将0值作为条件进行查询导致业务异常
			CommCodeEntity commCodeEntityJdbc = commCodeEntity.getReserve5()>0?CustomerContextHolder.getConnectionInfo(commCodeEntity.getReserve5()):null;
			
			Map<String,Object> customerTypeMap = new HashMap<>();
			if(commCodeEntityJdbc!=null){
				customerTypeMap.put("driverClass", commCodeEntityJdbc.getReserve());
				customerTypeMap.put("url", commCodeEntityJdbc.getReserve2());
				customerTypeMap.put("user", commCodeEntityJdbc.getReserve3());
				customerTypeMap.put("password", commCodeEntityJdbc.getReserve4());
			}
	        //切换数据源的变量准备工作stop
			
			try{			
				//CustomerContextHolder.setCustomerType(customerTypeMap);
					
		        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
		        JdbcTemplate jdbcTemplate = webApplicationContext.getBean(JdbcTemplate.class);
		        
	            if((commCodeEntity.getReserve2().toLowerCase().indexOf("insert ")>=0)
	                ||(commCodeEntity.getReserve2().toLowerCase().indexOf("update ")>=0)){
	                
                    int execResult = jdbcTemplate.update(commCodeEntity.getReserve2());
                    logger.info("DataBase任务["+context.getJobDetail().getKey().getName()+"]的结果值:" + JSON.toJSONString(execResult));
	            }else{
	                
                    List<Map<String, Object>> selectResult = jdbcTemplate.queryForList(commCodeEntity.getReserve2());
                    logger.info("DataBase任务["+context.getJobDetail().getKey().getName()+"]的结果值:" + JSON.toJSONStringWithDateFormat(selectResult, "yyyy-MM-dd HH:mm:ss"));
	            }
			}catch(Exception e){
				logger.error("切换数据源，执行出错:" + e.toString());
				return;
			}finally{
				//CustomerContextHolder.clearCustomerType();
			}
			
			break;
		default:
			
			logger.error("无效任务类型:"+commCodeEntity.getReserve());
			
			break;
		}
	}

}
