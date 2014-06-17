package com.king.demo.adapter;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.king.demo.R;
import com.king.demo.model.Image;
import com.king.refresh.PageAndRefreshBaseAdapter;
import com.king.refresh.PageAndRefreshRequestService;
import com.nostra13.universalimageloader.cache.disc.impl.TotalSizeLimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class WaterfallListAdapter extends PageAndRefreshBaseAdapter {

	/**
	 * Context
	 */
	private Context mContext;
	
	/**
	 * 布局解析器
	 */
	private LayoutInflater mInflater;
	
	/**
	 * 屏幕宽度
	 */
	private int mScreenWidth;
	
	public WaterfallListAdapter(Context context, PageAndRefreshRequestService requestService) {
		super(requestService);
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		mScreenWidth = ((Activity)mContext).getWindowManager().getDefaultDisplay().getWidth();
		initImageLoader(context.getApplicationContext());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.item_image, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		Image image = (Image) getItem(position);
		ImageLoader.getInstance().displayImage(image.url, holder.image);
		// 计算图片显示高度
		LayoutParams lp = holder.image.getLayoutParams();
		lp.width = (mScreenWidth - 4 * 10)/2;
		lp.height = lp.width * image.height / image.width;
		holder.image.setLayoutParams(lp);
		
		return convertView;
	}

	@Override
	public int getCount() {
		return super.getCount();
	}
	
	/**
	 * 初始化异步加载器
	 * @param context
	 */
	public void initImageLoader(Context context) {
		
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_launcher)
		.showImageOnFail(R.drawable.ic_launcher)
		.showImageForEmptyUri(R.drawable.ic_launcher)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
		
		//定义文件目录
		String mFileDir = Environment.getExternalStorageDirectory() + "refresh/";
		
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.defaultDisplayImageOptions(defaultOptions)
				.discCache(new TotalSizeLimitedDiscCache(new File(mFileDir), 10*1024*1024))
//				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
	
	/**
	 * 视图缓存
	 * @author King
	 * @since 2014-3-31 下午5:50:31
	 */
	private static class ViewHolder{
		
		/**
		 * 图片
		 */
		ImageView image;
		
	}
}
