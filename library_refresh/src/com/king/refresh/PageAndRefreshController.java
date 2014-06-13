package com.king.refresh;

import android.widget.ListView;

/**
 * A controller for {@link ListView}.<br>
 * The view used this to control whether we could send page request.
 *
 * @author King
 * @since 2013/11/26 13:00
 */
public interface PageAndRefreshController {

	/**
	 * Directly request constant.
	 */
	final int DIRECTLY_REQUEST = -100;
	
	/**
	 * Perform paging request.
	 */
	void requestPage();
	
	/**
	 * Whether the list could request next page.
	 *
	 * @param lastVisiblePosition  The last visible position of {@link ListView}.
	 * @return
	 */
	boolean isPageRequestEnable(int lastVisiblePosition);
	
	/**
	 * Whether the list could perform refresh.
	 *
	 * @return
	 */
	boolean isRefreshRequestEnable();
	
	/**
	 * Perform refresh request
	 */
	void doRefresh();
	
	/**
	 * Set the {@link OnViewUpdateListener}
	 *
	 * @param viewUpdateListener
	 */
	void setOnViewUpdateListener(OnViewUpdateListener viewUpdateListener);
}
