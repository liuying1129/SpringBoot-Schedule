package com.yklis.schedule.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *  <!-- 配置sqlSessionFactory -->
 * 	<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
 *		<property name="dataSource" ref="multipleDataSource" />
 *	</bean>
 *
 * 意味着,MyBatis执行SQL前先执行数据源路由类MultipleDataSource
 *
 * @author liuyi
 *
 */
@Configuration
public class MyBatisConfig {
	
    @Bean
    public SqlSessionFactory sqlSessionFactory(MultipleDataSource multipleDataSource) throws Exception {
    	
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(multipleDataSource);
        return sqlSessionFactoryBean.getObject();
    }
}
