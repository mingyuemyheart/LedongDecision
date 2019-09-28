package com.cxwl.shawn.wuzhishan.decision.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.adapter.MainAdapter;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.common.MyApplication;
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData;
import com.cxwl.shawn.wuzhishan.decision.util.AuthorityUtil;
import com.cxwl.shawn.wuzhishan.decision.util.AutoUpdateUtil;
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil;
import com.cxwl.shawn.wuzhishan.decision.util.FetchWeather;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;
import com.cxwl.shawn.wuzhishan.decision.util.SecretUrlUtil;
import com.cxwl.shawn.wuzhishan.decision.util.WeatherUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends BaseActivity implements View.OnClickListener, AMapLocationListener {

    private Context mContext;
    private RelativeLayout reTitle,reFact;
    private GridView gridView = null;
    private MainAdapter mAdapter = null;
    private ArrayList<ColumnData> channelList = new ArrayList<>();
    private int height, gridViewHeight;
    private long mExitTime;//记录点击完返回按钮后的long型时间
    private TextView tvLocation,tvPhe,tvTemp,tvForecast,tvPressure,tvWind,tvHumidity,tvAqi,tvTime;
    private ImageView ivPhe;
    private AMapLocationClientOption mLocationOption;//声明mLocationOption对象
    private AMapLocationClient mLocationClient;//声明AMapLocationClient类对象
    private String cityName = "五指山", cityId = "101310222";
    private double lat = CONST.DEFAULT_LAT, lng = CONST.DEFAULT_LNG;
    private SimpleDateFormat sdf2 = new SimpleDateFormat("HH", Locale.CHINA);
    private SimpleDateFormat sdf3 = new SimpleDateFormat("MM月dd日", Locale.CHINA);
    private SwipeRefreshLayout refreshLayout;//下拉刷新布局

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        MyApplication.addDestoryActivity(MainActivity.this, "MainActivity");
        initRefreshLayout();
        initWidget();
        initGridView();
    }

    /**
     * 初始化下拉刷新布局
     */
    private void initRefreshLayout() {
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
        refreshLayout.setProgressViewEndTarget(true, 400);
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkAuthority();
            }
        });
    }

    private void initWidget() {
        AutoUpdateUtil.checkUpdate(MainActivity.this, mContext, "120", getString(R.string.app_name), true);

        reTitle = findViewById(R.id.reTitle);
        reFact = findViewById(R.id.reFact);
        LinearLayout llLocation = findViewById(R.id.llLocation);
        llLocation.setOnClickListener(this);
        tvLocation = findViewById(R.id.tvLocation);
        tvPhe = findViewById(R.id.tvPhe);
        tvTemp = findViewById(R.id.tvTemp);
        tvForecast = findViewById(R.id.tvForecast);
        tvPressure = findViewById(R.id.tvPressure);
        tvWind = findViewById(R.id.tvWind);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvAqi = findViewById(R.id.tvAqi);
        TextView tvFifth = findViewById(R.id.tvFifth);
        tvFifth.setOnClickListener(this);
        tvTime = findViewById(R.id.tvTime);
        ivPhe = findViewById(R.id.ivPhe);
        ImageView ivControl = findViewById(R.id.ivControl);
        ivControl.setOnClickListener(this);

        getDeviceWidthHeight();
        checkAuthority();
    }

    private void getDeviceWidthHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        height = dm.heightPixels;
    }

    /**
     * 开始定位
     */
    private void startLocation() {
        if (CommonUtil.isLocationOpen(mContext)) {
            if (mLocationOption == null) {
                mLocationOption = new AMapLocationClientOption();//初始化定位参数
                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
                mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
                mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
                mLocationOption.setMockEnable(true);//设置是否允许模拟位置,默认为false，不允许模拟位置
                mLocationOption.setInterval(100000);//设置定位间隔,单位毫秒,默认为2000ms
            }
            if (mLocationClient == null) {
                mLocationClient = new AMapLocationClient(mContext);//初始化定位
                mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
                mLocationClient.setLocationListener(this);
            }
            mLocationClient.startLocation();//启动定位
        }else {
            refresh();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getErrorCode() == 0) {
            cityName = amapLocation.getDistrict();
            lat = amapLocation.getLatitude();
            lng = amapLocation.getLongitude();
            refresh();
        }
    }

    private void refresh() {
        tvLocation.setText(cityName);
        OkHttpGeo(lng, lat);
    }

    /**
     * 获取城市id
     * @param lng
     * @param lat
     */
    private void OkHttpGeo(final double lng, final double lat) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(SecretUrlUtil.geo(lng, lat)).build(), new Callback() {
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
                                        if (!obj.isNull("geo")) {
                                            JSONObject geoObj = obj.getJSONObject("geo");
                                            if (!geoObj.isNull("id")) {
                                                cityId = geoObj.getString("id");
                                                if (!TextUtils.isEmpty(cityId)) {
                                                    getWeatherInfo(cityId);
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
     * 获取天气数据
     * @param cityId
     */
    private void getWeatherInfo(String cityId) {
        FetchWeather fetch = new FetchWeather();
        fetch.perform(cityId, "all");
        fetch.setOnFetchWeatherListener(new FetchWeather.OnFetchWeatherListener() {
            @Override
            public void onFetchWeather(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                JSONArray array = new JSONArray(result);

                                String factTemp = "";
                                //实况
                                if (array.length() > 0) {
                                    JSONObject obj1 = array.getJSONObject(0);
                                    if (!obj1.isNull("l")) {
                                        JSONObject l = obj1.getJSONObject("l");
                                        if (!l.isNull("l7")) {
                                            String time = l.getString("l7");
                                            if (time != null) {
                                                time = sdf3.format(new Date())+" "+time;
                                                tvTime.setText("海南省气象台"+time + "发布");
                                            }
                                        }

                                        if (!l.isNull("l5")) {
                                            String pheCode = WeatherUtil.lastValue(l.getString("l5"));
                                            Drawable drawable;
                                                int current = Integer.parseInt(sdf2.format(new Date()));
                                                if (current >= 6 && current < 18) {
                                                    drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
                                                }else {
                                                    drawable = getResources().getDrawable(R.drawable.phenomenon_drawable_night);
                                                }

                                            drawable.setLevel(Integer.valueOf(pheCode));
                                            ivPhe.setBackground(drawable);
                                            tvPhe.setText(getString(WeatherUtil.getWeatherId(Integer.valueOf(pheCode))));
                                        }

                                        if (!l.isNull("l1")) {
                                            factTemp = WeatherUtil.lastValue(l.getString("l1"));
                                            tvTemp.setText(factTemp);
                                        }

                                        if (!l.isNull("l4")) {
                                            String windDir = WeatherUtil.lastValue(l.getString("l4"));
                                            if (!l.isNull("l3")) {
                                                String windForce = WeatherUtil.lastValue(l.getString("l3"));
                                                tvWind.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir))) + " " +
                                                        WeatherUtil.getFactWindForce(Integer.valueOf(windForce)));
                                            }
                                        }

                                        if (!l.isNull("l2")) {
                                            String humidity = WeatherUtil.lastValue(l.getString("l2"));
                                            tvHumidity.setText("湿度"+" "+humidity + getString(R.string.unit_percent));
                                        }

                                        if (!l.isNull("l10")) {
                                            String humidity = WeatherUtil.lastValue(l.getString("l10"));
                                            tvPressure.setText("气压"+" "+humidity + getString(R.string.unit_hPa));
                                        }
                                    }
                                }

                                //预报
                                if (array.length() > 1) {
                                    JSONObject obj2 = array.getJSONObject(1);
                                    if (!obj2.isNull("f")) {
                                        JSONObject f = obj2.getJSONObject("f");
                                        if (!f.isNull("f1")) {
                                            JSONArray f1 = f.getJSONArray("f1");
                                            if (f1.length() > 0) {
                                                JSONObject weeklyObj = f1.getJSONObject(0);
                                                //晚上
                                                int lowTemp = Integer.valueOf(weeklyObj.getString("fd"));
                                                if (!TextUtils.isEmpty(factTemp) && Integer.valueOf(factTemp) < lowTemp) {
                                                    lowTemp = Integer.valueOf(factTemp);
                                                }

                                                //白天
                                                int highTemp = Integer.valueOf(weeklyObj.getString("fc"));
                                                if (Integer.valueOf(factTemp) > highTemp) {
                                                    highTemp = Integer.valueOf(factTemp);
                                                }

                                                tvForecast.setText(highTemp+" ~ "+lowTemp+getString(R.string.unit_degree));
                                            }
                                        }
                                    }
                                }

                                //aqi
                                if (array.length() > 4) {
                                    JSONObject obj5 = array.getJSONObject(4);
                                    if (!obj5.isNull("p")) {
                                        JSONObject p = obj5.getJSONObject("p");
                                        if (!p.isNull("p2")) {
                                            String aqi = p.getString("p2");
                                            tvAqi.setText("AQI" + " "+ WeatherUtil.getAqi(mContext, Integer.valueOf(aqi)) + " " + aqi);
                                        }
                                    }
                                }

                                refreshLayout.setRefreshing(false);
                                reFact.setVisibility(View.VISIBLE);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }

    private void initGridView() {
        channelList.clear();
        if (!getIntent().hasExtra("dataList")) {
            return;
        }
        List<ColumnData> dataList = getIntent().getExtras().getParcelableArrayList("dataList");
        if (dataList.isEmpty()) {
            return;
        }
        channelList.addAll(dataList);
        gridView = findViewById(R.id.gridView);
        mAdapter = new MainAdapter(mContext, channelList);
        gridView.setAdapter(mAdapter);
        onLayoutMeasure();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ColumnData dto = channelList.get(arg2);
                Intent intent;
                if (TextUtils.equals(dto.id, "586")) {//灾害预警
                    intent = new Intent(mContext, WarningActivity.class);
                    intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("data", dto);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else if (TextUtils.equals(dto.id, "578")) {//台风路径
                    intent = new Intent(mContext, TyphoonRouteActivity.class);
                    intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
                    startActivity(intent);
                }else if (TextUtils.equals(dto.id, "613")) {//实况资料
                    intent = new Intent(mContext, FactActivity.class);
                    intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("data", dto);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else {//农气情报、生态气象、全省预报、灾害监测、橡胶气象、卫星云图
                    intent = new Intent(mContext, PdfTitleActivity.class);
                    intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("data", dto);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 判断navigation是否显示，重新计算页面布局
     */
    private void onLayoutMeasure() {
        getDeviceWidthHeight();

        int statusBarHeight = -1;//状态栏高度
        //获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        reTitle.measure(0, 0);
        int height1 = reTitle.getMeasuredHeight();
        reFact.measure(0, 0);
        int height2 = reFact.getMeasuredHeight();

        gridViewHeight = height-statusBarHeight-height1-height2;

        if (mAdapter != null) {
            mAdapter.height = gridViewHeight;
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(mContext, "再按一次退出"+getString(R.string.app_name), Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivControl:
                startActivity(new Intent(mContext, SettingActivity.class));
                break;
            case R.id.llLocation:
                startActivity(new Intent(mContext, CityActivity.class));
                break;
            case R.id.tvFifth:
                Intent intent = new Intent(mContext, ForecastActivity.class);
                intent.putExtra("cityId", cityId);
                intent.putExtra("cityName", cityName);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                startActivity(intent);
                break;
        }
    }

    /**
     * 申请权限
     */
    private void checkAuthority() {
        if (Build.VERSION.SDK_INT < 23) {
            startLocation();
        }else {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, AuthorityUtil.AUTHOR_LOCATION);
            }else {
                startLocation();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AuthorityUtil.AUTHOR_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocation();
                }else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        AuthorityUtil.intentAuthorSetting(this, "\""+getString(R.string.app_name)+"\""+"需要使用定位权限，是否前往设置？");
                    }
//                    refresh();
                }
                break;
        }
    }

}
