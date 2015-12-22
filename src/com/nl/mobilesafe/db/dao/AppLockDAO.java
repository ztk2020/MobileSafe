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
	 * ����һ��Ҫ����Ӧ�ó���İ���
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
	 * ɾ��һ��Ҫ����Ӧ�ó���İ���
	 */
	public void delete(String packname) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete("applock", "packname=?", new String[] { packname });
		db.close();
		Intent intent = new Intent();
		intent.setAction("com.nl.mobilesafe.db.dao.datachanged");
		context.sendBroadcast(intent);
		//���ݹ۲��ߣ������Զ���㲥�¼�
		//context.getContentResolver().registerContentObserver(uri, notifyForDescendents, observer)
	}

	/**
	 * ����һ������Ӧ�ó���İ����Ƿ����
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
	 * ����ȫ���İ���
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