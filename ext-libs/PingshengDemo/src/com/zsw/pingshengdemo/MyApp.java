package com.zsw.pingshengdemo;

import android.app.Application;

import com.wefi.zhuiju.MyDataCenter;


/**
 * App global context
 *
 */
public class MyApp extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        MyDataCenter.getInstance().initData(this);
    }
}
