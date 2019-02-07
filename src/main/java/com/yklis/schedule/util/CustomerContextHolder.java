package com.yklis.schedule.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yklis.schedule.dao.CommCodeDao;
import com.yklis.schedule.entity.CommCodeEntity;

/**
 * 动态数据源上下文
 * @author ying07.liu
 *
 */
public abstract class CustomerContextHolder {
    
	//Java 7之前只能这样写：
	//private static final ThreadLocal<Map<String, Object>> contextHolder = new ThreadLocal<Map<String, Object>>()
	//Java 7支持类型推断（type inference）,让编译器推断出合适的类
    
    //ThreadLocal为每个使用该变量的线程提供独立的变量副本,所以每个线程都可以独立地改变自己的副本,而不会影响其它线程所对应的副本
    //private static final ThreadLocal<Map<String, Object>> contextHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
    
    public static List<String> dataSourceIds = new ArrayList<>();
    	
    //public static void setCustomerType(Map<String, Object> customerType) {
        //ThreadLocal的set方法:设置当前线程的线程局部变量的值
    //    contextHolder.set(customerType);
    //}  
      
    //public static Map<String, Object> getCustomerType() {
        //ThreadLocal的get方法:返回当前线程所对应的线程局部变量
    //    return contextHolder.get();  
    //}  
      
    //public static void clearCustomerType() {
        //ThreadLocal的remove方法:将当前线程局部变量的值删除
    //    contextHolder.remove();
    //}
    
    public static void setDataSourceType(String dataSourceType) {

        contextHolder.set(dataSourceType);
     }

     public static String getDataSourceType() {

        return contextHolder.get();
     }

     public static void clearDataSourceType() {

        contextHolder.remove();
     }
     
     public static boolean containsDataSource(String dataSourceId){

         return dataSourceIds.contains(dataSourceId);
     }     
    
    //自定义的两个切换用函数
	public static int getJdbcUnidFromJobClass(String jobClassName){
		
		CommCodeDao commCodeDao = SpringUtils.getBean(CommCodeDao.class);
		
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
		
		CommCodeDao commCodeDao = SpringUtils.getBean(CommCodeDao.class);
		
		CommCodeEntity commCodeEntity = new CommCodeEntity();
		commCodeEntity.setTypeName("JDBC连接字符串");
		commCodeEntity.setUnid(jdbcUnid);
		List<CommCodeEntity> commCodeEntityList = commCodeDao.selectCommCode(commCodeEntity);
		
		if(commCodeEntityList == null)return null;
		if(commCodeEntityList.size()<=0)return null;
				
		return commCodeEntityList.get(0);
	}
    
}
