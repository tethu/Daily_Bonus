package com.example.ble_beacon;

public class Time_limit {

	String check_time_data;
	int state=0;
	int now_year = 0;
	int now_month = 0;
	int now_day = 0;
	int old_year = 0;
	int old_month = 0;
	int old_day = 0;
	int month_day[] = { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	Time_limit(String check_time_data,int state) {
		this.check_time_data=check_time_data;
		this.state=state;
	}
	Time_limit(int now_year,int now_month,int now_day,int old_year,int old_month,int old_day){
		this.now_year=now_year;
		this.now_month=now_month;
		this.now_day=now_day;
		this.old_year=old_year;
		this.old_month=old_month;
		this.old_day=old_day;
	}
	public boolean check_OK(){

		if (((now_year / 400) == 0)
				|| (((now_year / 4) == 0) && ((now_year / 100) != 0)))
			month_day[2] = 29;
		else
			month_day[2] = 28;
		if ((now_year == old_year)) {
			if (((now_month - old_month) == 0)) {
				if ((now_day - old_day) > 2)
					return false;
			} else if ((now_month - old_month) == 1) {
				if ((now_day > 2) || (old_day < (month_day[old_day] - 1)))
					return false;
				else if ((now_day == 2)
						&& (old_day == (month_day[old_day] - 1)))
					return false;
			} else
				return false;
		} else if (((now_year - old_year) == 1)) {
			if ((now_month == 1) && (old_month == 12)) {
				if (now_day > 2 || old_day < 30)
					return false;
				else if (now_day == 2 && old_day == 30)
					return false;
			} else
				return false;
		} else {
			return false;
		}
		return true;
	}
	
	public void time_limit() {
		int data_address = 0;
		String now_year_string = "";
		String now_month_string = "";
		String now_day_string = "";
		String old_year_string = "";
		String old_month_string = "";
		String old_day_string = "";
		String date = "";
		// Â^¨ú¦ì¸m§P©w
		for (int i = 0; i < check_time_data.length(); i++) {
			char ch;
			ch = check_time_data.charAt(i);
			if (ch == ' ')
				data_address = i;
		}
		date = check_time_data.substring(0, data_address);
		int[] date_address = new int[2];
		data_address = 0;
		for (int i = 0; i < date.length(); i++) {
			char ch;
			ch = date.charAt(i);
			if (ch == '-') {
				date_address[data_address] = i;
				data_address++;
			}
		}
		if (state == 0) {
			now_year_string = date.substring(0, date_address[0]);
			now_month_string = date.substring(date_address[0] + 1,
					date_address[1]);
			now_day_string = date.substring(date_address[1] + 1, date.length());
			now_year = Integer.valueOf(now_year_string);
			now_month = Integer.valueOf(now_month_string);
			now_day = Integer.valueOf(now_day_string);
		} else {
			old_year_string = date.substring(0, date_address[0]);
			old_month_string = date.substring(date_address[0] + 1,
					date_address[1]);
			old_day_string = date.substring(date_address[1] + 1, date.length());
			old_year = Integer.valueOf(old_year_string);
			old_month = Integer.valueOf(old_month_string);
			old_day = Integer.valueOf(old_day_string);
		}
	}

	public int get_now_year(){
		return now_year;
	}

	public int get_now_month(){
		return now_month;
	}

	public int get_now_day(){
		return now_day;
	}

	public int get_old_year(){
		return old_year;
	}

	public int get_old_month(){
		return old_month;
	}

	public int get_old_day(){
		return old_day;
	}
}
