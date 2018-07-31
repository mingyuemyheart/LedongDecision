package com.cxwl.shawn.wuzhishan.decision.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.adapter.WarningAdapter;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.dto.WarningDto;
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;

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
 * 预警信息
 * @author shawn_sun
 *
 */
public class WarningActivity extends BaseActivity implements OnClickListener, OnMapClickListener,
        OnMarkerClickListener, InfoWindowAdapter {
	
	private Context mContext;
	private LinearLayout llBack;
	private TextView tvTitle;
	private ImageView ivList,ivRefresh;
	private MapView mapView;//高德地图
	private AMap aMap;//高德地图
	private Marker selectMarker;
	private List<WarningDto> warningList = new ArrayList<>();
	private List<Marker> markers = new ArrayList<>();
	private RelativeLayout reList;
	private ListView cityListView;
	private WarningAdapter cityAdapter;
	private List<WarningDto> mList = new ArrayList<>();
	private EditText etSearch;
	private List<WarningDto> searchList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_warning);
		mContext = this;
		initAmap(savedInstanceState);
		initWidget();
		initListView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		ivList = findViewById(R.id.ivList);
		ivList.setOnClickListener(this);
		ivRefresh = findViewById(R.id.ivRefresh);
		ivRefresh.setOnClickListener(this);
		etSearch = findViewById(R.id.etSearch);
		etSearch.addTextChangedListener(watcher);
		reList = findViewById(R.id.reList);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}

		OkHttpWarning();
    }

	private TextWatcher watcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			searchList.clear();
			if (!TextUtils.isEmpty(arg0.toString().trim())) {
				for (int i = 0; i < warningList.size(); i++) {
					WarningDto data = warningList.get(i);
					if (data.name.contains(arg0.toString().trim())) {
						searchList.add(data);
					}
				}
				mList.clear();
				mList.addAll(searchList);
				if (cityAdapter != null) {
					cityAdapter.notifyDataSetChanged();
				}
			}else {
				mList.clear();
				mList.addAll(warningList);
				if (cityAdapter != null) {
					cityAdapter.notifyDataSetChanged();
				}
			}
		}
	};
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		cityListView = findViewById(R.id.listView);
		cityAdapter = new WarningAdapter(mContext, mList, false);
		cityListView.setAdapter(cityAdapter);
		cityListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				WarningDto data = mList.get(arg2);
				Intent intentDetail = new Intent(mContext, WarningDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", data);
				intentDetail.putExtras(bundle);
				startActivity(intentDetail);
			}
		});
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
	}
	
	/**
	 * 获取预警信息
	 */
	private void OkHttpWarning() {
		removeMarkers(markers);
		final String url = getIntent().getStringExtra(CONST.WEB_URL);
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
											warningList.clear();
											mList.clear();
											JSONArray jsonArray = object.getJSONArray("w");
											for (int i = 0; i < jsonArray.length(); i++) {
												WarningDto dto = new WarningDto();
												JSONObject itemObj = jsonArray.getJSONObject(i);
												String w1 = itemObj.getString("w1");
												String w2 = itemObj.getString("w2");
												String w4 = itemObj.getString("w4");
												String w5 = itemObj.getString("w5");
												String w6 = itemObj.getString("w6");
												String w7 = itemObj.getString("w7");
												String w8 = itemObj.getString("w8");
												String w9 = itemObj.getString("w9");
												String w11 = itemObj.getString("w11");

												dto.name = w1+w2+"发布"+w5+w7+"预警";
												dto.time = w8;
												dto.type = "icon_warning_"+w4;
												dto.color = w6;
												dto.content = w9;

												String[] latLngs = getResources().getStringArray(R.array.wuzhishan_hotCity);
												for (int j = 0; j < latLngs.length; j++) {
													String[] itemArray = latLngs[j].split(",");
													String value;
													if (!TextUtils.isEmpty(w2)) {
														value = w2;
													}else {
														value = w11;
													}
													if (value.contains(itemArray[1]) || itemArray[1].contains(value)) {
														if (!TextUtils.isEmpty(itemArray[3]) && !TextUtils.isEmpty(itemArray[2])) {
															dto.lat = itemArray[3];
															dto.lng = itemArray[2];
															break;
														}
													}
												}

												warningList.add(dto);
												mList.add(dto);
											}
											addMarkersToMap(warningList, markers);
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
	 * 移除地图上指定marker
	 * @param markers
	 */
	private void removeMarkers(List<Marker> markers) {
		for (int i = 0; i < markers.size(); i++) {
			final Marker marker = markers.get(i);
			ScaleAnimation animation = new ScaleAnimation(1,0,1,0);
			animation.setInterpolator(new LinearInterpolator());
			animation.setDuration(300);
			marker.setAnimation(animation);
			marker.startAnimation();
			marker.setAnimationListener(new ScaleAnimation.AnimationListener() {
				@Override
				public void onAnimationStart() {
				}
				@Override
				public void onAnimationEnd() {
					marker.remove();
				}
			});
		}
		markers.clear();
	}
	
	/**
	 * 在地图上添加marker
	 */
	private void addMarkersToMap(List<WarningDto> list, List<Marker> markerList) {
		LatLngBounds.Builder builder = LatLngBounds.builder();
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < list.size(); i++) {
			WarningDto dto = list.get(i);
	    	MarkerOptions optionsTemp = new MarkerOptions();
	    	optionsTemp.title(dto.lat);
	    	optionsTemp.snippet(dto.lng);
	    	optionsTemp.anchor(0.5f, 0.5f);
	    	if (!TextUtils.isEmpty(dto.lat) && !TextUtils.isEmpty(dto.lng)) {
	    		optionsTemp.position(new LatLng(Double.valueOf(dto.lat), Double.valueOf(dto.lng)));
				builder.include(new LatLng(Double.parseDouble(dto.lat), Double.parseDouble(dto.lng)));
	    	}
	    	
	    	View view = inflater.inflate(R.layout.marker_warning_icon, null);
	    	ImageView ivMarker = view.findViewById(R.id.ivMarker);
	    	
	    	Bitmap bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+dto.color+CONST.imageSuffix);
			if (bitmap == null) {
				bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+dto.color+CONST.imageSuffix);
			}
	    	ivMarker.setImageBitmap(bitmap);
	    	optionsTemp.icon(BitmapDescriptorFactory.fromView(view));
	    	
			Marker marker = aMap.addMarker(optionsTemp);
			markerList.add(marker);
			ScaleAnimation animation = new ScaleAnimation(0,1,0,1);
			animation.setInterpolator(new LinearInterpolator());
			animation.setDuration(300);
			marker.setAnimation(animation);
			marker.startAnimation();
		}

		aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));

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
		View view = inflater.inflate(R.layout.marker_info_warning_icon, null);
		ListView mListView;
		WarningAdapter mAdapter;
		final List<WarningDto> infoList = new ArrayList<>();
		
		addInfoList(warningList, marker, infoList);
		
		mListView = view.findViewById(R.id.listView);
		mAdapter = new WarningAdapter(mContext, infoList, true);
		mListView.setAdapter(mAdapter);
		LayoutParams params = mListView.getLayoutParams();
		if (infoList.size() == 1) {
			params.height = (int) CommonUtil.dip2px(mContext, 50);
		}else if (infoList.size() == 2) {
			params.height = (int) CommonUtil.dip2px(mContext, 100);
		}else if (infoList.size() > 2){
			params.height = (int) CommonUtil.dip2px(mContext, 150);
		}
		mListView.setLayoutParams(params);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentDetail(infoList.get(arg2));
			}
		});
		return view;
	}
	
	private void intentDetail(WarningDto data) {
		Intent intentDetail = new Intent(mContext, WarningDetailActivity.class);
		intentDetail.putExtra("data", data);
		startActivity(intentDetail);
	}
	
	private void addInfoList(List<WarningDto> list, Marker marker, List<WarningDto> infoList) {
		for (int i = 0; i < list.size(); i++) {
			WarningDto dto = list.get(i);
			if (TextUtils.equals(marker.getTitle(), dto.lat) && TextUtils.equals(marker.getSnippet(), dto.lng)) {
				infoList.add(dto);
			}
		}
	}
	
	@Override
	public View getInfoWindow(Marker arg0) {
		return null;
	}
	
	/**
	 * @param flag false为显示map，true为显示list
	 */
	private void startAnimation(boolean flag, final RelativeLayout reList) {
		//列表动画
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation;
		if (flag == false) {
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (reList.getVisibility() == View.VISIBLE) {
				startAnimation(true, reList);
				reList.setVisibility(View.GONE);
				ivList.setImageResource(R.drawable.iv_warning_list_unselected);
				return false;
			}else {
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			if (reList.getVisibility() == View.VISIBLE) {
				startAnimation(true, reList);
				reList.setVisibility(View.GONE);
				ivList.setImageResource(R.drawable.iv_warning_list_unselected);
			}else {
				finish();
			}
			break;
		case R.id.ivList:
			if (reList.getVisibility() == View.GONE) {
				startAnimation(false, reList);
				reList.setVisibility(View.VISIBLE);
				ivList.setImageResource(R.drawable.iv_warning_list_selected);
			}else {
				startAnimation(true, reList);
				reList.setVisibility(View.GONE);
				ivList.setImageResource(R.drawable.iv_warning_list_unselected);
			}
			break;
		case R.id.ivRefresh:
			OkHttpWarning();
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
