package com.yklis.schedule.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 工具类:获取Spring Boot管理的bean
 * 一般在非Spring Boot管理的类中使用
 * 
 * 对于工具类，阿里开发手册规定，包名如果使用util不能带s，工具类名为XxxUtils
 * 
 * @author liuyi
 *
 */
@Component
public class SpringUtils implements ApplicationContextAware {

	private static ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		
		if(SpringUtils.applicationContext == null) {

            SpringUtils.applicationContext = applicationContext;
        }		
	}
	
    public static ApplicationContext getApplicationContext() {

        return applicationContext;
    }
    
    public static Object getBean(String name) {

        return getApplicationContext().getBean(name);
    }

    public static <T> T getBean(Class<T> clazz) {

        return getApplicationContext().getBean(clazz);
    }

    public static <T> T getBean(String name,Class<T> clazz) {

        return getApplicationContext().getBean(name,clazz);
    }
}
