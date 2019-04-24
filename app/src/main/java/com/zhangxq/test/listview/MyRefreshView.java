package com.zhangxq.test.listview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhangxq.refreshlayout.RefreshView;
import com.zhangxq.test.R;

/**
 * Created by zhangxiaoqi on 2019/4/17.
 */

public class MyRefreshView extends RefreshView {
    private TextView tvContent;
    private View viewContent;
    private ProgressBar progressBar;

    public MyRefreshView(Context context) {
        this(context, null);
    }

    public MyRefreshView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewContent = LayoutInflater.from(context).inflate(R.layout.view_refresh, null);
        addView(viewContent);
        tvContent = viewContent.findViewById(R.id.tvContent);
        progressBar = viewContent.findViewById(R.id.progressBar);
    }

    @Override
    public void setHeight(float dragDistance, float distanceToRefresh, float totalDistance) {

    }

    @Override
    public void setRefresh() {
        tvContent.setText("正在刷新");
        progressBar.setVisibility(VISIBLE);
    }

    @Override
    public void setPullToRefresh() {
        progressBar.setVisibility(GONE);
        tvContent.setText("下拉刷新");
    }

    @Override
    public void setRefeaseToRefresh() {
        progressBar.setVisibility(GONE);
        tvContent.setText("释放刷新");
    }
}
