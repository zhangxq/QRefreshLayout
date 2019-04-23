package com.zhangxq.test.nestedscrollview;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.zhangxq.refreshlayout.RefreshLayout;
import com.zhangxq.test.R;

/**
 * Created by zhangxiaoqi on 2019/4/23.
 */

public class NestScrollViewActivity extends AppCompatActivity implements RefreshLayout.OnRefreshListener, RefreshLayout.OnLoadListener {
    private RefreshLayout refreshLayout;
    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nestedscrollview);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setOnLoadListener(this);

        imageView = findViewById(R.id.imageView);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setRotation(imageView.getRotation() + 180);
                refreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

    @Override
    public void onLoad() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setRotation(imageView.getRotation() + 180);
                refreshLayout.setLoading(false);
            }
        }, 1000);
    }
}
