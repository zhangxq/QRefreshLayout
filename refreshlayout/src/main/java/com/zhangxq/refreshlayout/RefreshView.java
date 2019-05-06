package com.zhangxq.refreshlayout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.RelativeLayout;

import com.zhangxq.refreshlayout.defaultview.Refresh;

/**
 * Created by zhangxiaoqi on 2019/4/22.
 */

public abstract class RefreshView extends RelativeLayout implements Refresh {
    public RefreshView(Context context) {
        this(context, null);
    }

    public RefreshView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
    }

    /**
     * 释放即可到达二楼
     */
    public abstract void setReleaseToSecondFloor();

    /**
     * 展示二楼
     */
    public abstract void setToSecondFloor();

    /**
     * 回到一楼
     */
    public abstract void setToFirstFloor();
}
