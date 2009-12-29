package org.yaxim.androidclient.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Chats implements ActiveRecord {
	public static final String COL_ID = "_id";
	public static final String COL_DATE = "date";
	public static final String COL_WITHJID = "withJID";
	public static final String COL_FROMJID = "fromJID";
	public static final String COL_TOJID = "toJID";
	public static final String COL_MESSAGE = "message";
	public static final String COL_READ = "read";
	
	private static final String[] ALL_COLUMNS = new String[] {
		COL_ID,
		COL_DATE,
		COL_WITHJID,
		COL_FROMJID,
		COL_TOJID,
		COL_MESSAGE,
		COL_READ
	};

	public static final String SQL_TABLE_NAME = "chats";
	public static final String SQL_CREATE_TABLE = "CREATE TABLE "
		+ SQL_TABLE_NAME + " "
		+ " (" + COL_ID + " integer primary key autoincrement, "
		+ COL_DATE + " INT, "
		+ COL_WITHJID + " TEXT, "
		+ COL_FROMJID + " TEXT, "
		+ COL_TOJID + " TEXT, "
		+ COL_MESSAGE + " TEXT, "
		+ COL_READ + " INT);";

	private SQLiteDatabase mSQLiteDatabase;

	private int id;
	private int date;
	private String withJID;
	private String fromJID;
	private String toJID;
	private String message;
	private int read;
	
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(int date) {
		this.date = date;
	}
	/**
	 * @return the date
	 */
	public int getDate() {
		return date;
	}
	/**
	 * @param withJID the withJID to set
	 */
	public void setWithJID(String withJID) {
		this.withJID = withJID;
	}
	/**
	 * @return the withJID
	 */
	public String getWithJID() {
		return withJID;
	}
	/**
	 * @param fromJID the fromJID to set
	 */
	public void setFromJID(String fromJID) {
		this.fromJID = fromJID;
	}
	/**
	 * @return the fromJID
	 */
	public String getFromJID() {
		return fromJID;
	}
	/**
	 * @param toJID the toJID to set
	 */
	public void setToJID(String toJID) {
		this.toJID = toJID;
	}
	/**
	 * @return the toJID
	 */
	public String getToJID() {
		return toJID;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param read the read to set
	 */
	public void setRead(int read) {
		this.read = read;
	}
	/**
	 * @return the read
	 */
	public int getRead() {
		return read;
	}
	
	public long save() {
		ContentValues values = new ContentValues();
		if (id <= 0) {
			values.put(COL_DATE, date);
			values.put(COL_WITHJID, withJID);
			values.put(COL_FROMJID, fromJID);
			values.put(COL_TOJID, toJID);
			values.put(COL_MESSAGE, message);
			values.put(COL_READ, read);
			return mSQLiteDatabase.insert(SQL_TABLE_NAME, null, values);
		} else {
			values.put(COL_DATE, date);
			values.put(COL_WITHJID, withJID);
			values.put(COL_FROMJID, fromJID);
			values.put(COL_TOJID, toJID);
			values.put(COL_MESSAGE, message);
			values.put(COL_READ, read);
			return mSQLiteDatabase.update(SQL_TABLE_NAME, values, COL_ID +"=" + id, null);
		}
	}
	
	public boolean delete(){
		return mSQLiteDatabase.delete(SQL_TABLE_NAME, COL_ID + "=" + id, null) > 0;
	}
	
	public void setSQLiteDatabase(SQLiteDatabase sqlitedb) {
		this.mSQLiteDatabase = sqlitedb;
	}
	
	public Cursor retrieveAll() {
		return mSQLiteDatabase.query(SQL_TABLE_NAME, ALL_COLUMNS, null, null, null, null, null);
	}

}
