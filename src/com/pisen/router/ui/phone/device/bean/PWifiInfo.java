package com.pisen.router.ui.phone.device.bean;

/**
 * 
 * 有效可连接WIFI
 * @author Liuhc
 * @version 1.0 2015年5月13日 下午1:32:17
 */
public class PWifiInfo {

	String essid;
	String rate;
	String sn;
	String encrypt;
	String passwd;
	
	public String getEssid() {
		return essid;
	}
	public void setEssid(String essid) {
		this.essid = essid;
	}
	public String getRate() {
		return rate;
	}
	public void setRate(String rate) {
		this.rate = rate;
	}
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getEncrypt() {
		return encrypt;
	}
	public void setEncrypt(String encrypt) {
		this.encrypt = encrypt;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	
	
}
