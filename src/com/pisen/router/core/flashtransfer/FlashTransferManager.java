package com.pisen.router.core.flashtransfer;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.core.filemanager.transfer.TransferInfo;

/**
 * (闪电互传)
 * @author yangyp
 */
public class FlashTransferManager extends Service {
	private static final String TAG =  FlashTransferManager.class.getSimpleName();
	private static final boolean DEBUG = true;
	
	private static SendFlashTransfer sendFlashTransfer;
	private static RecvFlashTransfer recvFlashTransfer;
	private FlashTransferBinder binder = new FlashTransferBinder();
	//所有正在进行中的发送任务
	private static List<TransferInfo> taskList;
	private static int sendCount;
	private static int recvCount;
	private WakeLock lock;
//	private TransferChangedReceiver receiver;
	
	/*任务完成*/
	public static final String ACTION_TRANSFER_COMPLETE_RECEIVE_IMAGE = "ft_complete_recv_img";
	public static final String ACTION_TRANSFER_COMPLETE_RECEIVE_MOVIE = "ft_complete_recv_movie";
	public static final String ACTION_TRANSFER_COMPLETE_RECEIVE_MUSIC = "ft_complete_recv_music";
	public static final String ACTION_TRANSFER_COMPLETE_RECEIVE_DOCUMENT = "ft_complete_recv_doc";
	public static final String ACTION_TRANSFER_COMPLETE_RECEIVE_APK = "ft_complete_recv_apk";
	public static final String ACTION_TRANSFER_COMPLETE_SEND = "flashtransfer_complete_send";
	
