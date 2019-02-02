package com.yklis.schedule;

import java.beans.PropertyVetoException;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 右键启动
 * 即右键入口main函数所在的文件就能启动整个项目
 * @author liuyi
 *
 */
@SpringBootApplication
//扫描指定包中的Mybatis接口，然后创建各自接口的动态代理类
@MapperScan(value = {"com.yklis.schedule.dao"})
//扫描本目录以及子目录的WebServlet注解
@ServletComponentScan
public class ScheduleApplication {

    //配置容器起动时候加载log4j配置文件
    //只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
    //在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
    //如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
    //PropertyConfigurator.configure("log4jj.properties");
    //private final Logger logger = Logger.getLogger(this.getClass());
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        
    //指定当前对象作为bean
    @Bean(name = "dataSource_master")
    //指定dataSource_master来DI
    @Qualifier(value = "dataSource_master")
    //当前数据库连接池作为默认数据库连接池
    //ComboPooledDataSource表示C3P0连接池
    @Primary
    public ComboPooledDataSource createDataSource() {
    	
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			dataSource.setDriverClass(driverClass);			
			dataSource.setJdbcUrl(jdbcUrl);
			dataSource.setUser(user);
			dataSource.setPassword(password);
									
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
    
	public static void main(String[] args) {
		
	    //private final Logger logger2 = Logger.getLogger(ScheduleApplication.getClass());

		//logger.info("");
		
		SpringApplication.run(ScheduleApplication.class, args);
	}

}

