package com.yklis.schedule.business.job;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yklis.schedule.util.Constants;
import com.yklis.schedule.util.SpringUtils;

/**
 * 命令模式
 * 命令实现类
 * 
 * 送货单运费费用回填上药
 * @author ying07.liu
 *
 */
public class JobSPHPushCostSendSheet implements Command {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//JAVA规定，如果类中没有定义任何构造函数，JVM自动为其生成一个默认的构造函数
    //故可不需要手动写下面的构造函数
	//public JobSPHPushCostSendSheet(){		
	//}

    @Override
	public void execute(Map<String,Object> map) {
		
        JdbcTemplate jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
        
		List<Map<String, Object>> list;
        try{                        	
        	list = jdbcTemplate.queryForList("select * from FeeInfo fi where fi.SC_COMPANY='"+Constants.SPH_SC_COMPANY+"' and isnull(fi.Send_Flag,0)<>1 ");
        }catch(Exception e){
        	logger.error("获取订单接口PushDeliverInfo,jdbcTemplate.queryForList报错"+e.toString());
        	return;
        }
        
        for (int i = 0; i < list.size(); i++) {
        	
            URL url = null;
            try {
                url = new URL(Constants.SPH_BASE_URL+"PushCostSendSheet/1");
            } catch (MalformedURLException e) {
                logger.error("new URL失败:"+e.toString());
            }
            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                logger.error("url.openConnection失败:"+e.toString());
            }
            try {
                httpURLConnection.setRequestMethod("POST");
            } catch (ProtocolException e) {
                logger.error("httpURLConnection.setRequestMethod失败:"+e.toString());
            }
            
            //设置连接主机超时（单位：毫秒）
            httpURLConnection.setConnectTimeout(9000);
            //设置从主机读取数据超时（单位：毫秒）
            httpURLConnection.setReadTimeout(9000);
            //设置是否向httpUrlConnection输出,因为这个是post请求,参数要放在http正文内,因此需要设为true, 默认情况下是false
            httpURLConnection.setDoOutput(true);
            //设置是否从httpUrlConnection读入,默认情况下是true
            httpURLConnection.setDoInput(true);
            //Post请求不能使用缓存
            httpURLConnection.setUseCaches(false);
            //表示请求参数为JSON格式
            httpURLConnection.setRequestProperty("Content-Type","application/json");

            JSONObject objItem = new JSONObject();
            objItem.put("BILLNO", list.get(i).get("SC_YDH"));
            objItem.put("PUSH_PRICE", list.get(i).get("INSURANCEFEE"));//运费单价//使用界面的保险费字段
            objItem.put("PUSH_START_COST", list.get(i).get("ADDSPORTFEE"));//起运费//使用界面的加点费字段
            objItem.put("PUSH_AREA_COST", list.get(i).get("DISTANCEFEE"));//区域附加费//使用界面的超远配送费字段
            objItem.put("PUSH_COLD_COST", list.get(i).get("OTHERFEE"));//冷狗回收费//使用界面的其他费用字段
            objItem.put("PUSH_WARM_COST", list.get(i).get("PACKAGING"));//保温箱回收费//使用界面的特殊包装费字段
            objItem.put("PUSH_TOTAL_COST", list.get(i).get("CARRIAGEFEE"));//总费用//使用界面的运费字段
            
            JSONArray arrayItem=new JSONArray();
            arrayItem.add(objItem);
            
            JSONObject objParam = new JSONObject();
            objParam.put("AUTHOR_CODE", Constants.SPH_AUTHOR_CODE);
            objParam.put("ITEMS", arrayItem);

            PrintWriter printWriter = null;
            try {
                printWriter = new PrintWriter(httpURLConnection.getOutputStream());

                //String param = "AUTHOR_CODE=1710153675&PUSH_STATE=0&BEGIN_DATE=2017-12-07&END_DATE=2017-12-07";
                printWriter.write(objParam.toString());
            } catch (IOException e) {
                logger.error("httpURLConnection.getOutputStream失败:"+e.toString());
            } finally {
                if (printWriter != null) printWriter.close();
            }
            
            //开始获取数据
            int responseCode = 0;
            try {
                responseCode = httpURLConnection.getResponseCode();
            } catch (IOException e) {
                logger.error("httpURLConnection.getResponseCode失败:"+e.toString());
            }

            if(responseCode!=HttpURLConnection.HTTP_OK){
                
                //只有在httpURLConnection.HTTP_OK的情况下才能读取返回信息
                logger.info("请求远程用户信息接口,返回非200代码:"+responseCode);
            }else{
            	
                StringBuffer sb=new StringBuffer();
                String line;
                BufferedReader bufferedReader = null;
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(),"UTF-8"));
                    while((line=bufferedReader.readLine())!=null){
                        sb.append(line);
                    }            
                } catch (UnsupportedEncodingException e) {
                        logger.error("上药读取送货单接口的读取返回值,new InputStreamReader失败:"+e.toString());
                } catch (IOException e) {
                        logger.error("上药读取送货单接口的读取返回值,httpURLConnection.getInputStream或bufferedReader.readLine失败:"+e.toString());
                } finally {
                    if (bufferedReader != null)
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            logger.error("bufferedReader.close失败:"+e.toString());
                        }
                }
                
                if(null!=sb&&!"".equals(sb.toString())){
                	
                    String ss1 = sb.toString();
                    ss1 = ss1.replace("\"{", "{");
                    ss1 = ss1.replace("}\"", "}");
                    ss1 = ss1.replace("\\\"", "\"");
                    logger.info("接口PushDeliverInfo返回2:"+ss1);
                    
                    JSONObject jso=JSON.parseObject(ss1);//json字符串转换成JSONObject(JSON对象)
                    
                    boolean bb1 = jso.getBooleanValue("Success");
                    if(bb1){ 
                    	StringBuilder sb3 = new StringBuilder();
                    	sb3.append("update FeeInfo set Send_Flag=1 where sc_company='");
                    	sb3.append(Constants.SPH_SC_COMPANY);
                    	sb3.append("' and SC_YDH='");
                    	sb3.append(list.get(i).get("SC_YDH"));
                    	sb3.append("' and isnull(Send_Flag,0)<>1");
                    	
                        JdbcTemplate jdbcTemplate1 = SpringUtils.getBean(JdbcTemplate.class);
                        try{                        	
                        	jdbcTemplate1.execute(sb3.toString());
		                }catch(Exception e){
		                	logger.error("上传费用信息接口PushCostSendSheet,jdbcTemplate.execute报错"+e.toString());
		                }

                    }else{
                    	logger.warn("上传费用信息接口PushCostSendSheet返回的Success不为true:"+sb.toString());
                    }
                }
            }
        }
	}
}
