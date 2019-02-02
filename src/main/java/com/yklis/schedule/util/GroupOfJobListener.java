package com.yklis.schedule.util;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yklis.schedule.entity.CommCodeEntity;

/**
 * Job监听器
 * @author ying07.liu
 *
 */
public class GroupOfJobListener implements JobListener {

    //配置容器起动时候加载log4j配置文件
    //只要将log4j.properties放在classes下，tomcat启动的时候会自动加载log4j的配置信息，
    //在程式代码不再需要使用PropertyConfigurator.configure("log4j.properties")来加载，
    //如果用了它反而会出现上面的错误--Could not read configuration file [log4jj.properties]
    //PropertyConfigurator.configure("log4jj.properties");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //获取监听器名称
    @Override
    public String getName() {
        
        return getClass().getSimpleName();
    }

    //Scheduler在JobDetail将要被执行时调用这个方法
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        
        //String jobName = context.getJobDetail().getKey().getName();
        CommCodeEntity commCodeEntity = (CommCodeEntity)context.getMergedJobDataMap().get(Constants.JOB_DATA_MAP_KEY);
        logger.info("JOB " + commCodeEntity.getName() + " is going to be executed");//这两句一样的效果
    }

    //Scheduler在JobDetail即将被执行，但又被TriggerListerner否决时会调用该方法
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        
        //String jobName = context.getJobDetail().getKey().getName();
        CommCodeEntity commCodeEntity = (CommCodeEntity)context.getMergedJobDataMap().get(Constants.JOB_DATA_MAP_KEY);
        logger.info("JOB " + commCodeEntity.getName() + " was vetoed and not executed");

    }

    //Scheduler在JobDetail被执行之后调用这个方法
    @Override
    public void jobWasExecuted(JobExecutionContext context,
            JobExecutionException jobException) {
        
        //String jobName = context.getJobDetail().getKey().getName();
        CommCodeEntity commCodeEntity = (CommCodeEntity)context.getMergedJobDataMap().get(Constants.JOB_DATA_MAP_KEY);
        logger.info("JOB " + commCodeEntity.getName() + " was executed");
    }
}
