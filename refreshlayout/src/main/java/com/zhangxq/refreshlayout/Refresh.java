package com.zhangxq.refreshlayout;

/**
 * Created by zhangxiaoqi on 2019/4/17.
 */

public interface Refresh {
    /**
     * 内容区高度变化
     *
     * @param height
     */
    void setHeight(float height);

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
