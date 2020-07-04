package com.yklis.schedule.jobtype;

/**
 * 定义上下文，负责使用JobTypeStrategy角色
 * 
 * @author liuyi
 *
 */
public class JobTypeContext {
	
	private String jobType;
	private JobTypeStrategy jobTypeStrategy;
	
	public JobTypeContext(String jobType,JobTypeStrategy jobTypeStrategy) {
		
		this.jobType = jobType;
		this.jobTypeStrategy = jobTypeStrategy;
	}
	
	public JobTypeStrategy getJobTypeStrategy() {
		
		return jobTypeStrategy;
	}
	
	public boolean options(String jobType) {
		
		return this.jobType.equals(jobType);
	}
}
