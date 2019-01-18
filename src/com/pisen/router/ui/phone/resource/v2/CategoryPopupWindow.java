package com.pisen.router.ui.phone.resource.v2;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager.LayoutParams;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.ui.base.BasePopupWindow;

public class CategoryPopupWindow extends BasePopupWindow implements View.OnClickListener {

	public interface OnCategoryItemClickCallback {
		void onCategoryItemClick(FileType type);
	}

	private OnCategoryItemClickCallback callback;

	public CategoryPopupWindow(Activity activity) {
		super(activity);
		// setFocusable(true);

		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.MATCH_PARENT);
		// setAnimationStyle(R.style.AppPopupWindow);
		setContentView(activity, R.layout.resource_home_category);

		findViewById(R.id.dismissView).setOnClickListener(this);
		findViewById(R.id.btnAll).setOnClickListener(this);
		findViewById(R.id.btnImage).setOnClickListener(this);
		findViewById(R.id.btnVideo).setOnClickListener(this);
		findViewById(R.id.btnMusic).setOnClickListener(this);
		findViewById(R.id.btnDocument).setOnClickListener(this);
		findViewById(R.id.btnApk).setOnClickListener(this);
	}

	public void setOnCategoryItemClickCallback(OnCategoryItemClickCallback callback) {
		this.callback = callback;
	}

	@Override
	public void onClick(View v) {
		dismiss();
		switch (v.getId()) {
		case R.id.dismissView:
			break;
		case R.id.btnAll:
			setItemCategoryClick(FileType.All);
			break;
		case R.id.btnImage:
			setItemCategoryClick(FileType.Image);
			break;
		case R.id.btnVideo:
			setItemCategoryClick(FileType.Video);
			break;
		case R.id.btnMusic:
			setItemCategoryClick(FileType.Audio);
			break;
		case R.id.btnDocument:
			setItemCategoryClick(FileType.Document);
			break;
		case R.id.btnApk:
			setItemCategoryClick(FileType.Apk);
			break;
		default:
			break;
		}

	}

	private void setItemCategoryClick(FileType type) {
		if (callback != null) {
			callback.onCategoryItemClick(type);
		}
	}
}
