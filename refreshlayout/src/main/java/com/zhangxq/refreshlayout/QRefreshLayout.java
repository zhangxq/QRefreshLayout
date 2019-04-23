package com.zhangxq.refreshlayout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ListViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.zhangxq.refreshlayout.defaultview.DefaultLoadView;
import com.zhangxq.refreshlayout.defaultview.DefaultRefreshView;

/**
 * Created by zhangxiaoqi on 2019/4/16.
 */

public class QRefreshLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {
    private View viewTarget; // 刷新目标

    // 滑动事件相关参数
    private float downY; // 按下的位置y
    private float overScroll; // 上拉和下拉的距离
    private boolean isDraging; // 是否开始滑动
    private boolean isDragDown; // 是否是下拉
    private int mTouchSlop; // 滑动最小阈值
    private final float dragRate = 0.5f; // 滑动速率

    // 刷新逻辑控制参数
    private boolean isRefreshing; // 是否正在进行刷新动画
    private boolean isLoading; // 是否正在进行加载更多动画
    private boolean isAnimating; // 是否正在进行状态切换动画
    private boolean isLoadEnable; // 是否可以加载更多
    private boolean isAutoLoad; // 是否打开自动加载更多
    private boolean isTouchDown; // 手指是否按下
    private boolean isPullingUp; // 是否手指上滑
    private RelativeLayout viewRefreshContainer; // 下拉刷新view容器
    private RelativeLayout viewLoadContainer; // 加载更多view容器
    private RefreshView viewRefresh; // 下拉刷新view
    private RefreshView viewLoad; // 加载更多view
    private final int viewContentHeight = 2000; // 内容区高度
    private final int refreshMidHeight = 170; // 刷新高度，超过这个高度，松手即可刷新
    private final int loadMidHeight = 170; // 加载更多高度，超过这个高度，松手即可加载更多
    private final int refreshHeight = 150; // 刷新动画高度
    private final int loadHeight = 110; // 加载更多动画高度
    private final int animateDuration = 100; // 动画时间ms

