package com.sprd.easymusic.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sprd.easymusic.MainActivity;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class DownloadTask extends AsyncTask<String, Integer, Void> {
	private final String TAG = "DownloadTask";
	private ImageView pause;
	private ImageView delete;
	private ProgressBar progressBar;
	private String title = null;
	private String artist = null;
	private RandomAccessFile targetFile;
	private int fileSize;
	private int downloadedSize;
	private boolean isPause;
	//private String artist = null;
	//private String url = null;
	
	public DownloadTask(String title, ImageView pause, ImageView delete, ProgressBar progressBar) {
		this.title = title;
		this.pause = pause;
		this.delete =delete;
		this.progressBar = progressBar;
	}
	
	public DownloadTask(String title, String artist, RandomAccessFile targetFile) {
		this.title = title;
		this.artist = artist;
		this.targetFile = targetFile;
	}

	@Override
	protected Void doInBackground(String... params) {
		String url = params[0];
		try {
			targetFile = new RandomAccessFile(new File(MainActivity.downloadedPath + title + ".mp3"), "rwd");
			HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
			fileSize = conn.getContentLength();
			conn.setConnectTimeout(5000);
			BufferedInputStream bi = new BufferedInputStream(conn.getInputStream());
			int read = 0;
			byte[] buff = new byte[1024];
			while ((read = bi.read(buff)) > 0) {
				targetFile.write(buff, 0, read);
				downloadedSize += read;
				publishProgress(downloadedSize);
			}
			Log.d(TAG, "下载完成");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void onPreExecute() {
		super.onPreExecute();
	}

	protected void onPostExecute(Void result) {
		
		super.onPostExecute(result);
	}

	protected void onProgressUpdate(Integer... values) {
		int progress  = values[0] / fileSize * 100;
		progressBar.setProgress(progress);
		super.onProgressUpdate(values);
	}

	protected void onCancelled(Void result) {
		super.onCancelled(result);
	}

	protected void onCancelled() {
		super.onCancelled();
	}

}
