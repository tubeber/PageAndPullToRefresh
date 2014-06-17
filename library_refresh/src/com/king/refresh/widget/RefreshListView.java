package com.king.refresh.widget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.king.refresh.R;

/**
 * 带下拉刷新功能的ListView<br>
 * <br>
 * 刷新事件通过设置刷新监听器{@link OnRefreshListener}执行
 * 
 * @author King
 */
public class RefreshListView extends ListView implements OnScrollListener {

	/**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;
	
    /**
     * 刷新完成
     */
    private final static int REFRESH_DONE = 0;

    /**
     * 正在刷新
     */
    private final static int REFRESH_DOING = 1;

    /**
     * 下拉中(超过阀值)
     */
    private final static int REFRESH_DROPING = 2;

    /**
     * 下拉中(未超过阀值)
     */
    private final static int REFRESH_PULLING = 3;

    /**
     * 刷新成功
     */
    private final static int REFRESH_SUCCESS = 4;

    /**
     * 刷新失败
     */
    private final static int REFRESH_FAILURE = 5;

    /**
     * 实际的padding的距离与界面上偏移距离的比例
     */
    private final static int RATIO = 3;

    /**
     * 头部视图隐藏时间
     */
    private final static int HEADVIEW_HIDEN_TIME = 500;

    /**
     * 头部视图回弹时间
     */
    private final static int HEADVIEW_UP_TIME = 600;

    /**
     * 可见第一项索引
     */
    private int mFirstVisibleItem;

    /**
     * 用于保证startY的值在一个完整的touch事件中只被记录一次
     */
    private boolean isRecored;

    /**
     * 头部View
     */
    private View mHeadView;

    /**
     * 触摸按下位置Y坐标
     */
    private int mStartY;

    /**
	 * 按在屏幕上的手指的id
	 */
	private int mActivePointerId = INVALID_POINTER;
    
    /**
     * 下拉刷新箭头
     */
    private ImageView mArrowImage;

    /**
     * 刷新进度条
     */
    private ProgressBar mProgressBar;

    /**
     * 提示文字
     */
    private TextView mTipText;

    /**
     * 最后一次更新文字
     */
    private TextView mLastUpdateText;

    /**
     * 箭头翻转向下动画
     */
    private RotateAnimation mArrowDownAnim;

    /**
     * 箭头翻转向上动画
     */
    private RotateAnimation mArrowUpAnim;

    /**
     * 头部View测量高度
     */
    private int mHeadContentHeight;

    /**
     * 刷新状态<br>
     * <br>
     */
    private int mRefreshState;

    /**
     * 是否是向上滑动，监听REFRESH_DROPING状态变为REFRESH_PULLING状态
     */
    private boolean isBack;

    /**
     * 刷新监听器
     */
    private OnRefreshListener refreshListener;

    /**
     * 是否可刷新
     */
    private boolean isRefreshable = true;

    /**
     * 滑动操作线程
     */
    private SmoothScrollRunnable mCurrentSmoothScrollRunnable;

    /**
     * 滑动剩余延迟时间
     */
    private int mDelayMillis;

    /**
     * 头部视图填充高度
     */
    private int mHeadViewPaddingTopHeight;

    /**
     * 新按下的手指的y坐标
     */
    private int mPointDownY;
    
    public RefreshListView(Context context) {
        super(context);
        initHeadView(context);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initHeadView(context);
	}

