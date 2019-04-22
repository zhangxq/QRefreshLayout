package com.zhangxq.refreshlayout.defaultview;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.widget.CircularProgressDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.zhangxq.refreshlayout.R;
import com.zhangxq.refreshlayout.RefreshView;

/**
 * Created by zhangxiaoqi on 2019/4/22.
 */

public class DefaultView extends RefreshView {
    private CircleImageView imageView;
    private CircularProgressDrawable mProgress;

    public DefaultView(Context context) {
        this(context, null);
    }

    public DefaultView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.view_default, null);
        addView(view);
        imageView = view.findViewById(R.id.viewCircle);

        mProgress = new CircularProgressDrawable(getContext());
        mProgress.setStyle(CircularProgressDrawable.DEFAULT);
        imageView.setImageDrawable(mProgress);
        mProgress.setArrowEnabled(true);
    }

    @Override
    public void setHeight(float dragDistance, float distanceToRefresh, float totalDistance) {
        mProgress.stop();
        moveSpinner(dragDistance, distanceToRefresh, totalDistance);
    }

    @Override
    public void setRefresh() {
        mProgress.setAlpha(255);
        mProgress.setArrowEnabled(false);
        mProgress.setProgressRotation(1f);
        mProgress.start();
    }

    @Override
    public void setPullToRefresh() {

    }

    @Override
    public void setRefeaseToRefresh() {

    }

    public void setColorSchemeColors(@ColorInt int... colors) {
        mProgress.setColorSchemeColors(colors);
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        imageView.setBackgroundColor(color);
    }

    private void moveSpinner(float overscrollTop, float distanceToRefresh, float totalDragDistance) {
        mProgress.setArrowEnabled(true);
        totalDragDistance = totalDragDistance / 2;
        final float density = getContext().getResources().getDisplayMetrics().density;
        float originalDragPercent = overscrollTop / totalDragDistance;
        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
        float adjustedPercent = (float) Math.max(dragPercent - 0.4, 0) * 5 / 3;
        float extraOS = Math.abs(overscrollTop) - totalDragDistance;
        float slingshotDist = 64 * density;
        float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, slingshotDist * 2) / slingshotDist);
        float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow((tensionSlingshotPercent / 4), 2)) * 2f;
        float strokeStart = adjustedPercent * 0.8f;
        mProgress.setStartEndTrim(0f, Math.min(0.8f, strokeStart));
        mProgress.setArrowScale(Math.min(1f, adjustedPercent));
        float rotation = (-0.25f + 0.4f * adjustedPercent + tensionPercent * 2) * 0.5f;
        mProgress.setProgressRotation(rotation);

        int alpha = (int) (overscrollTop / distanceToRefresh * 255);
        if (alpha < 255 && alpha > 100) alpha = 100;
        if (alpha > 255) alpha = 255;
        mProgress.setAlpha(alpha);
    }
}
