package com.pisen.router.ui.phone.settings;

import java.util.HashMap;
import java.util.LinkedHashMap;

import android.annotation.SuppressLint;

import com.pisen.router.R;

/**
 * @author  mahuan
 * @version 1.0 2015年5月28日 上午11:16:46
 */
public class IconResource {
	//默认头像
	private static final int KEY_ICON_DEFAULT = 1001;
	
	@SuppressLint("UseSparseArrays")
	static final LinkedHashMap<Integer, Integer> icons = new LinkedHashMap<Integer, Integer>();
	static {
		icons.put(1001, R.drawable.head_1);
		icons.put(1002, R.drawable.head_2);
		icons.put(1003, R.drawable.head_3);
		icons.put(1004, R.drawable.head_4);
		icons.put(1005, R.drawable.head_5);
		icons.put(1006, R.drawable.head_6);
		icons.put(1007, R.drawable.head_7);
		icons.put(1008, R.drawable.head_8);
	}
	
	/**
	 * @return the icons
	 */
	public static HashMap<Integer, Integer> getAllIcons() {
		return icons;
	}
	
	public static Integer getIcon(int key) {
		return icons.get(key) == null? icons.get(KEY_ICON_DEFAULT):icons.get(key);
	}
}
