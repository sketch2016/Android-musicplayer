package com.sprd.easymusic.myview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class MyProgressBar extends View {
	private int x, y;
	private int width, height;
	private int progress;
	private int maxProgress;
	
	public MyProgressBar(Context context) {
		super(context);
		maxProgress = 100;
	}

	public MyProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		maxProgress = 100;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
		canvas.drawRect(x, y, width, height, paint);
		paint.setColor(Color.MAGENTA);
		canvas.drawRect(x, y, width*progress/maxProgress, height, paint);
	}
	
	public void setProgress(int progress) {
		this.progress = progress;
		//this.invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w;
		height = h;
	}

	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
	}


}
