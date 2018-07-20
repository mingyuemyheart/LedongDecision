package com.cxwl.shawn.wuzhishan.decision.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.dto.MinuteFallDto;
import com.cxwl.shawn.wuzhishan.decision.dto.ShawnRainDto;
import com.cxwl.shawn.wuzhishan.decision.dto.WeatherDto;
import com.cxwl.shawn.wuzhishan.decision.manager.CaiyunManager;
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;
import com.cxwl.shawn.wuzhishan.decision.view.MinuteFallView;

import net.tsz.afinal.FinalBitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 逐小时预报
 */
public class MinuteFragment extends Fragment implements OnClickListener, CaiyunManager.RadarListener,
        OnMapClickListener, OnGeocodeSearchListener, AMapLocationListener {
	
	private MapView mMapView;
	private AMap aMap;
	private List<MinuteFallDto> mList = new ArrayList<>();
	private List<MinuteFallDto> images = new ArrayList<>();
	private GroundOverlay mOverlay = null;
	private CaiyunManager mRadarManager;
	private RadarThread mRadarThread;
	private static final int HANDLER_SHOW_RADAR = 1;
	private static final int HANDLER_PROGRESS = 2;
	private static final int HANDLER_LOAD_FINISHED = 3;
	private static final int HANDLER_PAUSE = 4;
	private LinearLayout llSeekBar = null;
	private ImageView ivPlay = null;
	private SeekBar seekBar = null;
	private TextView tvTime = null;
    private Marker clickMarker = null;
	private GeocodeSearch geocoderSearch = null;
	private TextView tvAddr = null;//地址信息
	private TextView tvRain = null;//降雨信息
	private LinearLayout llContainer3 = null;
	private int width = 0;
	private LinearLayout llLegend = null;
	private ImageView ivRank = null;
	private ImageView ivLegend = null;
    private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
    private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
    private String proName = "";
    
    //降水实况
    private ImageView ivSwitch = null;
    private TextView tvLayerName = null;
    private ImageView ivChart = null;
	private List<ShawnRainDto> nameList = new ArrayList<>();
	private List<Text> texts = new ArrayList<>();//等值线
	private List<Text> cityNames = new ArrayList<>();//城市名称
	private List<Circle> circles = new ArrayList<>();//城市名称下方黑点
	private List<Polygon> polygons = new ArrayList<>();//降水图层
	private List<Polyline> polylines = new ArrayList<>();//行政区划边界线
	private String layerResult = null;//降水图层数据
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_minute, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initMap(savedInstanceState, view);
		initWidget(view);
	}

	private void initMap(Bundle bundle, View view) {
		mMapView = view.findViewById(R.id.map);
		mMapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mMapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.zoomTo(8.0f));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMapClickListener(this);

		TextView tvMapNumber = view.findViewById(R.id.tvMapNumber);
		tvMapNumber.setText(aMap.getMapContentApprovalNumber());
	}

	private void initWidget(View view) {
		ivPlay = view.findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		seekBar = view.findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(seekbarListener);
		tvTime = view.findViewById(R.id.tvTime);
		llSeekBar = view.findViewById(R.id.llSeekBar);
		tvAddr = view.findViewById(R.id.tvAddr);
		tvRain = view.findViewById(R.id.tvRain);
		llLegend = view.findViewById(R.id.llLegend);
		ivRank = view.findViewById(R.id.ivRank);
		ivRank.setOnClickListener(this);
		ivLegend = view.findViewById(R.id.ivLegend);
		llContainer3 = view.findViewById(R.id.llContainer3);
		tvLayerName = view.findViewById(R.id.tvLayerName);
		ivChart = view.findViewById(R.id.ivChart);
		ivSwitch = view.findViewById(R.id.ivSwitch);
		ivSwitch.setOnClickListener(this);
		
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		
		geocoderSearch = new GeocodeSearch(getActivity());
		geocoderSearch.setOnGeocodeSearchListener(this);
		
		mRadarManager = new CaiyunManager(getActivity());
		
		startLocation();
	}
	
	/**
	 * 开始定位
	 */
	private void startLocation() {
		mLocationOption = new AMapLocationClientOption();//初始化定位参数
        mLocationClient = new AMapLocationClient(getActivity());//初始化定位
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
        mLocationOption.setWifiActiveScan(true);//设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();//启动定位
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getErrorCode() == 0) {
            LatLng latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8.0f));
            addMarkerToMap(latLng);

            proName = amapLocation.getProvince();
            if (proName.contains("海南")) {//海南境内
                ivSwitch.setImageResource(R.drawable.iv_radar_chart);
                ivChart.setVisibility(View.VISIBLE);
                tvLayerName.setVisibility(View.VISIBLE);
                llSeekBar.setVisibility(View.GONE);
                llLegend.setVisibility(View.GONE);
            }else {
                ivSwitch.setImageResource(R.drawable.iv_hour_rain);
                ivChart.setVisibility(View.GONE);
                tvLayerName.setVisibility(View.GONE);
                llSeekBar.setVisibility(View.VISIBLE);
                llLegend.setVisibility(View.VISIBLE);
            }

            OkHttpRainFact();
            OkHttpCaiyunRain();
        }
	}

	private void addMarkerToMap(LatLng latLng) {
		MarkerOptions options = new MarkerOptions();
		options.position(latLng);
		options.anchor(0.5f, 0.5f);
		Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_map_location),
				(int)(CommonUtil.dip2px(getActivity(), 15)), (int)(CommonUtil.dip2px(getActivity(), 15)));
		if (bitmap != null) {
			options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
		}else {
			options.icon(BitmapDescriptorFactory.fromResource(R.drawable.iv_map_location));
		}
		clickMarker = aMap.addMarker(options);
		OkHttpMinute(latLng.longitude, latLng.latitude);
		searchAddrByLatLng(latLng.latitude, latLng.longitude);
	}
	
	/**
	 * 异步加载一小时内降雨、或降雪信息
	 * @param lng
	 * @param lat
	 */
	private void OkHttpMinute(double lng, double lat) {
		final String url = "http://api.caiyunapp.com/v2/HyTVV5YAkoxlQ3Zd/"+lng+","+lat+"/forecast";
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
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("result")) {
												JSONObject obj = object.getJSONObject("result");
												if (!obj.isNull("minutely")) {
													JSONObject objMin = obj.getJSONObject("minutely");
													if (!objMin.isNull("description")) {
														String rain = objMin.getString("description");
														if (!TextUtils.isEmpty(rain)) {
															tvRain.setText(rain.replace("小彩云", ""));
															tvRain.setVisibility(View.VISIBLE);
														}else {
															tvRain.setVisibility(View.GONE);
														}
													}
													if (!objMin.isNull("precipitation_2h")) {
														JSONArray array = objMin.getJSONArray("precipitation_2h");
														int size = array.length();
														List<WeatherDto> minuteList = new ArrayList<>();
														for (int i = 0; i < size; i++) {
															WeatherDto dto = new WeatherDto();
															dto.minuteFall = (float) array.getDouble(i);
															minuteList.add(dto);
														}

														MinuteFallView minuteFallView = new MinuteFallView(getActivity());
														minuteFallView.setData(minuteList, tvRain.getText().toString());
														llContainer3.removeAllViews();
														llContainer3.addView(minuteFallView, width, (int)(CommonUtil.dip2px(getActivity(), 120)));
													}
												}
											}
										}
									} catch (JSONException e1) {
										e1.printStackTrace();
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}
	
	private OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.setCurrent(seekBar.getProgress());
				mRadarThread.stopTracking();
			}
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.startTracking();
			}
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		}
	};
	
	@Override
	public void onMapClick(LatLng arg0) {
		if (clickMarker != null) {
			clickMarker.remove();
		}
		tvAddr.setText("");
		tvRain.setText("");
		addMarkerToMap(arg0);
	}
	
	/**
	 * 通过经纬度获取地理位置信息
	 * @param lat
	 * @param lng
	 */
	private void searchAddrByLatLng(double lat, double lng) {
		//latLonPoint参数表示一个Latlng，第二参数表示范围多少米，GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系   
		RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(lat, lng), 200, GeocodeSearch.AMAP);
    	geocoderSearch.getFromLocationAsyn(query); 
	}
	
	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
	}
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		if (result != null && result.getRegeocodeAddress() != null && result.getRegeocodeAddress().getFormatAddress() != null) {
			String addr = result.getRegeocodeAddress().getFormatAddress();
			if (!TextUtils.isEmpty(addr)) {
				tvAddr.setText(addr);
			}
		}
	}

	/**
	 * 彩云降水图片集合
	 */
	private void OkHttpCaiyunRain() {
		final String url = "http://api.tianqi.cn:8070/v1/img.py";
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
										if (!obj.isNull("status")) {
											if (obj.getString("status").equals("ok")) {//鎴愬姛
												if (!obj.isNull("radar_img")) {
													mList.clear();
													JSONArray array = new JSONArray(obj.getString("radar_img"));
													for (int i = 0; i < array.length(); i++) {
														JSONArray array0 = array.getJSONArray(i);
														MinuteFallDto dto = new MinuteFallDto();
														dto.setImgUrl(array0.optString(0));
														dto.setTime(array0.optLong(1));
														JSONArray itemArray = array0.getJSONArray(2);
														dto.setP1(itemArray.optDouble(0));
														dto.setP2(itemArray.optDouble(1));
														dto.setP3(itemArray.optDouble(2));
														dto.setP4(itemArray.optDouble(3));
														mList.add(dto);
													}
													if (mList.size() > 0) {
														startDownLoadImgs(mList);
													}
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
			}
		}).start();
	}
	
	private void startDownLoadImgs(List<MinuteFallDto> list) {
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
 		mRadarManager.loadImagesAsyn(list, this);
	}
	
	@Override
	public void onResult(int result, List<MinuteFallDto> images) {
		mHandler.sendEmptyMessage(HANDLER_LOAD_FINISHED);
		if (result == CaiyunManager.RadarListener.RESULT_SUCCESSED) {
//			if (mRadarThread != null) {
//				mRadarThread.cancel();
//				mRadarThread = null;
//			}
//			mRadarThread = new RadarThread(images);
//			mRadarThread.start();
			
			this.images.clear();
			this.images.addAll(images);
			
			if (!proName.contains("海南")) {
				//把最新的一张降雨图片覆盖在地图上
				MinuteFallDto radar = images.get(images.size()-1);
				Message message = mHandler.obtainMessage();
				message.what = HANDLER_SHOW_RADAR;
				message.obj = radar;
				message.arg1 = 100;
				message.arg2 = 100;
				mHandler.sendMessage(message);
			}
		}
	}

	@Override
	public void onProgress(String url, int progress) {
		Message msg = new Message();
		msg.obj = progress;
		msg.what = HANDLER_PROGRESS;
		mHandler.sendMessage(msg);
	}
	
	private void showRadar(Bitmap bitmap, double p1, double p2, double p3, double p4) {
		BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
		LatLngBounds bounds = new LatLngBounds.Builder()
		.include(new LatLng(p3, p2))
		.include(new LatLng(p1, p4))
		.build();
		
		if (mOverlay == null) {
			mOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
				.anchor(0.5f, 0.5f)
				.positionFromBounds(bounds)
				.image(fromView)
				.transparency(0.0f));
		} else {
			mOverlay.setImage(null);
			mOverlay.setPositionFromBounds(bounds);
			mOverlay.setImage(fromView);
		}
		aMap.runOnDrawFrame();
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case HANDLER_SHOW_RADAR:
				if (msg.obj != null) {
					MinuteFallDto dto = (MinuteFallDto) msg.obj;
					if (dto.getPath() != null) {
						Bitmap bitmap = BitmapFactory.decodeFile(dto.getPath());
						if (bitmap != null) {
							showRadar(bitmap, dto.getP1(), dto.getP2(), dto.getP3(), dto.getP4());
						}
					}
					changeProgress(dto.getTime(), msg.arg2, msg.arg1);
				}
				break;
			case HANDLER_PROGRESS:
