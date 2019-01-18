package com.pisen.router.ui.phone.device;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.studio.os.LogCat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pisen.router.CloudApplication;
import com.pisen.router.config.HttpKeys;

/**
 * （净音云路由）
 * 路由器请求参数及缓存信息
 * @author Liuhc
 * @version 1.0 2015年5月12日 下午2:17:09
 */
public class PisenConstant {

	public static String sessionId = "";
	public static String username = "";
	
	/**登陆*/
	public static final String Action_Login = "login";
	/**退出*/
	public static final String Action_LoginOut = "loginOut";
	/**验证登陆*/
	public static final String Action_CheckLogin = "checkLogin";
	/**查询是否已有其它用户登录*/
	public static final String Action_Search = "search";
	/**查询路由器是否已进行过配置*/
	public static final String Action_UserInitRead = "userInitRead";
	/**设置路由器为已配置*/
	public static final String Action_UserInitSave = "userInitSave";
	/**查询用户是否同意协议*/
	public static final String Action_AgreementStaRead = "agreementStaRead";
	/**用户同意协议*/
	public static final String Action_SaveAgreementSel = "saveAgreementSel";
	
	/**读取上网模式配置*/
	public static final String Action_CurrentLinkModeRead = "currentLinkModeRead";
	/**读取有线上网配置*/
	public static final String Action_WiredConfigRead = "WiredConfigRead";
	/**读取无线中继上网配置*/
	public static final String Action_ReadRepeatStatus = "readRepeatStatus";
	
	
	/**读取无线配置*/
	public static final String Action_WireLesscfgRead = "WireLesscfgRead";
	/**修改无线配置*/
	public static final String Action_WireLesscfgSave = "WireLesscfgSave";
	
	/**无线中继个数扫描 & 设置无线中继上网*/
	public static final String Action_LinkAp = "linkAp";
	/**无线中继列表扫描 */
	public static final String Action_ReadValidAps = "readValidAps";
	/**查询无线中继是否成功 */
	public static final String Action_ReadApLinkSta = "readApLinkSta";
	
	/**设置静态获取IP上网 */
	public static final String Action_WiredConfigSave = "WiredConfigSave";
	/**设置动态获取IP上网*/
	public static final String Action_DynamicConfigSave = "DynamicConfigSave";
	/**设置/断开宽带拨号上网*/
	public static final String Action_PppoeAccountSave = "pppoeAccountSave";
	/**读取宽带拨号上网*/
	public static final String Action_PppoeAccountRead = "pppoeAccountRead";
	
	/**设置恢复出厂*/
	public static final String Action_ResetToDefaultAction = "resetToDefaultAction";
	/**恢复出厂后重启路由器*/
	public static final String Action_RouteReboot = "routeReboot";
	
	/**
	 * 请求路由器配置信息
	 * @param action  操作方法
	 * @param listener 回调
	 */
	public static void request(final String action,final IRouterResponse listener) {
		Map<String, String> params = new HashMap<String, String>();
		if (Action_Login.equals(action)) {
			params = PisenConstant.getLoginParams();
		}else if(Action_WireLesscfgRead.equals(action)){
			params = PisenConstant.getWirelessParams();
		}else if(Action_LinkAp.equals(action)){
			params = getWirelessListNumsParams();
		}else if(Action_ReadValidAps.equals(action)){
			params = getWirelessListParams();
		}else if(Action_WiredConfigRead.equals(action)){
			params = getWiredParams();
		}
		request(action, params, listener);
	}
	
	/**
	 * * 请求路由器配置信息
	 * @param action  操作方法
	 * @param listener 回调
	 * @param params 请求参数
	 */
	public static void request(final String action,final Map<String, String> params,final IRouterResponse listener) {
		StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpKeys.ROUTER_URL, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				LogCat.i("response -> " + response);
				listener.onSuccess(response);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				LogCat.e("error:" + error.getMessage());
				listener.onError(error.getMessage());
			}
		}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				LogCat.i("<Pisen>参数 -> " + params.toString());
				return params;
			}
//			@Override
//			public byte[] getBody() throws AuthFailureError {
//				LogCat.i("<Pisen>参数 -> " + params.toString());
//				return params.toString().getBytes();
//			}
		};

		RetryPolicy retryPolicy = new DefaultRetryPolicy(30000
				, DefaultRetryPolicy.DEFAULT_MAX_RETRIES
				, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		stringRequest.setRetryPolicy(retryPolicy);
		Volley.newRequestQueue(CloudApplication.getInstance()).add(stringRequest);
	}
	
	/**
	 * 获取登陆验证
	 * @return
	 */
	public static Map<String, String> getCheckLoginParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		map.put("username", "pisen");
		map.put("actName", "checkLogin");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
	
	/**
	 * 获取登陆参数
	 * @return
	 */
	public static Map<String, String> getLoginParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("password", "pisen");
		map.put("actName", "login");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
	
	/**
	 * 读取上网模式配置
	 * @return
	 */
	public static Map<String, String> getWiredLinkModeParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("actName", Action_CurrentLinkModeRead);
		map.put("reqNames", "readCurrentLinkMode");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
	
	/**
	 * 获取有线上网参数
	 * @return
	 */
	public static Map<String, String> getWiredParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("actName", Action_WiredConfigRead);
		map.put("reqNames", "readWiredConfig");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
	
	/**
	 * 读取无线中继上网配置
	 * @return
	 */
	public static Map<String, String> getWirelessStatusParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("actName", Action_ReadRepeatStatus);
		map.put("reqNames", "readRepeatStatus");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
	
	/**
	 * 读取宽带拨号上网配置
	 * @return
	 */
	public static Map<String, String> getWiredDialParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("actName", Action_PppoeAccountRead);
		map.put("reqNames", "readPppoeAccount");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
	
	/**
	 * 获取无线参数
	 * @return
	 */
	public static Map<String, String> getWirelessParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("actName", Action_WireLesscfgRead);
		map.put("reqNames", "readWireLesscfg");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
	
	/**
	 * 无线中继个数扫描
	 * @return
	 */
	public static Map<String, String> getWirelessListNumsParams(){
		Map<String, String> map = new HashMap<String, String>();
		try {
			JSONObject j2 = new JSONObject();
			j2.put("resv", "resv");
			JSONObject j = new JSONObject();
			j.put("sessionId", PisenConstant.sessionId);
			j.put("username", PisenConstant.username);
			j.put("actName", PisenConstant.Action_LinkAp);
			j.put("reqNames", "scanValidAps");
			j.put("cfgvalues", j2);
			map.put("datas", j.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	/**
	 * 无线中继列表扫描
	 * @return
	 */
	public static Map<String, String> getWirelessListParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", PisenConstant.sessionId);
		map.put("username", PisenConstant.username);
		map.put("actName", PisenConstant.Action_ReadValidAps);
		map.put("reqNames", "0");
		return map;
	}
	
	/**
	 * 查询无线中继是否成功
	 * @return
	 */
	public static Map<String, String> getIsWirelessSuccessParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", PisenConstant.sessionId);
		map.put("username", PisenConstant.username);
		map.put("actName", PisenConstant.Action_ReadApLinkSta);
		map.put("reqNames", "readApLinkSta");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
}
