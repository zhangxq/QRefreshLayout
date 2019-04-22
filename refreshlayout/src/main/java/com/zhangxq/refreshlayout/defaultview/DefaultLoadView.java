package com.zhangxq.refreshlayout.defaultview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.zhangxq.refreshlayout.R;
import com.zhangxq.refreshlayout.RefreshView;

/**
 * Created by zhangxiaoqi on 2019/4/22.
 */

public class DefaultLoadView extends RefreshView {
    private ImageView ivPull;
    private ProgressBar progressBar;

    public DefaultLoadView(Context context) {
        this(context, null);
    }

    public DefaultLoadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View viewContent = LayoutInflater.from(context).inflate(R.layout.view_default_load, null);
        addView(viewContent);
        ivPull = viewContent.findViewById(R.id.ivPull);
        progressBar = viewContent.findViewById(R.id.progressBar);
    }

    @Override
    public void setHeight(float height) {
        progressBar.setVisibility(GONE);
        ivPull.setVisibility(VISIBLE);
    }

    @Override
    public void setRefresh() {
        progressBar.setVisibility(VISIBLE);
        ivPull.setVisibility(GONE);
    }

    @Override
    public void setPullToRefresh() {
        ivPull.setRotation(180);
    }

    @Override
    public void setRefeaseToRefresh() {
        ivPull.setRotation(0);
    }
}
