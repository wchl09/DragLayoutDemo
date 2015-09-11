package huimeitimes.com.draglayoutdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by RayWang on 15/9/11.
 */
public class DragLayout extends LinearLayout {
    /**
     * View拖拽工具
     */
    private ViewDragHelper mDragHelper;
    /**
     * 当前为打开还是关闭状态
     */
    boolean opened = false;
    /**
     * 把手
     */
    private View mHandleView;
    /**
     * 填充的View
     */
    private View mContentView;
    /**
     * 抽屉状态改变监听事件
     */
    private OnDragLayoutListener listener;
    /**
     * 把手距各个位置的距离
     */
    private int mHandleBottom = 230, mHandleTop = 230, mHandleLeft = 0;
    /**
     * 抽屉和把手的参数
     */
    private int mParentHeight, mParentWidth, mHandleHeight;
    /**
     * Enable
     */
    private boolean mEnable = true;
    /**
     * 把手和抽屉内容的ID
     */
    private int mHandleViewId, mContentViewId;
    /**
     * view移动时的TOP
     */
    private int top;

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // 初始化参数
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragLayout);
            mHandleViewId = typedArray.getResourceId(R.styleable.DragLayout_handle, 0);
            mContentViewId = typedArray.getResourceId(R.styleable.DragLayout_content, 0);
            mHandleTop = typedArray.getDimensionPixelSize(R.styleable.DragLayout_handle_Top, mHandleTop);
            mHandleBottom = typedArray.getDimensionPixelSize(R.styleable.DragLayout_handle_Bottom, mHandleBottom);
            // mHandleBottom = Tools.dipToPixel(mHandleBottom);
            // mHandleTop = Tools.dipToPixel(mHandleTop);
            typedArray.recycle();
        }
        // 初始化ViewDragHelper
        mDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            /**
             * 根据返回结果决定当前child是否可以拖拽
             *
             * @param child
             *            当前被拖拽的view
             * @param pointerId
             *            区分多点触摸的id
             * @return
             */
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return mEnable && child == mHandleView;
            }

            /**
             * 传入拖拽时手指的移动距离，返回View将要移动的距离(位置没有改变)
             */
            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                if (top < (mParentHeight >> 1)) {
                    opened = true;
                } else {
                    opened = false;
                }
                return top;
            }

            /**
             * 边界触碰
             */
            @Override
            public void onEdgeTouched(int edgeFlags, int pointerId) {
                super.onEdgeTouched(edgeFlags, pointerId);
            }

            @Override
            public void onEdgeDragStarted(int edgeFlags, int pointerId) {
                super.onEdgeDragStarted(edgeFlags, pointerId);
                mDragHelper.captureChildView(mHandleView, pointerId);
            }

            /**
             * ViewDragHelper状态改变
             */
            @Override
            public void onViewDragStateChanged(int state) {
                super.onViewDragStateChanged(state);
                if (state == ViewDragHelper.STATE_IDLE) {
                    if (listener != null) {
                        if (opened) {
                            listener.onOpened();
                        } else {
                            listener.onClosed();
                        }
                    }
                }
            }

            /***
             * 传入View位置改变之后的参数
             */
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                mContentView.layout(mHandleLeft, mHandleView.getBottom(), mParentWidth, mParentHeight);
                if (listener != null) {
                    listener.onDrag(top);
                }
                DragLayout.this.top = top;
            }

            /**
             * 手指停止拖动，传入方向向量
             */
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                float absY = Math.abs(yvel);
                // 速率如果超过2000就切换状态
                if (absY > 2000) {
                    if (yvel > 2000) {
                        mDragHelper.settleCapturedViewAt(mHandleLeft, mParentHeight - mHandleBottom - mHandleHeight);
                        opened = false;

                    } else if (yvel < -2000) {
                        mDragHelper.settleCapturedViewAt(mHandleLeft, mHandleTop);
                        opened = true;
                    }
                } else {
                    // 不超过2000就恢复状态
                    if (opened) {
                        // open();
                        mDragHelper.settleCapturedViewAt(mHandleLeft, mHandleTop);
                    } else {
                        // close();
                        mDragHelper.settleCapturedViewAt(mHandleLeft, mParentHeight - mHandleBottom - mHandleHeight);
                    }
                }
                invalidate();
            }

            /**
             * 水平方向的限制
             */
            @Override
            public int getViewHorizontalDragRange(View child) {
                return super.getViewHorizontalDragRange(child);
            }

            /**
             * 垂直方向的拖拽限制，return 0不能拖拽，大于0可以拽(当子view中onClickable=true时，要重写此方法)
             */
            @Override
            public int getViewVerticalDragRange(View child) {

                // 子View可以被点击
                return getMeasuredHeight() - child.getMeasuredHeight();
            }
        });
        // 设置触碰边接触发点
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_BOTTOM);
    }

    /**
     * 动态打开抽屉(移动把手到打开位置，不管初始位置)
     */
    public void animateOpen() {
        if (mEnable) {
            if (mDragHelper.smoothSlideViewTo(mHandleView, mHandleLeft, mHandleTop)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
            opened = true;
        }
    }

    /**
     * 计算控件大小
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mParentHeight = MeasureSpec.getSize(heightMeasureSpec);
        mParentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int childMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0, mHandleView.getLayoutParams().height);
        mHandleHeight = MeasureSpec.getSize(childMeasureSpec);
    }

    /**
     * 布局控件
     * </p>
     * 要考虑抽屉的状态
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (top == 0 && mDragHelper.getViewDragState() == ViewDragHelper.STATE_IDLE) {
            top = mParentHeight - mHandleBottom - mHandleHeight;
        }
        mHandleView.layout(mHandleLeft, top, mParentWidth, top + mHandleHeight);
        mContentView.layout(mHandleLeft, top + mHandleHeight, mParentWidth, mParentHeight);
    }

    /***
     * 动态关闭抽屉
     */
    public void animateClose() {
        if (mEnable) {
            if (mDragHelper.smoothSlideViewTo(mHandleView, mHandleLeft,
                    mParentHeight - mHandleBottom - mHandleHeight)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
            opened = false;
        }
    }

    /**
     * 动画打开/关闭抽屉
     */
    public void animateToggle() {
        if (opened) {
            // 关闭
            animateClose();
        } else {
            // 打开
            animateOpen();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHandleView = findViewById(mHandleViewId);
        mContentView = findViewById(mContentViewId);
    }

    /**
     * 交个{@link ViewDragHelper}来控制是否拦截事件
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    /**
     * {@link ViewDragHelper}控制事件
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mDragHelper.processTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    /**
     * Called by a parent to request that a child update its values for mScrollX
     * and mScrollY if necessary. This will typically be done if the child is
     * animating a scroll using a Scroller object. 需要调用invalidate();才能触发
     */
    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 设置OnDragLayoutListener
     *
     * @param listener
     */
    public void setOnDragLayoutListener(OnDragLayoutListener listener) {
        this.listener = listener;
    }

    /***
     * 抽屉是否打开
     *
     * @return
     */
    public boolean getIsOpened() {
        return opened;
    }

    /**
     * 设置是否可以操作抽屉
     *
     * @param enable
     */
    public void setEnable(boolean enable) {
        this.mEnable = enable;
    }

    /**
     * 获取抽屉是否可以操作
     *
     * @return
     */
    public boolean getEnable() {
        return mEnable;
    }

    /**
     * 抽屉状态监听事件
     */
    public interface OnDragLayoutListener {
        /**
         * 打开
         */
        public void onOpened();

        /**
         * 关闭
         */
        public void onClosed();

        /**
         * 抽屉正在被拖动时，抽屉的top
         *
         * @param top
         */
        public void onDrag(int top);
    }
}