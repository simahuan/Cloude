package com.pisen.router.ui.phone.device.bean;

/**
 * 固件信息
 * @author Liuhc
 * @version 1.0 2015年7月3日09:37:14
 */
public class ZFirmwareInfo {

	/**
	 * "distrib_id": "WefiOS",
        "distrib_name": "clover",
        "distrib_release": "1.0.2",
        "machine": "MT7620a zorlik evaluation board",
        "cur_version_name": "1.0.2",
        "new_version_name": "1.0.2",
		"new_firm_info": "固件描述信息",
        "service_version_name": "1.0.2"
        “service_firm_info”:”固件描述信息”
	 */
	String distrib_id;
	String distrib_name;
	String distrib_release;
	String machine;
	String cur_version_name;
	String new_version_name;
	String new_firm_info;
	String service_version_name;
	String service_firm_info;
	
	public String getDistrib_id() {
		return distrib_id;
	}
	public void setDistrib_id(String distrib_id) {
		this.distrib_id = distrib_id;
	}
	public String getDistrib_name() {
		return distrib_name;
	}
	public void setDistrib_name(String distrib_name) {
		this.distrib_name = distrib_name;
	}
	public String getDistrib_release() {
		return distrib_release;
	}
	public void setDistrib_release(String distrib_release) {
		this.distrib_release = distrib_release;
	}
	public String getMachine() {
		return machine;
	}
	public void setMachine(String machine) {
		this.machine = machine;
	}
	public String getCur_version_name() {
		return cur_version_name;
	}
	public void setCur_version_name(String cur_version_name) {
		this.cur_version_name = cur_version_name;
	}
	public String getNew_version_name() {
		return new_version_name;
	}
	public void setNew_version_name(String new_version_name) {
		this.new_version_name = new_version_name;
	}
	public String getNew_firm_info() {
		return new_firm_info;
	}
	public void setNew_firm_info(String new_firm_info) {
		this.new_firm_info = new_firm_info;
	}
	public String getService_version_name() {
		return service_version_name;
	}
	public void setService_version_name(String service_version_name) {
		this.service_version_name = service_version_name;
	}
	public String getService_firm_info() {
		return service_firm_info;
	}
	public void setService_firm_info(String service_firm_info) {
		this.service_firm_info = service_firm_info;
	}
	
	
}
