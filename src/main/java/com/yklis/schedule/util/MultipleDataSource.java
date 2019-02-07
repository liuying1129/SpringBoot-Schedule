package com.yklis.schedule.util;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 数据源路由
 * 
 * @author liuyi
 *
 */
public class MultipleDataSource extends AbstractRoutingDataSource {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//Java 7之前只能这样写：
	//private Map<Object, Object> targetDataSources = new HashMap<Object, Object>()
	//Java 7支持类型推断（type inference）,让编译器推断出合适的类
	private final Map<Object, Object> targetDataSources = new HashMap<>();
	
    @Override
    protected Object determineCurrentLookupKey() {
    	
    	/*Map<String, Object> customerTypeMap =  CustomerContextHolder.getCustomerType();
    	
    	//从未setCustomerType或clearCustomerType，customerTypeMap为null.
    	//会使用defaultTargetDataSource
    	if(customerTypeMap == null) return null;
    	if(null == customerTypeMap.get("driverClass")) return null;
    	if(null == customerTypeMap.get("url")) return null;
    	if(null == customerTypeMap.get("user")) return null;
    	
    	String dataSourceKey = customerTypeMap.get("driverClass").toString() + (char)2 + customerTypeMap.get("url").toString() + (char)2 + customerTypeMap.get("user").toString();
    	
    	if(targetDataSources.containsKey(dataSourceKey)) return dataSourceKey;
    	
		ComboPooledDataSource dataSource = createDataSource(customerTypeMap);
		this.addTargetDataSource(dataSourceKey, dataSource);
    	
    	return dataSourceKey;*/
    	
    	return CustomerContextHolder.getDataSourceType();
    }
    
	public void addTargetDataSource(String key, ComboPooledDataSource dataSource) {
		
		this.targetDataSources.put(key,  dataSource);
		super.setTargetDataSources(targetDataSources);
		afterPropertiesSet();
	}
    
	public ComboPooledDataSource createDataSource(Map<String, Object> customerType) {
		
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			if(customerType.get("driverClass") != null)
				dataSource.setDriverClass(customerType.get("driverClass").toString());
			
			if(customerType.get("url") != null)
				dataSource.setJdbcUrl(customerType.get("url").toString());
			
			if(customerType.get("user") != null)
				dataSource.setUser(customerType.get("user").toString());
			
			if(customerType.get("password") != null)
				dataSource.setPassword(customerType.get("password").toString());
			
						
			//连接池在无空闲连接可用时一次性创建的新数据库连接数,default : 3
			if(customerType.get("acquireIncrement") != null)
				dataSource.setAcquireIncrement(Integer.parseInt(customerType.get("acquireIncrement").toString()));
			
			//连接池初始化时创建的连接数,default : 3，取值应在minPoolSize与maxPoolSize之间
			if(customerType.get("initialPoolSize") != null)
				dataSource.setInitialPoolSize(Integer.parseInt(customerType.get("initialPoolSize").toString()));
			
			//连接的最大空闲时间，如果超过这个时间，某个数据库连接还没有被使用，则会断开掉这个连接。如果为0，则永远不会断开连接,即回收此连接。default : 0 单位 s
			if(customerType.get("maxIdleTime") != null)
				dataSource.setMaxIdleTime(Integer.parseInt(customerType.get("maxIdleTime").toString()));
			
			//连接池保持的最小连接数,default : 10
			if(customerType.get("minPoolSize") != null)
				dataSource.setMinPoolSize(Integer.parseInt(customerType.get("minPoolSize").toString()));
			
			//连接池中拥有的最大连接数，如果获得新连接时会使连接总数超过这个值则不会再获取新连接，而是等待其他连接释放，所以这个值有可能会设计地很大,default : 100
			if(customerType.get("maxPoolSize") != null)
				dataSource.setMaxPoolSize(Integer.parseInt(customerType.get("maxPoolSize").toString()));
			
			//每900秒检查所有连接池中的空闲连接。default : 0
			if(customerType.get("idleConnectionTestPeriod") != null)
				dataSource.setIdleConnectionTestPeriod(Integer.parseInt(customerType.get("idleConnectionTestPeriod").toString()));
			
			//配置PreparedStatement缓存
			//连接池为数据源缓存的PreparedStatement的总数。由于PreparedStatement属于单个Connection,所以这个数量应该根据应用中平均连接数乘以每个连接的平均PreparedStatement来计算。同时maxStatementsPerConnection的配置无效。default : 0（不建议使用）
			if(customerType.get("maxStatements") != null)
				dataSource.setMaxStatements(Integer.parseInt(customerType.get("maxStatements").toString()));

			//连接池为数据源单个Connection缓存的PreparedStatement数，这个配置比maxStatements更有意义，因为它缓存的服务对象是单个数据连接，
			//如果设置的好，肯定是可以提高性能的。为0的时候不缓存。default : 0（看情况而论）
			if(customerType.get("maxStatementsPerConnection") != null)
				dataSource.setMaxStatementsPerConnection(Integer.parseInt(customerType.get("maxStatementsPerConnection").toString()));
			
			//连接池在获得新连接失败时重试的次数，如果小于等于0则无限重试直至连接获得成功。default : 30（建议使用）
			if(customerType.get("acquireRetryAttempts") != null)
				dataSource.setAcquireRetryAttempts(Integer.parseInt(customerType.get("acquireRetryAttempts").toString()));
			
			//两次连接中间隔时间，单位毫秒，连接池在获得新连接时的间隔时间。default : 1000 单位ms（建议使用）
			if(customerType.get("acquireRetryDelay") != null)
				dataSource.setAcquireRetryDelay(Integer.parseInt(customerType.get("acquireRetryDelay").toString()));
			
			//如果为true，则当连接获取失败时自动关闭数据源，除非重新启动应用程序。所以一般不用。default : false（不建议使用）
			if(customerType.get("breakAfterAcquireFailure") != null)
				dataSource.setBreakAfterAcquireFailure(Boolean.parseBoolean(customerType.get("breakAfterAcquireFailure").toString()));
			
		} catch (PropertyVetoException e) {
			
			logger.error("createDataSource失败:" + e.toString());
			return null;
		}
		
		return dataSource;
	}

}
