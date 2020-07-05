package com.yklis.schedule.util;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.yklis.schedule.entity.CommCodeEntity;
import com.yklis.schedule.jobtype.JobTypeClass;
import com.yklis.schedule.jobtype.JobTypeDataBase;
import com.yklis.schedule.jobtype.JobTypeStrategy;

/**
 * 
 * 见类AddTaskObserverServiceImpl，代码JobBuilder.newJob(QuartzJobFactory.class).withIdentity(jobKey).build();
 * 上述代码自定义任务，到达触发时间就会执行本类的execute方法
 * 
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
               
		//策略模式.策略选择begin
		JobTypeStrategy jobTypeStrategy = null;
		
		switch(commCodeEntity.getReserve()){
		case "Class":
			
			jobTypeStrategy = new JobTypeClass(context);
	        break;
		case "DataBase":
			
			jobTypeStrategy = new JobTypeDataBase(context);
			break;
		/*case "*****":
			//新增策略
			jobTypeStrategy = new JobType*****(context);
			break;*/	
		default:
			
			logger.error("无效任务类型:"+commCodeEntity.getReserve());
			break;
		}
		
		if(null!=jobTypeStrategy) jobTypeStrategy.jobTypeMethod();
		//策略模式.策略选择end
	}
}
