package com.yklis.schedule.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.quartz.CronTrigger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yklis.schedule.dao.CommCodeDao;
import com.yklis.schedule.entity.CommCodeEntity;
import com.yklis.schedule.entity.TaskOperateTypeEntity;
import com.yklis.schedule.service.impl.AddTaskObserverServiceImpl;
import com.yklis.schedule.service.impl.DeleteTaskObserverServiceImpl;
import com.yklis.schedule.service.impl.TaskSubjectServiceImpl;
import com.yklis.schedule.service.impl.UpdateTaskObserverServiceImpl;
import com.yklis.schedule.util.Constants;
import com.yklis.schedule.util.SpringUtil;

/**
 * 观察者模式,应用类
 * 
 * 该job配置在quartz_jobs.xml，由系统启动
 * 该JOB的作用：根据数据库中的任务配置通知观察者动态生成其他业务需要的JOB
 * @author ying07.liu
 *
 */
//该注解表示等待该job执行完后才会执行下一次
@DisallowConcurrentExecution
public class JobRefreshTask implements Job {
	
    //配置容器起动时候加载log4j配置文件
    //只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
    //在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
    //如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
    //PropertyConfigurator.configure("log4jj.properties");
    private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private CommCodeDao commCodeDao = SpringUtil.getBean(CommCodeDao.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		             				
		//查询调度器中所有的Job start
		List<Map<String, Object>> quartzJoblist = new ArrayList<>();
		
		GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
		Set<JobKey> jobKeys = null;
		try {
			jobKeys = context.getScheduler().getJobKeys(matcher);
		} catch (SchedulerException e) {
			logger.error("getJobKeys报错:"+e.toString());
		}
		
		if(null != jobKeys){
			
			for (JobKey jobKey : jobKeys) {
				
			    List<? extends Trigger> triggers = null;
				try {
					triggers = context.getScheduler().getTriggersOfJob(jobKey);
				} catch (SchedulerException e) {
					logger.error("getTriggersOfJob报错,参数JobKey【"+jobKey+"】,详细信息:"+e.toString());
				}			
				if(null == triggers){
					logger.info("getTriggersOfJob为null");
			        continue;
				}
				
			    for (Trigger trigger : triggers) {
			    	
			    	if(!(trigger instanceof CronTrigger))continue;
			    	
			    	Map<String, Object> quartzJobMap = new HashMap<>();
			    	
			    	CommCodeEntity quartzJob = null;
					try {
						quartzJob = (CommCodeEntity)context.getScheduler().getJobDetail(jobKey).getJobDataMap().get(Constants.JOB_DATA_MAP_KEY);
					} catch (Exception e) {//SchedulerException
						logger.error("getJobDetail报错,jobKey="+jobKey.toString()+":"+e.toString());					
					}			
			    	if(null!=quartzJob){
	                    quartzJobMap.put("jobName", quartzJob.getName());
			    		quartzJobMap.put("jobDataMapTaskContentCode", quartzJob.getReserve5());
			    	}
                    quartzJobMap.put("jobDataMapTaskConfigCode", jobKey.getName());
			    	
				    CronTrigger cronTrigger = (CronTrigger) trigger;
				    quartzJobMap.put("jobTriggerCron", cronTrigger.getCronExpression());
			    					    	
				    quartzJoblist.add(quartzJobMap);
			    }
			}
		}
		//查询调度器中所有的Job stop				

		//查询数据库配置的所有Job start
		CommCodeEntity commCodeEntity = new CommCodeEntity();
		commCodeEntity.setTypeName("定时任务");
		commCodeEntity.setReserve6(1);
		List<CommCodeEntity> dbJobList = null;
		try {
		dbJobList = commCodeDao.selectCommCode(commCodeEntity);
		logger.info("Mybatis commCodeDao查询成功111111111");
	} catch (Exception e) {
		logger.error("Mybatis commCodeDao查询出错22222222"+e.toString());					
	}			
		//查询数据库配置的所有Job stop
				
		
		/**
		 * 通知观察者进行增、删、改
		 * 删:quartzJoblist中有, dbJobList中没有
		 * 改:quartzJoblist、dbJobList都有,但值有修改
		 * 增:quartzJoblist中没有,dbJobList中有
		 */				
		//删
		if((null==dbJobList)||(dbJobList.isEmpty())){
							
			TaskSubjectServiceImpl taskSubjectServiceImpl = new TaskSubjectServiceImpl();
			//注册观察者
			new AddTaskObserverServiceImpl(taskSubjectServiceImpl);
			new DeleteTaskObserverServiceImpl(taskSubjectServiceImpl);
			new UpdateTaskObserverServiceImpl(taskSubjectServiceImpl);
			
			//通知观察者
		    taskSubjectServiceImpl.setTaskInfo(TaskOperateTypeEntity.DELETE_ALL,null, null);
			
			return;
		}
		
		for (Map<String, Object> map2 : quartzJoblist) {
			
		    List<CommCodeEntity> dbJobList2 = dbJobList.stream()
		            //true:保留元素
		            .filter(x -> map2.get("jobDataMapTaskConfigCode").equals(x.getId()))
		            .collect(Collectors.toList());
		    
			
		    if((null==dbJobList2)||(dbJobList2.isEmpty())){
		    	
				TaskSubjectServiceImpl taskSubjectServiceImpl = new TaskSubjectServiceImpl();
				//注册观察者
				new AddTaskObserverServiceImpl(taskSubjectServiceImpl);
				new DeleteTaskObserverServiceImpl(taskSubjectServiceImpl);
				new UpdateTaskObserverServiceImpl(taskSubjectServiceImpl);
				
		    	//删				
				//通知观察者
				taskSubjectServiceImpl.setTaskInfo(TaskOperateTypeEntity.DELETE,map2.get("jobDataMapTaskConfigCode").toString(), null);
			}else{
				
				//改
                if(!map2.get("jobTriggerCron").equals(dbJobList2.get(0).getReserve3())){
                    
            		TaskSubjectServiceImpl taskSubjectServiceImpl = new TaskSubjectServiceImpl();
            		//注册观察者
            		new AddTaskObserverServiceImpl(taskSubjectServiceImpl);
            		new DeleteTaskObserverServiceImpl(taskSubjectServiceImpl);
            		new UpdateTaskObserverServiceImpl(taskSubjectServiceImpl);
            		
                    //通知观察者
                    taskSubjectServiceImpl.setTaskInfo(TaskOperateTypeEntity.UPDATE,null, dbJobList2.get(0));
                }
			}
		}
		
		for (CommCodeEntity commCodeEntity3 : dbJobList){
		    
		    boolean b = quartzJoblist.stream()
		            .noneMatch(x -> x.get("jobDataMapTaskConfigCode").equals(commCodeEntity3.getId()));			
				
			//增
		    if(b) {

				TaskSubjectServiceImpl taskSubjectServiceImpl = new TaskSubjectServiceImpl();
				//注册观察者
				new AddTaskObserverServiceImpl(taskSubjectServiceImpl);
				new DeleteTaskObserverServiceImpl(taskSubjectServiceImpl);
				new UpdateTaskObserverServiceImpl(taskSubjectServiceImpl);
				
				//通知观察者
				taskSubjectServiceImpl.setTaskInfo(TaskOperateTypeEntity.ADD,null, commCodeEntity3);
			}
		}
	}
}