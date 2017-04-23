package com.sprd.easymusic.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.sprd.easymusic.mysql.MyDBHelper;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class DownloadUtil {
	private String TAG = "DownloadUtil";
	private String targetUrl = null;
	private String targetFile = null;
	private String title = null;
	private String artist = null;
	private int threadNum;
	private Context mContext;
	private int fileSize;
	private boolean pause;
	private boolean delete;
	private int downloadSuccessThread;
	//private boolean 
	private DownloadThread[] downloadThreads;
	private MyDBHelper myHelper;
	private SQLiteDatabase db;
	private int[] downloadedFileSize;
	//private int downloadedSize;
	private int currentFileSize;
	
	public DownloadUtil (String title, String artist, String targetUrl, String targetFile, int threadNum, 
			Context context, MyDBHelper myHelper) {
		this.title = title;
		this.artist = artist;
		this.targetUrl = targetUrl;
		this.targetFile = targetFile;
		this.threadNum = threadNum;
		downloadThreads = new DownloadThread[threadNum];
		mContext = context;
		this.myHelper = myHelper;
		db = myHelper.getReadableDatabase();
		downloadedFileSize = new int[threadNum];
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
			currentFileSize = fileSize % threadNum == 0 ? fileSize / threadNum : fileSize / threadNum + 1;
			for (int i=0; i<threadNum; i++) {
				downloadThreads[i] = new DownloadThread(targetUrl, targetFile, i, currentFileSize * i, currentFileSize);
				downloadThreads[i].start();
			}
			saveDownloadingTask();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//断点继续下载
	public void continueDownloading(int fileSize, int currentSize, String size) {
		Log.d(TAG, "continueDownloading:fileSize = " + fileSize);
		this.fileSize = fileSize;
		this.currentFileSize = currentSize;
		decodeDownloadedSize(size);
		for (int i=0; i<threadNum; i++) {
			downloadThreads[i] = new DownloadThread(targetUrl, targetFile, i, 
					currentFileSize * i + downloadedFileSize[i], currentFileSize - downloadedFileSize[i]);
			downloadThreads[i].start();
		}
	}
	
	//将下载任务录入正在下载数据表中
	private void saveDownloadingTask() {
		db.execSQL("insert into downloading_music values(null,?,?,?,?,?,?,?,?)", new Object[] {
				title,artist,targetUrl,targetFile,fileSize,threadNum,currentFileSize,encodeDownloadedSize(downloadedFileSize)});
		Log.d(TAG, "saveDownloadingTask");
	}
	
	//更新数据表中的下载信息
	private void updateDownloadedMusicInfo(int id, int length) {
		downloadedFileSize[id] += length;
		db.execSQL("update downloading_music set downloadedSize = ? " +
				"where title = ?", new Object[]{encodeDownloadedSize(downloadedFileSize), title});
		Log.d(TAG, "updateDownloadedMusicInfo:id = " + id + "downloadedSize = " + downloadedFileSize[id]);
	}
	
	//移除下载任务
	private void removeDownloadingMusic() {
		db.execSQL("delete from downloading_music where fileSize = " + fileSize);
		Log.d(TAG, "removeDownloadingMusic:" + title + "移出数据库");
	}

	//将多个线程的已完成下载编码
	private String encodeDownloadedSize(int[] size) {
		StringBuilder sb = new StringBuilder();
		for (int i: size) {
			sb.append(i + "/");
		}
		Log.d(TAG, "encodeDownloadedSize:value = " + sb);
		return sb.toString();
	}
	//解码多线程的已完成下载
	private void decodeDownloadedSize(String size) {
		String[] str = size.split("/");
		Log.d(TAG, "threadNum = " + str.length);
		for (int i=0; i<str.length; i++) {
			if (str[i] == null) {
				downloadedFileSize[i] = 0;
				return;
			}
			downloadedFileSize[i] = Integer.parseInt(str[i]);
			Log.d(TAG, "decodeDownloadedSize:downloadedFileSize[" + i + "]=" + str[i]);
		}
	}
	
	public int getDownloadProgress() {
		int downloadLength = 0;
//		for (DownloadThread thread : downloadThreads) {
//			if (thread != null) downloadLength += thread.length;
//		}
		for (int i=0; i<downloadedFileSize.length; i++) {
			downloadLength += downloadedFileSize[i];
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
		private int id;
		private String downloadUrl = null;
		private String file;
		private int startPos;
		private int currentFileSize;
		private BufferedInputStream bis;
		
		public DownloadThread (String downloadUrl, String file, int id, int startPos, int currentFileSize) {
			this.downloadUrl = downloadUrl;
			this.file = file;
			this.id = id;
			this.startPos = startPos;
			this.currentFileSize = currentFileSize;
		}

		public void run() {
			try {
				HttpURLConnection conn = (HttpURLConnection)new URL(downloadUrl).openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestProperty("Range", "bytes=" + startPos + "-" + startPos+currentFileSize);//设置获取资源数据的范围，从startPos到endPos
				InputStream is = conn.getInputStream();
				bis = new BufferedInputStream(is);
				//bis.skip(startPos);
				File downloadFile = new File(file);
				RandomAccessFile raf = new RandomAccessFile(downloadFile, "rwd");
				raf.seek(startPos);
				
				int hasRead = 0;
				byte[] buff = new byte[1024*4];
				//delete置为true后会马上停止读取数据，之后会删除对应文件
				while (length < currentFileSize && (hasRead = bis.read(buff)) > 0 && !delete) {
					while (pause){//暂停状态不读取数据，也不退出数据区，等待继续读取
						Log.d(TAG, "DownloadUtil pause!");
					}
					if (!pause) {
						if (length + hasRead > currentFileSize) {
							int lastRead = currentFileSize - length;
							raf.write(buff, 0, lastRead);
							length += lastRead;
							Log.d(TAG, "last read " + lastRead + " bytes");
							updateDownloadedMusicInfo(id, lastRead);
						} else {
							raf.write(buff, 0, hasRead);
							length += hasRead;
							Log.d(TAG, "read " + hasRead + " bytes");
							updateDownloadedMusicInfo(id, hasRead);
						}
						//downloadedFileSize[id] += hasRead;
					}
				}
				Log.d(TAG, "download success? " + (currentFileSize == length));
				raf.close();
				bis.close();
				//删除文件
				if (delete) {
					boolean isDeleted = downloadFile.delete();
					Log.d(TAG, "delete file success ? " + isDeleted);
					removeDownloadingMusic();
				} else {
					Log.d(TAG, "run:currentFileSize = " + currentFileSize + " downloadLength = " + length);
					downloadSuccessThread++;
					if (downloadSuccessThread == threadNum) {
						Log.d(TAG, "download success");
						//下载完成移出数据表
						removeDownloadingMusic();
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
