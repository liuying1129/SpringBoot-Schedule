package com.yklis.schedule;

import org.mybatis.spring.annotation.MapperScan;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import com.yklis.schedule.config.DynamicDataSourceRegister;
import com.yklis.schedule.util.Constants;
import com.yklis.schedule.util.GlobalScheduler;
import com.yklis.schedule.util.GroupOfJobListener;

/**
 * 右键启动
 * 即右键入口main函数所在的文件就能启动整个项目
 * 
 * SpringBoot的CommandLineRunner的作用:
 * 项目启动后执行的功能.实现功能的代码放在实现的run方法中
 * @author liuyi
 *
 */
@SpringBootApplication
//不影响默认配置文件的读取
@PropertySource(value = {"file:/ykschedule-cfg/jdbc.properties"})
//扫描指定包中的Mybatis接口，然后创建各自接口的动态代理类
@MapperScan(value = {"com.yklis.schedule.dao"})
@Import({DynamicDataSourceRegister.class})
public class ScheduleApplication implements CommandLineRunner,DisposableBean {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
	public static void main(String[] args) {
				
		SpringApplication.run(ScheduleApplication.class, args);
	}

	/**
	 * 接口CommandLineRunner的方法实现
	 */
	@Override
	public void run(String... args) throws Exception {
		                
        /*//手动设置Job、Trigger
        JobDetail getMessageJob = newJob(Job1.class).withIdentity("getDetailsJob", "group1").build();  

        Trigger getMessageTrigger = newTrigger()
        		.withIdentity("getDetailsTrigger", "group1")
        		.startNow()
                .withSchedule(simpleSchedule().withIntervalInSeconds(5).repeatForever())  
                .build();

        try {
			scheduler.scheduleJob(getMessageJob, getMessageTrigger);
		} catch (SchedulerException e) {
			
			System.out.println("为Scheduler设置job失败");
			return;
		} 
        //===================*/
                
        Scheduler scheduler = GlobalScheduler.getScheduler();
        try {
            //按Job组注册JOB监听器
            scheduler.getListenerManager().addJobListener(new GroupOfJobListener(), GroupMatcher.jobGroupEquals(Constants.DEFAULT_JOB_GROUP));
        } catch (SchedulerException e) {
            
            logger.error("注册监听器失败:"+e.toString());
        }
        try {
        	scheduler.start();
	        logger.info("Scheduler has been started");                
		} catch (SchedulerException e) {
			
			logger.error("启动Scheduler失败:"+e.toString());
			return;
		}		
		
	}

	/**
	 * 接口DisposableBean的方法实现
	 */
	@Override
	public void destroy() throws Exception {
		
    	Scheduler scheduler = GlobalScheduler.getScheduler();
    	
        if (scheduler != null) {
            try {
            	//true:等待进行中的Job完成后才关闭
				scheduler.shutdown(true);
			} catch (SchedulerException e) {
				logger.error("Quartz Scheduler shutdown fail:"+e.toString());
				return;
			}
        }        
	}
}
