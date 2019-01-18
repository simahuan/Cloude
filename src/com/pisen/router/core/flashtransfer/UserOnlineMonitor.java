package com.pisen.router.core.flashtransfer;

import java.util.ArrayList;
import java.util.Collections;

import com.pisen.router.core.flashtransfer.scan.UserInfo;

public abstract class UserOnlineMonitor implements Runnable {

	protected ArrayList<UserInfo> users = new ArrayList<UserInfo>();

	public void add(UserInfo... infos) {
		Collections.addAll(users, infos);
	}

	public void remove(UserInfo info) {
		users.remove(info);
	}

}
