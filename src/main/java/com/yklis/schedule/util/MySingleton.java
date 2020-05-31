package com.yklis.schedule.util;

import org.quartz.Scheduler;

/**
 * 单例模式
 * 饿汉式单例类.在类初始化时，已经自行实例化
 * @author liuying
 *
 */
public class MySingleton {
	
	//类中成员变量定义的顺序决定了它们初始化的顺序.即使成员变量定义散布在方法定义之间,它们仍会在任何方法(包括构造器)被调用之前得到初始化
	
	//自行实例化
	//静态数据成员只会被初始化一次
	//初次创建MySingleton对象或初次使用mySingleton时,mySingleton才会初始化
	private static final MySingleton mySingleton = new MySingleton();
	
	private Scheduler scheduler;//单例类的成员变量	

	//JAVA规定，如果类中没有定义任何构造函数，JVM自动为其生成一个默认的构造函数
    //故可不需要手动写下面的构造函数
    //public MySingleton(){
    //}
	
	//构造函数为private,这样该类就不会被外部实例化
	private MySingleton() {}
		
    //静态工厂方法 
    public static MySingleton getInstance() {  
        return mySingleton;
    }
    
	public Scheduler getScheduler() {
		return this.scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
}
