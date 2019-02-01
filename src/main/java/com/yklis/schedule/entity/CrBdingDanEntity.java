package com.yklis.schedule.entity;

import java.util.Date;
import java.util.List;

public class CrBdingDanEntity {
	
    private String dingDanBianHao;
    private String yunShuMoShi;
    private String fuWuFangShi;
    private String jieSuanFangShi;
    private Date faHuoRiQi;//java.util.Date:该类型包含日期时间
    private int shiXiaoYaoQiu;
    private String shouHuoKeHuBianHao;
    private String liLunJianShu;
    private String jianShu;//件数，用String是为了与数据库中一到
    private double zhongLiang;
    private String shiFaZhan;
    private String muDiZhan;
    private String xuFanHuiWenDuJi;
    private String shiFouTouBao;
    private double touBaoJinE;
    private String dingDanBeiZhu;
    private String dangQianZhuangTai;
    private String xianLuHao;
    private String erpDingDanHao;
    private String carNo;
    private String xingCheDanHao;
    private String shangPinLeiXing;
    
    private List<CrDingDanHuoPinEntity> crDingDanHuoPinEntityList;
    private CrShouHuoKeHuEntity crShouHuoKeHuEntity;
    private CrShouHuoDiZhiEntity crShouHuoDiZhiEntity;
    
    public String getDingDanBianHao() {
        return dingDanBianHao;
    }
    public void setDingDanBianHao(String dingDanBianHao) {
       this.dingDanBianHao = dingDanBianHao;
    }	     
   
    public String getYunShuMoShi() {
        return yunShuMoShi;
    }
    public void setYunShuMoShi(String yunShuMoShi) {
       this.yunShuMoShi = yunShuMoShi;
    }	     
   
    public String getFuWuFangShi() {
        return fuWuFangShi;
    }
    public void setFuWuFangShi(String fuWuFangShi) {
       this.fuWuFangShi = fuWuFangShi;
    }	     
   
    public String getJieSuanFangShi() {
        return jieSuanFangShi;
    }
    public void setJieSuanFangShi(String jieSuanFangShi) {
       this.jieSuanFangShi = jieSuanFangShi;
    }	     
   
    public Date getFaHuoRiQi() {
        return faHuoRiQi;
    }
    public void setFaHuoRiQi(Date faHuoRiQi) {
       this.faHuoRiQi = faHuoRiQi;
    }	     
   
    public int getShiXiaoYaoQiu() {
        return shiXiaoYaoQiu;
    }
    public void setShiXiaoYaoQiu(int shiXiaoYaoQiu) {
       this.shiXiaoYaoQiu = shiXiaoYaoQiu;
    }	     
   
    public String getShouHuoKeHuBianHao() {
        return shouHuoKeHuBianHao;
    }
    public void setShouHuoKeHuBianHao(String shouHuoKeHuBianHao) {
       this.shouHuoKeHuBianHao = shouHuoKeHuBianHao;
    }	     
   
    public String getLiLunJianShu() {
        return liLunJianShu;
    }
    public void setLiLunJianShu(String liLunJianShu) {
       this.liLunJianShu = liLunJianShu;
    }	     
   
    public String getJianShu() {
       return jianShu;
    }
    public void setJianShu(String jianShu) {
      this.jianShu = jianShu;
    }	     
  
    public double getZhongLiang() {
        return zhongLiang;
    }
    
    public void setZhongLiang(double zhongLiang) {
      this.zhongLiang = zhongLiang;
    }
  
    public String getShiFaZhan() {
        return shiFaZhan;
    }
    public void setShiFaZhan(String shiFaZhan) {
       this.shiFaZhan = shiFaZhan;
    }	     
   
    public String getMuDiZhan() {
        return muDiZhan;
    }
    public void setMuDiZhan(String muDiZhan) {
       this.muDiZhan = muDiZhan;
    }	     
   
    public String getXuFanHuiWenDuJi() {
        return xuFanHuiWenDuJi;
    }
    public void setXuFanHuiWenDuJi(String xuFanHuiWenDuJi) {
       this.xuFanHuiWenDuJi = xuFanHuiWenDuJi;
    }	     
   
    public String getShiFouTouBao() {
        return shiFouTouBao;
    }
    public void setShiFouTouBao(String shiFouTouBao) {
       this.shiFouTouBao = shiFouTouBao;
    }	     
   
    public double getTouBaoJinE() {
        return touBaoJinE;
    }
    public void setTouBaoJinE(double touBaoJinE) {
       this.touBaoJinE = touBaoJinE;
    }	     
   
    public String getDingDanBeiZhu() {
        return dingDanBeiZhu;
    }
    public void setDingDanBeiZhu(String dingDanBeiZhu) {
       this.dingDanBeiZhu = dingDanBeiZhu;
    }	     
   
    public String getDangQianZhuangTai() {
        return dangQianZhuangTai;
    }
    public void setDangQianZhuangTai(String dangQianZhuangTai) {
       this.dangQianZhuangTai = dangQianZhuangTai;
    }	     
   
    public String getXianLuHao() {
         return xianLuHao;
    }
    public void setXianLuHao(String xianLuHao) {
        this.xianLuHao = xianLuHao;
    }	     
    
    public String getErpDingDanHao() {
         return erpDingDanHao;
    }
    public void setErpDingDanHao(String erpDingDanHao) {
        this.erpDingDanHao = erpDingDanHao;
    }	
    
    public String getCarNo() {
         return carNo;
    }
    public void setCarNo(String carNo) {
        this.carNo = carNo;
    }	     
    
    public String getXingCheDanHao() {
        return xingCheDanHao;
    }
    public void setXingCheDanHao(String xingCheDanHao) {
       this.xingCheDanHao = xingCheDanHao;
    }
                   
    public String getShangPinLeiXing() {
         return shangPinLeiXing;
    }
    public void setShangPinLeiXing(String shangPinLeiXing) {
        this.shangPinLeiXing = shangPinLeiXing;
    }	
    
    public List<CrDingDanHuoPinEntity> getCrDingDanHuoPinList(){
    	return crDingDanHuoPinEntityList;
    }
    public void setCrDingDanHuoPinList(List<CrDingDanHuoPinEntity> crDingDanHuoPinEntityList){
    	this.crDingDanHuoPinEntityList = crDingDanHuoPinEntityList;
    }

	public CrShouHuoKeHuEntity getCrShouHuoKeHu() {
	    return crShouHuoKeHuEntity;
	}
	public void setCrShouHuoKeHu(CrShouHuoKeHuEntity crShouHuoKeHuEntity) {
	   this.crShouHuoKeHuEntity = crShouHuoKeHuEntity;
	}	     

	public CrShouHuoDiZhiEntity getCrShouHuoDiZhi() {
	    return crShouHuoDiZhiEntity;
	}
	public void setCrShouHuoDiZhi(CrShouHuoDiZhiEntity crShouHuoDiZhiEntity) {
	   this.crShouHuoDiZhiEntity = crShouHuoDiZhiEntity;
	}	     

}
