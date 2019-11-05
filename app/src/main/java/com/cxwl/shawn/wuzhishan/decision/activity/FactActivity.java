package com.cxwl.shawn.wuzhishan.decision.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.adapter.DialogFactHistoryTimeAdapter;
import com.cxwl.shawn.wuzhishan.decision.adapter.FactAdapter;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData;
import com.cxwl.shawn.wuzhishan.decision.dto.ShawnRainDto;
import com.cxwl.shawn.wuzhishan.decision.fragment.FactHourCheckFragment;
import com.cxwl.shawn.wuzhishan.decision.fragment.FactMinuteCheckFragment;
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;
import com.cxwl.shawn.wuzhishan.decision.view.MainViewPager;
import com.cxwl.shawn.wuzhishan.decision.view.ScrollviewListview;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 实况资料
 * @author shawn_sun
 *
 */
public class FactActivity extends FragmentActivity implements OnClickListener, OnCameraChangeListener {
	
	private Context mContext;
	private RelativeLayout reTitle;
	private TextView tvTitle,tvControl,tvLayerName,tvToast,tvDetail,tvHistory,tvIntro;
	private LinearLayout llBack,llContainer2,llContainer,llContainer1,listTitle,llBottom,llViewPager,llRainCheck;
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private String url,layerName,startTime,endTime,title,stationName,area,val;
	private int selectId = 0;
	private ImageView ivChart;
	private ProgressBar progressBar;

	private List<Text> valueTexts = new ArrayList<>();//等值线
	private List<Polygon> factPolygons = new ArrayList<>();//实况图层
	private List<Text> cityNames = new ArrayList<>();//城市名称
	private List<Polyline> adcodePolylines = new ArrayList<>();//行政区划边界线

