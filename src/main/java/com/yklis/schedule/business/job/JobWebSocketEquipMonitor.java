package com.yklis.schedule.business.job;

import java.io.IOException;
import java.util.HashMap;
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
    //private static int unid;

	@Override
	public void execute(Map<String, Object> map) {
		
		if(WebSocketEquipMonitor.wsSet.size()<=0) return;//表示没有socket客户端连接
		
		JdbcTemplate jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
			
        List<Map<String, Object>> list = null;
        try{

        	list = jdbcTemplate.queryForList("SELECT Unid,Type,Model,Remark,Supplier,Brand,ManuFacturer,Create_Date_Time FROM EquipManage WITH(NOLOCK) ");
        }catch(Exception e){
            logger.error("jdbcTemplate.queryForList失败:"+e.toString());
        }
        
        for(Map<String, Object> map1 : list) {
        	
			String equipUnid = map1.get("unid").toString();
			
			//获取指定设备最后检验结果所对应的pkunid
	        List<Map<String, Object>> list3 = null;
	        try{

	        	list3 = jdbcTemplate.queryForList("SELECT TOP 1 pkunid,ifCompleted FROM view_chk_valu_All WITH(NOLOCK) where EquipUnid="+equipUnid+" order by valueid desc ");
	        }catch(Exception e){
	            logger.error("jdbcTemplate.queryForList失败:"+e.toString());
	        }
	        
	        String pkunid="0";
	        String ifCompleted = null;
	        for(Map<String, Object> map3 : list3) {
	        	
	        	pkunid = map3.get("pkunid").toString();
	        	ifCompleted = map3.get("ifCompleted").toString();	        	
	        }
	               
	        //根据pkunid获取病人信息
            List<Map<String, Object>> list2 = null;
        	if(Integer.parseInt(pkunid)>0) {        		
	            try{
	
	            	list2 = jdbcTemplate.queryForList("select patientname,check_date from "+("1".equals(ifCompleted)?"chk_con_bak":"chk_con")+" WITH(NOLOCK) where unid="+pkunid);
	            }catch(Exception e){
	                logger.error("jdbcTemplate.queryForList失败:"+e.toString());
	            }
        	}
        	
            String patientname = null;
            String check_date = null;
	        for(Map<String, Object> map2 : list2) {
	        	
	        	patientname = map2.get("patientname").toString();
	        	check_date = map2.get("check_date").toString();	        	
	        }
	        
            Map<String, Object> mapSend = new HashMap<>();
            mapSend.put("equipUnid", equipUnid);
            mapSend.put("patientname", patientname);
            mapSend.put("check_date", check_date);
            
            //return JSON.toJSONString(map);
			
	        //发送信息
	        for (WebSocketEquipMonitor wsItem : WebSocketEquipMonitor.wsSet) {
	            try {
	           	
	            	wsItem.getSession().getBasicRemote().sendText(equipUnid + patientname+" "+check_date);
	            } catch (IOException e) {
	            	logger.error("设备监控WebSocket sendText错误");
	            }
	        }			
        }
	}
}
