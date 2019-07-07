package com.yklis.schedule.business.job;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.yklis.schedule.config.CustomerContextHolder;
import com.yklis.schedule.entity.CommCodeEntity;
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
    
    //JAVA规定，如果类中没有定义任何构造函数，JVM自动为其生成一个默认的构造函数
    //故可不需要手动写下面的构造函数
    //public JobSPH2CJ(){       
    //}

    @Override
	public void execute(Map<String, Object> map) {
		
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
		
		List<Map<String, Object>> list11 = null;
		
		final String strQuery11 = "select IP.ID_Patient ,IP.PatientName ," + 
                "IP.Org_Name ,IP.Org_Depart ,IP.Sex ," + 
                "IP.Age ,IP.AgeUnit ,IP.Marriage ,IP.DateRegister ,IP.DoctorReg ," + 
                "IPFI.ID_PatientFeeItem ,IPFI.PatientCode ," + 
                "IPFI.ExamFeeItem_Code ,IPFI.LabType_Code ,IPFI.LabSampleTime " + 
                " from IntPatient IP " + 
                " inner join IntPatientFeeItem IPFI " + 
                " on IP.ID_Patient=IPFI.ID_Patient " + 
                " AND IPFI.F_LabSampled=1 " + 
                " AND IPFI.TransfterTarget='LIS' " + 
                " and IPFI.LabSampleTime+0.8>getdate() ";
		try{	
			
			CustomerContextHolder.setCustomerType(customerTypeMap);						
				
    		list11 = jdbcTemplate.queryForList(strQuery11);
			            
		}catch(Exception e){
			logger.error("切换数据源，执行出错:" + e.toString());
		}finally{
			CustomerContextHolder.clearCustomerType();
		}
		
		if((null==list11)||list11.size()<=0)return;
		
        for(Map<String, Object> map11 : list11) {
        	
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            int ID_Patient = null==map11.get("ID_Patient")?-1:Integer.parseInt(map11.get("ID_Patient").toString());
            String PatientCode = null==map11.get("PatientCode")?"":map11.get("PatientCode").toString();
            String PatientName = null==map11.get("PatientName")?"":map11.get("PatientName").toString();            
            String Org_Name = null==map11.get("Org_Name")?"":map11.get("Org_Name").toString();
            String Org_Depart = null==map11.get("Org_Depart")?"":map11.get("Org_Depart").toString();
            String Sex = null==map11.get("Sex")?"":map11.get("Sex").toString();
            int Age = null==map11.get("Age")?-1:Integer.parseInt(map11.get("Age").toString());
            String AgeUnit = null==map11.get("AgeUnit")?"":map11.get("AgeUnit").toString();
            String Marriage = null==map11.get("Marriage")?"":map11.get("Marriage").toString();
            String DateRegister = null==map11.get("DateRegister")?"1901-01-01":dateFormat.format(map11.get("DateRegister"));//送检日期
            String DoctorReg = null==map11.get("DoctorReg")?"":map11.get("DoctorReg").toString();
            int ID_PatientFeeItem = null==map11.get("ID_PatientFeeItem")?-1:Integer.parseInt(map11.get("ID_PatientFeeItem").toString());
            String ExamFeeItem_Code = null==map11.get("ExamFeeItem_Code")?"":map11.get("ExamFeeItem_Code").toString();
            String LabType_Code = null==map11.get("LabType_Code")?"":map11.get("LabType_Code").toString();
            String LabSampleTime = null==map11.get("LabSampleTime")?"1901-01-01":dateFormat.format(map11.get("LabSampleTime"));//采样时间

            //判断该申请单ID的病人申请单是否存在start
            String strQuery22 = " select cch.unid from chk_con_his cch where cch.His_Unid='" + ID_Patient + 
            		            //表示未被LIS取过的申请单
            		            "' and (select count(*) from chk_valu_his cvh2 where cvh2.pkunid=cch.unid and isnull(cvh2.itemvalue,'')='1')<=0 " ;
            
            List<Map<String, Object>> list22;
            try{
        		
	            list22 = jdbcTemplate.queryForList(strQuery22);
        	}catch(Exception e){

                logger.error("在LIS主表中查询PEIS申请单ID"+ID_Patient+"时失败:"+e.toString());
                continue;
        	}
            
            String Insert_Identity = null;
            for(Map<String, Object> map22 : list22) {
            	Insert_Identity = null==map22.get("unid")?"":map22.get("unid").toString();
            }
            
            if((null==Insert_Identity)||"".equals(Insert_Identity)) {
        	    
            	String strQuery33 = " select cvh.pkunid from chk_valu_his cvh where cvh.Surem1='"+ID_Patient+
      	                           //表示未被LIS取过的申请单
      	                           "' and (select count(*) from chk_valu_his cvh2 where cvh2.pkunid=cvh.pkunid and isnull(cvh2.itemvalue,'')='1')<=0 ";
            	
                List<Map<String, Object>> list33;
                try{
            		
    	            list33 = jdbcTemplate.queryForList(strQuery33);
            	}catch(Exception e){

                    logger.error("在LIS从表中查询PEIS申请单ID"+ID_Patient+"时失败:"+e.toString());
                    continue;
            	}
                
                for(Map<String, Object> map33 : list33) {
                	Insert_Identity = null==map33.get("pkunid")?"":map33.get("pkunid").toString();
                }
      	    
            }
            //判断该申请单ID的病人申请单是否存在stop

            if((null==Insert_Identity)||"".equals(Insert_Identity)) {
            	
      	        //根据申请科室查询申请单工作组start
            	//Org_Name->scombin_id
      	        //根据申请科室查询申请单工作组stop

      	        //不插入PeIS的样本类型，由拆分存储过程去处理（拿组合项目的默认样本类型）
            	String strQuery44 = "insert into chk_con_his (patientname,sex,age,report_date,bedno,His_Unid,check_doctor,deptname,combin_id,Caseno,diagnose,Diagnosetype,typeflagcase,WorkCompany,WorkDepartment,ifMarry) values "+
      	                            " ('"+PatientName+"','"+Sex+"','"+Age+AgeUnit+"','"+DateRegister+"','"+""+"','"+ID_Patient+"','"+DoctorReg+"','"+"体检科"+"','"+""+"','"+PatientCode+"','"+""+"','常规','正常','"+Org_Name+"','"+Org_Depart+"','"+Marriage+"') "+
            			            " SELECT SCOPE_IDENTITY() AS Insert_Identity ";                
                try{
                	
                    Insert_Identity = jdbcTemplate.queryForObject(strQuery44,String.class);

                }catch(Exception e){
                        
                	logger.error("向LIS中插入PEIS基本信息"+ID_Patient+"时失败:"+e.toString());
                	continue;
                }                
            }
            
            if((null==Insert_Identity)||"".equals(Insert_Identity)) {
            	
            	logger.error("LIS中查询不到刚插入的PEIS基本信息"+ID_Patient);
            	continue;
            }
            	
            //获取申请单元ID的LIS对照start
            String strQuery66 = "select c.id from HisCombItem hci,combinitem c "+
                                " where hci.CombUnid=c.unid and hci.ExtSystemId='PEIS' and hci.HisItem='"+ExamFeeItem_Code+"' ";
            List<Map<String, Object>> list66;
            try{
        		
	            list66 = jdbcTemplate.queryForList(strQuery66);
        	}catch(Exception e){

                logger.error("在LIS中取PEIS申请单元ID"+ExamFeeItem_Code+"的对照关系时失败:"+e.toString());
                continue;
        	}

            if((null==list66)||(list66.size()<=0)) {
            	
                logger.error("PEIS申请单元ID"+ExamFeeItem_Code+"在LIS中无对照关系");
                continue;
            }
            
            for(Map<String, Object> map66 : list66) {
            
            	String sID = null==map66.get("id")?"":map66.get("id").toString();
            	
                String strQuery44 = "select count(*) as RecNum from "+
                                    " chk_valu_his cvh where cvh.Surem1='"+ID_Patient+
                                    "' and cvh.pkcombin_id='"+sID+"' ";
                int RecNum;
                try {                	
                    RecNum = jdbcTemplate.queryForObject(strQuery44,Integer.class);
            	}catch(Exception e){

                    logger.error("判断PEIS申请单"+ID_Patient+",申请单元ID"+ExamFeeItem_Code+"("+sID+")在LIS是否存在时失败:"+e.toString());
                    continue;
            	}

                if(RecNum<=0) {
                	
                    String strQuery222 = "insert into chk_valu_his (pkunid,pkcombin_id,Surem1,Surem2,Urine1,Urine2,TakeSampleTime) values ('"+Insert_Identity+"','"+sID+"','"+ID_Patient+"','"+ExamFeeItem_Code+"','"+PatientCode+"','"+LabType_Code+"','"+LabSampleTime+"') ";                    
                    try{
                    	
                        jdbcTemplate.update(strQuery222);

                    }catch(Exception e){
                            
                    	logger.error("向LIS中插入PEIS申请单元ID"+ExamFeeItem_Code+"("+sID+")时失败:"+e.toString());
                    	continue;
                    }

                    //回写“已读取”标志。该标志对PEIS无任何控制作用，只是让PEIS知道该条记录已被读走
                    //切换数据源的变量准备工作start
            		String selfClassName444 = this.getClass().getName();		
            		int jdbcUnid444 = CustomerContextHolder.getJdbcUnidFromJobClass(selfClassName444);				
            		CommCodeEntity commCodeEntity444 = CustomerContextHolder.getConnectionInfo(jdbcUnid444);
            		Map<String,Object> customerTypeMap444 = new HashMap<String,Object>();
            		if(commCodeEntity444!=null){
            			customerTypeMap444.put("driverClass", commCodeEntity444.getReserve());
            			customerTypeMap444.put("url", commCodeEntity444.getReserve2());
            			customerTypeMap444.put("user", commCodeEntity444.getReserve3());
            			customerTypeMap444.put("password", commCodeEntity444.getReserve4());
            		}
                    //切换数据源的变量准备工作stop

                    String strQuery444 = "UPDATE IntPatientFeeItem SET F_Back_Transfered=1 WHERE ID_PatientFeeItem="+ID_PatientFeeItem;
            		try {
            			
	            		CustomerContextHolder.setCustomerType(customerTypeMap444);
	            		
	                    jdbcTemplate.update(strQuery444);                    
	        		}catch(Exception e){
	        			logger.error("切换数据源，执行出错:" + e.toString());
	        		}finally{
	        			CustomerContextHolder.clearCustomerType();
	        		}
                }
            }
            //获取申请单元ID的LIS对照stop
        }
		
        //拆分申请单        		
        try{
        	
            jdbcTemplate.update("dbo.pro_SplitRequestBill");

        }catch(Exception e){
                
        	logger.error("拆分PEIS申请单元时失败:"+e.toString());
        }

        //合并申请单
        try{
        	
            jdbcTemplate.update("dbo.pro_MergeRequestBill");

        }catch(Exception e){
                
        	logger.error("合并PEIS申请单元时失败:"+e.toString());
        }
        
        /**
                         * 越秀中医:回写条码任务
                         * 越秀中医扫描体检条码慢
                         * 将chk_valu_his中的条码号回写到chk_con_his
                         * 提高扫描速度
         *
         */
        //写条码到chk_con_his.TjJianYan begin
        //以前是专用服务JobYXZYWriteBarcode写条码,可能导致拆分、合并之前写入,导致扫码出现问题        
        List<Map<String, Object>> list = null;
        try{
            list = jdbcTemplate.queryForList(" select Unid,TjJianYan from chk_con_his cch WITH(NOLOCK) where cch.check_date+10>getdate() ");
        }catch(Exception e){            
            logger.error("jdbcTemplate.queryForList失败:"+e.toString());
        }
        
        for(Map<String, Object> map1 : list) {

            StringBuilder sb1 = new StringBuilder();
            sb1.append(" select dbo.uf_Peis_Br_Barcode(");
            sb1.append(map1.get("Unid"));
            sb1.append(") ");
            
            String newBarcode = null;
            try{
                newBarcode = jdbcTemplate.queryForObject(sb1.toString(),String.class);
            }catch(Exception e){            
                logger.error("jdbcTemplate.queryForObject失败:"+e.toString());
            }
            
            if(null!=newBarcode&&!"".equals(newBarcode)){
                newBarcode = ","+newBarcode+",";
            }
            if(null==newBarcode){
                newBarcode = "";
            }
            
            String oldBarcode = "";
            if(null!=map1.get("TjJianYan")){
                oldBarcode = map1.get("TjJianYan").toString();
            }
                
            if(!newBarcode.equals(oldBarcode)){
                
                StringBuilder sb11 = new StringBuilder();
                sb11.append(" update chk_con_his set TjJianYan='");
                sb11.append(newBarcode);
                sb11.append("' where Unid=");
                sb11.append(map1.get("Unid"));
                
                try{
                    jdbcTemplate.update(sb11.toString());
                }catch(Exception e){            
                    logger.error("jdbcTemplate.update失败:"+e.toString());
                }
            }                                                   
        }
        //写条码到chk_con_his.TjJianYan end
	}
}
