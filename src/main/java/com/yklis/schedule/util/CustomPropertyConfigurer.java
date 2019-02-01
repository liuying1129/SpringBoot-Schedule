package com.yklis.schedule.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * 享元模式(又叫蝇量模式)
 * 用HashMap存储对象,重用
 * 
 * 继承PropertyPlaceholderConfigurer
 * 使Java普通类能读取applicationContext.xml定义的配置文件的键值
 * applicationContext.xml中配置为该类
 * @author ying07.liu
 *
 */
public class CustomPropertyConfigurer extends PropertyPlaceholderConfigurer {
    
	//final
	//如果是基本数据类型的变量，则其数值一旦在初始化之后便不能更改
	//如果是引用类型的变量，则在对其初始化之后便不能再让其指向另一个对象.但是对象本身是可以被修改的
    private static final Map<String, String> ctxPropertiesMap = new HashMap<>();

    @Override
    protected void processProperties(
            ConfigurableListableBeanFactory beanFactoryToProcess,
            Properties props) throws BeansException {
        
        super.processProperties(beanFactoryToProcess, props);        
        
        //扩展内容:将键值写入map
        for (Object key : props.keySet()) {
            String keyStr = key.toString();
            String value = props.getProperty(keyStr);
            
            ctxPropertiesMap.put(keyStr, value);
        }
    }
    
    /**
     * Java普通类调用该方法获取配置文件的值
     * @param key
     * @return
     */
    protected static String getProperty(String key){
        
        return ctxPropertiesMap.get(key);
    }
}
