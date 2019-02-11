package com.yklis.schedule.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import com.yklis.schedule.entity.CommCodeEntity;
import com.yklis.schedule.util.Constants;
import com.yklis.schedule.util.MySingleton;

@Controller
@RequestMapping("/") 
public class HomeController {
	
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //该请求默认跳转到欢迎页。用该Ctroller重定向
    //@RequestMapping("/")
    //public String abc(HttpServletRequest request) {
    	
    //	return "index";
    //}
    
    @RequestMapping("index")
    public ModelAndView handleIndexPageRequest(HttpServletRequest request) {
    	
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	    	
		MySingleton mySingleton = MySingleton.getInstance();
		Scheduler scheduler = mySingleton.getScheduler();
		if(null == scheduler){
			logger.warn("scheduler为空");
	        //return new ModelAndView("index", null);//to do
		}
		
		ModelAndView mv = new ModelAndView();
		mv.setViewName("index");
		
	    try{
			//查询所有Job start
	    	List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
	    	
			GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
			Set<JobKey> jobKeys = null;
			try {
				jobKeys = scheduler.getJobKeys(matcher);
			} catch (SchedulerException e) {
				logger.error("getJobKeys报错:"+e.toString());
			}
			if(null != jobKeys){
				for (JobKey jobKey : jobKeys) {
															
				    List<? extends Trigger> triggers = null;
					try {
						triggers = scheduler.getTriggersOfJob(jobKey);
					} catch (SchedulerException e) {
						logger.error("getTriggersOfJob报错:"+e.toString());
				        //return new ModelAndView("index", null);//to do
					}
					if(null == triggers){
						logger.info("getTriggersOfJob为null");
				        continue;
					}
				    for (Trigger trigger : triggers) {
				    	
				    	Map<String, Object> map = new HashMap<String, Object>();
				    			    	
				    	CommCodeEntity quartzJob = null;
						try {
							quartzJob = (CommCodeEntity)scheduler.getJobDetail(jobKey).getJobDataMap().get(Constants.JOB_DATA_MAP_KEY);
						} catch (Exception e) {//SchedulerException
							logger.error("getJobDetail报错,jobKey="+jobKey.toString()+":"+e.toString());					
						}			
				    	if(null!=quartzJob){
				    	    map.put("jobName", quartzJob.getName());
					    	map.put("jobDataMapTaskContentCode", quartzJob.getReserve5());
				    	}		    	
				    	
                        map.put("jobDataMapTaskConfigCode", jobKey.getName());
				    	map.put("jobTrigger", trigger.getKey());//DEFAULT.SimpleTriggerOfRefreshJob
				    	map.put("triggerNextFireTime", dateFormat.format(trigger.getNextFireTime()));
				    	map.put("triggerNextFireTime2", trigger.getNextFireTime());//用于排序
				    	if(null == trigger.getPreviousFireTime()){
				    		map.put("triggerPreviousFireTime", null);
				    	}else{
				    		map.put("triggerPreviousFireTime", dateFormat.format(trigger.getPreviousFireTime()));
				    	}		    	
				    	map.put("triggerStartTime", dateFormat.format(trigger.getStartTime()));
				    	
				    	try {
					    	map.put("jobTriggerState", scheduler.getTriggerState(trigger.getKey()).name());
						} catch (SchedulerException e) {
							logger.error("getTriggerState报错:"+e.toString());
						}
				    	
					    if (trigger instanceof CronTrigger) {
					        CronTrigger cronTrigger = (CronTrigger) trigger;
					    	map.put("jobTriggerCron", cronTrigger.getCronExpression());
					    }
				    					    	
				    	list.add(map);
				    }
				}
			}
			//查询所有Job stop
			
			//排序
			Collections.sort(list,new AllJobMapComparator());			
		
			mv.addObject("AllJobList", list);

    	}catch(Exception e){
        	logger.error("查询所有Job出错:"+e.toString());
    	}
			
	    try{
			//查询运行中的Job start
	    	List<Map<String, Object>> listRun = new ArrayList<Map<String,Object>>();
	    	    			
			List<JobExecutionContext> executingJobs = null;
			try {
				executingJobs = scheduler.getCurrentlyExecutingJobs();
			} catch (SchedulerException e) {
				logger.error("getCurrentlyExecutingJobs报错:"+e.toString());
			}
			if(null != executingJobs){
				
				for (JobExecutionContext executingJob : executingJobs) {
					
				    JobDetail jobDetail = executingJob.getJobDetail();
				    JobKey jobKey = jobDetail.getKey();
				    Trigger trigger = executingJob.getTrigger();
				    
			    	Map<String, Object> map = new HashMap<String, Object>();
			    	
			    	CommCodeEntity quartzJob = null;
			    	try{	    		
			    		quartzJob = (CommCodeEntity)executingJob.getMergedJobDataMap().get(Constants.JOB_DATA_MAP_KEY);			
			    	}catch(Exception e){
			    		logger.error("任务["+jobKey.getName()+"]getMergedJobDataMap报错:"+e.toString());
			    	}
			    	if(null!=quartzJob){
					    map.put("jobDataMapDatasource", quartzJob.getReserve5());	    			
				    	map.put("jobDataMapTaskDesc", quartzJob.getRemark());
				    	map.put("jobName", quartzJob.getName());
				    	map.put("jobDataMapTaskContentCode", quartzJob.getReserve5());
			    	}
			    	
                    map.put("jobDataMapTaskConfigCode", jobKey.getName());
			    	//map.put("jobGroup", jobKey.getGroup());
			    	//map.put("jobTrigger", trigger.getKey());//DEFAULT.SimpleTriggerOfRefreshJob
			    	//map.put("triggerCalendarName", trigger.getCalendarName());
			    	//map.put("triggerEndTime", trigger.getEndTime());
			    	//map.put("triggerFinalFireTime", trigger.getFinalFireTime());
			    	//map.put("triggerMisfireInstruction", trigger.getMisfireInstruction());
			    	map.put("triggerNextFireTime", dateFormat.format(trigger.getNextFireTime()));
			    	if(null == trigger.getPreviousFireTime()){
			    		map.put("triggerPreviousFireTime", null);
			    	}else{
			    		map.put("triggerPreviousFireTime", dateFormat.format(trigger.getPreviousFireTime()));
			    	}		    	
			    	//map.put("triggerPriority", trigger.getPriority());
			    	//map.put("triggerStartTime", dateFormat.format(trigger.getStartTime()));
			    	
			    	//实际开始执行时间
			    	if(null == executingJob.getFireTime()){
			    		map.put("executingJobFireTime", null);
			    	}else{
			    		map.put("executingJobFireTime", dateFormat.format(executingJob.getFireTime()));
			    	}
			    	
			    	//运行时长
			    	//map.put("executingJobRunTime", executingJob.getJobRunTime());//总为-1	
			    	Date now = new Date();
			    	long diffSeconds = 0;
			    	//毫秒ms
		            long diff = now.getTime() - executingJob.getFireTime().getTime();
		            diffSeconds = diff / 1000 ;
		            //long diffMinutes = diff / (60 * 1000) % 60;
		            //long diffHours = diff / (60 * 60 * 1000) % 24;
		            //long diffDays = diff / (24 * 60 * 60 * 1000);		    		
			    	map.put("executingJobRunTime", diffSeconds);
			    	
					try {
				    	map.put("jobTriggerState", scheduler.getTriggerState(trigger.getKey()).name());
					} catch (SchedulerException e) {
						logger.error("getTriggerState报错:"+e.toString());
					}
				    if (trigger instanceof CronTrigger) {
				        CronTrigger cronTrigger = (CronTrigger) trigger;
				    	map.put("jobTriggerCron", cronTrigger.getCronExpression());
				    }
				    
				    listRun.add(map);
				}
			}
			//查询运行中的Job stop*/
			
			//排序
			Collections.sort(listRun,new RunJobMapComparator());
	
			mv.addObject("RunJobList", listRun);
			
	    }catch(Exception e){
        	logger.error("查询运行中的Job出错:"+e.toString());
	    }			
			
		return mv;
    }
    
