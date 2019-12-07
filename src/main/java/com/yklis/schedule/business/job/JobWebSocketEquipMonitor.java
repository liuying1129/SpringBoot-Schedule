package com.yklis.schedule.business.job;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.fastjson.JSON;
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
    
	@Override
	public void execute(Map<String, Object> map) {
		
		if(null == WebSocketEquipMonitor.wsSet) return;//表示没有socket客户端连接
		if(WebSocketEquipMonitor.wsSet.size()<=0) return;//表示没有socket客户端连接
		
		JdbcTemplate jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
			
        List<Map<String, Object>> list = null;
        try{

        	list = jdbcTemplate.queryForList("SELECT * FROM EquipManage WITH(NOLOCK) ");
        }catch(Exception e){
            logger.error("jdbcTemplate.queryForList失败:"+e.toString());
        }
        
        for(Map<String, Object> map1 : list) {
        	
			String equipUnid = map1.get("Unid").toString();
			
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
	
	            	list2 = jdbcTemplate.queryForList("select patientname,CONVERT(CHAR(19),check_date,121) AS check_date from "+("1".equals(ifCompleted)?"chk_con_bak":"chk_con")+" WITH(NOLOCK) where unid="+pkunid);
	            }catch(Exception e){
	                logger.error("jdbcTemplate.queryForList失败:"+e.toString());
	            }
        	}
        	
            String patientname = null;
            String check_date = null;
            
            if(null!=list2) {
            	
		        for(Map<String, Object> map2 : list2) {
		        	
		        	patientname = null==map2.get("patientname") ? null : map2.get("patientname").toString();
		        	check_date = null==map2.get("check_date") ? null : map2.get("check_date").toString();
		        }
            }
            
            patientname=replaceNameX(patientname);
            
	        //指定设备当天的样本数量
	        int todayNum = 0;
        	try{
        		//sql要求：
        		//1、有且仅有一条记录
        		//2、有且仅有一个字段
        		//3、字段在DB中的类型不限
        		todayNum = jdbcTemplate.queryForObject("select count(distinct pkunid) from view_chk_valu_All c WITH(NOLOCK),view_Chk_Con_All z WITH(NOLOCK) where z.unid=c.pkunid and CONVERT(CHAR(10),z.check_date,121)=CONVERT(CHAR(10),getdate(),121) and c.EquipUnid="+equipUnid,int.class);
        	}catch(Exception e){
                logger.error("jdbcTemplate.queryForObject失败:"+e.toString());
        	}
        	
	        //指定设备当月的样本数量
	        int thisMonthNum = 0;
        	try{
        		//sql要求：
        		//1、有且仅有一条记录
        		//2、有且仅有一个字段
        		//3、字段在DB中的类型不限
        		thisMonthNum = jdbcTemplate.queryForObject("select count(distinct pkunid) from view_chk_valu_All c WITH(NOLOCK),view_Chk_Con_All z WITH(NOLOCK) where z.unid=c.pkunid and z.check_date>=DATEADD(MONTH,DATEDIFF(MONTH,0,getdate()),0) and z.check_date<=dateadd(ms,-3,DATEADD(mm,DATEDIFF(m,0,getdate())+1,0)) and c.EquipUnid="+equipUnid,int.class);
        	}catch(Exception e){
                logger.error("jdbcTemplate.queryForObject失败:"+e.toString());
        	}
                        	        
	        //将病人信息转换为待发送的map
            Map<String, Object> mapSend = new HashMap<>();
            mapSend.put("equipUnid", equipUnid);
            mapSend.put("patientname", null==patientname||"".equals(patientname)?"未知姓名":patientname);
            mapSend.put("check_date", null==check_date||"".equals(check_date)?"未知时间":check_date);
            mapSend.put("todayNum", todayNum);
            mapSend.put("thisMonthNum", thisMonthNum);
                		
	        //发送信息
	        for (WebSocketEquipMonitor wsItem : WebSocketEquipMonitor.wsSet) {
	            try {
	           	
	            	wsItem.getSession().getBasicRemote().sendText(JSON.toJSONString(mapSend));
	            } catch (IOException e) {
	            	logger.error("设备监控WebSocket sendText错误");
	            }
	        }
        }
	}
	
	/**
	 * 正则姓名加密，保留姓，名用*号代替
	 * @param str
	 * @return
	 */
	private String replaceNameX(String nameStr){
		
		if(null==nameStr) return null;

		String reg = ".{1}";
		StringBuffer sb = new StringBuffer();
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(nameStr);
		int i = 0;
		while(m.find()){
			i++;
			if(i==1) continue;
			m.appendReplacement(sb, "*");
		}
		m.appendTail(sb);
		return sb.toString();
	}	
}