    // nested 相关参数
    private float nestedOverScroll;
    private boolean isNestedScrolling;
    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];

    // 动画
    ValueAnimator animatorToRefresh; // 移动到刷新位置
    ValueAnimator animatorToRefreshReset; // 移动到刷新初始位置
    ValueAnimator animatorToLoad; // 移动到加载更多位置
    ValueAnimator animatorToLoadReset; // 移动到加载更多初始位置

    private OnRefreshListener refreshListener;
    private OnLoadListener loadListener;

    public QRefreshLayout(Context context) {
        this(context, null);
    }

    public QRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setNestedScrollingEnabled(true);

        viewRefresh = new DefaultRefreshView(context);
        viewLoad = new DefaultLoadView(context);
        viewRefreshContainer = new RelativeLayout(context);
        viewRefreshContainer.setGravity(Gravity.CENTER);
        viewRefreshContainer.addView(viewRefresh);
        viewLoadContainer = new RelativeLayout(context);
        viewLoadContainer.setGravity(Gravity.CENTER);
        viewLoadContainer.addView(viewLoad);
        viewLoadContainer.setVisibility(GONE);
        addView(viewRefreshContainer);
        addView(viewLoadContainer);
    }

    public void setLoadEnable(boolean isEnable) {
        this.isLoadEnable = isEnable;
        if (isLoadEnable) {
            viewLoadContainer.setVisibility(VISIBLE);
        } else {
            viewLoadContainer.setVisibility(GONE);
        }
    }

    /**
     * 设置自动加载更多开关，默认开启
     *
     * @param isAutoLoad
     */
    public void setAutoLoad(boolean isAutoLoad) {
        this.isAutoLoad = isAutoLoad;
    }

    /**
     * 设置下拉刷新view
     *
     * @param refreshView
     */
    public void setRefreshView(RefreshView refreshView) {
        this.viewRefresh = refreshView;
        viewRefreshContainer.removeAllViews();
        viewRefreshContainer.addView(viewRefresh);
    }

    /**
     * 设置加载更多view
     *
     * @param loadView
     */
    public void setLoadView(RefreshView loadView) {
        this.viewLoad = loadView;
        viewLoadContainer.removeAllViews();
        viewLoadContainer.addView(viewLoad);
    }

    /**
     * 如果使用了默认加载动画，设置进度圈颜色资源
     *
     * @param colorResIds
     */
    public void setColorSchemeResources(@ColorRes int... colorResIds) {
        final Context context = getContext();
        int[] colorRes = new int[colorResIds.length];
        for (int i = 0; i < colorResIds.length; i++) {
            colorRes[i] = ContextCompat.getColor(context, colorResIds[i]);
        }
        setColorSchemeColors(colorRes);
    }

    /**
     * 如果使用了默认加载动画，设置进度圈颜色
     *
     * @param colors
     */
    public void setColorSchemeColors(@ColorInt int... colors) {
        if (viewRefresh instanceof DefaultRefreshView) {
            ((DefaultRefreshView) viewRefresh).setColorSchemeColors(colors);
        }
    }

    /**
     * Set the background color of the progress spinner disc.
     *
     * @param colorRes Resource id of the color.
     */
    public void setProgressBackgroundColorSchemeResource(@ColorRes int colorRes) {
        setProgressBackgroundColorSchemeColor(ContextCompat.getColor(getContext(), colorRes));
    }

    /**
     * Set the background color of the progress spinner disc.
     *
     * @param color
     */
    public void setProgressBackgroundColorSchemeColor(@ColorInt int color) {
        if (viewRefresh instanceof DefaultRefreshView) {
            ((DefaultRefreshView) viewRefresh).setBackgroundColor(color);
        }
    }

    /**
     * 设置下拉刷新监听
     *
     * @param listener
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        refreshListener = listener;
    }

    /**
     * 设置加载更多监听
     *
     * @param listener
     */
    public void setOnLoadListener(OnLoadListener listener) {
        loadListener = listener;
        this.isLoadEnable = true;
        setAutoLoad(true);
        viewLoadContainer.setVisibility(VISIBLE);
    }

    /**
     * 设置是否显示正在刷新
     *
     * @param refreshing
     */
    public void setRefreshing(boolean refreshing) {
        if (refreshing) {
            if (isRefreshing || isLoading || isDraging) return;
            if (!isRefreshing) {
                animateToRefresh();
            }
        } else {
            if (isRefreshing) {
                if (overScroll > 0) {
                    animateToRefreshReset();
                }
            }
        }
    }

    /**
     * 获取是否正在刷新
     *
     * @return
     */
    public boolean isRefreshing() {
        return isRefreshing;
    }

    /**
     * 设置是否显示正在加载更多
     *
     * @param loading
     */
    public void setLoading(boolean loading) {
        if (loading) {
            if (isLoading || isRefreshing || isDraging) return;
            if (!isLoading) {
                animateToLoad();
            }
        } else {
            if (isLoading) {
                if (overScroll < 0) {
                    animateToLoadReset();
                }
            }
        }
    }

    /**
     * 获取是否加载更多
     *
     * @return
     */
    public boolean isLoading() {
        return isLoading;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        ensureTarget();
        if (viewTarget == null) {
            return;
        }
        final View child = viewTarget;
        if (child.getBackground() == null) {
            child.setBackgroundColor(0xffffffff);
        } else {
            child.getBackground().setAlpha(255);
        }
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);

        viewRefreshContainer.layout(0, -viewContentHeight / 2, width, viewContentHeight / 2);
        viewLoadContainer.layout(0, height - viewContentHeight / 2, width, height + viewContentHeight / 2);


    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ensureTarget();
        if (viewTarget == null) {
            return;
        }
        viewTarget.measure(
                MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        viewRefreshContainer.measure(
                MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(viewContentHeight, MeasureSpec.EXACTLY));
        viewLoadContainer.measure(
                MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(viewContentHeight, MeasureSpec.EXACTLY));
    }

    private void onScroll() {
        if (!canChildScrollUp() && isAutoLoad && isLoadEnable && !isLoading && isPullingUp && !isTouchDown) {
            animateToLoad();
        }
    }

    private void ensureTarget() {
        if (viewTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(viewRefreshContainer) && !child.equals(viewLoadContainer)) {
                    viewTarget = child;
                    setScrollListener();
                    break;
                }
            }
        }
    }

    private void setScrollListener() {
        if (viewTarget instanceof ListView) {
            ((ListView) viewTarget).setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    QRefreshLayout.this.onScroll();
                }
            });
        }
        if (viewTarget instanceof RecyclerView) {
            ((RecyclerView) viewTarget).addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    onScroll();
                }
            });
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled() || isAnimating || isRefreshing || isLoading || isNestedScrolling) {
            return false;
        }
        if (isDraging) {
            return true;
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isTouchDown = true;
                isDraging = false;
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float y = ev.getY();
                float yDiff = y - downY;
                if (downY > y) isPullingUp = true;
                if (yDiff > mTouchSlop && !canChildScrollDown()) {
                    isDragDown = true;
                    isDraging = true;
                }
                if (yDiff < -mTouchSlop && !canChildScrollUp() && isLoadEnable) {
                    isDragDown = false;
                    isDraging = true;
                    downY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
                isTouchDown = false;
                break;
        }
        return isDraging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || isAnimating || isRefreshing || isLoading || isNestedScrolling) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isDraging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                if (!isDraging && y > downY) {
                    isDragDown = true;
                }
                overScroll = (y - downY) * dragRate;
                if (isDragDown && overScroll < 0 || !isDragDown && overScroll > 0) {
                    overScroll = 0;
                    viewRefreshContainer.setTranslationY(overScroll);
                    viewTarget.setTranslationY(overScroll);
                    viewLoad.setTranslationY(overScroll);
                    return false;
                }
                if (isDragDown && overScroll >= 0) {
                    if (overScroll > viewContentHeight / 2) {
                        overScroll = viewContentHeight / 2;
                    }
                    viewRefreshContainer.setTranslationY(overScroll / 2);
                    viewTarget.setTranslationY(overScroll);
                }
                if (!isDragDown && overScroll <= 0) {
                    if (Math.abs(overScroll) > viewContentHeight / 2) {
                        overScroll = -viewContentHeight / 2;
                    }
                    viewLoad.setTranslationY(overScroll / 2);
                    viewTarget.setTranslationY(overScroll);
                }

                if (overScroll > 0) {
                    viewRefresh.setHeight(overScroll, refreshMidHeight, viewContentHeight / 2);
                    if (overScroll > refreshMidHeight) {
                        viewRefresh.setRefeaseToRefresh();
                    } else {
                        viewRefresh.setPullToRefresh();
                    }
                } else {
                    viewLoad.setHeight(Math.abs(overScroll), loadMidHeight, viewContentHeight / 2);
                    if (overScroll < -loadMidHeight) {
                        viewLoad.setRefeaseToRefresh();
                    } else {
                        viewLoad.setPullToRefresh();
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onTouchUp();
                isDraging = false;
                isDragDown = false;
                return false;
        }
        return true;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        if ((android.os.Build.VERSION.SDK_INT < 21 && viewTarget instanceof AbsListView) || (viewTarget != null && !ViewCompat.isNestedScrollingEnabled(viewTarget))) {
        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    // 处理与父view之间的关联滑动

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() && !isRefreshing && !isLoading && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
        isTouchDown = true;
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
        nestedOverScroll = 0;
        isNestedScrolling = true;
    }

    @Override
    public int getNestedScrollAxes() {
        return super.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View target) {
        super.onStopNestedScroll(target);
        isNestedScrolling = false;
        isTouchDown = false;
        onTouchUp();
        nestedOverScroll = 0;
        stopNestedScroll();
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (dy > 0) isPullingUp = true;
        if (nestedOverScroll > 0 && dy > 0 || nestedOverScroll < 0 && dy < 0) {
            nestedOverScroll -= dy;
            consumed[1] = dy;
            onNestedDraging(nestedOverScroll);
        }

        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed, final int dxUnconsumed, final int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow);
        int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (dy > 0 && !canChildScrollUp()) {
            if (dy > 50) dy = 50;
            nestedOverScroll -= dy;
        }
        if (dy < 0 && !canChildScrollDown()) {
            if (dy < -50) dy = -50;
            nestedOverScroll -= dy;
        }
        onNestedDraging(nestedOverScroll);
    }

    private void onNestedDraging(float offset) {
        overScroll = offset * dragRate * 0.7f;
        if (overScroll > 0) {
            if (overScroll > viewContentHeight / 2) {
                overScroll = viewContentHeight / 2;
            }
            viewRefreshContainer.setTranslationY(overScroll / 2);
            viewTarget.setTranslationY(overScroll);
            viewRefresh.setHeight(overScroll, refreshMidHeight, viewContentHeight / 2);
            if (overScroll > refreshMidHeight) {
                viewRefresh.setRefeaseToRefresh();
            } else {
                viewRefresh.setPullToRefresh();
            }
        } else {
            if (overScroll < -viewContentHeight / 2) {
                overScroll = -viewContentHeight / 2;
            }
            viewLoad.setTranslationY(overScroll / 2);
            viewTarget.setTranslationY(overScroll);
            viewLoad.setHeight(Math.abs(overScroll), loadMidHeight, viewContentHeight / 2);
            if (overScroll < -loadMidHeight) {
                viewLoad.setRefeaseToRefresh();
            } else {
                viewLoad.setPullToRefresh();
            }
        }
    }

    private void onTouchUp() {
        if (overScroll == 0) return;
        if (overScroll > 0) {
            if (overScroll > refreshMidHeight) {
                animateToRefresh();
            } else {
                animateToRefreshReset();
            }
        } else {
            if (overScroll < -loadMidHeight) {
                animateToLoad();
            } else {
                animateToLoadReset();
            }
        }
    }

    /**
     * 动画移动到刷新位置
     */
    private void animateToRefresh() {
        if (isAnimating) return;
        isAnimating = true;
        if (animatorToRefresh == null) {
            animatorToRefresh = ValueAnimator.ofFloat(Math.abs(overScroll), refreshHeight);
            animatorToRefresh.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float height = (float) animation.getAnimatedValue();
                    overScroll = height;
                    viewRefreshContainer.setTranslationY(overScroll / 2);
                    viewRefresh.setHeight(overScroll, refreshMidHeight, viewContentHeight / 2);
                    viewTarget.setTranslationY(overScroll);
                    if (height == refreshHeight) {
                        viewRefresh.setRefresh();
                        isRefreshing = true;
                        if (refreshListener != null) {
                            refreshListener.onRefresh();
                        }
                        isAnimating = false;
                    }
                }
            });
            animatorToRefresh.setDuration(animateDuration);
        } else {
            animatorToRefresh.setFloatValues(Math.abs(overScroll), refreshHeight);
        }
        animatorToRefresh.start();
    }

    /**
     * 动画移动到加载更多位置
     */
    private void animateToLoad() {
        if (isAnimating) return;
        isAnimating = true;
        if (animatorToLoad == null) {
            animatorToLoad = ValueAnimator.ofFloat(overScroll, -loadHeight);
            animatorToLoad.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float height = (float) animation.getAnimatedValue();
                    overScroll = height;
                    viewLoad.setTranslationY(overScroll / 2);
                    viewLoad.setHeight(Math.abs(overScroll), loadMidHeight, viewContentHeight / 2);
                    viewTarget.setTranslationY(overScroll);
                    if (height == -loadHeight) {
                        viewLoad.setRefresh();
                        isLoading = true;
                        if (loadListener != null) {
                            loadListener.onLoad();
                        }
                        isAnimating = false;
                    }
                }
            });
            animatorToLoad.setDuration(animateDuration);
        } else {
            animatorToLoad.setFloatValues(overScroll, -loadHeight);
        }
        animatorToLoad.start();
        isPullingUp = false;
    }

    /**
     * 动画移动到刷新初始位置
     */
    private void animateToRefreshReset() {
        if (isAnimating) return;
        isAnimating = true;
        if (animatorToRefreshReset == null) {
            animatorToRefreshReset = ValueAnimator.ofFloat(Math.abs(overScroll), 0);
            animatorToRefreshReset.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float height = (float) animation.getAnimatedValue();
                    overScroll = height;
                    viewRefreshContainer.setTranslationY(overScroll / 2);
                    viewRefresh.setHeight(overScroll, refreshMidHeight, viewContentHeight / 2);
                    viewTarget.setTranslationY(overScroll);
                    isRefreshing = false;
                    if (height == 0) {
                        isAnimating = false;
                    }
                }
            });
            animatorToRefreshReset.setDuration(animateDuration);
        } else {
            animatorToRefreshReset.setFloatValues(Math.abs(overScroll), 0);
        }
        animatorToRefreshReset.start();
    }

    /**
     * 动画移动到加载更多初始位置
     */
    private void animateToLoadReset() {
        if (isAnimating) return;
        isAnimating = true;
        if (animatorToLoadReset == null) {
            animatorToLoadReset = ValueAnimator.ofFloat(overScroll, 0);
            animatorToLoadReset.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float height = (float) animation.getAnimatedValue();
                    overScroll = height;
                    viewLoad.setTranslationY(overScroll / 2);
                    viewLoad.setHeight(Math.abs(overScroll), loadMidHeight, viewContentHeight / 2);
                    viewTarget.setTranslationY(overScroll);
                    isLoading = false;
                    if (height == 0) {
                        isAnimating = false;
                    }
                }
            });
            animatorToLoadReset.setDuration(animateDuration);
        } else {
            animatorToLoadReset.setFloatValues(overScroll, 0);
        }
        animatorToLoadReset.start();
    }

    /**
     * 全部恢复到初始位置
     */
    private void reset() {
        if (animatorToRefresh != null) animatorToRefresh.cancel();
        if (animatorToRefreshReset != null) animatorToRefreshReset.cancel();
        if (animatorToLoad != null) animatorToLoad.cancel();
        if (animatorToLoadReset != null) animatorToLoadReset.cancel();
        viewRefreshContainer.setTranslationY(0);
        viewLoad.setTranslationY(0);
        viewTarget.setTranslationY(0);
    }

    /**
     * 目标是否可以向下滚动
     *
     * @return
     */
    private boolean canChildScrollDown() {
        if (viewTarget instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) viewTarget, -1);
        }
        return viewTarget.canScrollVertically(-1);
    }

    /**
     * 目标是否可以向上滚动
     *
     * @return
     */
    private boolean canChildScrollUp() {
        if (viewTarget instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) viewTarget, 1);
        }
        return viewTarget.canScrollVertically(1);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    public interface OnLoadListener {
        void onLoad();
    }
}
