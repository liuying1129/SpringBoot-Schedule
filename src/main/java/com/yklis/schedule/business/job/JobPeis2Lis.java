package com.yklis.schedule.business.job;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.yklis.schedule.util.SpringUtils;

/**
 * 越秀区中医医院
 * 北京标软PEIS->LIS
 * 
 * @author liuyi
 *
 */
public class JobPeis2Lis implements Command {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private JdbcTemplate jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);

    @Override
	public void execute(Map<String, Object> map) {
		

	}
}
