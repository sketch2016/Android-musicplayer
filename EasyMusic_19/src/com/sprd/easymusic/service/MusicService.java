package com.sprd.easymusic.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sprd.easymusic.MainActivity;
import com.sprd.easymusic.util.DownloadUtil;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MusicService extends Service {
	private final String TAG = "PlayMusicService";
	private MediaPlayer mPlayer = new MediaPlayer();
	// 执行下载任务的线程池
	private ExecutorService pool;
	// 这里将线程池最大下载任务设置为3，设置太大会造成资源浪费
	private int DownloadLimit = 3;
	private Handler handler;
	private Timer timer;
	private List<Watcher> watcherList = new ArrayList<Watcher>();

	@Override
	public void onCreate() {
		super.onCreate();
		// 下载线程池的初始化
		pool = Executors.newFixedThreadPool(DownloadLimit);
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if (mPlayer.isPlaying()) {
					Log.d(TAG, "start update proress...");
					//int progress = mPlayer.getCurrentPosition() * 100 / mPlayer.getDuration();
					int progress = mPlayer.getCurrentPosition();
					for (Watcher watcher : watcherList) {
						watcher.update(progress);
					}
				}
			}
			
		}, 0, 300);
		Log.d(TAG, "onCreate");
	}

	/*
	 * MainActivity调用bindService后该方法回调，紧接着MainActivity的ServiceConnection的
	 * onServiceConnected方法回调，onBind回调的返回值传递给onServiceConnected中的参数service
	 * 从而MainActivity就可以通过Binder的getService方法获得PlayMusicService的引用，后续的音乐播放
	 * 控制就简单了
	 */
	public IBinder onBind(Intent intent) {
		Toast.makeText(this, "onBind", Toast.LENGTH_LONG).show();
		return new MusicBinder();
	}

	public class MusicBinder extends Binder {

		public MusicService getService() {
			return MusicService.this;
		}
	}

	public void play(String url) {
		try {
			mPlayer.reset();
			Log.d("MusicService", "play reset ");
			mPlayer.setDataSource(url);
			Log.d("MusicService", "play setDataSource ");
			mPlayer.prepare();
			Log.d("MusicService", "play prepare ");
			mPlayer.start();
			Log.d("MusicService", "play start ");
			mPlayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer player) {
					handler.sendMessage(handler.obtainMessage(MainActivity.AUTO_CHANGE_SONG));
					Log.d("MusicService", "sendBroadcast->nextsong");
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pauseMusic() {
		mPlayer.pause();
	}
	
	public void continuePlaying() {
		mPlayer.start();
	}
	
	public void changePlayProgress(int pos) {
		mPlayer.seekTo(pos);
	}
	
	public void setHandler(Handler handler) {
		this.handler = handler;
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
	
	public void addWatcher(Watcher watcher) {
		watcherList.add(watcher);
	}
	
	public void removeWatcher(Watcher watcher) {
		watcherList.remove(watcher);
	}
	
	public interface Watcher {
		public void update(int currentProgress);
	}

}
