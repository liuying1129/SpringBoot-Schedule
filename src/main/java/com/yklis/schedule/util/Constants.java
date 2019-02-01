package com.yklis.schedule.util;

/**
 * 定义跨类使用的常量
 * @author ying07.liu
 *
 */
public interface Constants {
	
    //interface的数据成员、方法默认是public;数据成员默认为static final
    //充分使用上述特点,所以使用interface代替class
    
	String JOB_DATA_MAP_KEY = "scheduleJob";
    String DEFAULT_JOB_GROUP = "GroupOfJob";
	
	String SPH_SC_COMPANY = "上药控股广东";
	//测试环境:14.23.76.194:8050
	//正式环境:14.23.76.197:8888
	//20180611上药更改接口IP地址59.37.39.206
	String SPH_BASE_URL = "http://59.37.39.206:8888/api/IOutCarrier/Carrier/";
	String SPH_AUTHOR_CODE = "1710153675";
	
	//用于生成体检结论、建议
	String SYSNAME = "PEIS";
	
    //用于LIS记录导入Redis功能.key过期天数,检查日期向后加该天数
    int EXPIRE_DAYS = 210;
}
