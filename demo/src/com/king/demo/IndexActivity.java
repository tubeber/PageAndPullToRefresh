package com.king.demo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class IndexActivity extends Activity {
	
	private ListView mListView;
	
	private List<String> mApiNames;
	
	private List<String> mApiActivityNames;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_index);
		mListView = (ListView) findViewById(R.id.index_list);
		initApiNames();
		
		mListView.setAdapter(new IndexAdapter(getApplicationContext(), mApiNames));
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.setClassName(getApplicationContext(), mApiActivityNames.get(position));
				startActivity(intent);
			}
		});
	}
	
	private void initApiNames() {
		mApiNames = new ArrayList<String>();
		mApiActivityNames = new ArrayList<String>();
		
		mApiNames.add("PageListView");
		mApiActivityNames.add("com.king.demo.activity.PageListViewActivity");
		
		mApiNames.add("ImagePageListView");
		mApiActivityNames.add("com.king.demo.activity.ImagePageListViewActivity");

		mApiNames.add("WaterfallListView");
		mApiActivityNames.add("com.king.demo.activity.ImageWaterfallPageListViewActivity");
	}
}
