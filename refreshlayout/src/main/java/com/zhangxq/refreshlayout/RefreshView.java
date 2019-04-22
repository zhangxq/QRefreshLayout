package com.zhangxq.refreshlayout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.RelativeLayout;

/**
 * Created by zhangxiaoqi on 2019/4/22.
 */

public abstract class RefreshView extends RelativeLayout {
    public RefreshView(Context context) {
        this(context, null);
    }

    public RefreshView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
    }

    /**
     * 内容区高度变化
     *
     * @param height
     */
    public abstract void setHeight(float height);

    /**
     * 触发刷新
     */
    public abstract void setRefresh();

    /**
     * 下拉刷新
     */
    public abstract void setPullToRefresh();

    /**
     * 释放即可刷新
     */
    public abstract void setRefeaseToRefresh();
}
