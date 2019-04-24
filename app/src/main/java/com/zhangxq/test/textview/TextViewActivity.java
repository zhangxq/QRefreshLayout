package com.zhangxq.test.textview;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.zhangxq.refreshlayout.QRefreshLayout;
import com.zhangxq.test.R;

/**
 * Created by zhangxiaoqi on 2019/4/24.
 */

public class TextViewActivity extends AppCompatActivity implements QRefreshLayout.OnRefreshListener {
    private QRefreshLayout refreshLayout;
    private TextView textView;
    private int num = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textview);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);
        textView = findViewById(R.id.textView);
    }

    @Override
    public void onRefresh() {
        textView.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
                textView.setText("数数字：" + num++);
            }
        }, 1000);
    }
}
