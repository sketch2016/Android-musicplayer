package com.sprd.easymusic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sprd.easymusic.fragment.DownloadFragment;
import com.sprd.easymusic.fragment.MusicListFragment;
import com.sprd.easymusic.fragment.NetFragment;
import com.sprd.easymusic.fragment.StoredSongFragment;
import com.sprd.easymusic.mysql.MyDBHelper;
import com.sprd.easymusic.service.DownloadService;
import com.sprd.easymusic.service.PlayMusicService;
import com.sprd.easymusic.util.DownloadUtil;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		MusicListFragment.CallBack, StoredSongFragment.CallBack,DownloadFragment.CallBack,
		View.OnClickListener {
	private String TAG = "MainActivity";
	private final int FRAGMENT_COUNT = 4;
	// dbMusic保存媒体库中的所有音乐
	private ViewPager mViewPager;
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private List<Map<String, Object>> allMusic = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> storedMusic = new ArrayList<Map<String, Object>>();
	private Context mContext;
	private TextView fragmentTitle1, fragmentTitle2, fragmentTitle3,
			fragmentTitle4, titleBottomLine;
	private int screenWidth, bottomLineWidth;
	private PlayMusicService playService;
	private DownloadService downloadService;
	private MyDBHelper myHelper;
	public static final String downloadedPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/musicplayer";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		myHelper = new MyDBHelper(mContext, "easyMusic.db3", null, 1);
		createFileDir();
		getAllMusicFromDb();
		getStoredMusic(storedMusic, myHelper);
		getBottomLineWidth();
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		fragmentTitle1 = (TextView) findViewById(R.id.fragment1);
		fragmentTitle1.setOnClickListener(this);
		fragmentTitle2 = (TextView) findViewById(R.id.fragment2);
		fragmentTitle2.setOnClickListener(this);
		fragmentTitle3 = (TextView) findViewById(R.id.fragment3);
		fragmentTitle3.setOnClickListener(this);
		fragmentTitle4 = (TextView) findViewById(R.id.fragment4);
		fragmentTitle4.setOnClickListener(this);
		titleBottomLine = (TextView) findViewById(R.id.fragmentTitle);
		bindToService();
		bindToDownloadService();
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			public void onPageSelected(int position) {

			}

			public void onPageScrolled(int item, float percent, int offset) {
				titleBottomLine.setX(item * bottomLineWidth + offset
						/ FRAGMENT_COUNT);
			}

			public void onPageScrollStateChanged(int position) {

			}
		});
	}
	
	private void createFileDir() {
		File musciPlayer = new File(downloadedPath);
		if (!musciPlayer.exists()) {
			try {
				musciPlayer.mkdir();
				File lrc = new File(downloadedPath + "/lrc");
				if (!lrc.exists()) lrc.mkdir();
				File album = new File(downloadedPath + "/album");
				if (!album.exists()) album.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void getBottomLineWidth() {
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;
		bottomLineWidth = screenWidth / FRAGMENT_COUNT;

	}

	// 绑定服务时的ServiceConnection参数
	private ServiceConnection conn = new ServiceConnection() {

		// 绑定成功后该方法回调，并获得服务端IBinder的引用
		public void onServiceConnected(ComponentName name, IBinder service) {
			// 通过获得的IBinder获取PlayMusicService的引用
			playService = ((PlayMusicService.MusicBinder) service).getService();
			Toast.makeText(mContext, "onServiceConnected:playService", Toast.LENGTH_LONG)
					.show();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected:playService");
		}

	};
	
	// 绑定服务时的ServiceConnection参数
		private ServiceConnection conn1 = new ServiceConnection() {

			// 绑定成功后该方法回调，并获得服务端IBinder的引用
			public void onServiceConnected(ComponentName name, IBinder service) {
				// 通过获得的IBinder获取PlayMusicService的引用
				//playService = ((PlayMusicService.MusicBinder) service).getService();
				downloadService = ((DownloadService.DownloadBinder)service).getDownloadService();
				Toast.makeText(mContext, "onServiceConnected:downloadService", Toast.LENGTH_LONG)
						.show();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected:downloadService");
			}

		};

	// 绑定服务PlayMusicService
	private void bindToService() {
		bindService(new Intent(mContext,
				com.sprd.easymusic.service.PlayMusicService.class), conn,
				Service.BIND_AUTO_CREATE);
	}
	
	public void bindToDownloadService() {
		bindService(new Intent(mContext,
				com.sprd.easymusic.service.DownloadService.class), conn1,
				Service.BIND_AUTO_CREATE);
	}

	// 通过获得的PlayMusicService引用调用播放音乐的方法，方法传进去的参数为音乐url
	protected void playMusic(String url) {
		if (playService != null) {
			playService.play(url);
		}
	}

	// 从媒体库中查询音乐
	private void getAllMusicFromDb() {
		if (allMusic.size() > 0)
			allMusic.clear();
		Cursor musicCursor1 = this
				.getContentResolver()
				.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
						null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		// 从外部存储获取
		getMusic(musicCursor1);
		Cursor musicCursor2 = this
				.getContentResolver()
				.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, null,
						null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		// 从内部存储获取
		getMusic(musicCursor2);
	}

	// 检查当前歌曲是否在收藏列表中，在收藏列表返回true，否则返回false
	protected boolean checkIfStored(String url) {
		for (Map<String, Object> map : storedMusic) {
			if (url.equals((String) map.get("url"))) {
				return true;
			}
		}
		return false;
	}

	// 获取到的音乐以Map的形式存储在dbMusic中
	private void getMusic(Cursor musicCursor) {
		while (musicCursor.moveToNext()) {
			Map<String, Object> item = new HashMap<String, Object>();
			long id = musicCursor.getLong(musicCursor
					.getColumnIndex(MediaStore.Audio.Media._ID));
			String title = musicCursor.getString(musicCursor
					.getColumnIndex(MediaStore.Audio.Media.TITLE));
			String artist = musicCursor.getString(musicCursor
					.getColumnIndex(MediaStore.Audio.Media.ARTIST));
			if (artist != null && artist.equals("<unknown>")) {
				continue;
			}
			long duration = musicCursor.getLong(musicCursor
					.getColumnIndex(MediaStore.Audio.Media.DURATION));
			long size = musicCursor.getLong(musicCursor
					.getColumnIndex(MediaStore.Audio.Media.SIZE));
			String url = musicCursor.getString(musicCursor
					.getColumnIndex(MediaStore.Audio.Media.DATA));
			int isMusic = musicCursor.getInt(musicCursor
					.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
			if (isMusic != 0) {
				item.put("id", id);
				item.put("title", title);
				item.put("artist", artist);
				item.put("duration", formatDuration(duration));
				item.put("size", size);
				item.put("url", url);
				Log.d("MainActivity", "MusicTitle = " + title);
				Log.d("MainActivity", "MusicArtist = " + artist);
				Log.d("MainActivity", "MusicUrl = " + url);
				allMusic.add(item);
			}

		}
	}

	// 将音乐时长转换为00:00格式
	public static String formatDuration(long dur) {
		long totalSecond = dur / 1000;
		String minute = totalSecond / 60 + "";
		if (minute.length() < 2)
			minute = "0" + minute;
		String second = totalSecond % 60 + "";
		if (second.length() < 2)
			second = "0" + second;
		return minute + ":" + second;
	}

	// 从数据库中查询已收藏音乐
	private void getStoredMusic(List<Map<String, Object>> storedMusic,
			MyDBHelper myHelper) {
		if (storedMusic.size() > 0)
			storedMusic.clear();
		Cursor cursor = myHelper.getReadableDatabase().rawQuery(
				"select * from stored_music", null);
		while (cursor.moveToNext()) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("title", cursor.getString(1));
			item.put("artist", cursor.getString(2));
			item.put("duration", cursor.getString(3));
			item.put("url", cursor.getString(4));
			storedMusic.add(item);
		}
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch (position) {
			case 0:
				fragment = new MusicListFragment();
				break;
			case 1:
				fragment = new StoredSongFragment();
				break;
			case 2:
				fragment = new NetFragment();
				break;
			case 3:
				fragment = new DownloadFragment();
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return FRAGMENT_COUNT;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			case 3:
				return getString(R.string.title_section4).toUpperCase(l);
			}
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemSelected(String url) {
		playMusic(url);
	}
	
	@Override
	public List<Map<String, Object>> getAllMusic() {
		return allMusic;
	}
	
	@Override
	public List<Map<String, Object>> getStoredMusic() {
		return storedMusic;
	}
	
	public void executeDownloadUtil(DownloadUtil util) {
		Toast.makeText(mContext, "executeDownloadUtil", Toast.LENGTH_LONG)
		.show();
		downloadService.downloadMusic(util);
	}

	@Override
	public void onClick(View v) {
		if (v == fragmentTitle1) {
			mViewPager.setCurrentItem(0);
		} else if (v == fragmentTitle2) {
			mViewPager.setCurrentItem(1);
		} else if (v == fragmentTitle3) {
			mViewPager.setCurrentItem(2);
		} else if (v == fragmentTitle4) {
			mViewPager.setCurrentItem(3);
		}
	}

	@Override
	protected void onDestroy() {
		this.unbindService(conn);
		this.unbindService(conn1);
		super.onDestroy();
	}
}
