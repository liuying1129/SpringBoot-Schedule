package com.yklis.schedule.service.impl;

import java.util.Set;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yklis.schedule.entity.CommCodeEntity;
import com.yklis.schedule.entity.TaskOperateTypeEntity;
import com.yklis.schedule.service.TaskObserverService;
import com.yklis.schedule.service.TaskSubjectService;
import com.yklis.schedule.util.Constants;
import com.yklis.schedule.util.MySingleton;

/**
 * 观察者模式
 * 观察者实现类
 * 观察者模式为1对多的关系:一个主题对应多个观察者
 * 通知时,所有观察者都会收到通知
 * @author liuying
 *
 */
public class DeleteTaskObserverServiceImpl implements TaskObserverService {

    //配置容器起动时候加载log4j配置文件
    //只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
    //在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
    //如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
    //PropertyConfigurator.configure("log4jj.properties");
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
	public DeleteTaskObserverServiceImpl(TaskSubjectService taskSubjectService){
		
		taskSubjectService.registerObserver(this);
	}
	
	@Override
	public void update(TaskOperateTypeEntity operateType,String strJobKey, CommCodeEntity commCodeEntity) {
		
        MySingleton mySingleton = MySingleton.getInstance();
        Scheduler scheduler = mySingleton.getScheduler();
        if(null == scheduler){
            logger.warn("scheduler为空");
            return;
        }

        switch(operateType){
	    case DELETE:
	        
	        //JobKey.jobKey方法的作用:通过一个字符串生成quartz认识的jobKey
	        JobKey jobKey = JobKey.jobKey(strJobKey, Constants.DEFAULT_JOB_GROUP);

	        try {
	            scheduler.deleteJob(jobKey);
	            logger.info("移除任务,JobKey:" + jobKey.getName());
	        } catch (SchedulerException e) {
	            logger.error("deleteJob报错:"+e.toString());
	        }

	        break;
	    case DELETE_ALL:
	        
			//GroupMatcher.anyJobGroup()方法匹配到所有组的job,包含了下面的两个jobKey
			//GroupOfJobRefreshTask.JobRefreshTask
			//JobSchedulingDataLoaderPlugin.JobSchedulingDataLoaderPlugin_jobInitializer_quartz_jobs_xml
			//GroupOfJobRefreshTask、JobSchedulingDataLoaderPlugin为JobGroup
			//这两个job是本程序的守护job,不能删除，故使用GroupMatcher.jobGroupEquals方法
            //JobSchedulingDataLoaderPlugin_jobInitializer_quartz_jobs_xml是quartz自带的检测quartz_jobs.xml的job
			GroupMatcher<JobKey> matcher = GroupMatcher.jobGroupEquals(Constants.DEFAULT_JOB_GROUP);
	        Set<JobKey> jobKeys = null;
	        try {
	            jobKeys = scheduler.getJobKeys(matcher);
	        } catch (SchedulerException e) {
	            logger.error("getJobKeys报错:"+e.toString());
	        }
	        for (JobKey jobKey22 : jobKeys) {
	            
                try {
                    scheduler.deleteJob(jobKey22);
                    logger.info("移除任务,JobKey:" + jobKey22.getName());
                } catch (SchedulerException e) {
                    logger.error("deleteJob报错:"+e.toString());
                }
	        }
	        
	        break;
	        
        default:
            break;
	    }
	}
}
