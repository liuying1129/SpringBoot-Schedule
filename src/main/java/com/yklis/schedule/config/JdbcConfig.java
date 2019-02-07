package com.yklis.schedule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.yklis.schedule.util.MultipleDataSource;

/**
 *  <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
 *       <property name="dataSource">
 *           <ref bean="multipleDataSource" />
 *       </property>
 * 	</bean>
 * 	
 * @author liuyi
 * 
 */
@Configuration
public class JdbcConfig {

    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate(MultipleDataSource multipleDataSource) throws Exception {
    	        
        return new JdbcTemplate(multipleDataSource);
    }
}
