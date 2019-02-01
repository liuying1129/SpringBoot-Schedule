package com.yklis.schedule.util;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import org.quartz.SchedulerException;
import org.quartz.utils.ConnectionProvider;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 自定义类，用于quartz集群时获取连接DB的数据源
 * 替换quartz.properties中的硬编码
 * org.quartz.dataSource.myDS.driver = net.sourceforge.jtds.jdbc.Driver  
 * org.quartz.dataSource.myDS.URL = jdbc:jtds:sqlserver://211.97.0.5:1433;DatabaseName=TMS
 * org.quartz.dataSource.myDS.user = liu  
 * org.quartz.dataSource.myDS.password = 
 * @author ying07.liu
 *
 */
public class QuartzConnectionProvider implements ConnectionProvider {
		
	private ComboPooledDataSource datasource;
	
    //配置容器起动时候加载log4j配置文件
    //只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
    //在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
    //如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
    //PropertyConfigurator.configure("log4jj.properties");
    //private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public Connection getConnection() throws SQLException {
		return datasource.getConnection();
	}

	@Override
	public void shutdown() throws SQLException {
		datasource.close();		
	}

	@Override
	public void initialize() throws SQLException {
		
		//资源文件在classpath时用该方法
		//InputStream in = this.getClass().getClassLoader().getResourceAsStream("jdbc.properties");
				
		//获取项目所在的盘符//在项目包之外配置是为了部署、升级更方便
		/*String ss1 = this.getClass().getResource("").getFile();///D:/Tools/apache-tomcat-8.5.4/webapps/YkSchedule/WEB-INF/classes/com/yklis/util/		
		int ii = ss1.indexOf(':');
		ss1 = ss1.substring(0,ii);
		ss1 = ss1.replace("/", "");

		InputStream in = null;
		try {
			in = new FileInputStream(ss1+":/ykschedule-cfg/jdbc.properties");
		} catch (FileNotFoundException e) {
			logger.error("new FileInputStream报错:"+e.toString());
		}
				
		Properties props = new Properties();
		try {
			props.load(in);
		} catch (IOException e) {
			logger.error("Properties加载jdbc.properties报错:"+e.toString());
		}
        String driver = props.getProperty("jdbc.driverClass");
        String url = props.getProperty("jdbc.jdbcUrl");
        String user = props.getProperty("jdbc.user");
        String password = props.getProperty("jdbc.password");
        //*/
	    
        String driver = CustomPropertyConfigurer.getProperty("jdbc.driverClass");
        String url = CustomPropertyConfigurer.getProperty("jdbc.jdbcUrl");
        String user = CustomPropertyConfigurer.getProperty("jdbc.user");
        String password = CustomPropertyConfigurer.getProperty("jdbc.password");
        
        //logger.info("Quartz jdbc.driverClass:"+driver);
        //logger.info("Quartz jdbc.jdbcUrl:"+url);
        //logger.info("Quartz jdbc.user:"+user);
        //logger.info("Quartz jdbc.password:"+password);
		
        if (driver == null) {  
            throw new SQLException("DBPool driver could not be created: DB driver class name cannot be null!");  
        }
    
        if (url == null) {  
            throw new SQLException("DBPool could not be created: DB URL cannot be null");  
        }  
  
        datasource = new ComboPooledDataSource();  
        try {  
            datasource.setDriverClass(driver);  
        } catch (PropertyVetoException e) {  
            try {  
                throw new SchedulerException("Problem setting driver class name on datasource: " + e.getMessage(), e);  
            } catch (SchedulerException e1) {  
            }  
        }  
        datasource.setJdbcUrl(url);  
        datasource.setUser(user);  
        datasource.setPassword(password);  		
	}
}
