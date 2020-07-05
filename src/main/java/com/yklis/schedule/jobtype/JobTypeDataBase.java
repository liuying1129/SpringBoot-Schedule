package com.yklis.schedule.jobtype;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.fastjson.JSON;
import com.yklis.schedule.config.CustomerContextHolder;
import com.yklis.schedule.entity.CommCodeEntity;
import com.yklis.schedule.util.Constants;
import com.yklis.schedule.util.SpringUtils;

/**
 * 策略模式
 * 定义具体的策略
 * DataBase类型任务的具体策略
 * 
 * @author liuyi
 *
 */
public class JobTypeDataBase implements JobTypeStrategy {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private JobExecutionContext context;

    public JobTypeDataBase(JobExecutionContext context) {
    	
    	this.context = context;
    }

	@Override
	public void jobTypeMethod() {
		
		CommCodeEntity commCodeEntity = (CommCodeEntity)context.getMergedJobDataMap().get(Constants.JOB_DATA_MAP_KEY);

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
			CustomerContextHolder.setCustomerType(customerTypeMap);
				
			JdbcTemplate jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
	        
            if((commCodeEntity.getReserve2().toLowerCase().indexOf("insert ")>=0)
                ||(commCodeEntity.getReserve2().toLowerCase().indexOf("delete ")>=0)
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
			CustomerContextHolder.clearCustomerType();
		}
	}
}
