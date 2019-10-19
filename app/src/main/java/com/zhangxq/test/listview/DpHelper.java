package com.zhangxq.test.listview;

import android.content.Context;

public class DpHelper {

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        return (int) (0.5F + pxValue / context.getResources().getDisplayMetrics().density);
    }
}
