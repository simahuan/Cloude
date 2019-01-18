package com.pisen.router.core.image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.studio.os.LogCat;
import android.studio.util.URLUtils;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.pisen.router.R;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.ResourceInfo.RSource;
import com.pisen.router.core.filemanager.SardineCacheResource;
import com.pisen.router.core.filemanager.transfer.TransferManagerV2;
import com.pisen.router.core.filemanager.transfer.TransferServiceV2;
import com.pisen.router.ui.base.CloudActivity;
import com.pisen.router.ui.base.INavigationBar;

/**
 * 图片详情，根据各种转换的URI异步加载图片(需要确定调用本activity时传递进来的参数)
 */
public class ImageViewer extends CloudActivity implements OnClickListener{
	private static final String STATE_POSITION = "STATE_POSITION";
	public static final String EXTRA_IMAGE_INDEX = "image_index";
	public static final String EXTRA_IMAGE_URLS = "image_urls";
	private HackyViewPager mPager;
	private static List<ResourceInfo> playlist;
	private static ResourceInfo curInfo;
	private Uri[] urls;
	private int itemSelectedIndex;
	private static INavigationBar navigationBar;
	public  static  LinearLayout   llayoutControl;
	private int currentItemIndex = 0;
	private static ImageViewer instance;
	
	ImagePagerAdapter mAdapter;
	TransferManagerV2 transManger;
	IResource  sardineManager;
	List<ResourceInfo> itemCheckedList = new ArrayList<ResourceInfo>();
	
