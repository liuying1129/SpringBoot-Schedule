package com.yklis.schedule;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * 右键启动
 * 即右键入口main函数所在的文件就能启动整个项目
 * @author liuyi
 *
 */
@SpringBootApplication
//扫描指定包中的Mybatis接口，然后创建各自接口的动态代理类
@MapperScan(value = {"com.yklis.schedule.dao"})
//扫描本目录以及子目录的WebServlet注解
@ServletComponentScan
public class ScheduleApplication {
    
	public static void main(String[] args) {
				
		SpringApplication.run(ScheduleApplication.class, args);
	}

}

