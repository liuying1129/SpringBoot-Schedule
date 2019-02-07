package com.yklis.schedule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class JdbcConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(MultipleDataSource multipleDataSource) throws Exception {
    	        
        return new JdbcTemplate(multipleDataSource);
    }
}
