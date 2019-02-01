package com.yklis.schedule.util;

import org.quartz.Scheduler;

/**
 * 单例模式
 * 饿汉式单例类.在类初始化时，已经自行实例化
 * @author liuying
 *
 */
public class MySingleton {
	
	//构造函数为 private,这样该类就不会被实例化
	private MySingleton() {}  
	
	//自行实例化
	//静态数据成员只会被初始化一次
	private static final MySingleton mySingleton = new MySingleton();
	
	private Scheduler scheduler;//单例类的成员变量
	
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
