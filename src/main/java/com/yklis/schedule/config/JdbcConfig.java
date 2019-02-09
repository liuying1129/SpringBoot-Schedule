package com.yklis.schedule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *  <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
 *       <property name="dataSource">
 *           <ref bean="multipleDataSource" />
 *       </property>
 * 	</bean>
 * 
 * 意味着,JDBC执行SQL前先执行数据源路由类MultipleDataSource
 * 	
 * @author liuyi
 * 
 */
@Configuration
@Order(5)
public class JdbcConfig {

	//任何标志了@Bean的方法，其返回值将作为一个Bean注册到Spring的IOC容器中
	//方法名默认成为该bean定义的id
    @Bean
    public JdbcTemplate jdbcTemplate(MultipleDataSource multipleDataSource) throws Exception {
    	        
        return new JdbcTemplate(multipleDataSource);
    }
}
