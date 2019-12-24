package com.zhangxq.test.nestedscrollview;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

import com.zhangxq.refreshlayout.QRefreshLayout;
import com.zhangxq.test.R;

/**
 * Created by zhangxiaoqi on 2019/4/23.
 */

public class NestScrollViewActivity extends AppCompatActivity implements QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener {
    private QRefreshLayout qRefreshLayout;
    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nestedscrollview);
        setTitle("NestScrollView示例");
        qRefreshLayout = findViewById(R.id.refreshLayout);
        qRefreshLayout.setOnRefreshListener(this);
        qRefreshLayout.setOnLoadListener(this);

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
        }, 1000);
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
