package com.king.demo;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class IndexAdapter extends BaseAdapter{

	private Context mContext;
	
	private List<String> mApiNames;
	
	public IndexAdapter(Context context, List<String> apiNames) {
		mContext = context;
		mApiNames = apiNames;
	}

	@Override
	public int getCount() {
		return mApiNames.size();
	}

	@Override
	public Object getItem(int position) {
		return mApiNames.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_index, null);
			holder = new ViewHolder();
			holder.mTextView = (TextView) convertView.findViewById(R.id.name);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mTextView.setText(mApiNames.get(position));
		return convertView;
	}
	
	private static class ViewHolder{
		TextView mTextView;
	}
}

