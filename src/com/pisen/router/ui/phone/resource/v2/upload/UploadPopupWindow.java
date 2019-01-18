package com.pisen.router.ui.phone.resource.v2.upload;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;

import com.pisen.router.R;
import com.pisen.router.core.camera.CameraActivity;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.ui.base.BasePopupWindow;

public class UploadPopupWindow extends BasePopupWindow implements OnClickListener {

	public interface OnUploadItemClickListener {
		void onUploadItemClick(FileType type);
	}

	private Activity activity;

	private OnUploadItemClickListener uploadItemClickListener;

	public UploadPopupWindow(Activity activity) {
		super(activity, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		this.activity = activity;

		setContentView(activity, R.layout.resource_home_upload);

		findViewById(R.id.btnClose).setOnClickListener(this);
		findViewById(R.id.btnCamera).setOnClickListener(this);
		findViewById(R.id.btnAll).setOnClickListener(this);
		findViewById(R.id.btnImage).setOnClickListener(this);
		findViewById(R.id.btnVideo).setOnClickListener(this);
		findViewById(R.id.btnMusic).setOnClickListener(this);
		findViewById(R.id.btnDocument).setOnClickListener(this);
	}

	public void setOnUploadItemClickListener(OnUploadItemClickListener listener) {
		this.uploadItemClickListener = listener;
	}

	@Override
	public void onClick(View v) {
		FileType fileType = null;
		switch (v.getId()) {
		case R.id.btnClose:
			dismiss();
			break;
		case R.id.btnCamera:
			dismiss();
			activity.startActivity(new Intent(activity, CameraActivity.class));
			break;
		case R.id.btnImage:
			fileType = FileType.Image;
			break;
		case R.id.btnVideo:
			fileType = FileType.Video;
			break;
		case R.id.btnMusic:
			fileType = FileType.Audio;
			break;
		case R.id.btnDocument:
			fileType = FileType.Document;
			break;
		case R.id.btnAll:
		default:
			fileType = FileType.All;
			break;
		}

		if (fileType != null) {
			dismiss();
			if (uploadItemClickListener != null) {
				uploadItemClickListener.onUploadItemClick(fileType);
			}
			// activity.startActivity(new Intent(activity,
			// RootUploadActivity.class).setData(Uri.parse(fileType.name())));
		}
	}
}
