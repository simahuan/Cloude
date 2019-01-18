package com.pisen.router.ui.phone.welcome;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.studio.os.PreferencesUtils;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.ui.HomeActivity;
import com.pisen.router.ui.base.CloudActivity;

/**
 * 引导欢迎页
 * @author  mahuan
 * @version 1.0 2015年6月11日 上午10:17:00
 */
public class WelcomActivity extends CloudActivity {
	/** 是否首次启动 */
	public static final String StartCount = "StartCount";
	/** 启动时间停留3秒 */
	public static final int START_SLEEP_TIME = 1500;
	/** 第一次启动 */
	private static final int REQUEST_GUIDE = 100;
	/** 显示启动页的图片 */
	private RelativeLayout relLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cloud_welcome);
		initView();
//		initService();
	}

	@Override
	protected void onResume() {
		super.onResume();
		initService();
	}
	
	@SuppressWarnings("deprecation")
	private void initView(){
		relLayout = (RelativeLayout) findViewById(R.id.relLayout_welcome);
		((TextView)relLayout.findViewById(R.id.txtPublishVer)).setText(UIHelper.getCloudVersion(this));
		Bitmap bit = hasStartImg();
		if (bit != null) {
			relLayout.setBackground(new BitmapDrawable(bit));
		}
	}
	

	private void initService(){
		//停留3秒 
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (isFirstStart()) {
					startActivityForResult(new Intent(WelcomActivity.this, GuideActivity.class), REQUEST_GUIDE);
					overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
					setFirstStarted();
				} else {
					startActivity(new Intent(WelcomActivity.this, HomeActivity.class));
					WelcomActivity.this.finish();
				}
			}
		}, START_SLEEP_TIME);
		
		startService(new Intent(this, DownLoadImageService.class));
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}

	/**
	 * 判断是否有启动页图片
	 * @return
	 */
	private Bitmap hasStartImg() {
		String imagePath = PreferencesUtils.getString(KeyUtils.APP_START_IMAGE, null);
		if (imagePath != null) {
			File f = new File(imagePath);
			if (f.exists()) {
				Bitmap bit = BitmapFactory.decodeFile(imagePath);
				return bit;
			}
		}
		return null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_GUIDE:
			startActivity(new Intent(this, HomeActivity.class));
			overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
			finish();
			break;
		default:
			break;
		}
	}

	/** 获取启动次数 */
	public int getStartCount() {
		return PreferencesUtils.getInt(StartCount, 0);
	}

	/** 是否首次启动 */
	public boolean isFirstStart() {
		return getStartCount() <= 0;
	}

	/** 设置第一次已启动 */
	public void setFirstStarted() {
		PreferencesUtils.setInt(StartCount, getStartCount() + 1);
	}
}
