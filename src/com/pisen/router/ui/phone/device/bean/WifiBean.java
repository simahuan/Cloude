package com.pisen.router.ui.phone.device.bean;

import java.io.Serializable;

/**
 * WIFI信息
 * @author Liuhc
 * @version 1.0 2015年5月14日 下午4:34:22
 */
public class WifiBean implements Serializable,Cloneable{

	String ssid;
	String mode;
	String encryption;
	String key;
	int netWorkId;
	
	String charset;
	String signal;
	String channel;
	
	String wirelessTerm;
	String wireless_net_name;
	String wireless_net_secruity;
	String hide_wifi;
	String disabled;
	String state;
	String rate;
	String ap_separate;
	String shortgi;
	String open_wmm;
	String capacity;
	String wireLessSwitch;
	String wireless_net_passwd;
	int color;
	boolean isConnnect;
	
	public String getDisabled() {
		return disabled;
	}
	public void setDisabled(String disabled) {
		this.disabled = disabled;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public boolean isConnnect() {
		return isConnnect;
	}
	public void setConnnect(boolean isConnnect) {
		this.isConnnect = isConnnect;
	}
	public int getColor() {
		return color;
	}
	public void setColor(int color) {
		this.color = color;
	}
	public String getSsid() {
		return ssid;
	}
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getEncryption() {
		return encryption;
	}
	public void setEncryption(String encryption) {
		this.encryption = encryption;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getNetWorkId() {
		return netWorkId;
	}
	public void setNetWorkId(int netWorkId) {
		this.netWorkId = netWorkId;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
	public String getSignal() {
		return signal;
	}
	public void setSignal(String signal) {
		this.signal = signal;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getWirelessTerm() {
		return wirelessTerm;
	}
	public void setWirelessTerm(String wirelessTerm) {
		this.wirelessTerm = wirelessTerm;
	}
	public String getWireless_net_name() {
		return wireless_net_name;
	}
	public void setWireless_net_name(String wireless_net_name) {
		this.wireless_net_name = wireless_net_name;
	}
	public String getWireless_net_secruity() {
		return wireless_net_secruity;
	}
	public void setWireless_net_secruity(String wireless_net_secruity) {
		this.wireless_net_secruity = wireless_net_secruity;
	}
	public String getHide_wifi() {
		return hide_wifi;
	}
	public void setHide_wifi(String hide_wifi) {
		this.hide_wifi = hide_wifi;
	}
	public String getRate() {
		return rate;
	}
	public void setRate(String rate) {
		this.rate = rate;
	}
	public String getAp_separate() {
		return ap_separate;
	}
	public void setAp_separate(String ap_separate) {
		this.ap_separate = ap_separate;
	}
	public String getShortgi() {
		return shortgi;
	}
	public void setShortgi(String shortgi) {
		this.shortgi = shortgi;
	}
	public String getOpen_wmm() {
		return open_wmm;
	}
	public void setOpen_wmm(String open_wmm) {
		this.open_wmm = open_wmm;
	}
	public String getCapacity() {
		return capacity;
	}
	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}
	public String getWireLessSwitch() {
		return wireLessSwitch;
	}
	public void setWireLessSwitch(String wireLessSwitch) {
		this.wireLessSwitch = wireLessSwitch;
	}
	public String getWireless_net_passwd() {
		return wireless_net_passwd;
	}
	public void setWireless_net_passwd(String wireless_net_passwd) {
		this.wireless_net_passwd = wireless_net_passwd;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
}
