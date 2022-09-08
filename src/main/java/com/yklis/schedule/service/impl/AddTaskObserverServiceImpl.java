package com.yklis.schedule.service.impl;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yklis.schedule.entity.CommCodeEntity;
import com.yklis.schedule.entity.TaskOperateTypeEntity;
import com.yklis.schedule.service.TaskObserverService;
import com.yklis.schedule.service.TaskSubjectService;
import com.yklis.schedule.util.Constants;
import com.yklis.schedule.util.GlobalScheduler;
import com.yklis.schedule.util.QuartzJobFactory;

/**
 * 观察者模式
 * 观察者实现类
 * 观察者模式为1对多的关系:一个主题对应多个观察者
 * 通知时,所有观察者都会收到通知
 * @author liuying
 *
 */
public class AddTaskObserverServiceImpl implements TaskObserverService {

    //配置容器起动时候加载log4j配置文件
    //只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
    //在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
    //如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
    //PropertyConfigurator.configure("log4jj.properties");
    private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public AddTaskObserverServiceImpl(TaskSubjectService taskSubjectService){
		
		taskSubjectService.registerObserver(this);
	}
	
	@Override
	public void update(TaskOperateTypeEntity operateType,String strJobKey,CommCodeEntity commCodeEntity) {
		
		if(!TaskOperateTypeEntity.ADD.equals(operateType))return;
		
        if(null == commCodeEntity)return;
		
		Scheduler scheduler = GlobalScheduler.getScheduler();
		if(null == scheduler){
			logger.warn("scheduler为空");
	        return;
		}
					
        //JobKey.jobKey方法的作用:通过一个字符串生成quartz认识的jobKey
		JobKey jobKey = JobKey.jobKey(commCodeEntity.getId(), Constants.DEFAULT_JOB_GROUP);

		JobDetail jobDetail = JobBuilder.newJob(QuartzJobFactory.class).withIdentity(jobKey).build();
		jobDetail.getJobDataMap().put(Constants.JOB_DATA_MAP_KEY, commCodeEntity);
 
        //TriggerKey.triggerKey方法的作用:通过一个字符串生成quartz认识的triggerKey
        TriggerKey triggerKey = TriggerKey.triggerKey(commCodeEntity.getId(), Constants.DEFAULT_JOB_GROUP);
         
		//cron表达式调度构建器
		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(commCodeEntity.getReserve3());
 
		//按新的cronExpression表达式构建一个新的trigger
		CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
 
		try {
			scheduler.scheduleJob(jobDetail, trigger);
            logger.info("增加任务:" + commCodeEntity.getName());
		} catch (SchedulerException e) {
			logger.error("scheduleJob创建定时任务失败:"+e.toString());
		}
	}
}
