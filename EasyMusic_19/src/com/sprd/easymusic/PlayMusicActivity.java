package com.sprd.easymusic;

import com.sprd.easymusic.service.MusicService;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class PlayMusicActivity extends Activity implements MusicService.Watcher {
	private static final String TAG = "PlayMusicActivity";
	private static final String ACTION_NEXT_SONG = "action.nextsong";
	private static final String ACTION_PAUSE = "action.pause";
	private static final String ACTION_PRE_SONG = "action.presong";
	private static final String ACTION_PLAY_SONG = "action.playsong";
	private static final String ACTION_CONTINUE_PLAYING_SONG = "action.continueplaying";
	private static final String ACTION_NOTIFY_PLAYACTIVITY = "com.sprd.easymusic.updataplaymusic";
	private ImageView pre, playAndPause, next, cycleChoice;
	private TextView title, artist, duration, playedTimeView;
	private SeekBar seekBar;
	private boolean isPlaying;
	private boolean pause;
	private MusicService musicService;
	private Context mContext;
	private String currentMusicTitle, currentMusicArtist;
	private int currentMusicDuration;
	private int touchX, touchY;
	private int xSpeed, ySpeed;
	private long startTime, stopTime;
	private Handler myHandler;
	private static final int UPDATE_PROGRESS = 1;
	

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play);
		mContext = this;
		bindToService();
		registerPlayReceiver();
		ActionBar bar = this.getActionBar();
		bar.setHomeButtonEnabled(true);
		bar.setDisplayHomeAsUpEnabled(true);
		getPlayMusicInfo(this.getIntent());
		pre = (ImageView) findViewById(R.id.pre);
		pre.setOnClickListener(musicClickListener);
		playAndPause = (ImageView) findViewById(R.id.playAndpause);
		playAndPause.setOnClickListener(musicClickListener);
		next = (ImageView) findViewById(R.id.next);
		next.setOnClickListener(musicClickListener);
		cycleChoice = (ImageView) findViewById(R.id.cycleview);
		cycleChoice.setOnClickListener(musicClickListener);
		title = (TextView) findViewById(R.id.title);
		artist = (TextView) findViewById(R.id.artist);
		duration = (TextView) findViewById(R.id.duration);
		playedTimeView = (TextView) findViewById(R.id.playedtime);
		seekBar = (SeekBar) findViewById(R.id.musicProgress);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					musicService.changePlayProgress(progress);
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
		});
		myHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPDATE_PROGRESS:
					int progress = (Integer)msg.obj;
					seekBar.setProgress(progress);
					//long playedTime = currentMusicDuration * progress / 100;
					playedTimeView.setText(MainActivity.formatDuration(progress));
					Log.d(TAG, "update progress success!");
					break;
				default:
					break;
				}
			}
		};
	}
	
	private void registerPlayReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_NOTIFY_PLAYACTIVITY);
		this.registerReceiver(playReceiver, filter);
	}

	// 绑定服务时的ServiceConnection参数
		private ServiceConnection conn = new ServiceConnection() {

			// 绑定成功后该方法回调，并获得服务端IBinder的引用
			public void onServiceConnected(ComponentName name, IBinder service) {
				// 通过获得的IBinder获取PlayMusicService的引用
				musicService = ((MusicService.MusicBinder) service).getService();
				musicService.addWatcher(PlayMusicActivity.this);
				Toast.makeText(mContext, "onServiceConnected:musicService", Toast.LENGTH_LONG)
						.show();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected:musicService");
			}

		};

		// 绑定服务MusicService
		private void bindToService() {
			bindService(new Intent(mContext,
					com.sprd.easymusic.service.MusicService.class), conn,
					Service.BIND_AUTO_CREATE);
		}

	private void getPlayMusicInfo(Intent intent) {
		Bundle data = intent.getExtras();
		currentMusicTitle = data.getString("title");
		currentMusicArtist = data.getString("artist");
		currentMusicDuration = (int)data.getLong("duration");
		isPlaying = data.getBoolean("isPlaying");
	}

	protected void onStart() {
		updataPlayState();
		super.onStart();
	}
	
	

	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void updataPlayState() {
		title.setText(currentMusicTitle);
		artist.setText(currentMusicArtist);
		duration.setText(MainActivity.formatDuration(currentMusicDuration));
		seekBar.setMax((int)currentMusicDuration);
		if (isPlaying) {
			playAndPause.setImageResource(android.R.drawable.ic_media_pause);
		} else {
			playAndPause.setImageResource(android.R.drawable.ic_media_play);
		}
	}
	
	private void changeMusicState(Intent intent) {
		Log.d(TAG, "changeMusicState:action = " + intent.getAction());
		this.sendBroadcast(intent);
	}
	
	private OnClickListener musicClickListener = new OnClickListener() {
		public void onClick(View v) {
			if (v == pre) {
				isPlaying = true;
				pause = false;
				Intent intent = new Intent(ACTION_PRE_SONG);
				changeMusicState(intent);
			} else if (v == playAndPause) {
				if (pause) {
					isPlaying = true;
					pause = false;
					Intent intent = new Intent(ACTION_CONTINUE_PLAYING_SONG);
					changeMusicState(intent);
					return;
				}
				if (isPlaying) {
					isPlaying = false;
					pause = true;
					Intent intent = new Intent(ACTION_PAUSE);
					changeMusicState(intent);
				} else {
					isPlaying = true;
					Intent intent = new Intent(ACTION_PLAY_SONG);
					changeMusicState(intent);
				}
			} else if (v == next) {
				isPlaying = true;
				pause = false;
				Intent intent = new Intent(ACTION_NEXT_SONG);
				changeMusicState(intent);
			} else if (v == cycleChoice) {
				
			}
			
			updataPlayState();
		}
		
	};


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchX = (int)event.getX();
			touchY = (int)event.getY();
			startTime = System.currentTimeMillis();
			break;
		case MotionEvent.ACTION_MOVE:
			//computeSpeed(event);
			break;
		case MotionEvent.ACTION_UP:
			//checkState();
			stopTime = System.currentTimeMillis();
			computeSpeed(event);
			checkState();
			break;
		}
		//computeSpeed(event);
		return super.onTouchEvent(event);
	}

	private void computeSpeed(MotionEvent event) {
//		VelocityTracker velocityTracker = VelocityTracker.obtain();
//		velocityTracker.addMovement(event);
//		velocityTracker.computeCurrentVelocity(100);
//		xSpeed = (int)velocityTracker.getXVelocity(100);
//		ySpeed = (int)velocityTracker.getYVelocity(100);
		xSpeed =(int) ((event.getX() - touchX) * 1000 / (stopTime - startTime));
		ySpeed =(int) ((event.getY() - touchY) * 1000 / (stopTime - startTime));
		Log.d(TAG, "xSpeed = " + xSpeed + " ySpeed = " + ySpeed);
	}
	
	private void checkState() {
		if ( touchX < 300 && xSpeed > 500 && xSpeed >= ySpeed) {
			touchX = 0;
			xSpeed = 0;
			ySpeed = 0;
			Intent intent = new Intent(this, MainActivity.class);
			this.startActivity(intent);
		}
	}

	@Override
	public void update(int currentProgress) {
		myHandler.sendMessage(myHandler.obtainMessage(UPDATE_PROGRESS, currentProgress));
	}
	
	private BroadcastReceiver playReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_NOTIFY_PLAYACTIVITY)) {
				getPlayMusicInfo(intent);
				updataPlayState();
			}
		}
		
	};


}
