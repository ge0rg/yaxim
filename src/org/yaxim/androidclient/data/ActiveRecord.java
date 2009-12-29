package org.yaxim.androidclient.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public interface ActiveRecord {
	public boolean delete();
	public void setSQLiteDatabase(SQLiteDatabase mSQLiteDatabase);
	public Cursor retrieveAll();
}
