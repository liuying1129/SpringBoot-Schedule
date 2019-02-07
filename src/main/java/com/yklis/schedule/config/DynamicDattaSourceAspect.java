package com.yklis.schedule.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.yklis.schedule.util.CustomerContextHolder;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * 动态数据源通知
 * 
 * @author liuyi
 *
 */
@Aspect
//保证该AOP在@Transactional之前执行
@Order(-1)
@Component
public class DynamicDattaSourceAspect {

    private Logger logger = LoggerFactory.getLogger(DynamicDattaSourceAspect.class);

    /**
     * 改变数据源
     * 
     * @Before：在方法执行之前进行执行
     * @annotation(targetDataSource)：会拦截注解targetDataSource的方法，否则不拦截
     * 
     * @param joinPoint
     * @param targetDataSource
     */
    @Before("@annotation(targetDataSource)")
    public void changeDataSource(JoinPoint joinPoint, TargetDataSource targetDataSource) {
    	
        String dbid = targetDataSource.name();

        if (!CustomerContextHolder.containsDataSource(dbid)) {
            //joinPoint.getSignature() ：获取连接点的方法签名对象
            logger.error("数据源 " + dbid + " 不存在，使用默认的数据源 -> " + joinPoint.getSignature());
        } else {
            logger.debug("使用数据源：" + dbid);
            CustomerContextHolder.setDataSourceType(dbid);//.setDataSourceType(dbid);
        }
    }

    @After("@annotation(targetDataSource)")
    public void clearDataSource(JoinPoint joinPoint, TargetDataSource targetDataSource) {
    	
        logger.debug("清除数据源 " + targetDataSource.name() + " !");
        
        //方法执行完毕之后，销毁当前数据源信息，进行垃圾回收
        CustomerContextHolder.clearDataSourceType();
    }
}
