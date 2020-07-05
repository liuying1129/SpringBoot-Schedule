package com.yklis.schedule.jobtype;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yklis.schedule.entity.CommCodeEntity;
import com.yklis.schedule.util.Constants;

/**
 * 策略模式
 * 定义具体的策略
 * Class类型任务的具体策略
 * 
 * @author liuyi
 *
 */
public class JobTypeClass implements JobTypeStrategy {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private JobExecutionContext context;

    public JobTypeClass(JobExecutionContext context) {
    	
    	this.context = context;
    }
    
	@Override
	public void jobTypeMethod() {
		
		CommCodeEntity commCodeEntity = (CommCodeEntity)context.getMergedJobDataMap().get(Constants.JOB_DATA_MAP_KEY);
		
		//使用 Class<?>比单纯使用 Class要好,虽然它们是等价的,并且单纯使用 Class不会产生编译器警告信息.
		//使用 Class<?>的好处是,表示你并非是碰巧或者由于疏忽才使用了一个非具体的类引用，而是特意为之
        Class<?> ownerClass = null;
		try {
			ownerClass = Class.forName(commCodeEntity.getReserve2());
		} catch (ClassNotFoundException e) {
			logger.error("Class.forName报错:"+e.toString());
			return;
		}
        Object owner = null;
		try {
			owner = ownerClass.newInstance();
		} 
		/*
		 * InstantiationException | IllegalAccessException
		 * 实现类JobLis2Redis,成员变量private ShardedJedis shardedJedis = shardedJedisPool.getResource();获取不到连接时的报错用上述异常类无法捕获
		 * 故使用Exception
		 */
		catch (Exception e) {
			logger.error("newInstance报错:"+e.toString());
			return;
		}
		
		Class<?>[] params = { Map.class };
		Map<String,Object> map = new HashMap<>();
		map.put("k1", "abcdefg");
		Object[] paramValues = { map };
				
        Method method = null;
		try {
			//命令类都实现interface Command,故方法名固定为execute
			method = ownerClass.getMethod("execute",params);//commCodeEntity.getReserve4()
		} catch (NoSuchMethodException | SecurityException e) {
			logger.error("getMethod报错:"+e.toString());
			return;
		}
        try {
			method.invoke(owner,paramValues);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("invoke报错:"+e.toString());
			return;
		}
	}
}
