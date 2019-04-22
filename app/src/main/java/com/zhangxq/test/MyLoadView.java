package com.zhangxq.test;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhangxq.refreshlayout.RefreshView;

/**
 * Created by zhangxiaoqi on 2019/4/17.
 */

public class MyLoadView extends RefreshView {
    private TextView tvContent;
    private View viewContent;
    private ProgressBar progressBar;

    public MyLoadView(Context context) {
        this(context, null);
    }

    public MyLoadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewContent = LayoutInflater.from(context).inflate(R.layout.view_load, null);
        addView(viewContent);
        tvContent = viewContent.findViewById(R.id.tvContent);
        progressBar = viewContent.findViewById(R.id.progressBar);
    }

    @Override
    public void setHeight(float height) {
        progressBar.setVisibility(GONE);
    }

    @Override
    public void setRefresh() {
        tvContent.setText("正在加载更多");
        progressBar.setVisibility(VISIBLE);
    }

    @Override
    public void setPullToRefresh() {
        tvContent.setText("下拉加载更多");
    }

    @Override
    public void setRefeaseToRefresh() {
        tvContent.setText("释放加载更多");
    }
}
