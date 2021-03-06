package com.pisen.router.config;

/**
 * http 请求地址
 */
public class HttpKeys {
	public static final String TEST_APP = "http://test.soa.pisen.com.cn:9212/baseapp/AppVersionService.svc/rest/GetLatestVersion?appKey=13602889";

	public static final String BASE_URL = "http://soa.pisen.com.cn";
	/**
	 * 启动页广告图片地址
	 */
	public static final String START_BANNER_URL = BASE_URL + "/adsense/AdsenseService.svc/rest/GetAdvertises?code=A00001";
	/**
	 * 主界面广告图片地址
	 */
	public static final String MAIN_BANNER_URL = BASE_URL + "/adsense/AdsenseService.svc/rest/GetAdvertises?code=I00002";

	/**
	 * 软件更新
	 */
//	public static final String REFRESH_APP = TEST_APP;
	public static final String REFRESH_APP = BASE_URL + "/platform.app/AppVersionService.svc/rest/GetLatestVersion?AppKey=13602889";
	public static final String TEST_BAIDU = "www.baidu.com";
	
	/**
	 * 登录
	 */
	public static final String USER_LOGIN = "http://sso.pisen.com.cn/api/Token/EncryptRequestToken";

	/**
	 * 帮助
	 */
	public static final String HELP_URL = "http://cloud.pisen.com.cn/route/CloudService.svc/GetDocuments?categoryCode=D00001";
	public static final String HELP_WEBPAGE_URL = "http://cloud.pisen.com.cn/mobile/Help/HelpDetail/%s";
	/**
	 * 意见反馈反馈
	 */
	public static final String FEED_BACK_URL = "http://cloud.pisen.com.cn/route/CloudService.svc/AddFeedback";
	/**
	 *空气净化状态 
	 */
	public static final String AIR_CLEAN_STATU = "http://192.168.168.1/cgi-bin/dev.cgi?actName=airPurge&reqName=getStatus";
	/**
	 *打开空气净化
	 */
	public static final String AIR_CLEAN_OPEN = "http://192.168.168.1/cgi-bin/dev.cgi?actName=airPurge&reqName=open";
	/**
	 *关闭空气净化
	 */
	public static final String AIR_CLEAN_CLOSE = "http://192.168.168.1/cgi-bin/dev.cgi?actName=airPurge&reqName=close";
	
	public static final String ROUTER_URL = "http://192.168.168.1/cgi-bin/dev.cgi";
	/**
	 * 使用帮助内置静态html
	 */
	public static final String USEING_HELP_URL ="file:///android_asset/helpCenter.html";
	
	/**
	 * 品胜微商城　
	 */
	public static final String PISENEASY_URL ="http://m.piseneasy.com";
}
