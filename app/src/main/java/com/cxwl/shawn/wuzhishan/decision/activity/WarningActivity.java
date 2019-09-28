package com.cxwl.shawn.wuzhishan.decision.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData;
import com.cxwl.shawn.wuzhishan.decision.dto.WarningDto;
import com.cxwl.shawn.wuzhishan.decision.fragment.PdfListFragment;
import com.cxwl.shawn.wuzhishan.decision.fragment.WarningFragment;
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;
import com.cxwl.shawn.wuzhishan.decision.view.MainViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 预警信息
 * @author shawn_sun
 *
 */
public class WarningActivity extends FragmentActivity implements OnClickListener, OnMapClickListener,
        OnMarkerClickListener, InfoWindowAdapter {
	
	private Context mContext;
	private ImageView ivExpand;
	private boolean isExpand = false;
	private MapView mapView;//高德地图
	private AMap aMap;//高德地图
	private List<WarningDto> dataList = new ArrayList<>();
	private Marker selectMarker;
	private LinearLayout llContainer,llContainer1;
	private HorizontalScrollView hScrollView1;
	private int width;
	private float density;
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	private ColumnData data;
	private List<Marker> markers = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_warning);
		mContext = this;
		initAmap(savedInstanceState);
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		ivExpand = findViewById(R.id.ivExpand);
		ivExpand.setOnClickListener(this);
		llContainer = findViewById(R.id.llContainer);
		llContainer1 = findViewById(R.id.llContainer1);
		hScrollView1 = findViewById(R.id.hScrollView1);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		density = dm.density;

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}

		data = getIntent().getParcelableExtra("data");
		if (data != null) {
			ColumnData dto = data.child.get(0);
			if (TextUtils.equals(dto.showType, CONST.WARNING)) {
				OkHttpWarning(dto.dataUrl);
			}
			initViewPager(data);
		}
    }

	/**
	 * 初始化高德地图
	 */
	private void initAmap(Bundle bundle) {
		mapView = findViewById(R.id.mapView);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		
		LatLng centerLatLng = new LatLng(CONST.DEFAULT_LAT, CONST.DEFAULT_LNG);
		aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 8.0f));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMapClickListener(this);
		aMap.setOnMarkerClickListener(this);
		aMap.setInfoWindowAdapter(this);

		TextView tvMapNumber = findViewById(R.id.tvMapNumber);
		tvMapNumber.setText(aMap.getMapContentApprovalNumber());
	}

	/**
	 * 获取预警信息
	 */
	private void OkHttpWarning(final String url) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
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
										JSONObject object = new JSONObject(result);
										if (!object.isNull("w")) {
											JSONArray jsonArray = object.getJSONArray("w");
											dataList.clear();
											for (int i = 0; i < jsonArray.length(); i++) {
												WarningDto dto = new WarningDto();
												JSONObject itemObj = jsonArray.getJSONObject(i);
												String w1 = itemObj.getString("w1");
												dto.w2 = itemObj.getString("w2");
												String w4 = itemObj.getString("w4");
												String w5 = itemObj.getString("w5");
												String w6 = itemObj.getString("w6");
												String w7 = itemObj.getString("w7");
												String w8 = itemObj.getString("w8");
												String w9 = itemObj.getString("w9");
												dto.w11 = itemObj.getString("w11");

												dto.name = w1+dto.w2+"发布"+w5+w7+"预警";
												dto.time = w8;
												dto.type = "icon_warning_"+w4;
												dto.color = w6;
												dto.content = w9;

												String[] names = getResources().getStringArray(R.array.district_name);
												for (int j = 0; j < names.length; j++) {
													String[] itemArray = names[j].split(",");
													String value;
													if (!TextUtils.isEmpty(dto.w2)) {
														value = dto.w2;
													}else {
														value = dto.w11;
													}
													if (value.contains(itemArray[0]) || itemArray[0].contains(value)) {
														if (!TextUtils.isEmpty(itemArray[2]) && !TextUtils.isEmpty(itemArray[1])) {
															dto.lat = itemArray[2];
															dto.lng = itemArray[1];
															break;
														}
													}
												}

												dataList.add(dto);
											}
											addMarkerAndDrawDistrict();
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

	private void removeMarkers() {
		for (int i = 0; i < markers.size(); i++) {
			Marker marker = markers.get(i);
			marker.remove();
		}
		markers.clear();
	}

	private void addMarkerAndDrawDistrict() {
		removeMarkers();
		for (int i = 0; i < dataList.size(); i++) {
			WarningDto dto = dataList.get(i);
			if (TextUtils.isEmpty(dto.lat) || TextUtils.isEmpty(dto.lng)) {
				return;
			}
			MarkerOptions options = new MarkerOptions();
			if (!TextUtils.isEmpty(dto.w2)) {
				options.title(dto.w2);
			}else {
				options.title(dto.w11);
			}
			options.position(new LatLng(Double.parseDouble(dto.lat), Double.parseDouble(dto.lng)));

			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View markerView = inflater.inflate(R.layout.marker_warning_icon, null);
			ImageView ivMarker = markerView.findViewById(R.id.ivMarker);

			Bitmap bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+dto.color+CONST.imageSuffix);
			if (bitmap == null) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+dto.color+CONST.imageSuffix);
			}
			ivMarker.setImageBitmap(bitmap);
			LayoutParams params = ivMarker.getLayoutParams();
			if ("琼州海峡".contains(options.getTitle()) || "本岛西部".contains(options.getTitle()) || "本岛南部".contains(options.getTitle())
					|| "本岛东部".contains(options.getTitle()) || "北部湾北部".contains(options.getTitle()) || "北部湾南部".contains(options.getTitle())
					|| "中沙附近".contains(options.getTitle()) || "西沙附近".contains(options.getTitle()) || "南沙附近".contains(options.getTitle())) {
				params.width = (int) CommonUtil.dip2px(getApplicationContext(), 30);
				params.height = (int) CommonUtil.dip2px(getApplicationContext(), 30);
			}else {
				params.width = (int) CommonUtil.dip2px(getApplicationContext(), 20);
				params.height = (int) CommonUtil.dip2px(getApplicationContext(), 20);
			}
			ivMarker.setLayoutParams(params);
			options.icon(BitmapDescriptorFactory.fromView(markerView));
			Marker marker = aMap.addMarker(options);
			markers.add(marker);
		}

		Collections.sort(dataList, new Comparator<WarningDto>() {
			@Override
			public int compare(WarningDto a, WarningDto b) {
				return a.color.compareTo(b.color);
			}
		});

		Map<String, WarningDto> map = new HashMap<>();
		int color = 0;
		for (int i = 0; i < dataList.size(); i++) {
			WarningDto data = dataList.get(i);
			String name = data.w2;
			if (TextUtils.isEmpty(data.w2)) {
				name = data.w11;
			}
			if (map.containsKey(name)) {
				String c = data.color;
				if (!TextUtils.isEmpty(c)) {
					if (color <= Integer.valueOf(c)) {
						color = Integer.valueOf(c);
						map.put(name, data);
					}
				}
			}else {
				map.put(name, data);
				color = 0;
			}
		}

		for (Map.Entry<String, WarningDto> entry : map.entrySet()) {
//			JsonMap dto = map.get(map.keySet().iterator().next());
			WarningDto dto = entry.getValue();
			String c = dto.color;
			if (!TextUtils.isEmpty(c)) {
				int color2 = 0;
				if (TextUtils.equals(c, "01")) {
					color2 = getResources().getColor(R.color.blue);
				}else if (TextUtils.equals(c, "02")) {
					color2 = getResources().getColor(R.color.yellow);
				}else if (TextUtils.equals(c, "03")) {
					color2 = getResources().getColor(R.color.orange);
				}else if (TextUtils.equals(c, "04")) {
					color2 = getResources().getColor(R.color.red);
				}
				String districtName = dto.w2;
				if (!TextUtils.isEmpty(districtName)) {
					if (districtName.contains("陵水")) {
						districtName = "陵水黎族自治县";
					}else if (districtName.contains("昌江")) {
						districtName = "昌江黎族自治县";
					}else if (districtName.contains("白沙")) {
						districtName = "白沙黎族自治县";
					}else if (districtName.contains("琼中")) {
						districtName = "琼中黎族苗族自治县";
					}else if (districtName.contains("乐东")) {
						districtName = "乐东黎族自治县";
					}else if (districtName.contains("保亭")) {
						districtName = "保亭黎族苗族自治县";
					}
					CommonUtil.drawWarningDistrict(getApplicationContext(), aMap, districtName, color2);
				}
			}
		}
	}

	@Override
	public void onMapClick(LatLng arg0) {
		if (selectMarker != null) {
			selectMarker.hideInfoWindow();
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		selectMarker = marker;
		marker.showInfoWindow();
		return true;
	}

	@Override
	public View getInfoContents(final Marker marker) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.marker_warning_info, null);
		TextView tvName = view.findViewById(R.id.tvName);
		tvName.setText(marker.getTitle());

		LinearLayout llContainer = view.findViewById(R.id.llContainer);

		final List<WarningDto> tempList = new ArrayList<>();
		for (int i = 0; i < dataList.size(); i++) {
			WarningDto dto = dataList.get(i);
			String title = dto.w2;
			if (TextUtils.isEmpty(title)) {
				title = dto.w11;
			}
			if (title.contains(marker.getTitle()) || marker.getTitle().contains(title)) {
				tempList.add(dto);
			}
		}

		llContainer.removeAllViews();
		for (int i = 0; i < tempList.size(); i++) {
			final WarningDto data = tempList.get(i);
			ImageView imageView = new ImageView(getApplicationContext());
			Bitmap bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+data.type+data.color+CONST.imageSuffix);
			if (bitmap == null) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+data.color+CONST.imageSuffix);
			}
			imageView.setImageBitmap(bitmap);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)CommonUtil.dip2px(getApplicationContext(), 30), (int)CommonUtil.dip2px(getApplicationContext(), 30));
			params.setMargins(0, 0, 15, 0);
			imageView.setLayoutParams(params);
			imageView.setTag(i+"");
			llContainer.addView(imageView);

			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, WarningDetailActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", data);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
		}

		return view;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		return null;
	}

	/**
	 * 初始化viewPager
	 */
	private void initViewPager(ColumnData data) {
		List<ColumnData> columnList = data.child;
		int columnSize = columnList.size();
		if (columnSize <= 1) {
			llContainer.setVisibility(View.GONE);
			llContainer1.setVisibility(View.GONE);
		}
		llContainer.removeAllViews();
		llContainer1.removeAllViews();
		for (int i = 0; i < columnSize; i++) {
			ColumnData dto = columnList.get(i);

			TextView tvName = new TextView(mContext);
			tvName.setGravity(Gravity.CENTER);
			tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			tvName.setPadding(0, (int)(density*10), 0, (int)(density*10));
			tvName.setOnClickListener(new MyOnClickListener(i));
			if (i == 0) {
				tvName.setTextColor(getResources().getColor(R.color.colorPrimary));
			}else {
				tvName.setTextColor(getResources().getColor(R.color.text_color3));
			}
			if (!TextUtils.isEmpty(dto.name)) {
				tvName.setText(dto.name);
			}
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			params.weight = 1.0f;
			if (columnSize == 1) {
				params.width = width;
			}else if (columnSize == 2) {
				params.width = width/2;
			}else if (columnSize == 3) {
				params.width = width/3;
			}else {
				params.width = width/4;
			}
			tvName.setLayoutParams(params);
			llContainer.addView(tvName, i);

			TextView tvBar = new TextView(mContext);
			tvBar.setGravity(Gravity.CENTER);
			tvBar.setOnClickListener(new MyOnClickListener(i));
			if (i == 0) {
				tvBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
			}else {
				tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
			}
			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			params1.weight = 1.0f;
			if (columnSize == 1) {
				params1.width = width;
			}else if (columnSize == 2) {
				params1.width = width/2-(int)(density*20);
			}else if (columnSize == 3) {
				params1.width = width/3-(int)(density*20);
			}else {
				params1.width = width/4-(int)(density*20);
			}
			params1.height = (int) (density*2);
			params1.setMargins((int)(density*10), 0, (int)(density*10), 0);
			tvBar.setLayoutParams(params1);
			llContainer1.addView(tvBar, i);

			Fragment fragment = new WarningFragment();
			if (TextUtils.equals(dto.showType, CONST.DOCUMENT)) {
				fragment = new PdfListFragment();
			}else if (TextUtils.equals(dto.showType, CONST.WARNING)) {
				fragment = new WarningFragment();
			}
			Bundle bundle = new Bundle();
			bundle.putString(CONST.WEB_URL, dto.dataUrl);
			fragment.setArguments(bundle);
			fragments.add(fragment);
		}

		viewPager = findViewById(R.id.viewPager);
		viewPager.setSlipping(true);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
	}

	public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			if (llContainer != null) {
				for (int i = 0; i < llContainer.getChildCount(); i++) {
					TextView tvName = (TextView) llContainer.getChildAt(i);
					if (i == arg0) {
						tvName.setTextColor(getResources().getColor(R.color.colorPrimary));
					}else {
						tvName.setTextColor(getResources().getColor(R.color.text_color3));
					}
				}

				if (llContainer.getChildCount() > 4) {
					hScrollView1.smoothScrollTo(width/4*arg0, 0);
				}

			}

			if (llContainer1 != null) {
				for (int i = 0; i < llContainer1.getChildCount(); i++) {
					TextView tvBar = (TextView) llContainer1.getChildAt(i);
					if (i == arg0) {
						tvBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
					}else {
						tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
					}
				}
			}

			ColumnData dto = data.child.get(arg0);
			if (TextUtils.equals(dto.showType, CONST.WARNING)) {
				OkHttpWarning(dto.dataUrl);
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
	 * @param flag false为显示map，true为显示list
	 */
	private void startAnimation(boolean flag, final RelativeLayout reList) {
		//列表动画
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation;
		if (!flag) {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f,
					Animation.RELATIVE_TO_SELF,0f);
		}else {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f);
		}
		animation.setDuration(400);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		reList.startAnimation(animationSet);
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				reList.clearAnimation();
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivExpand:
			LayoutParams params = mapView.getLayoutParams();
			if (!isExpand) {
				ivExpand.setImageResource(R.drawable.iv_collose);
				params.width = LayoutParams.MATCH_PARENT;
				params.height = LayoutParams.MATCH_PARENT;
				isExpand = true;
			}else {
				ivExpand.setImageResource(R.drawable.iv_expand);
				params.width = LayoutParams.MATCH_PARENT;
				params.height = (int) CommonUtil.dip2px(getApplicationContext(), 300);
				isExpand = false;
			}
			mapView.setLayoutParams(params);
			break;

		default:
			break;
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (mapView != null) {
			mapView.onResume();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
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
