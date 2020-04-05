package com.yklis.schedule.business.job;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.yklis.schedule.util.SpringUtils;

/**
 * 删除为0的耗材库存
 * 
 * 强烈建议23:03执行:0 3 23 * * ?
 * @author liuyi
 *
 */
public class JobCleanSuppliesInventory implements Command {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    //JAVA规定，如果类中没有定义任何构造函数，JVM自动为其生成一个默认的构造函数
    //故可不需要手动写下面的构造函数
    //public JobSPH2CJ(){       
    //}
    
	@Override
	public void execute(Map<String, Object> map) {
		
        JdbcTemplate jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
        
        try{
            jdbcTemplate.update("delete from SJ_KC where SL=0");
        }catch(Exception e){            
            logger.error("jdbcTemplate.update失败:"+e.toString());
        }
	}
}
