package com.yklis.schedule.service;

import com.yklis.schedule.entity.CommCodeEntity;
import com.yklis.schedule.entity.TaskOperateTypeEntity;

/**
 * 观察者模式
 * 观察者interface
 * 观察者模式为1对多的关系:一个主题对应多个观察者
 * 通知时,所有观察者都会收到通知
 * @author liuying
 *
 */
public interface TaskObserverService {

	/**
	 * 
	 * @param strJobKey:用于删除JOB
	 * @param commCodeEntity:用于增加、更新JOB
	 */
	void update(TaskOperateTypeEntity operateType,String strJobKey,CommCodeEntity commCodeEntity);

}
