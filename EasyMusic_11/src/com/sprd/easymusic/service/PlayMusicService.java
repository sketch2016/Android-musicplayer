package com.sprd.easymusic.service;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class PlayMusicService extends Service {
	private final String TAG = "PlayMusicService";
	private MediaPlayer mPlayer = new MediaPlayer();

	@Override
	public void onCreate() {
		super.onCreate();
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
		
		public PlayMusicService getService() {
			return PlayMusicService.this;
		}
	}
	
	public void play(String url) {
//		myPlayThread.setUrl(url);
//		myPlayThread.start();
		if (mPlayer.isPlaying()) {
			mPlayer.stop();
		}
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
					Intent completeIntent = new Intent();
					completeIntent.setAction("action.nextsong");  
                    sendBroadcast(completeIntent);
                    Log.d("MusicService", "sendBroadcast->nextsong");
				}				
			});		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
