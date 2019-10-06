package com.yklis.schedule.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

/**
    <!-- redis连接池配置 -->
	<bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">  
	    <property name="maxTotal" value="${redis.pool.maxTotal}" />  
	    <property name="maxIdle" value="${redis.pool.maxIdle}" />  
	    <property name="maxWaitMillis" value="${redis.pool.maxWaitMillis}" />  
	</bean>
		
	<!-- redis连接池 -->
	<bean id="shardedJedisPool" class="redis.clients.jedis.ShardedJedisPool">
	    <!-- 构造器依赖注入 -->  
	    <constructor-arg index="0" ref="jedisPoolConfig" />  
	    <constructor-arg index="1">  
	        <list>  
	            <bean class="redis.clients.jedis.JedisShardInfo">  
	                <constructor-arg index="0" value="${redis.host}" />  
	                <constructor-arg index="1" value="${redis.port}" type="int" />  
	            </bean>  
	        </list>  
	    </constructor-arg>  
	</bean>
	
 * @author liuyi
 *
 * @Configuration把一个类作为一个IoC容器，它的某个方法头上如果注册了@Bean，就会作为这个Spring容器中的Bean
 * @Configuration可理解为用spring的时候xml里面的<beans>标签
 * @Bean可理解为用spring的时候xml里面的<bean>标签
 */

@Configuration
@PropertySource(value = {"file:/ykschedule-cfg/redis.properties"})
public class RedisConfig {
	
    @Value("${redis.host}")
    private String host;
    
    //@Value("${redis.password}")
    //private String password;
 
    @Value("${redis.port}")
    private int port;
 
    //@Value("${redis.timeout}")
    //private int timeout;        		    		   		
 
    @Value("${redis.pool.maxTotal}")
    private int maxTotal;
             
    @Value("${redis.pool.maxIdle}")
    private int maxIdle;
 
    @Value("${redis.pool.maxWaitMillis}")
    private int maxWaitMillis;
 
    //@Value("${redis.pool.testOnBorrow}")
    //private boolean  testOnBorrow;
    
    //@Value("${redis.pool.testOnReturn}")
    //private boolean  testOnReturn;

    @Bean
    public ShardedJedisPool shardedJedisPool() throws Exception{

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        //最大连接数：能够同时建立的“最大链接个数”
        jedisPoolConfig.setMaxTotal(maxTotal);
        //资源池允许最大空闲的连接数 默认值8
        jedisPoolConfig.setMaxIdle(maxIdle);
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        
        JedisShardInfo jedisShardInfo1 = new JedisShardInfo(host, port);
        List<JedisShardInfo> jedisShardInfoList = Arrays.asList(jedisShardInfo1);
        
        ShardedJedisPool shardedJedisPool = new ShardedJedisPool(jedisPoolConfig, jedisShardInfoList);
        return shardedJedisPool;
    }
}