package com.yklis.schedule.config;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 *   <!-- 统一数据库 -->
 *   <bean id="multipleDataSource" class="com.yklis.util.MultipleDataSource">
 *       <!-- 从未setCustomerType或clearCustomerType，使用defaultTargetDataSource -->
 *       <property name="defaultTargetDataSource" ref="dataSource_master"/>
 *       <property name="targetDataSources">
 *           <map>     
 *               <!-- 注意这里的value是和上面的DataSource的id对应，key要和类CustomerContextHolder中的常量对应 -->
 *               <!-- <entry value-ref="dataSource_1" key="dataSource_1"/>
 *               <entry value-ref="dataSource_2" key="dataSource_2"/> -->
 *           </map>   
 *       </property>
 *   </bean>
 * 
 * @author liuyi
 *
 */
public class DynamicDataSourceRegister implements ImportBeanDefinitionRegistrar {

    private Logger logger = LoggerFactory.getLogger(DynamicDataSourceRegister.class);
    
    public static final Map<String, String> ctxPropertiesMap = new HashMap<>();

	@Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		        
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			dataSource.setDriverClass("net.sourceforge.jtds.jdbc.Driver");			
			dataSource.setJdbcUrl("jdbc:jtds:sqlserver://127.0.0.1:1433;DatabaseName=YkLis");
			dataSource.setUser("sa");
			dataSource.setPassword("yklissa");												
		} catch (PropertyVetoException e) {
			
			logger.error("createDataSource失败:" + e.toString());
		}
		
		ctxPropertiesMap.put("jdbc.driverClass", "net.sourceforge.jtds.jdbc.Driver");
		ctxPropertiesMap.put("jdbc.jdbcUrl", "jdbc:jtds:sqlserver://127.0.0.1:1433;DatabaseName=YkLis");
		ctxPropertiesMap.put("jdbc.user", "sa");
		ctxPropertiesMap.put("jdbc.password", "yklissa");
		
		
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(MultipleDataSource.class);
        beanDefinition.setSynthetic(true);

        MutablePropertyValues mpv = beanDefinition.getPropertyValues();

        mpv.addPropertyValue("defaultTargetDataSource", dataSource);
		Map<String, Object> targetDataSources = new HashMap<>();
        mpv.addPropertyValue("targetDataSources", targetDataSources);

        registry.registerBeanDefinition("multipleDataSource", beanDefinition);
	}
	    
    /**
     * Java普通类调用该方法获取配置文件的值
     * @param key
     * @return
     */
	public static String getProperty(String key){
        
        return ctxPropertiesMap.get(key);
    }    
}
