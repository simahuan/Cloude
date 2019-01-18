package com.pisen.router.ui.phone.device;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.studio.os.NetUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.pisen.router.R;
import com.pisen.router.ui.base.NavigationBarActivity;

/**
 * 150M路由管理界面
 * @author Liuhc
 * @version 1.0 2015年5月28日 下午7:05:50
 */
public class RouterManagerActivity extends NavigationBarActivity {

	// 超时处理
	private static final int LOAD_TIMEOUT = 10 * 1000;
	private Handler mHandler;
	
	private WebView mWebView;
	private ProgressBar mProgressBar;
//	private RelativeLayout mErrorLayout;
//	private Button mBtnRefresh;
	private boolean isTimeout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_device_router_mgr);
		setTitle("路由管理");
		
		mWebView = (WebView) findViewById(R.id.webView);
		mProgressBar = (ProgressBar) findViewById(R.id.proWait);
		
//		mErrorLayout = (RelativeLayout) findViewById(R.id.errorLayout);
//		mBtnRefresh = (Button) findViewById(R.id.btnRefresh);
//		mBtnRefresh.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				refreshData();
//			}
//		});
		mHandler = new Handler();
		connection();
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
	private void connection() {
		String url = "http://"+NetUtils.getGateway(RouterManagerActivity.this);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.loadUrl(url);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				timeout();
			}

			@Override
			public void onPageStarted(final WebView view, String url, Bitmap favicon) {
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

	public void backOnclick(View v) {
		this.finish();
	}

	private void refreshData() {
		isTimeout = false;
		mWebView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
//		mErrorLayout.setVisibility(View.GONE);
		connection();
	}

	private void timeout() {
		mHandler.removeCallbacks(timeoutTask);
		mWebView.stopLoading();
		mWebView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.GONE);
//		mErrorLayout.setVisibility(View.VISIBLE);
		isTimeout = true;
	}

	private void loadSuccess() {
		mHandler.removeCallbacks(timeoutTask);
		mProgressBar.setVisibility(View.GONE);
//		mErrorLayout.setVisibility(View.GONE);
		mWebView.setVisibility(View.VISIBLE);
	}
}