//				if (mDialog != null) {
//					if (msg.obj != null) {
//						int progress = (Integer) msg.obj;
//						mDialog.setPercent(progress);
//					}
//				}
				break;
			case HANDLER_LOAD_FINISHED:
//				cancelDialog();
//				llSeekBar.setVisibility(View.VISIBLE);
//				llLegend.setVisibility(View.VISIBLE);
				break;
			case HANDLER_PAUSE:
				if (ivPlay != null) {
					ivPlay.setImageResource(R.drawable.iv_play);
				}
				break;
			default:
				break;
			}

		};
	};

	private class RadarThread extends Thread {
		static final int STATE_NONE = 0;
		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private List<MinuteFallDto> images;
		private int state;
		private int index;
		private int count;
		private boolean isTracking = false;

		public RadarThread(List<MinuteFallDto> images) {
			this.images = images;
			this.count = images.size();
			this.index = 0;
			this.state = STATE_NONE;
			this.isTracking = false;
		}

		public int getCurrentState() {
			return state;
		}

		@Override
		public void run() {
			super.run();
			this.state = STATE_PLAYING;
			while (true) {
				if (state == STATE_CANCEL) {
					break;
				}
				if (state == STATE_PAUSE) {
					continue;
				}
				if (isTracking) {
					continue;
				}
				sendRadar();
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void sendRadar() {
			if (index >= count || index < 0) {
				index = 0;

//				if (mRadarThread != null) {
//					mRadarThread.pause();
//
//					Message message = mHandler.obtainMessage();
//					message.what = HANDLER_PAUSE;
//					mHandler.sendMessage(message);
//					if (seekBar != null) {
//						seekBar.setProgress(100);
//					}
//				}
			}else {
				MinuteFallDto radar = images.get(index);
				Message message = mHandler.obtainMessage();
				message.what = HANDLER_SHOW_RADAR;
				message.obj = radar;
				message.arg1 = count - 1;
				message.arg2 = index ++;
				mHandler.sendMessage(message);
			}
		}

		public void cancel() {
			this.state = STATE_CANCEL;
		}
		public void pause() {
			this.state = STATE_PAUSE;
		}
		public void play() {
			this.state = STATE_PLAYING;
		}

		public void setCurrent(int index) {
			this.index = index;
		}

		public void startTracking() {
			isTracking = true;
		}

		public void stopTracking() {
			isTracking = false;
			if (this.state == STATE_PAUSE) {
				sendRadar();
			}
		}
	}

	private void changeProgress(long time, int progress, int max) {
		if (seekBar != null) {
			seekBar.setMax(max);
			seekBar.setProgress(progress);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String value = time + "000";
		Date date = new Date(Long.valueOf(value));
		tvTime.setText(sdf.format(date));
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ivPlay) {
			if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PLAYING) {
				mRadarThread.pause();
				ivPlay.setImageResource(R.drawable.iv_play);
			} else if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PAUSE) {
				mRadarThread.play();
				ivPlay.setImageResource(R.drawable.iv_pause);
			} else if (mRadarThread == null) {
				ivPlay.setImageResource(R.drawable.iv_pause);
				if (mRadarThread != null) {
					mRadarThread.cancel();
					mRadarThread = null;
				}
				if (!images.isEmpty()) {
					mRadarThread = new RadarThread(images);
					mRadarThread.start();
				}
			}
		}else if (v.getId() == R.id.ivRank) {
			if (ivLegend.getVisibility() == View.VISIBLE) {
				ivLegend.setVisibility(View.GONE);
			}else {
				ivLegend.setVisibility(View.VISIBLE);
			}
		}else if (v.getId() == R.id.ivSwitch) {
			if (llSeekBar.getVisibility() == View.VISIBLE) {
				ivSwitch.setImageResource(R.drawable.iv_radar_chart);
				ivChart.setVisibility(View.VISIBLE);
				tvLayerName.setVisibility(View.VISIBLE);
				llSeekBar.setVisibility(View.GONE);
				llLegend.setVisibility(View.GONE);
				if (mOverlay != null) {
					mOverlay.remove();
					mOverlay = null;
				}
				if (mRadarThread != null) {
					mRadarThread.cancel();
					mRadarThread = null;
				}
				if (layerResult != null) {
					drawDataToMap(layerResult);
				}
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(19.211397,109.795324), 7.8f));
			}else {
				ivSwitch.setImageResource(R.drawable.iv_hour_rain);
				ivChart.setVisibility(View.GONE);
				tvLayerName.setVisibility(View.GONE);
				llSeekBar.setVisibility(View.VISIBLE);
				llLegend.setVisibility(View.VISIBLE);
				removeCityNames();
				removeTexts();
				removePolylines();
				removePolygons();

				if (!images.isEmpty()) {
					//把最新的一张降雨图片覆盖在地图上
					MinuteFallDto radar = images.get(images.size()-1);
					Message message = mHandler.obtainMessage();
					message.what = HANDLER_SHOW_RADAR;
					message.obj = radar;
					message.arg1 = 100;
					message.arg2 = 100;
					mHandler.sendMessage(message);
				}
			}
		}
	}

	/**
	 * 请求降水实况
	 */
	private void OkHttpRainFact() {
		final String url = "http://59.50.130.88:8888/decision-admin/dates/getallid?tim=";
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
										if (!obj.isNull("times")) {
											JSONArray array = new JSONArray(obj.getString("times"));
											for (int i = 0; i < array.length(); i++) {
												JSONObject itemObj = array.getJSONObject(i);
												if (!itemObj.isNull("timeString")) {
													String timeString = itemObj.getString("timeString");
													if (i == 0 && timeString != null) {
														tvLayerName.setText(timeString+"降水实况");
														if (proName.contains("海南")) {
															tvLayerName.setVisibility(View.VISIBLE);
														}
													}
												}
											}
										}

										if (!obj.isNull("cutlineUrl")) {
											FinalBitmap finalBitmap = FinalBitmap.create(getActivity());
											finalBitmap.display(ivChart, obj.getString("cutlineUrl"), null, 0);
											if (proName.contains("海南")) {
												ivChart.setVisibility(View.VISIBLE);
											}
										}

										if (!obj.isNull("dataUrl")) {
											String dataUrl = obj.getString("dataUrl");
											if (!TextUtils.isEmpty(dataUrl)) {
												OkHttpLayer(dataUrl);
											}
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}else {
									drawCityName();
									removePolygons();
//				progressBar.setVisibility(View.GONE);
//				tvToast.setVisibility(View.VISIBLE);
//				new Handler().postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						tvToast.setVisibility(View.GONE);
//					}
//				}, 1000);
								}
							}
						});
					}
				});
			}
		}).start();
	}

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
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									layerResult = result;
									if (result != null) {
										if (proName.contains("海南")) {
											drawDataToMap(result);
										}
									}
