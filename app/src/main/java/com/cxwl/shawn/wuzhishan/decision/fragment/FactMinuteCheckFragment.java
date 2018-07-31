package com.cxwl.shawn.wuzhishan.decision.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.activity.FactStationListDetailActivity;
import com.cxwl.shawn.wuzhishan.decision.adapter.FactStationListAdapter;
import com.cxwl.shawn.wuzhishan.decision.dto.ShawnRainDto;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;

import net.sourceforge.pinyin4j.PinyinHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 任意时段查询,分钟
 * @author shawn_sun
 *
 */
public class FactMinuteCheckFragment extends Fragment implements OnClickListener {
	
	private LinearLayout llStart, llEnd, ll1, ll2, ll3;
	private ImageView iv1, iv2, iv3;
	private TextView tv1, tv2, tv3;
	private boolean b1 = false, b2 = false, b3 = false;//false为将序，true为升序
	private FactStationListAdapter checkAdapter;
	private List<ShawnRainDto> checkList = new ArrayList<>();
	private TextView tvStartDay, tvStartHour, tvStartMinute, tvEndDay, tvEndHour, tvEndMinute, tvCheck;
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
	private SimpleDateFormat sdf4 = new SimpleDateFormat("HH", Locale.CHINA);
	private SimpleDateFormat sdf5 = new SimpleDateFormat("dd", Locale.CHINA);
	private String startTimeCheck,endTimeCheck,maxDate,minDate,childId="";
	private RelativeLayout reContent;
	private ProgressBar progressBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_fact_check, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
		initCheckListView(view);
	}

	private void initWidget(View view) {
		tvStartDay = view.findViewById(R.id.tvStartDay);
		tvStartDay.setOnClickListener(this);
		tvStartHour = view.findViewById(R.id.tvStartHour);
		tvStartHour.setOnClickListener(this);
		tvStartMinute = view.findViewById(R.id.tvStartMinute);
		tvStartMinute.setOnClickListener(this);
		tvEndDay = view.findViewById(R.id.tvEndDay);
		tvEndDay.setOnClickListener(this);
		tvEndHour = view.findViewById(R.id.tvEndHour);
		tvEndHour.setOnClickListener(this);
		tvEndMinute = view.findViewById(R.id.tvEndMinute);
		tvEndMinute.setOnClickListener(this);
		tvCheck = view.findViewById(R.id.tvCheck);
		tvCheck.setOnClickListener(this);
		reContent = view.findViewById(R.id.reContent);
		llStart = view.findViewById(R.id.llStart);
		llStart.setOnClickListener(this);
		llEnd = view.findViewById(R.id.llEnd);
		llEnd.setOnClickListener(this);
		progressBar = view.findViewById(R.id.progressBar);
		
		ll1 = view.findViewById(R.id.ll1);
		ll1.setOnClickListener(this);
		ll2 = view.findViewById(R.id.ll2);
		ll2.setOnClickListener(this);
		ll3 = view.findViewById(R.id.ll3);
		ll3.setOnClickListener(this);
		iv1 = view.findViewById(R.id.iv1);
		iv2 = view.findViewById(R.id.iv2);
		iv3 = view.findViewById(R.id.iv3);
		tv1 = view.findViewById(R.id.tv1);
		tv2 = view.findViewById(R.id.tv2);
		tv3 = view.findViewById(R.id.tv3);

		childId = getArguments().getString("childId");
		if (TextUtils.isEmpty(childId)) {
			childId = "";
		}
		OkHttpList("http://59.50.130.88:8888/decision-admin/dates/newgetrainfall?city=五指山&start=&end=&cid="+childId, childId);
	}
	
	private void initCheckListView(View view) {
		ListView listViewCheck = view.findViewById(R.id.listViewCheck);
		checkAdapter = new FactStationListAdapter(getActivity(), checkList);
		listViewCheck.setAdapter(checkAdapter);
		listViewCheck.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ShawnRainDto dto = checkList.get(arg2);
				Intent intent = new Intent(getActivity(), FactStationListDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				bundle.putString("childId", childId);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
	private void OkHttpList(final String url, final String childId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
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
										JSONObject obj = new JSONObject(result);
										if (TextUtils.isEmpty(startTimeCheck) && TextUtils.isEmpty(endTimeCheck)) {
											if (!obj.isNull("maxtime")) {
												try {
													maxDate = obj.getString("maxtime");
													minDate = obj.getString("mintime");
													endTimeCheck = obj.getString("maxtime");

													long timeDivider = 1000*60;
													//648降水实况，657最高温，658最低温， 655极大风
													if (TextUtils.equals(childId, "648")) {
														timeDivider = 1000*60;
													}else if (TextUtils.equals(childId, "648") || TextUtils.equals(childId, "657")
															|| TextUtils.equals(childId, "658")  || TextUtils.equals(childId, "655")) {
														timeDivider = 1000*60*5;
													}
													startTimeCheck = sdf3.format(sdf3.parse(endTimeCheck).getTime()-timeDivider);

													String y = startTimeCheck.substring(0, 4);
													String m = startTimeCheck.substring(4, 6);
													String d = startTimeCheck.substring(6, 8);
													String h = startTimeCheck.substring(8, 10);
													String mm = startTimeCheck.substring(10, 12);
													tvStartDay.setText(y+"-"+m+"-"+d);
													tvStartHour.setText(h+"时");
													tvStartMinute.setText(mm+"分");

													String y2 = endTimeCheck.substring(0, 4);
													String m2 = endTimeCheck.substring(4, 6);
													String d2 = endTimeCheck.substring(6, 8);
													String h2 = endTimeCheck.substring(8, 10);
													String mm2 = endTimeCheck.substring(10, 12);
													tvEndDay.setText(y2+"-"+m2+"-"+d2);
													tvEndHour.setText(h2+"时");
													tvEndMinute.setText(mm2+"分");
												} catch (ParseException e) {
													e.printStackTrace();
												}
											}
										}

										if(!obj.isNull("th")) {
											JSONObject th = obj.getJSONObject("th");
											if (!th.isNull("stationName")) {
												tv1.setText(th.getString("stationName"));
											}
											if (!th.isNull("area")) {
												tv2.setText(th.getString("area"));
											}
											if (!th.isNull("val")) {
												tv3.setText(th.getString("val"));
											}
										}

										if (!obj.isNull("list")) {
											checkList.clear();
											JSONArray array = obj.getJSONArray("list");
											if (TextUtils.equals(childId, "658")) {//最低温
												for (int i = array.length()-1; i >= 0; i--) {
													JSONObject itemObj = array.getJSONObject(i);
													ShawnRainDto dto = new ShawnRainDto();
													if (!itemObj.isNull("stationCode")) {
														dto.stationCode = itemObj.getString("stationCode");
													}
													if (!itemObj.isNull("stationName")) {
														dto.stationName = itemObj.getString("stationName");
													}
													if (!itemObj.isNull("area")) {
														dto.area = itemObj.getString("area");
													}
													if (!itemObj.isNull("val")) {
														dto.val = itemObj.getDouble("val");
													}
													if (!TextUtils.isEmpty(dto.area)) {
														checkList.add(dto);
													}
												}
											}else {
												for (int i = 0; i < array.length(); i++) {
													JSONObject itemObj = array.getJSONObject(i);
													ShawnRainDto dto = new ShawnRainDto();
													if (!itemObj.isNull("stationCode")) {
														dto.stationCode = itemObj.getString("stationCode");
													}
													if (!itemObj.isNull("stationName")) {
														dto.stationName = itemObj.getString("stationName");
													}
													if (!itemObj.isNull("area")) {
														dto.area = itemObj.getString("area");
													}
													if (!itemObj.isNull("val")) {
														dto.val = itemObj.getDouble("val");
													}
													if (!TextUtils.isEmpty(dto.area)) {
														checkList.add(dto);
													}
												}
											}
											if (checkAdapter != null) {
												checkAdapter.notifyDataSetChanged();
											}
										}

										if (TextUtils.equals(childId, "658")) {//最低温
											if (!b3) {//将序
												iv3.setImageResource(R.drawable.arrow_up);
											}else {//将序
												iv3.setImageResource(R.drawable.arrow_down);
											}
										}else {
											if (!b3) {//将序
												iv3.setImageResource(R.drawable.arrow_down);
											}else {//将序
												iv3.setImageResource(R.drawable.arrow_up);
											}
										}
										iv3.setVisibility(View.VISIBLE);

										progressBar.setVisibility(View.GONE);
										reContent.setVisibility(View.VISIBLE);
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
	
	// 返回中文的首字母
    public static String getPinYinHeadChar(String str) {
        String convert = "";
        int size = str.length();
        if (size >= 2) {
        	size = 2;
		}
        for (int j = 0; j < size; j++) {  
            char word = str.charAt(j);  
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
            if (pinyinArray != null) {  
                convert += pinyinArray[0].charAt(0);  
            } else {  
                convert += word;  
            }  
        }  
        return convert;  
    }  
    
    private void selectStartDateTimeDialog(String message) {
    	LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_fact_check_time, null);
		TextView tvMessage = view.findViewById(R.id.tvMessage);
		LinearLayout llNegative = view.findViewById(R.id.llNegative);
		LinearLayout llPositive = view.findViewById(R.id.llPositive);
		final DatePicker datePickr = view.findViewById(R.id.datePickr);
		final TimePicker timePicker = view.findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);
		
		try {
			datePickr.setMinDate(sdf3.parse(minDate).getTime());
			datePickr.setMaxDate(sdf3.parse(maxDate).getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		try {
			String y,m,d,h,mm;
			if (!TextUtils.isEmpty(startTimeCheck)) {
				y = startTimeCheck.substring(0, 4);
				m = startTimeCheck.substring(4, 6);
				d = startTimeCheck.substring(6, 8);
				h = startTimeCheck.substring(8, 10);
				mm = startTimeCheck.substring(10, 12);

				final int minDay = Integer.valueOf(sdf5.format(sdf3.parse(minDate)));
				final int maxDay = Integer.valueOf(sdf5.format(sdf3.parse(maxDate)));
				final int minHour = Integer.valueOf(sdf4.format(sdf3.parse(minDate)));
				final int maxHour = Integer.valueOf(sdf4.format(sdf3.parse(maxDate)));

				datePickr.init(Integer.valueOf(y), Integer.valueOf(m) - 1, Integer.valueOf(d), new DatePicker.OnDateChangedListener() {
					@Override
					public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//						startCurrentDay = dayOfMonth;
//						if (startCurrentDay == minDay) {
//							if (timePicker.getCurrentHour() < minHour) {
//								timePicker.setCurrentHour(minHour);
//							}
//							if (timePicker.getCurrentHour() == minHour) {
//								timePicker.setCurrentMinute(0);
//							}
//						}else if (startCurrentDay == maxDay) {
//							if (timePicker.getCurrentHour() > maxHour) {
//								timePicker.setCurrentHour(maxHour);
//							}
//							if (timePicker.getCurrentHour() == maxHour) {
//								timePicker.setCurrentMinute(0);
//							}
//						}
					}
				});
				timePicker.setCurrentHour(Integer.valueOf(h));
				timePicker.setCurrentMinute(Integer.valueOf(mm));

				timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
					@Override
					public void onTimeChanged(TimePicker arg0, int arg1, int arg2) {
//						if (startCurrentDay == minDay) {
//							if (arg0.getCurrentHour() < minHour) {
//								arg0.setCurrentHour(minHour);
//							}
//						}else if (startCurrentDay == maxDay) {
//							if (arg0.getCurrentHour() > maxHour) {
//								arg0.setCurrentHour(maxHour);
//							}
//							if (arg0.getCurrentHour() == maxHour) {
//								arg0.setCurrentMinute(0);
//							}
//						}
					}
				});
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		final Dialog dialog = new Dialog(getActivity(), R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText(message);
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				
				int year = datePickr.getYear();
				int month = datePickr.getMonth()+1;
				int day = datePickr.getDayOfMonth();
				int hour = timePicker.getCurrentHour();
				int minute = timePicker.getCurrentMinute();
				
				String yearStr = year+"";
				String monthStr = month+"";
				if (month < 10) {
					monthStr = "0"+monthStr;
				}
				String dayStr = day+"";
				if (day < 10) {
					dayStr = "0"+dayStr;
				}
				String hourStr = hour+"";
				if (hour < 10) {
					hourStr = "0"+hourStr;
				}
				String minuteStr = minute+"";
				if (minute < 10) {
					minuteStr = "0"+minuteStr;
				}
				
				startTimeCheck = yearStr+monthStr+dayStr+hourStr+minuteStr;
				tvStartDay.setText(yearStr+"-"+monthStr+"-"+dayStr);
				tvStartHour.setText(hourStr+"时");
				tvStartMinute.setText(minuteStr+"分");
			}
		});
		
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
    }
    
    private void selectEndDateTimeDialog(String message) {
    	LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_fact_check_time, null);
		TextView tvMessage = view.findViewById(R.id.tvMessage);
		LinearLayout llNegative = view.findViewById(R.id.llNegative);
		LinearLayout llPositive = view.findViewById(R.id.llPositive);
		final DatePicker datePickr = view.findViewById(R.id.datePickr);
		final TimePicker timePicker = view.findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);
		
		try {
			datePickr.setMinDate(sdf3.parse(minDate).getTime());
			datePickr.setMaxDate(sdf3.parse(maxDate).getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		try {
			String y,m,d,h,mm;
			if (!TextUtils.isEmpty(endTimeCheck)) {
				y = endTimeCheck.substring(0, 4);
				m = endTimeCheck.substring(4, 6);
				d = endTimeCheck.substring(6, 8);
				h = endTimeCheck.substring(8, 10);
				mm = endTimeCheck.substring(10, 12);

				final int minDay = Integer.valueOf(sdf5.format(sdf3.parse(minDate)));
				final int maxDay = Integer.valueOf(sdf5.format(sdf3.parse(maxDate)));
				final int minHour = Integer.valueOf(sdf4.format(sdf3.parse(minDate)));
				final int maxHour = Integer.valueOf(sdf4.format(sdf3.parse(maxDate)));

				datePickr.init(Integer.valueOf(y), Integer.valueOf(m) - 1, Integer.valueOf(d), new DatePicker.OnDateChangedListener() {
					@Override
					public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//						endCurrentDay = dayOfMonth;
//						if (endCurrentDay == minDay) {
//							if (timePicker.getCurrentHour() < minHour) {
//								timePicker.setCurrentHour(minHour);
//							}
//						}else if (endCurrentDay == maxDay) {
//							if (timePicker.getCurrentHour() > maxHour) {
//								timePicker.setCurrentHour(maxHour);
//							}
//							if (timePicker.getCurrentHour() == maxHour) {
//								timePicker.setCurrentMinute(0);
//							}
//						}
					}
				});
				timePicker.setCurrentHour(Integer.valueOf(h));
				timePicker.setCurrentMinute(Integer.valueOf(mm));

				timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
					@Override
					public void onTimeChanged(TimePicker arg0, int arg1, int arg2) {
//						if (endCurrentDay == minDay) {
//							if (arg0.getCurrentHour() < minHour) {
//								arg0.setCurrentHour(minHour);
//							}
//							if (arg0.getCurrentHour() == minHour) {
//								arg0.setCurrentMinute(0);
//							}
//						}else if (endCurrentDay == maxDay) {
//							if (arg0.getCurrentHour() > maxHour) {
//								arg0.setCurrentHour(maxHour);
//							}
//							if (arg0.getCurrentHour() == maxHour) {
//								arg0.setCurrentMinute(0);
//							}
//						}
					}
				});
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}


		final Dialog dialog = new Dialog(getActivity(), R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText(message);
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				
				int year = datePickr.getYear();
				int month = datePickr.getMonth()+1;
				int day = datePickr.getDayOfMonth();
				int hour = timePicker.getCurrentHour();
				int minute = timePicker.getCurrentMinute();
				
				String yearStr = year+"";
				String monthStr = month+"";
				if (month < 10) {
					monthStr = "0"+monthStr;
				}
				String dayStr = day+"";
				if (day < 10) {
					dayStr = "0"+dayStr;
				}
				String hourStr = hour+"";
				if (hour < 10) {
					hourStr = "0"+hourStr;
				}
				String minuteStr = minute+"";
				if (minute < 10) {
					minuteStr = "0"+minuteStr;
				}
				
				endTimeCheck = yearStr+monthStr+dayStr+hourStr+minuteStr;
				tvEndDay.setText(yearStr+"-"+monthStr+"-"+dayStr);
				tvEndHour.setText(hourStr+"时");
				tvEndMinute.setText(minuteStr+"分");
			}
		});
		
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvStartDay:
		case R.id.tvStartHour:
		case R.id.tvStartMinute:
		case R.id.llStart:
			selectStartDateTimeDialog("选择开始时间");
			break;
		case R.id.tvEndDay:
		case R.id.tvEndHour:
		case R.id.tvEndMinute:
		case R.id.llEnd:
			selectEndDateTimeDialog("选择结束时间");
			break;
		case R.id.tvCheck:
			if (TextUtils.isEmpty(startTimeCheck)) {
				Toast.makeText(getActivity(), "请选择开始时间", Toast.LENGTH_SHORT).show();
				return;
			}
			if (TextUtils.isEmpty(endTimeCheck)) {
				Toast.makeText(getActivity(), "请选择结束时间", Toast.LENGTH_SHORT).show();
				return;
			}
			if (Long.valueOf(startTimeCheck) >= Long.valueOf(endTimeCheck)) {
				Toast.makeText(getActivity(), "开始时间不能大于或等于结束时间", Toast.LENGTH_SHORT).show();
				return;
			}
			progressBar.setVisibility(View.VISIBLE);
			String url = String.format("http://59.50.130.88:8888/decision-admin/dates/newgetrainfall?city=五指山&start=%s&end=%s&cid=%s", startTimeCheck,endTimeCheck,childId);
			OkHttpList(url, childId);
			break;
		case R.id.ll1:
			if (b1) {//升序
				b1 = false;
				iv1.setImageResource(R.drawable.arrow_up);
				iv1.setVisibility(View.VISIBLE);
				iv2.setVisibility(View.INVISIBLE);
				iv3.setVisibility(View.INVISIBLE);
				Collections.sort(checkList, new Comparator<ShawnRainDto>() {
					@Override
					public int compare(ShawnRainDto arg0, ShawnRainDto arg1) {
						if (TextUtils.isEmpty(arg0.stationName) || TextUtils.isEmpty(arg1.stationName)) {
							return 0;
						}else {
							return getPinYinHeadChar(arg0.stationName).compareTo(getPinYinHeadChar(arg1.stationName));
						}
					}
				});
			}else {//将序
				b1 = true;
				iv1.setImageResource(R.drawable.arrow_down);
				iv1.setVisibility(View.VISIBLE);
				iv2.setVisibility(View.INVISIBLE);
				iv3.setVisibility(View.INVISIBLE);
				Collections.sort(checkList, new Comparator<ShawnRainDto>() {
					@Override
					public int compare(ShawnRainDto arg0, ShawnRainDto arg1) {
						if (TextUtils.isEmpty(arg0.stationName) || TextUtils.isEmpty(arg1.stationName)) {
							return -1;
						}else {
							return getPinYinHeadChar(arg1.stationName).compareTo(getPinYinHeadChar(arg0.stationName));
						}
					}
				});
			}
			if (checkAdapter != null) {
				checkAdapter.notifyDataSetChanged();
			}
			break;
		case R.id.ll2:
			if (b2) {//升序
				b2 = false;
				iv2.setImageResource(R.drawable.arrow_up);
				iv1.setVisibility(View.INVISIBLE);
				iv2.setVisibility(View.VISIBLE);
				iv3.setVisibility(View.INVISIBLE);
				Collections.sort(checkList, new Comparator<ShawnRainDto>() {
					@Override
					public int compare(ShawnRainDto arg0, ShawnRainDto arg1) {
						if (TextUtils.isEmpty(arg0.area) || TextUtils.isEmpty(arg1.area)) {
							return 0;
						}else {
							return getPinYinHeadChar(arg0.area).compareTo(getPinYinHeadChar(arg1.area));
						}
					}
				});
			}else {//将序
				b2 = true;
				iv2.setImageResource(R.drawable.arrow_down);
				iv1.setVisibility(View.INVISIBLE);
				iv2.setVisibility(View.VISIBLE);
				iv3.setVisibility(View.INVISIBLE);
				Collections.sort(checkList, new Comparator<ShawnRainDto>() {
					@Override
					public int compare(ShawnRainDto arg0, ShawnRainDto arg1) {
						if (TextUtils.isEmpty(arg0.area) || TextUtils.isEmpty(arg1.area)) {
							return -1;
						}else {
							return getPinYinHeadChar(arg1.area).compareTo(getPinYinHeadChar(arg0.area));
						}
					}
				});
			}
			if (checkAdapter != null) {
				checkAdapter.notifyDataSetChanged();
			}
			break;
		case R.id.ll3:
			if (b3) {//升序
				b3 = false;
				iv3.setImageResource(R.drawable.arrow_up);
				iv1.setVisibility(View.INVISIBLE);
				iv2.setVisibility(View.INVISIBLE);
				iv3.setVisibility(View.VISIBLE);
				Collections.sort(checkList, new Comparator<ShawnRainDto>() {
					@Override
					public int compare(ShawnRainDto arg0, ShawnRainDto arg1) {
						return Double.valueOf(arg0.val).compareTo(Double.valueOf(arg1.val));
					}
				});
			}else {//将序
				b3 = true;
				iv3.setImageResource(R.drawable.arrow_down);
				iv1.setVisibility(View.INVISIBLE);
				iv2.setVisibility(View.INVISIBLE);
				iv3.setVisibility(View.VISIBLE);
				Collections.sort(checkList, new Comparator<ShawnRainDto>() {
					@Override
					public int compare(ShawnRainDto arg0, ShawnRainDto arg1) {
						return Double.valueOf(arg1.val).compareTo(Double.valueOf(arg0.val));
					}
				});
			}
			if (checkAdapter != null) {
				checkAdapter.notifyDataSetChanged();
			}
			break;

		default:
			break;
		}
	}
	
}
