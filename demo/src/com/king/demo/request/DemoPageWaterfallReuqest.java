package com.king.demo.request;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;

import com.king.demo.model.Image;
import com.king.refresh.PageAndRefreshRequestCallBack;
import com.king.refresh.PageAndRefreshRequestService;

/**
 * 分页请求Request示例<br><br>
 */
public class DemoPageWaterfallReuqest implements PageAndRefreshRequestService {

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
					mCallBack.onRequestComplete((List<Image>) msg.obj, 5);
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
				List<Image> data = new ArrayList<Image>();
				// 由于请求是异步的，延时2s，表示请求过程有2s延时
				Thread.sleep(2000);
				for (int i = 0; i < 2; i++) {
					Image image = new Image();
					image.url = "http://img0.bdstatic.com/img/image/shouye/bzhy-9424714072.jpg";
					image.width = 800;
					image.height = 1000;
					data.add(image);
					
					image = new Image();
					image.url = "http://img0.bdstatic.com/img/image/shouye/bzsjb-12218402224.jpg";
					image.width = 450;
					image.height = 946;
					data.add(image);
					
					image = new Image();
					image.url = "http://img0.bdstatic.com/img/image/shouye/sjpmsj-9630647162.jpg";
					image.width = 698;
					image.height = 1000;
					data.add(image);
					
					image = new Image();
					image.url = "http://img0.bdstatic.com/img/image/shouye/dengni37.jpg";
					image.width = 631;
					image.height = 946;
					data.add(image);
					
					image = new Image();
					image.url = "http://img0.bdstatic.com/img/image/shouye/xiu8.jpg";
					image.width = 1000;
					image.height = 300;
					data.add(image);
					
					image = new Image();
					image.url = "http://img0.bdstatic.com/img/image/shouye/bzmwyj-11681019223.jpg";
					image.width = 698;
					image.height = 250;
					data.add(image);
					
					image = new Image();
					image.url = "http://img0.bdstatic.com/img/image/shouye/qchc-9562164448.jpg";
					image.width = 631;
					image.height = 946;
					data.add(image);
					
					
					image = new Image();
					image.url = "http://img2.bdstatic.com/img/image/shouye/lylj02.jpg";
					image.width = 631;
					image.height = 946;
					data.add(image);
					
					image = new Image();
					image.url = "http://imgt7.bdstatic.com/it/u=2,2860306715&fm=19&gp=0.jpg";
					image.width = 698;
					image.height = 1000;
					data.add(image);
					
					image = new Image();
					image.url = "http://img0.bdstatic.com/img/image/shouye/dengni37.jpg";
					image.width = 631;
					image.height = 946;
					data.add(image);
				}
				// 将请求的结果数据抛给Handler消息队列，在UI线程中渲染ListView视图
				handler.sendMessage(handler.obtainMessage(1, data));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

}
