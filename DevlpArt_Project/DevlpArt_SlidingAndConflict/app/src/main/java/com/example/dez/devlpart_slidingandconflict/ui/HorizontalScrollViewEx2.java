package com.example.dez.devlpart_slidingandconflict.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by Dez on 2018/7/23.
 */

public class HorizontalScrollViewEx2 extends ViewGroup{

    private static final String TAG = "HorizontalScrollViewEx2";
    int mLastX, mLastY;
    private int mLastXIntercept = 0;
    private int mLastYIntercept = 0;
    int mChildrenSize;
    int mChildWidth;
    int mChildIndex;
    Scroller mScroller;
    VelocityTracker mVelocityTracker;

    public HorizontalScrollViewEx2(Context context) {
        super(context);
        init();
    }

    public HorizontalScrollViewEx2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalScrollViewEx2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        mScroller = new Scroller(getContext());
        mVelocityTracker = VelocityTracker.obtain();  //构造方法是私有的
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        int x = (int)ev.getX();
        int y = (int)ev.getY();

        //触碰事件不拦截，否则则拦截
        //在触碰事件中，如果还Scroller还在滚动则拦截
        if(ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            mLastX = x;
            mLastY = y;
            if(!mScroller.isFinished())
            {
                mScroller.abortAnimation();
                return true;
            }
            return false;
        }
        else
            return true;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.d(TAG,"onTouchEvent action:" + event.getAction());
        mVelocityTracker.addMovement(event);

        int x = (int)event.getX();
        int y = (int)event.getY();

        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if(!mScroller.isFinished())
                    mScroller.abortAnimation();
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                Log.d(TAG, "move, deltaX:" + deltaX + " deltaY:" + deltaY);
                scrollBy(-deltaX,0);
                break;
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX();
                int scrollToChildIndex = scrollX / mChildWidth;
                Log.d(TAG, "current index:" + scrollToChildIndex);
                mVelocityTracker.computeCurrentVelocity(1000);
                float xVelocity = mVelocityTracker.getXVelocity();
                if(Math.abs(xVelocity) >= 50){

                    mChildIndex = xVelocity > 0?mChildIndex -1:mChildIndex+1;
                }else{
                    mChildIndex = (scrollX + mChildWidth/2) / mChildWidth;
                }
                mChildIndex = Math.max(0,Math.min(mChildIndex,mChildrenSize - 1));
                int dx = mChildIndex * mChildWidth - scrollX;
                smoothScrollBy(dx,0);
                mVelocityTracker.clear();
                Log.d(TAG, "index:" + scrollToChildIndex + " dx:" + dx);
                break;
            default:
                break;
        }
        mLastX = x;
        mLastY = y;
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = 0;
        int measuredHeight = 0;
        final int childCount = getChildCount();
        measureChildren(widthMeasureSpec,heightMeasureSpec);

        int widthSpaceSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpaceSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        if(childCount == 0)
        {
            setMeasuredDimension(0,0);
        }
        else if(heightSpecMode == MeasureSpec.AT_MOST)
        {
            measuredHeight = getChildAt(0).getMeasuredHeight();
            setMeasuredDimension(widthSpaceSize,measuredHeight);
        }
        else if(widthSpecMode == MeasureSpec.AT_MOST)
        {
            measuredWidth = getChildAt(0).getMeasuredWidth()*childCount;
            setMeasuredDimension(measuredWidth,heightSpaceSize);
        }
        else{
            final View childView = getChildAt(0);
            measuredHeight = childView.getMeasuredHeight();
            measuredWidth = childView.getMeasuredWidth();
            setMeasuredDimension(measuredWidth,measuredHeight);
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        Log.d(TAG, "width:" + getWidth());
        int childLeft = 0;
        final int childCount = getChildCount();
        mChildrenSize = childCount;

        for(int i = 0;i < childCount;++i)
        {
            final View childView = getChildAt(i);
            if(childView.getVisibility() != View.GONE) {
                final int childWidth = childView.getMeasuredWidth();
                mChildWidth = childWidth;
                childView.layout(childLeft, 0, childLeft + childWidth, childView.getMeasuredHeight());
                childLeft += childWidth;
            }
        }

    }

    private void smoothScrollBy(int dx, int dy){

        mScroller.startScroll(getScrollX(),0,dx,0,500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset())
        {
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }
}
