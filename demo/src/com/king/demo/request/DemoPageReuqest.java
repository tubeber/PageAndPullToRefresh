package com.king.demo.request;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;

import com.king.demo.model.Student;
import com.king.refresh.PageAndRefreshRequestCallBack;
import com.king.refresh.PageAndRefreshRequestService;

/**
 * 分页请求Request示例<br><br>
 */
public class DemoPageReuqest implements PageAndRefreshRequestService {

	private boolean first = true;
	private PageAndRefreshRequestCallBack mCallBack;
	private Handler handler = new Handler(){

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// 模拟请求结果，一次成功，一次失败，循环
			case 1:
				if(first){
					mCallBack.onRequestComplete(null, 0);
				}else{
					mCallBack.onRequestComplete((List<Student>) msg.obj, 5);
				}
				first = !first;
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	private int page;
	
	@Override
	public void sendRequest(int page, PageAndRefreshRequestCallBack listener) {
		if(mCallBack == null){
			mCallBack = listener;
		}
		this.page = page;
		
		// 此处使用线程模拟数据请求(网络或数据库)过程
		// 在实际项目应用中，此处应该是发送一个异步网络请求或数据库操作，然后使用Handler处理请求的结果数据
		new Thread(r).start();
	}

	private Runnable r = new Runnable() {
		@Override
		public void run() {
			try {
				List<Student> data = new ArrayList<Student>();
				// 由于请求是异步的，延时2s，表示请求过程有2s延时
				Thread.sleep(2000);
				Student student = null;
				for (int i = 0; i < 15; i++) {
					student = new Student();
					student.name = "Student " + ((page-1)*15 + i);
					data.add(student);
				}
				// 将请求的结果数据抛给Handler消息队列，在UI线程中渲染ListView视图
				handler.sendMessage(handler.obtainMessage(1, data));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

}
