package com.king.refresh.widget.waterfall;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.king.refresh.R;
import com.king.refresh.widget.waterfall.internal.WaterfallAbsListView;
import com.king.refresh.widget.waterfall.internal.WaterfallAbsListView.OnScrollListener;

/**
 * 带图片下拉刷新功能的ListView,类似于新浪微博、QQ空间效果<br>
 * <br>
 * 刷新事件通过设置刷新监听器{@link OnRefreshListener}执行
 * 
 * @author 13071499
 */
public class RefreshImageWaterfallListView extends MultiColumnListView implements OnScrollListener {

	/**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;
	
	/**
     * 刷新完成
     */
    private static final int REFRESH_DONE = 0;
	
	/**
     * 下拉中(超过阀值)
     */
    private static final int REFRESH_DROPING = 1;

    /**
     * 下拉中(未超过阀值)
     */
    private static final int REFRESH_PULLING = 2;
    
    /**
     * 下拉触发刷新阀值
     */
    private static final int REFREH_HEIFHT = 40;
    
    /**
     * 头部视图回弹时间
     */
    private static final int HEADVIEW_UP_TIME = 600;
    
    /**
     * 图片初始固定填充值
     */
    private static final int IMAGE_PADDING = -180;
    
    /**
     * 实际的padding的距离与界面上偏移距离的比例
     */
    private static final int RATIO = 6;

    /**
     * 可见第一项索引
     */
    private int mFirstVisibleItem;

    /**
     * 用于保证startY的值在一个完整的touch事件中只被记录一次
     */
    private boolean isRecored;

    /**
     * 触摸按下位置Y坐标
     */
    private int mStartY;

    /**
	 * 按在屏幕上的手指的id
	 */
	private int mActivePointerId = INVALID_POINTER;
    
    /**
     * 刷新状态<br>
     * <br>
     */
    private int mRefreshState;

    /**
     * 移动高度
     */
    private int mMoveHeight;
    
    /**
     * 刷新监听器
     */
    private OnRefreshListener refreshListener;

    /**
     * 是否可刷新
     */
    private boolean isRefreshable = true;

    /**
     * 头部图片
     */
    private View mHeadImageView;
    
    /**
     * 滑动操作线程
     */
    private SmoothScrollRunnable mCurrentSmoothScrollRunnable;
    
    /**
     * 滑动剩余延迟时间
     */
    private int mDelayMillis;
    
    /**
     * 是否正在刷新
     */
    private boolean isRefreshing;
    
    /**
     * 头部图片
     */
    private ImageView mHeadImage;
    
    /**
     * 刷新进度条
     */
    private ProgressBar mProgressBar;
    
    /**
     * 固定的头部透明视图
     */
    private View mFixedHeadView;
    
    /**
     * 背景视图
     */
    private View mBackgroundView;
    
    /**
     * 固定的头部透明视图高度
     */
    private int mFixedHeadViewHeight;
    
    /**
     * 新按下的手指的y坐标
     */
    private int mPointDownY;
    
    public RefreshImageWaterfallListView(Context context) {
        this(context, null);
    }

    public RefreshImageWaterfallListView(Context context, AttributeSet attrs) {
    	this(context, attrs, 0);
    }

