package com.yklis.schedule.entity;

public class CrDingDanHuoPinEntity {
	
    private String dingDanBianHao;
    private String huoPinBianHao;
    private String huoPinPiCi;
    private double huoPinShuLiang;
    private String huoPinDanWei;
    
    private CrHuoPinXinXiEntity crHuoPinXinXiEntity;

    public String getDingDanBianHao() {
        return dingDanBianHao;
    }
    public void setDingDanBianHao(String dingDanBianHao) {
       this.dingDanBianHao = dingDanBianHao;
    }	     

    public String getHuoPinBianHao() {
       return huoPinBianHao;
    }
    public void setHuoPinBianHao(String huoPinBianHao) {
      this.huoPinBianHao = huoPinBianHao;
    }	     
  
    public String getHuoPinPiCi() {
      return huoPinPiCi;
    }
    public void setHuoPinPiCi(String huoPinPiCi) {
      this.huoPinPiCi = huoPinPiCi;
    }	     
 
    public double getHuoPinShuLiang() {
      return huoPinShuLiang;
    }
	public void setHuoPinShuLiang(double huoPinShuLiang) {
	    this.huoPinShuLiang = huoPinShuLiang;
	}	     
	
	public String getHuoPinDanWei() {
	    return huoPinDanWei;
	}
	public void setHuoPinDanWei(String huoPinDanWei) {
	   this.huoPinDanWei = huoPinDanWei;
	}	     

	public CrHuoPinXinXiEntity getCrHuoPinXinXi() {
	    return crHuoPinXinXiEntity;
	}
	public void setCrHuoPinXinXi(CrHuoPinXinXiEntity crHuoPinXinXiEntity) {
	   this.crHuoPinXinXiEntity = crHuoPinXinXiEntity;
	}	     


}
