package com.yklis.schedule.service.impl;

import java.util.ArrayList;
import com.yklis.schedule.entity.CommCodeEntity;
import com.yklis.schedule.entity.TaskOperateTypeEntity;
import com.yklis.schedule.service.TaskObserverService;
import com.yklis.schedule.service.TaskSubjectService;

/**
 * 观察者模式
 * 主题实现类
 * @author liuying
 *
 */
public class TaskSubjectServiceImpl implements TaskSubjectService {

	private ArrayList<TaskObserverService> taskObservers;
	private TaskOperateTypeEntity operateType;
	private String strJobKey;
	private CommCodeEntity commCodeEntity;
	
	public TaskSubjectServiceImpl(){
		
		taskObservers = new ArrayList<>();
	}
	
	@Override
	public void registerObserver(TaskObserverService taskObserverService) {
		
		taskObservers.add(taskObserverService);
	}

	@Override
	public void removeObserver(TaskObserverService taskObserverService) {
		
		int i = taskObservers.indexOf(taskObserverService);
		if(i>=0){
			taskObservers.remove(i);
		}

	}

	@Override
	public void notifyObservers() {
		
		for(int i = 0;i<taskObservers.size();i++){
			
			TaskObserverService taskObserverService = (TaskObserverService)taskObservers.get(i);
			taskObserverService.update(operateType, strJobKey, commCodeEntity);
		}

	}
	
	public void taskInfoChanged(){
		
		notifyObservers();
	}
	
	public void setTaskInfo(TaskOperateTypeEntity operateType,String strJobKey,CommCodeEntity commCodeEntity){
		
		this.operateType = operateType;
		this.strJobKey = strJobKey;
		this.commCodeEntity = commCodeEntity;
		
		taskInfoChanged();
	}

}