	public static ImageViewer getInstance(){
		if (null == instance){
			instance = new ImageViewer();
		}
		return instance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.imagebrower_detail_pager);
		initImages();
		navigationBar = getNavigationBar();
		navigationBar.setBackgroundResource(R.drawable.video_topshadow);		
		navigationBar.setLeftButton(null, R.drawable.menu_ic_back, new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		llayoutControl = (LinearLayout) this.findViewById(R.id.llayoutControl);
		findViewById(R.id.btnDownload).setOnClickListener(this);
		findViewById(R.id.btnDelete).setOnClickListener(this);
		
		if (curInfo.source == RSource.Local) { llayoutControl.setVisibility(View.GONE); }
		sardineManager = new SardineCacheResource();
		bindService();
		
		mPager = (HackyViewPager) findViewById(R.id.pager);
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), urls);
		mPager.setAdapter(mAdapter);
		navigationBar.setTitle(String.format("%1$d/%2$d", 1, mPager.getAdapter().getCount()));

		// 更新下标
		mPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			@Override
			public void onPageSelected(int arg0) {
				currentItemIndex = arg0;
				String text = String.format("%1$d/%2$d", arg0 + 1, mPager.getAdapter().getCount());
				navigationBar.setTitle(text);
			}
		});
		if (savedInstanceState != null) {
			itemSelectedIndex = savedInstanceState.getInt(STATE_POSITION);
		}
		mPager.setCurrentItem(itemSelectedIndex);
	}

	private int getCurrentItemIndex(){
		return currentItemIndex;
	}
	
	public static void start(Context context, ResourceInfo info, List<ResourceInfo> playlist) {
		ImageViewer.playlist = playlist;
		curInfo = info;
		context.startActivity(new Intent(context, ImageViewer.class));
	}

	private void initImages() {
		if (playlist != null && curInfo != null && !playlist.isEmpty()) {
			urls = getImageUrls();
			itemSelectedIndex = getImageIndex();
		}
	}

	private ResourceInfo getResourceforIndex(int location){
		return ImageViewer.playlist.get(location);
	}
	
	private Uri[] getImageUrls() {
		List<Uri> list = new ArrayList<Uri>();
		for (int i = 0; i < playlist.size(); i++) {
			list.add(resourceInfoToUri(playlist.get(i)));
		}
		return list.toArray(new Uri[0]);
	}

	private int getImageIndex() {
		for (int i = 0; i < playlist.size(); i++) {
			if (playlist.get(i).path.equals(curInfo.path)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		playlist = null;
		curInfo = null;
		unbindService();
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, mPager.getCurrentItem());
	}

	private class ImagePagerAdapter extends FragmentStatePagerAdapter {

		public Uri[] fileList;

		public ImagePagerAdapter(FragmentManager fm, Uri[] fileList) {
			super(fm);
			this.fileList = fileList;
		}

		public void setResourceAdapter(Uri[] fileList){
			this.fileList = fileList;
		}
		
		@Override
		public int getCount() {
			return fileList == null ? 0 : fileList.length;
		}

		@Override
		public Fragment getItem(int position) {
			Uri url = fileList[position];
			return ImageDetailFragment.newInstance(url);
		}
	}

	/**
	 * 包装资源路径到UniversalImageLoader使用的路径(含本地资源与云端资源处理，图片浏览器使用)
	 * @param ri
	 * @return
	 */
	public static Uri resourceInfoToUri(ResourceInfo ri) {
		if (ri.source == RSource.Local) {
			return Uri.fromFile(new File(ri.path)); //Scheme.FILE.wrap(ri.path);
		} else {
			return Uri.parse(URLUtils.encodeURL(ri.path));
		}
	}

	@Override
	public void onClick(View v) {
		itemCheckedList.clear();
		switch (v.getId()) {
		case R.id.btnDownload:
			itemCheckedList.add(curInfo);
			downloadPicture(itemCheckedList);
			break;
		case R.id.btnDelete:
			LogCat.e("currentItemIndex = " + getCurrentItemIndex());
			curInfo = getResourceforIndex(getCurrentItemIndex());
			LogCat.e("curInfo.path = " + getResourceforIndex(getCurrentItemIndex()).path);
			itemCheckedList.add(curInfo);
			deletePicture(itemCheckedList); // 远程图库要刷新，当前浏览集要删除
			break;
		}
	}
	
	public void layoutControlToggle() {
		if (null != llayoutControl && null != navigationBar && curInfo.source == RSource.Remote) {
			if (llayoutControl.getVisibility() == View.GONE) {
				llayoutControl.setVisibility(View.VISIBLE);
				navigationBar.getView().setVisibility(View.VISIBLE);
			} else {
				llayoutControl.setVisibility(View.GONE);
				navigationBar.getView().setVisibility(View.GONE);
			}
		}
	}
	
	/**
	 * @des 下载图片
	 */
	private void downloadPicture(final List<ResourceInfo> itemCheckedList) {
		AsyncTaskUtils.execute(new InBackgroundCallback<Boolean>() {
			@Override
			public Boolean doInBackground() {
				try {
					transManger.addDownloadTask(itemCheckedList);
					return true;
				} catch (Exception e) {
					return false;
				}
			}
			@Override
			public void onPostExecute(Boolean result) {
				UIHelper.showToast(ImageViewer.this, "已为你添加到下载列表");
			}
		});
	}
	
	
	/**　
	 * @des　删除图片
	 */
	private void deletePicture(final List<ResourceInfo> itemCheckedList) {
		ConfirmDialog.show(this, "确定要删除选中项吗?", "删除", "确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AsyncTaskUtils.execute(new InBackgroundCallback<Boolean>() {
					@Override
					public Boolean doInBackground() {
						try {
							for (ResourceInfo info : itemCheckedList) {
								LogCat.e("=====deleteResource==="+info.path);
								sardineManager.delete(info.path);
							}
							return true;
						} catch (Exception e) {
							return false;
						}
					}

					@Override
					public void onPostExecute(Boolean result) {
						if (result) {
							UIHelper.showToast(ImageViewer.this, "删除成功");
							ImageViewer.playlist.remove(getCurrentItemIndex());
							mAdapter.setResourceAdapter(getImageUrls());
//							mAdapter.destroyItem(null, getCurrentItemIndex(), mAdapter.getItem(getCurrentItemIndex()));
							mAdapter.notifyDataSetChanged();
						} else {
							UIHelper.showToast(ImageViewer.this, "删除失败");
						}
					}
				});
			}
		}, "取消", null);
	}
	
	private ServiceConnection conn;
	private void bindService() {
		conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {}
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				transManger = ((TransferServiceV2.TransferBinder) service).getTransferManager();
			}
		};
		Intent in = new Intent(this, TransferServiceV2.class);
		this.bindService(in, conn, Service.BIND_AUTO_CREATE);
	}
	
	private void unbindService() {
		this.unbindService(conn);
	}
}