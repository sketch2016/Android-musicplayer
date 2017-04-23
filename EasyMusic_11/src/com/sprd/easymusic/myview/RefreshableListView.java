package com.sprd.easymusic.myview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class RefreshableListView extends ListView implements OnScrollListener {
	private LayoutInflater inflater;
	
	public RefreshableListView(Context context) {
		super(context);
		initHeaderView(context);
	}
	
	//必须实现的方法，否则xml文件解析出错
	public RefreshableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHeaderView(context);
	}

	private void initHeaderView(Context context) {
		inflater = LayoutInflater.from(context);
		TextView tv = new TextView(context);
		tv.setText("header");
		this.addHeaderView(tv);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
	}

}
