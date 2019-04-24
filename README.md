# QRefreshLayout
下拉刷新，上拉加载更多，自动加载更多，用法同SwipeRefreshLayout，兼容所有view，兼容nested滚动，可以自定义刷新动画
### 效果展示
![下拉刷新和加载更多](http://upload-images.jianshu.io/upload_images/6425806-82c42b4ac11b9ccf.gif?imageMogr2/auto-orient/strip)
![与Nested滚动兼容效果](http://upload-images.jianshu.io/upload_images/6425806-c7caf713a04bb54c.gif?imageMogr2/auto-orient/strip)

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
  implementation 'com.github.zhangxq:QRefreshLayout:1.0.6'
}
```
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <com.zhangxq.refreshlayout.QRefreshLayout
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
|setListViewScrollListener|设置ListView的滚动监听器（由于需要做自动加更多，所以占用了监听器，在这里回调回来）|
### 自定义下拉刷新和加载更多view
setRefreshView 和 setLoadView 两个方法用于接收用户自定义的下拉刷新和加载更多动画view，参数都是接收一个继承自RefreshView的子类，RefreshView实现了一个Refresh接口，代码如下：
```
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
```
可以看到，接口提供了三个个回调方法，对应与下拉或上拉过程中常用的三个时间点，setHeight方法提供了手指拖动的距离，方便用户处理拖动动画。
继承RefreshView覆盖这四个方法，就可以方便得实现自己想要的动画效果。
