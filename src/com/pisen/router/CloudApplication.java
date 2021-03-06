package com.pisen.router;

import io.vov.vitamio.Vitamio;

import java.io.File;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.studio.ApplicationSupport;
import android.studio.os.LogCat;
import android.studio.os.PreferencesUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.config.WifiConfig;
import com.pisen.router.core.filemanager.cancheinfo.WebdavCacheServiceUtils;
import com.pisen.router.core.filemanager.cancheinfo.WebdavCacheServiceUtils.ServiceToken;
import com.pisen.router.core.monitor.DiskMonitor;
import com.pisen.router.core.monitor.RedHotMonitor;
import com.pisen.router.core.monitor.WifiMonitor;
import com.pisen.router.core.monitor.WifiMonitor.WifiStateCallback;
import com.pisen.router.core.monitor.WifiSSIDMonitor;
import com.pisen.router.ui.phone.settings.upgrade.AppVersion;
import com.pisen.router.ui.phone.settings.upgrade.DownLoadApp;
import com.pisen.router.ui.phone.settings.upgrade.UpgradeApp.UpgradeAppCallBack;
import com.pisen.router.ui.phone.settings.upgrade.UpgradeAppService;
import com.pisen.router.ui.phone.settings.upgrade.UpgradeAppService.MyBinder;
import com.wefi.zhuiju.MyDataCenter;

//资源配置信息文件转移-> com.pisen.router.config.ResourceConfig文件
public class CloudApplication extends ApplicationSupport {
	private static CloudApplication instance = null;
	public static final File ROOT_PATH = Environment.getExternalStoragePublicDirectory("PisenRouter");
	public static final File CAMERA_PATH = Environment.getExternalStoragePublicDirectory("PisenRouter/即拍即传");
	public static final File RECORDER_PATH = Environment.getExternalStoragePublicDirectory("PisenRouter/即录即传");
	public static final File DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory("PisenRouter/Download");
	public static final File LOGO_PATH = Environment.getExternalStoragePublicDirectory("PisenRouter/~temp");
	private boolean isBind = false;
	private WifiMonitor wifiMonitor;
	private WifiStateCallback callback;

	public static CloudApplication getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		updateConfiguration(1.0f);
		super.onCreate();
		instance = this;
		setWifiDormancy();
		MyDataCenter.getInstance().initData(this);
		MediaScannerConnection.scanFile(this, new String[] { DOWNLOAD_PATH.getAbsolutePath() }, null, null);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		Log.e("DisplayMetrics", "width->" + dm.widthPixels + "  height" + dm.heightPixels + " density->" + dm.densityDpi);
		Log.e("DisplayMetrics", "memery->" + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB  ");
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				return Vitamio.initialize(CloudApplication.this, getResources().getIdentifier("libarm", "raw", getPackageName()));
			}
		}.execute();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// super.onConfigurationChanged(newConfig);
		updateConfiguration(1.0f);
	}

	private Configuration updateConfiguration(float fontScale) {
		Configuration confg = getResources().getConfiguration();
		confg.fontScale = 1.0f;
		getResources().updateConfiguration(confg, getResources().getDisplayMetrics());// 固定字体大小
		return confg;
	}

	private ServiceToken serviceToken;

	@Override
	public void onActivityLauncher() {
		super.onActivityLauncher();
		WifiMonitor.getInstance().startMonitor(this);
		DiskMonitor.getInstance().startMonitor(this);
		serviceToken = WebdavCacheServiceUtils.bindToService(this, null);
		bindService();
		monitorService();
	}

	@Override
	public void onActivityTerminate() {
		super.onActivityTerminate();
		WifiMonitor.getInstance().stopMonitor(this);
		DiskMonitor.getInstance().stopMonitor(this);
		WebdavCacheServiceUtils.unbindFromService(serviceToken);
		unbindService();
		restoreWifiDormancy();
	}

	/**
	 * 设置wifi休眠策略
	 */
	private void setWifiDormancy() {
		int value = Settings.System.getInt(getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY, Settings.Global.WIFI_SLEEP_POLICY_DEFAULT);
		PreferencesUtils.setInt(Settings.Global.WIFI_SLEEP_POLICY, value);
		if (Settings.Global.WIFI_SLEEP_POLICY_NEVER != value) {
			Settings.System.putInt(getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY, Settings.Global.WIFI_SLEEP_POLICY_NEVER);
		}
	}

	/**
	 * 重置wifi休眠策略为之前默认配置
	 */
	private void restoreWifiDormancy() {
		Settings.System.putInt(getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY,
				PreferencesUtils.getInt(Settings.Global.WIFI_SLEEP_POLICY, Settings.Global.WIFI_SLEEP_POLICY_DEFAULT));
	}

	public void bindService() {
		Intent intents = new Intent(this, UpgradeAppService.class);
		this.isBind = bindService(intents, connection, Service.BIND_AUTO_CREATE);
	}

	public void monitorService() {
		wifiMonitor = WifiMonitor.getInstance();
		callback = new WifiStateCallback() {
			@Override
			public void onConnected(WifiConfig config) {
				if (appService != null) {
					appService.refresh();
				}
				WifiSSIDMonitor.getInstance().notifyChange(config, true);
			}

			@Override
			public void onDisconnected(WifiConfig config) {
				WifiSSIDMonitor.getInstance().notifyChange(config, false);
			}
		};
		wifiMonitor.registerObserver(callback);
	}

	private UpgradeAppService appService;
	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			appService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			((MyBinder) service).setUpgradeAppCallBack(back);
			((MyBinder) service).setIsShow(false);
			appService = ((MyBinder) service).getUpgradeAppService();
			appService.refresh();
		}
	};

	private UpgradeAppCallBack back = new UpgradeAppCallBack() {
		@Override
		public void downLoad(final AppVersion ver) throws NumberFormatException, Exception {
			final DownLoadApp app = new DownLoadApp();
			app.apkUrl = ver.Link;
			PreferencesUtils.setString(KeyUtils.APP_NETVER, ver.Version);
			if (!UIHelper.getVersion(CloudApplication.this).equals(ver.Version)) {
				RedHotMonitor.getInstance().notifyUpdate(ver, app);
				PreferencesUtils.setBoolean(KeyUtils.APP_VERSION, true);
			} else {
				PreferencesUtils.setBoolean(KeyUtils.APP_VERSION, false);
			}
		}

		@Override
		public void callBack(String result) {
			LogCat.e("%s\n", "callBack 出错,您的网络不太顺畅哦~");
		}
	};

	public void unbindService() {
		if (this.isBind) {
			unbindService(connection);
			this.isBind = false;
		}
		// wifiMonitor.unregisterObserver(callback);
	}
}
