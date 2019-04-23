package com.zhangxq.test.imageview;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.zhangxq.refreshlayout.QRefreshLayout;
import com.zhangxq.test.R;


/**
 * Created by zhangxiaoqi on 2019/4/18.
 */

public class ImageViewActivity extends AppCompatActivity implements QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener {
    private QRefreshLayout QRefreshLayout;
    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);
        QRefreshLayout = findViewById(R.id.refreshLayout);
        QRefreshLayout.setOnRefreshListener(this);
        QRefreshLayout.setOnLoadListener(this);
        QRefreshLayout.setColorSchemeColors(0xffff0000, 0xff00ff00, 0xff0000ff);
        QRefreshLayout.setProgressBackgroundColorSchemeColor(0xffabcdef);

        imageView = findViewById(R.id.imageView);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setRotationY(imageView.getRotationY() + 180);
                QRefreshLayout.setRefreshing(false);
            }
        }, 5000);
    }

    @Override
    public void onLoad() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setRotationY(imageView.getRotationY() + 180);
                QRefreshLayout.setLoading(false);
            }
        }, 1000);
    }
}
