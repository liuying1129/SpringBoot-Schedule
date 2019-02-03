package com.yklis.schedule.business.job;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import com.yklis.schedule.util.Constants;
import com.yklis.schedule.util.SpringUtils;

/**
 * 命令模式
 * 命令实现类
 * 
 * 生成体检结论、建议
 * 前后台进程分开,变相的实现多线程
 * 前端生成标识 chk_con.Weight “待生成”
 * 后端JOB类根据标识生成，并修改标识chk_con.Weight “已生成”
 * 前端定时器定时刷新“体检结论、建议生成中...”警告灯
 * 已生成列表进行颜色标识，表示可以进行后续工作了
 * @author ying07.liu
 *
 */
public class JobMakeTjDescription implements Command {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private int makeTjDescDays;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private JdbcTemplate jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
    
    //JAVA规定，如果类中没有定义任何构造函数，JVM自动为其生成一个默认的构造函数
    //故可不需要手动写下面的构造函数
    //public JobSPH2CJ(){       
    //}
    
    @Override
    public void execute(Map<String,Object> map) {
        
        try{
        	makeTjDescDays = Integer.parseInt(jdbcTemplate.queryForObject("select Name from CommCode where TypeName='系统代码' and ReMark='生成体检结论的偏差天数'", String.class));
        }catch(Exception e){            
            makeTjDescDays = 0;
            logger.error("获取系统代码【生成体检结论、建议的偏差天数】失败:"+e.toString());
        }  
        
        String bAppendMakeTjDesc = null;
        try{
        	bAppendMakeTjDesc = jdbcTemplate.queryForObject("select Name from CommCode where TypeName='系统代码' and ReMark='允许追加生成体检结论'", String.class);
        }catch(Exception e){
            logger.error("获取系统代码【允许追加生成体检结论】失败:"+e.toString());
        }

        List<Map<String, Object>> list = null;
        try{
            list = jdbcTemplate.queryForList(" select unid from chk_con ch where ch.Weight='待生成' ");
        }catch(Exception e){            
            logger.error("获取待生成列表失败:"+e.toString());
        }
        
        for(Map<String, Object> map1 : list) {
        	
            //市政医院【允许追加生成体检结论】值为【是】.故下面这段代码未经实践
            if(!"是".equals(bAppendMakeTjDesc)){
            
            	int RecNum = 0;
                try{
                	RecNum = Integer.parseInt(jdbcTemplate.queryForObject("select count(*) from chk_valu cv,clinicchkitem cci where cv.itemid=cci.itemid and cv.pkunid="+map1.get("unid")+" and cci.Reserve5 in (1,2) and isnull(cv.itemvalue,'')<>'' ", String.class));
                }catch(Exception e){            
                    logger.error("获取病人非空结论、建议记录行数失败【pkunid:"+map1.get("unid")+"】:"+e.toString());
                }  

                if(RecNum>0) {
            	  
	                try{
	                	jdbcTemplate.update("update chk_valu set issure=1 where pkunid="+map1.get("unid")+" and isnull(issure,'')<>'1' and (select cci.Reserve5 from clinicchkitem cci where cci.itemid=chk_valu.itemid) in (1,2)");
	                	jdbcTemplate.update("update chk_con set Weight='已生成' where unid="+map1.get("unid")+" and Weight='待生成' ");
	                }catch(Exception e){
	                    logger.error("更新病人的有效性标志失败【pkunid:"+map1.get("unid")+"】:"+e.toString());
	                }
	
	                continue;
                }
            }
            //============================================

        	singleMakeTjDescription(Integer.parseInt(map1.get("unid").toString()));
        }
    }
    
