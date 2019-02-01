package com.yklis.schedule.business.job;

import java.util.Map;

/**
 * 命令模式
 * 命令接口
 * @author liuying
 *
 */
public interface Command {
	
	void execute(Map<String,Object> map);

}
