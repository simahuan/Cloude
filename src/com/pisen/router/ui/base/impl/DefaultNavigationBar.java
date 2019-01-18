package com.pisen.router.ui.base.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.ui.base.INavigationBar;

public class DefaultNavigationBar extends FrameLayout implements INavigationBar {

	private Button btnLeft;
	private Button btnRight;
	private TextView txtTitle;

	public DefaultNavigationBar(Context context) {
		this(context, null);
	}

	public DefaultNavigationBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.cloud_navibar_default, this);
		btnLeft = (Button) findViewById(R.id.btnLeft);
		btnRight = (Button) findViewById(R.id.btnRight);
		txtTitle = (TextView) findViewById(R.id.txtTitle);
	}

	@Override
	public DefaultNavigationBar getView() {
		return this;
	}

	@Override
	public CharSequence getTitle() {
		return null;
	}

	@Override
	public void setTitle(int resid) {
		setTitle(getResources().getText(resid));
	}

	@Override
	public void setTitle(CharSequence text) {
		txtTitle.setText(text);
	}

	public TextView getTitleView() {
		return txtTitle;
	}

	public Button getLeftButton() {
		return btnLeft;
	}

	public Button getRightButton() {
		return btnRight;
	}

	public void setLeftButton(CharSequence text, OnClickListener l) {
		btnLeft.setText(text);
		if (l != null) {
			btnLeft.setOnClickListener(l);
		}
	}

	public void setRightButton(CharSequence text, OnClickListener l) {
		btnRight.setVisibility(View.VISIBLE);
		btnRight.setText(text);
		if (l != null) {
			btnRight.setOnClickListener(l);
		}
	}

	@Override
	public void setLeftButton(CharSequence text, int iconResId, OnClickListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRightButton(CharSequence text, int iconResId, OnClickListener l) {
		// TODO Auto-generated method stub
		
	}

}
