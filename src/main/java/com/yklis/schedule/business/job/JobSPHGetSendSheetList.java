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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yklis.schedule.util.Constants;

/**
 * 命令模式
 * 命令实现类
 * 
 * 获取上药送货单
 * @author ying07.liu
 *
 */
public class JobSPHGetSendSheetList implements Command {

    //配置容器起动时候加载log4j配置文件
    //只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
    //在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
    //如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
    //PropertyConfigurator.configure("log4jj.properties");
    private Logger logger = Logger.getLogger(this.getClass());
	
	//JAVA规定，如果类中没有定义任何构造函数，JVM自动为其生成一个默认的构造函数
    //故可不需要手动写下面的构造函数
	//public JobSPH2CJ(){		
	//}
	
    @Override
	public void execute(Map<String,Object> map) {
		
        URL url = null;
        try {
            url = new URL(Constants.SPH_BASE_URL+"GetSendSheetList/1");
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
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();
        String strToday = sdf.format(today);
        
        Calendar c = Calendar.getInstance();  
        c.setTime(today);  
        c.add(Calendar.DAY_OF_MONTH, -3);//今天+1天 
        Date yesterday = c.getTime();
        String strYesterday = sdf.format(yesterday);
        
        StringBuilder sbParam = new StringBuilder();
        sbParam.append("AUTHOR_CODE=");
        sbParam.append(Constants.SPH_AUTHOR_CODE);
        sbParam.append("&PUSH_STATE=0&BEGIN_DATE=");
        sbParam.append(strYesterday);
        sbParam.append("&END_DATE=");
        sbParam.append(strToday);
        
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(httpURLConnection.getOutputStream());

            //String param = "AUTHOR_CODE=1710153675&PUSH_STATE=0&BEGIN_DATE=2017-12-07&END_DATE=2017-12-07";
            printWriter.write(sbParam.toString());
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
                
                //将送货单写入CJ-TMS
                //logger.info("接口GetSendSheetList返回:"+sb.toString());
                
                String ss1 = sb.toString();
                ss1 = ss1.replace("\"{", "{");
                ss1 = ss1.replace("}\"", "}");
                ss1 = ss1.replace("\\\"", "\"");
                logger.info("接口GetSendSheetList返回2:"+ss1);
                
                JSONObject jso=JSON.parseObject(ss1);//json字符串转换成JSONObject(JSON对象)
                
                boolean bb1 = jso.getBooleanValue("Success");
                if(bb1){                	
                    
                    JSONArray jsarrData=jso.getJSONArray("Data");//JSONObject取得response对应的JSONArray(JSON数组)
                                    	
                    Map<String, Object> map1 = null;
                    for(int i=0;i<jsarrData.size();i++){   
                    	
                    	//{"BILLNO":"ZP17120601205","MID":26163,"S_ADDRESS":"惠州市鹅岭北路41号（静配中心）","S_ROADLINE":"粤东","PACK_TYPE":"普通","S_TEL":"0752-2288567","R_NAME":"上药控股广东有限公司","S_NAME":"惠州市中心人民医院","S_CONTACT":"林岭海","R_ADDRESS":"广州市黄埔区中山大道东138号","SEND_NUM":1,"OUT_DATE":"2017-12-07 15:18"}
                        map1 = jsarrData.getJSONObject(i);                                           	
                    	
                        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
                        
                    	StringBuilder sb2 = new StringBuilder();
                    	sb2.append("select count(*) as RecNum from Wait_Sched where SC_COMPANY='");
                    	sb2.append(Constants.SPH_SC_COMPANY);
                    	sb2.append("' and Reserve='");
                    	sb2.append(map1.get("MID"));
                    	sb2.append("'");
                    	
                    	Integer ii = 0;
                        JdbcTemplate jdbcTemplate2 = webApplicationContext.getBean(JdbcTemplate.class);
                        try{
                            ii = jdbcTemplate2.queryForObject(sb2.toString(), Integer.class);
                        }catch(Exception e){
                        	logger.error("获取订单接口GetSendSheetList,jdbcTemplate.queryForObject报错"+e.toString());
                        }
                        
                        if(ii<=0){
                        	
                            StringBuilder sb1 = new StringBuilder();
                            sb1.append("insert into Wait_Sched (SC_COMPANY,SC_YDH,SC_CUSTOMER,SC_SendAddr,SC_BillNo,Pick_Date_time,Send_Aear,Remark,Reserve,Reserve3,Reserve4,SC_BillNo_Js,SC_ORIG_PKT_QTY,SC_UNITS,KCFSL) values ('");
                            sb1.append(Constants.SPH_SC_COMPANY);
                            sb1.append("','");
                            sb1.append(map1.get("BILLNO"));//清单号放入运单号字段，因为SPH按清单号计费（刚好SPH也没有运单号概念）
                            sb1.append("','");
                            sb1.append(map1.get("S_NAME"));
                            sb1.append("','");
                            sb1.append(map1.get("S_ADDRESS"));
                            sb1.append(" ");
                            sb1.append(map1.get("S_CONTACT"));
                            sb1.append(" ");
                            sb1.append(map1.get("S_TEL"));
                            sb1.append("','");
                            sb1.append(map1.get("BILLNO"));
                            sb1.append("','");
                            sb1.append(map1.get("OUT_DATE"));
                            sb1.append("','");
                            sb1.append(map1.get("S_ROADLINE"));
                            sb1.append("','");
                            sb1.append(map1.get("R_NAME"));
                            sb1.append("','");
                            sb1.append(map1.get("MID"));
                            sb1.append("','");
                            sb1.append(map1.get("PACK_TYPE"));
                            sb1.append("','");
                            sb1.append(map1.get("R_ADDRESS"));
                            sb1.append(" ");
                            sb1.append(map1.get("R_CONTACT"));
                            sb1.append(" ");
                            sb1.append(map1.get("R_TEL"));
                            sb1.append("',");
                            sb1.append(map1.get("SEND_NUM"));
                            sb1.append(",");
                            sb1.append(map1.get("SEND_NUM"));
                            sb1.append(",'件',");
                            sb1.append(map1.get("SEND_NUM"));
                            sb1.append(")");
                        	
                            JdbcTemplate jdbcTemplate = webApplicationContext.getBean(JdbcTemplate.class);
                            try{                        	
                            	jdbcTemplate.execute(sb1.toString());
                            	
                            	//插入成功后调用“获取送货单成功回填状态接口”begin
                                URL url2 = null;
                                try {
                                    url2 = new URL(Constants.SPH_BASE_URL+"PushStateSendSheet/1");
                                } catch (MalformedURLException e) {
                                    logger.error("new URL失败:"+e.toString());
                                }
                                HttpURLConnection httpURLConnection2 = null;
                                try {
                                    httpURLConnection2 = (HttpURLConnection) url2.openConnection();
                                } catch (IOException e) {
                                    logger.error("url2.openConnection失败:"+e.toString());
                                }
                                try {
                                    httpURLConnection2.setRequestMethod("POST");
                                } catch (ProtocolException e) {
                                    logger.error("httpURLConnection2.setRequestMethod失败:"+e.toString());
                                }
                                
                                //设置连接主机超时（单位：毫秒）
                                httpURLConnection2.setConnectTimeout(9000);
                                //设置从主机读取数据超时（单位：毫秒）
                                httpURLConnection2.setReadTimeout(9000);
                                //设置是否向httpUrlConnection输出,因为这个是post请求,参数要放在http正文内,因此需要设为true, 默认情况下是false
                                httpURLConnection2.setDoOutput(true);
                                //设置是否从httpUrlConnection读入,默认情况下是true
                                httpURLConnection2.setDoInput(true);
                                //Post请求不能使用缓存
                                httpURLConnection2.setUseCaches(false);
                                
                                StringBuilder sbParam2 = new StringBuilder();
                                sbParam2.append("AUTHOR_CODE=");
                                sbParam2.append(Constants.SPH_AUTHOR_CODE);
                                sbParam2.append("&STATE=1&BILLNOS=");
                                sbParam2.append(map1.get("BILLNO"));
                                
                                PrintWriter printWriter2 = null;
                                try {
                                    printWriter2 = new PrintWriter(httpURLConnection2.getOutputStream());

                                    //String param = "AUTHOR_CODE=1710153675&STATE=1&BILLNOS="+map1.get("BILLNO");
                                    printWriter2.write(sbParam2.toString());
                                } catch (IOException e) {
                                    logger.error("httpURLConnection2.getOutputStream失败:"+e.toString());
                                } finally {
                                    if (printWriter2 != null) printWriter2.close();
                                }
                                
                                int responseCode2 = 0;
                                try {
                                    responseCode2 = httpURLConnection2.getResponseCode();
                                } catch (IOException e) {
                                    logger.error("httpURLConnection2.getResponseCode失败:"+e.toString());
                                }

                                if(responseCode2!=HttpURLConnection.HTTP_OK){
                                    
                                    //只有在httpURLConnection.HTTP_OK的情况下才能读取返回信息
                                    logger.info("请求【获取送货单成功回填状态接口】,返回非200代码:"+responseCode);
                                }else{
                                    StringBuffer sb22=new StringBuffer();
                                    String line22;
                                    BufferedReader bufferedReader22 = null;
                                    try {
                                        bufferedReader22 = new BufferedReader(new InputStreamReader(httpURLConnection2.getInputStream(),"UTF-8"));
                                        while((line22=bufferedReader22.readLine())!=null){
                                            sb22.append(line22);
                                        }            
                                    } catch (UnsupportedEncodingException e) {
                                            logger.error("上药读取送货单接口的读取返回值,new InputStreamReader失败:"+e.toString());
                                    } catch (IOException e) {
                                            logger.error("上药读取送货单接口的读取返回值,httpURLConnection.getInputStream或bufferedReader.readLine失败:"+e.toString());
                                    } finally {
                                        if (bufferedReader22 != null)
                                            try {
                                                bufferedReader22.close();
                                            } catch (IOException e) {
                                                logger.error("bufferedReader.close失败:"+e.toString());
                                            }
                                    }
                                    
                                    if(null!=sb22&&!"".equals(sb22.toString())){
                                    	//logger.info("接口PushStateSendSheet返回:"+sb22.toString());
                                    }
                                }
                            	//插入成功后调用“获取送货单成功回填状态接口”end//*/
                                
    		                }catch(Exception e){
    		                	logger.error("获取订单接口GetSendSheetList,jdbcTemplate.execute报错"+e.toString());
    		                }
                        }                        
                    }
                }else{
                	logger.warn("获取订单接口GetSendSheetList返回的Success不为true");
                }
            }
        }
	}
}
