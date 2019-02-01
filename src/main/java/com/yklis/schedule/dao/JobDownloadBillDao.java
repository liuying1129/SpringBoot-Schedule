package com.yklis.schedule.dao;

import java.util.List;

import com.yklis.schedule.entity.CrBdingDanEntity;
import com.yklis.schedule.entity.WaitSchedEntity;

public interface JobDownloadBillDao {

	/**
	 * 查询待下载的单据
	 * @return
	 */
	List<CrBdingDanEntity> selectBill();
	
	void insertWaitSched(WaitSchedEntity waitSchedEntity);
	
	void updateBillStatus(String dingDanBianHao);
	
}
