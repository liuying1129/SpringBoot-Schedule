package com.yklis.schedule.util;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取一个全局Scheduler实例
 * @author liuying
 *
 */
public class GlobalScheduler {
	
	//类中成员变量定义的顺序决定了它们初始化的顺序.即使成员变量定义散布在方法定义之间,它们仍会在任何方法(包括构造器)被调用之前得到初始化
	
    private static final Logger logger = LoggerFactory.getLogger(GlobalScheduler.class);
    
	//静态数据成员只会被初始化一次
	//初次创建GlobalScheduler对象或初次使用GlobalScheduler时,factory才会初始化
	//默认从ClassPath读取quartz.properties
	//也可通过参数指定其他位置的配置文件
	private static SchedulerFactory factory = new StdSchedulerFactory();
	
	//JAVA规定，如果类中没有定义任何构造函数，JVM自动为其生成一个默认的构造函数
    //故可不需要手动写下面的构造函数
    //public GlobalScheduler(){
    //}
	
	//构造函数为private,这样该类就不会被外部实例化
	private GlobalScheduler() {}
	
	public static Scheduler getScheduler() {
		
		//Scheduler是单例模式(懒汉式).通过右键StdSchedulerFactory.getScheduler,Quick Type Hierarchy可查看实现方法
		Scheduler scheduler = null;
		try {
			
			scheduler = factory.getScheduler();
		} catch (SchedulerException e) {
			logger.error("获取Scheduler失败:"+e.toString());
		}
		return scheduler;
	}
}