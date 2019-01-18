package com.pisen.router.ui.base;

import android.app.Dialog;
import android.studio.app.ActivitySupport;
import android.view.View;
import android.view.ViewGroup;

import com.pisen.router.common.dialog.LoadingDialog;
import com.pisen.router.ui.phone.resource.v2.NavigationBar;

public abstract class CloudActivity extends ActivitySupport {

	private INavigationBar navigationBar;

	@Override
	public Dialog newLoadingDialog() {
		return new LoadingDialog(this);
	}

	@Override
	public void setContentView(int layoutResID) {
		getWindow().setContentView(layoutResID);
		initNavigationBar();
		//ButterKnife.inject(this);
	}

	@Override
	public void setContentView(View view) {
		getWindow().setContentView(view);
		initNavigationBar();
		//ButterKnife.inject(this);
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		getWindow().setContentView(view, params);
		initNavigationBar();
		//ButterKnife.inject(this);
	}

	@Override
	public void addContentView(View view, ViewGroup.LayoutParams params) {
		getWindow().addContentView(view, params);
		initNavigationBar();
		//ButterKnife.inject(this);
	}

	private void initNavigationBar() {
		if (navigationBar == null) {
			navigationBar = newNavigationBar();
		}
	}

	protected INavigationBar newNavigationBar() {
		return new NavigationBar(this);
	}

	public INavigationBar getNavigationBar() {
		return navigationBar;
	}

}
