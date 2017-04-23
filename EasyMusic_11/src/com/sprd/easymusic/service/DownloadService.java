package com.sprd.easymusic.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sprd.easymusic.util.DownloadUtil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class DownloadService extends Service {
	private Context mContext;
	//执行下载任务的线程池
	private ExecutorService pool;
	//这里将线程池最大下载任务设置为3，设置太大会造成资源浪费
	private int DownloadLimit = 3;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		//下载线程池的初始化
		pool = Executors.newFixedThreadPool(DownloadLimit);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return new DownloadBinder();
	}
	
	public class DownloadBinder extends Binder {
		public DownloadService getDownloadService() {
			return DownloadService.this;
		}
	}
	
	public void downloadMusic(final DownloadUtil util) {
		pool.execute(new DownloadRunnable(util));
	}
	
	private class DownloadRunnable implements Runnable {
		private DownloadUtil util;
		
		public DownloadRunnable(DownloadUtil util) {
			this.util = util;
		}

		@Override
		public void run() {
			util.download();
		}
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
