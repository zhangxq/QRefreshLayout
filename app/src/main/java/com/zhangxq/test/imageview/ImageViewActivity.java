package com.zhangxq.test.imageview;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.zhangxq.refreshlayout.RefreshLayout;
import com.zhangxq.test.R;


/**
 * Created by zhangxiaoqi on 2019/4/18.
 */

public class ImageViewActivity extends AppCompatActivity implements RefreshLayout.OnRefreshListener, RefreshLayout.OnLoadListener {
    RefreshLayout refreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setOnLoadListener(this);
        refreshLayout.setColorSchemeColors(0xffff0000, 0xff00ff00, 0xff0000ff);
        refreshLayout.setProgressBackgroundColorSchemeColor(0xffabcdef);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
            }
        }, 10000);
    }

    @Override
    public void onLoad() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setLoading(false);
            }
        }, 1000);
    }
}
