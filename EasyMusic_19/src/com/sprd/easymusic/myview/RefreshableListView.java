package com.sprd.easymusic.myview;

import com.sprd.easymusic.MainActivity;
import com.sprd.easymusic.R;
import com.sprd.easymusic.fragment.MusicListFragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView.LayoutParams;
import android.widget.Toast;
import android.view.View.OnTouchListener;

public class RefreshableListView extends ListView implements OnScrollListener, OnTouchListener {
	private static final String TAG = "RefreshableListView";
	private LayoutInflater inflater;
	private LinearLayout header;
	private RelativeLayout headerView;
	private TextView refreshText, refreshTime;
	private ImageView arrow;
	private ProgressBar bar;
	private int detY = 0;
	private int temY = 0;
	private int headerHeight;
	private boolean refreshFinish;
	private boolean isRefreshing;
	private boolean canRefresh;
	private Handler handler;
	private Handler refreshHandler;
	private static final int REFRESH_COMPLETE = 1;
	private static final int REFRESH_FINISH = 2;
	
	public RefreshableListView(Context context) {
		super(context);
		initHeaderView(context);
	}
	
	//必须实现的方法，否则xml文件解析出错
	public RefreshableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHeaderView(context);
	}

	private void initHeaderView(final Context context) {
		inflater = LayoutInflater.from(context);
		header = (LinearLayout)inflater.inflate(R.layout.header_view, this, false);
		headerView = (RelativeLayout) header.findViewById(R.id.header_view);
		setHeaderViewHeight();
		//header.setY(-headerHeight);
		//this.setY(-headerHeight);
		refreshText = (TextView) header.findViewById(R.id.refreshText);
		refreshText.setText("下拉可以刷新");
		bar = (ProgressBar) header.findViewById(R.id.progressBar1);
		bar.setVisibility(View.GONE);
		arrow = (ImageView) header.findViewById(R.id.arrow);
		headerView.setVisibility(View.GONE);
		this.addHeaderView(header);
		this.setOnTouchListener(this);
		this.setOnScrollListener(this);
		handler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case REFRESH_COMPLETE:
					bar.setVisibility(View.INVISIBLE);
					refreshText.setText("刷新成功");
					//Toast.makeText(context, "刷新成功", 300).show();
					sendMessageDelayed(handler.obtainMessage(REFRESH_FINISH), 300);
					break;
				case REFRESH_FINISH:
					isRefreshing = false;
					headerView.setVisibility(View.GONE);
					arrow.setVisibility(View.VISIBLE);
					bar.setVisibility(View.GONE);
					refreshText.setText("下拉可以刷新");
					break;
				default:
					break;
				}
			}
			
		};
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		Log.d(TAG, "onScrollStateChanged: scrollState = " + scrollState);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		Log.d(TAG, "onScroll"); 
		canRefresh = firstVisibleItem > 0 ? false : true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (isRefreshing || !canRefresh) return false;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			temY = (int)event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			detY = (int)event.getY() - temY;
			detY = detY > 600 ? 600 : detY;
			applyChange();
			break;
		case MotionEvent.ACTION_UP:
			refreshFinish = true;
			checkIfNeedRefresh();
			detY = 0;
			temY = 0;
			break;
		}
		return false;
	}

	private void checkIfNeedRefresh() {
		header.setPadding(0, 0, 0, 0);
		if (detY > 200) {
			startRefreshing();
		} else {
			headerView.setVisibility(View.GONE);
			refreshText.setText("下拉可以刷新");
		}
	}

	private void startRefreshing() {
		arrow.setVisibility(View.GONE);
		bar.setVisibility(View.VISIBLE);
		refreshText.setText("正在刷新");
		isRefreshing = true;
		refreshMusic();
	}
	
	public void refreshComplete() {
		handler.sendMessage(handler.obtainMessage(REFRESH_COMPLETE));
	}

	private void refreshMusic() {
		if (refreshHandler != null) {
			refreshHandler.sendMessage(refreshHandler.obtainMessage(MusicListFragment.REFERSH_MUSIC));
		}
	}

	private void applyChange() {
		Log.d(TAG, "detY = " + detY);
		//this.setY(-headerHeight + detY);
		if (detY <= 20) return;
		//header.setY(-headerHeight + detY/3);
		Log.d(TAG, "detY = " + detY);
		arrow.setVisibility(View.VISIBLE);
		headerView.setVisibility(View.VISIBLE);
		header.setPadding(0, detY/3, 0, 0);
		if (detY > 200) {
			refreshText.setText("释放立即刷新");
			arrow.setImageResource(R.drawable.refresh2);
		} else {
			refreshText.setText("下拉可以刷新");
			arrow.setImageResource(R.drawable.refresh1);
		}
	}

	public void setHandler(Handler refreshHandler) {
		this.refreshHandler = refreshHandler;
	}

	public boolean isRefreshing() {
		return isRefreshing;
	}

	public void setHeaderViewHeight() {
		header.measure(0, 0);
		headerHeight = header.getMeasuredHeight();
		Log.d(TAG, "headerHeight = " + headerHeight + "position-y = " + header.getY());
	}

}
