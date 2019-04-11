package com.yklis.schedule.business.job;

import com.yklis.schedule.business.job.Command;
import com.yklis.schedule.misc.WebSocketNewValue;
import com.yklis.schedule.util.SpringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 病人有新结果时，向送检医生发送消息
 * bug:转到bak的病人不提醒
 * @author liuyi
 *
 */
public class JobWebSocketNewValue implements Command {

    //配置容器起动时候加载log4j配置文件
    //只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
    //在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
    //如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
    //PropertyConfigurator.configure("log4jj.properties");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    //类全局使用(递增),故使用static
    private static int unid;
	
	@Override
	public void execute(Map<String, Object> map) {
		
        JdbcTemplate jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
                
        if(0==unid) {
        	try{
        		//sql要求：
        		//1、有且仅有一条记录
        		//2、有且仅有一个字段
        		//3、字段在DB中的类型不限
        		unid = jdbcTemplate.queryForObject("select MAX(unid) from chk_con where isnull(report_doctor,'')<>'' ",int.class);
        	}catch(Exception e){
                logger.error("jdbcTemplate.queryForObject失败:"+e.toString());
        	}
        }
                        
        List<Map<String, Object>> list = null;
        try{
        	//bug:如果有多个相同名称的送检医生，会产生重复的病人结果，但送检医生id不同,即该结果会向每个相同名称的送检医生发送(其实问题也不大)
            list = jdbcTemplate.queryForList("SELECT cc.unid,cc.patientname,lower(w.id) as id FROM Chk_Con cc,worker w WHERE cc.check_doctor=w.name and cc.unid>"+unid+" AND isnull(cc.report_doctor,'')<>'' order by cc.unid ");
        }catch(Exception e){
            logger.error("jdbcTemplate.queryForList失败:"+e.toString());
        }
        
        for(Map<String, Object> map1 : list) {
        	
			unid = Integer.parseInt(map1.get("unid").toString());
				        
            String doctor_id = "";
            if(null!=map1.get("id")){
            	doctor_id = map1.get("id").toString();
            }
                
			if (!WebSocketNewValue.wsMap.containsKey(doctor_id)) continue;
				
			try {
				WebSocketNewValue.wsMap.get(doctor_id).getSession().getBasicRemote().sendText("hello " + doctor_id + ",新结果:"+map1.get("patientname"));
			} catch (IOException e) {
				logger.error("WebSocket sendText错误");
			}
        }
	}
}
