package com.example.ble_beacon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {
	TabHost tabHost;
	Context context;
	File dir_Internal;
	static String tablename = "NAME";
	public List<TextView> textList = new ArrayList<TextView>();
	public List<ImageView> imageList = new ArrayList<ImageView>();
	public List<ImageView> imageList_new_data = new ArrayList<ImageView>();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_layout);
		context = this;
		dir_Internal = context.getFilesDir();
		Tab("兌獎", R.drawable.icon_coupon_off, SCAN_BLE.class);
		Tab("優惠券", R.drawable.icon_survey_on, Scan_beacon.class);
		Tab("設定", R.drawable.icon_setting_off, SET.class);
		imageList_new_data.get(0).setVisibility(View.INVISIBLE);
		imageList_new_data.get(1).setVisibility(View.INVISIBLE);
		imageList_new_data.get(2).setVisibility(View.INVISIBLE);
		tabHost.setCurrentTab(1);
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				imageList.get(0).setImageDrawable(
						getResources().getDrawable(R.drawable.icon_coupon_off));
				imageList.get(1).setImageDrawable(
						getResources().getDrawable(R.drawable.icon_survey_off));
				imageList.get(2)
						.setImageDrawable(
								getResources().getDrawable(
										R.drawable.icon_setting_off));
				if (tabId.equals("兌獎")) {
					imageList.get(0).setImageDrawable(
							getResources().getDrawable(
									R.drawable.icon_coupon_on));
					File new_data_status = new File(dir_Internal, "new_data_status.txt");
					writeToFile(new_data_status, "0");
					imageList_new_data.get(0).setVisibility(View.INVISIBLE);
				}
				if (tabId.equals("優惠券")) {
					imageList.get(1).setImageDrawable(
							getResources().getDrawable(
									R.drawable.icon_survey_on));
				}
				if (tabId.equals("設定")) {
					imageList.get(2).setImageDrawable(
							getResources().getDrawable(
									R.drawable.icon_setting_on));
				}
			}
		});
		pager();
		
		
	}

	// 頁籤圖示更換及頁籤切換的判定
	private void pager() {
		final Runnable updateUI = new Runnable() {
			@Override
			public void run() {
				// 資料庫資料數顯示
				File new_data_status = new File(dir_Internal, "new_data_status.txt");
				if(readFromFile(new_data_status).equals("1")){
					imageList_new_data.get(0).setVisibility(View.VISIBLE);
				}
			}
		};
		long period = 50;
		final Handler handler = new Handler();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.removeCallbacks(updateUI);
				handler.post(updateUI);
			}
		}, 0, period);
	}

	public void Tab(String name, int image,
			@SuppressWarnings("rawtypes") Class c) {
		tabHost = getTabHost();
		Intent intent = new Intent(this, c);
		TabHost.TabSpec spec = tabHost.newTabSpec(name);
		// 建立tab先做一tab將所需的屬性用layoyt完成-如標題、圖像等....
		View tabIndicator = LayoutInflater.from(this).inflate(
				R.layout.tab_layout_set, getTabWidget(), false);
		TextView title = (TextView) tabIndicator.findViewById(R.id.question_tv);
		title.setText(name);// 標題匯入
		textList.add(title);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.imageView1);
		ImageView icon_new_data = (ImageView) tabIndicator.findViewById(R.id.imageView2);
		imageList.add(icon);
		imageList_new_data.add(icon_new_data);
		icon.setImageResource(image);// 匯入圖
		spec.setIndicator(tabIndicator);// 將完成的layout匯入tab
		spec.setContent(intent);// 將對應標題(按鈕)跟對應class合再一起
		tabHost.addTab(spec);// 將完成的tab加進物件
	}

	/******************************************* 檔案建立 *******************************************/
	//讀取檔案資料
	  private String readFromFile(File fin) {
	  StringBuilder data = new StringBuilder();
	  BufferedReader reader = null;
	  try {
	      reader = new BufferedReader(new InputStreamReader(
	               new FileInputStream(fin), "utf-8"));
	      String line;
	      while ((line = reader.readLine()) != null) {
	          data.append(line);
	      }
	  } catch (Exception e) {
	      ;
	  } finally {
	      try {
	          reader.close();
	      } catch (Exception e) {
	          ;
	      }
	  }
	  return data.toString();
	}
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

}