package com.zhangxq.test.imageview;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

import com.zhangxq.refreshlayout.QRefreshLayout;
import com.zhangxq.test.R;


/**
 * Created by zhangxiaoqi on 2019/4/18.
 */

public class ImageViewActivity extends AppCompatActivity implements QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener {
    private QRefreshLayout qRefreshLayout;
    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);
        setTitle("ImageView示例");
        qRefreshLayout = findViewById(R.id.refreshLayout);
        qRefreshLayout.setOnRefreshListener(this);
        qRefreshLayout.setOnLoadListener(this);
        qRefreshLayout.setColorSchemeColors(0xffff0000, 0xff00ff00, 0xff0000ff);
        qRefreshLayout.setProgressBackgroundColorSchemeColor(0xffabcdef);

        imageView = findViewById(R.id.imageView);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setRotationY(imageView.getRotationY() + 180);
                qRefreshLayout.setRefreshing(false);
            }
        }, 5000);
    }

    @Override
    public void onLoad() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setRotationY(imageView.getRotationY() + 180);
                qRefreshLayout.setLoading(false);
            }
        }, 1000);
    }
}
