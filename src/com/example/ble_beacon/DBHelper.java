package com.example.ble_beacon;

import static android.provider.BaseColumns._ID;
import static com.example.ble_beacon.DbConstants.TABLE_NAME;
import static com.example.ble_beacon.DbConstants.COMPANY_NAME;
import static com.example.ble_beacon.DbConstants.TRADE_IN;
import static com.example.ble_beacon.DbConstants.NUMBER;
import static com.example.ble_beacon.DbConstants.STATUS;
import static com.example.ble_beacon.DbConstants.DATA;
import static com.example.ble_beacon.DbConstants.DATE;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	
	private final static String DATABASE_NAME = "TRADE.db";
	private final static int DATABASE_VERSION = 1;
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		final String INIT_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
								  _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
								  COMPANY_NAME + " CHAR, " +
								  DATA + " CHAR, " +
								  STATUS + " CHAR, " +
								  DATE + " CHAR, " +
								  NUMBER + " CHAR, " +
								  TRADE_IN + "  CHAR);"; 
		db.execSQL(INIT_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(DROP_TABLE);
		onCreate(db);
	}

}
