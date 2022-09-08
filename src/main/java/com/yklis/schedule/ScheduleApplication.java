package com.yklis.schedule;

import org.mybatis.spring.annotation.MapperScan;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
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
import com.yklis.schedule.util.GroupOfJobListener;
import com.yklis.schedule.util.MySingleton;

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
		
        SchedulerFactory factory;
		try {
			//默认从ClassPath读取quartz.properties
			//也可通过参数指定其他位置的配置文件
			factory = new StdSchedulerFactory();
			logger.info("创建Scheduler Factory成功");
		} catch (Exception e) {
			
			logger.error("创建Scheduler Factory失败:"+e.toString());
			return;
		}
		
		MySingleton mySingleton = MySingleton.getInstance();
		
        try {
    		//Scheduler是单例模式(懒汉式).StdSchedulerFactory.getScheduler方法右键,Quick Type Hierarchy查看实现方法
        	Scheduler scheduler = factory.getScheduler();
        	//因为本人并不知道如何通过其他方式生成一个全局Scheduler对象,故此处使用另一个单例类MySingleton
    		mySingleton.setScheduler(scheduler);
			logger.info("获取Scheduler成功");
		} catch (SchedulerException e) {

			logger.error("获取Scheduler失败:"+e.toString());
			return;

		}
                
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
                
        Scheduler scheduler = mySingleton.getScheduler();
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
		
    	MySingleton mySingleton = MySingleton.getInstance();
    	Scheduler scheduler = mySingleton.getScheduler();
    	
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
