package com.zhangxq.test.listview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhangxq.refreshlayout.RefreshView;
import com.zhangxq.test.R;

/**
 * Created by zhangxiaoqi on 2019/4/17.
 */

public class MyRefreshView extends RefreshView implements View.OnClickListener {
    private TextView tvContent;
    private TextView tvHeight;
    private View viewContent;
    private ProgressBar progressBar;
    private Button btnReturn;
    private View viewCover;
    private View viewContainer;

    private Listener listener;

    public MyRefreshView(Context context) {
        this(context, null);
    }

    public MyRefreshView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewContent = LayoutInflater.from(context).inflate(R.layout.view_refresh, null);
        addView(viewContent);
        tvContent = viewContent.findViewById(R.id.tvContent);
        viewContainer = viewContent.findViewById(R.id.viewContainer);
        tvHeight = viewContent.findViewById(R.id.tvHeight);
        progressBar = viewContent.findViewById(R.id.progressBar);
        viewCover = viewContent.findViewById(R.id.viewCover);
        btnReturn = viewContent.findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(this);
    }

    public MyRefreshView setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void setHeight(float dragDistance, float distanceToRefresh, float totalDistance) {
        tvHeight.setText((int) dragDistance + ":" + (int) distanceToRefresh + ":" + (int) totalDistance);
        viewCover.setAlpha((totalDistance - dragDistance) / totalDistance);
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
    public void setReleaseToRefresh() {
        progressBar.setVisibility(GONE);
        tvContent.setText("释放刷新");
    }

    @Override
    public void setReleaseToSecondFloor() {
        progressBar.setVisibility(GONE);
        tvContent.setText("释放到达二楼");
    }

    @Override
    public void setToSecondFloor() {
        viewCover.setAlpha(0);
        viewContainer.setVisibility(GONE);
    }

    @Override
    public void setToFirstFloor() {
        viewCover.setAlpha(1);
        viewContainer.setVisibility(VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) listener.onBackFirstFloor();
    }
}
