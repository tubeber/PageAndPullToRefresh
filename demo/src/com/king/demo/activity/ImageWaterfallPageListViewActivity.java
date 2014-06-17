package com.king.demo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.king.demo.R;
import com.king.demo.adapter.WaterfallListAdapter;
import com.king.demo.request.DemoPageWaterfallReuqest;
import com.king.refresh.widget.waterfall.PageAndRefreshImageWaterfallListView;

public class ImageWaterfallPageListViewActivity extends Activity {

	private PageAndRefreshImageWaterfallListView mListView;
	private WaterfallListAdapter mAdapter;

	private View headView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_waterfall_page_and_refresh);
		
		mListView = (PageAndRefreshImageWaterfallListView) findViewById(R.id.demo_page_list);
		headView = LayoutInflater.from(this).inflate(R.layout.header, null);
		mListView.setFixedHeaderView(headView);
		mListView.setHeadImage(R.drawable.image);
		mAdapter = new WaterfallListAdapter(this, new DemoPageWaterfallReuqest());
		mListView.setAdapter(mAdapter);

	}

}
