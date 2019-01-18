package com.pisen.router.ui.phone.resource.v2;

import java.io.File;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.studio.os.LogCat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.dialog.InputDialog;
import com.pisen.router.common.dialog.LoadingDialog;
import com.pisen.router.common.dialog.ProgressDialog;
import com.pisen.router.common.utils.FileUtils;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.common.view.GuideStepPopuWindow;
import com.pisen.router.config.AppConfig;
import com.pisen.router.config.Config;
import com.pisen.router.config.WifiConfig;
import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.SardineCacheResource;
import com.pisen.router.core.filemanager.async.MoveAsyncTask;
import com.pisen.router.core.filemanager.async.ResourceAsyncTask.ResourceItemCallback;
import com.pisen.router.core.filemanager.async.ResourceAsyncTask.ResourceResult;
import com.pisen.router.core.filemanager.transfer.DownloadSardineTask;
import com.pisen.router.core.filemanager.transfer.TransferManagerV2;
import com.pisen.router.core.filemanager.transfer.TransferServiceV2;
import com.pisen.router.core.filemanager.transfer.TransferStatus;
import com.pisen.router.core.filemanager.transfer.TransferTask;
import com.pisen.router.core.filemanager.transfer.UploadSardineTask;
import com.pisen.router.core.monitor.DiskMonitor;
import com.pisen.router.core.monitor.DiskMonitor.DiskEntity;
import com.pisen.router.core.monitor.DiskMonitor.OnDiskChangedListener;
import com.pisen.router.core.monitor.WifiMonitor;
import com.pisen.router.core.monitor.WifiMonitor.WifiStateCallback;
import com.pisen.router.ui.HomeActivity;
import com.pisen.router.ui.base.INavigationBar;
import com.pisen.router.ui.phone.resource.ResourceFragment;
import com.pisen.router.ui.phone.resource.transfer.TransferRecordActivity;
import com.pisen.router.ui.phone.resource.v2.CategoryPopupWindow.OnCategoryItemClickCallback;
import com.pisen.router.ui.phone.resource.v2.ChoiceNavigationBar.OnChoiceItemClickListener;
import com.pisen.router.ui.phone.resource.v2.ToolbarPopupWindow.OnToolbarItemClickCallback;
import com.pisen.router.ui.phone.resource.v2.panel.ISelectionActionBar;
import com.pisen.router.ui.phone.resource.v2.panel.ResourceManager;
import com.pisen.router.ui.phone.resource.v2.upload.RootUploadActivity;
import com.pisen.router.ui.phone.resource.v2.upload.UploadPopupWindow;
import com.pisen.router.ui.phone.resource.v2.upload.UploadPopupWindow.OnUploadItemClickListener;

/**
 * 私有云
 * 
 * @author yangyp
 */
