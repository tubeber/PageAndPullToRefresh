package com.king.refresh.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.king.refresh.OnViewUpdateListener;
import com.king.refresh.PageAndRefreshController;
import com.king.refresh.R;

/**
 * 分页请求ListView效果类,绑定适配器自动请求第一页<br>
 * 
 * @author 13071499
 * @since 2013/11/26 11:30
 */
public class PageAndImageRefreshListView extends RefreshImageListView implements
		OnScrollListener, OnViewUpdateListener {

	/**
	 * FooterView隐藏
	 */
	private static final int FOOTERVIEW_GONE = 0;

	/**
	 * FooterView显示重试
	 */
	private static final int FOOTERVIEW_RETRY = 1;

	/**
	 * FooterView正在加载
	 */
	private static final int FOOTERVIEW_LOADING = 2;

	/**
	 * 上下文
	 */
	private Context mContext;

	/**
	 * 分页加载控制器
	 */
	private PageAndRefreshController mController;

	/**
     * 滑动监听器
     */
    private OnNewScrollListener scrollListener;
	
	/**
	 * 分页加载功能是否可用
	 */
	private boolean mIsEnable = true;

	/**
	 * 分页是否加锁禁用
	 */
	private boolean mIsPageClock;
	
	/**
	 * 空视图
	 */
	private View mEmptyView;

	/**
	 * FooterView视图
	 */
	private View mFooterView;

	/**
	 * NoDataView无数据视图
	 */
	private View mNoDataView;
	
	/**
	 * 无网络连接视图
	 */
	private View mNoLinkView;
	
	/**
	 * 无数据视图位置
	 */
	private int mNoDataGravity;
	
	/**
	 * 无网络连接视图位置
	 */
	private int mNoLinkGravity;
	
	/**
	 * FooterView视图高度
	 */
	private int mFooterViewHight;

	public PageAndImageRefreshListView(Context context) {
		this(context, null);
	}

	public PageAndImageRefreshListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.PageAndImageRefreshListView);
		mIsEnable = arr.getBoolean(R.styleable.PageAndImageRefreshListView_image_pageDemandingEnable, true);
		this.setRefreshable(arr.getBoolean(R.styleable.PageAndImageRefreshListView_image_refreshable, true));
		mNoDataGravity = arr.getInt(R.styleable.PageAndImageRefreshListView_image_noDataGravity, Gravity.CENTER);
		mNoLinkGravity = arr.getInt(R.styleable.PageAndImageRefreshListView_image_noLinkGravity, Gravity.CENTER);
		setNoDataImage(arr.getResourceId(R.styleable.PageAndImageRefreshListView_image_noDataImage, -1), mNoDataGravity);
		setNoLinkImage(arr.getResourceId(R.styleable.PageAndImageRefreshListView_image_noLinkImage, -1), mNoLinkGravity);
		setHeadImage(arr.getResourceId(R.styleable.PageAndImageRefreshListView_image_headimage, R.drawable.ic_launcher));
		arr.recycle();
	}

	public PageAndImageRefreshListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// 调用新的滑动监听器
		if(scrollListener != null){
			scrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
		
		//验证是否可分页
		if (mIsEnable && mController.isPageRequestEnable(view.getLastVisiblePosition())) {
			mController.requestPage();
		}else{
			//验证是否可下拉刷新
			super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}
	
	public void setOnNewScrollListener(OnNewScrollListener scrollListener) {
		this.scrollListener = scrollListener;
	}
	
	@Override
	public void setAdapter(ListAdapter adapter) {
		// 初始化ListView视图
		initListView();
		// 添加适配器    因为有footerView，所以必须在initListView()之后调用
		super.setAdapter(adapter);
		if (adapter instanceof PageAndRefreshController) {
			mController = (PageAndRefreshController) adapter;
			mController.setOnViewUpdateListener(this);
		}
		//先请求第一页，然后设置分页滑动监听器
		mController.requestPage();
		this.setOnScrollListener(this);
	}

	@Override
	public void onLoading() {
	}

	@Override
	public void beforeLoading(boolean isFirstPage) {
		//如果是第一页,显示加载的PrograssBar
		if(isFirstPage){
			updateEmptyView(View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
		//如果不是第一页，显示FooterView正在加载
		}else{
			updateFooterView(FOOTERVIEW_LOADING);
		}
	}

	@Override
	public void afterLoading(boolean isSuccess, boolean isFirstPage) {
		//如果是第一页
		if(isFirstPage){
			//请求成功显示NoDataView,否则显示重试
			updateEmptyView(View.INVISIBLE, isSuccess ? View.INVISIBLE : View.VISIBLE, isSuccess ? View.VISIBLE : View.INVISIBLE);
		//如果不是第一页
		}else{
			//请求成功隐藏FooterView，否则显示FooterView重试
			updateFooterView(isSuccess ? FOOTERVIEW_GONE : FOOTERVIEW_RETRY);
		}
	}

	@Override
	public void beforeRefresh() {
		// 刷新之前 禁用分页加载
		mIsPageClock = mIsEnable ? true : false;
		mIsEnable = false;
		//隐藏footerView
		updateFooterView(FOOTERVIEW_GONE);
	}

	@Override
	public void afterRefresh(boolean isSuccess) {
		// 刷新之后  解除禁用分页加载
		mIsEnable = mIsPageClock ? true : false;
		//设置刷新完成
		onRefreshComplete(isSuccess);
	}
	
	/**
	 * 功能描述: 设置分页加载功能是否可用,必须在 {@link #setAdapter(ListAdapter)} 后调用<br>
	 * 
	 * @param isEnable
	 */
	public void setPageDemandingEnable(boolean isEnable) {
		if (mController != null) {
			mIsEnable = isEnable;
		}
	}
	
	/**
	 * 功能描述:添加列表无数据视图 <br><br>
	 * 必须在 {@link #setAdapter(ListAdapter)} 之前调用
	 * @param view
	 * @param gravity
	 */
	public void addNoDataView(View view, int gravity){
		mNoDataView = view;
		mNoDataGravity = gravity;
	}
	
	/**
	 * 功能描述:添加无网络连接视图 <br><br>
	 * 必须在 {@link #setAdapter(ListAdapter)} 之前调用
	 * @param view
	 * @param gravity
	 */
	public void addNoLinkView(View view, int gravity){
		mNoLinkView = view;
		mNoLinkGravity = gravity;
	}
	
	/**
	 * 功能描述: 设置无数据图片<br>
	 * 如果已经调用{@link #addNoDataView(View)} 则无效<br>
	 * 必须在 {@link #setAdapter(ListAdapter)} 之前调用
	 *
	 * @param resId
	 * @param gravity
	 */
	public void setNoDataImage(int resId, int gravity){
		
		if(resId == -1){
			return;
		}
		
		ImageView view = new ImageView(mContext);
		view.setImageResource(resId);
		addNoDataView(view, gravity);
	}
	
	/**
	 * 功能描述: 设置无网络连接图片<br>
	 * 如果已经调用{@link #addNoLinkView(View)} 则无效<br>
	 * 必须在 {@link #setAdapter(ListAdapter)} 之前调用
	 *
	 * @param resId
	 * @param gravity
	 */
	public void setNoLinkImage(int resId, int gravity){
		if(resId == -1){
			return;
		}
		
		ImageView view = new ImageView(mContext);
		view.setImageResource(resId);
		addNoLinkView(view, gravity);
	}
	
	/**
	 * 功能描述: 获取分页加载功能是否可用<br>
	 * 
	 * @return
	 */
	public boolean isPageDemandingEnable() {
		return mIsEnable;
	}

	/**
	 * 功能描述: 初始化ListView视图 <br>
	 * <br>
	 * 设置ListView的EmptyView、FooterView、HeaderView等
	 * 
	 * @param context
	 */
	public void initListView() {
		setEmptyView(R.layout.unit_listview_empty_view);
		setFootView(R.layout.unit_listview_foot_view);
		
		//设置下拉刷新监听器
		this.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				mController.doRefresh();
			}
		});
	}

	/**
	 * 功能描述:设置EmptyView <br>
	 * 
	 * @param ResId
	 */
	public void setEmptyView(int ResId) {
		mEmptyView = LayoutInflater.from(mContext).inflate(ResId, null);
		final EmptyViewHolder emptyViewHolder = new EmptyViewHolder();

		// 初始化控件
		emptyViewHolder.emptyViewLoading = (LinearLayout) mEmptyView
				.findViewById(R.id.llCompRequestEmptyViewLoading);
		emptyViewHolder.emptyViewRetry = (LinearLayout) mEmptyView
				.findViewById(R.id.llCompRequestEmptyViewRetry);
		emptyViewHolder.emptyViewRetryBtn = (Button) mEmptyView
				.findViewById(R.id.btnCompRequestEmptyView);
		emptyViewHolder.emptyViewNoData = (LinearLayout) mEmptyView
				.findViewById(R.id.emptyViewNoData);
		emptyViewHolder.emptyViewNoLink = (LinearLayout) mEmptyView
				.findViewById(R.id.emptyViewNoLink);
		// 添加mNoDataView到EmptyView视图中
		if(mNoDataView != null){
			emptyViewHolder.emptyViewNoData.setGravity(mNoDataGravity);
			emptyViewHolder.emptyViewNoData.addView(mNoDataView, new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
		// 添加mNoLinkView到EmptyView视图中
		if (mNoLinkView != null) {
			emptyViewHolder.emptyViewNoLink.setGravity(mNoLinkGravity);
			emptyViewHolder.emptyViewNoLink.addView(mNoLinkView,
					new LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
		}

		// 重试按钮监听
		emptyViewHolder.emptyViewRetryBtn
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 重新加载
						doRetryRequest();
					}
				});

		// 添加到缓存
		mEmptyView.setTag(emptyViewHolder);

		// 获取listView所在父控件里的视图层索引
		int index = 0;
		int viewCount = ((ViewGroup) this.getParent()).getChildCount();
		for (int i = 0; i < viewCount; i++) {
			if (((ViewGroup) this.getParent()).getChildAt(i) == this) {
				index = i;
				break;
			}
		}
		// 添加空视图在listView下层显示
		((ViewGroup) this.getParent()).addView(mEmptyView, index,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
		// 设置ListView空视图
		setEmptyView(mEmptyView);
	}

	/**
	 * 初始化底部View
	 * 
	 * @param ResId
	 */
	private void setFootView(int ResId) {
		mFooterView = LayoutInflater.from(mContext).inflate(
				R.layout.unit_listview_foot_view, null);
		final FootViewHolder footViewHolder = new FootViewHolder();
		// 初始化底部View控件
		footViewHolder.pbFootView = (ProgressBar) mFooterView
				.findViewById(R.id.pbCompRequestFootView);
		footViewHolder.tvFootViewLoadingTips = (TextView) mFooterView
				.findViewById(R.id.tvCompRequestFootViewLoadingTips);
		footViewHolder.tvFootViewRetryTips = (LinearLayout) mFooterView
				.findViewById(R.id.tvCompRequestFootViewRetryTips);

		// 重新加载下一页点击事件
		footViewHolder.tvFootViewRetryTips
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 重新加载
						doRetryRequest();
					}
				});

		// 添加底部视图缓存
		mFooterView.setTag(footViewHolder);

		// 设置点击footerView不响应listView的onItemClick事件
		mFooterView.setOnClickListener(null);

		measureView(mFooterView);

		mFooterViewHight = mFooterView.getMeasuredHeight();

		// 设置ListView底部视图
		this.addFooterView(mFooterView);
		
		//初始隐藏FooterView
		updateFooterView(FOOTERVIEW_GONE);
	}

	/**
	 * 功能描述: 更新EmptyView
	 * 
	 * @param pdVisible
	 *            控制progressBar的显示方式
	 * @param btnVisible
	 *            控制空View上重试按钮的显示方式
	 * @param noDataVisible
	 *            控制无数据显示
	 */
	private void updateEmptyView(int pdVisible, int btnVisible,
			int noDataVisible) {
		// 获取保存的缓存ViewHolder
		final EmptyViewHolder eh = (EmptyViewHolder) mEmptyView.getTag();

		// 设置PrograssBar
		eh.emptyViewLoading.setVisibility(pdVisible);
		// 设置重试按钮
		if(mNoLinkView == null){
			eh.emptyViewRetry.setVisibility(btnVisible);
		}else{
			eh.emptyViewNoLink.setVisibility(btnVisible);
		}
		// 设置无数据显示
		eh.emptyViewNoData.setVisibility(noDataVisible);
	}

	/**
	 * 功能描述: 更新FooterView <br>
	 * 
	 * @param flag
	 * 标志位: 0--隐藏 1--重试 2--正在加载
	 */
	private void updateFooterView(int flag) {
		final FootViewHolder fh = (FootViewHolder) mFooterView.getTag();
		switch (flag) {
		case FOOTERVIEW_GONE:
			setFooterViewVisable(false);
			break;

		case FOOTERVIEW_RETRY:
			setFooterViewVisable(true);
			fh.pbFootView.setVisibility(View.GONE);
	        fh.tvFootViewLoadingTips.setVisibility(View.GONE);
	        fh.tvFootViewRetryTips.setVisibility(View.VISIBLE);
			break;
		case FOOTERVIEW_LOADING:
			setFooterViewVisable(true);
			fh.pbFootView.setVisibility(View.VISIBLE);
	        fh.tvFootViewLoadingTips.setVisibility(View.VISIBLE);
	        fh.tvFootViewRetryTips.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	/**
	 * 功能描述: 设置FooterView是否可见<br>
	 *
	 * @param isVisable
	 */
	private void setFooterViewVisable(boolean isVisable){
		
		if(isVisable && (mFooterView.getPaddingTop() == 0)){
			return;
		}
		if(!isVisable && (mFooterView.getPaddingTop() == -mFooterViewHight)){
			return;
		}
		//设置FooterView分割线
		this.setFooterDividersEnabled(isVisable);
		//设置FooterView填充，使其isVisable
        mFooterView.setPadding(0, isVisable ? 0 : -mFooterViewHight, 0, 0);
	}
	
	/**
	 * 功能描述: 执行重新请求<br>
	 */
	private void doRetryRequest() {
		if (mController.isPageRequestEnable(PageAndRefreshController.DIRECTLY_REQUEST)) {
			mController.requestPage();
		}
	}

	/**
	 * 功能描述: 测量视图宽高，以便获取其宽高<br>
	 * 
	 * @param child
	 */
	public static void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	/**
	 * 空View对应的组件列表
	 */
	public static final class EmptyViewHolder {
		/**
		 * 空页面展示时的转动progressBar
		 */
		LinearLayout emptyViewLoading;
		/**
		 * 空页面时的文字显示
		 */
		LinearLayout emptyViewRetry;
		/**
		 * 重试按钮定义
		 */
		Button emptyViewRetryBtn;
		/**
		 * 无数据时显示的View
		 */
		LinearLayout emptyViewNoData;
		/**
		 * 无网络连接的View
		 */
		LinearLayout emptyViewNoLink;
	}

	/**
	 * 底部View对应的组件列表
	 */
	public static final class FootViewHolder {
		/**
		 * 加载progressBar
		 */
		ProgressBar pbFootView;
		/**
		 * 加载提示
		 */
		TextView tvFootViewLoadingTips;
		/**
		 * 重试提示
		 */
		LinearLayout tvFootViewRetryTips;
	}

	
}
