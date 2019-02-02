package com.yklis.schedule.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yklis.schedule.dao.JobDownloadBillDao;
import com.yklis.schedule.entity.CrBdingDanEntity;
import com.yklis.schedule.entity.CrDingDanHuoPinEntity;
import com.yklis.schedule.entity.WaitSchedEntity;
import com.yklis.schedule.service.JobDownloadBillService;

@Service
public class JobDownloadBillServiceImpl implements JobDownloadBillService {
	
	// 配置容器起动时候加载log4j配置文件
	// 只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
	// 在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
	// 如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
	// PropertyConfigurator.configure("log4jj.properties");
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private JobDownloadBillDao jobDownloadBillDao;
		
	public void downloadBill(){
		
		try{
			
			List<CrBdingDanEntity> crBdingDanEntityList = jobDownloadBillDao.selectBill();
			
			if(crBdingDanEntityList==null)return;
			if(crBdingDanEntityList.size()<=0)return;		
			
			for(CrBdingDanEntity crBdingDanEntity : crBdingDanEntityList){
				
				List<CrDingDanHuoPinEntity> crDingDanHuoPinEntityList = crBdingDanEntity.getCrDingDanHuoPinList();
	
				if(crDingDanHuoPinEntityList==null)continue;
				if(crDingDanHuoPinEntityList.size()<=0)continue;		
	
				for(CrDingDanHuoPinEntity crDingDanHuoPinEntity : crDingDanHuoPinEntityList){
					
					WaitSchedEntity waitSchedEntity = new WaitSchedEntity();
					waitSchedEntity.setScCompany("华润");
					waitSchedEntity.setScYdh(crBdingDanEntity.getXingCheDanHao());
					waitSchedEntity.setScCustomer(crBdingDanEntity.getCrShouHuoKeHu().getKeHuMingCheng());
					waitSchedEntity.setScSendAddr(crBdingDanEntity.getCrShouHuoDiZhi().getKeHuDiZhi());
					waitSchedEntity.setScBillNo(crBdingDanEntity.getDingDanBianHao());
					waitSchedEntity.setScRequestSendTime(crBdingDanEntity.getFaHuoRiQi());
					waitSchedEntity.setPickDatetime(crBdingDanEntity.getFaHuoRiQi());
					waitSchedEntity.setRteNbr(crBdingDanEntity.getXianLuHao());
					waitSchedEntity.setUserName("Auto");
					float fJianShu;
					try {
						fJianShu = Float.parseFloat(crBdingDanEntity.getJianShu());
					} catch (Exception e) {
						logger.error("字段JianShu的值为非数值:" + e.toString());
						continue;
					}
					int iJianShu = (int) Math.ceil(fJianShu);
					waitSchedEntity.setSC_BillNo_Js(iJianShu);
					waitSchedEntity.setReserve(crBdingDanEntity.getErpDingDanHao());
					waitSchedEntity.setReserve2(crBdingDanEntity.getCarNo());
					waitSchedEntity.setReserve3(crBdingDanEntity.getShangPinLeiXing());
					
					waitSchedEntity.setScSizeDesc(crDingDanHuoPinEntity.getHuoPinBianHao());
					waitSchedEntity.setScSkuDesc(crDingDanHuoPinEntity.getCrHuoPinXinXi().getHuoPinMingCheng());
					waitSchedEntity.setScOrigPktQty(crDingDanHuoPinEntity.getHuoPinShuLiang());
					waitSchedEntity.setScUnits(crDingDanHuoPinEntity.getHuoPinDanWei());
					waitSchedEntity.setScBatchNbr(crDingDanHuoPinEntity.getHuoPinPiCi());
					waitSchedEntity.setKcfsl(crDingDanHuoPinEntity.getHuoPinShuLiang());
					
					jobDownloadBillDao.insertWaitSched(waitSchedEntity);
					int i = waitSchedEntity.getUnid();
					if (i>0) {
						jobDownloadBillDao.updateBillStatus(crBdingDanEntity.getDingDanBianHao());
					}				
				}
							
			}
		
		}catch(Exception e){
			
			logger.error("执行downloadBill出错:" + e.toString());
			
		}
	
	}	
}
