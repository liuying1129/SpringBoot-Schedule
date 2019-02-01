package com.yklis.schedule.dao;

import java.util.List;

import com.yklis.schedule.entity.CommCodeEntity;

public interface CommCodeDao {
	
	List<CommCodeEntity> selectCommCode(CommCodeEntity commCodeEntity);

}
