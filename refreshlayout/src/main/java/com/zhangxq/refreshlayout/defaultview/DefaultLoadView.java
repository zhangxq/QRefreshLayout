package com.zhangxq.refreshlayout.defaultview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhangxq.refreshlayout.RefreshView;

/**
 * Created by zhangxiaoqi on 2019/4/17.
 */

public class DefaultLoadView extends RefreshView {
    private TextView tvContent;
    private ProgressBar progressBar;

    public DefaultLoadView(Context context) {
        this(context, null);
    }

    public DefaultLoadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        tvContent = new TextView(context);
        tvContent.setId(View.generateViewId());
        addView(tvContent);
        RelativeLayout.LayoutParams contentParams = (LayoutParams) tvContent.getLayoutParams();
        contentParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        progressBar = new ProgressBar(context);
        addView(progressBar);
        final float density = getContext().getResources().getDisplayMetrics().density;
        RelativeLayout.LayoutParams params = (LayoutParams) progressBar.getLayoutParams();
        params.width = (int) (20 * density);
        params.height = (int) (20 * density);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        params.rightMargin = (int) (10 * density);
        params.addRule(RelativeLayout.LEFT_OF, tvContent.getId());
        progressBar.setLayoutParams(params);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.d("onLayout", progressBar.getLeft() + ":" + progressBar.getTop() + ":" + progressBar.getRight() + ":" + progressBar.getBottom());
    }

    @Override
    public void setHeight(float dragDistance, float distanceToRefresh, float totalDistance) {
        progressBar.setVisibility(GONE);
    }

    @Override
    public void setRefresh() {
        tvContent.setText("正在加载更多");
        progressBar.setVisibility(VISIBLE);
    }

    @Override
    public void setPullToRefresh() {
        tvContent.setText("上拉加载更多");
    }

    @Override
    public void setRefeaseToRefresh() {
        tvContent.setText("释放加载更多");
    }
}
