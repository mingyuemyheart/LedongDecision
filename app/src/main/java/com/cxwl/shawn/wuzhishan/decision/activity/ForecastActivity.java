package com.cxwl.shawn.wuzhishan.decision.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.adapter.WeeklyForecastAdapter;
import com.cxwl.shawn.wuzhishan.decision.dto.WeatherDto;
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil;
import com.cxwl.shawn.wuzhishan.decision.util.FetchWeather;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;
import com.cxwl.shawn.wuzhishan.decision.util.WeatherUtil;
import com.cxwl.shawn.wuzhishan.decision.view.CubicView;
import com.cxwl.shawn.wuzhishan.decision.view.ScrollviewListview;
import com.cxwl.shawn.wuzhishan.decision.view.WeeklyView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 天气预报
 * @author shawn_sun
 *
 */
public class ForecastActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext;
	private LinearLayout llBack,llFactButton,llContainer1, llContainer2;
	private TextView tvTitle,tvLocation,tvTime,tvPhe,tvTemperature,tvWind,tvHumidity,tvAqi,tvPressure,tvFact1,tvFact2;
	private ImageView ivPhe,ivList;//天气显现对应的图标
	private ScrollView scrollView = null;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmm");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd");
	private int width = 0;
	private ScrollviewListview mListView;//一周预报列表listview
	private WeeklyForecastAdapter mAdapter;
	private List<WeatherDto> weeklyList = new ArrayList<>();
	private HorizontalScrollView hScrollView2;
	private double lat = 0, lng = 0;
	private String l7, l5, l1, l4, l3, l2, l10;//基本站
	private String nl7, nl5, nl1, nl4, nl3, nl2, nl10;//最近站

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forecast);
		mContext = this;
		showDialog();
		initWidget();
		initListView();
	}

	private void initWidget() {
		llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("天气详情");
		tvLocation = findViewById(R.id.tvLocation);
		tvTime = findViewById(R.id.tvTime);
		tvTime.setFocusable(true);
		tvTime.setFocusableInTouchMode(true);
		tvTime.requestFocus();
		tvTemperature = findViewById(R.id.tvTemperature);
		tvWind = findViewById(R.id.tvWind);
		tvHumidity = findViewById(R.id.tvHumidity);
		tvAqi = findViewById(R.id.tvAqi);
		ivPhe = findViewById(R.id.ivPhe);
		tvPhe = findViewById(R.id.tvPhe);
		tvPressure = findViewById(R.id.tvPressure);
		ivList = findViewById(R.id.ivList);
		ivList.setOnClickListener(this);
		scrollView = findViewById(R.id.scrollView);
		llContainer1 = findViewById(R.id.llContainer1);
		llContainer2 = findViewById(R.id.llContainer2);
		hScrollView2 = findViewById(R.id.hScrollView2);
		llFactButton = findViewById(R.id.llFactButton);
		tvFact1 = findViewById(R.id.tvFact1);
		tvFact1.setOnClickListener(this);
		tvFact2 = findViewById(R.id.tvFact2);
		tvFact2.setOnClickListener(this);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;

		String cityName = getIntent().getExtras().getString("cityName");
		if (!TextUtils.isEmpty(cityName)) {
			tvLocation.setText(cityName);
		}
		lat = getIntent().getExtras().getDouble("lat", 0);
		lng = getIntent().getExtras().getDouble("lng", 0);
		String cityId = getIntent().getExtras().getString("cityId");
		if (!TextUtils.isEmpty(cityId)) {
			OkHttpWeatherInfo(cityId);
		}
	}

	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = findViewById(R.id.listView);
		mAdapter = new WeeklyForecastAdapter(mContext, weeklyList);
		mListView.setAdapter(mAdapter);
	}
	
	/**
	 * 获取天气数据
	 */
	private void OkHttpWeatherInfo(final String cityId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final String url;
				if (cityId.startsWith("10131")) {
					llFactButton.setVisibility(View.VISIBLE);
					if (lat != 0 && lng != 0) {
						url = "http://data-fusion.tianqi.cn/datafusion/test?type=HN&ID="+cityId+"&lonlat="+lng+","+lat;
					}else {
						url = "http://data-fusion.tianqi.cn/datafusion/test?type=HN&ID="+cityId;
					}
				}else {
					llFactButton.setVisibility(View.GONE);
					url = FetchWeather.weather2Url(cityId, "all");
				}

				new Thread(new Runnable() {
					@Override
					public void run() {
						OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
							@Override
							public void onFailure(Call call, IOException e) {
								if (url.startsWith("http://data-fusion.tianqi.cn/datafusion/")) {
									OkHttpWeatherInfo(FetchWeather.weather2Url(cityId, "all"));
								}
							}

							@Override
							public void onResponse(Call call, Response response) throws IOException {
								if (!response.isSuccessful()) {
									return;
								}
								final String result = response.body().string();
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (!TextUtils.isEmpty(result)) {
											try {
												JSONArray array = new JSONArray(result);

												JSONObject fact = array.getJSONObject(0);
												if (!fact.isNull("l")) {
													JSONObject object = fact.getJSONObject("l");
													//实况信息
													if (!object.isNull("l7")) {
														l7 = object.getString("l7");
													}
													if (!object.isNull("l5")) {
														l5 = object.getString("l5");
													}
													if (!object.isNull("l1")) {
														l1 = object.getString("l1");
													}

													if (!object.isNull("l4")) {
														l4 = object.getString("l4");
														if (!object.isNull("l3")) {
															l3 = object.getString("l3");
														}
													}

													if (!object.isNull("l2")) {
														l2 = object.getString("l2");
													}

													if (!object.isNull("l10")) {
														l10 = object.getString("l10");
													}
												}

												if (array.length() > 6) {
													JSONObject nfact = array.getJSONObject(6);
													if (!nfact.isNull("nl")) {
														JSONObject object = nfact.getJSONObject("nl");

														//实况信息
														if (!object.isNull("l7")) {
															nl7 = object.getString("l7");
														}
														if (!object.isNull("l5")) {
															nl5 = object.getString("l5");
														}
														if (!object.isNull("l1")) {
															nl1 = object.getString("l1");
														}

														if (!object.isNull("l4")) {
															nl4 = object.getString("l4");
															if (!object.isNull("l3")) {
																nl3 = object.getString("l3");
															}
														}

														if (!object.isNull("l2")) {
															nl2 = object.getString("l2");
														}

														if (!object.isNull("l10")) {
															nl10 = object.getString("l10");
														}
													}
												}

												switchHNFactData(true);

												//城市信息
												JSONObject city = array.getJSONObject(1);
												if (!city.isNull("f")) {
													JSONObject fObj = city.getJSONObject("f");

													String f0 = sdf3.format(sdf2.parse(fObj.getString("f0")));
													long time = sdf3.parse(f0).getTime();
													long currentDate = sdf3.parse(sdf3.format(new Date())).getTime();

													if (!fObj.isNull("f1")) {
														weeklyList.clear();
														String currentTime = sdf1.format(new Date().getTime());
														int hour = Integer.valueOf(currentTime);
														JSONArray f1 = fObj.getJSONArray("f1");
														for (int i = 0; i < f1.length(); i++) {
															WeatherDto dto = new WeatherDto();
															JSONObject weeklyObj = f1.getJSONObject(i);
															//晚上
															dto.lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
															dto.lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))));
															dto.lowTemp = Integer.valueOf(weeklyObj.getString("fd"));

															//白天
															dto.highPheCode = Integer.valueOf(weeklyObj.getString("fa"));
															dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))));
															dto.highTemp = Integer.valueOf(weeklyObj.getString("fc"));

															if (hour >= 6 && hour <= 18) {
																dto.windDir = Integer.valueOf(weeklyObj.getString("fe"));
																dto.windForce = Integer.valueOf(weeklyObj.getString("fg"));
																if (hour >= 17 || hour <= 7) {
																	if (i <= 6) {
																		dto.windForceString = dto.windForce+"级";
																	}else {
																		dto.windForceString = WeatherUtil.getDayWindForce(Integer.valueOf(dto.windForce));
																	}
																}else {
																	if (i <= 2) {
																		dto.windForceString = dto.windForce+"级";
																	}else {
																		dto.windForceString = WeatherUtil.getDayWindForce(Integer.valueOf(dto.windForce));
																	}
																}
															}else {
																dto.windDir = Integer.valueOf(weeklyObj.getString("ff"));
																dto.windForce = Integer.valueOf(weeklyObj.getString("fh"));
																if (hour >= 17 || hour <= 7) {
																	if (i <= 6) {
																		dto.windForceString = dto.windForce+"级";
																	}else {
																		dto.windForceString = WeatherUtil.getDayWindForce(Integer.valueOf(dto.windForce));
																	}
																}else {
																	if (i <= 2) {
																		dto.windForceString = dto.windForce+"级";
																	}else {
																		dto.windForceString = WeatherUtil.getDayWindForce(Integer.valueOf(dto.windForce));
																	}
																}
															}

															dto.date = sdf3.format(new Date(time+1000*60*60*24*i));//日期
															if (currentDate > time) {
																dto.week = CommonUtil.getWeek(mContext, i-1);//星期几
															}else {
																dto.week = CommonUtil.getWeek(mContext, i);//星期几
															}

															weeklyList.add(dto);
														}

														if (weeklyList.size() > 0 && mAdapter != null) {
															mAdapter.foreTime = time;
															mAdapter.currentTime = currentDate;
															mAdapter.notifyDataSetChanged();

															//一周预报曲线
															WeeklyView weeklyView = new WeeklyView(mContext);
															weeklyView.setData(weeklyList, time, currentDate);
															llContainer2.removeAllViews();
															llContainer2.addView(weeklyView, width*2, (int)(CommonUtil.dip2px(mContext, 360)));
														}

													}
												}

												//空气质量
												if (!array.isNull(4)) {
													JSONObject aqiObj = array.getJSONObject(4);
													if (!aqiObj.isNull("p")) {
														JSONObject itemObj = aqiObj.getJSONObject("p");
														if (!itemObj.isNull("p2")) {
															String aqi = itemObj.getString("p2");
															tvAqi.setText("AQI" + " "+ WeatherUtil.getAqi(mContext, Integer.valueOf(aqi)) + " " + aqi);
														}
													}
												}

												//逐小时预报信息
												JSONObject hour = array.getJSONObject(3);
												if (!hour.isNull("jh")) {
													List<WeatherDto> hourlyList = new ArrayList<>();
													JSONArray jhArray = hour.getJSONArray("jh");
													for (int i = 0; i < jhArray.length(); i++) {
														JSONObject itemObj = jhArray.getJSONObject(i);
														WeatherDto dto = new WeatherDto();
														dto.hourlyCode = Integer.valueOf(itemObj.getString("ja"));
														dto.hourlyTemp = Float.parseFloat(itemObj.getString("jb"));
														dto.hourlyTime = itemObj.getString("jf");
														dto.hourlyWindDirCode = Integer.valueOf(itemObj.getString("jc"));
														dto.hourlyWindForceCode = Integer.valueOf(itemObj.getString("jd"));
														hourlyList.add(dto);
													}
													//逐小时预报信息
													CubicView cubicView = new CubicView(mContext);
													cubicView.setData(hourlyList);
													llContainer1.removeAllViews();
													llContainer1.addView(cubicView, width*2, (int)(CommonUtil.dip2px(mContext, 300)));
												}

												//海南逐小时预报信息
												if (array.length() > 5) {
													JSONObject hnHour = array.getJSONObject(5);
													if (!hnHour.isNull("njh")) {
														List<WeatherDto> hourlyList = new ArrayList<>();
														JSONArray jhArray = hnHour.getJSONArray("njh");
														for (int i = 0; i < jhArray.length(); i++) {
															JSONObject itemObj = jhArray.getJSONObject(i);
															WeatherDto dto = new WeatherDto();
															dto.hourlyCode = Integer.valueOf(itemObj.getString("ja"));
															dto.hourlyTemp = Float.parseFloat(itemObj.getString("jb"));
															dto.hourlyTime = itemObj.getString("jf");
															dto.hourlyWindDirCode = Integer.valueOf(itemObj.getString("jc"));
															dto.hourlyWindForceCode = Integer.valueOf(itemObj.getString("jd"));
															hourlyList.add(dto);
														}
														//逐小时预报信息
														CubicView cubicView = new CubicView(mContext);
														cubicView.setData(hourlyList);
														llContainer1.removeAllViews();
														llContainer1.addView(cubicView, width*2, (int)(CommonUtil.dip2px(mContext, 300)));
													}
												}

											} catch (JSONException e) {
												e.printStackTrace();
												if (url.startsWith("http://data-fusion.tianqi.cn/datafusion/")) {
													OkHttpWeatherInfo(FetchWeather.weather2Url(cityId, "all"));
												}
											} catch (ParseException e1) {
												e1.printStackTrace();
											}

											cancelDialog();
											scrollView.setVisibility(View.VISIBLE);
										}
									}
								});
							}
						});
					}
				}).start();

			}
		});
	}

	/**
	 * 切换海南实况数据
	 */
	private void switchHNFactData(boolean flag) {
		if (flag) {//基本站数据
			if (!TextUtils.isEmpty(l7)) {
				tvTime.setText(l7 + "发布");
			}

			if (!TextUtils.isEmpty(l5)) {
				String currentTime = sdf1.format(new Date().getTime());
				int hour = Integer.valueOf(currentTime);
				Drawable drawable;
				if (hour >= 6 && hour < 18) {
					drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
				}else {
					drawable = getResources().getDrawable(R.drawable.phenomenon_drawable_night);
				}
				drawable.setLevel(Integer.valueOf(l5));
				ivPhe.setBackground(drawable);
				tvPhe.setText(getString(WeatherUtil.getWeatherId(Integer.valueOf(l5))));
			}

			if (!TextUtils.isEmpty(l1)) {
				tvTemperature.setText(l1+"℃");
			}

			if (!TextUtils.isEmpty(l4) && !TextUtils.isEmpty(l3)) {
					tvWind.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(l4))) + " " +
							WeatherUtil.getFactWindForce(Integer.valueOf(l3)));
			}

			if (!TextUtils.isEmpty(l2)) {
				tvHumidity.setText("湿度" + " "+ l2 + "%");
			}

			if (!TextUtils.isEmpty(l10)) {
				tvPressure.setText("气压"+" "+l10 + getString(R.string.unit_hPa));
			}
		}else {//最近站数据
			if (!TextUtils.isEmpty(nl7)) {
				tvTime.setText(nl7 + "发布");
			}

			if (!TextUtils.isEmpty(nl5)) {
				String currentTime = sdf1.format(new Date().getTime());
				int hour = Integer.valueOf(currentTime);
				Drawable drawable;
				if (hour >= 6 && hour < 18) {
					drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
				}else {
					drawable = getResources().getDrawable(R.drawable.phenomenon_drawable_night);
				}
				drawable.setLevel(Integer.valueOf(nl5));
				ivPhe.setBackground(drawable);
				tvPhe.setText(getString(WeatherUtil.getWeatherId(Integer.valueOf(nl5))));
			}

			if (!TextUtils.isEmpty(nl1)) {
				tvTemperature.setText(nl1+"℃");
			}

			if (!TextUtils.isEmpty(nl4) && !TextUtils.isEmpty(nl3)) {
				tvWind.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(nl4))) + " " +
						WeatherUtil.getFactWindForce(Integer.valueOf(nl3)));
			}

			if (!TextUtils.isEmpty(nl2)) {
				tvHumidity.setText("湿度" + " "+ nl2 + "%");
			}

			if (!TextUtils.isEmpty(nl10)) {
				tvPressure.setText("气压"+" "+nl10 + getString(R.string.unit_hPa));
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivList:
			if (hScrollView2.getVisibility() == View.VISIBLE) {
				ivList.setImageResource(R.drawable.iv_list);
				mListView.setVisibility(View.VISIBLE);
				hScrollView2.setVisibility(View.GONE);
			}else {
				ivList.setImageResource(R.drawable.iv_trend);
				mListView.setVisibility(View.GONE);
				hScrollView2.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.tvFact1:
			tvFact1.setBackgroundResource(R.drawable.btn_lb_corner_selected);
			tvFact2.setBackgroundResource(R.drawable.btn_rb_corner_unselected);
			switchHNFactData(true);
			break;
		case R.id.tvFact2:
			tvFact1.setBackgroundResource(R.drawable.btn_lb_corner_unselected);
			tvFact2.setBackgroundResource(R.drawable.btn_rb_corner_selected);
			switchHNFactData(false);
			break;

		default:
			break;
		}
	}

}
