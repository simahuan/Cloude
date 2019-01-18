package com.pisen.router.ui.phone.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.pisen.router.R;
import com.pisen.router.common.view.ProgressWebView;
import com.pisen.router.config.HttpKeys;
import com.pisen.router.ui.base.FragmentActivity;
import com.pisen.router.ui.base.impl.DefaultNavigationBar;
import com.pisen.router.ui.phone.HomeFragment;

/**
 * 品胜商城
 * @author  mahuan
 * @version 1.0 2015年5月20日 上午10:02:20
 */
public class HuiYuanDiFragment extends HomeFragment {
	private String mUrl = HttpKeys.PISENEASY_URL;
	private static final int LOAD_TIMEOUT = 10 * 1000;// 10s
	private FragmentActivity activity;
	private View layoutView;
	private Handler mHandler;
	private ProgressWebView mWebView;
	private RelativeLayout mErrorLayout;
	private Button mBtnRefresh;
	private boolean isTimeout;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (FragmentActivity)activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		activity.showLoadingProgressDialog();
		layoutView = inflater.inflate(R.layout.cloud_settings_huiyuandi, (ViewGroup)null);
		DefaultNavigationBar naviBar = (DefaultNavigationBar) layoutView.findViewById(R.id.navibar);
		naviBar.setTitle("品胜商城");
		naviBar.setLeftButton("返回", new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onBackPressed();
			}
		});
		mWebView = (ProgressWebView) layoutView.findViewById(R.id.wv_main_huiyuandi_webview);
		mErrorLayout = (RelativeLayout) layoutView.findViewById(R.id.errorLayout);
		mBtnRefresh = (Button) layoutView.findViewById(R.id.btnRefresh);
		mBtnRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshData();
			}
		});
		mHandler = new Handler();
		connection();
		return layoutView;
	}
	
	@Override
	public void onStop() {
		mHandler.removeCallbacks(timeoutTask);
		super.onStop();
	}

	Runnable timeoutTask = new Runnable() {
		@Override
		public void run() {
			timeout();
		}
	};
	
	/**
	 * 连接本地的html
	 */
	@SuppressLint("SetJavaScriptEnabled")
	private void connection() {
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.loadUrl(mUrl);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url == null || url.trim().equals("") || url.length() < 5) {
					return false;
				}
				mUrl = url;
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				timeout();
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				mHandler.postDelayed(timeoutTask, LOAD_TIMEOUT);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (!isTimeout) {
					loadSuccess();
				}
			}
		});
	}

	private void refreshData() {
		activity.showLoadingProgressDialog();
		isTimeout = false;
		mWebView.setVisibility(View.GONE);
		mErrorLayout.setVisibility(View.GONE);
		connection();
	}

	private void timeout() {
		mHandler.removeCallbacks(timeoutTask);
		mWebView.stopLoading();
		mWebView.setVisibility(View.GONE);
		mErrorLayout.setVisibility(View.VISIBLE);
		isTimeout = true;
		activity.dismissProgressDialog();
	}

	private void loadSuccess() {
		activity.dismissProgressDialog();
		mHandler.removeCallbacks(timeoutTask);
		mErrorLayout.setVisibility(View.GONE);
		mWebView.setVisibility(View.VISIBLE);
	}

}
