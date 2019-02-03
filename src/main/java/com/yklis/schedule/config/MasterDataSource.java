package com.yklis.schedule.config;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * C3P0连接池
 * 主数据源
 * 
 * 享元模式(又叫蝇量模式)
 * 用HashMap存储对象,重用
 * 使quartz可读取到该连接属性,实现quartz集群(JobStoreTX)
 * @author liuyi
 *
 */
@Component//或@Configuration
@PropertySource(value = { "classpath:jdbc.properties"})
public class MasterDataSource {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
	//final
	//如果是基本数据类型的变量，则其数值一旦在初始化之后便不能更改
	//如果是引用类型的变量，则在对其初始化之后便不能再让其指向另一个对象.但是对象本身是可以被修改的
    public static final Map<String, String> ctxPropertiesMap = new HashMap<>();

    //@Value的作用是将我们配置文件的属性读出来
    @Value("${jdbc.driverClass}")
    private String driverClass;
    @Value("${jdbc.jdbcUrl}")
    private String jdbcUrl;
    @Value("${jdbc.user}")
    private String user;
    @Value("${jdbc.password}")
    private String password;

    @Value("${jdbc.pool.initialPoolSize}")
    private String initialPoolSize;
    @Value("${jdbc.pool.maxIdleTime}")
    private String maxIdleTime;
    @Value("${jdbc.pool.maxPoolSize}")
    private String maxPoolSize;
    @Value("${jdbc.pool.minPoolSize}")
    private String minPoolSize;
    @Value("${jdbc.pool.checkoutTimeout}")
    private String checkoutTimeout;
        
    /**
     * ComboPooledDataSource表示C3P0连接池
     * @return
     */
    @Bean
    public ComboPooledDataSource createDataSource() {
    	
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			dataSource.setDriverClass(driverClass);			
			dataSource.setJdbcUrl(jdbcUrl);
			dataSource.setUser(user);
			dataSource.setPassword(password);
									
			ctxPropertiesMap.put("driverClass", driverClass);
			ctxPropertiesMap.put("jdbcUrl", jdbcUrl);
			ctxPropertiesMap.put("user", user);
			ctxPropertiesMap.put("password", password);

			//连接池在无空闲连接可用时一次性创建的新数据库连接数,default : 3
			//dataSource.setAcquireIncrement(Integer.parseInt(customerType.get("acquireIncrement").toString()));
			
			//连接池初始化时创建的连接数,default : 3，取值应在minPoolSize与maxPoolSize之间
			dataSource.setInitialPoolSize(Integer.parseInt(initialPoolSize));
			
			//连接的最大空闲时间，如果超过这个时间，某个数据库连接还没有被使用，则会断开掉这个连接。如果为0，则永远不会断开连接,即回收此连接。default : 0 单位 s
			dataSource.setMaxIdleTime(Integer.parseInt(maxIdleTime));
			
			//连接池保持的最小连接数,default : 10
			dataSource.setMinPoolSize(Integer.parseInt(minPoolSize));
			
			//连接池中拥有的最大连接数，如果获得新连接时会使连接总数超过这个值则不会再获取新连接，而是等待其他连接释放，所以这个值有可能会设计地很大,default : 100
			dataSource.setMaxPoolSize(Integer.parseInt(maxPoolSize));
			
			//每900秒检查所有连接池中的空闲连接。default : 0
			//dataSource.setIdleConnectionTestPeriod(Integer.parseInt(customerType.get("idleConnectionTestPeriod").toString()));
			
			//配置PreparedStatement缓存
			//连接池为数据源缓存的PreparedStatement的总数。由于PreparedStatement属于单个Connection,所以这个数量应该根据应用中平均连接数乘以每个连接的平均PreparedStatement来计算。同时maxStatementsPerConnection的配置无效。default : 0（不建议使用）
			//dataSource.setMaxStatements(Integer.parseInt(customerType.get("maxStatements").toString()));

			//连接池为数据源单个Connection缓存的PreparedStatement数，这个配置比maxStatements更有意义，因为它缓存的服务对象是单个数据连接，
			//如果设置的好，肯定是可以提高性能的。为0的时候不缓存。default : 0（看情况而论）
			//dataSource.setMaxStatementsPerConnection(Integer.parseInt(customerType.get("maxStatementsPerConnection").toString()));
			
			//连接池在获得新连接失败时重试的次数，如果小于等于0则无限重试直至连接获得成功。default : 30（建议使用）
			//dataSource.setAcquireRetryAttempts(Integer.parseInt(customerType.get("acquireRetryAttempts").toString()));
			
			//两次连接中间隔时间，单位毫秒，连接池在获得新连接时的间隔时间。default : 1000 单位ms（建议使用）
			//dataSource.setAcquireRetryDelay(Integer.parseInt(customerType.get("acquireRetryDelay").toString()));
			
			//如果为true，则当连接获取失败时自动关闭数据源，除非重新启动应用程序。所以一般不用。default : false（不建议使用）
			//dataSource.setBreakAfterAcquireFailure(Boolean.parseBoolean(customerType.get("breakAfterAcquireFailure").toString()));
			
		    //连接池用完时客户端调用getConnection()后等待获取新连接的时间，超时将抛出 SQLException,如设为0则无限期等待。单位毫秒。Default: 0
			dataSource.setCheckoutTimeout(Integer.parseInt(checkoutTimeout));
			
		} catch (PropertyVetoException e) {
			
			logger.error("createDataSource失败:" + e.toString());
			return null;
		}
		
		return dataSource;
    }
}
