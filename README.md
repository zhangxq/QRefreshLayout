# QRefreshLayout
下拉刷新，上拉加载更多，自动加载更多，用法同SwipeRefreshLayout，兼容所有view，兼容nested滚动

### 使用方式
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
```
dependencies {
  implementation 'com.github.zhangxq:QRefreshLayout:1.0.2'
}
```
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <com.zhangxq.refreshlayout.RefreshLayout
        android:id="@+id/refreshLayout"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <android.support.v7.widget.RecyclerView
            android:id="@+id/recyclerView"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
    </com.zhangxq.refreshlayout.RefreshLayout>
</LinearLayout>
```
```
refreshLayout.setOnRefreshListener(this);
refreshLayout.setOnLoadListener(this);
```
### 接口说明
|名称| 功能 |
|--|--|
| setOnRefreshListener | 设置下拉刷新监听 |
| setOnLoadListener | 设置加载更多监听|
| setRefreshing | 打开或者关闭下拉刷新动画 |
| setLoading | 打开或关闭加载更多动画 |
| setColorSchemeResources setColorSchemeColors | 设置默认下拉刷新进度圈颜色 |
| setProgressBackgroundColorSchemeResource setProgressBackgroundColorSchemeResource | 设置默认下拉刷新进度圈背景颜色 |
| setRefreshView | 设置下拉刷新view |
| setLoadView | 设置加载更多view |
| setLoadEnable | 设置加载更多开关， setOnLoadListener调用后默认开启 |
| setAutoLoad  |  设置自动加载更多开关，setOnLoadListener调用后默认开启 |
### 自定义下拉刷新和加载更多view
setRefreshView 和 setLoadView 两个方法用于接收用户自定义的下拉刷新和加载更多动画view，参数都是接收一个继承自RefreshView的子类，RefreshView实现了一个Refresh接口，代码如下：
```
public interface Refresh {
    /**
     * 手指拖动中
     *
     * @param dragDistance      手指拖动的距离
     * @param distanceToRefresh 下拉到触发刷新位置的距离
     * @param totalDistance     总的下拉空间
     */
    void setHeight(float dragDistance, float distanceToRefresh, float totalDistance);

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
```
可以看到，接口提供了三个个回调方法，对应与下拉或上拉过程中常用的三个时间点，setHeight方法提供了手指拖动的距离，方便用户处理拖动动画。
继承RefreshView覆盖这四个方法，就可以方便得实现自己想要的动画效果。
