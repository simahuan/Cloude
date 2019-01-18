package com.pisen.router.ui.phone.device;

import java.util.Random;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.studio.os.LogCat;
import android.studio.os.NetUtils;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.config.ResourceConfig;
import com.pisen.router.core.device.AbstractDevice;
import com.pisen.router.core.monitor.entity.RouterConfig.Model;
import com.pisen.router.ui.base.CloudActivity;
import com.pisen.router.ui.phone.device.bean.FirmwareData;
import com.pisen.router.ui.phone.device.bean.ZFirmwareInfo;

/**
 * 固件升级
 * 
 * @author Liuhc
 * @version 1.0 2015年5月18日 上午9:55:18
 */
public class FirmwareUpgradeActivity extends CloudActivity implements OnClickListener {

	private static final String FIRMWARE_STATE_DOWNLOAD = "download"; // 正在下载
	private static final String FIRMWARE_STATE_ABNORMAL = "abnormal"; // 下载被异常中断
	private static final String FIRMWARE_STATE_COMPLET = "complet"; // 下载完成
	
	private Button btnDeviceUpgrade;
	private ImageView ivPoint,ivlogo;
	private TextView tvDeviceVersion, tvDeviceNewVersion;
	private TextView tvDeviceMode, tvTip,txtProgress,txtProgressPa;
	private Handler handler;
	private ZFirmwareInfo firmwareInfo;
	private boolean isUpdate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_device_firmwareupgrade);

		ivPoint = (ImageView) findViewById(R.id.ivPoint);
		ivlogo = (ImageView) findViewById(R.id.ivlogo);
		btnDeviceUpgrade = (Button) findViewById(R.id.btnDeviceUpgrade);
		tvDeviceVersion = (TextView) findViewById(R.id.tvDeviceVersion);
		tvDeviceMode = (TextView) findViewById(R.id.tvDeviceMode);
		tvDeviceNewVersion = (TextView) findViewById(R.id.tvDeviceNewVersion);
		txtProgress = (TextView) findViewById(R.id.txtProgress);
		txtProgressPa = (TextView) findViewById(R.id.txtProgressPa);
		tvTip = (TextView) findViewById(R.id.tvTip);
		btnDeviceUpgrade.setOnClickListener(this);
		btnDeviceUpgrade.setText(getString(R.string.dev_upgrade_checkup));
		tvDeviceMode.setText(ResourceConfig.getInstance(this).getDeviceName());

		initViews();
		
		if (NetUtils.isWifiConnected(this)) {
			if (Model.RZHIXIANG.equals(ResourceConfig.getInstance(this).getDeviceMode())) {
				isUpdate = true;
				new FirmwareGetAsyncTask().execute();
				return;
			}else{
				tvDeviceVersion.setVisibility(View.INVISIBLE);
				tvDeviceNewVersion.setVisibility(View.GONE);
				btnDeviceUpgrade.setVisibility(View.GONE);
				tvTip.setText("正在检查更新,请稍后…");
				startAnimation();
			}
		} else {
			UIHelper.showToast(this, "网络不给力");
		}
		isUpdate = false;
	}

	private void initViews(){
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == 0) {
					new FirmwareDownAsyncTask().execute(true);
				}
			}
		};
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnDeviceUpgrade:
			if (NetUtils.isWifiConnected(this)) {
				upgrade();
			} else {
				UIHelper.showToast(this, "网络不给力");
			}
			break;
		default:
			break;
		}

	}

	/**
	 * 显示升级确认对话框
	 */
	private void showFirmwareDialog() {
		// 未发现设备
		ConfirmDialog dialog = new ConfirmDialog(FirmwareUpgradeActivity.this);
		dialog.setTitle("固件升级");
		dialog.setMessage(FirmwareUpgradeActivity.this.getString(R.string.dev_upgrade_execute_tip));
		dialog.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialog.setPositiveButton("现在更新", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				new ExecuteAsyncTask().execute();
			}
		});
		dialog.show();
	}
	
	public void back(View view) {
		finish();
	}

	public void upgrade() {
		String txt = btnDeviceUpgrade.getText().toString();
		if (!TextUtils.isEmpty(txt)) {
			if (txt.equals(getString(R.string.dev_upgrade_checkup))) {
				new FirmwareGetAsyncTask().execute();
			} else if (txt.equals(getString(R.string.dev_upgrade_download))) {
				isRunning = false;
				new FirmwareDownAsyncTask().execute(false);
			} else if (txt.equals(getString(R.string.dev_upgrade_execute))) {
				showFirmwareDialog();
			}
		}
	}

	public void startAnimation() {
		if (!AbstractDevice.getInstance().isLogin(FirmwareUpgradeActivity.this)) {
			return;
		}
		if (ivPoint != null) {
			ivPoint.clearAnimation();
			final RotateAnimation animation = new RotateAnimation(0f, 359f, Animation.RELATIVE_TO_PARENT, 0.03f, Animation.RELATIVE_TO_PARENT, 0.5f);
			animation.setDuration(2000);
			animation.setRepeatCount(-1);
			animation.setInterpolator(new LinearInterpolator());
			animation.setFillAfter(false);
			animation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					if (!isUpdate) {
						tvTip.setText("当前已是最新版本");
					}
				}
			});
			
			ivPoint.startAnimation(animation);
			
			if (!Model.RZHIXIANG.equals(ResourceConfig.getInstance(FirmwareUpgradeActivity.this).getDeviceMode())) {
				long time = (long)((new Random().nextInt(8))*1000);
			    if (time == 0) {
			    	time = 2000;
				}
			    
			    handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						ivPoint.clearAnimation();
					}
				}, time);
			}
		}
	}

	public void stopAnimation() {
		if (ivPoint != null) {
			ivPoint.clearAnimation();
		}
	}

	/**
	 * 刷新界面数据
	 * 
	 * @param info
	 */
	private void setData(ZFirmwareInfo info) {
		this.firmwareInfo = info;
		// 当前版本
		tvDeviceVersion.setText("当前固件版本:v" + info.getCur_version_name());
		tvDeviceVersion.setVisibility(View.VISIBLE);

		// 判断是否有新版本
		if (TextUtils.isEmpty(info.getService_version_name())) {
			// 无新版本
			tvTip.setText("当前已是最新版本");
		} else if (!TextUtils.isEmpty(info.getService_version_name())) {
			// 有新版本
			if (info.getService_version_name().equals(info.getNew_version_name())) {
				// 服务器已经下载完成最新版本,立即更新
				tvTip.setText("最新固件版本v" + info.getService_version_name() + "已完成下载");
				tvDeviceNewVersion.setText("");
				tvDeviceNewVersion.setVisibility(View.GONE);
				btnDeviceUpgrade.setText(getString(R.string.dev_upgrade_execute));
			} else {
				tvTip.setText("检测到您有新的版本可以更新");
				tvDeviceNewVersion.setText("最新固件版本:v" + info.getService_version_name());
				tvDeviceNewVersion.setVisibility(View.VISIBLE);
				btnDeviceUpgrade.setText(getString(R.string.dev_upgrade_download));
			}
			btnDeviceUpgrade.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 更新下载进度
	 * @param progress
	 */
	private void upgradeProgress(boolean isReset,String progress){
		LogCat.e("当前下载进度:%s", progress+"%");
		if (isReset) {
			txtProgress.setText("0");
			txtProgress.setVisibility(View.GONE);
			txtProgressPa.setVisibility(View.GONE);
			ivlogo.setVisibility(View.VISIBLE);
		}else{
			txtProgress.setText(progress);
			txtProgress.setVisibility(View.VISIBLE);
			txtProgressPa.setVisibility(View.VISIBLE);
			ivlogo.setVisibility(View.INVISIBLE);
		}
	}
	
	private class FirmwareGetAsyncTask extends AsyncTask<String, Void, ZFirmwareInfo> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			tvDeviceVersion.setVisibility(View.INVISIBLE);
			tvDeviceNewVersion.setVisibility(View.GONE);
			btnDeviceUpgrade.setVisibility(View.GONE);
			tvTip.setText("正在检查更新,请稍后…");
			startAnimation();
		}

		@Override
		protected ZFirmwareInfo doInBackground(String... params) {
			return AbstractDevice.getInstance().getFirmwareInfo();
		}

		@Override
		protected void onPostExecute(ZFirmwareInfo result) {
			// 停止动画
			stopAnimation();
			if (result != null) {
				setData(result);
			} else {
				tvTip.setText("");
				UIHelper.showToast(FirmwareUpgradeActivity.this, "检测版本信息出错");
			}
		}
	}

	private boolean isRunning = false;
	private class FirmwareDownAsyncTask extends AsyncTask<Boolean, Void, FirmwareData> {
		boolean isProgressQuery;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			tvTip.setText("正在下载最新固件,请稍后…");
			tvDeviceNewVersion.setVisibility(View.GONE);
			btnDeviceUpgrade.setVisibility(View.GONE);
			if (!isRunning) {
				isRunning = true;
				startAnimation();
			}
		}

		@Override
		protected FirmwareData doInBackground(Boolean... params) {
			isProgressQuery = params[0];
			return AbstractDevice.getInstance().downloadFirmware(isProgressQuery);
		}

		@Override
		protected void onPostExecute(FirmwareData result) {
			super.onPostExecute(result);
			if (result != null) {
				if(!isProgressQuery) {
					upgradeProgress(false,"0");
					//间隔一秒去请求一次
					handler.sendEmptyMessageDelayed(0, 1000);
					return;
				}
				
				if (FIRMWARE_STATE_COMPLET.equals(result.getState())) {
					LogCat.e("下载完成");
					stopAnimation();
					upgradeProgress(false,"100");
					tvTip.setText("最新固件版本v" + firmwareInfo.getService_version_name() + "已完成下载");
					tvDeviceNewVersion.setText("");
					tvDeviceNewVersion.setVisibility(View.GONE);
					btnDeviceUpgrade.setText(getString(R.string.dev_upgrade_execute));
					btnDeviceUpgrade.setVisibility(View.VISIBLE);
					return;
				}else if( FIRMWARE_STATE_DOWNLOAD.equals(result.getState())){
					if (!TextUtils.isEmpty(result.getReceived_Percentage())) {
						upgradeProgress(false,result.getReceived_Percentage());
						//间隔一秒去请求一次
						handler.sendEmptyMessageDelayed(0, 1000);
						return;
					}
				}
				LogCat.e("下载被异常中断:%s",result.getState());
				stopAnimation();
				upgradeProgress(true,"0");
				tvTip.setText("下载最新固件版本出错");
				tvDeviceNewVersion.setText("最新固件版本:v" + firmwareInfo.getService_version_name());
				tvDeviceNewVersion.setVisibility(View.VISIBLE);
				btnDeviceUpgrade.setText(getString(R.string.dev_upgrade_download));
				btnDeviceUpgrade.setVisibility(View.VISIBLE);
			}else{
				LogCat.e("下载最新固件出错");
				stopAnimation();
				tvTip.setText("");
				UIHelper.showToast(FirmwareUpgradeActivity.this, "下载最新固件出错");
			}
		}
	}

	
	/**
	 * 执行升级
	 * 
	 * @author Liuhc
	 * @version 1.0 2015年7月3日 下午1:33:29
	 */
	private class ExecuteAsyncTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			tvDeviceNewVersion.setVisibility(View.GONE);
			btnDeviceUpgrade.setVisibility(View.GONE);
			startAnimation();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			return AbstractDevice.getInstance().executeUpgrade();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			// 停止动画
			stopAnimation();
			if (result) {
				WifiConnectUtils wifiMgr = new WifiConnectUtils(FirmwareUpgradeActivity.this);
				wifiMgr.disconnectCurrent();
				tvTip.setText(FirmwareUpgradeActivity.this.getString(R.string.dev_upgrade_execute_wait));
				UIHelper.showToast(FirmwareUpgradeActivity.this, "设备即将自动升级,请稍后");
			} else {
				UIHelper.showToast(FirmwareUpgradeActivity.this, "设备升级失败");
				btnDeviceUpgrade.setVisibility(View.VISIBLE);
			}
		}
	}
}