    private void singleMakeTjDescription(final int checkUnid) {
    
    	List<Map<String, Object>> list = null;
    	try{
	      list = jdbcTemplate.queryForList(" select * from chk_con cc where cc.unid="+checkUnid);
    	}catch(Exception e){            
	      logger.error("获取病人基本信息失败【Unid:"+checkUnid+"】:"+e.toString());
    	}
  
    	if((null==list)||(list.size()<=0)){
    	    
    	    jdbcTemplate.update("update chk_con set Weight='获取病人基本信息失败' where unid="+checkUnid+" and Weight='待生成' ");
    	    return;
    	}
        
    	if(null==list.get(0).get("check_date")) {
  
    		jdbcTemplate.update("update chk_con set Weight='检查日期为空' where unid="+checkUnid+" and Weight='待生成' ");
    		return;//检查日期为空则不更新
    	}
  
    	String patientname = null==list.get(0).get("patientname")?"":list.get(0).get("patientname").toString();
    	String sex = null==list.get(0).get("sex")?"":list.get(0).get("sex").toString();
    	String age = null==list.get(0).get("age")?"":list.get(0).get("age").toString();
    	String check_date = dateFormat.format(list.get(0).get("check_date"));
  
    	if("".equals(patientname.trim())) {
  
    		jdbcTemplate.update("update chk_con set Weight='姓名为空' where unid="+checkUnid+" and Weight='待生成' ");
    		return;//无姓名则不更新
    	}
  
    	StringBuilder sb1 = new StringBuilder();
    	sb1.append(" select cv.*,cci.Reserve1 as TJAdvice_L,cci.Reserve2 as TJAdvice_H ");
    	sb1.append(" from chk_con cc,chk_valu cv,clinicchkitem cci ");
    	sb1.append(" where cc.unid=cv.pkunid and cc.patientname='");
    	sb1.append(patientname);
    	sb1.append("' and isnull(cc.sex,'')='");
    	sb1.append(sex);
    	sb1.append("' and dbo.uf_GetAgeReal(cc.age)=dbo.uf_GetAgeReal('");
    	sb1.append(age);
    	sb1.append("') and cv.issure=1 and ltrim(rtrim(isnull(itemvalue,'')))<>'' ");
    	sb1.append(" and cci.itemid=cv.itemid ");
    	sb1.append(" and (isnull(cci.Reserve1,'')<>'' or isnull(cci.Reserve2,'')<>'') ");
    	sb1.append(" union all ");
    	sb1.append(" select cv.*, ");
    	sb1.append(" (select cci.Reserve1 from clinicchkitem cci where cci.itemid=cv.itemid) as TJAdvice_L,");
    	sb1.append(" (select cci.Reserve2 from clinicchkitem cci where cci.itemid=cv.itemid) as TJAdvice_H ");
    	sb1.append(" from chk_con_bak cc,chk_valu_bak cv,clinicchkitem cci ");
    	sb1.append(" where cc.unid=cv.pkunid and cc.patientname='");
    	sb1.append(patientname);
    	sb1.append("' and isnull(cc.sex,'')='");
    	sb1.append(sex);
    	sb1.append("' and dbo.uf_GetAgeReal(cc.age)=dbo.uf_GetAgeReal('");
    	sb1.append(age);
    	sb1.append("') and cv.issure=1 and ltrim(rtrim(isnull(itemvalue,'')))<>'' ");
    	sb1.append(" and cci.itemid=cv.itemid ");
    	sb1.append(" and (isnull(cci.Reserve1,'')<>'' or isnull(cci.Reserve2,'')<>'') ");
    	sb1.append(" and CONVERT(CHAR(10),cc.check_date,121)>=DATEADD(day,");
    	sb1.append(-1*makeTjDescDays);
    	sb1.append(",'");
    	sb1.append(check_date);
    	sb1.append("')");
    	sb1.append(" and CONVERT(CHAR(10),cc.check_date,121)<=DATEADD(day,");
    	sb1.append(makeTjDescDays);
    	sb1.append(",'");
    	sb1.append(check_date);
    	sb1.append("')");
    	sb1.append(" order by cv.pkunid,cv.pkcombin_id,cv.printorder ");      

    	List<Map<String, Object>> list1 = null;
    	try{
    		list1 = jdbcTemplate.queryForList(sb1.toString());
    	}catch(Exception e){         
    		
    		jdbcTemplate.update("update chk_con set Weight='获取病人检验结果失败' where unid="+checkUnid+" and Weight='待生成' ");
    		logger.error("获取病人检验结果失败【姓名:"+patientname+"】:"+e.toString());
    		return;
    	}

    	String pre_Combin_Name;
    	String combin_Name="";
    	int j = 0;
  
    	for(Map<String, Object> map1 : list1) {
      
    		StringBuilder sb11 = new StringBuilder();
    		sb11.append("select cv.valueid from chk_valu cv,clinicchkitem cci where cv.pkunid=");
    		sb11.append(checkUnid);
    		sb11.append(" and cci.itemid=cv.itemid and cci.Reserve5=1 and cci.SysName='");
    		sb11.append(Constants.SYSNAME);
    		sb11.append("'");
    		String sTjjl_Unid = null;
    		try{      
    			sTjjl_Unid = jdbcTemplate.queryForObject(sb11.toString(),String.class);
    		}catch(EmptyResultDataAccessException e){
    		}catch(IncorrectResultSizeDataAccessException e){
    			jdbcTemplate.update("update chk_con set Weight='获取到多条病人体检结论记录' where unid="+checkUnid+" and Weight='待生成' ");
    			logger.error("获取到多条病人体检结论记录【PkUnid:"+checkUnid+"】:"+e.toString());
    			return;
    		}catch(Exception e){
    			jdbcTemplate.update("update chk_con set Weight='获取病人的体检结论记录失败' where unid="+checkUnid+" and Weight='待生成' ");
    			logger.error("获取病人的体检结论记录失败【PkUnid:"+checkUnid+"】:"+e.toString());
    			return;
    		}          

    		StringBuilder sb22 = new StringBuilder();
    		sb22.append("select cv.valueid from chk_valu cv,clinicchkitem cci where cv.pkunid=");
    		sb22.append(checkUnid);
    		sb22.append(" and cci.itemid=cv.itemid and cci.Reserve5=2 and cci.SysName='");
    		sb22.append(Constants.SYSNAME);
    		sb22.append("'");
    		String sTjjy_Unid = null;
    		try{      
    			sTjjy_Unid = jdbcTemplate.queryForObject(sb22.toString(),String.class);
    		}catch(EmptyResultDataAccessException e){
    		}catch(IncorrectResultSizeDataAccessException e){
    			jdbcTemplate.update("update chk_con set Weight='获取到多条病人体检建议记录' where unid="+checkUnid+" and Weight='待生成' ");
    			logger.error("获取到多条病人体检建议记录【PkUnid:"+checkUnid+"】:"+e.toString());
    			return;
    		}catch(Exception e){
    			jdbcTemplate.update("update chk_con set Weight='获取病人的体检建议记录失败' where unid="+checkUnid+" and Weight='待生成' ");
    			logger.error("获取病人的体检建议记录失败【PkUnid:"+checkUnid+"】:"+e.toString());
    			return;
    		}
  
    		String itemid=null==map1.get("itemid")?"":map1.get("itemid").toString().trim();
    		String itemChnName=null==map1.get("Name")?"":map1.get("Name").toString().trim();
    		String itemvalue=null==map1.get("itemvalue")?"":map1.get("itemvalue").toString().trim();
    		String min_value=null==map1.get("Min_value")?"":map1.get("Min_value").toString().trim();
    		String max_value=null==map1.get("Max_value")?"":map1.get("Max_value").toString().trim();
    		String tjAdvice_L=null==map1.get("TJAdvice_L")?"":map1.get("TJAdvice_L").toString().trim();
    		String tjAdvice_H=null==map1.get("TJAdvice_H")?"":map1.get("TJAdvice_H").toString().trim();        

			StringBuilder sb33 = new StringBuilder();
			sb33.append("select dbo.uf_ValueAlarm('");
			sb33.append(itemid);
			sb33.append("','");
			sb33.append(min_value);
			sb33.append("','");
			sb33.append(max_value);
			sb33.append("','");
			sb33.append(itemvalue);
			sb33.append("') as ifValueAlarm");
			
			int i=0;
			try{//uf_ValueAlarm中的convert函数可能抛出异常
			    i = jdbcTemplate.queryForObject(sb33.toString(),int.class);
			}catch(Exception e){            
			    logger.error("执行自定义函数uf_ValueAlarm失败:"+e.toString());
			}        
			
			if((i!=1)&&(i!=2)) continue;//该项目无异常
			j=j+1;
			
			//写结论
			String LvlInd;
			switch(i){
			case 1:
				LvlInd="偏低;";
				break;
			case 2:
				LvlInd="偏高;";
				break;
			default:
				LvlInd="";
				break;
			}
			
			pre_Combin_Name=combin_Name;//记录上一个组合项目名称
			combin_Name = null==map1.get("combin_Name")?"":map1.get("combin_Name").toString().trim();
			  
			String combin_Name_Flag="";
			//如不加(char)0x0A,打印时内容会换行,但LIS界面上不会换行
			//(char)0x0D、(char)0x0A之间如不加"",则会变成字符串23
			if (!combin_Name.equals(pre_Combin_Name)) combin_Name_Flag=(char)0x0D+""+(char)0x0A+combin_Name+":";
			  
			if((!"".equals(sTjjl_Unid))&&(null!=sTjjl_Unid)){

				String Ex_TjDescription = null;
				try{
					Ex_TjDescription = jdbcTemplate.queryForObject("select itemvalue from chk_valu where valueid="+sTjjl_Unid,String.class);
				}catch(Exception e){
				}
				if((null==Ex_TjDescription)||("".equals(Ex_TjDescription))) {
				    
                    if((combin_Name_Flag.length()>=3)&&(combin_Name_Flag.charAt(0)==(char)0x0D)) combin_Name_Flag=combin_Name_Flag.substring(2);
				}
	    
				try{
                    jdbcTemplate.update("update chk_valu set issure=1,itemvalue=isnull(itemvalue,'')+'"+combin_Name_Flag+itemChnName+LvlInd+"' where valueid="+sTjjl_Unid);
				}catch(Exception e){
                    jdbcTemplate.update("update chk_con set Weight='更新病人的体检结论失败' where unid="+checkUnid+" and Weight='待生成' ");
					logger.error("更新病人的体检结论失败【valueid:"+sTjjl_Unid+"】:"+e.toString());
					return;
				}

			} else {

		        List<Map<String, Object>> list2 = null;
		        try{
		          list2 = jdbcTemplate.queryForList("select cci.itemid,cbi.id from clinicchkitem cci,CombSChkItem csi,combinitem cbi where cci.Reserve5=1 and cci.unid=csi.itemunid and csi.combunid=cbi.unid and cci.SysName='"+Constants.SYSNAME+"' and cbi.SysName='"+Constants.SYSNAME+"'");
		        }catch(Exception e){            
		          logger.error("获取【体检结论】项目代码失败:"+e.toString());
		        }
		        
		        String sitemid = null;
		        String spkcombin_id = null;
		        for(Map<String, Object> map2 : list2) {		            
	                sitemid=null==map2.get("itemid")?"":map2.get("itemid").toString();
                    spkcombin_id=null==map2.get("id")?"":map2.get("id").toString();
		        }
				if((!"".equals(sitemid)) && (!"".equals(spkcombin_id))&&(null!=sitemid) && (null!=spkcombin_id)){
				    
				    if((combin_Name_Flag.length()>=3)&&(combin_Name_Flag.charAt(0)==(char)0x0D)) combin_Name_Flag=combin_Name_Flag.substring(2);
					try{
						jdbcTemplate.update("insert into chk_valu(pkunid,issure,pkcombin_id,itemid,itemvalue) values (" + checkUnid+",1,'" +spkcombin_id+"','"+sitemid+"','"+combin_Name_Flag+itemChnName+LvlInd+"')");
					}catch(Exception e){
	                    jdbcTemplate.update("update chk_con set Weight='插入病人的体检结论失败' where unid="+checkUnid+" and Weight='待生成' ");
						logger.error("插入病人的体检结论失败【pkunid:"+checkUnid+"】:"+e.toString());
						return;
					}
                }else{
                    logger.info("获取【体检结论】项目代码为空,请正确设置项目");
                }
			}
  
			//写建议
			switch(i){
			case 1:
				LvlInd=tjAdvice_L;
				break;
			case 2:
				LvlInd=tjAdvice_H;
				break;
			default:
				LvlInd="";
				break;
			}
			
			if(!"".equals(LvlInd)) {
			
				if((!"".equals(sTjjy_Unid))&&(null!=sTjjy_Unid)){
			  
					String Ex_TJAdvice = null;
			        try{
			        	Ex_TJAdvice = jdbcTemplate.queryForObject("select itemvalue from chk_valu where valueid="+sTjjy_Unid,String.class);
			        }catch(Exception e){
			        }
			        
                    String sCR=(char)0x0D+""+(char)0x0A+""+(char)0x0D+""+(char)0x0A;
		            if((null==Ex_TJAdvice)||("".equals(Ex_TJAdvice))) sCR="";
			  			
			        try{
			        	jdbcTemplate.update("update chk_valu set issure=1,itemvalue=isnull(itemvalue,'')+'"+sCR+LvlInd+"' where valueid="+sTjjy_Unid);
			        }catch(Exception e){
                        jdbcTemplate.update("update chk_con set Weight='更新病人的体检建议失败' where unid="+checkUnid+" and Weight='待生成' ");
			        	logger.error("更新病人的体检建议失败【valueid:"+sTjjy_Unid+"】:"+e.toString());
			        	return;
			        }
				} else {
			  
	                List<Map<String, Object>> list2 = null;
	                try{
	                  list2 = jdbcTemplate.queryForList("select cci.itemid,cbi.id from clinicchkitem cci,CombSChkItem csi,combinitem cbi where cci.Reserve5=2 and cci.unid=csi.itemunid and csi.combunid=cbi.unid and cci.SysName='"+Constants.SYSNAME+"' and cbi.SysName='"+Constants.SYSNAME+"'");
	                }catch(Exception e){            
	                  logger.error("获取【体检建议】项目代码失败:"+e.toString());
	                }
	                
	                String sitemid = null;
	                String spkcombin_id = null;
	                for(Map<String, Object> map2 : list2) {                 
	                    sitemid=null==map2.get("itemid")?"":map2.get("itemid").toString();
	                    spkcombin_id=null==map2.get("id")?"":map2.get("id").toString();
	                }
					if ((!"".equals(sitemid)) && (!"".equals(spkcombin_id))&&(null!=sitemid) && (null!=spkcombin_id)){
			
						try{
							jdbcTemplate.update("insert into chk_valu(pkunid,issure,pkcombin_id,itemid,itemvalue) values (" + checkUnid+",1,'" +spkcombin_id+"','"+sitemid+"','"+LvlInd+"')");
						}catch(Exception e){
	                        jdbcTemplate.update("update chk_con set Weight='插入病人的体检建议失败' where unid="+checkUnid+" and Weight='待生成' ");
							logger.error("插入病人的体检建议失败【pkunid:"+checkUnid+"】:"+e.toString());
							return;
			            }
					}else{
                        logger.info("获取【体检建议】项目代码为空,请正确设置项目");
					}
				}
			}
    	}
    	
    	if(j==0){
    	    
            jdbcTemplate.update("update chk_con set Weight='无异常项目' where unid="+checkUnid+" and Weight='待生成' ");
    	}else{
    	    
            jdbcTemplate.update("update chk_con set Weight='已生成' where unid="+checkUnid+" and Weight='待生成' ");
    	}
    }
}
