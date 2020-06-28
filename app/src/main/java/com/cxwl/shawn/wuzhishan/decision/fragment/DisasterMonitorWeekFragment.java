package com.cxwl.shawn.wuzhishan.decision.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.adapter.DisasterMonitorAdapter;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import wheelview.NumericWheelAdapter;
import wheelview.OnWheelScrollListener;
import wheelview.WheelView;

/**
 * 灾害监测-逐周监测
 * @author shawn_sun
 */
public class DisasterMonitorWeekFragment extends Fragment implements OnClickListener {
	
	private TextView tvYear,tvWeek,tvCityName,tvType,tvInfo,tvMark,tvPrompt;
	private LinearLayout llContent;
	private ImageView imageView;
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy", Locale.CHINA);
	private String selectYear = "", selectWeek = "";
	private List<String> weeks = new ArrayList<>();
	private Map<String, JSONObject> dataMap = new LinkedHashMap<>();
	private List<String> types = new ArrayList<>();//灾害种类
	private List<String> citys = new ArrayList<>();//选择市县
	private SwipeRefreshLayout refreshLayout;//下拉刷新布局
	private WheelView year,month,day,hour,minute;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_disaster_monitor_week, null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initRefreshLayout(view);
		initWidget(view);
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout(View view) {
		refreshLayout = view.findViewById(R.id.refreshLayout);
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setProgressViewEndTarget(true, 400);
		refreshLayout.post(new Runnable() {
			@Override
			public void run() {
				refreshLayout.setRefreshing(true);
			}
		});
		refreshLayout.setEnabled(false);
	}

	private void refresh() {
		weeks.clear();
		for (int i = 0; i < 52; i++) {
			weeks.add(String.valueOf(i+1));
		}

		types.clear();
		citys.clear();
		dataMap.clear();
		if (!refreshLayout.isRefreshing()) {
			refreshLayout.setRefreshing(true);
		}
		okHttpCitys();
	}

	private void initWidget(View view) {
		tvYear = view.findViewById(R.id.tvYear);
		tvYear.setOnClickListener(this);
		tvWeek = view.findViewById(R.id.tvWeek);
		tvWeek.setOnClickListener(this);
		tvCityName = view.findViewById(R.id.tvCityName);
		TextView tvCheck = view.findViewById(R.id.tvCheck);
		tvCheck.setOnClickListener(this);
		LinearLayout llType = view.findViewById(R.id.llType);
		llType.setOnClickListener(this);
		LinearLayout llCity = view.findViewById(R.id.llCity);
		llCity.setOnClickListener(this);
		tvType = view.findViewById(R.id.tvType);
		tvInfo = view.findViewById(R.id.tvInfo);
		tvMark = view.findViewById(R.id.tvMark);
		tvPrompt = view.findViewById(R.id.tvPrompt);
		llContent = view.findViewById(R.id.llContent);
		imageView = view.findViewById(R.id.imageView);
		LinearLayout llYear = view.findViewById(R.id.llYear);
		llYear.setOnClickListener(this);
		LinearLayout llWeek = view.findViewById(R.id.llWeek);
		llWeek.setOnClickListener(this);

		tvCityName.setText("海南省");
		initParameters();
		refresh();
	}

	/**
	 * 初始化接口参数
	 */
	private void initParameters() {
		Calendar calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		int week = calendar.get(Calendar.WEEK_OF_YEAR);
		if (week == 1) {
			if (month == 1) {
				year = year-1;
			}
			week = 52;
		} else {
			week = week-1;
		}
		selectYear = year+"";
		tvYear.setText(selectYear+getString(R.string.year));
		selectWeek = week+"";
		tvWeek.setText(selectWeek+getString(R.string.week));
	}
	
