/**
 * 
 */
package com.pisen.router.config;

import android.studio.os.PreferencesUtils;


/**
 * 轻量级的配置信息
 * @author Liuhc
 * @version 1.0 2015年5月19日 下午7:31:33
 */
public class Config {

	/** 设备WIFI */
	public static final String DEVICE_WIFI_INFO = "DEVICE_WIFI_INFO";
	/** 设备外网无线WIFI */
	public static final String DEVICE_OUTTER_WIFI_INFO = "DEVICE_OUTTER_WIFI_INFO";
	/** 品胜版本登陆管理密码 */
	public static final String DEVICE_MANAGER_PASSWORD = "DEVICE_MANAGER_PASSWORD";
	
	/** 当有新的下载任务或者上传任务时 */
	public static final String NEW_MESSAGE_TASK = "NEW_MESSAGE_TASK";
	
	/** 云盘是否有存储空间  */
	public static boolean hasStorage = false;
	
	/** WIFI是否隐藏密码  */
	public static final String SHOW_PASSWORD = "NEW_MESSAGE_TASK";
	
	public static String getDeviceMgrPassword() {
		return PreferencesUtils.getString(DEVICE_MANAGER_PASSWORD, null);
	}

	public static void setDeviceMgrPassword(String pwd) {
		PreferencesUtils.setString(DEVICE_MANAGER_PASSWORD, pwd);
	}
	
	public static String getWifiConfig() {
		return PreferencesUtils.getString(DEVICE_WIFI_INFO, null);
	}

	public static void setWifiConfig(String info) {
		PreferencesUtils.setString(DEVICE_WIFI_INFO, info);
	}
	
	public static String getWifiOutterConfig() {
		return PreferencesUtils.getString(DEVICE_OUTTER_WIFI_INFO, null);
	}

	public static void setWifiOutterConfig(String info) {
		PreferencesUtils.setString(DEVICE_OUTTER_WIFI_INFO, info);
	}
	
	public static boolean hasNewMessageTask() {
		return PreferencesUtils.getBoolean(NEW_MESSAGE_TASK, false);
	}

	public static void setNewMessageTask(boolean info) {
		PreferencesUtils.setBoolean(NEW_MESSAGE_TASK, info);
	}

	public static boolean hasStorage() {
		return hasStorage;
	}

	public static void setHasStorage(boolean hasStorage) {
		Config.hasStorage = hasStorage;
	}

	public static boolean isShowPassword() {
		return PreferencesUtils.getBoolean(SHOW_PASSWORD, false);
	}

	public static void setShowPassword(boolean showPassword) {
		PreferencesUtils.setBoolean(NEW_MESSAGE_TASK, showPassword);
	}
	
}
