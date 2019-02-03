package com.yklis.schedule.business.job;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import com.yklis.schedule.entity.CommCodeEntity;
import com.yklis.schedule.service.JobDownloadBillService;
import com.yklis.schedule.util.CustomerContextHolder;
import com.yklis.schedule.util.SpringUtils;

public class JobDownloadBill {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//JAVA规定，如果类中没有定义任何构造函数，JVM自动为其生成一个默认的构造函数
    //故可不需要手动写下面的构造函数
	//public JobDownloadBill(){		
	//}
	
	public void execute(Map<String,Object> map) {	     			
				
        JobDownloadBillService jobDownloadBillService= SpringUtils.getBean(JobDownloadBillService.class);
        
        //切换数据源的变量准备工作start
		String selfClassName = this.getClass().getName();		
		int jdbcUnid = CustomerContextHolder.getJdbcUnidFromJobClass(selfClassName);				
		CommCodeEntity commCodeEntity = CustomerContextHolder.getConnectionInfo(jdbcUnid);
		Map<String,Object> customerTypeMap = new HashMap<String,Object>();
		if(commCodeEntity!=null){
			customerTypeMap.put("driverClass", commCodeEntity.getReserve());
			customerTypeMap.put("url", commCodeEntity.getReserve2());
			customerTypeMap.put("user", commCodeEntity.getReserve3());
			customerTypeMap.put("password", commCodeEntity.getReserve4());
		}
        //切换数据源的变量准备工作stop
			
		try{			
			if(!customerTypeMap.isEmpty()) CustomerContextHolder.setCustomerType(customerTypeMap);						
				
	        JdbcTemplate jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
			final String strQuery = "select title from Employees where employeeid=1";
			String northwind = jdbcTemplate.queryForObject(strQuery,String.class);
			logger.info("selectNorthwindString值:" + northwind);
		}catch(Exception e){
			logger.error("切换数据源，执行出错:" + e.toString());
		}finally{
			CustomerContextHolder.clearCustomerType();
		}	
		
		jobDownloadBillService.downloadBill();

	}
}
