package com.yklis.schedule.dao;

import java.util.List;
import java.util.Map;

import com.yklis.schedule.entity.CrJiFeiEntity;

public interface JobUploadFeeDao {
	
	List<Map<String, Object>> selectWaitUploadFee();
	
	int selectBJiFeiRecNum(String xingCheDanHao);
	
	String selectXiuGaiBiaoShi(String xingCheDanHao);
	
	void updateBJiFei(CrJiFeiEntity crJiFeiEntity);
	
	void insertBJiFei(CrJiFeiEntity crJiFeiEntity);
	
	void updateFeeInfo(String SC_COMPANY, String SC_YDH);

}
