package com.zhangxq.refreshlayout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ListViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by zhangxiaoqi on 2019/4/16.
 */

public class RefreshLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {
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
    private RefreshView viewRefresh; // 下拉刷新view
    private LoadView viewLoad; // 加载更多view
    private final int viewRefreshHeight = 500; // 下拉刷新和加载更多内容区高度
    private final int overRefreshHeight = 200; // 下拉刷新最大高度
    private final int overLoadHeight = 200; // 加载更多最大高度
    private final int refreshMidHeight = 170; // 刷新高度，超过这个高度，松手即可刷新
    private final int loadMidHeight = 170; // 加载更多高度，超过这个高度，松手即可加载更多
    private final int refreshHeight = 100; // 刷新动画高度
    private final int loadHeight = 100; // 加载更多动画高度
    private final int animateDuration = 100; // 动画时间，100ms

    // 动画
    ValueAnimator animatorToRefresh; // 移动到刷新位置
    ValueAnimator animatorToRefreshReset; // 移动到刷新初始位置
    ValueAnimator animatorToLoad; // 移动到加载更多位置
    ValueAnimator animatorToLoadReset; // 移动到加载更多初始位置

    // NestedScroll相关参数
    private float mTotalUnconsumed;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];
    private boolean mNestedScrollInProgress;

    private OnRefreshListener refreshListener;
    private OnLoadListener loadListener;

    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        viewRefresh = new RefreshView(context);
        addView(viewRefresh);
        viewLoad = new LoadView(context);
        addView(viewLoad);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        refreshListener = listener;
    }

    public void setOnLoadListener(OnLoadListener listener) {
        loadListener = listener;
    }

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

    public boolean isRefreshing() {
        return isRefreshing;
    }

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
        if (viewTarget == null) {
            ensureTarget();
        }
        if (viewTarget == null) {
            return;
        }
        final View child = viewTarget;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);

        viewRefresh.layout(0, -viewRefreshHeight, width, 0);
        viewLoad.layout(0, height, width, height + viewRefreshHeight);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (viewTarget == null) {
            ensureTarget();
        }
        if (viewTarget == null) {
            return;
        }
        viewTarget.measure(
                MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        viewRefresh.measure(
                MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(viewRefreshHeight, MeasureSpec.EXACTLY));
        viewLoad.measure(
                MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(viewRefreshHeight, MeasureSpec.EXACTLY));
    }

    private void ensureTarget() {
        if (viewTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(viewRefresh) && !child.equals(viewLoad)) {
                    viewTarget = child;
                    break;
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled() || isAnimating || isRefreshing || isLoading || mNestedScrollInProgress) {
            return false;
        }
        if (isDraging) {
            return true;
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isDraging = false;
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float y = ev.getY();
                float yDiff = y - downY;
                if (yDiff > mTouchSlop && !canChildScrollDown()) {
                    isDragDown = true;
                    isDraging = true;
                }
                if (yDiff < -mTouchSlop && !canChildScrollUp()) {
                    isDragDown = false;
                    isDraging = true;
                }
                break;
        }
        return isDraging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || isAnimating || isRefreshing || isLoading || mNestedScrollInProgress) {
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
                    viewRefresh.setTranslationY(overScroll);
                    viewTarget.setTranslationY(overScroll);
                    viewLoad.setTranslationY(overScroll);
                    return false;
                }
                if (isDragDown && overScroll >= 0) {
                    if (overScroll > overRefreshHeight) {
                        overScroll = overRefreshHeight;
                    }
                    viewRefresh.setTranslationY(overScroll);
                    viewTarget.setTranslationY(overScroll);
                }
                if (!isDragDown && overScroll <= 0) {
                    if (Math.abs(overScroll) > overLoadHeight) {
                        overScroll = -overLoadHeight;
                    }
                    viewLoad.setTranslationY(overScroll);
                    viewTarget.setTranslationY(overScroll);
                }

                if (overScroll > 0) {
                    viewRefresh.setHeight(overScroll);
                    if (overScroll > refreshMidHeight) {
                        viewRefresh.setRefeaseToRefresh();
                    } else {
                        viewRefresh.setPullToRefresh();
                    }
                } else {
                    viewLoad.setHeight(Math.abs(overScroll));
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
        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        if ((android.os.Build.VERSION.SDK_INT < 21 && viewTarget instanceof AbsListView) || (viewTarget != null && !ViewCompat.isNestedScrollingEnabled(viewTarget))) {
        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    // NestedScrollingParent

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() && !isRefreshing && !isLoading && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        // Dispatch up to the nested parent
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mTotalUnconsumed = 0;
        mNestedScrollInProgress = true;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        // If we are in the middle of consuming, a scroll, then we want to move the spinner back up
        // before allowing the list to scroll
        if (dy > 0 && mTotalUnconsumed > 0) {
            if (dy > mTotalUnconsumed) {
                consumed[1] = dy - (int) mTotalUnconsumed;
                mTotalUnconsumed = 0;
            } else {
                mTotalUnconsumed -= dy;
                consumed[1] = dy;
            }

            // todo
//            moveSpinner(mTotalUnconsumed);
        }

        // If a client layout is using a custom start position for the circle
        // view, they mean to hide it again before scrolling the child view
        // If we get back to mTotalUnconsumed == 0 and there is more to go, hide
        // the circle so it isn't exposed if its blocking content is moved
//        if (dy > 0 && mTotalUnconsumed == 0 && Math.abs(dy - consumed[1]) > 0) {
//            mCircleView.setVisibility(View.GONE);
//        }

        // Now let our nested parent consume the leftovers
        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;
        // Finish the spinner for nested scrolling if we ever consumed any
        // unconsumed nested scroll

        // todo
//        if (mTotalUnconsumed > 0) {
//            finishSpinner(mTotalUnconsumed);
//            mTotalUnconsumed = 0;
//        }

        // Dispatch up our nested parent
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed, final int dxUnconsumed, final int dyUnconsumed) {
        // Dispatch up to the nested parent first
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow);

        // This is a bit of a hack. Nested scrolling works from the bottom up, and as we are
        // sometimes between two nested scrolling views, we need a way to be able to know when any
        // nested scrolling parent has stopped handling events. We do that by using the
        // 'offset in window 'functionality to see if we have been moved from the event.
        // This is a decent indication of whether we should take over the event stream or not.
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (dy < 0 && !canChildScrollUp()) {
            mTotalUnconsumed += Math.abs(dy);
//            moveSpinner(mTotalUnconsumed);
        }
    }

    // NestedScrollingChild

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    private void onTouchUp() {
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
                    viewRefresh.setTranslationY(height);
                    viewRefresh.setHeight(height);
                    viewTarget.setTranslationY(height);
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
        }
        animatorToRefresh.setDuration(animateDuration);
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
                    viewLoad.setTranslationY(height);
                    viewLoad.setHeight(Math.abs(height));
                    viewTarget.setTranslationY(height);
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
        }
        animatorToLoad.setDuration(animateDuration);
        animatorToLoad.start();
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
                    viewRefresh.setTranslationY(height);
                    viewRefresh.setHeight(height);
                    viewTarget.setTranslationY(height);
                    isRefreshing = false;
                    if (height == 0) {
                        isAnimating = false;
                    }
                }
            });
        }
        animatorToRefreshReset.setDuration(animateDuration);
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
                    viewLoad.setTranslationY(height);
                    viewLoad.setHeight(Math.abs(height));
                    viewTarget.setTranslationY(height);
                    isLoading = false;
                    if (height == 0) {
                        isAnimating = false;
                    }
                }
            });
        }
        animatorToLoadReset.setDuration(animateDuration);
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
        viewRefresh.setTranslationY(0);
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
