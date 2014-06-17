package com.king.refresh;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

/**
 * A base adapter of paging request and drop-down refresh.<br>
 * The subclass just needs to overwrite the abstract method {@link #getView(Integer, View, ViewGroup)},
 * 
 * @author King
 * @since 2013/11/26 11:30
 */
public abstract class PageAndRefreshBaseAdapter extends BaseAdapter implements
		PageAndRefreshController, PageAndRefreshRequestCallBack {

	/**
	 * The current page. 
	 */
	private int mCurrentPage = 1;
	
	/**
	 * The total page.
	 */
	private int mTotalPage = Integer.MAX_VALUE;
	
	/**
	 * Whether data is loading.
	 */
	private boolean mIsLoading;
	
	/**
	 * Whether current action is refresh.
	 */
	private boolean mIsRefresh;
	
	/**
	 * A UI controller.
	 */
	private OnViewUpdateListener mListener;

	/**
	 * The data listener.
	 */
	private OnDataChangeListener mDataListener;
	
	/**
	 * The data request interface.
	 */
	private final PageAndRefreshRequestService mRequestService;
	
	/**
	 * The data of list.
	 */
	private List<Object> mData;
	
	/**
	 * The additional data.
	 */
	private Bundle mBundle;
	
	/**
	 * Number of preloading.
	 * this count must be less than every page size.
	 */
	private int mPrestrainCount;
	
	/**
	 * Whether using fake data to replace empty data.
	 */
	private boolean isFake;

	/**
	 * A constructor
	 * 
	 * @param requestService  Data request
	 */
	public PageAndRefreshBaseAdapter(PageAndRefreshRequestService requestService) {
		this.mRequestService = requestService;
	}

	/**
	 * A constructor
	 * 
	 * @param dataRequest  Data request
	 * @param prestrainCount Number of preloading.
	 */
	public PageAndRefreshBaseAdapter(PageAndRefreshRequestService requestService, int prestrainCount) {
		this(requestService);
		setPrestrainCount(prestrainCount);
	}
	
	/**
	 * Set the listener for updating the view.
	 *
	 * @param listener
	 */
	public void setOnViewUpdateListener(OnViewUpdateListener listener) {
		this.mListener = listener;
	}

	/**
	 * Set the listener for handle data.
	 * 
	 * @param listener
	 */
	public void setOnDataChangeListener(OnDataChangeListener listener){
		mDataListener = listener;
	}
	
	@Override
	public void requestPage() {
		if(mIsRefresh){
			//调用下拉刷新之前的处理
			mListener.beforeRefresh();
		}else{
			//调用分页请求之前的处理
			mListener.beforeLoading(mCurrentPage == 1);
		}
		//添加标志位，确保只请求一次
		mIsLoading = true;
		//调用请求
		mRequestService.sendRequest(mIsRefresh ? 1 : mCurrentPage, this);
	}

	/* 是否可以执行分页请求
	 * @see com.suning.mobile.corptravel.ui.widget.pagelistview.PageDemandingController#isRequestEnable(int)
	 */
	@Override
	public boolean isPageRequestEnable(int lastVisiblePosition) {
		//已经加载完所有页
		if(mCurrentPage > mTotalPage){
			return false;
		}
		
		//正在刷新
		if(mIsRefresh){
			return false;
		}
		
		//正在加载
		if(mIsLoading){
			return false;
		}
		
		// 正使用的fake数据
		if(isFake && getAllData() != null && !getAllData().isEmpty() && getAllData().get(0) instanceof FakeData){
			return false; 
		}
		
		if(lastVisiblePosition == DIRECTLY_REQUEST){
			return true;
		}
		
		if(getCount() == 0){
			return false;
		}
		
		if(getCount() < mPrestrainCount || lastVisiblePosition < getCount() - mPrestrainCount - 1){
			return false;
		}
		return true;
	};
	
	@Override
	public boolean isRefreshRequestEnable() {
		return !mIsLoading;
	}
	
	@Override
	public int getCount() {
		if(mData == null){
			return 0;
		}
		return mData.size();
	}
	
	@Override
	public Object getItem(int position) {
		if(mData != null || mData.size() > position){
			return mData.get(position);
		}
		return null;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public void onRequestComplete(List<? extends Object> data, int totalPage){
		
		if(mDataListener != null){
			mDataListener.onListData(data == null ?  null : new ArrayList<Object>(data));
		}
		
		//初始化列表数据集合
		if(mData == null){
			mData = new ArrayList<Object>();
		}
		
		//判断服务是否有数据返回
		if(data == null){
			if(mIsRefresh){
				mListener.afterRefresh(false);
			}else{
				mListener.afterLoading(false, mCurrentPage == 1);
				
				// 如果是初次加载，判断是否加载假数据
				if(isFake && mCurrentPage == 1){
					mData.clear();
					mTotalPage = 1;
					mData.add(new FakeData(true));
					notifyDataSetChanged();
				}
			}
		}else{
			mTotalPage = totalPage;
			if(mIsRefresh){
				mCurrentPage = 1;
				mListener.afterRefresh(true);
			}else{
				mListener.afterLoading(true, mCurrentPage == 1);
			}
			// 如果是第一页，清除原有所有数据
			if(mCurrentPage == 1){
				mData.clear();
			}
			
			if(isFake && mCurrentPage == 1 && data.isEmpty()){
				mTotalPage = 1;
				mData.add(new FakeData(false));
			}
			
			mData.addAll(data);
			notifyDataSetChanged();
			mCurrentPage ++;
		}
		
		//如果是刷新重置标志位
		if(mIsRefresh){
			mIsRefresh = false;
		}
		//重置标志位
		mIsLoading = false;
	}
	
	@Override
	public void onRequestCompleteAndRefresh(List<? extends Object> data, int totalPage){
		this.onRequestComplete(data, totalPage);
		doRefresh();
	}
	
	@Override
	public void onRequestComplete(Bundle bundle) {
		this.mBundle = bundle;
		if(mDataListener != null){
			mDataListener.onExtraData(bundle);
		}
	}
	
	@Override
	public void doRefresh() {
		//添加标志位
		mIsRefresh = true;
		//请求
		requestPage();
	}
	
	/**
	 * Get all data of list.
	 *
	 * @return
	 */
	public List<? extends Object> getAllData(){
		return mData;
	}
	
	/**
	 * Get current additional data
	 *
	 * @return
	 */
	public Bundle getExtras(){
		return mBundle;
	}
	
	/**
	 * Set the number of preloading.
	 * 
	 * @param prestrainCount
	 */
	public void setPrestrainCount(int prestrainCount){
		if(prestrainCount >= 0 && mCurrentPage == 1){
			mPrestrainCount = prestrainCount;
		};
	}
	
	/**
	 * Clear all the list data.
	 * 
	 * @return Whether clear is successful.
	 */
	public boolean clear(){
		if(mData == null){
			return false;
		}
		mData.clear();
		mData = null;
		return true;
	}
	
	/**
	 * Whether use the fake data.<br>
	 * if the list is empty, will use a fake data to show in ListView.
	 *
	 * @param isUseFake
	 */
	public void isUseFake(boolean isUseFake){
		this.isFake = isUseFake;
	}
	
	/**
	 * Reset the ListView.<br>
	 * The method will clear all data and reset all flags.
	 */
	public void resetListView(){
		if(mIsLoading){
			return;
		}
		
		//清除数据，重置页数
		if(clear()){
			mCurrentPage = 1;
			mTotalPage = Integer.MAX_VALUE;
			//必须调用此方法
			notifyDataSetChanged(true);
		}
		//重置正在加载标志位
		mIsLoading = false;
		//重置正在刷新标志位
		mIsRefresh = false;
	}
	
	/**
	 * Refresh the ListView and request the data from first page.
	 */
	public void refreshListView(){
		//复位列表
		resetListView();
		//重新加载数据
		requestPage();
	}
	
	public boolean getIsLoading(){
		return mIsLoading;
	}
	
	public Object remove(int position){
		if(mData == null || mData.size() <= position){
			return null;
		}
		return mData.remove(position);
	}
	
	/**
	 * Set the data and notify the list directly.
	 */
	@SuppressWarnings("unchecked")
	public <E> void setDataList(List<E> list) {
		mData = (List<Object>) list;
		notifyDataSetInvalidated();
	}
	
	@Override
	public void notifyDataSetChanged() {
		notifyDataSetChanged(false);
	}
	
	public void notifyDataSetChanged(boolean isDirectly) {
		
		if(isDirectly){
			super.notifyDataSetChanged();
			return;
		}
		
		if(isFake && mData != null && mData.size() == 0){
			mData.add(new FakeData(false));
		}
		super.notifyDataSetChanged();
	}
	
	@Override
	public boolean getIsRefresh() {
		return mIsRefresh;
	}
	
	/**
	 * The fake data model. 
	 *
	 * @author King
	 * @since 2014-2-25 下午4:06:10
	 */
	public class FakeData{
		
		/**
		 * If the data is empty, this will be true.
		 * If failed to get data, this will be false.
		 */
		public boolean isFail = true;

		public FakeData(boolean isFail) {
			this.isFail = isFail;
		}
		
	}
	
	/**
	 * A listener was called before the data was rendered to {@link ListView}.
	 * 
	 * @author King
	 * @since 2014-3-6 下午3:02:09
	 */
	public interface OnDataChangeListener{
		
		/**
		 * The list data.
		 * 
		 * @param data  The data of current page in {@link ListView}.
		 */
		void onListData(List<Object> data);
		
		/**
		 * The additional data.
		 * 
		 * @param bundle  A Bundle data.
		 */
		void onExtraData(Bundle bundle);
		
	}
}
