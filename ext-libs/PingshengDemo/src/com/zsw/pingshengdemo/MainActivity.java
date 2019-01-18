
package com.zsw.pingshengdemo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

import com.wefi.pingshengdemo.R;

public class MainActivity extends FragmentActivity {
    
    private FragmentTabHost mFgTabHost;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mFgTabHost=(FragmentTabHost)this.findViewById(android.R.id.tabhost);
        mFgTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent_main);
        
        mFgTabHost.addTab(mFgTabHost.newTabSpec("A").setIndicator("追剧"), FollowFragment.class, null);
        mFgTabHost.addTab(mFgTabHost.newTabSpec("B").setIndicator("B"), Fragment3.class, null);
        mFgTabHost.addTab(mFgTabHost.newTabSpec("C").setIndicator("C"), Fragment3.class, null);
    }
    

}
