package com.example.ble_beacon;

import static android.provider.BaseColumns._ID;
import static com.example.ble_beacon.DbConstants.COMPANY_NAME;
import static com.example.ble_beacon.DbConstants.DATA;
import static com.example.ble_beacon.DbConstants.DATE;
import static com.example.ble_beacon.DbConstants.STATUS;
import static com.example.ble_beacon.DbConstants.TABLE_NAME;
import static com.example.ble_beacon.DbConstants.TRADE_IN;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class SET extends Activity {
	private DBHelper dbhelper = null;
	ListView database_list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set);
		database_list = (ListView) findViewById(R.id.list_database_data);
		openDatabase();
		initView();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		closeDatabase();
	}

	public void database_data(View v) {
		showInList();
	}

	/******************************************* SQL *******************************************/

	private void openDatabase() {
		dbhelper = new DBHelper(this);
	}

	private void closeDatabase() {
		dbhelper.close();
	}

	private void initView() {
		database_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			}
		});
	}

	private Cursor getCursor() {
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		String[] columns = { _ID, COMPANY_NAME, DATA, TRADE_IN, STATUS, DATE };
		int list_f = 0;

		Cursor cursor = db.query(TABLE_NAME, columns, STATUS + "=" + "?",
				new String[] { "2" }, null, null, null);
		if (cursor.getCount() > 0) {
			
			
			while (cursor.moveToNext()) {
			}
		}
		startManagingCursor(cursor);
		return cursor;
	}

	// 顯示資料庫所有的資料
	private void showInList() {
		Cursor cursor = getCursor();
		String[] from = { COMPANY_NAME,DATE };
		int[] to = { R.id.txtName,R.id.txtdate };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.sqlite_record_listview, cursor, from, to);
		database_list.setAdapter(adapter);
	}

	/******************************************* SQL *******************************************/

}