    static class RunJobMapComparator implements Comparator<Map<String, Object>> {
    	 
    	private Logger logger = LoggerFactory.getLogger(this.getClass());
    	
        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {

        	try{
	            String b1 = o1.get("executingJobRunTime").toString();
	            String b2 = o2.get("executingJobRunTime").toString();
	            
	            int i1 = Integer.parseInt(b1);
	            int i2 = Integer.parseInt(b2);
	            
	            //升、倒序在这里控制
	            if (i1 > i2) {
	                return -1;
	            } else if (i1 < i2) {
	                return 1;
	            } else {
	                return 0;
	            }
            }catch(Exception e){
            	logger.error("任务1["+o1.get("jobName")+"],任务2["+o2.get("jobName")+"]比较运行时长出错:"+e.toString());
            	return 0;
            }
        }
 
    }
    
    static class AllJobMapComparator implements Comparator<Map<String, Object>> {
   	 
    	private Logger logger = LoggerFactory.getLogger(this.getClass());
    	
        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {

        	try{
	            Date b1 = (Date) o1.get("triggerNextFireTime2");
	            Date b2 = (Date) o2.get("triggerNextFireTime2");
	                        
	            if((null == b1)||(null == b2)){
	            	return 0;
	            }
	            
	            //升、倒序在这里控制
	            if (b1.after(b2)) {
	                return 1;
	            } else if (b1.before(b2)) {
	                return -1;
	            } else {
	                return 0;
	            }
        	}catch(Exception e){
            	logger.error("任务1["+o1.get("jobName")+"],任务2["+o2.get("jobName")+"]比较下次运行时间出错:"+e.toString());
            	return 0;
        	}
        }
 
    }
}
