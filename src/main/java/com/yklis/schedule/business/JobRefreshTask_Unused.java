package com.yklis.schedule.business;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.yklis.schedule.dao.CommCodeDao;
import com.yklis.schedule.entity.CommCodeEntity;
import com.yklis.schedule.util.Constants;
import com.yklis.schedule.util.QuartzJobFactory;

/**
 * 该job配置在quartz_jobs.xml，由系统启动
 * 该JOB的作用：根据数据库中的任务配置动态生成其他业务需要的JOB
 * @author ying07.liu
 *
 *该类暂不用了.JobRefreshTask代替了该类,使用观察者模式
 */
//该注解表示等待该job执行完后才会执行下一次
@DisallowConcurrentExecution
public class JobRefreshTask_Unused implements Job {
	
    //配置容器起动时候加载log4j配置文件
    //只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
    //在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
    //如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
    //PropertyConfigurator.configure("log4jj.properties");
    private Logger logger = Logger.getLogger(this.getClass());
		
	private WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();		
	private CommCodeDao commCodeDao = webApplicationContext.getBean(CommCodeDao.class);			
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {	
		             				
		//查询调度器中所有的Job start
		GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
		Set<JobKey> jobKeys = null;
		try {
			jobKeys = context.getScheduler().getJobKeys(matcher);//StartupServlet.scheduler
		} catch (SchedulerException e) {
			logger.error("getJobKeys报错:"+e.toString());
		}
		for (JobKey jobKey : jobKeys) {
			
			//删除未配置或停用的job start
			if(
				  (!"JobRefreshTask".equalsIgnoreCase(jobKey.getName()))
				&&(!"JobSchedulingDataLoaderPlugin_jobInitializer_quartz_jobs_xml".equalsIgnoreCase(jobKey.getName()))
			){
				//JobRefreshTask是本身这个job
				//JobSchedulingDataLoaderPlugin_jobInitializer_quartz_jobs_xml是检测quartz_jobs。xml的job
				CommCodeEntity commCodeEntity = new CommCodeEntity();
				commCodeEntity.setTypeName("定时任务");
				commCodeEntity.setName(jobKey.getName());
				commCodeEntity.setReserve6(1);
				List<CommCodeEntity> commCodeEntityList = commCodeDao.selectCommCode(commCodeEntity);
				if((null == commCodeEntityList)||(commCodeEntityList.isEmpty())){
	            	try {
	            		context.getScheduler().deleteJob(jobKey);
		                logger.info("移除任务:" + jobKey.getName());
					} catch (SchedulerException e) {
						logger.error("deleteJob报错:"+e.toString());
					}
				}
			}
			//删除未配置或停用的job stop
			
			//更新定时设置 start
		    List<? extends Trigger> triggers = null;
			try {
				triggers = context.getScheduler().getTriggersOfJob(jobKey);
			} catch (SchedulerException e) {
				logger.error("getTriggersOfJob报错,参数JobKey【"+jobKey+"】,详细信息:"+e.toString());
			}
		    for (Trigger trigger : triggers) {
		    	
		    	if(!(trigger instanceof CronTrigger))continue;
		    	
	            CronTrigger cronTrigger = (CronTrigger) trigger;
				CommCodeEntity commCodeEntity2 = new CommCodeEntity();
				commCodeEntity2.setTypeName("定时任务");
				commCodeEntity2.setName(jobKey.getName());
				commCodeEntity2.setReserve6(1);
				List<CommCodeEntity> commCodeEntityList2 = commCodeDao.selectCommCode(commCodeEntity2);
				if(
						(commCodeEntityList2 != null)
						&&(!commCodeEntityList2.isEmpty())
						&&(cronTrigger.getCronExpression() != null)
						&&(!cronTrigger.getCronExpression().equals(commCodeEntityList2.get(0).getReserve3()))
				){
					//表达式调度构建器
					CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(commCodeEntityList2.get(0).getReserve3());
			 
					//按新的cronExpression表达式重新构建trigger
					cronTrigger = cronTrigger.getTriggerBuilder().withIdentity(trigger.getKey()).withSchedule(scheduleBuilder).build();
			 
					//按新的trigger重新设置job执行
					try {
						context.getScheduler().rescheduleJob(trigger.getKey(), cronTrigger);
		                logger.info("更新任务时间:" + jobKey.getName());			
					} catch (SchedulerException e) {					
						logger.error("rescheduleJob失败:"+e.toString());
					}
				}
		    }
			//更新定时设置 stop
		}
		//查询调度器中所有的Job stop		
		
		//增加job start		
		CommCodeEntity commCodeEntity = new CommCodeEntity();
		commCodeEntity.setTypeName("定时任务");
		commCodeEntity.setReserve6(1);
		List<CommCodeEntity> commCodeEntityList = commCodeDao.selectCommCode(commCodeEntity);
		
		if(commCodeEntityList == null) return;
		if(commCodeEntityList.isEmpty()) return;
		
		for (CommCodeEntity job : commCodeEntityList) {
			
			TriggerKey triggerKey = TriggerKey.triggerKey(job.getName(), Constants.DEFAULT_JOB_GROUP);
			JobKey jobKey = JobKey.jobKey(job.getName(), Constants.DEFAULT_JOB_GROUP);
			 
			CronTrigger trigger = null;
			try {
				//根据triggerKey获取调度器中的trigger
				trigger = (CronTrigger) context.getScheduler().getTrigger(triggerKey);
			} catch (SchedulerException e) {
				logger.error("通过指定triggerKey获取Trigger报错:"+e.toString());
			}
						 
			if (null == trigger) {
				JobDetail jobDetail = JobBuilder.newJob(QuartzJobFactory.class).withIdentity(jobKey).build();
				jobDetail.getJobDataMap().put(Constants.JOB_DATA_MAP_KEY, job);
		 
				//cron表达式调度构建器
				CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getReserve3());
		 
				//按新的cronExpression表达式构建一个新的trigger
				trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
		 
				try {
					context.getScheduler().scheduleJob(jobDetail, trigger);
	                logger.info("增加任务:" + jobKey.getName());			
				} catch (SchedulerException e) {					
					logger.error("scheduleJob创建定时任务失败:"+e.toString());
				}
			}			
		}
		//增加job stop*/
	}

}
