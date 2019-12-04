package com.yklis.schedule.business.job;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.yklis.schedule.misc.WebSocketEquipMonitor;
import com.yklis.schedule.util.SpringUtils;

/**
 * 发送数据给设备监控界面
 * 
 * 广播(群发)
 * @author liuyi
 *
 */
public class JobWebSocketEquipMonitor implements Command {

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
			
        List<Map<String, Object>> list = null;
        try{

        	list = jdbcTemplate.queryForList("SELECT Unid,Type,Model,Remark,Supplier,Brand,ManuFacturer,Create_Date_Time FROM EquipManage ");
        }catch(Exception e){
            logger.error("jdbcTemplate.queryForList失败:"+e.toString());
        }
        
        for(Map<String, Object> map1 : list) {
        	
			String equipUnid = map1.get("unid").toString();
			
			int pkunid=0;
        	try{
        		//sql要求：
        		//1、有且仅有一条记录
        		//2、有且仅有一个字段
        		//3、字段在DB中的类型不限
        		pkunid = jdbcTemplate.queryForObject("SELECT max(pkunid) FROM view_chk_valu_All WITH(NOLOCK) where EquipUnid="+equipUnid,int.class);
        	}catch(Exception e){
                logger.error("jdbcTemplate.queryForObject失败:"+e.toString());
        	}
        	
            List<Map<String, Object>> list2 = null;
            try{

            	list2 = jdbcTemplate.queryForList("select patientname,check_date from dbo.view_Chk_Con_All WITH(NOLOCK) where unid="+pkunid);
            }catch(Exception e){
                logger.error("jdbcTemplate.queryForList失败:"+e.toString());
            }
        	
				        
            /*String doctor_id = "";
            if(null!=map1.get("id")){
            	doctor_id = map1.get("id").toString();
            }
                
			if (!WebSocketNewValue.wsMap.containsKey(doctor_id)) continue;
				
			try {
				WebSocketNewValue.wsMap.get(doctor_id).getSession().getBasicRemote().sendText("hello " + doctor_id + ",新结果:"+map1.get("patientname"));
			} catch (IOException e) {
				logger.error("WebSocket sendText错误");
			}*/
			
			
	        for (WebSocketEquipMonitor item : WebSocketEquipMonitor.wsSet) {
	            try {
	           	
	            	item.getSession().getBasicRemote().sendText("");
	            } catch (IOException e) {
	            	logger.error("设备监控WebSocket sendText错误");
	            }
	        }			
        }
	}
}
