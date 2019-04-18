package com.zhangxq.myswiperefreshlayout;

import android.app.Activity;
import android.content.Context;

public class ScreenHelper {

    public static int getScreenWidth(Activity activity) {
        return activity.getWindowManager().getDefaultDisplay().getWidth();
    }

    public static int getScreenHeight(Activity activity) {
        return activity.getWindowManager().getDefaultDisplay().getHeight();
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        return (int) (0.5F + pxValue / context.getResources().getDisplayMetrics().density);
    }
}
