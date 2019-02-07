package com.yklis.schedule.config;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.yklis.schedule.util.CustomerContextHolder;
import com.yklis.schedule.util.MultipleDataSource;

//@PropertySource(value = {"file:/ykschedule-cfg/jdbc.properties"})
public class DynamicDataSourceRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Logger logger = LoggerFactory.getLogger(DynamicDataSourceRegister.class);
    
    // 默认数据源
    private ComboPooledDataSource defaultDataSource;

    private Map<String, ComboPooledDataSource> customDataSources = new HashMap<>();    
    
	@Override
	public void setEnvironment(Environment environment) {
		
        initDefaultDataSource(environment);
        initslaveDataSources(environment);
	}

	@Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		
		Map<String, Object> targetDataSources = new HashMap<>();

	    //将主数据源添加到更多数据源中
        targetDataSources.put("dataSource", defaultDataSource);

        CustomerContextHolder.dataSourceIds.add("dataSource");

        //添加更多数据源
        targetDataSources.putAll(customDataSources);

        for (String key : customDataSources.keySet()) {

        	CustomerContextHolder.dataSourceIds.add(key);
        }
        
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			dataSource.setDriverClass("net.sourceforge.jtds.jdbc.Driver");			
			dataSource.setJdbcUrl("jdbc:jtds:sqlserver://127.0.0.1:1433;DatabaseName=YkLis");
			dataSource.setUser("sa");
			dataSource.setPassword("yklissa");												
		} catch (PropertyVetoException e) {
			
			logger.error("createDataSource失败:" + e.toString());
		}

        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(MultipleDataSource.class);
        beanDefinition.setSynthetic(true);

        MutablePropertyValues mpv = beanDefinition.getPropertyValues();

        mpv.addPropertyValue("defaultTargetDataSource", dataSource);
        mpv.addPropertyValue("targetDataSources", targetDataSources);

        registry.registerBeanDefinition("dataSource", beanDefinition);
	}
	
    private void initDefaultDataSource(Environment env) {
        // 读取主数据源
        Map<String, Object> dsMap = new HashMap<>();
        dsMap.put("driver", "net.sourceforge.jtds.jdbc.Driver");
        dsMap.put("url", "jdbc:jtds:sqlserver://127.0.0.1:1433;DatabaseName=YkLis");
        dsMap.put("username", "sa");
        dsMap.put("password", "yklissa");
        defaultDataSource = buildDataSource(dsMap);
        //dataBinder(defaultDataSource, env);*/        
    }


    private void initslaveDataSources(Environment env) {
        // 读取配置文件获取更多数据源
        //String dsPrefixs = env.getProperty("slave.datasource.names");
        //for (String dsPrefix : dsPrefixs.split(",")) {
            // 多个数据源
            Map<String, Object> dsMap = new HashMap<>();
            dsMap.put("driver", "net.sourceforge.jtds.jdbc.Driver");
            dsMap.put("url", "jdbc:jtds:sqlserver://127.0.0.1:1433;DatabaseName=ERP");
            dsMap.put("username", "sa");
            dsMap.put("password", "yklissa");
            ComboPooledDataSource ds = buildDataSource(dsMap);
            customDataSources.put("ERP", ds);
            //dataBinder(ds, env);
        //}
    }
    
    public ComboPooledDataSource buildDataSource(Map<String, Object> dataSourceMap) {
        /*try {
            Object type = dataSourceMap.get("type");
            if (type == null) {
                type = DATASOURCE_TYPE_DEFAULT;// 默认DataSource
            }
            Class<? extends ComboPooledDataSource> dataSourceType;
            dataSourceType = (Class<? extends ComboPooledDataSource>) Class.forName((String) type);
            String driverClassName = dataSourceMap.get("driver").toString();
            String url = dataSourceMap.get("url").toString();
            String username = dataSourceMap.get("username").toString();
            String password = dataSourceMap.get("password").toString();
            // 自定义DataSource配置
            DataSourceBuilder factory = DataSourceBuilder.create().driverClassName(driverClassName).url(url)
                    .username(username).password(password).type(dataSourceType);
            return (ComboPooledDataSource) factory.build();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;*/
        
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			dataSource.setDriverClass(dataSourceMap.get("driver").toString());			
			dataSource.setJdbcUrl(dataSourceMap.get("url").toString());
			dataSource.setUser(dataSourceMap.get("username").toString());
			dataSource.setPassword(dataSourceMap.get("password").toString());									
			
		} catch (PropertyVetoException e) {
			
			logger.error("createDataSource失败:" + e.toString());
			return null;
		}
		
		return dataSource;        
    }  
    
    /*private void dataBinder(ComboPooledDataSource dataSource, Environment env){

        RelaxedDataBinder dataBinder = new RelaxedDataBinder(dataSource);

        dataBinder.setConversionService(conversionService);

        dataBinder.setIgnoreNestedProperties(false);//false

         dataBinder.setIgnoreInvalidFields(false);//false

         dataBinder.setIgnoreUnknownFields(true);//true

        

         if(dataSourcePropertyValues == null){

             Map<String, Object> rpr = new RelaxedPropertyResolver(env, "spring.datasource").getSubProperties(".");

             Map<String, Object> values = new HashMap<>(rpr);

             // 排除已经设置的属性

             values.remove("type");

             values.remove("driverClassName");

             values.remove("url");

             values.remove("username");

             values.remove("password");

             dataSourcePropertyValues = new MutablePropertyValues(values);

         }

         dataBinder.bind(dataSourcePropertyValues);

        

     }*/
}