	private List<ShawnRainDto> times = new ArrayList<>();
	private List<ShawnRainDto> realDatas = new ArrayList<>();
	public static String childId = null;
	private int width = 0, height = 0;
	private float density = 0;
	private ScrollviewListview listView;
	private FactAdapter mAdapter;
	private List<ShawnRainDto> dataList = new ArrayList<>();
	private ScrollView scrollView;
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	private TextView tv1, tv2, tv3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fact);
		mContext = this;
		initListView();
		initWidget();
		initAmap(savedInstanceState);
	}

	private void initListView() {
		listView = findViewById(R.id.listView);
		mAdapter = new FactAdapter(mContext, dataList);
		listView.setAdapter(mAdapter);
	}
	
	private void initWidget() {
		reTitle = findViewById(R.id.reTitle);
		llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		tvControl = findViewById(R.id.tvControl);
		tvControl.setOnClickListener(this);
		tvControl.setText("天气统计");
		tvControl.setVisibility(View.VISIBLE);
		llContainer2 = findViewById(R.id.llContainer2);
		llContainer = findViewById(R.id.llContainer);
		llContainer1 = findViewById(R.id.llContainer1);
		tvLayerName = findViewById(R.id.tvLayerName);
		progressBar = findViewById(R.id.progressBar);
		ivChart = findViewById(R.id.ivChart);
		tvDetail = findViewById(R.id.tvDetail);
		tvDetail.setOnClickListener(this);
		tvHistory = findViewById(R.id.tvHistory);
		tvHistory.setOnClickListener(this);
		tvToast = findViewById(R.id.tvToast);
		tvIntro = findViewById(R.id.tvIntro);
		listTitle = findViewById(R.id.listTitle);
		llBottom = findViewById(R.id.llBottom);
		scrollView = findViewById(R.id.scrollView);
		llViewPager = findViewById(R.id.llViewPager);
		llRainCheck = findViewById(R.id.llRainCheck);
		tv1 = findViewById(R.id.tv1);
		tv2 = findViewById(R.id.tv2);
		tv3 = findViewById(R.id.tv3);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		density = dm.density;

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}

		if (getIntent().hasExtra("data")) {
			ColumnData data = getIntent().getParcelableExtra("data");
			if (data != null) {
				final List<ColumnData> columnList = new ArrayList<>(data.child);
				llContainer2.removeAllViews();
				if (columnList.size() > 0) {
					for (int i = 0; i < columnList.size(); i++) {
						ColumnData dto = columnList.get(i);

						//降水、温度、风速标签背景
						LinearLayout llItem = new LinearLayout(mContext);
						llItem.setGravity(Gravity.CENTER);
						llItem.setOrientation(LinearLayout.HORIZONTAL);
						if (!TextUtils.isEmpty(dto.name)) {
							llItem.setTag(dto.name);
						}

						//降水、温度、风速标签文字
						TextView tvItem = new TextView(mContext);
						tvItem.setGravity(Gravity.CENTER);
						tvItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
						tvItem.setPadding(0, (int)(density*10), 0, (int)(density*10));
						tvItem.setMaxLines(1);
						if (!TextUtils.isEmpty(dto.name)) {
							tvItem.setText(dto.name);
						}

						if (i == 0) {
							llItem.setBackgroundColor(getResources().getColor(R.color.white));
							tvItem.setTextColor(getResources().getColor(R.color.colorPrimary));
							switchItem(dto);
						}else {
							llItem.setBackgroundColor(getResources().getColor(R.color.gray));
							tvItem.setTextColor(getResources().getColor(R.color.text_color3));
						}
						llItem.addView(tvItem);
						llContainer2.addView(llItem);
						LayoutParams params = llItem.getLayoutParams();
						if (columnList.size() <= 3) {
							params.width = width/3;
						}else {
							params.width = width/4;
						}
						llItem.setLayoutParams(params);
						llItem.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								if (llContainer2 != null) {
									for (int i = 0; i < llContainer2.getChildCount(); i++) {
										LinearLayout llItem = (LinearLayout) llContainer2.getChildAt(i);
										ColumnData dto = columnList.get(i);
										TextView tvItem = (TextView) llItem.getChildAt(0);
										if (TextUtils.equals((String) arg0.getTag(), (String) llItem.getTag())) {
											llItem.setBackgroundColor(getResources().getColor(R.color.white));
											tvItem.setTextColor(getResources().getColor(R.color.colorPrimary));
											switchItem(dto);
										}else {
											llItem.setBackgroundColor(getResources().getColor(R.color.gray));
											tvItem.setTextColor(getResources().getColor(R.color.text_color3));
										}
									}
								}
							}
						});
					}
				}

				reTitle.setFocusable(true);
				reTitle.setFocusableInTouchMode(true);
				reTitle.requestFocus();
			}
		}

	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		if (viewPager != null) {
			viewPager.removeAllViewsInLayout();
			fragments.clear();
		}

		llRainCheck.removeAllViews();
		for (int i = 0; i < 2; i++) {
			TextView tvName = new TextView(mContext);
			if (i == 0) {
				tvName.setText("近7天分钟查询");
				tvName.setBackgroundColor(Color.WHITE);
				tvName.setTextColor(getResources().getColor(R.color.text_color3));
			}else {
				tvName.setText("近3个月逐时查询");
				tvName.setBackgroundColor(getResources().getColor(R.color.light_gray));
				tvName.setTextColor(getResources().getColor(R.color.text_color3));
			}
			tvName.setOnClickListener(new MyOnClickListener(i));
			tvName.setGravity(Gravity.CENTER);
			tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
			tvName.setPadding(0, (int)(density*10), 0, (int)(density*10));
			tvName.setMaxLines(1);
			tvName.setTag(i);
			llRainCheck.addView(tvName);
			LayoutParams params = tvName.getLayoutParams();
			params.width = width/2;
			tvName.setLayoutParams(params);
		}

		Bundle bundle = new Bundle();
		bundle.putString("childId", childId);
		Fragment fragment1 = new FactMinuteCheckFragment();
		fragment1.setArguments(bundle);
		fragments.add(fragment1);
		Fragment fragment2 = new FactHourCheckFragment();
		fragment2.setArguments(bundle);
		fragments.add(fragment2);

		if (viewPager == null) {
			viewPager = findViewById(R.id.viewPager);
			viewPager.setSlipping(true);//设置ViewPager是否可以滑动
			viewPager.setOffscreenPageLimit(fragments.size());
			viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		}
		viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			if (llRainCheck != null) {
				for (int i = 0; i < llRainCheck.getChildCount(); i++) {
					TextView tv = (TextView) llRainCheck.getChildAt(i);
					if (i == arg0) {
						tv.setBackgroundColor(Color.WHITE);
						tv.setTextColor(getResources().getColor(R.color.text_color3));
					}else {
						tv.setBackgroundColor(getResources().getColor(R.color.light_gray));
						tv.setTextColor(getResources().getColor(R.color.text_color3));
					}
				}
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	/**
	 * 头标点击监听
	 * @author shawn_sun
	 */
	private class MyOnClickListener implements View.OnClickListener {
		private int index;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			if (viewPager != null) {
				viewPager.setCurrentItem(index, true);
			}
		}
	}

	/**
	 * @ClassName: MyPagerAdapter
	 * @Description: TODO填充ViewPager的数据适配器
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:37:47
	 *
	 */
	private class MyPagerAdapter extends FragmentStatePagerAdapter {

		private MyPagerAdapter(FragmentManager fm) {
			super(fm);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public Fragment getItem(int arg0) {
			return fragments.get(arg0);
		}

		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}
	}

	/**
	 * 初始化高德地图
	 */
	private void initAmap(Bundle bundle) {
		mapView = findViewById(R.id.mapView);
		mapView.setVisibility(View.VISIBLE);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}

		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(18.834362424286152,109.52166572213174), 10f));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.setOnCameraChangeListener(this);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.showMapText(false);
		aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
			@Override
			public void onMapLoaded() {
				drawCityNameAndDistrict();
			}
		});

		TextView tvMapNumber = findViewById(R.id.tvMapNumber);
		tvMapNumber.setText(aMap.getMapContentApprovalNumber());

		aMap.setOnMapTouchListener(new OnMapTouchListener() {
			@Override
			public void onTouch(MotionEvent arg0) {
				if (scrollView != null) {
					if (arg0.getAction() == MotionEvent.ACTION_UP) {
						scrollView.requestDisallowInterceptTouchEvent(false);
					}else {
						scrollView.requestDisallowInterceptTouchEvent(true);
					}
				}
			}
		});

		LatLngBounds bounds = new LatLngBounds.Builder()
