package org.yaxim.androidclient.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DBAdapter extends SQLiteOpenHelper {
	private static SQLiteDatabase mSQLiteDatabase;
	private static DBAdapter instance;

	private static final String DATABASE_NAME = "yaxim";
	private static final int DATABASE_VERSION = 1;

	private DBAdapter(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Chats.SQL_CREATE_TABLE);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		db.execSQL("DROP TABLE IF EXISTS " + Chats.SQL_CREATE_TABLE);
		onCreate(db);
	}

	public void initialize(Context context) {
		if (instance == null) {
			instance = new DBAdapter(context, DATABASE_NAME, null, DATABASE_VERSION);
			mSQLiteDatabase = instance.getWritableDatabase();
		}
	}

	public SQLiteDatabase getDatabase() {
		return mSQLiteDatabase;
	}
}
