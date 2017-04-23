package com.sprd.easymusic.mysql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {
	
	private static MyDBHelper myHelper;
	private final String CREATE_TABLE_LOCALMUSIC = "create table stored_music(" +
			"_id integer primary key autoincrement,title,artist,duration,url)";

	public MyDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		myHelper = this;
		db.execSQL(CREATE_TABLE_LOCALMUSIC);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
