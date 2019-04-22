package com.zhangxq.refreshlayout.defaultview;

/**
 * Created by zhangxiaoqi on 2019/4/22.
 */

public interface Refresh {
    /**
     * 手指拖动中
     *
     * @param dragDistance      手指拖动的距离
     * @param distanceToRefresh 下拉到触发刷新位置的距离
     * @param totalDistance     总的下拉空间
     */
    public abstract void setHeight(float dragDistance, float distanceToRefresh, float totalDistance);

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
