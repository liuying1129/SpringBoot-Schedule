package com.yklis.schedule.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 动态生成定时任务时，传输了该实体类:jobDetail.getJobDataMap().put("scheduleJob", job);
 * 该实体类只有实现Serializable接口，才能使用quartz的JobStoreTX类来实现集群功能,否则只能使用RAMJobStore
 * 尽管什么也没做，只是加了implements Serializable
 * @author ying07.liu
 * 为什么要序列化？
 * 1、将对象的状态保存在存储媒体中以便可以在以后重新创建出完全相同的副本
 * 2、按值将对象从一个应用程序域发送至另一个应用程序域
 * quartz实现集群时，是需要将状态保存在数据库的，所以该实体类要实现序列化
 */
public class CommCodeEntity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//类的数据成员是基本类型时,即使没有进行初始化,JAVA也会确保它获得默认值
	//但基本类型的局部变量并不会自动初始化
	//int为基本类型,默认值为0.数据库中为null,经过mybatis查询返为0。注:包装类Integer的默认值为null
	private int unid;
	private String typeName;
	private String id;
	private String name;
	private String pym;
	private String wbm;
	private String remark;
	private String reserve;
	private String reserve2;
	private String reserve3;
	private String reserve4;
	private int reserve5;
	private int reserve6;
	//double为基本类型,默认值为0.0
	private double reserve7;
	private double reserve8;
	private Date reserve9;
	private Date reserve10;
	
    public int getUnid() {
        return unid;
    }
    public void setUnid(int unid) {
       this.unid = unid;
    }		

    public String getTypeName() {
        return typeName;
    }
    public void setTypeName(String typeName) {
       this.typeName = typeName;
    }		

    public String getId() {
        return id;
    }
    public void setId(String id) {
       this.id = id;
    }		

    public String getName() {
        return name;
    }
    public void setName(String name) {
       this.name = name;
    }		

    public String getPym() {
        return pym;
    }
    public void setPym(String pym) {
       this.pym = pym;
    }		

    public String getWbm() {
        return wbm;
    }
    public void setWbm(String wbm) {
       this.wbm = wbm;
    }		

    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
       this.remark = remark;
    }		

    public String getReserve() {
        return reserve;
    }
    public void setReserve(String reserve) {
       this.reserve = reserve;
    }		

    public String getReserve2() {
        return reserve2;
    }
    public void setReserve2(String reserve2) {
       this.reserve2 = reserve2;
    }		

    public String getReserve3() {
        return reserve3;
    }
    public void setReserve3(String reserve3) {
       this.reserve3 = reserve3;
    }		

    public String getReserve4() {
        return reserve4;
    }
    public void setReserve4(String reserve4) {
       this.reserve4 = reserve4;
    }		

    public int getReserve5() {
        return reserve5;
    }
    public void setReserve5(int reserve5) {
       this.reserve5 = reserve5;
    }		

    public int getReserve6() {
        return reserve6;
    }
    public void setReserve6(int reserve6) {
       this.reserve6 = reserve6;
    }		

    public double getReserve7() {
        return reserve7;
    }
    public void setReserve7(double reserve7) {
       this.reserve7 = reserve7;
    }		

    public double getReserve8() {
        return reserve8;
    }
    public void setReserve8(double reserve8) {
       this.reserve8 = reserve8;
    }		

    public Date getReserve9() {
        return reserve9;
    }
    public void setReserve9(Date reserve9) {
       this.reserve9 = reserve9;
    }		

    public Date getReserve10() {
        return reserve10;
    }
    public void setReserve10(Date reserve10) {
       this.reserve10 = reserve10;
    }		

}
