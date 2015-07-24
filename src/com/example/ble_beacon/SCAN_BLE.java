package com.example.ble_beacon;

import static android.provider.BaseColumns._ID;
import static com.example.ble_beacon.DbConstants.COMPANY_NAME;
import static com.example.ble_beacon.DbConstants.DATA;
import static com.example.ble_beacon.DbConstants.DATE;
import static com.example.ble_beacon.DbConstants.NUMBER;
import static com.example.ble_beacon.DbConstants.STATUS;
import static com.example.ble_beacon.DbConstants.TABLE_NAME;
import static com.example.ble_beacon.DbConstants.TRADE_IN;
import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SCAN_BLE extends Activity {
	Queue_rssi action_in;
	Queue_rssi action_out;
	boolean sucess_flag = false;
	boolean write_f = false;
	String[] numner;
	String UUID = "";
	String STATE = "2";
	String NEW_DATE = "";
	String Number = "";
	boolean ble_status_f = false;
	String device_name = "";
	int listview_item;
	private DBHelper dbhelper = null;
	int database_data = 0;
	/**************************** ble_connect_read&write ***************************/
	Runnable SCAN_BLE_Device;
	String now_connect_address = "";
	String uuid, ble_rssi;
	String[] ble_rssi_array = new String[1000];
	String[] ble_uuid_array = new String[1000];
	int ble_rssi_f = 0;
	int check_f = 0;
	int check_uuid_status = 0;
	String test_Instruction = "s,et,01,dic,0005,e";
	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	int uuid_f = 0;
	boolean Trade_in = true;
	private TextView mConnectionState;
	private TextView mDataField;
	private String mDeviceName;
	private String mDeviceAddress;
	private ExpandableListView mGattServicesList;
	public BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;
	ListView scan_list;
	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";
	Context context;
	File dir_Internal;
	int updata_database_data = 0;
	// Code to manage Service lifecycle.
	public final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e("BLE_connect", "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Toast.makeText(getApplicationContext(), "connect_error",
					Toast.LENGTH_SHORT).show();
		}
	};

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				updateConnectionState(R.string.connected);
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				mConnected = false;
				updateConnectionState(R.string.disconnected);
				invalidateOptionsMenu();
				clearUI();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {

				displayGattServices(mBluetoothLeService
						.getSupportedGattServices());
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				String data = intent
						.getStringExtra(BluetoothLeService.EXTRA_DATA);
				if (data.equals("s,et,01,cfm,0005,e") && Trade_in) {
					Toast.makeText(getApplicationContext(), "連接成功，請稍待資料庫更新",
							Toast.LENGTH_SHORT).show();
					write_f = false;
					handler_times.removeCallbacks(updateTimer);
					success_flag = true;
					state_updata();
					Trade_in = false;
					Log.d("Data", "1:" + data);
				}
				Log.d("Data", "2:" + data);

			}
		}
	};

	private void clearUI() {
		mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
		mDataField.setText(R.string.no_data);
	}

	public void write() {

		try {
			Notifications();
			Thread.sleep(200);
			if (mGattCharacteristics != null) {
				int add = 0;
				if (check_uuid_status == 2)
					add = 0;
				if (check_uuid_status == 4)
					add = 1;

				BluetoothGattCharacteristic characteristic = mGattCharacteristics
						.get(uuid_f - 1).get(add);
				int charaProp = characteristic.getProperties();

				byte[] value = new byte[1];
				value = ascll(test_Instruction);
				characteristic.setValue(value);
				mBluetoothLeService.writeCharacteristic(characteristic);

			}

		} catch (Exception e) {
			Log.d("worry", "write1");
			// TODO Auto-generated catch block
			dialog_rescan();
			Toast.makeText(getApplicationContext(), "error_write",
					Toast.LENGTH_SHORT).show();
		}

	}

	private byte[] ascll(String data) {
		byte[] byte_data;
		byte[] byte_data_check;
		byte_data = data.getBytes();
		byte_data_check = new byte[byte_data.length + 2];
		byte_data_check[0] = 0x13;
		for (int i = 0; i < byte_data.length; i++) {
			byte_data_check[i + 1] = byte_data[i];
		}
		byte_data_check[byte_data_check.length - 1] = 0x0A;
		Log.d("Length", "" + byte_data.length);
		return byte_data_check;
	}

	public void Notifications() {
		try {
			Thread.sleep(100);
			int add = 1;
			if (check_uuid_status == 2)
				add = 1;
			if (check_uuid_status == 4)
				add = 0;
			if (mGattCharacteristics != null) {
				final BluetoothGattCharacteristic characteristic = mGattCharacteristics
						.get(uuid_f - 1).get(add);

				final int charaProp = characteristic.getProperties();
				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
					mNotifyCharacteristic = characteristic;
					mBluetoothLeService.setCharacteristicNotification(
							characteristic, true);
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void updateConnectionState(final int resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectionState.setText(resourceId);
			}
		});
	}

	private void displayData(String data) {
		if (data != null) {
			mDataField.setText(data);
		}
	}

	private void displayGattServices(List<BluetoothGattService> gattServices) {
		int f = 0;
		if (gattServices == null)
			return;
		String uuid = null;
		String unknownServiceString = getResources().getString(
				R.string.unknown_service);
		String unknownCharaString = getResources().getString(
				R.string.unknown_characteristic);
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices) {
			f = f + 1;
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();
			Log.d("UUID_information", uuid);

			currentServiceData.put(LIST_NAME,
					SampleGattAttributes.lookup(uuid, unknownServiceString));
			currentServiceData.put(LIST_UUID, uuid);
			gattServiceData.add(currentServiceData);

			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();
			ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				uuid = gattCharacteristic.getUuid().toString();
				if (uuid.equals("0000fff3-0000-1000-8000-00805f9b34fb")) {// 將此uuid以外的空間隱掉

					charas.add(gattCharacteristic);
					HashMap<String, String> currentCharaData = new HashMap<String, String>();
					uuid_f = f;
					currentCharaData.put(LIST_NAME, SampleGattAttributes
							.lookup(uuid, unknownCharaString));
					currentCharaData.put(LIST_UUID, uuid);
					gattCharacteristicGroupData.add(currentCharaData);
					if (check_f == 0)
						check_f = 2;
				}

				if (uuid.equals("0000fff4-0000-1000-8000-00805f9b34fb")) {
					charas.add(gattCharacteristic);
					HashMap<String, String> currentCharaData = new HashMap<String, String>();
					uuid_f = f;
					currentCharaData.put(LIST_NAME, SampleGattAttributes
							.lookup(uuid, unknownCharaString));
					currentCharaData.put(LIST_UUID, uuid);
					gattCharacteristicGroupData.add(currentCharaData);
					if (check_f == 0)
						check_f = 4;
				}

			}
			mGattCharacteristics.add(charas);
			gattCharacteristicData.add(gattCharacteristicGroupData);
			check_uuid_status = check_f;

		}

		SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
				this, gattServiceData,
				android.R.layout.simple_expandable_list_item_2, new String[] {
						LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
						android.R.id.text2 }, gattCharacteristicData,
				android.R.layout.simple_expandable_list_item_2, new String[] {
						LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
						android.R.id.text2 });
		mGattServicesList.setAdapter(gattServiceAdapter);
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	/**************************** ble_connect_read&write ***************************/

	int tag = 0;
	Timer timer;
	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;
	ListView database_list;
	private static final int REQUEST_ENABLE_BT = 1;
	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 1500;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan__ble);
		openDatabase();
		/**************************** ble_connect_read&write ***************************/
		((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
		mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
		mConnectionState = (TextView) findViewById(R.id.connection_state);
		mDataField = (TextView) findViewById(R.id.data_value);

		/**************************** ble_connect_read&write ***************************/

		mHandler = new Handler();
		database_list = (ListView) findViewById(R.id.database);
		scan_list = (ListView) findViewById(R.id.scan_list);
		initView();
		showInList();
		context = this;
		dir_Internal = context.getFilesDir();
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		updata_sql();
	}

	private void openDatabase() {
		dbhelper = new DBHelper(this);
	}

	private void closeDatabase() {
		dbhelper.close();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		showInList();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		super.onActivityResult(requestCode, resultCode, data);
		try {
			if (requestCode == REQUEST_ENABLE_BT
					&& resultCode == Activity.RESULT_CANCELED) {
				finish();
				return;
			}
		} catch (Exception e) {
			Log.d("onActivityResult", "" + e.getMessage());
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			mBluetoothLeService.disconnect();
			mBluetoothLeService.close();
			mGattCharacteristics.clear();
			getApplicationContext().unbindService(mServiceConnection);
			mLeDeviceListAdapter.clear();
			stop_scan = false;
			scanLeDevice(false);
			Thread.sleep(500);
		} catch (Exception e) {
			Log.d("onPause_error", "" + e.getMessage());
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unbindService(mServiceConnection);
			mBluetoothLeService = null;
			closeDatabase();
		} catch (Exception e) {
			Log.d("onDestroy_error", "" + e.getMessage());
		}

	}

	boolean stop_scan = false;

	private void updata_sql(){
		final Runnable updata_sql= new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try{
					showInList();
				}catch(Exception e){}
			}
		};
		long period = 1000;
		final Handler handler = new Handler();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.removeCallbacks(updata_sql);
				handler.post(updata_sql);
			}
		}, 0, period);
	}
	
	
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			SCAN_BLE_Device = new Runnable() {
				@Override
				public void run() {
					Log.d("scan_device", "out");
					try {
						if (stop_scan) {
							Log.d("scan_device", "in");
							// Stops scanning after a pre-defined scan period.
							mScanning = true;
							ble_rssi_f = 0;
							for (int i = 0; i < ble_rssi_array.length; i++)
								ble_rssi_array[i] = "";
							for (int i = 0; i < ble_uuid_array.length; i++)
								ble_uuid_array[i] = "";
							mBluetoothAdapter.startLeScan(mLeScanCallback);
							invalidateOptionsMenu();
						}
					} catch (Exception e) {
						Log.d("SCAN_BLE_DEVICE", "" + e.getMessage());
					}
				}
			};
			long period = 1000;
			final Handler handler = new Handler();
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					handler.removeCallbacks(SCAN_BLE_Device);
					handler.post(SCAN_BLE_Device);
					invalidateOptionsMenu();
					TextView status = (TextView) findViewById(R.id.connection_state);
					if (status.getText().toString().equals("Connected")
							&& write_f) {
						try {

							Thread.sleep(500);
							write();
							write_f = false;
							Log.d("write", "write");
							timer.cancel();
							mScanning = false;
							mBluetoothAdapter.stopLeScan(mLeScanCallback);
							handler.removeCallbacks(SCAN_BLE_Device);
							mLeDeviceListAdapter.clear();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Log.d("worry", "write3");
							// Toast.makeText(getApplicationContext(),
							// "write_error", Toast.LENGTH_SHORT).show();
						}

						mBluetoothAdapter.stopLeScan(mLeScanCallback);
						handler.removeCallbacks(SCAN_BLE_Device);
					} else {

					}
				}
			}, 0, period);
		} else {
			mScanning = false;
			final Handler handler = new Handler();
			handler.removeCallbacks(SCAN_BLE_Device);
			timer.cancel();
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}
	// Adapter for holding devices found through scanning.
	public class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = SCAN_BLE.this.getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			Log.d("BLE_connect", "Connect request result=");
			ViewHolder viewHolder;
			try {

				// General ListView optimization code.
				if (view == null) {
					ble_rssi = "";
					view = mInflator
							.inflate(R.layout.listitem_device_ble, null);
					viewHolder = new ViewHolder();
					viewHolder.deviceAddress = (TextView) view
							.findViewById(R.id.device_address);
					viewHolder.deviceName = (TextView) view
							.findViewById(R.id.device_name);
					viewHolder.devicerssi = (TextView) view
							.findViewById(R.id.device_rssi);
					view.setTag(viewHolder);
				} else {
					viewHolder = (ViewHolder) view.getTag();
				}

				BluetoothDevice device = mLeDevices.get(i);
				final String deviceName = device.getName();
				if (deviceName != null && deviceName.length() > 0)
					viewHolder.deviceName.setText(deviceName);
				else
					viewHolder.deviceName.setText(R.string.unknown_device);
				viewHolder.deviceAddress.setText(device.getAddress());
				viewHolder.devicerssi.setText("rssi:" + ble_rssi_array[i]);
				int check_rssi = Integer.parseInt(ble_rssi_array[i]);

				try {
					Log.d("error_scan", "-2" + device.getName() + "l:"
							+ device.getName().length());
					if (device.getName().toString().equals("null")) {
						rescan();
						Log.d("error_scan", "-1" + device.getName());
					}
					if (device.getName().length() != 0
							&& device.getName() != null) {
						Log.d("error_scan", "0.1:" + device.getName());
						if (ble_uuid_array[i]
								.equals("f510ffdb-0001-0001-0001-000000000000")) {
							UUID = ble_uuid_array[i];
							Log.d("UUID:", device.getName() + UUID);
							Log.d("BLE_RSSI", "" + check_rssi);
							Log.d("error_scan", "0");
							boolean in;
							in = action_in.Queue_function(check_rssi);

							if (in) {
								Log.d("rssi", "enter");
								Log.d("error_scan", "1");
								handler_times
										.removeCallbacks(scan_time_runnable);
								tag = 2;
								stop_scan = false;
								Log.d("error_scan", "2");
								mBluetoothAdapter.stopLeScan(mLeScanCallback);
								Log.d("error_scan", "3");
								TextView svan_tv = (TextView) findViewById(R.id.Scan_device_tv);
								svan_tv.setText("掃描到裝置，連接中...");
								mDeviceAddress = device.getAddress();
								Log.d("error_scan", "4");
								registerReceiver(mGattUpdateReceiver,
										makeGattUpdateIntentFilter());
								Log.d("error_scan", "5");
								Intent gattServiceIntent = new Intent(
										SCAN_BLE.this, BluetoothLeService.class);
								Log.d("error_scan", "6");
								getApplicationContext().bindService(
										gattServiceIntent, mServiceConnection,
										BIND_AUTO_CREATE);
								Log.d("error_scan", "7");
								if (svan_tv.getText().toString()
										.equals("掃描到裝置，連接中..."))
									times(updateTimer);
								else
									handler_times.removeCallbacks(updateTimer);
								Log.d("error_scan", "8");

								Log.d("error_scan", "9");
							}
						}
						Log.d("error_scan", "11" + device.getName());

					}

				} catch (Exception f) {
					rescan();
					Log.d("error_scan", "rescan");
				}
			} catch (Exception e) {
				Log.d("SCAN_error", "" + e.getMessage());
				// try{toast.cancel();}catch(Exception h){}
				// toast.makeText(getApplicationContext(), "連接失敗或掃描錯誤",
				// Toast.LENGTH_SHORT).show();
			}

			return view;
		}
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			int startByte = 2;

			boolean patternFound = false;
			// 寻找ibeacon

			while (startByte <= 5) {
				if (((int) scanRecord[startByte + 2] & 0xff) == 0x02
						&& ((int) scanRecord[startByte + 3] & 0xff) == 0x15) {
					patternFound = true;
					break;
				}
				startByte++;
			}
			if (patternFound) {
			} else {

				ble_rssi = "" + rssi;
				ble_rssi_array[ble_rssi_f] = ble_rssi;
				String msg = "";
				int i = 0;
				for (byte b : scanRecord) {
					i++;
					if (i >= 6 && i <= 21)
						msg = String.format("%02x", b) + msg;
					if (i == 11)
						msg = "-" + msg;
					else if (i == 13)
						msg = "-" + msg;
					else if (i == 15)
						msg = "-" + msg;
					else if (i == 17)
						msg = "-" + msg;
				}
				Log.d("UUID_MSG:", device.getName() + ":" + msg);

				ble_uuid_array[ble_rssi_f] = "" + msg;
				ble_rssi_f = ble_rssi_f + 1;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						mLeDeviceListAdapter.addDevice(device);
						mLeDeviceListAdapter.notifyDataSetChanged();

					}
				});

			}
		}
	};

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
		TextView devicerssi;
	}

	String[] company_name_list_SQLdata;
	String[] trade_in_list_SQLdata;
	String[] status_list_SQLdata;

	/******* SQL *******/

	private void initView() {
		stop_scan = true;
		database_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (!mBluetoothAdapter.isEnabled()) {
					Intent enableBtIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				} else {
					try {
						stop_scan = true;
						Trade_in = true;
						check_Duijiang_dialog();
						listview_item = position;
						device_name = "";
					} catch (Exception e) {
						Toast.makeText(SCAN_BLE.this, "資料顯示錯誤",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}

	private Cursor getCursor() {
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		String[] columns = { _ID, COMPANY_NAME, TRADE_IN, STATUS, DATE, DATA,
				NUMBER };
		int list_f = 0;

		Cursor cursor = db.query(TABLE_NAME, columns, STATUS + "=" + "?",
				new String[] { "1" }, null, null, null);
		database_data = cursor.getCount();
		numner = new String[database_data];
		if (cursor.getCount() > 0) {
			company_name_list_SQLdata = new String[cursor.getCount()];
			trade_in_list_SQLdata = new String[cursor.getCount()];
			status_list_SQLdata = new String[cursor.getCount()];
			while (cursor.moveToNext()) {
				String company_name = cursor.getString(1);
				String trade_in = cursor.getString(2);
				String status_list = cursor.getString(0);
				String num = cursor.getString(6);
				company_name_list_SQLdata[list_f] = company_name;
				trade_in_list_SQLdata[list_f] = trade_in;
				status_list_SQLdata[list_f] = status_list;
				numner[list_f] = num;
				list_f++;
			}
		}
		startManagingCursor(cursor);
		return cursor;
	}

	// 顯示資料庫所有的資料
	private void showInList() {
		getlim();
		LinearLayout back_ground_layout;
		back_ground_layout = (LinearLayout) findViewById(R.id.back_ground_layout);
		Cursor cursor = getCursor();
		String[] from = { DATA };
		int[] to = { R.id.txtName };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.sqlite_listview, cursor, from, to);
		database_list.setAdapter(adapter);
		if (database_data > 0) {
			back_ground_layout.setVisibility(View.GONE);
			database_list.setVisibility(View.VISIBLE);
		} else {
			back_ground_layout.setVisibility(View.VISIBLE);
			database_list.setVisibility(View.GONE);
		}
	}

	private void updata_sqlite_data(int i, String state) {
		time();
		ContentValues values = new ContentValues();
		values.put(STATUS, state);
		values.put(DATE, finish_time);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		db.update(TABLE_NAME, values, _ID + "=" + "'" + status_list_SQLdata[i]
				+ "'", null);
		showInList();
	}

	/******************************************* dialog *******************************************/

	public void check_Duijiang_dialog() {

		LayoutInflater inflater = LayoutInflater.from(SCAN_BLE.this);
		final View v = inflater.inflate(R.layout.use_dialog, null);
		new AlertDialog.Builder(SCAN_BLE.this).setView(v).setCancelable(false)
				.setPositiveButton("使用", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						try {
							handler_times.removeCallbacks(updateTimer);
						} catch (Exception e) {
						}
						try {
							handler_times.removeCallbacks(scan_time_runnable);
						} catch (Exception e) {
						}
						try {
							handler_net.removeCallbacks(updata_state);
						} catch (Exception e) {
						}
						try {
							sec_time.cancel();
							handler_times
									.removeCallbacks(runnable_time_success);
						} catch (Exception e) {
						}
						try {
							action_in = new Queue_rssi(-70, 0);
							sucess_flag = false;
							Thread.sleep(500);
							TextView svan_tv = (TextView) findViewById(R.id.Scan_device_tv);
							svan_tv.setText("掃描裝置中...");
							mLeDeviceListAdapter = new LeDeviceListAdapter();
							scan_list.setAdapter(mLeDeviceListAdapter);
							scanLeDevice(true);
							/*
							 * LinearLayout beacon_layout = (LinearLayout)
							 * findViewById(R.id.beacon_layout); LinearLayout
							 * connect_layout = (LinearLayout)
							 * findViewById(R.id.connect_layout);
							 * beacon_layout.setVisibility(View.GONE);
							 * connect_layout.setVisibility(View.VISIBLE);
							 */
							success_flag = false;
							use_offsetting_dialog();
							stop_scan = true;
							write_f = true;
							times(scan_time_runnable);
						} catch (Exception e) {
							Toast.makeText(getApplicationContext(), "使用折抵卷出錯",
									Toast.LENGTH_SHORT).show();
						}
					}
				}).setNeutralButton("取消", null).show();

	}

	AlertDialog dialog;
	int time_sec = 10;
	private TypegifView view;
	public void use_offsetting_dialog() {
		AlertDialog.Builder use_offsetting_dialog_builder = new AlertDialog.Builder(
				SCAN_BLE.this);
		LayoutInflater inflater = LayoutInflater.from(SCAN_BLE.this);
		final View v = inflater.inflate(R.layout.scan_device_dialog, null);
		view=(TypegifView)v.findViewById(R.id.gifView);
		
	
		use_offsetting_dialog_builder
				.setView(v)
				.setCancelable(false)
				.setPositiveButton("取消兌換",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								try {
									view.view_this();
									sucess_flag = true;
									handler_times.removeCallbacks(updateTimer);
									handler_times
											.removeCallbacks(scan_time_runnable);
									TextView svan_tv = (TextView) findViewById(R.id.Scan_device_tv);
									svan_tv.setText("");
									/*
									 * LinearLayout beacon_layout =
									 * (LinearLayout)
									 * findViewById(R.id.beacon_layout);
									 * LinearLayout connect_layout =
									 * (LinearLayout)
									 * findViewById(R.id.connect_layout);
									 * beacon_layout
									 * .setVisibility(View.VISIBLE);
									 * connect_layout.setVisibility(View.GONE);
									 */
									all_clear();
									sec_time.cancel();
									dialog.dismiss();
									stop_scan = false;
									scanLeDevice(false);
									mLeDeviceListAdapter.clear();
									Thread.sleep(500);
								} catch (Exception e) {
									Log.d("worry", "取消兌換失敗");
								}

							}
						});
		dialog = use_offsetting_dialog_builder.show();
		view.setStart(R.raw.icon_waiting);
	}

	Timer sec_time;
	TextView time_tv;
	Runnable runnable_time_success;
	int rssi;
	boolean dis_state = false;

	public void dialog_success() {
		view.view_this();
		try {
			handler_times.removeCallbacks(updateTimer);
		} catch (Exception e) {
		}
		try {
			handler_times.removeCallbacks(scan_time_runnable);
		} catch (Exception e) {
		}
		try {
			handler_net.removeCallbacks(updata_state);
		} catch (Exception e) {
		}
		sucess_flag = true;
		time_sec = 120;
		dialog.dismiss();
		dialog_suc();
		dis_state = false;
		action_out = new Queue_rssi(-75, 1);
		runnable_time_success = new Runnable() {
			@Override
			public void run() {
				if (time_sec % 4 == 0)
					time_tv.setText("" + time_sec / 4);
				time_sec--;
				if (!dis_state) {
					try {
						rssi = mBluetoothLeService.get_rssi();
						boolean in = action_out.Queue_function(rssi);
						Log.d("rssi_remote:", "" + rssi);
						if (in) {
							rssi = 0;
							dis_state = true;
							Log.d("rssi", "disconnect");
							mBluetoothLeService.disconnect();
							mBluetoothLeService.close();
							mGattCharacteristics.clear();
							getApplicationContext().unbindService(
									mServiceConnection);
							Thread.sleep(400);
							Log.d("stop_scan_disconnect", "distance_disconnect");
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (time_sec == 0)
					success();
			}
		};
		long period = 250;
		final Handler handler = new Handler();
		sec_time = new Timer();
		sec_time.schedule(new TimerTask() {
			@Override
			public void run() {
				if (time_sec > 0) {
					handler.removeCallbacks(runnable_time_success);
					handler.post(runnable_time_success);
				} else
					handler.removeCallbacks(runnable_time_success);
			}
		}, 0, period);
	}

	public void dialog_suc() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(SCAN_BLE.this);
		final View v = inflater.inflate(R.layout.success_dialog, null);
		set_dialog(v);
		builder.setView(v)
				.setCancelable(false)
				.setNeutralButton("兌換結束",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								success();
							}
						});
		dialog = builder.show();
	}

	public void success() {
		try {
			handler_times.removeCallbacks(updateTimer);
		} catch (Exception e) {
		}
		try {
			handler_times.removeCallbacks(scan_time_runnable);
		} catch (Exception e) {
		}
		try {
			sec_time.cancel();
			handler_times.removeCallbacks(runnable_time_success);
		} catch (Exception e) {
		}
		try {
			handler_net.removeCallbacks(updata_state);
		} catch (Exception e) {
		}
		try {
			TextView svan_tv = (TextView) findViewById(R.id.Scan_device_tv);
			svan_tv.setText("");
			updata_sqlite_data(listview_item, "2");
			Trade_in = false;
			sec_time.cancel();
			dialog.dismiss();
			Thread.sleep(350);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "updata_error",
					Toast.LENGTH_SHORT).show();
		}

		try {
			all_clear();
			Thread.sleep(500);
			Log.d("stop_scan_disconnect", "successful");
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "無連接中的裝置可關閉",
					Toast.LENGTH_SHORT).show();
		}

		/*
		 * LinearLayout beacon_layout = (LinearLayout)
		 * findViewById(R.id.beacon_layout); LinearLayout connect_layout =
		 * (LinearLayout) findViewById(R.id.connect_layout);
		 * beacon_layout.setVisibility(View.VISIBLE);
		 * connect_layout.setVisibility(View.GONE);
		 */

	}

	public void set_dialog(View v) {
		time_tv = (TextView) v.findViewById(R.id.time_tv);
	}

	public void dialog_rescan() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("連接失敗");
		builder.setMessage("連接或者掃描時間過長，可以繼續嘗試兌獎或取消");
		builder.setCancelable(false);
		builder.setNeutralButton("取消兌換", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					sucess_flag = true;
					TextView svan_tv = (TextView) findViewById(R.id.Scan_device_tv);
					svan_tv.setText("");
					dialog.dismiss();
					/*
					 * LinearLayout beacon_layout = (LinearLayout)
					 * findViewById(R.id.beacon_layout); LinearLayout
					 * connect_layout = (LinearLayout)
					 * findViewById(R.id.connect_layout);
					 * beacon_layout.setVisibility(View.VISIBLE);
					 * connect_layout.setVisibility(View.GONE);
					 */
					handler_times.removeCallbacks(updateTimer);
					handler_times.removeCallbacks(scan_time_runnable);
					all_clear();
					sec_time.cancel();
					dialog.dismiss();
					stop_scan = false;
					scanLeDevice(false);
					mLeDeviceListAdapter.clear();
					Thread.sleep(500);
				} catch (Exception e) {
					Log.d("worry", "取消兌換失敗");
				}

			}
		});
		DialogInterface.OnClickListener onclick1 = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				rescan();
			}
		};
		builder.setPositiveButton("重新兌獎", onclick1);
		builder.show();
	}

	public void rescan() {
		action_in = new Queue_rssi(-70, 0);
		if (!sucess_flag) {
			TextView svan_tv = (TextView) findViewById(R.id.Scan_device_tv);
			// LinearLayout beacon_layout = (LinearLayout)
			// findViewById(R.id.beacon_layout);
			// LinearLayout connect_layout = (LinearLayout)
			// findViewById(R.id.connect_layout);
			try {
				svan_tv.setText("");
				// beacon_layout.setVisibility(View.VISIBLE);
				// connect_layout.setVisibility(View.GONE);
				scanLeDevice(false);
				mBluetoothLeService.disconnect();
				mBluetoothLeService.close();
				mGattCharacteristics.clear();
				getApplicationContext().unbindService(mServiceConnection);
				Trade_in = true;
				Thread.sleep(500);
			} catch (Exception e) {
				Log.d("rescan_disconnect_error", "");
			}
			try {
				svan_tv.setText("掃描裝置中...");
				mLeDeviceListAdapter = new LeDeviceListAdapter();
				scan_list.setAdapter(mLeDeviceListAdapter);
				scanLeDevice(true);
				times(scan_time_runnable);
				// beacon_layout.setVisibility(View.GONE);
				// connect_layout.setVisibility(View.VISIBLE);
				success_flag = false;
				stop_scan = true;
				device_name = "";
				write_f = true;
				try {
					handler_times.removeCallbacks(updateTimer);
				} catch (Exception x) {
				}
				Thread.sleep(500);
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "Connect_Error",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void all_clear() {
		displayGattServices(mBluetoothLeService.getSupportedGattServices());
		Trade_in = false;
		clearUI();
		write_f = false;
		stop_scan = false;
		scanLeDevice(false);
		mLeDeviceListAdapter.clear();
		mBluetoothLeService.disconnect();
		mBluetoothLeService.close();
		mGattCharacteristics.clear();
		getApplicationContext().unbindService(mServiceConnection);
		handler_times.removeCallbacks(updateTimer);
	}

	/******************************************* dialog *******************************************/

	/******************************************* time *******************************************/

	private Long startTime;
	private Handler handler_times = new Handler();
	private boolean success_flag = false;
	int rescan_flag = 0;
	int updateTimer_time = 0;
	int scan_time_runnable_time = 0;

	// 計時
	public void times(Runnable time) {
		rescan_flag = 0;
		updateTimer_time = 0;
		scan_time_runnable_time = 0;
		// 取得目前時間
		startTime = System.currentTimeMillis();
		// 設定定時要執行的方法
		handler_times.removeCallbacks(time);
		// 設定Delay的時間
		handler_times.postDelayed(time, 1000);

	}

	// 計時開始
	private Runnable updateTimer = new Runnable() {
		public void run() {
			Long spentTime = System.currentTimeMillis() - startTime;
			// 計算目前已過秒數
			Long seconds = (spentTime / 1000) % 60;
			int updateTimer_time = Integer.valueOf("" + seconds);

			if (updateTimer_time == 5) {

				TextView status = (TextView) findViewById(R.id.connection_state);
				if (!(status.getText().toString().equals("Connected") || success_flag)) {
					rescan_flag++;
					if (rescan_flag == 3)
						dialog_rescan();
					else
						rescan();
				} else {
					handler_times.removeCallbacks(updateTimer);
					if (!success_flag) {
						try {
							Thread.sleep(500);
							write();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Log.d("worry", "write3");
						}
						Toast.makeText(getApplicationContext(), "re_write",
								Toast.LENGTH_SHORT).show();
					}
				}
				Log.d("worry", "time_" + seconds);
			} else
				handler_times.postDelayed(this, 1000);

			Log.d("worry", "time_" + seconds);
		}
	};

	// 計時開始
	private Runnable scan_time_runnable = new Runnable() {
		public void run() {
			Long spentTime = System.currentTimeMillis() - startTime;
			// 計算目前已過秒數
			Long seconds = (spentTime / 1000) % 60;
			int scan_time_runnable_time = Integer.valueOf("" + seconds);
			if (scan_time_runnable_time == 2) {
				handler_times.removeCallbacks(scan_time_runnable);
				rescan();
			} else
				handler_times.postDelayed(this, 1000);
			Log.d("worry", "time_" + seconds);
		}
	};
	/******************************************* time *******************************************/
	/******************************************* 兌換卷過期判定 *******************************************/

	String Date[];

	private void getlim() {
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		String[] columns = { _ID, COMPANY_NAME, DATA, TRADE_IN, STATUS, DATE };
		int list_f = 0;

		Cursor cursor = db.query(TABLE_NAME, columns, STATUS + "=" + "?",
				new String[] { "1" }, null, null, null);
		if (cursor.getCount() > 0) {
			company_name_list_SQLdata = new String[cursor.getCount()];
			trade_in_list_SQLdata = new String[cursor.getCount()];
			status_list_SQLdata = new String[cursor.getCount()];
			Date = new String[cursor.getCount()];
			while (cursor.moveToNext()) {
				String company_name = cursor.getString(1);
				String trade_in = cursor.getString(2);
				String status_list = cursor.getString(0);
				String date_check = cursor.getString(5);
				company_name_list_SQLdata[list_f] = company_name;
				trade_in_list_SQLdata[list_f] = trade_in;
				status_list_SQLdata[list_f] = status_list;
				Date[list_f] = date_check;
				list_f++;
			}
			time();
			check_OK();
		}
		startManagingCursor(cursor);

	}

	int now_year = 0;
	int now_month = 0;
	int now_day = 0;
	int old_year = 0;
	int old_month = 0;
	int old_day = 0;

	public void check_OK() {
		for (int i = 0; i < Date.length; i++) {
			time_limit = new Time_limit(Date[i], 1);
			time_limit.time_limit();
			old_year = time_limit.get_old_year();
			old_month = time_limit.get_old_month();
			old_day = time_limit.get_old_day();
			Log.d("date", i + "-" + old_year + ":" + old_month + ":" + old_day);
			time_limit = new Time_limit(now_year, now_month, now_day, old_year,
					old_month, old_day);
			if (!time_limit.check_OK())
				updata_sqlite_data(i, "3");
		}
	}

	Date curDate = new Date(System.currentTimeMillis());
	String finish_time = "";

	Time_limit time_limit;

	public void time() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		curDate = new Date(System.currentTimeMillis());
		finish_time = formatter.format(curDate);
		time_limit = new Time_limit(finish_time, 0);
		time_limit.time_limit();
		now_year = time_limit.get_now_year();
		now_month = time_limit.get_now_month();
		now_day = time_limit.get_now_day();
	}

	/******************************************* 兌換卷過期判定 *******************************************/

	/***************************************** STATE_UPDATA ****************************************/
	final String URL = "http://healthifenas.synology.me/php_web/beacon/android/state_updata.php";// 要加上"http://"，否則會連線失敗

	public void state_updata() {
		try {
			STATE = "2";
			Number = numner[listview_item];
			new Thread(updata_state).start();// 啟動執行序runnable
		} catch (Exception e) {
			Log.d("state_updata", "" + e.getMessage());
		}

	}

	private Handler handler_net = new Handler();
	Runnable updata_state = new Runnable() {
		@Override
		public void run() {
			//
			// TODO: http request.
			//
			Message msg = new Message();
			Bundle data = new Bundle();
			msg.setData(data);
			try {
				// 連線到 url網址
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost method = new HttpPost(URL);

				// 傳值給PHP
				List<NameValuePair> vars = new ArrayList<NameValuePair>();
				time();
				NEW_DATE = finish_time;

				vars.add(new BasicNameValuePair("UUID", UUID));
				vars.add(new BasicNameValuePair("STATE", "2"));
				vars.add(new BasicNameValuePair("NEW_DATE", NEW_DATE));
				vars.add(new BasicNameValuePair("Number", Number));
				method.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));

				// 接收PHP回傳的資料
				HttpResponse response = httpclient.execute(method);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					data.putString("key", EntityUtils.toString(entity, "utf-8"));// 如果成功將網頁內容存入key
					updata_state_handler_Success.sendMessage(msg);

				} else {
					data.putString("key", "無資料");
					updata_state_handler_Nodata.sendMessage(msg);
				}
			} catch (Exception e) {
				try {
					TextView svan_tv = (TextView) findViewById(R.id.Scan_device_tv);
					svan_tv.setText("");
					/*
					 * LinearLayout beacon_layout = (LinearLayout)
					 * findViewById(R.id.beacon_layout); LinearLayout
					 * connect_layout = (LinearLayout)
					 * findViewById(R.id.connect_layout);
					 * beacon_layout.setVisibility(View.VISIBLE);
					 * connect_layout.setVisibility(View.GONE);
					 */
					mBluetoothLeService.disconnect();
					mBluetoothLeService.close();
					mGattCharacteristics.clear();
					getApplicationContext().unbindService(mServiceConnection);
					Trade_in = true;
					sec_time.cancel();
					dialog.dismiss();
					Thread.sleep(500);
				} catch (Exception ex) {
					Log.d("updata_state", "連線失敗_關掉掃描錯誤");
				}
				data.putString("key", "連線失敗，請檢查連線。");
				updata_state_handler_Error.sendMessage(msg);
				handler_net.removeCallbacks(updata_state);
			}

		}
	};

	Handler updata_state_handler_Success = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("key");// 取出key中的字串存入val
			String company_data = val.substring(1, val.length());
			Log.d("uuid_inte", company_data);
			dialog_success();
		}
	};

	Handler updata_state_handler_Error = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("key");
			Toast.makeText(getApplicationContext(), val, Toast.LENGTH_LONG)
					.show();
		}
	};

	Handler updata_state_handler_Nodata = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("key");
			System.out.print(val);
			Log.d("data", val);
			Toast.makeText(getApplicationContext(), val, Toast.LENGTH_LONG)
					.show();
		}
	};

	/***************************************** STATE_UPDATA ****************************************/

}
// 7/4-OK版