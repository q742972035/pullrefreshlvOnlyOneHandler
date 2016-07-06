package com.test.pullrefreshlvdemo.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by 13798 on 2016/7/2.
 */
public class PullRefreshListView extends ListView implements AbsListView.OnScrollListener{
    private Context mContext;
    /**
     * listview的头部
     */
    private View headerView;
    /**
     * listview的脚部
     */
    private View footerView;

    /**
     * 头部高度
     */
    private int headerHeight;

    /**
     * 下拉刷新状态
     */
    public static final int PULL_REFRESH = 0x001;

    /**
     * 松开刷新状态
     */
    public static final int LOOSEN_REFRESH = 0x002;

    /**
     * 正在刷新状态
     */
    public static final int REFRESHING = 0x003;

    /**
     * 正在加载
     */
    public static final int LOADING = 0x004;

    /**
     * 初始化状态
     */
    private int mState = PULL_REFRESH;
    private ValueAnimator valueAnimator;
    private int footerHeight;
    private int endHeight;
    private boolean isPulling;

    public PullRefreshListView(Context context) {
        super(context);
        mContext = context;
        setOnScrollListener(this);
        initHeight();
    }

    public PullRefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setOnScrollListener(this);
        initHeight();
    }

    public PullRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOnScrollListener(this);
        initHeight();
    }

    /**
     * 可见item高度
     */
    private int visiableItemHeights;

    public void initHeight() {
        hasScroll = false;
        visiableItemHeights = 0;
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                for (int i = 0; i < getChildCount(); i++) {
                    visiableItemHeights += getChildAt(i).getHeight();
                }
            }
        });
    }


    /**
     * 设置头部
     *
     * @param view
     */
    private void setHeaderView(View view) {
        headerView = view;
        headerView.measure(0, 0);
        headerView.setPadding(0, -headerView.getMeasuredHeight(), 0, 0);
        headerHeight = headerView.getPaddingTop();
        addHeaderView(headerView);
    }

    private float mDamp = 1.0f;

    /**
     * 设置阻尼系数，默认为1.0
     */
    private void setDamp(float damp) {
        mDamp = damp;
    }

    /**
     * 通过偏移量移动头部
     *
     * @param dY
     */
    private void scrollHeaderBy(int dY) {
        int paddingTop = headerView.getPaddingTop();
        int end = paddingTop + dY;
        headerView.setPadding(0, end <= headerHeight ? headerHeight : end, 0, 0);
    }


    /**
     * 设置脚部
     *
     * @param view
     */
    private void setFooterView(View view) {
        footerView = view;
        footerView.measure(0, 0);
        footerView.setPadding(0, 0, 0, -footerView.getMeasuredHeight());
        footerHeight = footerView.getPaddingBottom();
        addFooterView(footerView);
    }

    private int mDuration = 500;

    /**
     * 设置头部返回动画执行时间
     *
     * @param duration
     */
    private void setDuration(int duration) {
        mDuration = duration;
    }

    /**
     * 关闭下拉刷新
     */
    private void closeRefreshing() {
        isLoading = false;
        setSelection(1);
        initHeight();
        if (mState != PULL_REFRESH)
            setState(PULL_REFRESH);
        headerView.setPadding(0, headerHeight, 0, 0);
        if (mCallback != null)
            mCallback.stopRefresh();
    }


    private boolean isLoading;

    /**
     * 关闭正在加载
     */
    private void closeLoading() {
        isLoading = false;
        if (mState != PULL_REFRESH)
            setState(PULL_REFRESH);
        footerView.setPadding(0, 0, 0, footerHeight);
        if (mCallback != null)
            mCallback.stopLoad();
    }

    private boolean isUp = true;
    private int lastY;
    private int lastPadding;
    /**
     * 用于判断滑动，为true后将不变
     */
    private boolean hasScroll;

    /**
     * 不使用actiondown,避免adapter的点击事件与其冲突
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (headerView == null || mState == LOADING)
            return super.onTouchEvent(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (isUp) {
                    if (!hasScroll)
                        hasScroll = true;

                    isUp = false;
                    // TODO 这里进行down操作
                    lastY = (int) ev.getY();
                    if (valueAnimator != null && valueAnimator.isRunning())
                        valueAnimator.cancel();

                }
                isPulling = ev.getY() - lastY > 0;
                float dY = (ev.getY() - lastY) * mDamp;
                lastY = (int) ev.getY();
                int paddingTop = (int) (headerView.getPaddingTop() + dY);


                if (paddingTop > headerHeight && getFirstVisiblePosition() == 0) {
                    if (isPulling && mState == REFRESHING && paddingTop > 0) {
                        return true;
                    }

                    scrollHeaderBy((int) dY);
                    // 露出头的百分比，超过1转松开刷新
                    float percent = (headerView.getPaddingTop() + Math.abs(headerHeight)) * 1.0f / Math.abs(headerHeight);
                    if (mCallback != null) {
                        mCallback.drag(percent, (int) dY);
                        mCallback.dragToLoosen(percent <= 1 ? percent : 1, percent <= 1 ? (int) dY : 0);
                    }
                    if (percent <= 1) {
                        if (mState == LOOSEN_REFRESH)
                            setState(PULL_REFRESH);
                    } else {
                        if (mState != LOOSEN_REFRESH)
                            setState(LOOSEN_REFRESH);
                    }
                    return true;
                }

                break;
            case MotionEvent.ACTION_UP:
                isUp = true;
                // 最终位置
                endHeight = -1;
                if (mState == PULL_REFRESH)
                    endHeight = headerHeight;
                else if (mState == LOOSEN_REFRESH)
                    endHeight = 0;

                if (endHeight != -1) {
                    valueAnimator = ValueAnimator.ofInt(headerView.getPaddingTop(), endHeight);
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int padding = (int) animation.getAnimatedValue();
                            lastPadding = padding;
                            float percent = (padding + Math.abs(headerHeight)) * 1.0f / Math.abs(headerHeight);
                            if (mCallback != null) {
                                mCallback.drag(percent, padding - lastPadding);
                            }
                            if (padding < 0) {
                                if (mCallback != null)
                                    mCallback.dragToLoosen(percent, padding - lastPadding);
                            }
                            headerView.setPadding(0, padding, 0, 0);
                        }
                    });
                    valueAnimator.addListener(new SimpleAnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (endHeight == 0) {
                                setState(REFRESHING);
                            } else if (endHeight == headerHeight)
                                setState(PULL_REFRESH);
                        }
                    });

                    valueAnimator.setDuration(mDuration);
                    valueAnimator.start();
                }

                break;
        }

        return super.onTouchEvent(ev);
    }

    private void setState(int state) {
        if (isLoading)
            state = LOADING;


        switch (state) {
            // 下拉刷新状态
            case PULL_REFRESH:
                if (mState != PULL_REFRESH && headerView != null) {
                    mState = PULL_REFRESH;
                    if (mCallback != null)
                        mCallback.toRullRefresh();
                }
                break;
            // 松开刷新状态
            case LOOSEN_REFRESH:
                if (mState != LOOSEN_REFRESH && headerView != null) {
                    mState = LOOSEN_REFRESH;
                    if (mCallback != null)
                        mCallback.toLoosenRefresh();
                }
                break;
            // 正在刷新状态
            case REFRESHING:
                if (mState != REFRESHING && headerView != null) {
                    mState = REFRESHING;
                    if (mCallback != null)
                        mCallback.toRefreshing();
                }
                break;
            case LOADING:
                if (!isLoading) {
                    footerView.setPadding(0, 0, 0, 0);
                    isLoading = true;
                    if (mState != LOADING && footerView != null) {
                        mState = LOADING;
                        if (mCallback != null)
                            mCallback.toLoading();
                    }
                }
                break;
            default:
                break;
        }
    }


    /**
     * 避免繁琐的判断
     */
    private StateCallBack mCallback;

    public void setStateCallBace(StateCallBack callback) {
        mCallback = callback;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (headerView != null) {
            if (getFirstVisiblePosition() >= 1 && scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                headerView.setPadding(0, headerHeight, 0, 0);
                if (mState == REFRESHING)
                    headerView.setPadding(0, 0, 0, 0);
            }
        }

        if (scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            if (mCallback!=null)
                mCallback.scroll(view,scrollState);
        }

        if (footerView == null || !hasScroll || visiableItemHeights < getHeight())
            return;

        if (getLastVisiblePosition() == getCount() - 1 && mState != REFRESHING && scrollState==SCROLL_STATE_IDLE) {
            if ( !isLoading) {
                setState(LOADING);
                setSelection(getCount());
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    public interface StateCallBack {
        /**
         * 从下拉刷新到松开刷新的瞬间
         */
        void toLoosenRefresh();

        /**
         * 从松开刷新到下拉刷新的瞬间
         */
        void toRullRefresh();

        /**
         * toLoading
         * 状态为正在刷新
         */
        void toRefreshing();

        /**
         * 状态为正在加载
         */
        void toLoading();

        /**
         * 从下拉刷新拖拽到松开刷新的移动百分比
         *
         * @param percent 0-1
         * @param dY      调用间隔的偏移量
         */
        void dragToLoosen(float percent, int dY);

        /**
         * 头部有高度后移动的百分比
         *
         * @param percent 0-N
         * @param dY      调用间隔的偏移量
         */
        void drag(float percent, int dY);

        /**
         * 停止正在刷新（被动执行）
         */
        void stopRefresh();

        /**
         * 停止正在加载（被动执行）
         */
        void stopLoad();

        void scroll(AbsListView view, int scrollState);

    }

    class SimpleAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    public class Builder {
        private PullRefreshListView mLv;

        public Builder(PullRefreshListView listView) {
            mLv = listView;
        }

        /**
         * 这是头布局
         */
        public Builder setHeaderView(View view) {
            mLv.setHeaderView(view);
            return this;
        }

        /**
         * 这是脚布局
         */
        public Builder setFooterView(View view) {
            mLv.setFooterView(view);
            return this;
        }

        /**
         * 设置动画时间
         */
        public Builder setDuration(int duration) {
            mLv.setDuration(duration);
            return this;
        }

        /**
         * 设置滑动时阻尼系数
         */
        public Builder setDamp(float damp) {
            mLv.setDamp(damp);
            return this;
        }

        /**
         * 关闭加载
         */
        public Builder closeLoading() {
            mLv.closeLoading();
            return this;
        }

        /**
         * 关闭刷新
         */
        public Builder closeRefreshing() {
            mLv.closeRefreshing();
            return this;
        }
    }
}
