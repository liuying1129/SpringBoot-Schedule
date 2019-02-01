package com.yklis.schedule.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yklis.schedule.dao.JobUploadFeeDao;
import com.yklis.schedule.entity.CrJiFeiEntity;
import com.yklis.schedule.service.JobUploadFeeService;

@Service
public class JobUploadFeeServiceImpl implements JobUploadFeeService {
	
    //配置容器起动时候加载log4j配置文件
    //只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
    //在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
    //如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
    //PropertyConfigurator.configure("log4jj.properties");
    Logger logger = Logger.getLogger(this.getClass());
    
    @Autowired
    JobUploadFeeDao jobUploadFeeDao;
	
	public void uploadFee(){
		
		try{
			
			List<Map<String,Object>> mapList = jobUploadFeeDao.selectWaitUploadFee();
			
			if(mapList==null)return;
			if(mapList.size()<=0)return;
			
			for(Map<String,Object> map : mapList){
				
				CrJiFeiEntity crJiFeiEntity = new CrJiFeiEntity();
				crJiFeiEntity.setXingCheDanHao(map.get("SC_YDH").toString());
				crJiFeiEntity.setShiXiao(map.get("ShiXiao").toString());
				crJiFeiEntity.setJianShu(Integer.parseInt(map.get("YDH_JS").toString()));
				crJiFeiEntity.setZhongLiang(Float.parseFloat(map.get("YDH_WEIGHT").toString()));
				crJiFeiEntity.setTiJi(Float.parseFloat(map.get("YDH_VOLUME").toString()));
				
				int i = jobUploadFeeDao.selectBJiFeiRecNum(map.get("SC_YDH").toString());
				String xiuGaiBiaoShi = jobUploadFeeDao.selectXiuGaiBiaoShi(map.get("SC_YDH").toString());
				
				if(i>=1){
					if("E".equalsIgnoreCase(xiuGaiBiaoShi)){
						logger.info("货主:华润,行车单号:"+map.get("SC_YDH").toString()+"的计费信息已被取走,故该计费信息未上传");
					}else{
						
						jobUploadFeeDao.updateBJiFei(crJiFeiEntity);
					}
					
				}else{
					
					jobUploadFeeDao.insertBJiFei(crJiFeiEntity);
				}
				
				jobUploadFeeDao.updateFeeInfo("华润", map.get("SC_YDH").toString());
			}

		}catch(Exception e){
			
			logger.error("执行uploadFee出错:" + e.toString());
			
		}
	}

}
