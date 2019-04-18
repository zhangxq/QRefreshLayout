package com.zhangxq.myswiperefreshlayout.refreshLayout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.widget.ListViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by zhangxiaoqi on 2019/4/16.
 */

public class MySwipeRefreshLayout extends ViewGroup {
    // 刷新目标
    private View mTarget;

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
    private int animateDuration = 100; // 动画时间，100ms

    private OnRefreshListener refreshListener;
    private OnLoadListener loadListener;

    public MySwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        final View child = mTarget;
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
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        mTarget.measure(
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
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(viewRefresh) && !child.equals(viewLoad)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled() || isAnimating || isRefreshing || isLoading /*|| mNestedScrollInProgress*/) {
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
        if (!isEnabled() || isAnimating || isRefreshing || isLoading /*|| mNestedScrollInProgress*/) {
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
                    mTarget.setTranslationY(overScroll);
                    viewLoad.setTranslationY(overScroll);
                    return false;
                }
                if (isDragDown && overScroll >= 0) {
                    if (overScroll > overRefreshHeight) {
                        overScroll = overRefreshHeight;
                    }
                    viewRefresh.setTranslationY(overScroll);
                    mTarget.setTranslationY(overScroll);
                }
                if (!isDragDown && overScroll <= 0) {
                    if (Math.abs(overScroll) > overLoadHeight) {
                        overScroll = -overLoadHeight;
                    }
                    viewLoad.setTranslationY(overScroll);
                    mTarget.setTranslationY(overScroll);
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
        ValueAnimator animator = ValueAnimator.ofFloat(Math.abs(overScroll), refreshHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float height = (float) animation.getAnimatedValue();
                overScroll = height;
                viewRefresh.setTranslationY(height);
                viewRefresh.setHeight(height);
                mTarget.setTranslationY(height);
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
        animator.setDuration(animateDuration);
        animator.start();
    }

    /**
     * 动画移动到加载更多位置
     */
    private void animateToLoad() {
        if (isAnimating) return;
        isAnimating = true;
        ValueAnimator animator = ValueAnimator.ofFloat(overScroll, -loadHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float height = (float) animation.getAnimatedValue();
                overScroll = height;
                viewLoad.setTranslationY(height);
                viewLoad.setHeight(Math.abs(height));
                mTarget.setTranslationY(height);
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
        animator.setDuration(animateDuration);
        animator.start();
    }

    /**
     * 动画移动到刷新初始位置
     */
    private void animateToRefreshReset() {
        if (isAnimating) return;
        isAnimating = true;
        ValueAnimator animator = ValueAnimator.ofFloat(Math.abs(overScroll), 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float height = (float) animation.getAnimatedValue();
                overScroll = height;
                viewRefresh.setTranslationY(height);
                viewRefresh.setHeight(height);
                mTarget.setTranslationY(height);
                isRefreshing = false;
                if (height == 0) {
                    isAnimating = false;
                }
            }
        });
        animator.setDuration(animateDuration);
        animator.start();
    }

    /**
     * 动画移动到加载更多初始位置
     */
    private void animateToLoadReset() {
        if (isAnimating) return;
        isAnimating = true;
        ValueAnimator animator = ValueAnimator.ofFloat(overScroll, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float height = (float) animation.getAnimatedValue();
                overScroll = height;
                viewLoad.setTranslationY(height);
                viewLoad.setHeight(Math.abs(height));
                mTarget.setTranslationY(height);
                isLoading = false;
                if (height == 0) {
                    isAnimating = false;
                }
            }
        });
        animator.setDuration(animateDuration);
        animator.start();
    }

    /**
     * 目标是否可以向下滚动
     *
     * @return
     */
    private boolean canChildScrollDown() {
        if (mTarget instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) mTarget, -1);
        }
        return mTarget.canScrollVertically(-1);
    }

    /**
     * 目标是否可以向上滚动
     *
     * @return
     */
    private boolean canChildScrollUp() {
        if (mTarget instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) mTarget, 1);
        }
        return mTarget.canScrollVertically(1);
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    public interface OnLoadListener {
        void onLoad();
    }
}
