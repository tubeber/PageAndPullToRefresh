package com.king.refresh;

import java.util.List;

import android.os.Bundle;
import android.widget.ListView;

/**
 * A callback listener would be called after the request of data.
 * 
 * @author King
 * @since 2013/12/03 11:00
 */
public interface PageAndRefreshRequestCallBack {

	/**
	 * The method should be called after we got the data of {@link ListView}.<br>
	 * In the method, the adapter of {@link ListView} would call {@link PageAndRefreshBaseAdapter#notifyDataSetChanged()} automatically.
	 *
		 * @param data  The data of current page in {@link ListView}.
		 * @param totalPage  Total number of pages.
	 */
	void onRequestComplete(List<? extends Object> data, int totalPage);

	/**
	 * The method should be called after we got the data of {@link ListView}.<br>
	 * In the method, the adapter of {@link ListView} would call {@link PageAndRefreshBaseAdapter#notifyDataSetChanged()} automatically.<br>
	 * And after the data was Rendered to {@link ListView}, will call refresh automatically.
	 *
		 * @param data  The data of current page in {@link ListView}.
		 * @param totalPage  Total number of pages.
	 */
	void onRequestCompleteAndRefresh(List<? extends Object> data, int totalPage);
	
	/**
	 * The method was used to transfer additional data.
	 * If the request's result contains additional data which is not acted on {@link ListView} directly.
	 * 
	 * @param bundle  A Bundle data.
	 */
	void onRequestComplete(Bundle bundle);
	
	/**
	 * Whether the current state is refresh.
	 * 
	 * @return
	 */
	boolean getIsRefresh();
}