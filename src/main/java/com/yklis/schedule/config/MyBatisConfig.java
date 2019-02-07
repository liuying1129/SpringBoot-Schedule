package com.yklis.schedule.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.yklis.schedule.util.MultipleDataSource;

/**
 *  <!-- 配置sqlSessionFactory -->
 * 	<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
 *		<property name="dataSource" ref="multipleDataSource" />
 *	</bean>
 *
 * @author liuyi
 *
 */
@Configuration
public class MyBatisConfig {
	
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(MultipleDataSource multipleDataSource) throws Exception {
    	
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(multipleDataSource);
        return sqlSessionFactoryBean.getObject();
    }
}
