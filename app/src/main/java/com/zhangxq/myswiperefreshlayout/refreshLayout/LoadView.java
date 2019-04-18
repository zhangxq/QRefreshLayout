package com.zhangxq.myswiperefreshlayout.refreshLayout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhangxq.myswiperefreshlayout.R;

/**
 * Created by zhangxiaoqi on 2019/4/17.
 */

public class LoadView extends LinearLayout implements Refresh {
    private View viewContainer;
    private TextView tvContent;
    private View viewContent;
    private ProgressBar progressBar;

    public LoadView(Context context) {
        this(context, null);
    }

    public LoadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewContent = LayoutInflater.from(context).inflate(R.layout.view_load, null);
        addView(viewContent);
        tvContent = viewContent.findViewById(R.id.tvContent);
        progressBar = viewContent.findViewById(R.id.progressBar);
        viewContainer = viewContent.findViewById(R.id.viewContainer);
    }

    @Override
    public void setHeight(float height) {
        tvContent.setHeight((int) height);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewContent.measure(
                MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
    }
}
