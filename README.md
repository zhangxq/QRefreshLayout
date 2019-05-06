# QRefreshLayout
下拉刷新，上拉加载更多，自动加载更多，用法同SwipeRefreshLayout，兼容所有view，兼容nested滚动，可以自定义刷新动画，支持下拉到二楼

不定期更新，大家有什么想加的功能和意见可以在issues里提出来，我会尽力而为。
### 效果展示
![下拉刷新和加载更多](http://upload-images.jianshu.io/upload_images/6425806-82c42b4ac11b9ccf.gif?imageMogr2/auto-orient/strip)
![与Nested滚动兼容效果](http://upload-images.jianshu.io/upload_images/6425806-c7caf713a04bb54c.gif?imageMogr2/auto-orient/strip)
![下拉到二楼](https://upload-images.jianshu.io/upload_images/6425806-49cb4c2a398d843e.gif?imageMogr2/auto-orient/strip)
### [demo下载](https://github.com/zhangxq/QRefreshLayout/raw/master/app-release.apk)
### [版本更新说明](https://github.com/zhangxq/QRefreshLayout/releases)
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
  implementation 'com.github.zhangxq:QRefreshLayout:1.0.8'
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
| setListViewScrollListener | 设置ListView的滚动监听器（由于需要做自动加更多，所以占用了监听器，在这里回调回来） |
| setPullToRefreshHeight | 设置下拉到"释放即可更新"的高度（默认170px） |
| setLoadToRefreshHeight | 设置上拉到"释放即可加载更多"的高度（默认170px） |
| setRefreshHeight | 设置下拉刷新动画高度（默认150px，需要在setRefreshing之前调用） |
| setLoadHeight | 设置加载更多动画高度（默认110px） |
| setIsCanSecondFloor | 设置是否可以到达二楼 |
| setSecondFloorView | 设置二楼view，仅限于使用默认header的情况 |
| isSecondFloor | 当前是否在二楼 |
| setBackToFirstFloor | 回到一楼 |
| setPullToSecondFloorHeight | 设置下拉到"释放到达二楼"的高度（默认500px） |
### 自定义header和footer
setRefreshView 和 setLoadView 两个方法用于接收用户自定义的header和footer，setRefreshView接收一个继承自RefreshView的view，setLoadView接收一个继承自LoadView的view，RefreshView和LoadView的区别只是RefreshView比LoadView多了三个二楼相关的虚方法，所以我们只用看一下RefreshView的源码：
```
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
```
查看方法的注释，就可以明白对应的功能。
RefreshView和LoadView都实现了一个Refresh接口，代码如下：
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
可以看到，接口提供了三个回调方法，对应与下拉或上拉过程中常用的三个时间点，还有一个setHeight方法提供了手指拖动的距离，方便用户处理拖动动画。
继承RefreshView或者LoadView覆盖这四个方法，就可以方便得实现自己想要的动画效果。
### 下拉到二楼功能
使用默认header情况下，如下两行即可实现下拉到二楼的功能，第二行的参数view就是你想要展示的二楼布局view。
```
qRefreshLayout.setIsCanSecondFloor(true);
qRefreshLayout.setSecondFloorView(view);
```
使用自定义header，则可以去掉第二行，然后使用自定义header中的三个回调方法，实现自己的二楼效果。
