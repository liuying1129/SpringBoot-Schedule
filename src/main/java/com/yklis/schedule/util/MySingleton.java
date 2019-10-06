package com.yklis.schedule.util;

import org.quartz.Scheduler;

/**
 * 单例模式
 * 饿汉式单例类.在类初始化时，已经自行实例化
 * @author liuying
 *
 */
public class MySingleton {
	
    //JAVA规定，如果类中没有定义任何构造函数，JVM自动为其生成一个默认的构造函数
    //故可不需要手动写下面的构造函数
    //public JobSPH2CJ(){       
    //}
	
	//构造函数为private,这样该类就不会被外部实例化
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