//		.include(new LatLng(57.9079, 71.9282))
//		.include(new LatLng(3.9079, 134.8656))
		.include(new LatLng(1, 66))
		.include(new LatLng(60, 153))
		.build();
		aMap.addGroundOverlay(new GroundOverlayOptions()
			.anchor(0.5f, 0.5f)
			.positionFromBounds(bounds)
			.image(BitmapDescriptorFactory.fromResource(R.drawable.bg_empty))
			.transparency(0.0f));
		aMap.runOnDrawFrame();

	}

	/**
	 * 绘制城市名称及行政边界
	 */
	private void drawCityNameAndDistrict() {
		if (aMap == null) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				removeCityNames();
				removePolylines();
				String result = CommonUtil.getFromAssets(mContext, "json/all_citys.json");
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject obj = new JSONObject(result);
						if (!obj.isNull("districts")) {
							JSONArray array = obj.getJSONArray("districts");
							for (int i = 0; i < array.length(); i++) {
								JSONObject itemObj = array.getJSONObject(i);
								ShawnRainDto dto = new ShawnRainDto();
								if (!itemObj.isNull("name")) {
									String name = itemObj.getString("name");
									if (name.contains("五指山")) {
										dto.cityName = name.substring(0, 3);
									}else {
										dto.cityName = name.substring(0, 2);
									}
								}
								if (!itemObj.isNull("center")) {
									String[] latLng = itemObj.getString("center").split(",");
									dto.lng = Double.valueOf(latLng[0]);
									dto.lat = Double.valueOf(latLng[1]);
								}

								TextOptions options = new TextOptions();
								options.position(new LatLng(dto.lat+0.05, dto.lng));
								options.fontColor(Color.BLACK);
								options.fontSize(20);
								options.text(dto.cityName);
								options.backgroundColor(Color.TRANSPARENT);
								aMap.addText(options);
							}

							CommonUtil.drawAllDistrict(mContext, aMap, adcodePolylines);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	private void switchItem(ColumnData dto) {
		llContainer.removeAllViews();
		llContainer1.removeAllViews();
		if (dto.child.size() > 0) {
			for (int j = 0; j < dto.child.size(); j++) {
				final ColumnData itemDto = dto.child.get(j);
				TextView tvName = new TextView(mContext);
				tvName.setGravity(Gravity.CENTER);
				tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
				tvName.setPadding(0, (int)(density*10), 0, (int)(density*10));
				tvName.setMaxLines(1);

				TextView tvBar = new TextView(mContext);
				tvBar.setGravity(Gravity.CENTER);

				if (!TextUtils.isEmpty(itemDto.name)) {
					tvName.setText(itemDto.name);
					tvName.setTag(itemDto.dataUrl+","+itemDto.id);
				}
				if (j == 0) {
					childId = itemDto.id;
					if (mAdapter != null) {
						mAdapter.childId = itemDto.id;
					}

					//648任意时段查询，657最高温查询，658最低温查询，655风速查询
					if (TextUtils.equals(itemDto.id, "648") || TextUtils.equals(itemDto.id, "657")
							|| TextUtils.equals(itemDto.id, "658") || TextUtils.equals(itemDto.id, "655")) {
						llViewPager.setVisibility(View.VISIBLE);
						scrollView.setVisibility(View.GONE);
						initViewPager();
					} else {
						llViewPager.setVisibility(View.GONE);
						scrollView.setVisibility(View.VISIBLE);
						layerName = tvName.getText().toString();
						if (!TextUtils.isEmpty(itemDto.dataUrl)) {
							url = itemDto.dataUrl;
							progressBar.setVisibility(View.VISIBLE);
							OkHttpInfo(url);
						}
					}
					tvName.setTextColor(getResources().getColor(R.color.colorPrimary));
					tvBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
				}else {
					tvName.setTextColor(getResources().getColor(R.color.text_color3));
					tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
				}
				llContainer.addView(tvName);
				LayoutParams params = tvName.getLayoutParams();
				if (dto.child.size() <= 3) {
					params.width = width/3;
				}else {
					params.width = width/4;
				}
				tvName.setLayoutParams(params);

				llContainer1.addView(tvBar);
				LayoutParams params1 = tvBar.getLayoutParams();
				if (dto.child.size() <= 3) {
					params1.width = width/3;
				}else {
					params1.width = width/4;
				}
				params1.height = (int) (density*2);
				tvBar.setLayoutParams(params1);

				tvName.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (llContainer != null) {
							for (int i = 0; i < llContainer.getChildCount(); i++) {
								TextView tvName = (TextView) llContainer.getChildAt(i);
								TextView tvBar = (TextView) llContainer1.getChildAt(i);
								if (TextUtils.equals((String) arg0.getTag(), (String) tvName.getTag())) {
									String[] tags = ((String) arg0.getTag()).split(",");
									childId = itemDto.id;
									if (mAdapter != null) {
										mAdapter.childId = itemDto.id;
									}

									//648任意时段查询，657最高温查询，658最低温查询，655风速查询
									if (TextUtils.equals(itemDto.id, "648") || TextUtils.equals(itemDto.id, "657")
											|| TextUtils.equals(itemDto.id, "658") || TextUtils.equals(itemDto.id, "655")) {
										llViewPager.setVisibility(View.VISIBLE);
										scrollView.setVisibility(View.GONE);
										initViewPager();
									} else {
										llViewPager.setVisibility(View.GONE);
										scrollView.setVisibility(View.VISIBLE);
										url = tags[0];
										layerName = tvName.getText().toString();
									}
									tvName.setTextColor(getResources().getColor(R.color.colorPrimary));
									tvBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
								}else {
									tvName.setTextColor(getResources().getColor(R.color.text_color3));
									tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
								}
							}

							if (!TextUtils.isEmpty(url)) {
								progressBar.setVisibility(View.VISIBLE);
								OkHttpInfo(url);
							}
						}
					}
				});
			}
		}
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Point leftPoint = new Point(0, dm.heightPixels);
		Point rightPoint = new Point(dm.widthPixels, 0);
		LatLng leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
		LatLng rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);

