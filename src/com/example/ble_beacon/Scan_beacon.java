package com.example.ble_beacon;

import static com.example.ble_beacon.DbConstants.COMPANY_NAME;
import static com.example.ble_beacon.DbConstants.DATA;
import static com.example.ble_beacon.DbConstants.DATE;
import static com.example.ble_beacon.DbConstants.NUMBER;
import static com.example.ble_beacon.DbConstants.STATUS;
import static com.example.ble_beacon.DbConstants.TABLE_NAME;
import static com.example.ble_beacon.DbConstants.TRADE_IN;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class Scan_beacon extends Activity {
	String company[] = { "無公司", "IM Waffle", "清心" };

	boolean down_state = false;

	/*************************** MYSQL & WEB ***************************/
	final String URL = "http://healthifenas.synology.me/php_web/beacon/android/beacon.php";// 要加上"http://"，否則會連線失敗
	final String URL_ans = "http://healthifenas.synology.me/php_web/beacon/android/result_question_answer.php";// 要加上"http://"
	int time_scan = 0; // 否則會連線失敗
	Button like;
	Button unlike;
	LinearLayout choose_button_layout;
	LinearLayout title_background_layout;
	LinearLayout question_background_layout;
	ImageButton background_next_imbut;
	ImageButton next_imbut;
	ImageButton back_imbut;
	ImageButton background_back_imbut;
	String company_name;
	Button choose_but0;
	Button choose_but1;
	Button choose_but2;
	Button choose_but3;
	Button choose_but4;
	Button choose_but5;
	Button background_choose_but0;
	Button background_choose_but1;
	Button background_choose_but2;
	Button background_choose_but3;
	Button background_choose_but4;
	Button background_choose_but5;
	private TypegifView typegifview;
	File dir_Internal;
	Context context;
	Runnable SCAN_BLE_Device;
	String company_data = "";

	/******************************************* 問卷題目 *******************************************/
	int choose_but_in[] = new int[100];
	int now_write_question = 0;
	String[] Question_Data = new String[100];
	String[] Question_Choose_Data = new String[100];
	String[] user_answer = new String[100];
	int[] choose_length = new int[100];
	String[] question_ans = new String[100];
	String[][] dissect_choose_data = new String[100][100];
	int question_math = 0;// 題目數量
	int Interval = 100; // 間隔數量
	boolean jump_flag = false; // 是否題目內有跳題選項
	String[] jump_question_data;// 切割題目位置，如2(題目),2(選項一跳一題),1(選項二不跳題)等
	int[][] jump_question; // 對應題數[0]及[0]之後的選項跳題位置
	int jump_question_math = 0; // 需跳題題目數量
	int[] array_choose_math; // 跳題選項題數
	int[] question_array;
	int question_array_flag = 0;
	int[] array_question_state; // 跳題選項題數
	/******************************************* 問卷題目 *******************************************/

	String company_id;
	int question_f = 0;
	int now_question = 0;
	int view_f = 0;
	TextView question;
	TextView question_background_tv;
	private DBHelper dbhelper = null;
	static String tablename = "NAME";
	String Progress_Type = "";
	ProgressDialog myDialog;
	/*************************** MYSQL & WEB ***************************/

	/*************************** ble&beacon ***************************/
	boolean check_ble_scan_colse_f = true;
	int tag = 0;
	Timer timer;
	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;
	String test_Instruction = "s,et,01,dic,0005,e";
	int power, minor, major;
	// rssi
	int beacon_rssi_f = 0;
	String beacon_rssi = "";
	String[] beacon_rssi_array = new String[1000];
	// uuid
	int beacon_uuid_f = 0;
	String uuid;
	String[] beacon_uuid_array = new String[1000];
	// power
	int beacon_power_f = 0;
	String[] beacon_power_array = new String[1000];
	// distance
	int beacon_distance_f = 0;
	double distance = 0;
	String[] beacon_distance_array = new String[1000];
	// minor
	int beacon_minor_f = 0;
	String[] beacon_minor_array = new String[1000];
	// major
	int beacon_major_f = 0;;
	String[] beacon_major_array = new String[1000];

	ListView devic_listview;
	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 1000;// 掃描時間10秒
	LinearLayout beacon_layout;
	LinearLayout Questionnaire_layout;
	LinearLayout Questionnaire_background_layout;
	private ViewFlipper vf;
	private float lastX;
	Button find;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_beacon);
		basic_set();
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
					.show();
		}
		// Initializes Bluetooth adapter.
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "藍芽不支持", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			try {
				mLeDeviceListAdapter = new LeDeviceListAdapter();
				devic_listview.setAdapter(mLeDeviceListAdapter);
				scanLeDevice(true);
			} catch (Exception e) {
				Toast.makeText(Scan_beacon.this, "連線失敗", Toast.LENGTH_SHORT)
						.show();
			}
			devic_listview.setVisibility(View.INVISIBLE);
			set_gif_init(true);
			typegifview.setSrc(R.drawable.search);
			typegifview.setStart(R.raw.search);
		}
		choose_button_set();
	}

	boolean scan_state = true;

	public void set_gif_init(boolean flag) {
		TypegifView view_back = (TypegifView) findViewById(R.id.gifView1);
		if (flag) {
			typegifview.setVisibility(View.VISIBLE);
			view_back.setVisibility(View.GONE);
		} else {
			typegifview.setVisibility(View.GONE);
			view_back.setVisibility(View.VISIBLE);
		}
	}

	public void basic_set() {
		typegifview = (TypegifView) findViewById(R.id.gifView);
		find = (Button) findViewById(R.id.find_but);
		vf = (ViewFlipper) findViewById(R.id.view_flipper);
		title_background_layout = (LinearLayout) findViewById(R.id.title_background_layout);
		question_background_layout = (LinearLayout) findViewById(R.id.question_background_layout);
		beacon_layout = (LinearLayout) findViewById(R.id.beacon_layout);
		Questionnaire_layout = (LinearLayout) findViewById(R.id.Questionnaire_layout);
		Questionnaire_background_layout = (LinearLayout) findViewById(R.id.Questionnaire_background_layout);
		mHandler = new Handler();
		devic_listview = (ListView) findViewById(R.id.device_list);
		background_next_imbut = (ImageButton) findViewById(R.id.imageButton2);
		next_imbut = (ImageButton) findViewById(R.id.next_imbut);
		background_back_imbut = (ImageButton) findViewById(R.id.imageButton1);
		back_imbut = (ImageButton) findViewById(R.id.back_imbut);
		next_imbut.setVisibility(View.INVISIBLE);
		background_next_imbut.setVisibility(View.INVISIBLE);
		context = this;
		dir_Internal = context.getFilesDir();
		choose_button_layout = (LinearLayout) findViewById(R.id.choose_button_layout);
		choose_button_set();
		choose_button_layout.setOnTouchListener(LinearLayout_choose);
		choose_but0.setOnTouchListener(choose1);
		choose_but1.setOnTouchListener(choose2);
		choose_but2.setOnTouchListener(choose3);
		choose_but3.setOnTouchListener(choose4);
		choose_but4.setOnTouchListener(choose5);
		choose_but5.setOnTouchListener(choose6);
		openDatabase();
	}

	public void choose_button_set() {
		choose_but0 = (Button) findViewById(R.id.choose1);
		choose_but1 = (Button) findViewById(R.id.choose2);
		choose_but2 = (Button) findViewById(R.id.choose3);
		choose_but3 = (Button) findViewById(R.id.choose4);
		choose_but4 = (Button) findViewById(R.id.choose5);
		choose_but5 = (Button) findViewById(R.id.choose6);
		background_choose_but0 = (Button) findViewById(R.id.background_choose1);
		background_choose_but1 = (Button) findViewById(R.id.background_choose2);
		background_choose_but2 = (Button) findViewById(R.id.background_choose3);
		background_choose_but3 = (Button) findViewById(R.id.background_choose4);
		background_choose_but4 = (Button) findViewById(R.id.background_choose5);
		background_choose_but5 = (Button) findViewById(R.id.background_choose6);
	}

	/************************** 滑動 ************************/
	public void button_in(int question_flag, int question_background_flag) {
		button_reset_state();

		if (choose_but_in[question_flag] == 0)
			;
		else {
			if (choose_but_in[question_flag] == 1)
				choose_but0.setBackgroundResource(R.drawable.block_in);
			if (choose_but_in[question_flag] == 2)
				choose_but1.setBackgroundResource(R.drawable.block_in);
			if (choose_but_in[question_flag] == 3)
				choose_but2.setBackgroundResource(R.drawable.block_in);
			if (choose_but_in[question_flag] == 4)
				choose_but3.setBackgroundResource(R.drawable.block_in);
			if (choose_but_in[question_flag] == 5)
				choose_but4.setBackgroundResource(R.drawable.block_in);
			if (choose_but_in[question_flag] == 6)
				choose_but5.setBackgroundResource(R.drawable.block_in);
		}
		if (choose_but_in[question_background_flag] == 0)
			;
		else {
			if (choose_but_in[question_background_flag] == 1)
				background_choose_but0
						.setBackgroundResource(R.drawable.block_in);
			if (choose_but_in[question_background_flag] == 2)
				background_choose_but1
						.setBackgroundResource(R.drawable.block_in);
			if (choose_but_in[question_background_flag] == 3)
				background_choose_but2
						.setBackgroundResource(R.drawable.block_in);
			if (choose_but_in[question_background_flag] == 4)
				background_choose_but3
						.setBackgroundResource(R.drawable.block_in);
			if (choose_but_in[question_background_flag] == 5)
				background_choose_but4
						.setBackgroundResource(R.drawable.block_in);
			if (choose_but_in[question_background_flag] == 6)
				background_choose_but5
						.setBackgroundResource(R.drawable.block_in);
		}
	}

	int fin;

	public void question_now(int question_in) {// question_in是選項編號1~6
		boolean jump_flag = false;
		int flag = 0;
		question_ans[now_question] = user_answer[question_in - 1];

		Log.d("question_now", "1:" + view_state + "\n2:"
				+ question_array[question_array_flag] + "\n3:"
				+ question_array_flag + "\n4:" + question_math + "\n5:"
				+ Question_Data[now_question + 1] + "\n6:" + now_write_question
				+ "\n7:" + now_question);

		if (view_state == 0 && (choose_but_in[now_question] == 0)) {

			choose_but_in[now_question] = question_in;
			Log.d("question_error", "5");
			if (Question_Data[now_question + 1].length() != 0
					&& Question_Data[now_question + 1] != null) {

				for (int i = 0; i < jump_question_math; i++) {
					if (now_question == (jump_question[i][0] - 1)) {
						flag = i;
						jump_flag = true;
					}
				}

				if (jump_flag) {
					now_question = now_question
							+ jump_question[flag][question_in];
					now_write_question = now_write_question
							+ jump_question[flag][question_in];
				} else {
					now_question++;
					now_write_question++;
				}
				question_array[question_array_flag] = now_question;
				fin = now_question;
				question_array_flag++;

				button_set(choose_length[now_question], now_question);
				button_background_choose(choose_length[now_question],
						now_question);
				Log.d("question_error", "2");
				question.setText(Question_Data[now_question]);
				Log.d("question_error", "3");
				question_background_tv.setText(Question_Data[now_question]);
				Log.d("question_error", "4");

			} else {

				choose_but_in[now_question] = question_in;
				now_write_question = 0;
				question.setText("");
				question_background_tv.setText("");
				question_end();

			}
			if (Question_Data[now_question].length() != 0) {
				button_in(now_question, now_question);
				back_imbut.setVisibility(View.VISIBLE);
			}

		} else {
			if ((Question_Data[now_question].length() != 0)
					&& (now_write_question > now_question)) {

				for (int i = 0; i < jump_question_math; i++) {
					if (now_question == (jump_question[i][0] - 1)) {
						if (choose_but_in[now_question] != question_in
								&& (jump_question[i][choose_but_in[now_question]] != jump_question[i][question_in])) {
							flag = i;
							jump_flag = true;
							for (int j = question_array_flag; j < fin; j++)
								question_array[j] = question_math + 1;
						}
					}
				}

				choose_but_in[now_question] = question_in;
				question_background_tv.setText(Question_Data[now_question]);
				button_background_choose(choose_length[now_question],
						now_question);

				view_state--;
				if (jump_flag) {
					view_state = 0;
					now_question = now_question
							+ jump_question[flag][question_in];
					now_write_question = now_write_question
							+ jump_question[flag][question_in];
					button_in(now_question, now_question
							- jump_question[flag][question_in]);
					question_array[question_array_flag] = now_question;
					background_next_imbut.setVisibility(View.INVISIBLE);
					next_imbut.setVisibility(View.INVISIBLE);
				} else {
					int flag_question = 0;
					int flag_choose = 0;
					boolean flag_jump = false;
					for (int j = 0; j < jump_question_math; j++) {
						if (jump_question[j][0] == now_question + 1) {
							flag_jump = true;
							flag_question = j;
							flag_choose = choose_but_in[question_array[question_array_flag - 1]];
						}
					}

					if (flag_jump) {
						button_in(now_question
								+ jump_question[flag_question][flag_choose],
								now_question);
						now_question = now_question
								+ jump_question[flag_question][flag_choose];
					} else {
						button_in(now_question + 1, now_question);
						now_question++;
					}
				}

				question_array_flag++;
				button_set(choose_length[now_question], now_question);
				question.setText(Question_Data[now_question]);
				button_background_choose(choose_length[now_question - 1],
						now_question - 1);
				question_background_tv.setText(Question_Data[now_question - 1]);
				but_next_state--;
				back_imbut.setVisibility(View.VISIBLE);
				if (now_question != 1)
					background_back_imbut.setVisibility(View.VISIBLE);
				if (but_next_state == 0) {
					view_state = 0;
					next_imbut.setVisibility(View.INVISIBLE);
					background_next_imbut.setVisibility(View.VISIBLE);
				}
				button_set(choose_length[now_question], now_question);
				button_background_choose(choose_length[now_question],
						now_question);
				question.setText(Question_Data[now_question]);
				question_background_tv.setText(Question_Data[now_question]);
			}
			if (Question_Data[now_question].length() != 0) {
				button_in(now_question, now_question);
				back_imbut.setVisibility(View.VISIBLE);
			}
		}

	}

	int but_touch = 20;
	int but_next_state = 0;
	int view_state = 0;

	public boolean touch(MotionEvent touchevent, int i) {
		switch (touchevent.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			lastX = touchevent.getX();
			break;
		}
		case MotionEvent.ACTION_UP: {
			int flag_question = 0;
			int flag_choose = 0;
			boolean jump_flag = false;
			float currentX = touchevent.getX();
			if (lastX + but_touch < currentX) {
				if (now_question > 0) {

					question_array_flag--;
					for (int j = 0; j < jump_question_math; j++) {
						if (jump_question[j][0] == question_array[question_array_flag - 1] + 1) {
							jump_flag = true;
							flag_question = j;
							flag_choose = choose_but_in[question_array[question_array_flag - 1]];
						}
					}

					if (jump_flag) {
						int heart = jump_question[flag_question][flag_choose];
						button_in(now_question - heart, now_question);
						now_question = now_question - heart;

						button_background_choose(choose_length[now_question
								+ heart], now_question + heart);
						question_background_tv
								.setText(Question_Data[now_question + heart]);

					} else {
						button_in(now_question - 1, now_question);
						now_question--;

						button_background_choose(
								choose_length[now_question + 1],
								now_question + 1);
						question_background_tv
								.setText(Question_Data[now_question + 1]);

					}

					button_set(choose_length[now_question], now_question);
					question.setText(Question_Data[now_question]);
					but_next_state++;
					next_imbut.setVisibility(View.VISIBLE);
					if (but_next_state == 1)
						background_next_imbut.setVisibility(View.INVISIBLE);
					else
						background_next_imbut.setVisibility(View.VISIBLE);

					vf.setInAnimation(Scan_beacon.this, R.anim.in_from_left);
					vf.setOutAnimation(Scan_beacon.this, R.anim.out_to_right);
					vf.showNext();
					if ((now_question + 1) == 1) {
						back_imbut.setVisibility(View.INVISIBLE);
						background_back_imbut.setVisibility(View.INVISIBLE);
					} else {
						back_imbut.setVisibility(View.VISIBLE);
					}
					view_state++;
				}
			} else if (lastX - but_touch > currentX) {
				if ((Question_Data[now_question].length() != 0)
						&& (now_write_question > now_question)) {

					for (int j = 0; j < jump_question_math; j++) {
						if (jump_question[j][0] == now_question + 1) {
							jump_flag = true;
							flag_question = j;
							flag_choose = choose_but_in[question_array[question_array_flag - 1]];
						}
					}
					if (((choose_but_in[now_question]) != 0)) {

						question_background_tv
								.setText(Question_Data[now_question]);
						button_background_choose(choose_length[now_question],
								now_question);

						if (jump_flag) {
							button_in(
									now_question
											+ jump_question[flag_question][flag_choose],
									now_question);
							now_question = now_question
									+ jump_question[flag_question][flag_choose];
						} else {
							button_in(now_question + 1, now_question);
							now_question++;
						}

						question_array_flag++;
						button_set(choose_length[now_question], now_question);
						question.setText(Question_Data[now_question]);
						but_next_state--;
						vf.setInAnimation(Scan_beacon.this,
								R.anim.in_from_right);
						vf.setOutAnimation(Scan_beacon.this, R.anim.out_to_left);
						vf.showPrevious();
						back_imbut.setVisibility(View.VISIBLE);
						view_state--;
						if (now_question != 1)
							background_back_imbut.setVisibility(View.VISIBLE);
						if (but_next_state == 0) {
							view_state = 0;
							next_imbut.setVisibility(View.INVISIBLE);
							background_next_imbut.setVisibility(View.VISIBLE);
						}
					}

				}

			} else if (i < 7)
				question_now(i);

			if ((choose_but_in[now_question] == 0)) {
				view_state = 0;
				background_next_imbut.setVisibility(View.INVISIBLE);
				next_imbut.setVisibility(View.INVISIBLE);
			} else {
				background_next_imbut.setVisibility(View.VISIBLE);
				next_imbut.setVisibility(View.VISIBLE);
			}

			break;
		}

		}
		return true;
	}

	Button.OnTouchListener choose1 = new Button.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent touchevent) {
			return touch(touchevent, 1);
		}
	};

	Button.OnTouchListener choose2 = new Button.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent touchevent) {
			return touch(touchevent, 2);
		}
	};

	Button.OnTouchListener choose3 = new Button.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent touchevent) {
			return touch(touchevent, 3);
		}
	};

	Button.OnTouchListener choose4 = new Button.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent touchevent) {
			return touch(touchevent, 4);
		}
	};

	Button.OnTouchListener choose5 = new Button.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent touchevent) {
			return touch(touchevent, 5);
		}
	};

	Button.OnTouchListener choose6 = new Button.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent touchevent) {
			return touch(touchevent, 6);
		}
	};

	LinearLayout.OnTouchListener LinearLayout_choose = new Button.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent touchevent) {
			return touch(touchevent, 7);
		}
	};

	/************************** 滑動 ************************/
	public void back_imbut(View v) {
		if (now_question > 0) {
			view_state++;
			int flag_question = 0;
			int flag_choose = 0;
			boolean jump_flag = false;
			question_array_flag--;
			for (int j = 0; j < jump_question_math; j++) {
				if (jump_question[j][0] == question_array[question_array_flag - 1] + 1) {
					jump_flag = true;
					flag_question = j;
					flag_choose = choose_but_in[question_array[question_array_flag - 1]];
				}
			}
			button_background_choose(choose_length[now_question], now_question);
			question_background_tv.setText(Question_Data[now_question]);

			if (jump_flag) {
				button_in(now_question
						- jump_question[flag_question][flag_choose],
						now_question);
				now_question = now_question
						- jump_question[flag_question][flag_choose];
			} else {
				button_in(now_question - 1, now_question);
				now_question--;
			}
			button_set(choose_length[now_question], now_question);
			question.setText(Question_Data[now_question]);
			but_next_state++;
			next_imbut.setVisibility(View.VISIBLE);
			background_next_imbut.setVisibility(View.VISIBLE);
			if ((now_question + 1) == 1) {
				back_imbut.setVisibility(View.INVISIBLE);
				background_back_imbut.setVisibility(View.INVISIBLE);
			} else {
				back_imbut.setVisibility(View.VISIBLE);
			}
		}
	}

	public void next_imbut(View v) {

		int flag_question = 0;
		int flag_choose = 0;
		boolean jump_flag = false;

		if ((Question_Data[now_question].length() != 0)
				&& (now_write_question > now_question)) {

			for (int j = 0; j < jump_question_math; j++) {
				if (jump_question[j][0] == now_question + 1) {
					// question_array[question_array_flag]+1
					jump_flag = true;
					flag_question = j;
					flag_choose = choose_but_in[question_array[question_array_flag - 1]];
				}
			}
			if ((choose_but_in[now_question]) != 0) {

				for (int j = 0; j < jump_question_math; j++) {
					if (jump_question[j][0] == now_question + 1) {
						// question_array[question_array_flag]+1
						jump_flag = true;
						flag_question = j;
						flag_choose = choose_but_in[question_array[question_array_flag - 1]];
					}
				}

				question_background_tv.setText(Question_Data[now_question]);
				button_background_choose(choose_length[now_question],
						now_question);

				if (jump_flag) {
					button_in(now_question
							+ jump_question[flag_question][flag_choose],
							now_question);
					now_question = now_question
							+ jump_question[flag_question][flag_choose];
				} else {
					button_in(now_question + 1, now_question);
					now_question++;
				}

				question_array_flag++;
				if (view_state > 0)
					view_state--;
				button_set(choose_length[now_question], now_question);
				question.setText(Question_Data[now_question]);
				but_next_state--;
				back_imbut.setVisibility(View.VISIBLE);
				if (now_question != 1)
					background_back_imbut.setVisibility(View.VISIBLE);
				if (but_next_state == 0) {
					view_state = 0;
					next_imbut.setVisibility(View.INVISIBLE);
					background_next_imbut.setVisibility(View.INVISIBLE);
				}
			}
		}
		if ((choose_but_in[now_question] == 0)) {
			background_next_imbut.setVisibility(View.INVISIBLE);
			next_imbut.setVisibility(View.INVISIBLE);
			view_state = 0;
		} else {
			background_next_imbut.setVisibility(View.VISIBLE);
			next_imbut.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		scan_state = true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
		Log.d("error", "2");

	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			scan_state = false;
			try {
				final Handler handler = new Handler();
				set_gif_init(false);
				typegifview.view_this();
				mScanning = false;
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
				down_state = false;
				handler.removeCallbacks(SCAN_BLE_Device);
				timer.cancel();
				time_scan = 0;
				tag = 2;
				find.setClickable(true);
				dialog.dismiss();
				Log.d("dialog", "關閉dialog");
			} catch (Exception e) {
				Log.d("scan_state_error", "關閉dialog失敗");
			}
			scanLeDevice(false);
			mLeDeviceListAdapter.clear();
		} catch (Exception e) {
			Log.d("onPause_ERROR", "" + e.getMessage());
		}

	}

	int down_questuin_flag = 0;
	Runnable down_questuin;

	private void down_questuin() {
		final Handler handler = new Handler();
		down_questuin_flag=0;
		down_questuin = new Runnable() {
			@Override
			public void run() {
				if (down_state) {
					if (down_questuin_flag > 10) {
						try {
							find.setClickable(true);
							Toast.makeText(getApplicationContext(), "下載失敗", Toast.LENGTH_SHORT).show();
							set_gif_init(false);
							typegifview.view_this();
							down_state = false;
							handler.removeCallbacks(down_questuin);
						} catch (Exception e) {
						}

					}
				}else{
					try{
						down_state=false;
						find.setClickable(true);
						handler.removeCallbacks(down_questuin);
					}catch(Exception e){}
				}
			}
		};
		long period = 1000;

		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				down_questuin_flag++;
				handler.removeCallbacks(down_questuin);
				handler.post(down_questuin);
			}
		}, 0, period);

	}

	private void scanLeDevice(final boolean enable) {

		if (enable) {
			SCAN_BLE_Device = new Runnable() {
				@Override
				public void run() {
					final Handler handler = new Handler();
					try {
						if (scan_state) {
							tag = 1;
							mLeDeviceListAdapter.clear();
							mScanning = true;
							// clear
							beacon_rssi_f = 0;
							beacon_uuid_f = 0;
							beacon_power_f = 0;
							beacon_distance_f = 0;
							beacon_minor_f = 0;
							beacon_major_f = 0;
							for (int i = 0; i < beacon_rssi_array.length; i++)
								beacon_rssi_array[i] = "";
							for (int i = 0; i < beacon_uuid_array.length; i++)
								beacon_uuid_array[i] = "";
							for (int i = 0; i < beacon_power_array.length; i++)
								beacon_power_array[i] = "";
							for (int i = 0; i < beacon_distance_array.length; i++)
								beacon_distance_array[i] = "";
							for (int i = 0; i < beacon_minor_array.length; i++)
								beacon_minor_array[i] = "";
							for (int i = 0; i < beacon_major_array.length; i++)
								beacon_major_array[i] = "";

							mBluetoothAdapter.startLeScan(mLeScanCallback);
							if (time_scan >= 7)
								devic_listview.setVisibility(View.INVISIBLE);
							if (time_scan >= 8) {
								down_state = false;
								set_gif_init(false);
								typegifview.view_this();
								mScanning = false;
								mBluetoothAdapter.stopLeScan(mLeScanCallback);
								handler.removeCallbacks(SCAN_BLE_Device);
								timer.cancel();
								time_scan = 0;
								tag = 2;
								find.setClickable(true);
								try {
									down_state = false;
									handler_net
											.removeCallbacks(get_compamy_msg);
									dialog.dismiss();
								} catch (Exception e) {
								}
							}
						} else {
							set_gif_init(false);
							typegifview.view_this();
							mScanning = false;
							mBluetoothAdapter.stopLeScan(mLeScanCallback);
							handler.removeCallbacks(SCAN_BLE_Device);
							timer.cancel();
							time_scan = 0;
							tag = 2;
						}
					} catch (Exception e) {
						Log.d("SCAN_FUN_ERROR", "" + e.getMessage());
					}
				}
			};
			long period = 1000;
			final Handler handler = new Handler();
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					time_scan++;
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					handler.removeCallbacks(SCAN_BLE_Device);
					handler.post(SCAN_BLE_Device);

					// time_scan=0;

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
	private class LeDeviceListAdapter extends BaseAdapter {

		private ArrayList<BluetoothDevice> mLeDevices;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			try {
				mLeDevices = new ArrayList<BluetoothDevice>();
				mInflator = Scan_beacon.this.getLayoutInflater();
			} catch (Exception s) {

			}

		}

		public void addDevice(BluetoothDevice device) {
			try {
				if (!mLeDevices.contains(device)) {
					mLeDevices.add(device);
				}
			} catch (Exception s) {

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

			try {

				ViewHolder viewHolder;
				// General ListView optimization code.
				if (view == null) {
					view = mInflator.inflate(R.layout.listitem_device, null);
					viewHolder = new ViewHolder();
					viewHolder.deviceAddress = (TextView) view
							.findViewById(R.id.device_address);
					viewHolder.deviceName = (TextView) view
							.findViewById(R.id.device_name);
					viewHolder.devicerssi = (TextView) view
							.findViewById(R.id.device_rssi);
					viewHolder.deviceuuid = (TextView) view
							.findViewById(R.id.device_uuid);
					viewHolder.devicedistance = (TextView) view
							.findViewById(R.id.device_distance);
					viewHolder.devicemajor = (TextView) view
							.findViewById(R.id.device_major);
					viewHolder.deviceminor = (TextView) view
							.findViewById(R.id.device_minor);
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
				viewHolder.deviceAddress.setText("MAC:" + device.getAddress());

				if (distance == 0) {
					viewHolder.deviceuuid.setText("");
					viewHolder.devicedistance.setText("");
					viewHolder.devicerssi.setText("RSSI:"
							+ beacon_rssi_array[i]);

				} else {
					viewHolder.deviceuuid.setText("UUID:"
							+ beacon_uuid_array[i]);
					Log.i("MAC", "" + uuid);
					viewHolder.devicerssi.setText("RSSI:"
							+ beacon_rssi_array[i] + "\npower:"
							+ beacon_power_array[i]);
					int check_rssi = Integer.parseInt(beacon_rssi_array[i]);

					if (check_rssi >= -99) {
						if (check_ble_scan_colse_f) {
							try {
								Handler handler = new Handler();
								time_scan = 0;
								tag = 2;
								mScanning = false;
								mBluetoothAdapter.stopLeScan(mLeScanCallback);
								handler.removeCallbacks(SCAN_BLE_Device);
								timer.cancel();

							} catch (Exception e) {
							}
							
							Toast.makeText(getApplicationContext(),
									"掃描到beacon資訊，下載問卷中。", Toast.LENGTH_SHORT)
									.show();
							Thread thread;
							thread = new Thread(get_compamy_msg);// 啟動執行序runnable
							down_state = true;
							thread.start();
							find.setClickable(false);
							down_questuin();
						}
					}
					viewHolder.devicedistance.setText("距離:"
							+ beacon_distance_array[i] + "公尺");
					viewHolder.devicemajor.setText("major:"
							+ beacon_major_array[i]);
					viewHolder.deviceminor.setText("minor:"
							+ beacon_minor_array[i]);
				}
				return view;

			} catch (Exception e) {
				Log.d("getView_ERROR", "" + e.getMessage());
				return null;
			}

		}

	}

	// 掃描裝置
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
				beacon_rssi = "" + rssi;
				beacon_rssi_array[beacon_rssi_f] = beacon_rssi;
				beacon_rssi_f = beacon_rssi_f + 1;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							mLeDeviceListAdapter.addDevice(device);
							mLeDeviceListAdapter.notifyDataSetChanged();
						} catch (Exception e) {
							Log.d("connect_device_ERROR", "" + e.getMessage());
						}

					}
				});
				byte[] uuidBytes = new byte[16];
				// byte[] u = new byte[16];
				System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
				String hexString = bytesToHex(uuidBytes);

				// ibeacon的Major值
				major = (scanRecord[startByte + 20] & 0xff) * 0x100
						+ (scanRecord[startByte + 21] & 0xff);
				beacon_major_array[beacon_major_f] = major + "";
				beacon_major_f = beacon_major_f + 1;
				// ibeacon的Minor值
				minor = (scanRecord[startByte + 22] & 0xff) * 0x100
						+ (scanRecord[startByte + 23] & 0xff);
				beacon_minor_array[beacon_minor_f] = minor + "";
				beacon_minor_f = beacon_minor_f + 1;
				// beacon的UUID值
				uuid = hexString.substring(0, 8) + "-"
						+ hexString.substring(8, 12) + "-"
						+ hexString.substring(12, 16) + "-"
						+ hexString.substring(16, 20) + "-"
						+ hexString.substring(20, 32);
				beacon_uuid_array[beacon_uuid_f] = uuid;
				beacon_uuid_f = beacon_uuid_f + 1;
				int txPower = (scanRecord[startByte + 24]);
				power = txPower;
				beacon_power_array[beacon_power_f] = power + "";
				beacon_power_f = beacon_power_f + 1;
				distance = calculateAccuracy(txPower, rssi);

				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(2);
				beacon_distance_array[beacon_distance_f] = nf.format(distance)
						+ "";
				beacon_distance_f = beacon_distance_f + 1;
			}
		}

	};

	static class ViewHolder {
		TextView deviceName;
		TextView deviceuuid;
		TextView devicerssi;
		TextView deviceAddress;
		TextView devicedistance;
		TextView devicemajor;
		TextView deviceminor;
	}

	static final char[] hexArray = "0123456789ABCDEF".toCharArray();// uuid換算的值

	private static String bytesToHex(byte[] bytes) {// 將uuid資料換算成人類看得懂的值
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	protected static double calculateAccuracy(int txPower, double rssi) {
		if (rssi == 0) {
			return -1.0; // if we cannot determine accuracy, return -1.
		}

		double ratio = rssi * 1.0 / txPower;
		if (ratio < 1.0) {
			return Math.pow(ratio, 10);
		} else {
			double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
			return accuracy;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeDatabase();
	}

	public void exit(View v) {

		background_next_imbut.setVisibility(View.INVISIBLE);
		next_imbut.setVisibility(View.INVISIBLE);
		background_back_imbut.setVisibility(View.INVISIBLE);
		back_imbut.setVisibility(View.INVISIBLE);
		title_background_layout.setVisibility(View.GONE);
		question_background_layout.setVisibility(View.GONE);
		Questionnaire_layout.setVisibility(View.GONE);
		now_question = 0;

		but_next_state = 0;
		view_state = 0;

		beacon_layout.setVisibility(View.VISIBLE);
		Questionnaire_layout.setVisibility(View.GONE);
		Questionnaire_background_layout.setVisibility(View.GONE);
		mLeDeviceListAdapter.clear();
		devic_listview.setAdapter(mLeDeviceListAdapter);
		choose_button_layout.setVisibility(View.VISIBLE);

		now_question = 0;
		now_write_question = 0;
		but_next_state = 0;
		choose_but_in = new int[100];
		Question_Data = new String[100];
		Question_Choose_Data = new String[100];
		user_answer = new String[100];
		choose_length = new int[100];
		question_ans = new String[100];
		dissect_choose_data = new String[100][100];
		jump_flag = false;

	}

	/***************** 切割問卷題數及選項 ***************/
	public void question_data(String all_data) {
		question_array_flag = 1;
		String question = "";
		String choose = "";
		String jump = "";
		int[] quest_data_address = new int[Interval];
		int quest_data_address_f = 0;
		try {
			// 擷取位置判定
			for (int i = 0; i < all_data.length(); i++) {
				char ch;
				ch = all_data.charAt(i);
				if (ch == ';') {
					quest_data_address[quest_data_address_f] = i;
					quest_data_address_f++;
				}
			}
			question = all_data.substring(0, quest_data_address[0]);
			choose = all_data.substring(quest_data_address[0] + 1,
					quest_data_address[1]);
			jump = all_data.substring(quest_data_address[1] + 1,
					all_data.length());
			// 將問題擷取並放置陣列裡
			Dissect_question(question);
			Dissect_choose_all_data(choose);
			if (jump.length() != 0) {
				jump_flag = true;
				Dissect_jump(jump);
			} else
				jump_flag = false;
			// Toast.makeText(getApplicationContext(), jump,
			// Toast.LENGTH_LONG).show();

		} catch (Exception e) {
			Log.d("擷取_question_data_Error", "" + e.getMessage());
			Toast.makeText(this, "擷取_question_data_Error", Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void Dissect_jump(String jump_data) {
		jump_question = new int[100][100];
		int[] quest_data_address = new int[Interval];
		int quest_data_address_f = 0;
		jump_question_math = 0;
		// 擷取位置判定
		for (int i = 0; i < jump_data.length(); i++) {
			char ch;
			ch = jump_data.charAt(i);
			if (ch == '&') {
				quest_data_address[quest_data_address_f] = i;
				quest_data_address_f++;
			}
		}

		jump_question_data = new String[quest_data_address_f];
		array_choose_math = new int[quest_data_address_f];
		jump_question_math = quest_data_address_f;
		for (int i = 0; i < jump_question_data.length; i++) {
			if (i == jump_question_data.length - 1)
				jump_question_data[i] = jump_data.substring(
						quest_data_address[i] + 1, jump_data.length());
			else
				jump_question_data[i] = jump_data.substring(
						quest_data_address[i] + 1, quest_data_address[i + 1]);

		}

		for (int i = 0; i < jump_question_data.length; i++) {
			int[] jump_data_address = new int[Interval];
			int jump_data_address_f = 0;

			for (int j = 0; j < jump_question_data[i].length(); j++) {
				char ch;
				ch = jump_question_data[i].charAt(j);
				if (ch == ',') {
					jump_data_address[jump_data_address_f] = j;
					jump_data_address_f++;
				}
			}
			array_choose_math[i] = jump_data_address_f;

			for (int j = 0; j < jump_data_address_f + 1; j++) {
				if (j == 0)
					jump_question[i][j] = Integer.valueOf(jump_question_data[i]
							.substring(0, jump_data_address[j]));
				else if (j == (jump_data_address_f))
					jump_question[i][j] = Integer.valueOf(jump_question_data[i]
							.substring(jump_data_address[j - 1] + 1,
									jump_question_data[i].length()));
				else
					jump_question[i][j] = Integer.valueOf(jump_question_data[i]
							.substring(jump_data_address[j - 1] + 1,
									jump_data_address[j]));
			}
		}
	}

	public void Dissect_choose() {

		for (int i = 0; i < question_math; i++) {
			// dissect_choose_data[i][]=

			int[] quest_data_address = new int[Interval];

			int quest_data_address_f = 0;
			for (int j = 0; j < dissect_choose_data[i].length; j++)
				dissect_choose_data[i][j] = "";

			try {
				// 擷取選項位置判定
				for (int j = 0; j < Question_Choose_Data[i].length(); j++) {
					char ch;
					ch = Question_Choose_Data[i].charAt(j);
					if (ch == '，') {
						quest_data_address[quest_data_address_f] = j;
						quest_data_address_f++;
					}
				}
				choose_length[i] = quest_data_address_f + 1;
				// 將問題擷取並放置陣列裡
				for (int j = 0; j < quest_data_address_f + 1; j++) {

					if (j == 0)
						dissect_choose_data[i][j] = Question_Choose_Data[i]
								.substring(0, quest_data_address[j]);
					else if (j == quest_data_address_f)
						dissect_choose_data[i][j] = Question_Choose_Data[i]
								.substring(quest_data_address[j - 1] + 1,
										Question_Choose_Data[i].length());
					else
						dissect_choose_data[i][j] = Question_Choose_Data[i]
								.substring(quest_data_address[j - 1] + 1,
										quest_data_address[j]);
				}
			} catch (Exception e) {
				Log.d("擷取_Error_choose", "" + e.getMessage());
				Toast.makeText(this, "擷取_Error_choose", Toast.LENGTH_SHORT)
						.show();
			}

		}

	}

	public void Dissect_choose_all_data(String choose_data) {
		// 初始
		// Toast.makeText(this, "" + choose_data, Toast.LENGTH_LONG).show();
		int[] quest_data_address = new int[Interval];
		int quest_data_address_f = 0;
		for (int i = 0; i < Question_Choose_Data.length; i++)
			Question_Choose_Data[i] = "";
		try {
			// 擷取位置判定
			for (int i = 0; i < choose_data.length(); i++) {
				char ch;
				ch = choose_data.charAt(i);
				if (ch == '@') {
					quest_data_address[quest_data_address_f] = i;
					quest_data_address_f++;
				}
			}
			// 將問題擷取並放置陣列裡
			for (int i = 0; i < quest_data_address_f; i++) {
				if (i == (quest_data_address_f - 1))
					Question_Choose_Data[i] = choose_data.substring(
							quest_data_address[i] + 1, choose_data.length());
				else
					Question_Choose_Data[i] = choose_data.substring(
							quest_data_address[i] + 1,
							quest_data_address[i + 1]);
			}
			String data = "";
			for (int i = 0; i < Question_Choose_Data.length; i++)
				data = data + "\n" + Question_Choose_Data[i];

			Dissect_choose();
		} catch (Exception e) {
			Log.d("擷取_Choose_Error", "" + e.getMessage());
			Toast.makeText(this, "擷取_Choose_Error", Toast.LENGTH_SHORT).show();
		}
	}

	public void Dissect_question(String quest_data) {
		// 初始
		int[] quest_data_address = new int[Interval]; // 分隔
		int quest_data_address_f = 0;
		for (int i = 0; i < Question_Data.length; i++)
			Question_Data[i] = "";

		try {
			// 擷取位置判定
			for (int i = 1; i < quest_data.length(); i++) {
				char ch;
				ch = quest_data.charAt(i);
				if (ch == '|') {
					quest_data_address[quest_data_address_f] = i;
					quest_data_address_f++;
				}
			}
			question_math = quest_data_address_f;
			question_array = new int[question_math];
			array_question_state = new int[question_math];
			for (int i = 0; i < array_question_state.length; i++)
				array_question_state[i] = 0;
			question_array[0] = 0;
			// 將問題擷取並放置陣列裡
			for (int i = 0; i < quest_data_address_f; i++) {
				if (i == 0)
					Question_Data[i] = quest_data.substring(1,
							quest_data_address[i]);
				else
					Question_Data[i] = quest_data.substring(
							quest_data_address[i - 1] + 1,
							quest_data_address[i]);
				if (i == (quest_data_address_f - 1)) {
					company_id = quest_data.substring(
							quest_data_address[i] + 1, quest_data.length());
				}

			}
		} catch (Exception e) {
			Log.d("擷取_Error", "" + e.getMessage());
			Toast.makeText(this, "擷取_Error", Toast.LENGTH_SHORT).show();
		}

	}

	/***************** 切割問卷題數及選項 ***************/

	public void question_end() {
		try {
			new Thread(return_save_question_ans).start();// 啟動執行序runnable

			title_background_layout.setVisibility(View.GONE);
			question_background_layout.setVisibility(View.GONE);
			Questionnaire_layout.setVisibility(View.GONE);
			now_question = 0;

			but_next_state = 0;
			dialog_get();
		} catch (Exception e) {
			Log.d("question_end_error", "" + e.getMessage());
		}
	}

	public void add(String company_name, String data, String trade_in,
			String status, String number) {

		time();
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COMPANY_NAME, company_name);
		values.put(DATA, data);
		values.put(TRADE_IN, trade_in);
		values.put(STATUS, status);
		values.put(DATE, finish_time);
		values.put(NUMBER, number);
		db.insert(TABLE_NAME, null, values);
	}

	private void openDatabase() {
		dbhelper = new DBHelper(this);
	}

	private void closeDatabase() {
		dbhelper.close();
	}

	String finish_time = "";
	Date curDate = new Date(System.currentTimeMillis());

	public void time() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		curDate = new Date(System.currentTimeMillis());
		finish_time = formatter.format(curDate);
		// finish_tiom =curDate+"";
	}

	/********************** get_compamy_msg ********************/

	public void RESCAN(View v) {

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			try {
				scan_state = false;
				try {
					set_gif_init(false);
					typegifview.view_this();
					final Handler handler = new Handler();
					mScanning = false;

					handler.removeCallbacks(SCAN_BLE_Device);
					time_scan = 0;
					tag = 2;
					find.setClickable(true);
					dialog.dismiss();
					Log.d("dialog", "關閉dialog");
				} catch (Exception e) {
					Log.d("scan_state_error", "關閉dialog失敗");
				}
				scanLeDevice(false);
				mLeDeviceListAdapter.clear();
			} catch (Exception e) {
				Log.d("onPause_ERROR", "" + e.getMessage());
			}
			try {
				scan_state = true;
				find.setClickable(false);
				beacon_layout.setVisibility(View.VISIBLE);
				check_ble_scan_colse_f = true;
				mLeDeviceListAdapter = new LeDeviceListAdapter();
				devic_listview.setAdapter(mLeDeviceListAdapter);
				scanLeDevice(true);
				time_scan = 0;
				set_gif_init(true);
				typegifview.setSrc(R.drawable.search);
				typegifview.setStart(R.raw.search);
			} catch (Exception e) {
				Toast.makeText(Scan_beacon.this, "連線失敗", Toast.LENGTH_SHORT)
						.show();
			}
			try {
				dialog.dismiss();
			} catch (Exception e) {
			}

		}

	}

	AlertDialog dialog;

	private void dialog_success() {
		find.setClickable(true);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(Scan_beacon.this);
		final View v = inflater.inflate(R.layout.dialog, null);
		builder.setView(v)
				.setCancelable(false)
				.setPositiveButton("填寫", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						title_background_layout.setVisibility(View.VISIBLE);
						question_background_layout.setVisibility(View.VISIBLE);
						Questionnaire_layout.setVisibility(View.VISIBLE);
						set_gif_init(false);
						typegifview.view_this();
						back_imbut.setVisibility(View.INVISIBLE);
						background_back_imbut.setVisibility(View.INVISIBLE);
						question_data(company_data);
						now_question = 0;
						question.setText(Question_Data[now_question]);
						beacon_layout.setVisibility(View.GONE);
						Questionnaire_layout.setVisibility(View.VISIBLE);
						Questionnaire_background_layout
								.setVisibility(View.VISIBLE);
						LinearLayout question_layout = (LinearLayout) findViewById(R.id.question_layout);
						question_layout.setVisibility(View.VISIBLE);
						button_set(choose_length[now_question], now_question);
						button_background_choose(choose_length[now_question],
								now_question);
						vf.setVisibility(View.VISIBLE);
						now_question = 0;
						now_write_question = 0;
						for (int i = 0; i < choose_but_in.length; i++) {
							choose_but_in[i] = 0;
						}

						button_reset_state();
					}
				})
				.setNegativeButton("下次填寫",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								set_gif_init(false);
								typegifview.view_this();
								beacon_layout.setVisibility(View.VISIBLE);
								Questionnaire_layout.setVisibility(View.GONE);
								Questionnaire_background_layout
										.setVisibility(View.GONE);
								mLeDeviceListAdapter.clear();
								devic_listview.setAdapter(mLeDeviceListAdapter);
								dialog.dismiss();
								try {
									down_state = false;
									handler_net
											.removeCallbacks(get_compamy_msg);
								} catch (Exception e) {
								}

							}
						});
		dialog = builder.show();
	}

	public void dialog_get() {
		File new_data_status = new File(dir_Internal, "new_data_status.txt");
		writeToFile(new_data_status, "1");
		LayoutInflater inflater = LayoutInflater.from(Scan_beacon.this);
		final View v = inflater.inflate(R.layout.dialog_offsetting, null);
		new AlertDialog.Builder(Scan_beacon.this)
				.setView(v)
				.setCancelable(false)
				.setPositiveButton("確 認",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								try {
									beacon_layout.setVisibility(View.VISIBLE);
									Questionnaire_layout
											.setVisibility(View.GONE);
									Questionnaire_background_layout
											.setVisibility(View.GONE);
									mLeDeviceListAdapter.clear();
									devic_listview
											.setAdapter(mLeDeviceListAdapter);
									choose_button_layout
											.setVisibility(View.VISIBLE);
									background_next_imbut
											.setVisibility(View.INVISIBLE);
									next_imbut.setVisibility(View.INVISIBLE);
									background_back_imbut
											.setVisibility(View.INVISIBLE);
									back_imbut.setVisibility(View.INVISIBLE);
								} catch (Exception e) {
									Log.d("dialog_success_sure",
											"" + e.getMessage());
								}
							}
						}).show();
	}

	Handler get_compamy_msg_handler_Success = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("key");// 取出key中的字串存入val
			company_data = val.substring(1, val.length());
			question = (TextView) findViewById(R.id.question_tv);
			question_background_tv = (TextView) findViewById(R.id.question_background_tv);
			/********* 成功存進資料，換頁嘗試連線 *********/

			if (down_state) {
				if (company_data.equals("0"))
					Toast.makeText(Scan_beacon.this, "資料庫未尋到此裝置的資料",
							Toast.LENGTH_SHORT).show();
				else if (company_data.equals("該公司可能尚未傳問卷題目到資料庫"))
					Toast.makeText(Scan_beacon.this, company_data,
							Toast.LENGTH_SHORT).show();
				else {
					//scanLeDevice(false);
					if (scan_state)
						dialog_success();
				}
			}
			/********* 成功存進資料，換頁嘗試連線 *********/
			down_state = false;
			set_gif_init(false);
			typegifview.view_this();
		}
	};
	Handler get_compamy_msg_handler_Error = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("key");
			Toast.makeText(getApplicationContext(), val, Toast.LENGTH_LONG)
					.show();
		}
	};

	Handler get_compamy_msg_handler_Nodata = new Handler() {
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

	Runnable get_compamy_msg = new Runnable() {
		@Override
		public void run() {
			//
			// TODO: http request.
			//
			if (down_state) {

				Message msg = new Message();
				Bundle data = new Bundle();
				msg.setData(data);
				try {
					// 連線到 url網址
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost method = new HttpPost(URL);

					// 傳值給PHP
					List<NameValuePair> vars = new ArrayList<NameValuePair>();
					vars.add(new BasicNameValuePair("mac", uuid));
					// vars.add(new
					// BasicNameValuePair("mac","E2C56DB5-DFFB-48D2-B060-D0F5A71096E0"));
					method.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));

					// 接收PHP回傳的資料
					HttpResponse response = httpclient.execute(method);
					HttpEntity entity = response.getEntity();

					if (entity != null) {
						data.putString("key",
								EntityUtils.toString(entity, "utf-8"));// 如果成功將網頁內容存入key
						get_compamy_msg_handler_Success.sendMessage(msg);

					} else {
						data.putString("key", "無資料");
						get_compamy_msg_handler_Nodata.sendMessage(msg);
					}
				} catch (Exception e) {
					data.putString("key", "連線失敗1");
					get_compamy_msg_handler_Error.sendMessage(msg);
					try {
						down_state = false;
						handler_net.removeCallbacks(get_compamy_msg);
					} catch (Exception ex) {
					}
				}
			} else {
				try {
					handler_net.removeCallbacks(get_compamy_msg);
				} catch (Exception e) {
				}
			}

		}
	};

	private Handler handler_net = new Handler();
	Runnable return_save_question_ans = new Runnable() {
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
				HttpPost method = new HttpPost(URL_ans);

				// 傳值給PHP
				List<NameValuePair> vars = new ArrayList<NameValuePair>();
				time();
				vars.add(new BasicNameValuePair("company_id", company_id));
				for (int i = 1; i < 20; i++)
					vars.add(new BasicNameValuePair("question_answer" + i,
							question_ans[i - 1]));
				vars.add(new BasicNameValuePair("date", finish_time));

				method.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));

				// 接收PHP回傳的資料
				HttpResponse response = httpclient.execute(method);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					data.putString("key", EntityUtils.toString(entity, "utf-8"));// 如果成功將網頁內容存入key
					return_save_question_ans_handler_Success.sendMessage(msg);

				} else {
					data.putString("key", "無資料");
					get_compamy_msg_handler_Nodata.sendMessage(msg);
				}
			} catch (Exception e) {
				data.putString("key", "連線失敗");
				get_compamy_msg_handler_Error.sendMessage(msg);
				handler_net.removeCallbacks(return_save_question_ans);
			}

		}
	};

	Handler return_save_question_ans_handler_Success = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("key");// 取出key中的字串存入val
			company_data = val.substring(1, val.length());
			;

			/********* 成功存進資料，換頁嘗試連線 *********/

			add(company[Integer.valueOf(company_id)],
					company[Integer.valueOf(company_id)] + " 5元抵用卷",
					test_Instruction, "1", (Integer.valueOf(company_data) + 1)
							+ "");
			company_id = "";

			/********* 成功存進資料，換頁嘗試連線 *********/

		}
	};

	/********************** get_compamy_msg ********************/

	/******************************************* 檔案建立 *******************************************/

	// 寫入資料
	private void writeToFile(File fout, String data) {
		FileOutputStream osw = null;
		try {
			osw = new FileOutputStream(fout);
			osw.write(data.getBytes());
			osw.flush();
		} catch (Exception e) {
			;
		} finally {
			try {
				osw.close();
			} catch (Exception e) {
				;
			}
		}
	}

	/******************************************* 檔案建立 *******************************************/

	/********************* button_set *********************/
	public void button_view_choose(int choose_length, int question_id) {
		if (choose_length == 2) {
			choose_but0.setText("" + dissect_choose_data[question_id][0]);
			choose_but1.setText("" + dissect_choose_data[question_id][1]);
		} else if (choose_length == 3) {
			choose_but0.setText("" + dissect_choose_data[question_id][0]);
			choose_but1.setText("" + dissect_choose_data[question_id][1]);
			choose_but2.setText("" + dissect_choose_data[question_id][2]);
		} else if (choose_length == 4) {
			choose_but0.setText("" + dissect_choose_data[question_id][0]);
			choose_but1.setText("" + dissect_choose_data[question_id][1]);
			choose_but2.setText("" + dissect_choose_data[question_id][2]);
			choose_but3.setText("" + dissect_choose_data[question_id][3]);
		} else if (choose_length == 5) {
			choose_but0.setText("" + dissect_choose_data[question_id][0]);
			choose_but1.setText("" + dissect_choose_data[question_id][1]);
			choose_but2.setText("" + dissect_choose_data[question_id][2]);
			choose_but3.setText("" + dissect_choose_data[question_id][3]);
			choose_but4.setText("" + dissect_choose_data[question_id][4]);
		} else {
			choose_but0.setText("" + dissect_choose_data[question_id][0]);
			choose_but1.setText("" + dissect_choose_data[question_id][1]);
			choose_but2.setText("" + dissect_choose_data[question_id][2]);
			choose_but3.setText("" + dissect_choose_data[question_id][3]);
			choose_but4.setText("" + dissect_choose_data[question_id][4]);
			choose_but5.setText("" + dissect_choose_data[question_id][5]);
		}
		for (int i = 0; i < user_answer.length; i++)
			user_answer[i] = "";
		for (int i = 0; i < choose_length; i++)
			user_answer[i] = dissect_choose_data[question_id][i];

	}

	public void button_background_choose(int choose_length, int question_id) {
		button_background_reset();
		if (choose_length == 2) {
			background_choose_but0.setText(""
					+ dissect_choose_data[question_id][0]);
			background_choose_but1.setText(""
					+ dissect_choose_data[question_id][1]);
			background_choose_but0.setVisibility(View.VISIBLE);
			background_choose_but1.setVisibility(View.VISIBLE);
		} else if (choose_length == 3) {
			background_choose_but0.setText(""
					+ dissect_choose_data[question_id][0]);
			background_choose_but1.setText(""
					+ dissect_choose_data[question_id][1]);
			background_choose_but2.setText(""
					+ dissect_choose_data[question_id][2]);
			background_choose_but0.setVisibility(View.VISIBLE);
			background_choose_but1.setVisibility(View.VISIBLE);
			background_choose_but2.setVisibility(View.VISIBLE);
		} else if (choose_length == 4) {
			background_choose_but0.setText(""
					+ dissect_choose_data[question_id][0]);
			background_choose_but1.setText(""
					+ dissect_choose_data[question_id][1]);
			background_choose_but2.setText(""
					+ dissect_choose_data[question_id][2]);
			background_choose_but3.setText(""
					+ dissect_choose_data[question_id][3]);
			background_choose_but0.setVisibility(View.VISIBLE);
			background_choose_but1.setVisibility(View.VISIBLE);
			background_choose_but2.setVisibility(View.VISIBLE);
			background_choose_but3.setVisibility(View.VISIBLE);
		} else if (choose_length == 5) {
			background_choose_but0.setText(""
					+ dissect_choose_data[question_id][0]);
			background_choose_but1.setText(""
					+ dissect_choose_data[question_id][1]);
			background_choose_but2.setText(""
					+ dissect_choose_data[question_id][2]);
			background_choose_but3.setText(""
					+ dissect_choose_data[question_id][3]);
			background_choose_but4.setText(""
					+ dissect_choose_data[question_id][4]);
			background_choose_but0.setVisibility(View.VISIBLE);
			background_choose_but1.setVisibility(View.VISIBLE);
			background_choose_but2.setVisibility(View.VISIBLE);
			background_choose_but3.setVisibility(View.VISIBLE);
			background_choose_but4.setVisibility(View.VISIBLE);
		} else {
			background_choose_but0.setText(""
					+ dissect_choose_data[question_id][0]);
			background_choose_but1.setText(""
					+ dissect_choose_data[question_id][1]);
			background_choose_but2.setText(""
					+ dissect_choose_data[question_id][2]);
			background_choose_but3.setText(""
					+ dissect_choose_data[question_id][3]);
			background_choose_but4.setText(""
					+ dissect_choose_data[question_id][4]);
			background_choose_but5.setText(""
					+ dissect_choose_data[question_id][5]);

			background_choose_but0.setVisibility(View.VISIBLE);
			background_choose_but1.setVisibility(View.VISIBLE);
			background_choose_but2.setVisibility(View.VISIBLE);
			background_choose_but3.setVisibility(View.VISIBLE);
			background_choose_but4.setVisibility(View.VISIBLE);
			background_choose_but5.setVisibility(View.VISIBLE);
		}

	}

	public void button_set(int choose_length, int question_id) {
		button_reset();
		if (choose_length == 2) {
			choose_but0.setVisibility(View.VISIBLE);
			choose_but1.setVisibility(View.VISIBLE);
		} else if (choose_length == 3) {
			choose_but0.setVisibility(View.VISIBLE);
			choose_but1.setVisibility(View.VISIBLE);
			choose_but2.setVisibility(View.VISIBLE);
		} else if (choose_length == 4) {
			choose_but0.setVisibility(View.VISIBLE);
			choose_but1.setVisibility(View.VISIBLE);
			choose_but2.setVisibility(View.VISIBLE);
			choose_but3.setVisibility(View.VISIBLE);
		} else if (choose_length == 5) {
			choose_but0.setVisibility(View.VISIBLE);
			choose_but1.setVisibility(View.VISIBLE);
			choose_but2.setVisibility(View.VISIBLE);
			choose_but3.setVisibility(View.VISIBLE);
			choose_but4.setVisibility(View.VISIBLE);
		} else {
			choose_but0.setVisibility(View.VISIBLE);
			choose_but1.setVisibility(View.VISIBLE);
			choose_but2.setVisibility(View.VISIBLE);
			choose_but3.setVisibility(View.VISIBLE);
			choose_but4.setVisibility(View.VISIBLE);
			choose_but5.setVisibility(View.VISIBLE);
		}

		button_view_choose(choose_length, question_id);
	}

	public void button_background_set(int choose_length, int question_id) {
		button_background_reset();

		if (choose_length == 2) {
			background_choose_but0.setVisibility(View.VISIBLE);
			background_choose_but1.setVisibility(View.VISIBLE);
		} else if (choose_length == 3) {
			background_choose_but0.setVisibility(View.VISIBLE);
			background_choose_but1.setVisibility(View.VISIBLE);
			background_choose_but2.setVisibility(View.VISIBLE);
		} else if (choose_length == 4) {
			background_choose_but0.setVisibility(View.VISIBLE);
			background_choose_but1.setVisibility(View.VISIBLE);
			background_choose_but2.setVisibility(View.VISIBLE);
			background_choose_but3.setVisibility(View.VISIBLE);
		} else if (choose_length == 5) {
			background_choose_but0.setVisibility(View.VISIBLE);
			background_choose_but1.setVisibility(View.VISIBLE);
			background_choose_but2.setVisibility(View.VISIBLE);
			background_choose_but3.setVisibility(View.VISIBLE);
			background_choose_but4.setVisibility(View.VISIBLE);
		} else {
			background_choose_but0.setVisibility(View.VISIBLE);
			background_choose_but1.setVisibility(View.VISIBLE);
			background_choose_but2.setVisibility(View.VISIBLE);
			background_choose_but3.setVisibility(View.VISIBLE);
			background_choose_but4.setVisibility(View.VISIBLE);
			background_choose_but5.setVisibility(View.VISIBLE);
		}

	}

	public void button_reset() {
		choose_but0.setVisibility(View.INVISIBLE);
		choose_but1.setVisibility(View.INVISIBLE);
		choose_but2.setVisibility(View.INVISIBLE);
		choose_but3.setVisibility(View.INVISIBLE);
		choose_but4.setVisibility(View.INVISIBLE);
		choose_but5.setVisibility(View.INVISIBLE);
	}

	public void button_background_reset() {
		background_choose_but0.setVisibility(View.INVISIBLE);
		background_choose_but1.setVisibility(View.INVISIBLE);
		background_choose_but2.setVisibility(View.INVISIBLE);
		background_choose_but3.setVisibility(View.INVISIBLE);
		background_choose_but4.setVisibility(View.INVISIBLE);
		background_choose_but5.setVisibility(View.INVISIBLE);
	}

	public void button_reset_state() {
		choose_but0.setBackgroundResource(R.drawable.block);
		choose_but1.setBackgroundResource(R.drawable.block);
		choose_but2.setBackgroundResource(R.drawable.block);
		choose_but3.setBackgroundResource(R.drawable.block);
		choose_but4.setBackgroundResource(R.drawable.block);
		choose_but5.setBackgroundResource(R.drawable.block);
		background_choose_but0.setBackgroundResource(R.drawable.block);
		background_choose_but1.setBackgroundResource(R.drawable.block);
		background_choose_but2.setBackgroundResource(R.drawable.block);
		background_choose_but3.setBackgroundResource(R.drawable.block);
		background_choose_but4.setBackgroundResource(R.drawable.block);
		background_choose_but5.setBackgroundResource(R.drawable.block);
	}

	/********************* button_set *********************/

}
