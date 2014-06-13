package com.king.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.king.demo.R;
import com.king.demo.model.Student;
import com.king.refresh.PageAndRefreshBaseAdapter;
import com.king.refresh.PageAndRefreshRequestService;

/**
 * 适配器示例<br> 
 * 继承 {@link PageAndRefreshBaseAdapter} 基类<br>
 * 由于已经对数据源做了封装，使用者只需要关注视图的处理
 * 
 * @author King
 */
public class PageListViewAdapter extends PageAndRefreshBaseAdapter {

	private LayoutInflater mInflater;
	
	public PageListViewAdapter(Context context, PageAndRefreshRequestService requestService) {
		super(requestService);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		if(convertView == null || convertView.getTag() == null){
			convertView = mInflater.inflate(R.layout.item_listview, null);
			holder = new ViewHolder();
			holder.view = (TextView) convertView.findViewById(R.id.name);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		Student student = (Student) getItem(position);
		holder.view.setText(student.name); 
		return convertView;
	}

	private static class ViewHolder{
		TextView view;
	}

}
