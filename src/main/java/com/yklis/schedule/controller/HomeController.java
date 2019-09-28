package com.yklis.schedule.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSON;
import com.yklis.schedule.entity.CommCodeEntity;
import com.yklis.schedule.util.Constants;
import com.yklis.schedule.util.MySingleton;

@RestController
@RequestMapping("/") 
public class HomeController {
	
    private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
    //该请求默认跳转到欢迎页。用该Ctroller重定向
    //@RequestMapping("/")
    //public String abc(HttpServletRequest request) {
    	
    //	return "index";
    //}
    
    @RequestMapping("queryAllJob")
    public String queryAllJob() {
    	    	
		MySingleton mySingleton = MySingleton.getInstance();
		Scheduler scheduler = mySingleton.getScheduler();
		if(null == scheduler){
			
            Map<String, Object> mapResponse = new HashMap<>();
            mapResponse.put("errorCode", -123);
            mapResponse.put("errorMsg", "scheduler为空");
            
            Map<String, Object> map = new HashMap<>();
            map.put("success", false);
            map.put("response", mapResponse);
            
    		return JSON.toJSONString(map);
		}
		
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
						
			            Map<String, Object> mapResponse = new HashMap<>();
			            mapResponse.put("errorCode", -123);
			            mapResponse.put("errorMsg", "getTriggersOfJob报错:"+e.toString());
			            
			            Map<String, Object> map = new HashMap<>();
			            map.put("success", false);
			            map.put("response", mapResponse);
			            
			    		return JSON.toJSONString(map);
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
				    	map.put("triggerNextFireTime", trigger.getNextFireTime());//dateFormat.format()
				    	map.put("triggerNextFireTime2", trigger.getNextFireTime());//用于排序
				    	if(null == trigger.getPreviousFireTime()){
				    		map.put("triggerPreviousFireTime", null);
				    	}else{
				    		map.put("triggerPreviousFireTime", trigger.getPreviousFireTime());//dateFormat.format()
				    	}		    	
				    	map.put("triggerStartTime", trigger.getStartTime());//dateFormat.format()
				    	
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
		
	        Map<String, Object> map = new HashMap<>();
	        map.put("success", true);
	        map.put("response", list);

	    	return JSON.toJSONStringWithDateFormat(map, "yyyy-MM-dd HH:mm:ss");

    	}catch(Exception e){
        	
            Map<String, Object> mapResponse = new HashMap<>();
            mapResponse.put("errorCode", -123);
            mapResponse.put("errorMsg", "查询所有Job出错:"+e.toString());
            
            Map<String, Object> map = new HashMap<>();
            map.put("success", false);
            map.put("response", mapResponse);
            
    		return JSON.toJSONString(map);
    	}
    }
    
    @RequestMapping("queryRunningJob")
    public String queryRunningJob() {
    	
		MySingleton mySingleton = MySingleton.getInstance();
		Scheduler scheduler = mySingleton.getScheduler();
		if(null == scheduler){
			
            Map<String, Object> mapResponse = new HashMap<>();
            mapResponse.put("errorCode", -123);
            mapResponse.put("errorMsg", "scheduler为空");
            
            Map<String, Object> map = new HashMap<>();
            map.put("success", false);
            map.put("response", mapResponse);
            
    		return JSON.toJSONString(map);
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
			    	map.put("triggerNextFireTime", trigger.getNextFireTime());//dateFormat.format()
			    	if(null == trigger.getPreviousFireTime()){
			    		map.put("triggerPreviousFireTime", null);
			    	}else{
			    		map.put("triggerPreviousFireTime", trigger.getPreviousFireTime());//dateFormat.format()
			    	}		    	
			    	//map.put("triggerPriority", trigger.getPriority());
			    	//map.put("triggerStartTime", dateFormat.format(trigger.getStartTime()));
			    	
			    	//实际开始执行时间
			    	if(null == executingJob.getFireTime()){
			    		map.put("executingJobFireTime", null);
			    	}else{
			    		map.put("executingJobFireTime", executingJob.getFireTime());//dateFormat.format()
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
			
	        Map<String, Object> map = new HashMap<>();
	        map.put("success", true);
	        map.put("response", listRun);

	    	return JSON.toJSONStringWithDateFormat(map, "yyyy-MM-dd HH:mm:ss");
			
	    }catch(Exception e){
        	
            Map<String, Object> mapResponse = new HashMap<>();
            mapResponse.put("errorCode", -123);
            mapResponse.put("errorMsg", "查询运行中的Job出错:"+e.toString());
            
            Map<String, Object> map = new HashMap<>();
            map.put("success", false);
            map.put("response", mapResponse);
            
    		return JSON.toJSONString(map);
	    }
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
    
    @RequestMapping("static/queryJobList")
    public String queryJobList() {
    	    	
    	String sql = "select Unid,ID,Name,Remark,Reserve,Reserve2,Reserve3,Reserve5,Reserve6 from CommCode where TypeName='定时任务' ";
    	
    	try{
    		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
    		                
            Map<String, Object> map = new HashMap<>();
            map.put("success", true);
            map.put("response", list);
            
	    	return JSON.toJSONStringWithDateFormat(map, "yyyy-MM-dd HH:mm:ss");	    	
    	    	
    	}catch(Exception e){
    		    		                
            Map<String, Object> mapResponse = new HashMap<>();
            mapResponse.put("errorCode", -123);
            mapResponse.put("errorMsg", "sql执行出错:"+e.toString()+"。错误的SQL:"+sql);
            
            Map<String, Object> map = new HashMap<>();
            map.put("success", false);
            map.put("response", mapResponse);
            
	    	return JSON.toJSONString(map);
    	}    	
    }
    
    @RequestMapping("static/taskOnoff")
    public String taskOnoff(HttpServletRequest request,HttpServletResponse response) {
    	
    	String unid = request.getParameter("unid");
    	String reserve6 = request.getParameter("reserve6");
    	    	    	
    	String sql = "update CommCode set Reserve6="+("1".equals(reserve6)?0:1)+" where Unid="+unid;
    	
        try{
            jdbcTemplate.update(sql);
                            
            Map<String, Object> mapResponse = new HashMap<>();
            mapResponse.put("id", -1);
            mapResponse.put("msg", "sql执行成功");
            
            Map<String, Object> map = new HashMap<>();
            map.put("success", true);
            map.put("response", mapResponse);
            
            return JSON.toJSONString(map);

        }catch(Exception e){
                
            Map<String, Object> mapResponse = new HashMap<>();
            mapResponse.put("errorCode", -223);
            mapResponse.put("errorMsg", "sql执行出错:"+e.toString()+"。错误的SQL:"+sql);
            
            Map<String, Object> map = new HashMap<>();
            map.put("success", false);
            map.put("response", mapResponse);
            
            return JSON.toJSONString(map);
        }
    }
}
