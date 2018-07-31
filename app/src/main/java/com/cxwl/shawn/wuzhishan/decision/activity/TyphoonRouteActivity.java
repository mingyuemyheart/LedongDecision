package com.cxwl.shawn.wuzhishan.decision.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.adapter.TyphoonNameAdapter;
import com.cxwl.shawn.wuzhishan.decision.adapter.TyphoonYearAdapter;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.dto.MinuteFallDto;
import com.cxwl.shawn.wuzhishan.decision.dto.TyphoonDto;
import com.cxwl.shawn.wuzhishan.decision.dto.WindData;
import com.cxwl.shawn.wuzhishan.decision.dto.WindDto;
import com.cxwl.shawn.wuzhishan.decision.manager.CaiyunManager;
import com.cxwl.shawn.wuzhishan.decision.util.AuthorityUtil;
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;
import com.cxwl.shawn.wuzhishan.decision.util.SecretUrlUtil;
import com.cxwl.shawn.wuzhishan.decision.util.WeatherUtil;
import com.cxwl.shawn.wuzhishan.decision.view.WaitWindView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
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
 * 台风路径
 */
public class TyphoonRouteActivity extends BaseActivity implements OnClickListener, OnMapClickListener, AMapLocationListener, GeocodeSearch.OnGeocodeSearchListener,
        OnMarkerClickListener, InfoWindowAdapter, CaiyunManager.RadarListener, OnCameraChangeListener {

	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvTyphoonName = null;
	private MapView mapView = null;
	private AMap aMap = null;
	private ImageView ivLegend = null;//台风标注图
	private RelativeLayout reLegend = null;
	private ImageView ivLocation = null;
	private boolean isShowTime = false;//是否显示台风实况、预报时间
	private ImageView ivCancelLegend = null;
	private ImageView ivTyphoonList = null;
	private RelativeLayout reTyphoonList = null;
	private ImageView ivCancelList = null;
	private ListView yearListView = null;
	private TyphoonYearAdapter yearAdapter = null;
	private List<TyphoonDto> yearList = new ArrayList<>();
	private ListView nameListView = null;
	private TyphoonNameAdapter nameAdapter = null;
	private List<TyphoonDto> nameList = new ArrayList<>();//某一年所有台风
	private List<TyphoonDto> startList = new ArrayList<>();//某一年活跃台风
	private List<ArrayList<TyphoonDto>> pointsList = new ArrayList<>();//存放某一年所有活跃台风
	private RoadThread mRoadThread = null;//绘制台风点的线程
	private Marker clickMarker = null;//被点击的marker
	private Circle circle, circle2;//七级风圈和十级风圈
	private float zoom = 3.7f;
	private List<Polyline> fullLines = new ArrayList<>();//实线数据
	private List<Polyline> dashLines = new ArrayList<>();//虚线数据
	private List<Marker> markerPoints = new ArrayList<>();//台风点数据
	private List<Marker> markerTimes = new ArrayList<>();//预报点时间数据
	private ImageView ivTyphoonPlay = null;//台风回放按钮
	private int MSG_PAUSETYPHOON = 100;//暂停台风
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
	private ImageView ivTyphoonRadar = null;
	private ImageView ivTyphoonCloud = null;
	private ImageView ivTyphoonWind = null;
	private TextView tvFileTime = null;
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy年MM月dd日HH时");
	private SimpleDateFormat sdf4 = new SimpleDateFormat("dd日HH时");
	private SimpleDateFormat sdf5 = new SimpleDateFormat("MM月dd日HH时");
	private boolean isRadarOn = false;
	private boolean isCloudOn = false;
	private boolean isWindOn = false;
	private CaiyunManager mRadarManager = null;
	private List<MinuteFallDto> radarList = new ArrayList<>();
	private RadarThread mRadarThread = null;
	private static final int HANDLER_SHOW_RADAR = 1;
	private static final int HANDLER_LOAD_FINISHED = 3;
	private GroundOverlay radarOverlay = null;
	private GroundOverlay cloudOverlay = null;
	private Bitmap cloudBitmap = null;
	private RelativeLayout container = null;
	public RelativeLayout container2 = null;
	private WindData windData = null;
	private int width = 0, height = 0;
	private WaitWindView waitWindView = null;
	private boolean isHaveWindData = false;//是否已经加载完毕风场数据
	private List<Marker> factTimeMarkers = new ArrayList<>();
	private List<Marker> timeMarkers = new ArrayList<>();//预报时间markers
	private List<Marker> rotateMarkers = new ArrayList<>();
	private List<Marker> infoMarkers = new ArrayList<>();
	private boolean isShowInfoWindow = false;//是否显示气泡
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private ImageView ivTyphoonRange = null;//台风测距
	private Marker locationMarker = null;
	private List<Polyline> rangeLines = new ArrayList<>();//测距虚线数据
	private List<Marker> rangeMarkers = new ArrayList<>();
	private boolean isRanging = true;//是否允许测距
	private LatLng locationLatLng = null;
	private List<TyphoonDto> lastFactLatLngList = new ArrayList<>();
	private TextView tvNews1 = null;
	private TextSwitcher tvNews = null;
	private RollingThread rollingThread = null;
	private int MSG_ROLING_TYPHOON = 101;
	private GeocodeSearch geocoderSearch = null;
	private String locationCity = "";
	private Bundle bundle = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_typhoon_route);
		mContext = this;
		bundle = savedInstanceState;
		checkAuthority();
	}

	private void init() {
		showDialog();
		initWidget();
		initAmap(bundle);
		initYearListView();
	}

	//需要申请的所有权限
	public static String[] allPermissions = new String[] {
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	//拒绝的权限集合
	public static List<String> deniedList = new ArrayList<>();
	/**
	 * 申请定位权限
	 */
	private void checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			init();
		}else {
			deniedList.clear();
			for (int i = 0; i < allPermissions.length; i++) {
				if (ContextCompat.checkSelfPermission(mContext, allPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
					deniedList.add(allPermissions[i]);
				}
			}
			if (deniedList.isEmpty()) {//所有权限都授予
				init();
			}else {
				String[] permissions = deniedList.toArray(new String[deniedList.size()]);//将list转成数组
				ActivityCompat.requestPermissions(TyphoonRouteActivity.this, permissions, AuthorityUtil.AUTHOR_LOCATION);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_LOCATION:
				if (grantResults.length > 0) {
					boolean isAllGranted = true;//是否全部授权
					for (int i = 0; i < grantResults.length; i++) {
						if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
							isAllGranted = false;
							break;
						}
					}
					if (isAllGranted) {//所有权限都授予
						init();
					}else {//只要有一个没有授权，就提示进入设置界面设置
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限、存储权限，是否前往设置？");
					}
				}else {
					for (int i = 0; i < permissions.length; i++) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(TyphoonRouteActivity.this, permissions[i])) {
							AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"需要使用您的位置权限、存储权限，是否前往设置？");
							break;
						}
					}
				}
				break;
		}
	}

	private void initWidget() {
		llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		tvTyphoonName = findViewById(R.id.tvTyphoonName);
		ivLegend = findViewById(R.id.ivLegend);
		ivLegend.setOnClickListener(this);
		reLegend = findViewById(R.id.reLegend);
		ivCancelLegend = findViewById(R.id.ivCancelLegend);
		ivCancelLegend.setOnClickListener(this);
		ivLocation = findViewById(R.id.ivLocation);
		ivLocation.setOnClickListener(this);
		ivTyphoonList = findViewById(R.id.ivTyphoonList);
		ivTyphoonList.setOnClickListener(this);
		reTyphoonList = findViewById(R.id.reTyphoonList);
		ivCancelList = findViewById(R.id.ivCancelList);
		ivCancelList.setOnClickListener(this);
		ivTyphoonPlay = findViewById(R.id.ivTyphoonPlay);
		ivTyphoonPlay.setOnClickListener(this);
		ivTyphoonRange =  findViewById(R.id.ivTyphoonRange);
		ivTyphoonRange.setOnClickListener(this);
		tvFileTime = findViewById(R.id.tvFileTime);
		ivTyphoonWind = findViewById(R.id.ivTyphoonWind);
		ivTyphoonWind.setOnClickListener(this);
		ivTyphoonRadar = findViewById(R.id.ivTyphoonRadar);
		ivTyphoonRadar.setOnClickListener(this);
		ivTyphoonCloud = findViewById(R.id.ivTyphoonCloud);
		ivTyphoonCloud.setOnClickListener(this);
		container = findViewById(R.id.container);
		container2 = findViewById(R.id.container2);
		tvNews1 = findViewById(R.id.tvNews1);
		tvNews = findViewById(R.id.tvNews);

		geocoderSearch = new GeocodeSearch(mContext);
		geocoderSearch.setOnGeocodeSearchListener(this);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;

	}

	private void initAmap(Bundle bundle) {
		mapView = findViewById(R.id.mapView);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
		aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMarkerClickListener(this);
		aMap.setOnMapClickListener(this);
		aMap.setInfoWindowAdapter(this);
		aMap.setOnCameraChangeListener(this);

		startLocation();
		drawWarningLines();
	}

	/**
	 * 开始定位
	 */
	private void startLocation() {
		mLocationOption = new AMapLocationClientOption();//初始化定位参数
		mLocationClient = new AMapLocationClient(mContext);//初始化定位
		mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
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
			locationCity = amapLocation.getCity();
			if (amapLocation.getLongitude() != 0 && amapLocation.getLatitude() != 0) {
				locationLatLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
				addLocationMarker(locationLatLng);
			}
		}
	}

	private Circle circle100, circle300, circle500;
	private Text text100, text300, text500;
	private void addLocationMarker(LatLng latLng) {
		if (latLng == null) {
			return;
		}
		MarkerOptions options = new MarkerOptions();
		options.position(latLng);
		options.anchor(0.5f, 1.0f);
		Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_map_click_map),
				(int)(CommonUtil.dip2px(mContext, 21)), (int)(CommonUtil.dip2px(mContext, 32)));
		if (bitmap != null) {
			options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
		}else {
			options.icon(BitmapDescriptorFactory.fromResource(R.drawable.iv_map_click_map));
		}
		if (locationMarker != null) {
			locationMarker.remove();
		}
		locationMarker = aMap.addMarker(options);


		if (circle100 != null) {
			circle100.remove();
			circle100 = null;
		}
		circle100 = aMap.addCircle(new CircleOptions().center(latLng)
				.radius(100000).strokeColor(0x90ff6c00).strokeWidth(5));

		if (circle300 != null) {
			circle300.remove();
			circle300 = null;
		}
		circle300 = aMap.addCircle(new CircleOptions().center(latLng)
				.radius(300000).strokeColor(0x90ffd800).strokeWidth(5));

		if (circle500 != null) {
			circle500.remove();
			circle500 = null;
		}
		circle500 = aMap.addCircle(new CircleOptions().center(latLng)
				.radius(500000).strokeColor(0x9000b4ff).strokeWidth(5));

		if (text100 != null) {
			text100.remove();
			text100 = null;
		}
		text100 = addPolylinesCircle(latLng, 100000, 0xffff6c00, "100km");

		if (text300 != null) {
			text300.remove();
			text300 = null;
		}
		text300 = addPolylinesCircle(latLng, 300000, 0xffffd800, "300km");

		if (text500 != null) {
			text500.remove();
			text500 = null;
		}
		text500 = addPolylinesCircle(latLng, 500000, 0xff00b4ff, "500km");

	}

	/**
	 * @param center 中心点坐标
	 * @param radius      半径 米
	 */
	private Text addPolylinesCircle(LatLng center, int radius, int color, String distance) {
		double r = 6371000.79;
		int numpoints = 360;
		double phase = 2 * Math.PI / numpoints;

		//画图
//		for (int i = 0; i < numpoints; i++) {
			double dx = (radius * Math.cos(numpoints*3/4 * phase));
			double dy = (radius * Math.sin(numpoints*3/4 * phase));//乘以1.6 椭圆比例

			double dlng = dx / (r * Math.cos(center.latitude * Math.PI / 180) * Math.PI / 180);
			double dlat = dy / (r * Math.PI / 180);

		TextOptions textOptions = new TextOptions();
		textOptions.backgroundColor(Color.TRANSPARENT);
		textOptions.fontSize(40);
		textOptions.fontColor(color);
		textOptions.text(distance);
		textOptions.position(new LatLng(center.latitude + dlat, center.longitude + dlng));
		Text text = aMap.addText(textOptions);
//		}

		return text;

	}

	/**
	 * 绘制24h、48h警戒线
	 */
	private void drawWarningLines() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				//24小时
				PolylineOptions line1 = new PolylineOptions();
				line1.width(CommonUtil.dip2px(mContext, 2));
				line1.color(getResources().getColor(R.color.red));
				line1.add(new LatLng(34.005024, 126.993568), new LatLng(21.971252, 126.993568));
				line1.add(new LatLng(17.965860, 118.995521), new LatLng(10.971050, 118.995521));
				line1.add(new LatLng(4.486270, 113.018959) ,new LatLng(-0.035506, 104.998939));
				aMap.addPolyline(line1);
				drawWarningText(getString(R.string.line_24h), getResources().getColor(R.color.red), new LatLng(30.959474, 126.993568));

				//48小时
				PolylineOptions line2 = new PolylineOptions();
				line2.width(CommonUtil.dip2px(mContext, 2));
				line2.color(getResources().getColor(R.color.yellow));
				line2.add(new LatLng(-0.035506, 104.998939), new LatLng(-0.035506, 119.962318));
				line2.add(new LatLng(14.968860, 131.981361) ,new LatLng(33.959474, 131.981361));
				aMap.addPolyline(line2);
				drawWarningText(getString(R.string.line_48h), getResources().getColor(R.color.yellow), new LatLng(30.959474, 131.981361));
			}
		}).start();
	}

	/**
	 * 绘制警戒线提示文字
	 */
	private void drawWarningText(String text, int textColor, LatLng latLng) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.marker_typhoon_line_text, null);
		TextView tvLine = view.findViewById(R.id.tvLine);
		tvLine.setText(text);
		tvLine.setTextColor(textColor);
		MarkerOptions options = new MarkerOptions();
		options.anchor(-0.3f, 0.2f);
		options.position(latLng);
		options.icon(BitmapDescriptorFactory.fromView(view));
		aMap.addMarker(options);
	}

	private void initYearListView() {
		yearList.clear();
		final int currentYear = Integer.valueOf(sdf1.format(new Date()));
		int years = 5;//要获取台风的年数
		for (int i = 0; i < years; i++) {
			TyphoonDto dto = new TyphoonDto();
			dto.yearly = currentYear - i;
			yearList.add(dto);
		}
		yearListView = findViewById(R.id.yearListView);
		yearAdapter = new TyphoonYearAdapter(mContext, yearList);
		yearListView.setAdapter(yearAdapter);
		yearListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				for (int i = 0; i < yearList.size(); i++) {
					if (i == arg2) {
						yearAdapter.isSelected.put(i, true);
					}else {
						yearAdapter.isSelected.put(i, false);
					}
				}
				if (yearAdapter != null) {
					yearAdapter.notifyDataSetChanged();
				}

				for (int i = 0; i < nameList.size(); i++) {
					nameAdapter.isSelected.put(i, false);
				}
				if (nameAdapter != null) {
					nameAdapter.notifyDataSetChanged();
				}

				clearAllPoints();
				TyphoonDto dto = yearList.get(arg2);
				String url = "http://decision-admin.tianqi.cn/Home/extra/gettyphoon/list/" + dto.yearly;
				if (!TextUtils.isEmpty(url)) {
					OkHttpTyphoonList(url, currentYear, dto.yearly);
				}
			}
		});

		String url = "http://decision-admin.tianqi.cn/Home/extra/gettyphoon/list/" + yearList.get(0).yearly;
		if (!TextUtils.isEmpty(url)) {
			OkHttpTyphoonList(url, currentYear, yearList.get(0).yearly);
		}
	}

	/**
	 * 获取某一年的台风列表信息
	 */
	private void OkHttpTyphoonList(final String url, final int currentYear, final int selectYear) {
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
						final String requestResult = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(requestResult)) {
									String c = "(";
									String c2 = "})";
									String result = requestResult.substring(requestResult.indexOf(c)+c.length(), requestResult.indexOf(c2)+1);
									if (!TextUtils.isEmpty(result)) {
										try {
											JSONObject obj = new JSONObject(result);
											if (!obj.isNull("typhoonList")) {
												nameList.clear();
												startList.clear();
												JSONArray array = obj.getJSONArray("typhoonList");
												for (int i = 0; i < array.length(); i++) {
													JSONArray itemArray = array.getJSONArray(i);
													TyphoonDto dto = new TyphoonDto();
													dto.id = itemArray.getString(0);
													dto.enName = itemArray.getString(1);
													dto.name = itemArray.getString(2);
													dto.code = itemArray.getString(4);
													dto.status = itemArray.getString(7);
													nameList.add(dto);

													//把活跃台风过滤出来存放
													if (TextUtils.equals(dto.status, "start")) {
														startList.add(dto);
													}
												}

												String typhoonName = "";
												for (int i = startList.size()-1; i >= 0; i--) {
													TyphoonDto data = startList.get(i);
													String detailUrl = "", name = "";
													if (TextUtils.equals(data.enName, "nameless")) {
														if (!TextUtils.isEmpty(typhoonName)) {
															typhoonName = data.enName+"\n"+typhoonName;
														}else {
															typhoonName = data.enName;
														}
														detailUrl = "http://decision-admin.tianqi.cn/Home/extra/gettyphoon/view/" + data.id;
														name = data.code + " " + data.enName;
													}else {
														if (!TextUtils.isEmpty(typhoonName)) {
															typhoonName = data.code + " " + data.name + " " + data.enName+"\n"+typhoonName;;
														}else {
															typhoonName = data.code + " " + data.name + " " + data.enName;
														}
														detailUrl = "http://decision-admin.tianqi.cn/Home/extra/gettyphoon/view/" + data.id;
														name = data.code + " " + data.name + " " + data.enName;
													}
													OkHttpTyphoonDetail(detailUrl, name);
												}
												tvTyphoonName.setText(typhoonName);

												if (startList.size() == 0) {// 没有生效台风
													if (currentYear == selectYear) {// 判断选中年数==当前年数
														tvTyphoonName.setText(getString(R.string.no_typhoon));
													}else {
														tvTyphoonName.setText(selectYear+"年");
													}
//											ivLocation.setVisibility(View.GONE);
//											ivTyphoonPlay.setVisibility(View.GONE);
//											ivTyphoonWind.setVisibility(View.GONE);
//											ivTyphoonRange.setVisibility(View.GONE);
													cancelDialog();

													ivLocation.setVisibility(View.GONE);
													ivTyphoonWind.setVisibility(View.VISIBLE);
													ivTyphoonPlay.setVisibility(View.GONE);
													ivTyphoonRange.setVisibility(View.GONE);
													mRadarManager = new CaiyunManager(getApplicationContext());
													OkHttpMinute("http://api.tianqi.cn:8070/v1/img.py");
													OkHttpCloudChart("http://decision-admin.tianqi.cn/Home/other/getDecisionCloudImages");
												} else if (startList.size() == 1) {// 1个生效台风
													ivLocation.setVisibility(View.VISIBLE);
													ivTyphoonWind.setVisibility(View.VISIBLE);
													ivTyphoonPlay.setVisibility(View.VISIBLE);
													ivTyphoonRange.setVisibility(View.VISIBLE);
													mRadarManager = new CaiyunManager(getApplicationContext());
													OkHttpMinute("http://api.tianqi.cn:8070/v1/img.py");
													OkHttpCloudChart("http://decision-admin.tianqi.cn/Home/other/getDecisionCloudImages");
												} else {// 2个以上生效台风
													ivLocation.setVisibility(View.VISIBLE);
													ivTyphoonWind.setVisibility(View.VISIBLE);
													ivTyphoonRange.setVisibility(View.VISIBLE);
													ivTyphoonPlay.setVisibility(View.GONE);
													mRadarManager = new CaiyunManager(getApplicationContext());
													OkHttpMinute("http://api.tianqi.cn:8070/v1/img.py");
													OkHttpCloudChart("http://decision-admin.tianqi.cn/Home/other/getDecisionCloudImages");
												}
												tvTyphoonName.setVisibility(View.VISIBLE);
											}

											initNameListView();
										} catch (JSONException e) {
											e.printStackTrace();
										}

									}
								}else {
									tvTyphoonName.setText(getString(R.string.no_typhoon));
									tvTyphoonName.setVisibility(View.VISIBLE);
								}
							}
						});
					}
				});
			}
		}).start();

	}

	private void initNameListView() {
		nameListView = findViewById(R.id.nameListView);
		nameAdapter = new TyphoonNameAdapter(mContext, nameList);
		nameListView.setAdapter(nameAdapter);
		nameListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				isWindOn = false;
				ivTyphoonWind.setImageResource(R.drawable.iv_typhoon_fc_off);
				container.removeAllViews();
				container2.removeAllViews();
				tvFileTime.setVisibility(View.GONE);

				for (int i = 0; i < nameList.size(); i++) {
					if (i == arg2) {
						nameAdapter.isSelected.put(i, true);
					}else {
						nameAdapter.isSelected.put(i, false);
					}
				}
				if (nameAdapter != null) {
					nameAdapter.notifyDataSetChanged();
				}

				startList.clear();
				pointsList.clear();
				TyphoonDto dto = nameList.get(arg2);

				if (TextUtils.equals(dto.enName, "nameless")) {
					tvTyphoonName.setText(dto.enName);
				}else {
					tvTyphoonName.setText(dto.code + " " + dto.name + " " + dto.enName);
				}
//				if (TextUtils.isEmpty(dto.name) || TextUtils.equals(dto.name, "null")) {
//					tvTyphoonName.setText(dto.code + " " + dto.enName);
//				}else {
//					tvTyphoonName.setText(dto.code + " " + dto.name + " " + dto.enName);
//				}

				isShowInfoWindow = true;
				String detailUrl = "http://decision-admin.tianqi.cn/Home/extra/gettyphoon/view/" + dto.id;
				OkHttpTyphoonDetail(detailUrl, tvTyphoonName.getText().toString());

				clearAllPoints();

				ivLocation.setVisibility(View.VISIBLE);
				ivTyphoonPlay.setVisibility(View.VISIBLE);
				ivTyphoonRange.setVisibility(View.VISIBLE);
				if (reTyphoonList.getVisibility() == View.GONE) {
					legendAnimation(false, reTyphoonList);
					reTyphoonList.setVisibility(View.VISIBLE);
					ivLegend.setClickable(false);
					ivTyphoonList.setClickable(false);
				}else {
					legendAnimation(true, reTyphoonList);
					reTyphoonList.setVisibility(View.GONE);
					ivLegend.setClickable(true);
					ivTyphoonList.setClickable(true);
				}

			}
		});
	}

	/**
	 * 获取台风详情
	 */
	private void OkHttpTyphoonDetail(final String url, final String name) {
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
						String requestResult = response.body().string();
						if (!TextUtils.isEmpty(requestResult)) {
							String c = "(";
							String result = requestResult.substring(requestResult.indexOf(c)+c.length(), requestResult.indexOf(")"));
							if (!TextUtils.isEmpty(result)) {
								try {
									JSONObject obj = new JSONObject(result);
									if (!obj.isNull("typhoon")) {
										ArrayList<TyphoonDto> points = new ArrayList<>();//台风实点
										List<TyphoonDto> forePoints = new ArrayList<>();//台风预报点
										JSONArray array = obj.getJSONArray("typhoon");
										JSONArray itemArray = array.getJSONArray(8);
										for (int j = 0; j < itemArray.length(); j++) {
											JSONArray itemArray2 = itemArray.getJSONArray(j);
											TyphoonDto dto = new TyphoonDto();
											if (!TextUtils.isEmpty(name)) {
												dto.name = name;
											}
											long longTime = itemArray2.getLong(2);
											String time = sdf2.format(new Date(longTime));
											dto.time = time;
//									String time = itemArray2.getString(1);
											String str_year = time.substring(0, 4);
											if(!TextUtils.isEmpty(str_year)){
												dto.year = Integer.parseInt(str_year);
											}
											String str_month = time.substring(4, 6);
											if(!TextUtils.isEmpty(str_month)){
												dto.month = Integer.parseInt(str_month);
											}
											String str_day = time.substring(6, 8);
											if(!TextUtils.isEmpty(str_day)){
												dto.day = Integer.parseInt(str_day);
											}
											String str_hour = time.substring(8, 10);
											if(!TextUtils.isEmpty(str_hour)){
												dto.hour = Integer.parseInt(str_hour);
											}

											dto.lng = itemArray2.getDouble(4);
											dto.lat = itemArray2.getDouble(5);
											dto.pressure = itemArray2.getString(6);
											dto.max_wind_speed = itemArray2.getString(7);
											dto.move_speed = itemArray2.getString(9);
											String fx_string = itemArray2.getString(8);
											if( !TextUtils.isEmpty(fx_string)){
												String windDir = "";
												for (int i = 0; i < fx_string.length(); i++) {
													String item = fx_string.substring(i, i+1);
													if (TextUtils.equals(item, "N")) {
														item = "北";
													}else if (TextUtils.equals(item, "S")) {
														item = "南";
													}else if (TextUtils.equals(item, "W")) {
														item = "西";
													}else if (TextUtils.equals(item, "E")) {
														item = "东";
													}
													windDir = windDir+item;
												}
												dto.wind_dir = windDir;
											}

											String type = itemArray2.getString(3);
											if (TextUtils.equals(type, "TD")) {//热带低压
												type = "1";
											}else if (TextUtils.equals(type, "TS")) {//热带风暴
												type = "2";
											}else if (TextUtils.equals(type, "STS")) {//强热带风暴
												type = "3";
											}else if (TextUtils.equals(type, "TY")) {//台风
												type = "4";
											}else if (TextUtils.equals(type, "STY")) {//强台风
												type = "5";
											}else if (TextUtils.equals(type, "SuperTY")) {//超强台风
												type = "6";
											}
											dto.type = type;
											dto.isFactPoint = true;

											JSONArray array10 = itemArray2.getJSONArray(10);
											for (int m = 0; m < array10.length(); m++) {
												JSONArray itemArray10 = array10.getJSONArray(m);
												if (m == 0) {
													dto.radius_7 = itemArray10.getString(1);
													dto.en_radius_7 = itemArray10.getString(1);
													dto.es_radius_7 = itemArray10.getString(2);
													dto.wn_radius_7 = itemArray10.getString(3);
													dto.ws_radius_7 = itemArray10.getString(4);
												}else if (m == 1) {
													dto.radius_10 = itemArray10.getString(1);
													dto.en_radius_10 = itemArray10.getString(1);
													dto.es_radius_10 = itemArray10.getString(2);
													dto.wn_radius_10 = itemArray10.getString(3);
													dto.ws_radius_10 = itemArray10.getString(4);
												}
											}
											points.add(dto);

											if (!itemArray2.get(11).equals(null)) {
												JSONObject obj11 = itemArray2.getJSONObject(11);
												JSONArray array11 = obj11.getJSONArray("BABJ");
												if (array11.length() > 0) {
													forePoints.clear();
												}
												for (int n = 0; n < array11.length(); n++) {
													JSONArray itemArray11 = array11.getJSONArray(n);
													for (int i = 0; i < itemArray11.length(); i++) {
														TyphoonDto data = new TyphoonDto();
														if (!TextUtils.isEmpty(name)) {
															data.name = name;
														}
														data.lng = itemArray11.getDouble(2);
														data.lat = itemArray11.getDouble(3);
														data.pressure = itemArray11.getString(4);
														data.move_speed = itemArray11.getString(5);

														long t1 = longTime;
														long t2 = itemArray11.getLong(0)*3600*1000;
														long ttt = t1+t2;
														String ttime = sdf2.format(new Date(ttt));
														data.time = ttime;
														String year = ttime.substring(0, 4);
														if(!TextUtils.isEmpty(year)){
															data.year = Integer.parseInt(year);
														}
														String month = ttime.substring(4, 6);
														if(!TextUtils.isEmpty(month)){
															data.month = Integer.parseInt(month);
														}
														String day = ttime.substring(6, 8);
														if(!TextUtils.isEmpty(day)){
															data.day = Integer.parseInt(day);
														}
														String hour = ttime.substring(8, 10);
														if(!TextUtils.isEmpty(hour)){
															data.hour = Integer.parseInt(hour);
														}

														String babjType = itemArray11.getString(7);
														if (TextUtils.equals(babjType, "TD")) {//热带低压
															babjType = "1";
														}else if (TextUtils.equals(babjType, "TS")) {//热带风暴
															babjType = "2";
														}else if (TextUtils.equals(babjType, "STS")) {//强热带风暴
															babjType = "3";
														}else if (TextUtils.equals(babjType, "TY")) {//台风
															babjType = "4";
														}else if (TextUtils.equals(babjType, "STY")) {//强台风
															babjType = "5";
														}else if (TextUtils.equals(babjType, "SuperTY")) {//超强台风
															babjType = "6";
														}
														data.type = babjType;
														data.isFactPoint = false;

														forePoints.add(data);
													}
												}
											}
										}

										points.addAll(forePoints);
										pointsList.add(points);

										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												if (startList.size() <= 1) {
													drawTyphoon(false, pointsList.get(0));
												}else {
													for (int i = 0; i < pointsList.size(); i++) {
														drawTyphoon(false, pointsList.get(i));
													}
												}

												cancelDialog();
											}
										});

									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}
					}
				});
			}
		}).start();

	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == MSG_PAUSETYPHOON) {
				if (ivTyphoonPlay != null) {
					ivTyphoonPlay.setImageResource(R.drawable.iv_typhoon_play);
				}
				List<TyphoonDto> mPoints = (ArrayList<TyphoonDto>)msg.obj;
				LatLngBounds.Builder builder = LatLngBounds.builder();
				for (int i = 0; i < mPoints.size(); i++) {
					TyphoonDto dto = mPoints.get(i);
					LatLng latLng = new LatLng(dto.lat, dto.lng);
					builder.include(latLng);
				}
				aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
			}else if (msg.what == MSG_ROLING_TYPHOON) {
				int index = msg.arg1;
				TyphoonDto data = lastFactLatLngList.get(index);
				String name = "";
				if (!TextUtils.isEmpty(data.name)) {
					name = data.name;
				}
				String strength = "";
				if (!TextUtils.isEmpty(data.strength)) {
					strength = "("+data.strength+")";
				}
				String wind = "";
				if(!TextUtils.isEmpty(data.max_wind_speed)){
					wind = "中心风力"+ WeatherUtil.getHourWindForce(Float.parseFloat(data.max_wind_speed))+" ";
				}
				String length = getDistance(locationLatLng.longitude, locationLatLng.latitude, data.lng, data.lat)+"公里";
				if (!TextUtils.isEmpty(locationCity)) {
					tvNews.setText(name+strength+wind+"距"+locationCity+length);
				}else {
					tvNews.setText(name+strength+wind+length);
				}
			}
		};
	};

	/**
	 * 清除一个台风
	 */
	private void clearOnePoint() {
		if (startList.size() <= 1) {
			for (int i = 0; i < fullLines.size(); i++) {//清除实线
				fullLines.get(i).remove();
			}
			for (int i = 0; i < dashLines.size(); i++) {//清除虚线
				dashLines.get(i).remove();
			}
			for (int i = 0; i < rangeLines.size(); i++) {//清除测距虚线
				rangeLines.get(i).remove();
			}
			for (int i = 0; i < markerPoints.size(); i++) {//清除台风点
				markerPoints.get(i).remove();
			}
			for (int i = 0; i < markerTimes.size(); i++) {//清除预报点时间
				markerTimes.get(i).remove();
			}
			if (circle != null) {//清除七级风圈
				circle.remove();
				circle = null;
			}
			if (circle2 != null) {//清除十级风圈
				circle2.remove();
				circle2 = null;
			}
			for (int i = 0; i < rotateMarkers.size(); i++) {
				rotateMarkers.get(i).remove();
			}
			rotateMarkers.clear();
			for (int i = 0; i < factTimeMarkers.size(); i++) {
				factTimeMarkers.get(i).remove();
			}
			factTimeMarkers.clear();
			for (int i = 0; i < timeMarkers.size(); i++) {
				timeMarkers.get(i).remove();
			}
			timeMarkers.clear();
			for (int i = 0; i < rangeMarkers.size(); i++) {
				rangeMarkers.get(i).remove();
			}
			rangeMarkers.clear();
		}

	}

	/**
	 * 清除所有台风
	 */
	private void clearAllPoints() {
		for (int i = 0; i < fullLines.size(); i++) {//清除实线
			fullLines.get(i).remove();
		}
		for (int i = 0; i < dashLines.size(); i++) {//清除虚线
			dashLines.get(i).remove();
		}
		for (int i = 0; i < rangeLines.size(); i++) {//清除测距虚线
			rangeLines.get(i).remove();
		}
		for (int i = 0; i < markerPoints.size(); i++) {//清除台风点
			markerPoints.get(i).remove();
		}
		if (circle != null) {//清除七级风圈
			circle.remove();
			circle = null;
		}
		if (circle2 != null) {//清除十级风圈
			circle2.remove();
			circle2 = null;
		}
		for (int i = 0; i < rotateMarkers.size(); i++) {
			rotateMarkers.get(i).remove();
		}
		rotateMarkers.clear();
		for (int i = 0; i < factTimeMarkers.size(); i++) {
			factTimeMarkers.get(i).remove();
		}
		factTimeMarkers.clear();
		for (int i = 0; i < timeMarkers.size(); i++) {
			timeMarkers.get(i).remove();
		}
		timeMarkers.clear();
		for (int i = 0; i < infoMarkers.size(); i++) {
			infoMarkers.get(i).remove();
		}
		infoMarkers.clear();
		for (int i = 0; i < rangeMarkers.size(); i++) {
			rangeMarkers.get(i).remove();
		}
		rangeMarkers.clear();
	}

	/**
	 * 绘制台风
	 * @param isAnimate
	 */
	private void drawTyphoon(boolean isAnimate, List<TyphoonDto> list) {
		if (list.isEmpty()) {
			return;
		}

		clearOnePoint();

		if (mRoadThread != null) {
			mRoadThread.cancel();
			mRoadThread = null;
		}
		mRoadThread = new RoadThread(list, isAnimate);
		mRoadThread.start();
	}

	/**
	 * 绘制台风点
	 */
	private class RoadThread extends Thread {
		private boolean cancelled = false;
		private List<TyphoonDto> mPoints = null;//整个台风路径信息
		private int delay = 200;
		private boolean isAnimate = true;
		private int i = 0;
		private TyphoonDto lastShikuangPoint;
		private TyphoonDto prevShikuangPoint;

		public RoadThread(List<TyphoonDto> points, boolean isAnimate) {
			mPoints = points;
			this.isAnimate = isAnimate;
		}

		@Override
		public void run() {
			lastShikuangPoint = null;
			final int len = mPoints.size();

			final List<TyphoonDto> factPointList = new ArrayList<>();
			for (int j = 0; j < len; j++) {
				if (mPoints.get(j).isFactPoint) {
					factPointList.add(mPoints.get(j));
				}
			}

			for (i = 0; i < len; i++) {
				if (i == len-1) {
					Message msg = new Message();
					msg.what = MSG_PAUSETYPHOON;
					msg.obj = mPoints;
					handler.sendMessage(msg);
				}
				if (isAnimate) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (cancelled) {
					break;
				}
				final TyphoonDto start = mPoints.get(i);
				final TyphoonDto end = i >= (len - 1) ? null : mPoints.get(i + 1);
				final TyphoonDto lastPoint = null == end ? start : end;
				if (null == lastShikuangPoint && (TextUtils.isEmpty(lastPoint.type) || i == len - 1)) {
					lastShikuangPoint = prevShikuangPoint == null ? lastPoint : prevShikuangPoint;
				}
				prevShikuangPoint = lastPoint;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						drawRoute(start, end, factPointList.get(factPointList.size()-1));
					}
				});
			}

		}

		public void cancel() {
			cancelled = true;
		}
	}

	private void drawRoute(TyphoonDto start, TyphoonDto end, TyphoonDto lastFactPoint) {
		if (end == null) {//最后一个点
			return;
		}
		ArrayList<LatLng> temp = new ArrayList<>();
		if (end.isFactPoint) {//实况线
			PolylineOptions line = new PolylineOptions();
			line.width(CommonUtil.dip2px(mContext, 2));
			line.color(Color.RED);
			temp.add(new LatLng(start.lat, start.lng));
			LatLng latlng = new LatLng(end.lat, end.lng);
			temp.add(latlng);
			line.addAll(temp);
			Polyline fullLine = aMap.addPolyline(line);
			fullLines.add(fullLine);
		} else {//预报虚线
			LatLng start_latlng = new LatLng(start.lat, start.lng);
			double lng_start = start_latlng.longitude;
			double lat_start = start_latlng.latitude;
			LatLng end_latlng = new LatLng(end.lat, end.lng);
			double lng_end = end_latlng.longitude;
			double lat_end = end_latlng.latitude;
			double dis = Math.sqrt(Math.pow(lat_start - lat_end, 2)+ Math.pow(lng_start - lng_end, 2));
			int numPoint = (int) Math.floor(dis / 0.2);
			double lng_per = (lng_end - lng_start) / numPoint;
			double lat_per = (lat_end - lat_start) / numPoint;
			for (int i = 0; i < numPoint; i++) {
				PolylineOptions line = new PolylineOptions();
				line.color(Color.RED);
				line.width(CommonUtil.dip2px(mContext, 2));
				temp.add(new LatLng(lat_start + i * lat_per, lng_start + i * lng_per));
				if (i % 2 == 1) {
					line.addAll(temp);
					Polyline dashLine = aMap.addPolyline(line);
					dashLines.add(dashLine);
					temp.clear();
				}
			}
		}

		MarkerOptions options = new MarkerOptions();
		options.title(start.name+"|"+start.content(mContext));
		options.snippet(start.radius_7+","+start.radius_10);
		options.anchor(0.5f, 0.5f);
		options.position(new LatLng(start.lat, start.lng));
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.marker_typhoon_point, null);
		ImageView ivPoint = view.findViewById(R.id.ivPoint);
		if (TextUtils.equals(start.type, "1")) {
			ivPoint.setImageResource(R.drawable.typhoon_level1);
		}else if (TextUtils.equals(start.type, "2")) {
			ivPoint.setImageResource(R.drawable.typhoon_level2);
		}else if (TextUtils.equals(start.type, "3")) {
			ivPoint.setImageResource(R.drawable.typhoon_level3);
		}else if (TextUtils.equals(start.type, "4")) {
			ivPoint.setImageResource(R.drawable.typhoon_level4);
		}else if (TextUtils.equals(start.type, "5")) {
			ivPoint.setImageResource(R.drawable.typhoon_level5);
		}else if (TextUtils.equals(start.type, "6")) {
			ivPoint.setImageResource(R.drawable.typhoon_level6);
		}else {//预报点
			ivPoint.setImageResource(R.drawable.typhoon_yb);

			boolean isAdd = false;//是否已添加
			String time = start.month+"月"+start.day+"日"+start.hour+"时";
			for (int i = 0; i < markerTimes.size(); i++) {
				if (TextUtils.equals(markerTimes.get(i).getSnippet(), time)) {
					isAdd = true;
					break;
				}
			}
			if (isAdd == false) {
				View textView = inflater.inflate(R.layout.marker_typhoon_line_text, null);
				TextView tvLine = (TextView) textView.findViewById(R.id.tvLine);
				if (time != null) {
					tvLine.setText(time);
				}
				tvLine.setTextColor(Color.WHITE);
				MarkerOptions optionsTime = new MarkerOptions();
				optionsTime.snippet(time);
				optionsTime.anchor(-0.2f, 0.5f);
				optionsTime.position(new LatLng(start.lat, start.lng));
				optionsTime.icon(BitmapDescriptorFactory.fromView(textView));
				Marker timeMarker = aMap.addMarker(optionsTime);
				markerTimes.add(timeMarker);
			}
		}
		options.icon(BitmapDescriptorFactory.fromView(view));
		Marker marker = aMap.addMarker(options);
		markerPoints.add(marker);

		if (start.isFactPoint) {
			if(isShowInfoWindow) {
				marker.showInfoWindow();
				clickMarker = marker;
			}

			MarkerOptions tOption = new MarkerOptions();
			tOption.position(new LatLng(start.lat, start.lng));
			tOption.anchor(0.5f, 0.5f);
			ArrayList<BitmapDescriptor> iconList = new ArrayList<>();
			for (int i = 1; i <= 9; i++) {
				iconList.add(BitmapDescriptorFactory.fromAsset("typhoon/typhoon_icon"+i+".png"));
			}
			tOption.icons(iconList);
			tOption.period(2);
			tOption.zIndex(-10);


			if (circle != null) {
				circle.remove();
				circle = null;
			}
			if (!TextUtils.isEmpty(start.radius_7)) {
				circle = aMap.addCircle(new CircleOptions().center(new LatLng(start.lat, start.lng))
						.radius(Double.valueOf(start.radius_7)*1000).strokeColor(Color.YELLOW)
						.fillColor(0x30ffffff).strokeWidth(5));
			}

			if (circle2 != null) {
				circle2.remove();
				circle2 = null;
			}
			if (!TextUtils.isEmpty(start.radius_10)) {
				circle2 = aMap.addCircle(new CircleOptions().center(new LatLng(start.lat, start.lng))
						.radius(Double.valueOf(start.radius_10)*1000).strokeColor(Color.RED)
						.fillColor(0x30ffffff).strokeWidth(5));
			}

			View timeView = inflater.inflate(R.layout.marker_typhoon_time, null);
			TextView tvTime = timeView.findViewById(R.id.tvTime);
			if (!TextUtils.isEmpty(start.time)) {
				try {
					tvTime.setText(sdf5.format(sdf2.parse(start.time)));
					tvTime.setTextColor(Color.BLACK);
					tvTime.setBackgroundResource(R.drawable.bg_corner_typhoon_time);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			MarkerOptions mo = new MarkerOptions();
			mo.anchor(1.2f, 0.5f);
			mo.position(new LatLng(start.lat, start.lng));
			mo.icon(BitmapDescriptorFactory.fromView(timeView));
			if (lastFactPoint == start) {

				Marker factTimeMarker = aMap.addMarker(mo);
				factTimeMarkers.add(factTimeMarker);
				if (isShowTime) {
					factTimeMarker.setVisible(true);
				}else {
					factTimeMarker.setVisible(false);
				}

				Marker rotateMarker = aMap.addMarker(tOption);
				rotateMarkers.add(rotateMarker);

//				MarkerOptions info = new MarkerOptions();
//				info.anchor(0.5f, 1.0f);
//				info.position(new LatLng(lastFactPoint.lat, lastFactPoint.lng));
//				View infoView = inflater.inflate(R.layout.typhoon_marker_view, null);
//				TextView tvName = (TextView) infoView.findViewById(R.id.tvName);
//				TextView tvInfo = (TextView) infoView.findViewById(R.id.tvInfo);
//				tvName.setText(lastFactPoint.name);
//				tvInfo.setText(lastFactPoint.content(mContext));
//				info.icon(BitmapDescriptorFactory.fromView(infoView));
//				Marker infoMarker = aMap.addMarker(info);
//				infoMarkers.add(infoMarker);
//
//				if (lastFactPoint.lng < 131.981361) {
//					aMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lastFactPoint.lat, lastFactPoint.lng)));
//				}

				//多个台风最后实况点合在一起
				boolean isContain = false;
				for (int i = 0; i < lastFactLatLngList.size(); i++) {
					TyphoonDto data = lastFactLatLngList.get(i);
					if (data.lat == lastFactPoint.lat && data.lng == lastFactPoint.lng) {
						isContain = true;
						break;
					}
				}
				if (isContain == false) {
					if (startList.size() <= 1) {
						lastFactLatLngList.clear();
					}
					lastFactLatLngList.add(lastFactPoint);
				}

				ranging();
			}
		}else {
			View timeView = inflater.inflate(R.layout.marker_typhoon_time, null);
			TextView tvTime = (TextView) timeView.findViewById(R.id.tvTime);
			if (!TextUtils.isEmpty(start.time)) {
				try {
					tvTime.setText(sdf4.format(sdf2.parse(start.time)));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			MarkerOptions mo = new MarkerOptions();
			mo.anchor(-0.05f, 0.5f);
			mo.position(new LatLng(start.lat, start.lng));
			mo.icon(BitmapDescriptorFactory.fromView(timeView));
			Marker m = aMap.addMarker(mo);
			timeMarkers.add(m);
			if (isShowTime) {
				m.setVisible(true);
			}else {
				m.setVisible(false);
			}
		}
	}

	/**
	 * 测距
	 */
	private void ranging() {
		if (locationLatLng == null) {
			return;
		}

		for (int j = 0; j < lastFactLatLngList.size(); j++) {
			double lng_start = locationLatLng.longitude;
			double lat_start = locationLatLng.latitude;
			LatLng end_latlng = new LatLng(lastFactLatLngList.get(j).lat, lastFactLatLngList.get(j).lng);
			double lng_end = end_latlng.longitude;
			double lat_end = end_latlng.latitude;
			double dis = Math.sqrt(Math.pow(lat_start - lat_end, 2)+ Math.pow(lng_start - lng_end, 2));
			int numPoint = (int) Math.floor(dis / 0.2);
			double lng_per = (lng_end - lng_start) / numPoint;
			double lat_per = (lat_end - lat_start) / numPoint;
			List<LatLng> ranges = new ArrayList<>();
			for (int i = 0; i < numPoint; i++) {
				PolylineOptions line = new PolylineOptions();
				line.color(0xff6291E1);
				line.width(CommonUtil.dip2px(mContext, 2));
				ranges.add(new LatLng(lat_start + i * lat_per, lng_start + i * lng_per));
				if (i % 2 == 1) {
					line.addAll(ranges);
					Polyline dashLine = aMap.addPolyline(line);
					rangeLines.add(dashLine);
					ranges.clear();
				}
			}

			LatLng centerLatLng = new LatLng((lat_start + lat_end)/2, (lng_start + lng_end)/2);
			addRangeMarker(centerLatLng, lng_start, lat_start, lng_end, lat_end);
		}

		searchAddrByLatLng(locationLatLng.latitude, locationLatLng.longitude);

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
			locationCity = result.getRegeocodeAddress().getCity();

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					tvNews.removeAllViews();
					tvNews.setFactory(new ViewSwitcher.ViewFactory() {
						@Override
						public View makeView() {
							TextView textView = new TextView(mContext);
							textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
							textView.setTextColor(Color.WHITE);
							textView.setEllipsize(TextUtils.TruncateAt.END);
							return textView;
						}
					});
					if (lastFactLatLngList.size() >= 2) {
						tvNews.setVisibility(View.VISIBLE);
						tvNews1.setVisibility(View.GONE);

						removeThread();
						rollingThread = new RollingThread();
						rollingThread.start();
					}else if (lastFactLatLngList.size() == 1) {
						tvNews.setVisibility(View.GONE);
						tvNews1.setVisibility(View.VISIBLE);
						TyphoonDto data = lastFactLatLngList.get(0);
						String name = "";
						if (!TextUtils.isEmpty(data.name)) {
							name = data.name;
						}
						String strength = "";
						if (!TextUtils.isEmpty(data.strength)) {
							strength = "("+data.strength+")";
						}
						String wind = "";
						if(!TextUtils.isEmpty(data.max_wind_speed)){
							wind = "中心风力"+ WeatherUtil.getHourWindForce(Float.parseFloat(data.max_wind_speed))+" ";
						}
						String length = getDistance(locationLatLng.longitude, locationLatLng.latitude, data.lng, data.lat)+"公里";
						if (!TextUtils.isEmpty(locationCity)) {
							tvNews1.setText(name+strength+wind+"距"+locationCity+length);
						}else {
							tvNews1.setText(name+strength+wind+length);
						}
					}
				}
			});
		}
	}

	private void removeThread() {
		if (rollingThread != null) {
			rollingThread.cancel();
			rollingThread = null;
		}
	}

	private class RollingThread extends Thread {
		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private int state;
		private int index;
		private boolean isTracking = false;

		@Override
		public void run() {
			super.run();
			this.state = STATE_PLAYING;
			while (index < lastFactLatLngList.size()) {
				if (state == STATE_CANCEL) {
					break;
				}
				if (state == STATE_PAUSE) {
					continue;
				}
				if (isTracking) {
					continue;
				}
				try {
					Message msg = handler.obtainMessage();
					msg.what = MSG_ROLING_TYPHOON;
					msg.arg1 = index;
					handler.sendMessage(msg);
					sleep(4000);
					index++;
					if (index >= lastFactLatLngList.size()) {
						index = 0;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void cancel() {
			this.state = STATE_CANCEL;
		}
	}

	/**
	 * 添加每个台风的测距距离
	 */
	private void addRangeMarker(LatLng latLng, double longitude1, double latitude1, double longitude2, double latitude2) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		MarkerOptions options = new MarkerOptions();
		options.position(latLng);
		View mView = inflater.inflate(R.layout.marker_typhoon_range, null);
		TextView tvName = mView.findViewById(R.id.tvName);
		tvName.setText("距离台风"+getDistance(longitude1, latitude1, longitude2, latitude2)+"公里");

		options.icon(BitmapDescriptorFactory.fromView(mView));
		Marker marker = aMap.addMarker(options);
		rangeMarkers.add(marker);
	}

	/**
	 * 计算两点之间距离
	 *
	 * @param longitude1
	 * @param latitude1
	 * @param longitude2
	 * @param latitude2
	 * @return
	 */
	private static String getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
		double EARTH_RADIUS = 6378137;//单位米
		double Lat1 = rad(latitude1);
		double Lat2 = rad(latitude2);
		double a = Lat1 - Lat2;
		double b = rad(longitude1) - rad(longitude2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(Lat1) * Math.cos(Lat2) * Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		BigDecimal bd = new BigDecimal(s / 1000);
		double d = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
		String distance = d + "";

		String value = distance;
		if (value.length() >= 2 && value.contains(".")) {
			if (value.equals(".0")) {
				distance = "0";
			} else {
				if (TextUtils.equals(value.substring(value.length() - 2, value.length()), ".0")) {
					distance = value.substring(0, value.indexOf("."));
				} else {
					distance = value;
				}
			}
		}

		return distance;
	}

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		for (int i = 0; i < infoMarkers.size(); i++) {
			infoMarkers.get(i).remove();
		}
		infoMarkers.clear();

		clickMarker = arg0;
		if (arg0 == null) {
			return false;
		}
		arg0.showInfoWindow();

		String[] snippet = arg0.getSnippet().split(",");
		if (circle != null) {
			circle.remove();
			circle = null;
		}
		if (!TextUtils.isEmpty(snippet[0]) && !TextUtils.equals(snippet[0], "null")) {
			circle = aMap.addCircle(new CircleOptions().center(arg0.getPosition())
					.radius(Double.valueOf(snippet[0])*1000).strokeColor(Color.YELLOW)
					.fillColor(0x30000000).strokeWidth(5));
		}

		if (circle2 != null) {
			circle2.remove();
			circle2 = null;
		}
		if (!TextUtils.isEmpty(snippet[1]) && !TextUtils.equals(snippet[1], "null")) {
			circle2 = aMap.addCircle(new CircleOptions().center(arg0.getPosition())
					.radius(Double.valueOf(snippet[1])*1000).strokeColor(Color.RED)
					.fillColor(0x30ffffff).strokeWidth(5));
		}

		return true;
	}

	@Override
	public void onMapClick(LatLng arg0) {
		//测距状态下
		if (isRanging) {
			for (int i = 0; i < rangeMarkers.size(); i++) {
				rangeMarkers.get(i).remove();
			}
			rangeMarkers.clear();
			for (int i = 0; i < rangeLines.size(); i++) {//清除测距虚线
				rangeLines.get(i).remove();
			}
			locationLatLng = arg0;
			ranging();
			addLocationMarker(arg0);
		}

		mapClick();
	}

	private void mapClick() {
		for (int i = 0; i < infoMarkers.size(); i++) {
			infoMarkers.get(i).remove();
		}
		infoMarkers.clear();

		if (clickMarker != null) {
			clickMarker.hideInfoWindow();
			if (circle != null) {//清除七级风圈
				circle.remove();
				circle = null;
			}
			if (circle2 != null) {//清除十级风圈
				circle2.remove();
				circle2 = null;
			}
		}

//		if (reLegend.getVisibility() == View.VISIBLE) {
//			legendAnimation(true, reLegend);
//			reLegend.setVisibility(View.GONE);
//			ivLegend.setClickable(true);
//			ivTyphoonList.setClickable(true);
//		}
//
//		if (reTyphoonList.getVisibility() == View.VISIBLE) {
//			legendAnimation(true, reTyphoonList);
//			reTyphoonList.setVisibility(View.GONE);
//			ivLegend.setClickable(true);
//			ivTyphoonList.setClickable(true);
//		}
	}

	@Override
	public View getInfoContents(Marker arg0) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.marker_typhoon_icon, null);
		TextView tvName = view.findViewById(R.id.tvName);
		TextView tvInfo = view.findViewById(R.id.tvInfo);
		ImageView ivDelete = view.findViewById(R.id.ivDelete);
		if (!TextUtils.isEmpty(arg0.getTitle())) {
			String[] str = arg0.getTitle().split("\\|");
			if (!TextUtils.isEmpty(str[0])) {
				tvName.setText(str[0]);
			}
			if (!TextUtils.isEmpty(str[1])) {
				tvInfo.setText(str[1]);
			}
		}
		ivDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mapClick();
			}
		});
		return view;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		return null;
	}

	private void legendAnimation(boolean flag, final RelativeLayout reLayout) {
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation;
		if (flag == false) {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0,
					Animation.RELATIVE_TO_SELF, 0,
					Animation.RELATIVE_TO_SELF, 1f,
					Animation.RELATIVE_TO_SELF, 0);
		}else {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,1.0f);
		}
		animation.setDuration(400);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		reLayout.startAnimation(animationSet);
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				reLayout.clearAnimation();
			}
		});
	}

	/**
	 * 获取分钟级降水图
	 */
	private void OkHttpMinute(final String url) {
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
										if (!obj.isNull("status")) {
											if (obj.getString("status").equals("ok")) {
												if (!obj.isNull("radar_img")) {
													JSONArray array = new JSONArray(obj.getString("radar_img"));
													for (int i = 0; i < array.length(); i++) {
														JSONArray array0 = array.getJSONArray(i);
														MinuteFallDto dto = new MinuteFallDto();
														dto.imgUrl = array0.optString(0);
														dto.time = array0.optLong(1);
														JSONArray itemArray = array0.getJSONArray(2);
														dto.p1 = itemArray.optDouble(0);
														dto.p2 = itemArray.optDouble(1);
														dto.p3 = itemArray.optDouble(2);
														dto.p4 = itemArray.optDouble(3);
														radarList.add(dto);
													}

													if (radarList.size() > 0) {
														startDownLoadImgs(radarList);
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
		if (result == CaiyunManager.RadarListener.RESULT_SUCCESSED) {
			mHandler.sendEmptyMessage(HANDLER_LOAD_FINISHED);
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case HANDLER_SHOW_RADAR:
					if (msg.obj != null) {
						MinuteFallDto dto = (MinuteFallDto) msg.obj;
						if (!TextUtils.isEmpty(dto.path)) {
							try {
								Bitmap bitmap = BitmapFactory.decodeFile(dto.path);
								if (bitmap != null) {
									showRadar(bitmap, dto.p1, dto.p2, dto.p3, dto.p4);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					break;
				case HANDLER_LOAD_FINISHED:
					ivTyphoonRadar.setVisibility(View.VISIBLE);
					break;
				default:
					break;
			}
		};
	};

	@Override
	public void onProgress(String url, int progress) {
//		Message msg = new Message();
//		msg.obj = progress;
//		msg.what = HANDLER_PROGRESS;
//		mHandler.sendMessage(msg);
	}

	private void showRadar(Bitmap bitmap, double p1, double p2, double p3, double p4) {
		BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
		LatLngBounds bounds = new LatLngBounds.Builder()
				.include(new LatLng(p3, p2))
				.include(new LatLng(p1, p4))
				.build();

		if (radarOverlay == null) {
			radarOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
					.anchor(0.5f, 0.5f)
					.positionFromBounds(bounds)
					.image(fromView)
					.transparency(0.0f));
		} else {
			radarOverlay.setImage(null);
			radarOverlay.setPositionFromBounds(bounds);
			radarOverlay.setImage(fromView);
		}
		aMap.runOnDrawFrame();
	}

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

	/**
	 * 获取云图数据
	 */
	private void OkHttpCloudChart(final String url) {
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
										if (!obj.isNull("l")) {
											JSONArray array = obj.getJSONArray("l");
											if (array.length() > 0) {
												JSONObject itemObj = array.getJSONObject(0);
												String imgUrl = itemObj.getString("l2");
												if (!TextUtils.isEmpty(imgUrl)) {
													OkHttpLoadCloudImage(imgUrl);
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

	/**
	 * 下载云图
	 * @param imgUrl
	 */
	private void OkHttpLoadCloudImage(final String imgUrl) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(imgUrl).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final byte[] bytes = response.body().bytes();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
								if (bitmap != null) {
									cloudBitmap = bitmap;
									ivTyphoonCloud.setVisibility(View.VISIBLE);
								}
							}
						});
					}
				});
			}
		}).start();
	}

	private void showCloud(Bitmap bitmap) {
//		DisplayMetrics dm = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(dm);
//		Point leftPoint = new Point(0, dm.heightPixels);
//		Point rightPoint = new Point(dm.widthPixels, 0);
//		LatLng leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
//		LatLng rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);
		if (bitmap == null) {
			return;
		}

		BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
		LatLngBounds bounds = new LatLngBounds.Builder()
				.include(new LatLng(-10.787277369124666, 62.8820698883665))
				.include(new LatLng(56.385845314127209, 161.69675114151386))
				.build();

		if (cloudOverlay == null) {
			cloudOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
					.anchor(0.5f, 0.5f)
					.positionFromBounds(bounds)
					.image(fromView)
					.transparency(0.2f));
		} else {
			cloudOverlay.setImage(null);
			cloudOverlay.setPositionFromBounds(bounds);
			cloudOverlay.setImage(fromView);
		}
	}

	/**
	 * 获取风场数据
	 */
	private void OkHttpWind() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(SecretUrlUtil.windGFS("1000")).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						String result = response.body().string();
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONObject obj = new JSONObject(result);
								if (windData == null) {
									windData = new WindData();
								}
								if (obj != null) {
									if (!obj.isNull("gridHeight")) {
										windData.height = obj.getInt("gridHeight");
									}
									if (!obj.isNull("gridWidth")) {
										windData.width = obj.getInt("gridWidth");
									}
									if (!obj.isNull("x0")) {
										windData.x0 = obj.getDouble("x0");
									}
									if (!obj.isNull("y0")) {
										windData.y0 = obj.getDouble("y0");
									}
									if (!obj.isNull("x1")) {
										windData.x1 = obj.getDouble("x1");
									}
									if (!obj.isNull("y1")) {
										windData.y1 = obj.getDouble("y1");
									}
									if (!obj.isNull("filetime")) {
										windData.filetime = obj.getString("filetime");
									}

									if (!obj.isNull("field")) {
										windData.dataList.clear();
										JSONArray array = new JSONArray(obj.getString("field"));
										for (int i = 0; i < array.length(); i+=2) {
											WindDto dto2 = new WindDto();
											dto2.initX = (float)(array.optDouble(i));
											dto2.initY = (float)(array.optDouble(i+1));
											windData.dataList.add(dto2);
										}
									}

									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											cancelDialog();
											reloadWind();
											isHaveWindData = true;
										}
									});

								}
							} catch (JSONException e1) {
								e1.printStackTrace();
							}
						}
					}
				});
			}
		}).start();
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
		container.removeAllViews();
		container2.removeAllViews();
		tvFileTime.setVisibility(View.GONE);
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		if (isWindOn && isHaveWindData) {
			reloadWind();
		}
	}

	long t = new Date().getTime();

	/**
	 * 重新加载风场
	 */
	private void reloadWind() {
		t = new Date().getTime() - t;
		if (t < 1000) {
			return;
		}

		int statusBarHeight = CommonUtil.statusBarHeight(mContext);
		int naviBarHeight = CommonUtil.navigationBarHeight(mContext);

		LatLng latLngStart = aMap.getProjection().fromScreenLocation(new Point(0, statusBarHeight));
		LatLng latLngEnd = aMap.getProjection().fromScreenLocation(new Point(width, height-naviBarHeight-statusBarHeight));
		windData.latLngStart = latLngStart;
		windData.latLngEnd = latLngEnd;
		if (waitWindView == null) {
			waitWindView = new WaitWindView(mContext);
			waitWindView.init(TyphoonRouteActivity.this);
			waitWindView.setData(windData);
			waitWindView.start();
			waitWindView.invalidate();
		}

		container.removeAllViews();
		container.addView(waitWindView);
		ivTyphoonWind.setVisibility(View.VISIBLE);
		tvFileTime.setVisibility(View.VISIBLE);
		if (!TextUtils.isEmpty(windData.filetime)) {
			try {
				tvFileTime.setText("GFS "+sdf3.format(sdf2.parse(windData.filetime))+"风场预报");
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}else if (v.getId() == R.id.ivTyphoonRadar) {
			if (isRadarOn == false) {//添加雷达图

				if (isCloudOn) {//删除云图
					isCloudOn = false;
					ivTyphoonCloud.setImageResource(R.drawable.iv_typhoon_cloud_off);
					if (cloudOverlay != null) {
						cloudOverlay.remove();
						cloudOverlay = null;
					}
				}

				isRadarOn = true;
				ivTyphoonRadar.setImageResource(R.drawable.iv_typhoon_radar_on);
				if (mRadarThread != null) {
					mRadarThread.cancel();
					mRadarThread = null;
				}
				mRadarThread = new RadarThread(radarList);
				mRadarThread.start();
			}else {//删除雷达图
				isRadarOn = false;
				ivTyphoonRadar.setImageResource(R.drawable.iv_typhoon_radar_off);
				if (radarOverlay != null) {
					radarOverlay.remove();
					radarOverlay = null;
				}
				if (mRadarThread != null) {
					mRadarThread.cancel();
					mRadarThread = null;
				}
			}
		}else if (v.getId() == R.id.ivTyphoonCloud) {
			if (isCloudOn == false) {//添加云图

				if (isRadarOn) {//删除雷达图
					isRadarOn = false;
					ivTyphoonRadar.setImageResource(R.drawable.iv_typhoon_radar_off);
					if (radarOverlay != null) {
						radarOverlay.remove();
						radarOverlay = null;
					}
					if (mRadarThread != null) {
						mRadarThread.cancel();
						mRadarThread = null;
					}
				}

				isCloudOn = true;
				ivTyphoonCloud.setImageResource(R.drawable.iv_typhoon_cloud_on);
				showCloud(cloudBitmap);
			}else {//删除云图
				isCloudOn = false;
				ivTyphoonCloud.setImageResource(R.drawable.iv_typhoon_cloud_off);
				if (cloudOverlay != null) {
					cloudOverlay.remove();
					cloudOverlay = null;
				}
			}
		}else if (v.getId() == R.id.ivTyphoonWind) {
			if (isWindOn == false) {//添加图层
				if (isHaveWindData == false) {
					OkHttpWind();
				}else {
					reloadWind();
				}
				isWindOn = true;
				ivTyphoonWind.setImageResource(R.drawable.iv_typhoon_fc_on);
				tvFileTime.setVisibility(View.VISIBLE);
			}else {//清除图层
				isWindOn = false;
				ivTyphoonWind.setImageResource(R.drawable.iv_typhoon_fc_off);
				tvFileTime.setVisibility(View.GONE);
				container.removeAllViews();
				container2.removeAllViews();
				tvFileTime.setVisibility(View.GONE);
			}
		}else if (v.getId() == R.id.ivLegend || v.getId() == R.id.ivCancelLegend) {
			if (reLegend.getVisibility() == View.GONE) {
				legendAnimation(false, reLegend);
				reLegend.setVisibility(View.VISIBLE);
				ivLegend.setClickable(false);
				ivTyphoonList.setClickable(false);
			}else {
				legendAnimation(true, reLegend);
				reLegend.setVisibility(View.GONE);
				ivLegend.setClickable(true);
				ivTyphoonList.setClickable(true);
			}
		}else if (v.getId() == R.id.ivTyphoonList || v.getId() == R.id.ivCancelList) {
			if (reTyphoonList.getVisibility() == View.GONE) {
				legendAnimation(false, reTyphoonList);
				reTyphoonList.setVisibility(View.VISIBLE);
				ivLegend.setClickable(false);
				ivTyphoonList.setClickable(false);
			}else {
				legendAnimation(true, reTyphoonList);
				reTyphoonList.setVisibility(View.GONE);
				ivLegend.setClickable(true);
				ivTyphoonList.setClickable(true);
			}
		}else if (v.getId() == R.id.ivTyphoonPlay) {
			for (int i = 0; i < infoMarkers.size(); i++) {
				infoMarkers.get(i).remove();
			}
			infoMarkers.clear();
			ivTyphoonPlay.setImageResource(R.drawable.iv_typhoon_pause);
			container.removeAllViews();
			container2.removeAllViews();
			tvFileTime.setVisibility(View.GONE);
			isShowInfoWindow = true;
			if (!pointsList.isEmpty() && pointsList.get(0) != null) {
				drawTyphoon(false, pointsList.get(0));
			}
		}else if (v.getId() == R.id.ivLocation) {
			if (isShowTime == false) {
				isShowTime = true;
				for (int i = 0; i < factTimeMarkers.size(); i++) {
					factTimeMarkers.get(i).setVisible(true);
				}
				for (int i = 0; i < timeMarkers.size(); i++) {
					timeMarkers.get(i).setVisible(true);
				}
			}else {
				isShowTime = false;
				for (int i = 0; i < factTimeMarkers.size(); i++) {
					factTimeMarkers.get(i).setVisible(false);
				}
				for (int i = 0; i < timeMarkers.size(); i++) {
					timeMarkers.get(i).setVisible(false);
				}
			}
		}else if (v.getId() == R.id.ivTyphoonRange) {
			if (isRanging) {
				isRanging = false;
				ivTyphoonRange.setImageResource(R.drawable.iv_typhoon_cj_off);

				if (circle100 != null) {
					circle100.remove();
					circle100 = null;
				}

				if (circle300 != null) {
					circle300.remove();
					circle300 = null;
				}

				if (circle500 != null) {
					circle500.remove();
					circle500 = null;
				}

				if (text100 != null) {
					text100.remove();
					text100 = null;
				}

				if (text300 != null) {
					text300.remove();
					text300 = null;
				}

				if (text500 != null) {
					text500.remove();
					text500 = null;
				}

				for (int i = 0; i < rangeMarkers.size(); i++) {
					rangeMarkers.get(i).remove();
				}
				rangeMarkers.clear();
				for (int i = 0; i < rangeLines.size(); i++) {//清除测距虚线
					rangeLines.get(i).remove();
				}
			}else {
				isRanging = true;
				ivTyphoonRange.setImageResource(R.drawable.iv_typhoon_cj_on);

				ranging();

				addLocationMarker(locationLatLng);
			}
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
		if (mRadarManager != null) {
			mRadarManager.onDestory();
		}
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
		removeThread();
	}

}
