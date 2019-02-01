package com.yklis.schedule.service;

/**
 * 观察者模式
 * 主题interface
 * @author liuying
 *
 */
public interface TaskSubjectService {

	void registerObserver(TaskObserverService taskObserverService);
	
	void removeObserver(TaskObserverService taskObserverService);
	
	void notifyObservers();
}