    public RefreshImageWaterfallListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initHeadView(context);
	}

	/**
     * 功能描述: 初始化头部View控件<br>
     * 
     * @param context
     */
    private void initHeadView(Context context) {
//         设置ListView滑动到尽头无阴影
        if (android.os.Build.VERSION.SDK_INT >= 9) {
            this.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }

        mHeadImageView = LayoutInflater.from(context).inflate(R.layout.util_image_refresh_header, null);
        mHeadImage = (ImageView) mHeadImageView.findViewById(R.id.head_image);
        
        // 设置初始填充
        mHeadImageView.setPadding(0, IMAGE_PADDING, 0, IMAGE_PADDING);
        
        // 添加头部视图
        addHeaderView(mHeadImageView, null, false);
    
        setOnScrollListener(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (isRefreshable) { 
	    	switch (event.getAction()) {
	    	 	case MotionEvent.ACTION_DOWN:
		        if (mFirstVisibleItem == 0 && mRefreshState == REFRESH_DONE && mHeadImageView.getTop() == 0) {
		            isRecored = true;
		
		            // 第一次触摸屏幕的位置
		            mStartY = (int) event.getY();
		            
		            mActivePointerId = event.getPointerId(0);
		            
//		            return true;
		            // Log.v(TAG, "在down时候记录当前位置" + " startY:"+mStartY);
		        }
		        break;
		     }
    	}
    	return super.onInterceptTouchEvent(event);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isRefreshable) {

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_CANCEL:
                	mHeadImageView.setPadding(0, IMAGE_PADDING, 0, IMAGE_PADDING);
                	setFixedHeadMargins(0, -mFixedHeadViewHeight + IMAGE_PADDING, 0, 0);
                	mRefreshState = REFRESH_DONE;
                	// 重置手指触摸id
        			mActivePointerId = INVALID_POINTER;
        			mStartY = 0;
        			mPointDownY = 0;
                    isRecored = false;
                    break;

                case MotionEvent.ACTION_UP:
                	
                	mMoveHeight = mHeadImageView.getPaddingTop() - IMAGE_PADDING;
                	if(mRefreshState == REFRESH_DROPING){
                		smoothScroll(HEADVIEW_UP_TIME);
                		if(!isRefreshing){
                			onRefresh();
                			isRefreshing = true;
                			if(mProgressBar != null){
                				mProgressBar.setVisibility(View.VISIBLE);
                			}
                		}
                	}
                	if(mRefreshState == REFRESH_PULLING){
                		smoothScroll(HEADVIEW_UP_TIME);
                	}
                	// 重置手指触摸id
        			mActivePointerId = INVALID_POINTER;
        			mStartY = 0;
        			mPointDownY = 0;
                    isRecored = false;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                	if(mActivePointerId != INVALID_POINTER){
                		mPointDownY = (int) event.getY(event.findPointerIndex(mActivePointerId));
                	}
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                	onSecondaryPointerUp(event);
                    break;    
                case MotionEvent.ACTION_MOVE:
                	final int activePointerIndex = event.findPointerIndex(mActivePointerId);
                    if (activePointerIndex == -1) {
                        break;
                    }
                    int tempY = (int) event.getY(activePointerIndex);

                    if (isRecored) {
                        /**
                         * 保证在设置padding的过程中，当前的位置一直是在head， 否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动
                         */

                        // 如果此时状态为REFRESH_DROPING（释放后便可刷新）
                        if (mRefreshState == REFRESH_DROPING) {
                            // 选择头部视图
                            setSelection(0);

                            // 如果继续向上滑动，头部视图即将有部分被遮住时，改变状态为REFRESH_PULLING（释放后不刷新）
                            if (((tempY - mStartY) / RATIO < REFREH_HEIFHT) && (tempY - mStartY) > 0) {
                                mRefreshState = REFRESH_PULLING;
                                // Log.v(TAG, "由松开刷新状态转变到下拉刷新状态");
                            }

                            // 如果一下子滑动到顶部，将头部视图完全遮住,应该恢复DONE状态,这里机率很小
                            else if (tempY - mStartY <= 0) {
                                mRefreshState = REFRESH_DONE;
                                // Log.v(TAG, "---由松开刷新状态转变到done状态");
                            }

                            // 如果继续向下拉，渐增头部填充高度即可
                            else {
                                // 不用进行特别的操作，只用更新paddingTop的值就行了
                            }
                        }

                        // 如果此时状态为REFRESH_PULLING（释放后不刷新）
                        if (mRefreshState == REFRESH_PULLING) {
                            // 选择头部视图
                            setSelection(0);

                            /**
                             * 继续下拉可以进入REFRESH_DROPING的状态
                             * 
                             * 如果头部视图能够完全显示或者超出显示, 需要更改状态: mRefreshState = REFRESH_DROPING
                             */
                            if ((tempY - mStartY) / RATIO >= REFREH_HEIFHT) {
                                mRefreshState = REFRESH_DROPING;
                                // Log.v(TAG, "由done或者下拉刷新状态转变到松开刷新");
                            }

                            // 如果一下子滑动到顶部，将头部视图完全遮住,应该恢复DONE状态
                            else if (tempY - mStartY <= 0) {
                                mRefreshState = REFRESH_DONE;
                                // Log.v(TAG, "由done或者下拉刷新状态转变到done状态");
                            }
                        }

                        // 刷新完成状态
                        if (mRefreshState == REFRESH_DONE) {
                        	if(mBackgroundView != null && mBackgroundView.getVisibility() == View.VISIBLE){
                        		mBackgroundView.setVisibility(View.INVISIBLE);
                        	}
                            // 如果是下拉动作，头部视图即将有部分被遮住时，改变状态为REFRESH_PULLING（释放后不刷新）
                            if (tempY - mStartY > 0) {
                                mRefreshState = REFRESH_PULLING;
                            }
                        }
                        // 控制滑动距离，越往下滑动滑动距离越小
                        if(tempY - mStartY >= -IMAGE_PADDING * RATIO){
                        	double ins = Math.pow((double)(tempY - mStartY + IMAGE_PADDING * RATIO), 1.0/3);
                        	tempY = mStartY - IMAGE_PADDING * RATIO + (int)(ins * ins * 3);
                        }
                        
                        // 填充头部视图，更新paddingTop
                        if (mRefreshState == REFRESH_PULLING) {
                        	int paddingTop = (tempY - mStartY) / RATIO + IMAGE_PADDING;
                        	int paddingbottom = paddingTop <= 0 ? paddingTop : 0;
                        	mHeadImageView.setPadding(0, paddingTop, 0, paddingbottom);
                        	setFixedHeadMargins(0, -mFixedHeadViewHeight+paddingbottom, 0, 0);
                        }

                        // 继续更新headView的paddingTop
                        if (mRefreshState == REFRESH_DROPING) {
                        	
                        	if(mBackgroundView != null && mBackgroundView.getVisibility() != View.VISIBLE){
                        		mBackgroundView.setVisibility(View.VISIBLE);
                        	}
                        	int paddingTop = (tempY - mStartY) / RATIO + IMAGE_PADDING;
//                        	Log.v("test", "paddingTop  "+paddingTop);
                        	int paddingbottom = paddingTop <= 0 ? paddingTop : 0;
                        	mHeadImageView.setPadding(0, paddingTop, 0, paddingbottom);
                        	setFixedHeadMargins(0, -mFixedHeadViewHeight + paddingbottom, 0, 0);
                        }
                    }
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 功能描述: 设置可见第一列<br>
     * 
     * @param mFirstVisibleItem
     */
    public void setmFirstVisibleItem(int mFirstVisibleItem) {
        this.mFirstVisibleItem = mFirstVisibleItem;
    }
    
    /**
     * 功能描述:设置刷新监听器 <br>
     */
    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    /**
     * 功能描述: 刷新完成操作<br>
     * <br>
     * 通过 flag 判断刷新是否成功
     * 
     * @param flag
     */
    public void onRefreshComplete(boolean flag) {
    	isRefreshing = false;
    	if(mProgressBar != null){
    		mProgressBar.setVisibility(View.INVISIBLE);
    	}
    }

    /**
     * 功能描述: 设置下拉刷新是否可用<br>
     *
     * @param isEnable
     */
    public void setRefreshable(boolean isEnable){
    	isRefreshable = isEnable;
    }
    
    /**
     * 功能描述: 设置头部图片<br>
     *
     * @param resId
     */
    public void setHeadImage(int resId){
    	mHeadImage.setImageResource(resId);
    }
    
    /**
     * 功能描述: 设置头部图片<br>
     *
     * @param bitmap
     */
    public void setHeadImage(Bitmap bitmap){
    	mHeadImage.setImageBitmap(bitmap);
    }
    
    /**
     * 功能描述: 设置刷新进度条<br>
     *
     * @param progressBar
     */
    public void setPrograssBar(ProgressBar progressBar){
    	this.mProgressBar = progressBar;
    }
    
    /**
     * 功能描述: 执行刷新操作<br>
     */
    private void onRefresh() {
        if (refreshListener != null) {
            refreshListener.onRefresh();
        }
    }

    /**
	 * 功能描述: 更新触摸的活动手指id<br>
	 *
	 * @param ev
	 */
	private void onSecondaryPointerUp(MotionEvent ev) {
		final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		final int pointerId = ev.getPointerId(pointerIndex);
		if (pointerId == mActivePointerId) {
			// This was our active pointer going up. Choose a new
			// active pointer and adjust accordingly.
			// TODO: Make this decision more intelligent.
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mActivePointerId = ev.getPointerId(newPointerIndex);
			if(pointerId == 0){
				mStartY =  mStartY + (int)(ev.getY(ev.findPointerIndex(mActivePointerId)) - ev.getY());
			}else{
				mStartY =  mStartY + (int)(ev.getY() - mPointDownY);
			}
		}
		
	}
    
    /**
     * 功能描述: 设置固定的头部视图<br>
     *
     * @param view
     */
    public void setFixedHeaderView(View view){
    	mFixedHeadView = view;
    	
    	// 测量视图宽高
    	measureView(view);
    	mFixedHeadViewHeight = view.getMeasuredHeight();
    	
    	// 添加视图
    	((ViewGroup)mHeadImageView).addView(view);
    	
    	// 设置初始margin
    	setFixedHeadMargins(0, -mFixedHeadViewHeight + IMAGE_PADDING, 0, 0);
    }
    
    public void notifyFixedHeadView(int changes){
    	mFixedHeadViewHeight += changes;
    	// 设置初始margin
    	setFixedHeadMargins(0, -mFixedHeadViewHeight + IMAGE_PADDING, 0, 0);
    }
    
    public void setBackgroundView(View view){
    	if(view != null){
    		mBackgroundView = view;
    	}
    }
    
    /**
     * 功能描述:设置固定头部Margins值 <br>
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setFixedHeadMargins(int left, int top, int right, int bottom){
    	// 设定margin
    	ViewGroup.LayoutParams lp = mFixedHeadView.getLayoutParams();
    	if(lp instanceof LinearLayout.LayoutParams){
    		((LinearLayout.LayoutParams)lp).setMargins(left, top, right, bottom);
    	}else if(lp instanceof RelativeLayout.LayoutParams){
    		((RelativeLayout.LayoutParams)lp).setMargins(left, top, right, bottom);
    	}else if(lp instanceof FrameLayout.LayoutParams){
    		((FrameLayout.LayoutParams)lp).setMargins(left, top, right, bottom);
    	}
    	mFixedHeadView.setLayoutParams(lp);
    }
    
    /**
     * 头部隐藏操作线程<br>
     * 
     * @author 13071499
     */
    public final class SmoothScrollRunnable implements Runnable {

        /**
         * 滚动时间
         */
        public int mScrollTime;

        public SmoothScrollRunnable(int time) {
            this.mScrollTime = time;
        }

        @Override
        public void run() {
            mDelayMillis += 100;
            // 填充高度
            int paddingTop = mMoveHeight * (mScrollTime - mDelayMillis) / mScrollTime + IMAGE_PADDING;
            int paddingbottom = paddingTop <= 0 ? paddingTop : 0;
            mHeadImageView.setPadding(0, paddingTop, 0, paddingbottom);
            setFixedHeadMargins(0, -mFixedHeadViewHeight+paddingbottom, 0, 0);
            smoothScroll(mScrollTime);
            mHeadImageView.invalidate();
        }

        public void stop() {
            removeCallbacks(this);
        }
    }
    
    /**
     * 功能描述:头部视图滚动隐藏 <br>
     * 
     * @param time
     */
    private void smoothScroll(int time) {
        // 如果新线程实例不存在，新建一个；如果存在，停止当前线程操作
        if (null != mCurrentSmoothScrollRunnable) {
            mCurrentSmoothScrollRunnable.stop();
        }
        mCurrentSmoothScrollRunnable = new SmoothScrollRunnable(time);

        if(mDelayMillis < time){
        	postDelayed(mCurrentSmoothScrollRunnable, time / 100);
        }else{
        	mDelayMillis = 0;
        	mRefreshState = REFRESH_DONE;
        }
    }
    
    public View getHeadImageView(){
    	return mHeadImageView;
    }
    
    /**
     * 功能描述: 测量视图宽高，以便获取其宽高<br>
     * 
     * @param child
     */
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }
    
    /**
     * 下拉刷新监听器<br>
     * 
     * @author 13071499
     */
    public interface OnRefreshListener {
        void onRefresh();
    }
    
    public interface OnNewScrollListener{
    	void onScroll(WaterfallAbsListView view, int firstVisibleItem,
    			int visibleItemCount, int totalItemCount);
    	
    	void onScrollStateChanged(WaterfallAbsListView view, int scrollState);
    }

	@Override
	public void onScrollStateChanged(WaterfallAbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScroll(WaterfallAbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.mFirstVisibleItem = firstVisibleItem;
	}
}