//		if (listView != null) {
//			if (listView.getVisibility() == View.VISIBLE) {
//				if (leftlatlng.latitude <= 18.000322917671003 || rightLatlng.latitude >= 19.185356618508102 || leftlatlng.longitude <= 109.13228809833528
//						|| rightLatlng.longitude >= 109.88498568534852 || arg0.zoom < 9.5f) {
//					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(18.834362424286152,109.52166572213174), 10.0f));
//				}
//			}else {
//				if (leftlatlng.latitude <= 18.000322917671003 || rightLatlng.latitude >= 19.185356618508102 || leftlatlng.longitude <= 109.13228809833528
//						|| rightLatlng.longitude >= 109.88498568534852 || arg0.zoom < 10.0f) {
//					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(18.834362424286152,109.52166572213174), 10.5f));
//				}
//			}
//		}else {
//			if (leftlatlng.latitude <= 18.000322917671003 || rightLatlng.latitude >= 19.185356618508102 || leftlatlng.longitude <= 109.13228809833528
//					|| rightLatlng.longitude >= 109.88498568534852 || arg0.zoom < 9.5f) {
//				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(18.834362424286152,109.52166572213174), 10.0f));
//			}
//		}
	}

	/**
	 * 获取实况数据
	 * @param url
	 */
	private void OkHttpInfo(final String url) {
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
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);

										if (!obj.isNull("th")) {
											JSONObject itemObj = obj.getJSONObject("th");
											if (!itemObj.isNull("stationName")) {
												stationName = itemObj.getString("stationName");
											}
											if (!itemObj.isNull("area")) {
												area = itemObj.getString("area");
											}
											if (!itemObj.isNull("val")) {
												val = itemObj.getString("val");
											}
										}

										if (!obj.isNull("zh")) {
											JSONObject itemObj = obj.getJSONObject("zh");
											if (!itemObj.isNull("stationName")) {
												tv3.setText(itemObj.getString("stationName"));
											}
											if (!itemObj.isNull("area")) {
												tv2.setText(itemObj.getString("area"));
											}
											if (!itemObj.isNull("val")) {
												tv1.setText(itemObj.getString("val"));
											}
										}

										if (!obj.isNull("title")) {
											title = obj.getString("title");
										}

										if (!obj.isNull("cutlineUrl")) {
											Picasso.with(mContext).load(obj.getString("cutlineUrl")).into(ivChart);
										}

										if (!obj.isNull("times")) {
											times.clear();
											JSONArray array = new JSONArray(obj.getString("times"));
											for (int i = 0; i < array.length(); i++) {
												JSONObject itemObj = array.getJSONObject(i);
												ShawnRainDto dto = new ShawnRainDto();
												if (!itemObj.isNull("timeString")) {
													dto.timeString = itemObj.getString("timeString");
													if (i == selectId) {
														startTime = itemObj.getString("timestart");
														endTime = itemObj.getString("timeParams");
														tvLayerName.setText(dto.timeString);
														tvLayerName.setVisibility(View.VISIBLE);
														if (layerName != null) {
															tvLayerName.setText(dto.timeString+layerName);
														}
													}
												}
												if (!itemObj.isNull("timeParams")) {
													dto.timeParams = itemObj.getString("timeParams");
												}
												times.add(dto);
											}
										}

										if (!obj.isNull("realDatas")) {
											realDatas.clear();
											JSONArray array = new JSONArray(obj.getString("realDatas"));
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
													dto.value = itemObj.getDouble("val");
												}

												if (!TextUtils.isEmpty(dto.stationName) && !TextUtils.isEmpty(dto.area)) {
													realDatas.add(dto);
												}
											}
										}

										if (!obj.isNull("dataUrl")) {
											String dataUrl = obj.getString("dataUrl");
											if (!TextUtils.isEmpty(dataUrl)) {
												OkHttpLayer(dataUrl);
											}
										}

										if (!obj.isNull("zx")) {
											tvIntro.setText(obj.getString("zx"));
										}

										if (!obj.isNull("jb")) {
											dataList.clear();
											JSONArray array = obj.getJSONArray("jb");
											for (int i = 0; i < array.length(); i++) {
												JSONObject itemObj = array.getJSONObject(i);
												ShawnRainDto data = new ShawnRainDto();
												if (!itemObj.isNull("lv")) {
													data.rainLevel = itemObj.getString("lv");
												}
												if (!itemObj.isNull("count")) {
													data.count = itemObj.getInt("count")+"";
												}
												if (!itemObj.isNull("xs")) {
													JSONArray xsArray = itemObj.getJSONArray("xs");
													List<ShawnRainDto> list = new ArrayList<>();
													list.clear();
													for (int j = 0; j < xsArray.length(); j++) {
														ShawnRainDto d = new ShawnRainDto();
														d.area = xsArray.getString(j);
														list.add(d);
													}
													data.areaList.addAll(list);
												}
												dataList.add(data);
											}
											if (mAdapter != null) {
												mAdapter.startTime = startTime;
												mAdapter.endTime = endTime;
												mAdapter.notifyDataSetChanged();
												tvIntro.setVisibility(View.VISIBLE);
												listTitle.setVisibility(View.VISIBLE);
												listView.setVisibility(View.VISIBLE);
											}
										}else {
											tvIntro.setVisibility(View.GONE);
											listTitle.setVisibility(View.GONE);
											listView.setVisibility(View.GONE);
										}

										int statusBarHeight = -1;
										//获取status_bar_height资源的ID
										int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
										if (resourceId > 0) {
											//根据资源ID获取响应的尺寸值
											statusBarHeight = getResources().getDimensionPixelSize(resourceId);
										}
										int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
										int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
										reTitle.measure(w, h);
										llContainer2.measure(w, h);
										llContainer.measure(w, h);
										listTitle.measure(w, h);
										llBottom.measure(w, h);
										LayoutParams mapParams = mapView.getLayoutParams();
										if (listView.getVisibility() == View.VISIBLE) {
											mapParams.height = height-statusBarHeight-reTitle.getMeasuredHeight()-llContainer2.getMeasuredHeight()
													-llContainer.getMeasuredHeight()-listTitle.getMeasuredHeight()*8;
											new Handler().postDelayed(new Runnable() {
												@Override
												public void run() {
													aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(19.211397,109.795324), 7.8f));
												}
											}, 500);
										}else {
											mapParams.height = height-statusBarHeight-reTitle.getMeasuredHeight()-llContainer2.getMeasuredHeight()
													-llContainer.getMeasuredHeight()-llBottom.getMeasuredHeight();
											new Handler().postDelayed(new Runnable() {
												@Override
												public void run() {
													aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(19.211397,109.795324), 8.2f));
												}
											}, 500);
										}
										mapView.setLayoutParams(mapParams);

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}else {
									removeFactPolygons();
									progressBar.setVisibility(View.GONE);
									tvToast.setVisibility(View.VISIBLE);
									new Handler().postDelayed(new Runnable() {
										@Override
										public void run() {
											tvToast.setVisibility(View.GONE);
										}
									}, 1000);
								}
							}
						});
					}
				});
			}
		}).start();
	}

	/**
	 * 获取图层数据
	 * @param url
	 */
	private void OkHttpLayer(final String url) {
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
						if (!TextUtils.isEmpty(result)) {
							drawLayers(result);
						}else {
							removeFactPolygons();
							removeCityNames();
							removePolylines();

							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									progressBar.setVisibility(View.GONE);
									tvToast.setVisibility(View.VISIBLE);
									new Handler().postDelayed(new Runnable() {
										@Override
										public void run() {
											tvToast.setVisibility(View.GONE);
										}
									}, 1000);
								}
							});

						}

					}
				});
			}
		}).start();
	}

	/**
	 * 绘制所有图层、等值线等数据
	 */
	private void drawLayers(String result) {
		drawFactPolygons(result);
	}

	/**
	 * 清除等值线
	 */
	private void removeTexts() {
		for (int i = 0; i < valueTexts.size(); i++) {
			valueTexts.get(i).remove();
		}
		valueTexts.clear();
	}

	/**
	 * 清除实况图层
	 */
	private void removeFactPolygons() {
		for (int i = 0; i < factPolygons.size(); i++) {
			factPolygons.get(i).remove();
		}
		factPolygons.clear();
	}

	/**
	 * 清除城市名称
	 */
	private void removeCityNames() {
		for (int i = 0; i < cityNames.size(); i++) {
			cityNames.get(i).remove();
		}
		cityNames.clear();
	}

	/**
	 * 清除行政区划边界
	 */
	private void removePolylines() {
		for (int i = 0; i < adcodePolylines.size(); i++) {
			adcodePolylines.get(i).remove();
		}
		adcodePolylines.clear();
	}

	/**
	 * 绘制所有图层、等值线等数据
	 */
	private void drawFactPolygons(final String result) {
		removeTexts();
		removeFactPolygons();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject obj = new JSONObject(result);
					JSONArray array = obj.getJSONArray("l");
					int length = array.length();
					for (int i = 0; i < length; i++) {
						JSONObject itemObj = array.getJSONObject(i);
						JSONArray c = itemObj.getJSONArray("c");
						int r = c.getInt(0);
						int g = c.getInt(1);
						int b = c.getInt(2);
						int a = (int) (c.getInt(3)*255*1.0);

						double centerLat = 0;
						double centerLng = 0;
						String p = itemObj.getString("p");
						if (!TextUtils.isEmpty(p)) {
							String[] points = p.split(";");
							PolygonOptions polygonOption = new PolygonOptions();
							polygonOption.fillColor(Color.argb(a, r, g, b));
							polygonOption.strokeColor(Color.BLACK);
							polygonOption.strokeWidth(1);
							for (int j = 0; j < points.length; j++) {
								String[] value = points[j].split(",");
								double lat = Double.valueOf(value[1]);
								double lng = Double.valueOf(value[0]);
								polygonOption.add(new LatLng(lat, lng));
								if (j == points.length/2) {
									centerLat = lat;
									centerLng = lng;
								}
							}
							Polygon polygon = aMap.addPolygon(polygonOption);
							factPolygons.add(polygon);
						}

						if (!itemObj.isNull("v")) {
							int v = itemObj.getInt("v");
							TextOptions options = new TextOptions();
							options.position(new LatLng(centerLat, centerLng));
							options.fontColor(Color.BLACK);
							options.fontSize(25);
							options.text(v+"");
							options.backgroundColor(Color.TRANSPARENT);
							Text text = aMap.addText(options);
							valueTexts.add(text);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progressBar.setVisibility(View.GONE);
			}
		});
	}

	/**
	 * 历史数据查询
	 */
	private void dialogHistory() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_fact_history_time, null);
		TextView tvNegative = view.findViewById(R.id.tvNegative);
		ListView listView = view.findViewById(R.id.listView);
		DialogFactHistoryTimeAdapter mAdapter = new DialogFactHistoryTimeAdapter(mContext, times);
		listView.setAdapter(mAdapter);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				dialog.dismiss();
				ShawnRainDto dto = times.get(arg2);
				selectId = arg2;
				if (!TextUtils.isEmpty(url)) {
					progressBar.setVisibility(View.VISIBLE);
					OkHttpInfo(url+dto.timeParams);
				}
			}
		});
		
		tvNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.tvDetail:
			Intent intent = new Intent(mContext, FactStationListActivity.class);
			intent.putExtra("childId", childId);
			intent.putExtra("title", title);
			intent.putExtra("stationName", stationName);
			intent.putExtra("area", area);
			intent.putExtra("val", val);
			intent.putExtra("startTime", startTime);
			intent.putExtra("endTime", endTime);
			intent.putParcelableArrayListExtra("realDatas", (ArrayList<? extends Parcelable>) realDatas);
			startActivity(intent);
			break;
		case R.id.tvHistory:
			dialogHistory();
			break;
		case R.id.tvControl:
			startActivity(new Intent(mContext, StaticsActivity.class));
			break;

		default:
			break;
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onResume() {
		super.onResume();
		if (mapView != null) {
			mapView.onResume();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onPause() {
		super.onPause();
		if (mapView != null) {
			mapView.onPause();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mapView != null) {
			mapView.onSaveInstanceState(outState);
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mapView != null) {
			mapView.onDestroy();
		}
	}
	
}
