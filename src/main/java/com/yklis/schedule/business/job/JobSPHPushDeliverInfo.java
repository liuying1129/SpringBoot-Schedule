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
import java.net.URLEncoder;
import java.util.List;
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
 * 送货单物流信息回填上药
 * @author ying07.liu
 *
 */
public class JobSPHPushDeliverInfo implements Command {

    //配置容器起动时候加载log4j配置文件
    //只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
    //在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
    //如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
    //PropertyConfigurator.configure("log4jj.properties");
    private Logger logger = Logger.getLogger(this.getClass());
	
	//JAVA规定，如果类中没有定义任何构造函数，JVM自动为其生成一个默认的构造函数
    //故可不需要手动写下面的构造函数
	//public JobSPHPushDeliverInfo(){
	//}
	
    @Override
	public void execute(Map<String,Object> map) {
		
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
        JdbcTemplate jdbcTemplate = webApplicationContext.getBean(JdbcTemplate.class);
        
        StringBuilder sb11 = new StringBuilder();
        sb11.append("select '节点跟踪信息' as InfoType,gt.SC_BillNo,gt.Node_Name,gt.Node_Desc,convert(varchar(50),gt.Create_Date_Time,120) as Create_Date_Time,dbo.uf_Concat_Bus(yd_c.ydh) as Bus,gt.Unid from Goods_Track gt ");
        sb11.append(" left join yd_c on gt.SC_COMPANY=yd_c.SC_COMPANY and gt.SC_BillNo=yd_c.SC_BillNo ");
        sb11.append("where gt.sc_company='");
        sb11.append(Constants.SPH_SC_COMPANY);
        sb11.append("' and isnull(gt.Send_Flag,0)<>1 ");
        sb11.append(" union all ");
        sb11.append("select '异常签收信息' as InfoType,SC_BillNo,'异常签收' as Node_Name,'异常数量:'+ltrim(str(ExpSignQty))+',原因:'+ExpSignCauseCode+','+ExpSignDesc as Node_Desc,convert(varchar(50),ExpSignTime,120) as Create_Date_Time,dbo.uf_Concat_Bus(ydh) as Bus,Unid from YD_C where sc_company='");
        sb11.append(Constants.SPH_SC_COMPANY);
        sb11.append("' and ExpSignQty>0 and isnull(Send_ExpSign_Flag,0)<>1 ");
        
		List<Map<String, Object>> list;
        try{                        	
        	list = jdbcTemplate.queryForList(sb11.toString());
        }catch(Exception e){
        	logger.error("获取订单接口PushDeliverInfo,jdbcTemplate.queryForList报错"+e.toString());
        	return;
        }
        
        for (int i = 0; i < list.size(); i++) {
        
            URL url = null;
            try {
                url = new URL(Constants.SPH_BASE_URL+"PushDeliverInfo/1");
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
            objItem.put("BILLNO", list.get(i).get("SC_BillNo"));
            try {
				objItem.put("DELIVER_STATE", URLEncoder.encode(list.get(i).get("Node_Name").toString(),"UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error("URLEncoder.encode失败:"+e.toString());
			}
            objItem.put("DELIVER_DATE", list.get(i).get("Create_Date_Time"));
            try {
				objItem.put("DELIVER_INFO", URLEncoder.encode(list.get(i).get("Node_Desc").toString(),"UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error("URLEncoder.encode失败:"+e.toString());
			}
            try {
				objItem.put("CARRIER_NAME", URLEncoder.encode("超俊","UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error("URLEncoder.encode失败:"+e.toString());
			}
            try {
				objItem.put("NOTES", URLEncoder.encode("","UTF-8"));//以后可能会有中文,故保留encode
			} catch (UnsupportedEncodingException e) {
				logger.error("URLEncoder.encode失败:"+e.toString());
			}
            String strBus = "";
            if(null!=list.get(i).get("Bus")){
            	strBus = list.get(i).get("Bus").toString();
            }
            try {
				objItem.put("LICENCE", URLEncoder.encode(strBus,"UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error("URLEncoder.encode失败:"+e.toString());
			}
            
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
                    //logger.info("接口PushDeliverInfo返回2:"+ss1);
                    
                    JSONObject jso=JSON.parseObject(ss1);//json字符串转换成JSONObject(JSON对象)
                    
                    boolean bb1 = jso.getBooleanValue("Success");
                    if(bb1){ 
                    	StringBuilder sb3 = new StringBuilder();
                    	switch(list.get(i).get("InfoType").toString()){
                    	case "节点跟踪信息":
                        	sb3.append("update Goods_Track set Send_Flag=1 where Unid=");
                        	sb3.append(list.get(i).get("Unid"));
                    		break;
                    	case "异常签收信息":
                        	sb3.append("update YD_C set Send_ExpSign_Flag=1 where Unid=");
                        	sb3.append(list.get(i).get("Unid"));
                    		break;
                    	}                   	
                        JdbcTemplate jdbcTemplate1 = webApplicationContext.getBean(JdbcTemplate.class);
                        try{                        	
                        	jdbcTemplate1.execute(sb3.toString());
		                }catch(Exception e){
		                	logger.error("上传物流信息接口PushDeliverInfo,jdbcTemplate.execute报错"+e.toString());
		                }

                    }else{
                    	logger.warn("上传物流信息接口PushDeliverInfo返回的Success不为true:"+sb.toString());
                    }
                }
            }
        }
	}
}