	public static final String ACTION_TRANSFER_RECEIVE_BEGIN = "flashtransfer_complete_recv_changed";
	public static final String ACTION_TRANSFER_SEND_BEGIN = "flashtransfer_complete_recv_changed";
	/*数量变更*/
	public static final String ACTION_TRANSFER_RECEIVE_CHANGED = "flashtransfer_complete_recv_changed";
	public static final String ACTION_TRANSFER_SEND_CHANGED = "flashtransfer_complete_send_changed";
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		init();
		acquireLock();
//		registReceiver();
	}
	
	/**
	 * 设置电源管理策略
	 */
	private void acquireLock() {
		releaseLock();
		PowerManager pm = (PowerManager) getSystemService(Service.POWER_SERVICE);
		lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		lock.acquire();
	}
	
	/**
	 * 是否配置的电源管理策略
	 */
	private void releaseLock() {
		if(lock != null && lock.isHeld()) {
			lock.release();
		}
		lock = null;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	@Override
	public void onDestroy() {
		Log.e(TAG, "===onDestroy===");
		releaseLock();
//		unregisterReceiver();
		
		super.onDestroy();
	}
	
	private void init() {
		taskList = new ArrayList<TransferInfo>();
	}

	
	
//	/**
//	 * 注册刷新视图广播接收器
//	 */
//	private void registReceiver() {
//		IntentFilter in = new IntentFilter();
//		in.addAction(ACTION_TRANSFER_COMPLETE_RECEIVE_APK);
//		in.addAction(ACTION_TRANSFER_COMPLETE_RECEIVE_DOCUMENT);
//		in.addAction(ACTION_TRANSFER_COMPLETE_RECEIVE_IMAGE);
//		in.addAction(ACTION_TRANSFER_COMPLETE_RECEIVE_MOVIE);
//		in.addAction(ACTION_TRANSFER_COMPLETE_RECEIVE_MUSIC);
//		in.addAction(ACTION_TRANSFER_COMPLETE_SEND);
//		receiver = new TransferChangedReceiver();
//		registerReceiver(receiver, in);
//	}
//
//	/**
//	 * 注销广播接收器
//	 */
//	private void unregisterReceiver() {
//		unregisterReceiver(receiver);
//	}
	
//	private class TransferChangedReceiver extends BroadcastReceiver {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			String action = intent.getAction();
//			if(ACTION_TRANSFER_COMPLETE_SEND.equals(action)) {
//				TransferInfo tmp = getTransferInfo(intent.getLongExtra("id", -1));
//				if( tmp!= null) {
//					taskList.remove(tmp);
//					notifySendCountChanged();
//				}
//			}else if(ACTION_TRANSFER_COMPLETE_RECEIVE_APK.equals(action) ||
//					ACTION_TRANSFER_COMPLETE_RECEIVE_DOCUMENT.equals(action) ||
//					ACTION_TRANSFER_COMPLETE_RECEIVE_IMAGE.equals(action) ||
//					ACTION_TRANSFER_COMPLETE_RECEIVE_MOVIE.equals(action) ||
//					ACTION_TRANSFER_COMPLETE_RECEIVE_MUSIC.equals(action)){
//				recvCount++;
//				notifyRecvCountChanged();
//			}
//		}
//		
//		private TransferInfo getTransferInfo(long id) {
//			if(!taskList.isEmpty()) {
//				for(TransferInfo info : taskList) {
//					if(info._id == id) {
//						return info;
//					}
//				}
//			}
//			
//			return null;
//		}
//		
//	}
	
	private void notifyRecvCountChanged() {
		Intent in = new Intent(ACTION_TRANSFER_RECEIVE_CHANGED);
		in.putExtra("recvCount", recvCount);
		sendBroadcast(in);
	}

	private void notifySendCountChanged() {
//		Intent in = new Intent(ACTION_TRANSFER_SEND_CHANGED);
//		in.putExtra("sendCount", sendCount);
//		in.putExtra("curCompleteCount", sendCount - taskList.size());
//		sendBroadcast(in);
	}

	/**
	 * 启动发送服务
	 */
	public static void startSendService(Context ctx) {
		Log.e("FlashTransferManager", "===startSendService===");
		if(NetUtil.isWifiConnected(ctx) || new WifiApManager(ctx).isWifiApEnabled()) {
			if(sendFlashTransfer == null) {
				sendFlashTransfer = SendFlashTransfer.getInstance(ctx);
			}
		}
	}
	
	/**
	 * 启动接收服务
	 */
	public static void startRecvService(Context ctx) {
		if(recvFlashTransfer != null && recvFlashTransfer.isRunning()) {
			Log.e("FlashTransferManager", "===startRecvService=== recvFlashTransfer is running");
			return;
		}
		
		if(NetUtil.isWifiConnected(ctx) || new WifiApManager(ctx).isWifiApEnabled()) {
			Log.e("FlashTransferManager", "===startRecvService=== new recvFlashTransfer ");
			if(recvFlashTransfer == null) {
				recvFlashTransfer = RecvFlashTransfer.getInstance(ctx);
			}
			recvFlashTransfer.startRecvService();
		}else {
			Log.e("FlashTransferManager", "===startRecvService=== failed,network is not enable ");
		}
	}
	
	/**
	 * 是否资源
	 * @param forceStopRecv	是否强制停止接收数据。用于传输大文件时，由于当网络阻塞时接收不到用户在线广播导致接收线程被终止
	 */
	public static void release(boolean forceStopRecv) {
		taskList.clear();
		sendCount = 0;
		recvCount = 0;
		
		Log.e("FlashTransferManager", "===stopRecvService===");
		if(forceStopRecv && recvFlashTransfer != null) {
			recvFlashTransfer.stopRecvService();
		}
		recvFlashTransfer = null;
		
	}
	
	/**
	 * 开始单个发送任务
	 * @param url	接收地址
	 * @param info	发送的文件
	 */
	public void startSendTask(String url, TransferInfo info) {
		if(sendFlashTransfer != null) {
			sendFlashTransfer.sendFile(url, info);
			
			sendCount++;
			taskList.add(info);
			notifySendCountChanged();
		}else {
			Log.e("FlashTransferManager", "sendFlashTransfer is null!");
		}
	}
	
	/**
	 * 开始批量发送任务
	 * @param url	接收地址
	 * @param infos	发送的文件集
	 */
	public synchronized void startSendTask(String url, TransferInfo... infos) {
		if(sendFlashTransfer != null) {
			sendFlashTransfer.sendFile(url, infos);
		}
	}
	
	/**
	 * 暂停发送任务（不支持断点续传）
	 * @param info
	 */
	public static void pauseSendTask(TransferInfo info) {
		if(sendFlashTransfer != null) {
			sendFlashTransfer.pauseTask(info);
		}
	}
	
	/**
	 * 删除发送任务
	 * @param info
	 */
	public static void removeSendTask(TransferInfo info) {
		if(sendFlashTransfer != null) {
			sendFlashTransfer.removeTask(info);
		}
	}
	
	/**
	 * 删除接收任务
	 * @param info
	 */
	public static void removeRecvTask(TransferInfo info) {
		if(recvFlashTransfer != null) {
			recvFlashTransfer.removeTask(info);
		}
	}
	
	public class FlashTransferBinder extends Binder {
		
		public FlashTransferManager getFlashTransferManager() {
			return FlashTransferManager.this;
		}
	}
}
