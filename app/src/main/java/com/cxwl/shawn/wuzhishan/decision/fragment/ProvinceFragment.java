package com.cxwl.shawn.wuzhishan.decision.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.activity.ForecastActivity;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.dto.WeatherDto;
import com.cxwl.shawn.wuzhishan.decision.util.FetchWeather;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;

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
 * 全省预报
 */
public class ProvinceFragment extends Fragment implements OnMarkerClickListener {
	
	private TextView tvTitle,tvMapNumber;
	private MapView mMapView;
	private AMap aMap;
	private List<WeatherDto> mList = new ArrayList<>();
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy年MM月dd日HH时");

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_province, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
		initMap(savedInstanceState, view);
	}

	private void initWidget(View view){
		tvTitle = view.findViewById(R.id.tvTitle);
		tvMapNumber = view.findViewById(R.id.tvMapNumber);
	}
	
	private void initMap(Bundle bundle, View view) {
		mMapView = view.findViewById(R.id.map);
		mMapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mMapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(19.05, 109.83),8.0f));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMarkerClickListener(this);

		tvMapNumber.setText(aMap.getMapContentApprovalNumber());

		OkHttpStations();
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		Intent intent = new Intent(getActivity(), ForecastActivity.class);
		intent.putExtra("cityId", marker.getTitle());
		intent.putExtra("cityName", marker.getSnippet());
		intent.putExtra("lat", marker.getPosition().latitude);
		intent.putExtra("lng", marker.getPosition().longitude);
		startActivity(intent);
		return true;
	}
	
	/**
	 * 获取城市的位置信息
	 */
	private void OkHttpStations() {
		final String url = getArguments().getString(CONST.WEB_URL);
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
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										mList.clear();
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("list")) {
											JSONArray array = obj.getJSONArray("list");
											for (int i = 0; i < array.length(); i++) {
												JSONObject itemObj = array.getJSONObject(i);
												WeatherDto dto = new WeatherDto();
												if (!itemObj.isNull("name")) {
													dto.cityName = itemObj.getString("name");
												}
												if (!itemObj.isNull("city_id")) {
													dto.cityId = itemObj.getString("city_id");
												}
												if (!itemObj.isNull("geo")) {
													JSONArray geoArray = itemObj.getJSONArray("geo");
													dto.lat = geoArray.getDouble(1);
													dto.lng = geoArray.getDouble(0);
												}
												mList.add(dto);
											}
										}

										if (mList.size() > 0) {
											for (int i = 0; i < mList.size(); i++) {
												WeatherDto dto = mList.get(i);
												getAllWeather(dto);
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
			}
		}).start();

	}

	//获取所有天气信息
	private void getAllWeather(final WeatherDto dto) {
		FetchWeather fetch = new FetchWeather();
		fetch.perform(dto.cityId, "all");
		fetch.setOnFetchWeatherListener(new FetchWeather.OnFetchWeatherListener() {
			@Override
			public void onFetchWeather(final String result) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONArray array = new JSONArray(result);

								//实况信息
								if (TextUtils.equals(dto.cityId, "101310101")) {
									JSONObject fact = array.getJSONObject(0);
									if (!fact.isNull("l")) {
										JSONObject lObj = fact.getJSONObject("l");
										if (!lObj.isNull("l13")) {
											String time = lObj.getString("l13");
											if (!TextUtils.isEmpty(time)) {
												try {
													tvTitle.setText(sdf3.format(sdf2.parse(time))+"发布的市县未来24小时预报");
												} catch (ParseException e) {
													e.printStackTrace();
												}
											}
										}
									}
								}

								//逐小时预报信息
								JSONObject hour = array.getJSONObject(3);
								if (!hour.isNull("jh")) {
									JSONArray jhArray = hour.getJSONArray("jh");
									JSONObject itemObj = jhArray.getJSONObject(0);
									dto.factPheCode = itemObj.getString("ja");
									dto.factTemp = itemObj.getString("jb");

									addMarkers(dto);
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

    private void addMarkers(WeatherDto dto) {
    	LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		MarkerOptions options = new MarkerOptions();
		options.title(dto.cityId);
		options.snippet(dto.cityName);
		options.position(new LatLng(dto.lat, dto.lng));
		View mView = inflater.inflate(R.layout.layout_travel_marker, null);
		TextView tvName = mView.findViewById(R.id.tvName);
		TextView tvTemp = mView.findViewById(R.id.tvTemp);
		ImageView ivPhe = mView.findViewById(R.id.ivPhe);
		if (dto.cityName != null) {
			tvName.setText(dto.cityName);
		}
		if (dto.factTemp != null) {
			tvTemp.setText(dto.factTemp+"℃");
		}
		if (dto.factPheCode != null) {
			String currentTime = sdf1.format(new Date().getTime());
			int hour = Integer.valueOf(currentTime);
			Drawable drawable;
			if (hour >= 6 && hour < 18) {
				drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
			}else {
				drawable = getResources().getDrawable(R.drawable.phenomenon_drawable_night);
			}
			drawable.setLevel(Integer.valueOf(dto.factPheCode));
			if (drawable != null) {
				ivPhe.setBackground(drawable);
			}

		}
		options.icon(BitmapDescriptorFactory.fromView(mView));
		aMap.addMarker(options);
		
		LatLngBounds bounds = new LatLngBounds.Builder()
		.include(new LatLng(20.530793, 110.328859))
		.include(new LatLng(19.095361, 108.651775))
		.include(new LatLng(16.857606,112.350494))
		.include(new LatLng(19.54339,110.797648)).build();
		aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
    }
	
	/**
	 * 方法必须重写
	 */
	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

}
