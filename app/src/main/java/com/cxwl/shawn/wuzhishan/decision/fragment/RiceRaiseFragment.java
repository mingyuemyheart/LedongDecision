package com.cxwl.shawn.wuzhishan.decision.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.adapter.DisasterMonitorAdapter;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.dto.RiceRaiseDto;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import wheelview.NumericWheelAdapter;
import wheelview.OnWheelScrollListener;
import wheelview.WheelView;

/**
 * 生态气象-水稻长势
 * @author shawn_sun
 */
public class RiceRaiseFragment extends Fragment implements OnClickListener {
	
	private TextView tvStartDate,tvStartHour,tvStartMinute,tvEndDate,tvEndHour,tvEndMinute,tvStationName,tvPrompt;
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
	private String startDate,endDate, stationName;
	private WheelView year,month,day,hour,minute;
	private List<String> stationNames = new ArrayList<>();
	private List<RiceRaiseDto> dataList = new ArrayList<>();
	private SwipeRefreshLayout refreshLayout;//下拉刷新布局

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_rice_raise, null);
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
		dataList.clear();
		OkHttpList();
	}

	private void initWidget(View view) {
		LinearLayout llStartDate = view.findViewById(R.id.llStartDate);
		llStartDate.setOnClickListener(this);
		LinearLayout llEndDate = view.findViewById(R.id.llEndDate);
		llEndDate.setOnClickListener(this);
		LinearLayout llStationName = view.findViewById(R.id.llStationName);
		llStationName.setOnClickListener(this);
		tvStartDate = view.findViewById(R.id.tvStartDate);
		tvStartHour = view.findViewById(R.id.tvStartHour);
		tvStartMinute = view.findViewById(R.id.tvStartMinute);
		tvEndDate = view.findViewById(R.id.tvEndDate);
		tvEndHour = view.findViewById(R.id.tvEndHour);
		tvEndMinute = view.findViewById(R.id.tvEndMinute);
		tvStationName = view.findViewById(R.id.tvStationName);
		TextView tvCheck = view.findViewById(R.id.tvCheck);
		tvCheck.setOnClickListener(this);
		tvPrompt = view.findViewById(R.id.tvPrompt);

		startDate = sdf2.format(new Date().getTime()-1000*60*60*24);
		tvStartDate.setText(startDate.substring(0, 4)+"-"+startDate.substring(4, 6)+"-"+startDate.substring(6, 8));
		tvStartHour.setText(startDate.substring(8, 10)+getString(R.string.hour));
		tvStartMinute.setText(startDate.substring(10, 12)+getString(R.string.minute));
		endDate = sdf2.format(new Date());
		tvEndDate.setText(endDate.substring(0, 4)+"-"+endDate.substring(4, 6)+"-"+endDate.substring(6, 8));
		tvEndHour.setText(endDate.substring(8, 10)+getString(R.string.hour));
		tvEndMinute.setText(endDate.substring(10, 12)+getString(R.string.minute));

		OkHttpStations();
	}

	/**
	 * 获取站点信息
	 */
	private void OkHttpStations() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String url = "http://59.50.130.88:8888/decision-admin/ny/getzd";
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(@NotNull Call call, @NotNull IOException e) {
					}
					@Override
					public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
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
										stationNames.clear();
										for (int i = 0; i < array.length(); i++) {
											JSONObject itemObj = array.getJSONObject(i);
											if (!itemObj.isNull("C_CorpDev")) {
												stationNames.add(itemObj.getString("C_CorpDev"));
											}
										}
										if (stationNames.size() > 0) {
											stationName = stationNames.get(0);
											tvStationName.setText(stationName);
											refresh();
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

	private void selectStationDialog() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = inflater.inflate(R.layout.dialog_select_string, null);
		TextView tvContent = dialogView.findViewById(R.id.tvContent);
		tvContent.setText("选择站点");

		final Dialog dialog = new Dialog(getActivity(), R.style.CustomProgressDialog);
		dialog.setContentView(dialogView);
		dialog.show();

		ListView listView = dialogView.findViewById(R.id.listView);
		DisasterMonitorAdapter mAdapter = new DisasterMonitorAdapter(getActivity(), stationNames);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				dialog.dismiss();
				stationName = stationNames.get(i);
				tvStationName.setText(stationName);
			}
		});
	}

	private void selectDateDialog(final boolean isStartDate) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = inflater.inflate(R.layout.dialog_select_date, null);
		TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
		LinearLayout llNegative = dialogView.findViewById(R.id.llNegative);
		LinearLayout llPositive = dialogView.findViewById(R.id.llPositive);
		if (isStartDate) {
			tvMessage.setText("选择开始时间");
		}else {
			tvMessage.setText("选择结束时间");
		}

		initWheelView(dialogView);

		final Dialog dialog = new Dialog(getActivity(), R.style.CustomProgressDialog);
		dialog.setContentView(dialogView);
		dialog.show();

		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				setTextViewValue(isStartDate);
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

		day = view.findViewById(R.id.day);
		initDay(curYear,curMonth);
		day.setCyclic(false);

		hour = view.findViewById(R.id.hour);
		NumericWheelAdapter numericWheelAdapter3=new NumericWheelAdapter(getActivity(),1, 23, "%02d");
		numericWheelAdapter3.setLabel(getString(R.string.hour));
		hour.setViewAdapter(numericWheelAdapter3);
		hour.setCyclic(false);
		hour.addScrollingListener(scrollListener);

		minute = view.findViewById(R.id.minute);
		NumericWheelAdapter numericWheelAdapter4=new NumericWheelAdapter(getActivity(),1, 59, "%02d");
		numericWheelAdapter4.setLabel(getString(R.string.minute));
		minute.setViewAdapter(numericWheelAdapter4);
		minute.setCyclic(false);
		minute.addScrollingListener(scrollListener);

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
	private void setTextViewValue(final boolean isStartDate) {
		String yearStr = String.valueOf(year.getCurrentItem()+1950);
		String monthStr = String.valueOf((month.getCurrentItem() + 1) < 10 ? "0" + (month.getCurrentItem() + 1) : (month.getCurrentItem() + 1));
		String dayStr = String.valueOf(((day.getCurrentItem()+1) < 10) ? "0" + (day.getCurrentItem()+1) : (day.getCurrentItem()+1));
		String hourStr = String.valueOf(((hour.getCurrentItem()+1) < 10) ? "0" + (hour.getCurrentItem()+1) : (hour.getCurrentItem()+1));
		String minuteStr = String.valueOf(((minute.getCurrentItem()+1) < 10) ? "0" + (minute.getCurrentItem()+1) : (minute.getCurrentItem()+1));

		if (isStartDate) {
			startDate = yearStr+monthStr+dayStr+hourStr+minuteStr;
			tvStartDate.setText(yearStr+"-"+monthStr+"-"+dayStr);
			tvStartHour.setText(startDate.substring(8, 10)+getString(R.string.hour));
			tvStartMinute.setText(startDate.substring(10, 12)+getString(R.string.minute));
		}else {
			endDate = yearStr+monthStr+dayStr+hourStr+minuteStr;
			tvEndDate.setText(yearStr+"-"+monthStr+"-"+dayStr);
			tvEndHour.setText(endDate.substring(8, 10)+getString(R.string.hour));
			tvEndMinute.setText(endDate.substring(10, 12)+getString(R.string.minute));
		}
	}

	private void OkHttpList() {
		try {
			long start = sdf2.parse(startDate).getTime();
			long end = sdf2.parse(endDate).getTime();
			if (start >= end) {
				Toast.makeText(getActivity(), "结束时间不能小于开始时间", Toast.LENGTH_SHORT).show();
				return;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (TextUtils.isEmpty(stationName)) {
			Toast.makeText(getActivity(), "请选择站点", Toast.LENGTH_SHORT).show();
			return;
		}


		if (!refreshLayout.isRefreshing()) {
			refreshLayout.setRefreshing(true);
		}
		refreshLayout.setRefreshing(false);

		new Thread(new Runnable() {
			@Override
			public void run() {
				final String url = String.format("http://59.50.130.88:8888/decision-admin/ny/sdzs?ST=%s&ET=%s&DC=%s", startDate, endDate, stationName);
				Log.e("url", url);
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
//								if (!TextUtils.isEmpty(result)) {
//									try {
//										JSONArray array = new JSONArray(result);
//										for (int i = 0; i < array.length(); i++) {
//											JSONObject itemObj = array.getJSONObject(i);
//											if (!itemObj.isNull("CriterName")) {
//												String criterName = itemObj.getString("CriterName");
//												if (!TextUtils.isEmpty(criterName)) {
//													dataMap.put(criterName, itemObj);
//													types.add(criterName);
//													if (i == 0) {
//														setValue(criterName);
//													}
//												}
//											}
//										}
//										if (array.length() == 0) {
//											llContent.setVisibility(View.GONE);
//											tvPrompt.setVisibility(View.VISIBLE);
//										}else {
//											llContent.setVisibility(View.VISIBLE);
//											tvPrompt.setVisibility(View.GONE);
//										}
//									} catch (JSONException e) {
//										e.printStackTrace();
//									}
//								}

								refreshLayout.setRefreshing(false);
							}
						});
					}
				});
			}
		}).start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llStartDate:
				selectDateDialog(true);
				break;
			case R.id.llEndDate:
				selectDateDialog(false);
				break;
			case R.id.llStationName:
				selectStationDialog();
				break;
			case R.id.tvCheck:
				refresh();
				break;

		default:
			break;
		}
	}
	
}
