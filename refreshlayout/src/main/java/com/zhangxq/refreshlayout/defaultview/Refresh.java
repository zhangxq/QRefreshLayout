package com.zhangxq.refreshlayout.defaultview;

/**
 * Created by zhangxiaoqi on 2019/4/22.
 */

public interface Refresh {
    /**
     * 手指拖动中
     *
     * @param height        显示出来的区域高度
     * @param refreshHeight 下拉到触发刷新位置的显示区域高度
     * @param totalHeight   总的显示区域高度
     */
    void setHeight(float height, float refreshHeight, float totalHeight);

    /**
     * 触发刷新
     */
    void setRefresh();

    /**
     * 下拉刷新
     */
    void setPullToRefresh();

    /**
     * 释放即可刷新
     */
    void setRefeaseToRefresh();
}