//			else {
//				drawCityName();
//				removePolygons();
////				progressBar.setVisibility(View.GONE);
////				tvToast.setVisibility(View.VISIBLE);
////				new Handler().postDelayed(new Runnable() {
////					@Override
////					public void run() {
////						tvToast.setVisibility(View.GONE);
////					}
////				}, 1000);
//			}
								}
							}
						});
					}
				});
			}
		}).start();
	}

	private void removePolygons() {
		for (int i = 0; i < polygons.size(); i++) {
			polygons.get(i).remove();
		}
		polygons.clear();
	}

	private void removeTexts() {
		for (int i = 0; i < texts.size(); i++) {
			texts.get(i).remove();
		}
		texts.clear();
	}

	/**
	 * 回执区域
	 */
	private void drawDataToMap(String result) {
		if (TextUtils.isEmpty(result) || aMap == null) {
			return;
		}
		removeTexts();
		removePolygons();

		try {
			JSONObject obj = new JSONObject(result);
			JSONArray array = obj.getJSONArray("l");
			int length = array.length();
//			if (length > 200) {
//				length = 200;
//			}
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
					polygons.add(polygon);
				}

				if (!itemObj.isNull("v")) {
					int v = itemObj.getInt("v");
					TextOptions options = new TextOptions();
					options.position(new LatLng(centerLat, centerLng));
					options.fontColor(Color.BLACK);
					options.fontSize(20);
					options.text(v+"");
					options.backgroundColor(Color.TRANSPARENT);
					Text text = aMap.addText(options);
					texts.add(text);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Message msg = new Message();
		msg.what = 100;
		handler.sendMessage(msg);
	}

	private void removeCityNames() {
		for (int i = 0; i < cityNames.size(); i++) {
			cityNames.get(i).remove();
		}
		cityNames.clear();

		for (int i = 0; i < circles.size(); i++) {
			circles.get(i).remove();
		}
		circles.clear();
	}

	private void removePolylines() {
		for (int i = 0; i < polylines.size(); i++) {
			polylines.get(i).remove();
		}
		polylines.clear();
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 100:
//				tvHistory.setVisibility(View.VISIBLE);
//				tvDetail.setVisibility(View.VISIBLE);
//				ivChart.setVisibility(View.VISIBLE);
//				progressBar.setVisibility(View.GONE);
				drawCityName();
				break;
			case 101:
				for (int i = 0; i < nameList.size(); i++) {
					ShawnRainDto dto = nameList.get(i);
					TextOptions options = new TextOptions();
					options.position(new LatLng(dto.lat+0.05, dto.lng));
					options.fontColor(Color.BLACK);
					options.fontSize(20);
					options.text(dto.cityName);
					options.backgroundColor(Color.TRANSPARENT);
					Text text = aMap.addText(options);
					cityNames.add(text);
					
					CircleOptions cOptions = new CircleOptions();
					cOptions.center(new LatLng(dto.lat, dto.lng));
					cOptions.fillColor(Color.BLACK);
					cOptions.radius(50.0f);
					Circle circle = aMap.addCircle(cOptions);
					circles.add(circle);
				}
				
				removePolylines();
				CommonUtil.drawAllDistrict(getActivity(), aMap, 0xff72e5f3, polylines);
				break;

			default:
				break;
			}
		};
	};
	
	/**
     * 异步解析五中天气现象数据并绘制在地图上
     */
	private void drawCityName() {
		removeCityNames();
		if (aMap == null) {
			return;
		}
		String result = CommonUtil.getFromAssets(getActivity(), "hnGeo2.json");
		if (!TextUtils.isEmpty(result)) {
			AsynLoadTaskDistrict task = new AsynLoadTaskDistrict(result);  
			task.execute();
		}
	}
	
	private class AsynLoadTaskDistrict extends AsyncTask<Void, Void, Void> {
		
		private String result;
		
		private AsynLoadTaskDistrict(String result) {
			this.result = result;
		}

		@Override
		protected void onPreExecute() {
			//开始执行
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			//执行完毕
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				JSONObject obj = new JSONObject(result);
//				if (!obj.isNull("polyline")) {
//					String polyline = obj.getString("polyline");
//					String[] array1 = polyline.split("\\|");
//					for (int i = 0; i < 1; i++) {
//						String[] array2 = array1[i].split(";");
//						PolygonOptions polylineOption = new PolygonOptions();
//						polylineOption.strokeColor(0xff999999);
//						for (int j = 0; j < array2.length; j++) {
//							String[] array3 = array2[i].split(",");
//							if (!TextUtils.isEmpty(array3[0]) && !TextUtils.isEmpty(array3[1])) {
//								double lng = Double.valueOf(array3[0]);
//								double lat = Double.valueOf(array3[1]);
//								polylineOption.add(new LatLng(lat, lng));
//							}
//						}
//						aMap.addPolygon(polylineOption);
//					}
//				}
				if (!obj.isNull("districts")) {
					JSONArray array = obj.getJSONArray("districts");
					nameList.clear();
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
						nameList.add(dto);
					}
					
					Message msg = new Message();
					msg.what = 101;
					handler.sendMessage(msg);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mMapView != null) {
			mMapView.onDestroy();
		}
		if (mRadarManager != null) {
			mRadarManager.onDestory();
		}
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
	}
	
}
