package com.yklis.schedule.util;

import java.util.List;
import java.util.Map;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.yklis.schedule.dao.CommCodeDao;
import com.yklis.schedule.entity.CommCodeEntity;

/**
 * 动态切换数据源
 * @author ying07.liu
 *
 */
public abstract class CustomerContextHolder {
    
	//Java 7之前只能这样写：
	//private static final ThreadLocal<Map<String, Object>> contextHolder = new ThreadLocal<Map<String, Object>>()
	//Java 7支持类型推断（type inference）,让编译器推断出合适的类
    
    //ThreadLocal为每个使用该变量的线程提供独立的变量副本,所以每个线程都可以独立地改变自己的副本,而不会影响其它线程所对应的副本
    private static final ThreadLocal<Map<String, Object>> contextHolder = new ThreadLocal<>();
    	
    public static void setCustomerType(Map<String, Object> customerType) {
        //ThreadLocal的set方法:设置当前线程的线程局部变量的值
        contextHolder.set(customerType);
    }  
      
    public static Map<String, Object> getCustomerType() {
        //ThreadLocal的get方法:返回当前线程所对应的线程局部变量
        return contextHolder.get();  
    }  
      
    public static void clearCustomerType() {
        //ThreadLocal的remove方法:将当前线程局部变量的值删除
        contextHolder.remove();
    }
    
    //自定义的两个切换用函数
	public static int getJdbcUnidFromJobClass(String jobClassName){
		
		WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();		
		CommCodeDao commCodeDao = context.getBean(CommCodeDao.class);
		
		CommCodeEntity commCodeEntity = new CommCodeEntity();
		commCodeEntity.setTypeName("定时任务");
		commCodeEntity.setReserve2(jobClassName);
		List<CommCodeEntity> commCodeEntityList = commCodeDao.selectCommCode(commCodeEntity);
		
		if(commCodeEntityList == null) return -1;
		if(commCodeEntityList.size()<=0) return -1;
		
		int jdbcUnid = commCodeEntityList.get(0).getReserve5();
		
		return jdbcUnid;
	}
	
	public static CommCodeEntity getConnectionInfo(int jdbcUnid){
		
		WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();		
		CommCodeDao commCodeDao = context.getBean(CommCodeDao.class);
		
		CommCodeEntity commCodeEntity = new CommCodeEntity();
		commCodeEntity.setTypeName("JDBC连接字符串");
		commCodeEntity.setUnid(jdbcUnid);
		List<CommCodeEntity> commCodeEntityList = commCodeDao.selectCommCode(commCodeEntity);
		
		if(commCodeEntityList == null)return null;
		if(commCodeEntityList.size()<=0)return null;
				
		return commCodeEntityList.get(0);
	}
    
}
