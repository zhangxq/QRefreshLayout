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
    private static final int N_DOWN = 1; //正常下拉
    private static final int N_UP = 2; // 正常上拉
    private static final int R_UP = 3; // 刷新中上拉
    private static final int L_DOWN = 4; // 加载中下拉

    private View viewTarget; // 刷新目标

    // 滑动事件相关参数
    private float lastMoveY; // 上次移动到的位置y
    private float overScroll; // 上拉和下拉的距离
    private int dragMode; // 拖动模式
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
    private final int viewContentHeight = 2000; // 刷新动画内容区高度
    private int refreshMidHeight = 170; // 刷新高度，超过这个高度，松手即可刷新
    private int loadMidHeight = 170; // 加载更多高度，超过这个高度，松手即可加载更多
    private int refreshHeight = 150; // 刷新动画高度
    private int loadHeight = 110; // 加载更多动画高度
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
    private ListScrollListener listScrollListener;

    public QRefreshLayout(Context context) {
        this(context, null);
    }

    public QRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        ensureTarget();
    }

    /**
     * 设置下拉到"释放即可更新"的高度（默认170px）
     *
     * @param height
     */
    public void setPullToRefreshHeight(int height) {
        refreshMidHeight = height;
    }

    /**
     * 设置上拉到"释放即可加载更多"的高度（默认170px）
     *
     * @param height
     */
    public void setLoadToRefreshHeight(int height) {
        loadMidHeight = height;
    }

    /**
     * 设置下拉刷新动画高度（默认150px，需要在setRefreshing之前调用）
     *
     * @param height
     */
    public void setRefreshHeight(int height) {
        refreshHeight = height;
    }

    /**
     * 设置加载更多动画高度（默认110px）
     *
     * @param height
     */
    public void setLoadHeight(int height) {
        loadHeight = height;
    }

    /**
     * 设置是否可以加载更多
     *
     * @param isEnable
     */
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
        ensureTarget();
        if (refreshing) {
            if (isRefreshing || isLoading || dragMode != 0) return;
            if (!isRefreshing) {
                animateToRefresh();
            }
        } else {
            isRefreshing = false;
            if (overScroll >= 0) {
                animateToRefreshReset();
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
        if (!isLoadEnable) return;
        ensureTarget();
        if (loading) {
            if (isLoading || isRefreshing || dragMode != 0) return;
            if (!isLoading) {
                animateToLoad();
            }
        } else {
            isLoading = false;
            if (overScroll <= 0) {
                animateToLoadReset();
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

    /**
     * 设置ListView滚动监听
     *
     * @param listener
     */
    public void setListViewScrollListener(ListScrollListener listener) {
        this.listScrollListener = listener;
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
                    viewTarget.setClickable(true);
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
                    if (listScrollListener != null) {
                        listScrollListener.onScrollStateChanged(view, scrollState);
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (listScrollListener != null) {
                        listScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                    }
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
        if (!isEnabled() || isAnimating || isNestedScrolling) {
            return false;
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isTouchDown = true;
                lastMoveY = ev.getY();
                dragMode = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                final float y = ev.getY();
                float yDiff = y - lastMoveY;
                if (yDiff == 0) return false;
                if (yDiff < 0) isPullingUp = true;
                if (yDiff > 0) { // 下拉
                    if (overScroll < 0 && isLoading) {
                        dragMode = L_DOWN;
                    } else if (!canChildScrollDown()) {
                        dragMode = N_DOWN;
                    }
                } else { // 上拉
                    if (overScroll > 0 && isRefreshing) {
                        dragMode = R_UP;
                    } else if (!canChildScrollUp()) {
                        if (isLoadEnable) {
                            dragMode = N_UP;
                        }
                    }
                }
                if (dragMode != 0) {
                    lastMoveY = ev.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
                isTouchDown = false;
                break;
        }
        return dragMode != 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || isAnimating || isNestedScrolling) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dragMode = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                overScroll += (y - lastMoveY) * dragRate;
                lastMoveY = y;
                switch (dragMode) {
                    case N_DOWN:
                        if (overScroll < 0) {
                            overScroll = 0;
                            viewRefreshContainer.setTranslationY(overScroll);
                            viewTarget.setTranslationY(overScroll);
                            return false;
                        }
                        if (overScroll > viewContentHeight / 2) {
                            overScroll = viewContentHeight / 2;
                        }
                        viewRefreshContainer.setTranslationY(overScroll / 2);
                        viewTarget.setTranslationY(overScroll);
                        if (!isRefreshing) {
                            viewRefresh.setHeight(overScroll, refreshMidHeight, viewContentHeight);
                        }
                        if (!isRefreshing) {
                            if (overScroll > refreshMidHeight) {
                                viewRefresh.setRefeaseToRefresh();
                            } else {
                                viewRefresh.setPullToRefresh();
                            }
                        }
                        break;
                    case N_UP:
                        if (overScroll > 0) {
                            overScroll = 0;
                            viewLoadContainer.setTranslationY(overScroll);
                            viewTarget.setTranslationY(overScroll);
                            return false;
                        }
                        if (Math.abs(overScroll) > viewContentHeight / 2) {
                            overScroll = -viewContentHeight / 2;
                        }
                        viewLoadContainer.setTranslationY(overScroll / 2);
                        viewTarget.setTranslationY(overScroll);
                        if (!isLoading) {
                            viewLoad.setHeight(Math.abs(overScroll), loadMidHeight, viewContentHeight);
                        }
                        if (!isLoading) {
                            if (overScroll < -loadMidHeight) {
                                viewLoad.setRefeaseToRefresh();
                            } else {
                                viewLoad.setPullToRefresh();
                            }
                        }
                        break;
                    case R_UP:
                        if (overScroll < 0) {
                            overScroll = 0;
                            viewRefreshContainer.setTranslationY(overScroll);
                            viewTarget.setTranslationY(overScroll);
                            return false;
                        }
                        viewRefreshContainer.setTranslationY(overScroll / 2);
                        viewTarget.setTranslationY(overScroll);
                        break;
                    case L_DOWN:
                        if (overScroll > 0) {
                            overScroll = 0;
                            viewTarget.setTranslationY(overScroll);
                            viewLoadContainer.setTranslationY(overScroll);
                            return false;
                        }
                        viewTarget.setTranslationY(overScroll);
                        viewLoadContainer.setTranslationY(overScroll / 2);
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onTouchUp();
                dragMode = 0;
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
            if (!isLoadEnable) return;
            if (overScroll < -viewContentHeight / 2) {
                overScroll = -viewContentHeight / 2;
            }
            viewLoadContainer.setTranslationY(overScroll / 2);
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
                if (!isRefreshing) {
                    animateToRefreshReset();
                }
            }
        } else {
            if (!isLoadEnable) return;
            if (overScroll < -loadMidHeight) {
                animateToLoad();
            } else {
                if (!isLoading) {
                    animateToLoadReset();
                }
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
                    if (!isRefreshing) {
                        viewRefresh.setHeight(overScroll, refreshMidHeight, viewContentHeight / 2);
                    }
                    viewTarget.setTranslationY(overScroll);
                    if (height == refreshHeight) {
                        if (!isRefreshing) {
                            viewRefresh.setRefresh();
                            isRefreshing = true;
                            if (refreshListener != null) {
                                refreshListener.onRefresh();
                            }
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
                    viewLoadContainer.setTranslationY(overScroll / 2);
                    if (!isLoading) {
                        viewLoad.setHeight(Math.abs(overScroll), loadMidHeight, viewContentHeight / 2);
                    }
                    viewTarget.setTranslationY(overScroll);
                    if (height == -loadHeight) {
                        if (!isLoading) {
                            viewLoad.setRefresh();
                            isLoading = true;
                            if (loadListener != null) {
                                loadListener.onLoad();
                            }
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
        if (overScroll == 0) {
            isRefreshing = false;
        } else {
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
    }

    /**
     * 动画移动到加载更多初始位置
     */
    private void animateToLoadReset() {
        if (overScroll == 0) {
            isLoading = false;
        } else {
            if (isAnimating) return;
            isAnimating = true;
            if (animatorToLoadReset == null) {
                animatorToLoadReset = ValueAnimator.ofFloat(overScroll, 0);
                animatorToLoadReset.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float height = (float) animation.getAnimatedValue();
                        overScroll = height;
                        viewLoadContainer.setTranslationY(overScroll / 2);
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
        viewLoadContainer.setTranslationY(0);
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

    public interface ListScrollListener {
        void onScrollStateChanged(AbsListView view, int scrollState);

        void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount);
    }
}
