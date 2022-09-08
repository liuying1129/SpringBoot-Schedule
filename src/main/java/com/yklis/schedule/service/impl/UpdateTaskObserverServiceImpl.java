package com.yklis.schedule.service.impl;

import java.util.List;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yklis.schedule.entity.CommCodeEntity;
import com.yklis.schedule.entity.TaskOperateTypeEntity;
import com.yklis.schedule.service.TaskObserverService;
import com.yklis.schedule.service.TaskSubjectService;
import com.yklis.schedule.util.Constants;
import com.yklis.schedule.util.GlobalScheduler;

/**
 * 观察者模式
 * 观察者实现类
 * 观察者模式为1对多的关系:一个主题对应多个观察者
 * 通知时,所有观察者都会收到通知
 * @author liuying
 *
 */
public class UpdateTaskObserverServiceImpl implements TaskObserverService {

    //配置容器起动时候加载log4j配置文件
    //只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
    //在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
    //如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
    //PropertyConfigurator.configure("log4jj.properties");
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
	public UpdateTaskObserverServiceImpl(TaskSubjectService taskSubjectService){
		
		taskSubjectService.registerObserver(this);
	}

	@Override
	public void update(TaskOperateTypeEntity operateType,String strJobKey, CommCodeEntity commCodeEntity) {
		
		if(!TaskOperateTypeEntity.UPDATE.equals(operateType))return;
		
		if(null == commCodeEntity)return;
		
		Scheduler scheduler = GlobalScheduler.getScheduler();
		if(null == scheduler){
			logger.warn("scheduler为空");
	        return;
		}

        //JobKey.jobKey方法的作用:通过一个字符串生成quartz认识的jobKey
        JobKey jobKey = JobKey.jobKey(commCodeEntity.getId(), Constants.DEFAULT_JOB_GROUP);

	    List<? extends Trigger> triggers = null;
		try {
			triggers = scheduler.getTriggersOfJob(jobKey);
		} catch (SchedulerException e) {
			logger.error("getTriggersOfJob报错,参数JobKey【"+jobKey+"】,详细信息:"+e.toString());
			return;
		}
	    for (Trigger trigger : triggers) {
	    	
	    	if(!(trigger instanceof CronTrigger))continue;
	    	
            CronTrigger cronTrigger = (CronTrigger) trigger;
            
			//表达式调度构建器
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(commCodeEntity.getReserve3());
	 
			//按新的cronExpression表达式重新构建trigger
			cronTrigger = cronTrigger.getTriggerBuilder().withIdentity(trigger.getKey()).withSchedule(scheduleBuilder).build();
	 
			//按新的trigger重新设置job执行
			try {
				scheduler.rescheduleJob(trigger.getKey(), cronTrigger);
                logger.info("更新任务时间:" + commCodeEntity.getName());
			} catch (SchedulerException e) {					
				logger.error("rescheduleJob失败:"+e.toString());
			}
	    }
	}
}
