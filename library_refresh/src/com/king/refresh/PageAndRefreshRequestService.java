package com.king.refresh;

import java.util.List;

/**
 * The data request interface.
 *
 * @author King
 * @since 2013/12/03 10:00
 */
public interface PageAndRefreshRequestService {
	
	/**
	 * Send request to get data from net or database.
	 * When received result data of the request, we should called {@link PageAndRefreshRequestCallBack#onRequestComplete(List, int)} to
	 * render data.
	 *  
	 * @param page What page data was requested .
	 * @param callback The result data callback.
	 */
	void sendRequest(int page, PageAndRefreshRequestCallBack callback);
}
