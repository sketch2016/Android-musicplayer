package com.sprd.easymusic.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class DownloadUtil {
	private String TAG = "DownloadUtil";
	private String targetUrl = null;
	private String targetFile = null;
	private int threadNum;
	private Context mContext;
	private int fileSize;
	private boolean pause;
	private boolean delete;
	//private boolean 
	private DownloadThread[] downloadThreads;
	
	public DownloadUtil (String targetUrl, String targetFile, int threadNum, Context context) {
		this.targetUrl = targetUrl;
		this.targetFile = targetFile;
		this.threadNum = threadNum;
		downloadThreads = new DownloadThread[threadNum];
		mContext = context;
	}
	
	public void download() {
		try {
			URL url = new URL(targetUrl);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(5000);
			fileSize = conn.getContentLength();
			conn.disconnect();
			Log.d(TAG, "download：fileSize = " + fileSize);
			File file = new File(targetFile);
			if (!file.exists()) {
				file.createNewFile();
				Log.d(TAG, "create file:" + file);
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rwd");
			raf.setLength(fileSize);
			raf.close();
			for (int i=0; i<threadNum; i++) {
				downloadThreads[i] = new DownloadThread(targetUrl, targetFile);
				downloadThreads[i].start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getDownloadProgress() {
		int downloadLength = 0;
		for (DownloadThread thread : downloadThreads) {
			if (thread != null) downloadLength += thread.length;
		}
		Log.d(TAG, "fileSize = " + fileSize + " downloadLength = " +downloadLength);
		return fileSize > 0 ? downloadLength * 100 / fileSize: 0;
	}
	
	public boolean isPause() {
		return pause;
	}

	public void setPause(boolean pause) {
		this.pause = pause;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	private class DownloadThread extends Thread {
		public int length;
		private String downloadUrl = null;
		private String file;
		private BufferedInputStream bis;
		
		public DownloadThread (String downloadUrl, String file) {
			this.downloadUrl = downloadUrl;
			this.file = file;
		}

		public void run() {
			try {
				HttpURLConnection conn = (HttpURLConnection)new URL(downloadUrl).openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestMethod("GET");
				bis = new BufferedInputStream(conn.getInputStream());
				File downloadFile = new File(file);
				RandomAccessFile raf = new RandomAccessFile(downloadFile, "rwd");
				int hasRead = 0;
				byte[] buff = new byte[1024*4];
				while ((hasRead = bis.read(buff)) > 0 && !delete) {
					while (pause){
						Log.d(TAG, "DownloadUtil pause!");
					}
					if (!pause) {
						raf.write(buff, 0, hasRead);
						length += hasRead;
						Log.d(TAG, "read " + hasRead + " bytes");
					}
				}
				Log.d(TAG, "download success? " + (fileSize == length));
				raf.close();
				bis.close();
				if (delete) {
					boolean isDeleted = downloadFile.delete();
					Log.d(TAG, "delete file success ? " + isDeleted);
				} else {
					Log.d(TAG, "run:fileSize = " + fileSize + " downloadLength = " + length);
					if (fileSize == length) {
						Log.d(TAG, "download success");
						Intent notifyIntent = new Intent("action_download_success");
						notifyIntent.putExtra("url", targetUrl);
						mContext.sendBroadcast(notifyIntent);
						scanFileToMedia(targetFile);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public void scanFileToMedia(final String url) {
		new Thread(new Runnable() {
			public void run() {
				MediaScannerConnection.scanFile(mContext, new String[] {url}, null,
					new MediaScannerConnection.OnScanCompletedListener() {
						public void onScanCompleted(String path, Uri uri) {
							Log.d(TAG, "scan completed : file = " + url);
						}
					});
			}
			
		}).start();
	}

}
