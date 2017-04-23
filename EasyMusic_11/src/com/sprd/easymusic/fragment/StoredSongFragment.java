package com.sprd.easymusic.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sprd.easymusic.R;
import com.sprd.easymusic.myview.RefreshableListView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class StoredSongFragment extends Fragment {
	private final String TAG = "StoredSongFragment";
	private final String ACTION_REFRESH = "action.refreshmusicList";
	private List<Map<String, Object>> storedMusic = new ArrayList<Map<String, Object>>();
	private ListView storedMusicList;
	private CallBack mCallBack;
	private LayoutInflater inflater;
	private Context mContext;

	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallBack = (CallBack) activity;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this.getActivity();
		inflater = LayoutInflater.from(mContext);
		storedMusic = mCallBack.getStoredMusic();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_REFRESH);
		mContext.registerReceiver(refreshMusicReceiver, filter);
		Log.d(TAG, "onCreate");
	}

	// StoredSongFragment向外界展示的内容，返回值为view
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater
				.inflate(R.layout.storedmusiclist, container, false);
		storedMusicList = (ListView) view.findViewById(R.id.storedmusicList);
		storedMusicList.setAdapter(musicListAdapter);
		storedMusicList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mCallBack.onItemSelected((String) storedMusic.get(position).get("url"));
				TextView title = (TextView) view.findViewWithTag("title");
				Toast.makeText(mContext, "title = " + title.getText().toString(),Toast.LENGTH_SHORT).show();
			}
		});
		return view;
	}

	// 音乐列表适配器
	private BaseAdapter musicListAdapter = new BaseAdapter() {

		public int getCount() {
			return storedMusic.size();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			Map<String, Object> item = storedMusic.get(position);
			if (convertView == null) {
				view = inflater.inflate(R.layout.storedmusiclist_item, null);
			}
			TextView musicTitle = (TextView) view.findViewById(R.id.musicTitle);
			musicTitle.setTag("title");
			TextView musicArtist = (TextView) view.findViewById(R.id.musicArtist);
			musicTitle.setText((String) item.get("title"));
			musicArtist.setText((String) item.get("artist"));
			return view;
		}

	};

	public void onDestroy() {
		if (refreshMusicReceiver != null) mContext.unregisterReceiver(refreshMusicReceiver);
		super.onDestroy();
	}

	public interface CallBack {
		public void onItemSelected(String url);

		public List<Map<String, Object>> getStoredMusic();
	}
	
	private BroadcastReceiver refreshMusicReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_REFRESH)) {
				musicListAdapter.notifyDataSetChanged();
				Toast.makeText(mContext, "刷新收藏列表成功！", 100).show();
			}
		}
		
	};

}