public class RouterFragment extends ResourceFragment implements WifiStateCallback, OnDiskChangedListener, OnCategoryItemClickCallback, OnItemClickListener,
		OnToolbarItemClickCallback, OnUploadItemClickListener, OnChoiceItemClickListener {

	private ChoiceNavigationBar navigationBar;
	private ToolbarPopupWindow toolbarPopupWindow;

	private CategoryPopupWindow categoryPopupWindow;
	private UploadPopupWindow uploadPopupWindow;

	private DiskMonitor diskMonitor;
	private WifiMonitor wifiMonitor;

	private RouterShopPanel shopLayout;
	private LinearLayout diskLayout;
	private ResourceManager resourcePanel;
	private RouterFileFragment lastFragment;
	private RadioGroup tabHost;
	
	private ListView listContent;
	private RouterDiskAdapter diskAdapter;
	private TextView msgToast;
	private FileType type;

	public IResource sardineManager;
	private ISelectionActionBar<ResourceInfo> selectionActionBar;

	TransferManagerV2 transManger;
	private static final String KEY_FIRST_INIT = "is_first_init";

	@Override
	public View onInjectCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.resource_home, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		resourcePanel = new ResourceManager(this);
		shopLayout = new RouterShopPanel(this, getActivity());
		diskLayout = (LinearLayout) findViewById(R.id.diskLayout);
		msgToast = (TextView) findViewById(R.id.msgToast);
		listContent = (ListView) findViewById(R.id.listContent);

		diskMonitor = DiskMonitor.getInstance();
		diskMonitor.registerObserver(this);
		wifiMonitor = WifiMonitor.getInstance();
		wifiMonitor.registerObserver(this);
		sardineManager = new SardineCacheResource();

		diskAdapter = new RouterDiskAdapter(getActivity());
		listContent.setAdapter(diskAdapter);
		listContent.setOnItemClickListener(this);
		listContent.setFocusable(false);

		if (wifiMonitor.isPisenWifiConnected()) {
			if (diskMonitor.isScannerFinished()) {
				showDiskView();
				loadDiskData();
			} else {
				msgToast.setVisibility(View.VISIBLE);
				msgToast.setText("正在扫描磁盘，请稍候...");
			}
		} else {
			showShopView();
		}

		bindService();
		registerTransferChangeBroadcast();
	}

	private ServiceConnection conn;

	private void bindService() {
		conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				transManger = ((TransferServiceV2.TransferBinder) service).getTransferManager();
			}
		};
		Intent in = new Intent(getActivity(), TransferServiceV2.class);
		getActivity().bindService(in, conn, Service.BIND_AUTO_CREATE);
	}

	private void unbindService() {
		getActivity().unbindService(conn);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		try {
			unbindService();
			if (transferChangeBroadcast != null) {
				getActivity().unregisterReceiver(transferChangeBroadcast);
			}
			diskMonitor.unregisterObserver(this);
			wifiMonitor.unregisterObserver(this);
		} catch (Exception e) {
		}
	}

	@Override
	public ChoiceNavigationBar getNavigationBar() {
		return navigationBar;
	}

	@Override
	protected INavigationBar newNavigationBar() {
		navigationBar = new ChoiceNavigationBar(this);
		navigationBar.setOnItemClickListener(this);
		updateNavigationBarToDefault();
		setNavigationBarCategory(false);
		return navigationBar;
	}

	@Override
	public void onNavigationBarItemCheckAll(boolean checked) {
		selectionActionBar.onActionBarItemCheckAll(checked);
		updateActionBarChanged();
	}

	@Override
	public void onNavigationBarItemCheckCancel() {
		selectionActionBar.onActionBarItemCheckCancel();
		hideSelectionMenu();
	}

	/**
	 * @des 更新导航栏TO Default
	 */
	public void updateNavigationBarToDefault() {
		String title = lastFragment == null ? "私有云" : Uri.parse(lastFragment.getParentPath()).getLastPathSegment();
		navigationBar.setTitle(title);
		navigationBar.setLeftButton(null, R.drawable.route, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((HomeActivity) getActivity()).toggleMenu();
			}
		});
		navigationBar.getTitleView().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showCategoryPopupWindow();
			}
		});

		navigationBar.setRightButton(null, R.drawable.upload, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (uploadPopupWindow == null) {
					uploadPopupWindow = new UploadPopupWindow(getActivity());
					uploadPopupWindow.setOnUploadItemClickListener(RouterFragment.this);
					uploadPopupWindow.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss() {
							// navigationBar.getTitleView().setCompoundDrawablesWithIntrinsicBounds(0,
							// 0, R.drawable.pulldown, 0);
						}
					});
				}

				if (!uploadPopupWindow.isShowing()) {
					uploadPopupWindow.showAsDropDown(navigationBar.getView(), 0, -navigationBar.getView().getHeight());
					// navigationBar.getTitleView().setCompoundDrawablesWithIntrinsicBounds(0,
					// 0, R.drawable.personalcloudup, 0);
				}
			}
		});

		refreshTransferStatus();
	}

	/**
	 * 显示分类popwindow
	 */
	private void showCategoryPopupWindow() {
		if (categoryPopupWindow == null) {
			categoryPopupWindow = new CategoryPopupWindow(getActivity());
			categoryPopupWindow.setOnCategoryItemClickCallback(RouterFragment.this);
			categoryPopupWindow.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					navigationBar.getTitleView().setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pulldown, 0);
				}
			});
		}

		if (!categoryPopupWindow.isShowing()) {
			categoryPopupWindow.showAsDropDown(navigationBar.getView());
			navigationBar.getTitleView().setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.personalcloudup, 0);
		}
	}

	/**
	 * 设置是否启用分类导航
	 * 
	 * @param enabled
	 */
	public void setNavigationBarCategory(boolean enabled) {
		navigationBar.getTitleView().setClickable(enabled);
		navigationBar.getTitleView().setCompoundDrawablesWithIntrinsicBounds(0, 0, enabled ? R.drawable.pulldown : 0, 0);
		navigationBar.getRightButton().setVisibility(enabled ? View.VISIBLE : View.GONE);
		if (!enabled) {
			navigationBar.setTitle("私有云");
		}
	}

	/**
	 * 文件上传至路由操作类　
	 */
	@Override
	public void onUploadItemClick(FileType type) {
		Intent intent = new Intent(getActivity(), RootUploadActivity.class);
		intent.setDataAndType(Uri.parse(lastFragment.getParentPath()), type.name());
		startActivity(intent);
	}

	private void loadDiskData() {
		msgToast.setVisibility(View.GONE);
		DiskEntity[] disList = diskMonitor.getNetDisk();
		diskAdapter.setData(disList);
		diskAdapter.notifyDataSetChanged();
		if (disList.length == 0) {
			msgToast.setVisibility(View.VISIBLE);
			msgToast.setText("未检测到您的存储设备哦~");
			msgToast.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.usb, 0, 0);
			Config.setHasStorage(false);
		}else{
			Config.setHasStorage(true);
		}
	}

	/**
	 * 显示磁盘界面
	 */
	private void showDiskView() {
		shopLayout.hide();
		diskLayout.setVisibility(View.VISIBLE);
	}

	/**
	 * 显示购买界面
	 */
	private void showShopView() {
		diskLayout.setVisibility(View.GONE);
		shopLayout.show();
	}

	@Override
	public void onDiskChanged() {
		loadDiskData();
	}

	@Override
	public void onConnected(WifiConfig config) {
		if (config.isPisenWifi()) {
			showDiskView();
		} else {
			showShopView();
		}
	}

	@Override
	public void onDisconnected(WifiConfig config) {
		showShopView();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DiskEntity disk = diskAdapter.getItem(position);
		AppConfig.setCurrentDiskPath(disk.path);
		startChildFragment(new RouterFileFragment(this, disk.path));
		setNavigationBarCategory(true);

		// 第一次初始化，默认显示分类视图
//		if (PreferencesUtils.getBoolean(KEY_FIRST_INIT, true)) {
//			PreferencesUtils.setBoolean(KEY_FIRST_INIT, false);
//			showCategoryPopupWindow(); //首弹
//		}
//		initGuideStpeView();
	}

    private GuideStepPopuWindow guideStepPopuWindow;
    //使用指引蒙板view
    private static View guideStepView;
    /**
     * @describtion 蒙板向导
     */
    private void GuideStepPopuView() {
    	final Window window = getHomeActivity().getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = 0.5f;
        window.setAttributes(lp);//改变背景颜色
        //实例化PopupWindow
        guideStepPopuWindow = new GuideStepPopuWindow(getHomeActivity());
        //显示窗体，设置显示位置为activity的底部
                /*第一个参数指定PopupWindow的锚点view，即依附在哪个view上。
                	第二个参数指定起始点为parent的右下角，第三个参数设置以parent的右下角为原点，向左、上各偏移10像素。 //将PopupWindow作为anchor的下拉窗口显示。即在anchor的左下角显示*/
        guideStepPopuWindow.showAtLocation(RouterFragment.this.findViewById(R.id.diskLayout), Gravity.CENTER | Gravity.CENTER, 200, 200);//设置layout在PopupWindow中显示的位置
        guideStepPopuWindow.update();
        //当弹出界面销毁时恢复背景颜色
        guideStepPopuWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.alpha = 1f;//恢复背景颜色
                window.setAttributes(lp);
            }
        });
    }

    //延迟加载
    private void initGuideStpeView() {
        guideStepView = findViewById(R.id.diskLayout);
        final Handler mHandler = new Handler();
        //延迟时间加载view
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (guideStepView.getWidth() > 0) {
                    GuideStepPopuView();
                } else {
                    mHandler.postDelayed(this, 100);
                }
            }
        }, 1000);
    }
	
	
	
	/**
	 * 顶部菜单匹配
	 */
	@Override
	public void onCategoryItemClick(FileType type) {
		this.type = type;
		if (type == FileType.All) {
			updateNavigationBarToDefault();
			resourcePanel.removeCategoryPanel();
			selectionActionBar = lastFragment;
			return;
		}

		String categoryTitle = getCategoryTitle(type);
		updateNavigationBar(categoryTitle);
		selectionActionBar = resourcePanel.switchPanel(AppConfig.getCurrentDiskPath(), type); //拿到交换面板
		refreshTransferStatus();
	}

	private String getCategoryTitle(FileType type) {
		switch (type) {
		case Video:
			return "视频";
		case Image:
			return "图片";
		case Audio:
			return "音乐";
		case Document:
			return "文档";
		case Apk:
			return "应用";
		default:
			return "全部";
		}
	}

	private void updateNavigationBar(String title) {
		navigationBar.setTitle(title);
		navigationBar.setRightButton(null, R.drawable.down_transmission, new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), TransferRecordActivity.class));
			}
		});
	}

	private boolean childPopBackStack() {
		FragmentManager fm = getChildFragmentManager();
		int backCount = fm.getBackStackEntryCount();
		if (backCount > 0) {
			fm.popBackStack();
			if (backCount <= 1) {
				lastFragment = null;
				selectionActionBar = null;
				setNavigationBarCategory(false);
				listContent.setVisibility(View.VISIBLE);
			} else {
				lastFragment = (RouterFileFragment) fm.getFragments().get(backCount - 2);
				selectionActionBar = lastFragment;
				updateNavigationBarToDefault();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			// 如果导航是选中状态，那么关闭选择导航
			if (navigationBar.isShowChoiceBar()) {
				hideSelectionMenu();
				selectionActionBar.onActionBarItemCheckCancel();
				return true;
			}

			// 如果是分类，那么先关闭分类
			if (resourcePanel.isCategoryPanel()) {
				resourcePanel.removeCategoryPanel();
				type = null;
				updateNavigationBarToDefault();
				selectionActionBar = lastFragment;
				return true;
			}

			// 如果是RouterFileFragment，那么事件传下去
			if (selectionActionBar instanceof RouterFileFragment) {
				if (((RouterFileFragment) selectionActionBar).onKeyDown(keyCode, event)) {
					return true;
				}
			}

			if (childPopBackStack()) {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void startChildFragment(RouterFileFragment fragment) {
		this.lastFragment = fragment;
		this.selectionActionBar = fragment;
		listContent.setVisibility(View.GONE);
		getChildFragmentManager().beginTransaction().add(R.id.listfileLayout, fragment).addToBackStack(null).commit();
	}

	/**
	 * @des  显示底部操作菜单　
	 */
	public void showSelectionMenu() {
		navigationBar.showChoiceBar();
		if (toolbarPopupWindow == null) {
			toolbarPopupWindow = new ToolbarPopupWindow(getActivity());
			toolbarPopupWindow.setOnToolbarItemClickCallback(this);
		}

		if (!toolbarPopupWindow.isShowing()) {
			// toolbarPopupWindow.showAsDropDown(getView());
			toolbarPopupWindow.showAtLocation(diskLayout, Gravity.BOTTOM, 0, 0);
			if (tabHost != null) {
				tabHost.setVisibility(View.INVISIBLE);
			}
		}
		// 展示前刷新选中个数
		updateActionBarChanged();
		((HomeActivity)getActivity()).setSlidingMenuScrollable(false);
	}

	public void hideSelectionMenu() {
		navigationBar.setCheckedTextCount(0);
		navigationBar.showActionBar();
		if (toolbarPopupWindow != null) {
			toolbarPopupWindow.dismiss();
		}
		if (tabHost != null) {
			tabHost.setVisibility(View.VISIBLE);
		}
		((HomeActivity)getActivity()).setSlidingMenuScrollable(true);
	}

	public void updateActionBarChanged() {
		int itemCount = selectionActionBar.getItemAll().size();
		int itemSelectedCount = selectionActionBar.getCheckedItemAll().size();
		boolean checkAll = itemCount > 0 && itemCount == itemSelectedCount;

		navigationBar.setCheckedTextCount(itemSelectedCount);
		navigationBar.setCheckedChanged(checkAll);
	}

	@Override
	public void onToolbarItemClick(View v) {
		List<ResourceInfo> itemCheckedList = selectionActionBar.getCheckedItemAll();
		if (itemCheckedList.isEmpty()) {
			UIHelper.showToast(getActivity(), "请选择文件");
			return;
		}

		switch (v.getId()) {
		case R.id.btnDownload:
			downloadResource();
			break;
		case R.id.btnMove:
			moveResource();
			break;
		case R.id.btnRename:
			renameResource();
			break;
		case R.id.btnDelete:
			deleteResource();
			break;
		default:
			break;
		}
	}
	
	private LoadingDialog mLoadingDialog;
	protected void showLoading() {
		if(mLoadingDialog == null) {
			mLoadingDialog = new LoadingDialog(getActivity());
			mLoadingDialog.setTitle("请稍候...");
			mLoadingDialog.setCancelable(false);
		}
		
		mLoadingDialog.show();
	}

	protected void dismissLoading() {
		if(mLoadingDialog != null) {
			mLoadingDialog.dismiss();
		}
		
	}

	/**
	 * 下载文件
	 */
	private void downloadResource() {
		showLoading();
		AsyncTaskUtils.execute(new InBackgroundCallback<Boolean>() {
			@Override
			public Boolean doInBackground() {
				try {
					final List<ResourceInfo> itemCheckedList = selectionActionBar.getCheckedItemAll();
					transManger.addDownloadTask(itemCheckedList);
					return true;
				} catch (Exception e) {
					return false;
				}
			}

			@Override
			public void onPostExecute(Boolean result) {
				dismissLoading();
				UIHelper.showToast(getActivity(), "已为你添加到下载列表");
				selectionActionBar.onActionBarCompleted();
			}
		});
	}

	boolean isShowDialog = false;
	boolean isSuccess = true;

	/**
	 * 移动文件
	 */
	private void moveResource() {
		FileChooserActivity.start(getActivity(), "选择移动位置");
		isShowDialog = false;
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
		lbm.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (FileChooserActivity.Action_File_Chooser.equals(intent.getAction())) {
					if (isShowDialog) {
						return;
					}
					isShowDialog = true;
					isSuccess = true;
					final List<ResourceInfo> itemCheckedList = selectionActionBar.getCheckedItemAll();
					if (itemCheckedList == null || itemCheckedList.size() <= 0) {
						UIHelper.showToast(getActivity(), "请先选择移动文件");
						return;
					}

					final String path = intent.getStringExtra(FileChooserActivity.FileChooser_Path);
					for (ResourceInfo resourceInfo : itemCheckedList) {
						String s = FileUtils.getUpFileName(resourceInfo.path);
						if (path.equals(s)) {
							UIHelper.showToast(getActivity(), "不能移动到源路径");
							selectionActionBar.onActionBarCompleted();
							return;
						}
						if (resourceInfo.isDirectory) {
							if (path.contains(resourceInfo.path)) {
								UIHelper.showToast(getActivity(), "该目标路径已选,请选择其他路径");
								selectionActionBar.onActionBarCompleted();
								return;
							}
						}
					}

					final ProgressDialog moveDialog = new ProgressDialog(getActivity());
					getHomeActivity().showProgressDialog("移动准备...");
					final MoveAsyncTask moveTask = new MoveAsyncTask(sardineManager, itemCheckedList, path, selectionActionBar, null);
					moveTask.setItemCallback(new ResourceItemCallback() {
						@Override
						public void onItemCallback(ResourceResult result) {
							if (result == null || result.filename == null) {
								return;
							}
							
							if (!moveDialog.isShowing()) {
								getHomeActivity().dismissProgressDialog();
								moveDialog.show();
								moveDialog.setTitle("正在移动文件");
							}
							
							moveDialog.setFileName(result.filename);
							moveDialog.setProgressText((result.mCount * 100 / result.mTotal));
							moveDialog.setMaxText(result.mCount, result.mTotal);
							if (result.mCount == result.mTotal) {
								//LogCat.e("isSuccess:" + isSuccess);
								moveDialog.dismiss();
								if (isSuccess) {
									isSuccess = false;
									selectionActionBar.onActionBarCompleted();
									UIHelper.showToast(getActivity(), "移动成功");
								}
							} else if (result.getmStatus() == ResourceResult.UNKNOWN_ERROR) {
								moveTask.cancel(true);
								moveDialog.dismiss();
								selectionActionBar.onActionBarCompleted();
								UIHelper.showToast(getActivity(), "移动出错");
							}
						}
					});
					moveTask.execute();
				}
			}
		}, new IntentFilter(FileChooserActivity.Action_File_Chooser));
	}

	private void renameResource() {
		final List<ResourceInfo> itemCheckedList = selectionActionBar.getCheckedItemAll();
		if (itemCheckedList.size() != 1) {
			UIHelper.showToast(getActivity(), "重命名只能选择一个");
			return;
		}

		final ResourceInfo resource = itemCheckedList.get(0);
		// final String filename = FileUtils.getFileNameNoFormat(resource.name);
		// final String fileFormat = FileUtils.getFileFormat(resource.name);

		InputDialog renameDialog = new InputDialog(getActivity());
		renameDialog.setTitle("重命名");
		renameDialog.setInputText(resource.name);
		renameDialog.setOnClickListener(new InputDialog.SimpleClickListener() {
			@Override
			public void onOk(DialogInterface dialog, final String inputText) {
				if (TextUtils.isEmpty(inputText)) {
					UIHelper.showToast(getActivity(), "文件名称不能为空");
					return;
				}

				AsyncTaskUtils.execute(new InBackgroundCallback<Integer>() {
					@Override
					public Integer doInBackground() {
						try {
							if (lastFragment != null) {
								String destFile = resource.isDirectory ? (lastFragment.getParentPath() + inputText + File.separator) : (lastFragment
										.getParentPath() + inputText);
								if (sardineManager.exists(destFile)) {
									return -1;
								}
							}
							sardineManager.rename(resource.path, inputText);
							return 1;
						} catch (Exception e) {
							return 0;
						}
					}

					@Override
					public void onPostExecute(Integer result) {
						selectionActionBar.onActionBarCompleted();
						if (result == 1) {
							UIHelper.showToast(getActivity(), "重命名成功");
						} else if (result == -1) {
							UIHelper.showToast(getActivity(), "文件名已存在");
						} else {
							UIHelper.showToast(getActivity(), "重命名失败");
						}
					}
				});
			}
		});
		renameDialog.show();
	}

	private void deleteResource() {
		final List<ResourceInfo> itemCheckedList = selectionActionBar.getCheckedItemAll();
		ConfirmDialog.show(getActivity(), "确定要删除选中项吗?", "删除", "确定", new DialogInterface.OnClickListener() {
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
						selectionActionBar.onActionBarCompleted();
						if (result) {
							UIHelper.showToast(getActivity(), "删除成功");
						} else {
							UIHelper.showToast(getActivity(), "删除失败");
						}
					}
				});
			}
		}, "取消", null);
	}

	private BroadcastReceiver transferChangeBroadcast;

	/**
	 * @des 注册传输改变广播
	 */
	private void registerTransferChangeBroadcast() {
		transferChangeBroadcast = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (UploadSardineTask.ACTION_ADD.equals(intent.getAction())) {
					if (lastFragment != null) {
						refreshTransferStatus();
						return;
					}
				}

				TransferStatus status = (TransferStatus) intent.getSerializableExtra(TransferTask.EXTRA_TRANSFER_STATUS);
				if (TransferStatus.SUCCESS.equals(status) && lastFragment != null) {
					refreshTransferStatus();
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(UploadSardineTask.ACTION_ADD);
		filter.addAction(UploadSardineTask.ACTION_PROGRESS);
		filter.addAction(DownloadSardineTask.ACTION_PROGRESS);
		getActivity().registerReceiver(transferChangeBroadcast, filter);
	}

	/**
	 * 刷新传输状态
	 */
	private void refreshTransferStatus() {
		if (navigationBar != null) {
			if (type == null || type == FileType.All) {
				if (lastFragment != null) {
					navigationBar.setRightButton(null, R.drawable.upload, null);
					lastFragment.checkTransferTaskStatus();
				}
			} else {
				int bgid = lastFragment.checkTransferTaskStatus() ? R.drawable.down_transmission_new_message : R.drawable.down_transmission;
				navigationBar.setRightButton(null, bgid, null);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (selectionActionBar != null){
			selectionActionBar.onActionBarCompleted(); 
			selectionActionBar = null;
		}
		refreshTransferStatus();
	}
	
	public void setTabHost(RadioGroup tab){
		this.tabHost = tab;
	}
}
