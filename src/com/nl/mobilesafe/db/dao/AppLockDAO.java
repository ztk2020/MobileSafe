package com.nl.mobilesafe.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nl.mobilesafe.db.AppLockDBOpenHelper;

public class AppLockDAO {

	private AppLockDBOpenHelper helper;
	private Context context;
	public AppLockDAO(Context context) {
		helper = new AppLockDBOpenHelper(context);
		this.context = context;
	}

	/**
	 * 添加一个要锁定应用程序的包名
	 */
	public void add(String packname) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("packname", packname);
		db.insert("applock", null, values);
		db.close();
		Intent intent = new Intent();
		intent.setAction("com.nl.mobilesafe.db.dao.datachanged");
		context.sendBroadcast(intent);
	}

	/**
	 * 删除一个要锁定应用程序的包名
	 */
	public void delete(String packname) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete("applock", "packname=?", new String[] { packname });
		db.close();
		Intent intent = new Intent();
		intent.setAction("com.nl.mobilesafe.db.dao.datachanged");
		context.sendBroadcast(intent);
		//内容观察者，类似自定义广播事件
		//context.getContentResolver().registerContentObserver(uri, notifyForDescendents, observer)
	}

	/**
	 * 查找一个锁定应用程序的包名是否存在
	 */
	public boolean find(String packname) {
		boolean result = false;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("applock", null, "packname=?",
				new String[] { packname }, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			result = true;
		}
		cursor.close();
		db.close();
		return result;
	}
	/**
	 * 查找全部的包名
	 */
	public List<String> findAll() {
		List<String> protectPacknames = new ArrayList<String>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("applock", new String[]{"packname"}, null, null, null, null, null);
		while(cursor.moveToNext()){
			protectPacknames.add(cursor.getString(0));
		}
		cursor.close();
		db.close();
		return protectPacknames;
	}
}
