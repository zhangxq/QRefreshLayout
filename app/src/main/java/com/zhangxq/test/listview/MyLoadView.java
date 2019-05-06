package com.zhangxq.test.listview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.zhangxq.refreshlayout.LoadView;
import com.zhangxq.test.R;

/**
 * Created by zhangxiaoqi on 2019/4/17.
 */

public class MyLoadView extends LoadView {
    private TextView tvContent;
    private View viewContent;

    public MyLoadView(Context context) {
        this(context, null);
    }

    public MyLoadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewContent = LayoutInflater.from(context).inflate(R.layout.view_my_load, null);
        addView(viewContent);
        tvContent = viewContent.findViewById(R.id.tvContent);
    }

    @Override
    public void setHeight(float dragDistance, float distanceToRefresh, float totalDistance) {
    }

    @Override
    public void setRefresh() {
        tvContent.setText("看看还有没有~~");
    }

    @Override
    public void setPullToRefresh() {
        tvContent.setText("继续拉");
    }

    @Override
    public void setReleaseToRefresh() {
        tvContent.setText("松手");
    }
}
