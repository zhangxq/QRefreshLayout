package com.zhangxq.refreshlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.RelativeLayout;

import com.zhangxq.refreshlayout.defaultview.Refresh;

public abstract class LoadView extends RelativeLayout implements Refresh {
    public LoadView(Context context) {
        this(context, null);
    }

    public LoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
    }
}
