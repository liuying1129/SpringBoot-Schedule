package com.yklis.schedule.business.job;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;

import com.yklis.schedule.config.CustomerContextHolder;
import com.yklis.schedule.entity.CommCodeEntity;
import com.yklis.schedule.util.SpringUtils;

import oracle.jdbc.OracleTypes;

/**
 * 回传检验结果给万达HIS
 * 接口文档:区域LIS与HIS接口[存储过程方式]文档
 * 后端JOB类，回传成功后将chk_con.LeftEyesight “回传万达成功”“回传万达失败或具体错误信息”
 * @author liuyi
 *
 * Oracle JDBC驱动：https://blog.csdn.net/erlian1992/article/details/74279106
 */

public class JobLis2WondersHis implements Command {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private JdbcTemplate jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
    
    //JAVA规定，如果类中没有定义任何构造函数，JVM自动为其生成一个默认的构造函数
    //故可不需要手动写下面的构造函数
    //public JobSPH2CJ(){       
    //}

	@Override
	public void execute(Map<String, Object> map) {
		
        List<Map<String, Object>> list = null;
        try{
            list = jdbcTemplate.queryForList("select * from chk_con where isnull(chk_con.report_doctor,'')<>'' and isnull(LeftEyesight,'')<>'回传万达成功' ");
        }catch(Exception e){            
            logger.error("获取待回传万达列表失败:"+e.toString());
            return;
        }
        
        //切换数据源的变量准备工作start
		String selfClassName = this.getClass().getName();		
		int jdbcUnid = CustomerContextHolder.getJdbcUnidFromJobClass(selfClassName);				
		CommCodeEntity commCodeEntity = CustomerContextHolder.getConnectionInfo(jdbcUnid);
		Map<String,Object> customerTypeMap = new HashMap<String,Object>();
		if(commCodeEntity!=null){
			customerTypeMap.put("driverClass", commCodeEntity.getReserve());
			customerTypeMap.put("url", commCodeEntity.getReserve2());
			customerTypeMap.put("user", commCodeEntity.getReserve3());
			customerTypeMap.put("password", commCodeEntity.getReserve4());
		}
        //切换数据源的变量准备工作stop
        
        for(Map<String, Object> map1 : list) {
        	
        	map1.get("unid").toString();
        	map1.get("LSH").toString();//样本号
        	map1.get("Caseno").toString();//病历号
        	map1.get("patientname").toString();
        	map1.get("sex").toString();
        	map1.get("age").toString();
        	map1.get("operator").toString();//检验医生
        	map1.get("report_doctor").toString();//报告医生
        	map1.get("Audit_Date").toString();//报告时间
        	map1.get("issure").toString();//备注
        	
        	List resultList = (List) jdbcTemplate.execute(
    			     new CallableStatementCreator() {
    			        public CallableStatement createCallableStatement(Connection con) throws SQLException {
    			           String storedProc = "{call usp_yjjk_jcbrfb(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";//调用的sql
    			           CallableStatement cs = con.prepareCall(storedProc);
    			           cs.setString(1, "p1");//设置输入参数的值
    			           cs.setString(1, "p1");//设置输入参数的值
    			           cs.setString(1, "p1");//设置输入参数的值
    			           cs.setString(1, "p1");//设置输入参数的值
    			           cs.setString(1, "p1");//设置输入参数的值
    			           cs.setString(1, "p1");//设置输入参数的值
    			           cs.setString(1, "p1");//设置输入参数的值
    			           cs.setString(1, "p1");//设置输入参数的值
    			           cs.setString(1, "p1");//设置输入参数的值
    			           cs.registerOutParameter(2, OracleTypes.CURSOR);//注册输出参数的类型
    			           return cs;
    			        }
    			     }, new CallableStatementCallback() {
    			        public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {
    			           List resultsMap = new ArrayList();
    			           cs.execute();
    			           ResultSet rs = (ResultSet) cs.getObject(2);// 获取游标一行的值
    			           while (rs.next()) {// 转换每行的返回值到Map中
    			              Map rowMap = new HashMap();
    			              rowMap.put("id", rs.getString("id"));
    			              rowMap.put("name", rs.getString("name"));
    			              resultsMap.add(rowMap);
    			           }
    			           rs.close();
    			           return resultsMap;
    			        }
    			  });
    			  for (int i = 0; i < resultList.size(); i++) {
    			     Map rowMap = (Map) resultList.get(i);
    			     String id = rowMap.get("id").toString();
    			     String name = rowMap.get("name").toString();
    			     System.out.println("id=" + id + ";name=" + name);
    			  }       	
        }
	}

}
