package com.zhangxq.refreshlayout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by zhangxiaoqi on 2019/4/17.
 */

public class RefreshView extends LinearLayout implements Refresh {
    private TextView tvContent;
    private View viewContent;
    private ProgressBar progressBar;

    public RefreshView(Context context) {
        this(context, null);
    }

    public RefreshView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewContent = LayoutInflater.from(context).inflate(R.layout.view_refresh, null);
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
        tvContent.setText("正在刷新");
        progressBar.setVisibility(VISIBLE);
    }

    @Override
    public void setPullToRefresh() {
        tvContent.setText("下拉刷新");
    }

    @Override
    public void setRefeaseToRefresh() {
        tvContent.setText("释放刷新");
    }
}
