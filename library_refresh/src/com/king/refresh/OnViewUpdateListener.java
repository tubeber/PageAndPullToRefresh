package com.king.refresh;

/**
 * The UI controller.
 * When pulling to refresh or loading next page, the method will be called to update UI. 
 * 
 * @author King
 * @since 2013/11/26 13:00
 */
public interface OnViewUpdateListener {

	/**
	 * Loading action was called.
	 */
	void onLoading();

	/**
	 * Before loading action was called.
	 * If the page is the first, the ListView will show a whole loading view.
	 * Otherwise, the ListView will show the footer loading view.
	 * 
	 * @param isFirstPage Whether it is the first page
	 */
	void beforeLoading(boolean isFirstPage);

	/**
	 * After loading action was called.
	 * 
	 * @param isSuccess Whether the data request is successful
	 * @param isFirstPage Whether it is the first page
	 */
	void afterLoading(boolean isSuccess, boolean isFirstPage);
	
	/**
	 * Before refresh action was called.
	 */
	void beforeRefresh();
	
	/**
	 * After refresh action was called.
	 * 
	 * @param isSuccess Whether the data request is successful
	 */
	void afterRefresh(boolean isSuccess);
}