	/**
     * 功能描述: 初始化头部View控件<br>
     * 
     * @param context
     */
    private void initHeadView(Context context) {
        // 设置ListView滑动到尽头无阴影
//        if (android.os.Build.VERSION.SDK_INT >= 9) {
//            this.setOverScrollMode(View.OVER_SCROLL_NEVER);
//        }

        mHeadView = LayoutInflater.from(context).inflate(R.layout.util_refresh_header, null);

        mArrowImage = (ImageView) mHeadView.findViewById(R.id.head_arrowImageView);
        mArrowImage.setMinimumWidth(70);
        mArrowImage.setMinimumHeight(50);

        mProgressBar = (ProgressBar) mHeadView.findViewById(R.id.head_progressBar);

        mTipText = (TextView) mHeadView.findViewById(R.id.head_tipsTextView);
        mLastUpdateText = (TextView) mHeadView.findViewById(R.id.head_lastUpdatedTextView);
        mLastUpdateText.setText("最后更新：" + getRefreshFinishTime());
        // 估算headView的宽和高
        measureView(mHeadView);
        mHeadContentHeight = mHeadView.getMeasuredHeight();

        // 设置头部填充至不可见
        mHeadView.setPadding(0, -1 * mHeadContentHeight, 0, 0);
        mHeadView.invalidate();

        // 添加头部视图
        addHeaderView(mHeadView, null, false);

        // 箭头翻转动画
        mArrowDownAnim = (RotateAnimation) AnimationUtils.loadAnimation(context, R.anim.refresh_arrow_down);
        mArrowUpAnim = (RotateAnimation) AnimationUtils.loadAnimation(context, R.anim.refresh_arrow_up);
    
    }

    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (isRefreshable) { 
	    	switch (event.getAction()) {
	    	 	case MotionEvent.ACTION_DOWN:
		        if (mFirstVisibleItem ==0 && mRefreshState == REFRESH_DONE && mHeadView.getTop() == 0) {
		            isRecored = true;
		
		            // 第一次触摸屏幕的位置
		            mStartY = (int) event.getY();
		            // Log.v(TAG, "在down时候记录当前位置" + " startY:"+mStartY);
		            
		            mActivePointerId = event.getPointerId(0);
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

                case MotionEvent.ACTION_UP:
                	// 重置手指触摸id
        			mActivePointerId = INVALID_POINTER;
        			mStartY = 0;
        			mPointDownY = 0;
                    if (mRefreshState != REFRESH_DOING) {
                        // 此处发生概率很小
                        if (mRefreshState == REFRESH_DONE) {
                        }

                        // 如果下拉未超过阀值，松开后直接设置状态为：刷新完成
                        if (mRefreshState == REFRESH_PULLING) {
                            mRefreshState = REFRESH_DONE;
                            changeHeaderViewByState();
                            // Log.v(TAG, "由下拉刷新状态，到done状态");
                        }

                        // 如果下拉超过阀值，松开后执行刷新
                        if (mRefreshState == REFRESH_DROPING) {
                            mRefreshState = REFRESH_DOING;
                            changeHeaderViewByState();
                        }
                    }

                    isRecored = false;
                    isBack = false;
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

                    if (mRefreshState != REFRESH_DOING && isRecored) {
                        /**
                         * 保证在设置padding的过程中，当前的位置一直是在head， 否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动
                         */

                        // 如果此时状态为REFRESH_DROPING（释放后便可刷新）
                        if (mRefreshState == REFRESH_DROPING) {
                            // 选择头部视图
                            setSelection(0);

                            // 如果继续向上滑动，头部视图即将有部分被遮住时，改变状态为REFRESH_PULLING（释放后不刷新）
                            if (((tempY - mStartY) / RATIO < mHeadContentHeight) && (tempY - mStartY) > 0) {
                                mRefreshState = REFRESH_PULLING;
                                changeHeaderViewByState();
                                // Log.v(TAG, "由松开刷新状态转变到下拉刷新状态");
                            }

                            // 如果一下子滑动到顶部，将头部视图完全遮住,应该恢复DONE状态,这里机率很小
                            else if (tempY - mStartY <= 0) {
                                mRefreshState = REFRESH_DONE;
                                changeHeaderViewByState();
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
                             * 等于mHeadContentHeight时,即是正好完全显示header部分 大于mHeadContentHeight时,即是超出header部分更多
                             * 
                             * 如果头部视图能够完全显示或者超出显示, 需要更改状态: mRefreshState = REFRESH_DROPING
                             */
                            if ((tempY - mStartY) / RATIO >= mHeadContentHeight) {
                                mRefreshState = REFRESH_DROPING;
                                isBack = true;
                                changeHeaderViewByState();
                                // Log.v(TAG, "由done或者下拉刷新状态转变到松开刷新");
                            }

                            // 如果一下子滑动到顶部，将头部视图完全遮住,应该恢复DONE状态
                            else if (tempY - mStartY <= 0) {
                                mRefreshState = REFRESH_DONE;
                                changeHeaderViewByState();
                                // Log.v(TAG, "由done或者下拉刷新状态转变到done状态");
                            }
                        }

                        // 刷新完成状态
                        if (mRefreshState == REFRESH_DONE) {
                            // 如果是下拉动作，头部视图即将有部分被遮住时，改变状态为REFRESH_PULLING（释放后不刷新）
                            if (tempY - mStartY > 0) {
                                mRefreshState = REFRESH_PULLING;
                                changeHeaderViewByState();
                            }
                        }

                        // 填充头部视图，更新paddingTop
                        if (mRefreshState == REFRESH_PULLING) {
                        	// 控制滑动距离，越往下滑动滑动距离越小
                            if(tempY - mStartY >= 150 * RATIO){
                            	double ins = Math.pow((double)(tempY - mStartY - 150 * RATIO), 1.0/3);
                            	tempY = mStartY + 150 * RATIO + (int)(ins * ins * 3);
                            }
                            // Log.v(TAG, "----------------PULL_To_REFRESH2 " + (tempY - startY));
                            mHeadView.setPadding(0, (tempY - mStartY) / RATIO - mHeadContentHeight, 0, 0);
                        }

                        // 继续更新headView的paddingTop
                        if (mRefreshState == REFRESH_DROPING) {
                            mHeadView.setPadding(0, (tempY - mStartY) / RATIO - mHeadContentHeight, 0, 0);
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
     * 功能描述:根据 刷新状态 更新头部视图 <br>
     */
    private void changeHeaderViewByState() {
        switch (mRefreshState) {

        // 下拉至 松开刷新状态
            case REFRESH_DROPING:
                mArrowImage.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mTipText.setVisibility(View.VISIBLE);

                mArrowImage.clearAnimation();
                mArrowImage.startAnimation(mArrowUpAnim);

                mTipText.setText("松开刷新");

                // Log.v(TAG, "当前状态，松开刷新");
                break;

            // 下拉至 松开不刷新状态
            case REFRESH_PULLING:
                mProgressBar.setVisibility(View.GONE);
                mTipText.setVisibility(View.VISIBLE);
                mArrowImage.clearAnimation();
                mArrowImage.setVisibility(View.VISIBLE);

                if (isBack) {
                    isBack = false;
                    mArrowImage.clearAnimation();
                    mArrowImage.startAnimation(mArrowDownAnim);
                    mTipText.setText("下拉刷新");
                } else {
                    mTipText.setText("下拉刷新");
                }

                // Log.v(TAG, "当前状态，下拉刷新");
                break;

            case REFRESH_DOING:
                mHeadViewPaddingTopHeight = mHeadView.getPaddingTop();
                smoothScroll(HEADVIEW_UP_TIME);

                mProgressBar.setVisibility(View.VISIBLE);
                mArrowImage.clearAnimation();
                mArrowImage.setVisibility(View.GONE);
                mTipText.setText("正在刷新...");

//                LogX.d(TAG, "当前状态,正在刷新...");
                break;

            case REFRESH_SUCCESS:
                mHeadViewPaddingTopHeight = mHeadContentHeight;
                //如果头部已经隐藏，不调用头部滑动隐藏动作
                if(mHeadView.getPaddingTop() >= 0){
                	smoothScroll(HEADVIEW_HIDEN_TIME);
                }else{
                	smoothScroll(0);
                }
                mTipText.setText("刷新成功");
                mProgressBar.setVisibility(View.GONE);
                mArrowImage.clearAnimation();
                break;

            case REFRESH_FAILURE:
                mHeadViewPaddingTopHeight = mHeadContentHeight;
                //如果头部已经隐藏，不调用头部滑动隐藏动作
                if(mHeadView.getPaddingTop() >= 0){
                	smoothScroll(HEADVIEW_HIDEN_TIME);
                }else{
                	smoothScroll(0);
                }
                mTipText.setText("刷新失败");
                mProgressBar.setVisibility(View.GONE);
                mArrowImage.clearAnimation();
                break;

            case REFRESH_DONE:

                mHeadView.setPadding(0, -mHeadContentHeight, 0, 0);
                mProgressBar.setVisibility(View.GONE);
                mArrowImage.clearAnimation();
                mTipText.setText("下拉刷新");

                // Log.v(TAG, "当前状态，done");
                break;
        }
    }

    /**
     * 功能描述:设置刷新监听器 <br>
     */
    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
    	super.setAdapter(adapter);
    }
    
    /**
     * 功能描述: 刷新完成操作<br>
     * <br>
     * 通过 flag 判断刷新是否成功
     * 
     * @param flag
     */
    public void onRefreshComplete(boolean flag) {
        if (flag) {
            mRefreshState = REFRESH_SUCCESS;
            mLastUpdateText.setText("最后更新：" + getRefreshFinishTime());
        } else {
            mRefreshState = REFRESH_FAILURE;
        }
        changeHeaderViewByState();
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
     * 功能描述: 执行刷新操作<br>
     */
    private void onRefresh() {
        if (refreshListener != null) {
            refreshListener.onRefresh();
        }
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
     * 功能描述: 获取刷新时间<br>
     */
    private String getRefreshFinishTime() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        // 格式化时间
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sf.format(date);
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

        if (mDelayMillis < time) {
            // 隐藏操作开始时，固定停留0.5秒
            if (mDelayMillis == 0 && (mRefreshState == REFRESH_SUCCESS || mRefreshState == REFRESH_FAILURE)) {
                postDelayed(mCurrentSmoothScrollRunnable, 500);
                // 隐藏操作开始后，以固定时间执行一次paddingTop
            } else {
                postDelayed(mCurrentSmoothScrollRunnable, time / 100);
            }
        } else {
            mHeadViewPaddingTopHeight = 0;
            mDelayMillis = 0;
            // 头部向上滑动至固定位置后开始执行刷新
            if (time == HEADVIEW_UP_TIME) {
                onRefresh();
                // 头部视图完全隐藏后，设定状态为刷新完成
            } else {
                mRefreshState = REFRESH_DONE;
                changeHeaderViewByState();
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        this.mFirstVisibleItem = getFirstVisiblePosition();
    }
    
    /**
     * 头部隐藏操作线程<br>
     * 
     * @author King
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
            if (mScrollTime == HEADVIEW_UP_TIME) {
                mHeadView.setPadding(0, mHeadViewPaddingTopHeight * (mScrollTime - mDelayMillis) / mScrollTime, 0, 0);
                // Log.v("test", "mScrollTime"+mHeadView.getPaddingTop());
            }
            if (mScrollTime == HEADVIEW_HIDEN_TIME) {
                mHeadView.setPadding(0, -mHeadViewPaddingTopHeight * mDelayMillis / mScrollTime, 0, 0);
            }
            smoothScroll(mScrollTime);
            mHeadView.invalidate();
        }

        public void stop() {
            removeCallbacks(this);
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
     * 下拉刷新监听器<br>
     * 
     * @author King
     */
    public interface OnRefreshListener {
        void onRefresh();
    }
}