    private void selectDateDialog() {
    	LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = inflater.inflate(R.layout.dialog_select_date, null);
		TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
		LinearLayout llNegative = dialogView.findViewById(R.id.llNegative);
		LinearLayout llPositive = dialogView.findViewById(R.id.llPositive);
		tvMessage.setText("选择查询日期");

		initWheelView(dialogView);
		
		final Dialog dialog = new Dialog(getActivity(), R.style.CustomProgressDialog);
		dialog.setContentView(dialogView);
		dialog.show();
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				setTextViewValue();
			}
		});
		
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
    }

	private void initWheelView(View view) {
		Calendar c = Calendar.getInstance();
		int curYear = c.get(Calendar.YEAR);
		int curMonth = c.get(Calendar.MONTH) + 1;//通过Calendar算出的月数要+1
		int curDate = c.get(Calendar.DATE);
		int curHour = c.get(Calendar.HOUR_OF_DAY);
		int curMinute = c.get(Calendar.MINUTE);

		year = view.findViewById(R.id.year);
		NumericWheelAdapter numericWheelAdapter1=new NumericWheelAdapter(getActivity(),1950, curYear);
		numericWheelAdapter1.setLabel(getString(R.string.year));
		year.setViewAdapter(numericWheelAdapter1);
		year.setCyclic(false);//是否可循环滑动
		year.addScrollingListener(scrollListener);

		month = view.findViewById(R.id.month);
		NumericWheelAdapter numericWheelAdapter2=new NumericWheelAdapter(getActivity(),1, 12, "%02d");
		numericWheelAdapter2.setLabel(getString(R.string.month));
		month.setViewAdapter(numericWheelAdapter2);
		month.setCyclic(false);
		month.addScrollingListener(scrollListener);
		month.setVisibility(View.GONE);

		day = view.findViewById(R.id.day);
		initDay(curYear,curMonth);
		day.setCyclic(false);
		day.setVisibility(View.GONE);

		hour = view.findViewById(R.id.hour);
		NumericWheelAdapter numericWheelAdapter3=new NumericWheelAdapter(getActivity(),1, 23, "%02d");
		numericWheelAdapter3.setLabel(getString(R.string.hour));
		hour.setViewAdapter(numericWheelAdapter3);
		hour.setCyclic(false);
		hour.addScrollingListener(scrollListener);
		hour.setVisibility(View.GONE);

		minute = view.findViewById(R.id.minute);
		NumericWheelAdapter numericWheelAdapter4=new NumericWheelAdapter(getActivity(),1, 59, "%02d");
		numericWheelAdapter4.setLabel(getString(R.string.minute));
		minute.setViewAdapter(numericWheelAdapter4);
		minute.setCyclic(false);
		minute.addScrollingListener(scrollListener);
		minute.setVisibility(View.GONE);

		year.setVisibleItems(7);
		month.setVisibleItems(7);
		day.setVisibleItems(7);
		hour.setVisibleItems(7);
		minute.setVisibleItems(7);

		year.setCurrentItem(curYear - 1950);
		month.setCurrentItem(curMonth - 1);
		day.setCurrentItem(curDate - 1);
		hour.setCurrentItem(curHour - 1);
		minute.setCurrentItem(curMinute);
	}

	private OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
		@Override
		public void onScrollingStarted(WheelView wheel) {
		}
		@Override
		public void onScrollingFinished(WheelView wheel) {
			int n_year = year.getCurrentItem() + 1950;//年
			int n_month = month.getCurrentItem() + 1;//月
			initDay(n_year,n_month);
		}
	};

	/**
	 */
	private void initDay(int arg1, int arg2) {
		NumericWheelAdapter numericWheelAdapter=new NumericWheelAdapter(getActivity(),1, getDay(arg1, arg2), "%02d");
		numericWheelAdapter.setLabel(getString(R.string.day));
		day.setViewAdapter(numericWheelAdapter);
	}

	/**
	 *
	 * @param year
	 * @param month
	 * @return
	 */
	private int getDay(int year, int month) {
		int day;
		boolean flag;
		switch (year % 4) {
			case 0:
				flag = true;
				break;
			default:
				flag = false;
				break;
		}
		switch (month) {
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:
				day = 31;
				break;
			case 2:
				day = flag ? 29 : 28;
				break;
			default:
				day = 30;
				break;
		}
		return day;
	}

	/**
	 */
	private void setTextViewValue() {
		String yearStr = String.valueOf(year.getCurrentItem()+1950);
		String monthStr = String.valueOf((month.getCurrentItem() + 1) < 10 ? "0" + (month.getCurrentItem() + 1) : (month.getCurrentItem() + 1));
		String dayStr = String.valueOf(((day.getCurrentItem()+1) < 10) ? "0" + (day.getCurrentItem()+1) : (day.getCurrentItem()+1));
		String hourStr = String.valueOf(((hour.getCurrentItem()+1) < 10) ? "0" + (hour.getCurrentItem()+1) : (hour.getCurrentItem()+1));
		String minuteStr = String.valueOf(((minute.getCurrentItem()+1) < 10) ? "0" + (minute.getCurrentItem()+1) : (minute.getCurrentItem()+1));
		String time = yearStr+getString(R.string.year)+monthStr+getString(R.string.month)+dayStr+getString(R.string.day)+" "+hourStr+getString(R.string.hour);

		selectYear = yearStr;
		tvYear.setText(selectYear+getString(R.string.year));
	}

	private void selectWeekDialog() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = inflater.inflate(R.layout.dialog_select_string, null);
		TextView tvContent = dialogView.findViewById(R.id.tvContent);
		tvContent.setText("选择查询星期");

		final Dialog dialog = new Dialog(getActivity(), R.style.CustomProgressDialog);
		dialog.setContentView(dialogView);
		dialog.show();

		ListView listView = dialogView.findViewById(R.id.listView);
		DisasterMonitorAdapter mAdapter = new DisasterMonitorAdapter(getActivity(), weeks);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				dialog.dismiss();
				selectWeek = weeks.get(i);
				tvWeek.setText(selectWeek+getString(R.string.week));
			}
		});
	}

	private void okHttpCitys() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String url = "http://59.50.130.88:8888/decision-admin/ny/gezrdz";
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONArray array = new JSONArray(result);
										for (int i = 0; i < array.length(); i++) {
											JSONObject itemObj = array.getJSONObject(i);
											if (!itemObj.isNull("CityName")) {
												String cityName = itemObj.getString("CityName");
												if (!TextUtils.isEmpty(cityName)) {
													citys.add(cityName);
												}
											}
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						});
					}
				});

				OkHttpList();
			}
		}).start();
	}

	private void OkHttpList() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String url = String.format("http://59.50.130.88:8888/decision-admin/ny/zozk?Y=%s&Z=%s&CityName=%s", selectYear, selectWeek, tvCityName.getText().toString());
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								refreshLayout.setRefreshing(false);
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONArray array = new JSONArray(result);
										for (int i = 0; i < array.length(); i++) {
											JSONObject itemObj = array.getJSONObject(i);
											if (!itemObj.isNull("CriterName")) {
												String criterName = itemObj.getString("CriterName");
												if (!TextUtils.isEmpty(criterName)) {
													dataMap.put(criterName, itemObj);
													types.add(criterName);
													if (i == 0) {
														setValue(criterName);
													}
												}
											}
										}
										if (array.length() == 0) {
											llContent.setVisibility(View.GONE);
											tvPrompt.setVisibility(View.VISIBLE);
										}else {
											llContent.setVisibility(View.VISIBLE);
											tvPrompt.setVisibility(View.GONE);
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}

	/**
	 * 灾害种类
	 */
	private void dialogList() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = inflater.inflate(R.layout.dialog_select_string, null);
		TextView tvContent = dialogView.findViewById(R.id.tvContent);
		tvContent.setText("选择灾害种类");

		final Dialog dialog = new Dialog(getActivity(), R.style.CustomProgressDialog);
		dialog.setContentView(dialogView);
		dialog.show();

		ListView listView = dialogView.findViewById(R.id.listView);
		DisasterMonitorAdapter mAdapter = new DisasterMonitorAdapter(getActivity(), types);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				dialog.dismiss();
				String criterName = types.get(i);
				setValue(criterName);
			}
		});
	}

	private void setValue(String criterName) {
		if (!TextUtils.isEmpty(criterName)) {
			tvType.setText(criterName);
			if (dataMap.containsKey(criterName)) {
				JSONObject obj = dataMap.get(criterName);
				if (obj != null) {
					try {
						if (!obj.isNull("CriterionInfo")) {
							tvInfo.setText(obj.getString("CriterionInfo"));
						}
						if (!obj.isNull("Mark")) {
							tvMark.setText(obj.getString("Mark"));
						}
						if (!obj.isNull("Url")) {
							Picasso.get().load(obj.getString("Url")).into(imageView);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 选择市县
	 */
	private void dialogCity() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = inflater.inflate(R.layout.dialog_select_string, null);
		TextView tvContent = dialogView.findViewById(R.id.tvContent);
		tvContent.setText("选择市县");

		final Dialog dialog = new Dialog(getActivity(), R.style.CustomProgressDialog);
		dialog.setContentView(dialogView);
		dialog.show();

		ListView listView = dialogView.findViewById(R.id.listView);
		DisasterMonitorAdapter monitorAdapter = new DisasterMonitorAdapter(getActivity(), citys);
		listView.setAdapter(monitorAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				dialog.dismiss();
				tvCityName.setText(citys.get(i));
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llYear:
			case R.id.tvYear:
				selectDateDialog();
				break;
			case R.id.llWeek:
			case R.id.tvWeek:
				selectWeekDialog();
				break;
			case R.id.llCity:
				dialogCity();
				break;
			case R.id.tvCheck:
				refresh();
				break;
			case R.id.llType:
				dialogList();
				break;

		default:
			break;
		}
	}
	
}
