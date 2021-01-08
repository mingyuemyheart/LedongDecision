package com.cxwl.shawn.wuzhishan.decision.activity;

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.adapter.MainAdapter
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.common.MyApplication
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData
import com.cxwl.shawn.wuzhishan.decision.util.*
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivity(), View.OnClickListener, AMapLocationListener {

    private var mContext : Context? = null
    private var mAdapter : MainAdapter? = null
    private val channelList : ArrayList<ColumnData> = ArrayList()
    private var mExitTime : Long = 0//记录点击完返回按钮后的long型时间
    private var mLocationOption : AMapLocationClientOption? = null//声明mLocationOption对象
    private var mLocationClient : AMapLocationClient? = null//声明AMapLocationClient类对象
    private var cityName = "乐东"
    private var cityId = "101310221"
    private var lat = CONST.DEFAULT_LAT
    private var lng = CONST.DEFAULT_LNG
    private val sdf2 = SimpleDateFormat("HH", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("MM月dd日", Locale.CHINA)
    private val mUIHandler : Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this
        MyApplication.addDestoryActivity(this@MainActivity, "MainActivity")
        initRefreshLayout()
        initWidget()
        initGridView()
    }

    /**
     * 初始化下拉刷新布局
     */
    private fun initRefreshLayout() {
        refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4)
        refreshLayout.setProgressViewEndTarget(true, 400)
        refreshLayout.isRefreshing = true
        refreshLayout.setOnRefreshListener {
            checkAuthority()
        }
    }

    private fun initWidget() {
        AutoUpdateUtil.checkUpdate(this@MainActivity, mContext, "136", getString(R.string.app_name), true)

        llLocation.setOnClickListener(this)
        tvFifth.setOnClickListener(this)
        ivControl.setOnClickListener(this)

        checkAuthority()
    }

    /**
     * 开始定位
     */
    private fun startLocation() {
        if (CommonUtil.isLocationOpen(mContext)) {
            if (mLocationOption == null) {
                mLocationOption = AMapLocationClientOption()//初始化定位参数
                mLocationOption!!.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
                mLocationOption!!.isNeedAddress = true//设置是否返回地址信息（默认返回地址信息）
                mLocationOption!!.isOnceLocation = true//设置是否只定位一次,默认为false
                mLocationOption!!.isMockEnable = true//设置是否允许模拟位置,默认为false，不允许模拟位置
                mLocationOption!!.interval = 100000//设置定位间隔,单位毫秒,默认为2000ms
            }
            if (mLocationClient == null) {
                mLocationClient = AMapLocationClient(mContext)//初始化定位
                mLocationClient!!.setLocationOption(mLocationOption)//给定位客户端对象设置定位参数
                mLocationClient!!.setLocationListener(this)
            }
            mLocationClient!!.startLocation()//启动定位
        }else {
            refresh()
        }
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (amapLocation != null && amapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
            cityName = amapLocation.district
            lat = amapLocation.latitude
            lng = amapLocation.longitude
            tvLocation.text = cityName
            okHttpGeo(lng, lat)
        }
    }

    private fun refresh() {
        startLocation()
    }

    /**
     * 获取城市id
     * @param lng
     * @param lat
     */
    private fun okHttpGeo(lng : Double, lat : Double) {
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(SecretUrlUtil.geo(lng, lat)).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    mUIHandler.post {
                        if (!TextUtils.isEmpty(result)) {
                            val obj = JSONObject(result)
                            if (!obj.isNull("geo")) {
                                val geoObj = obj.getJSONObject("geo")
                                if (!geoObj.isNull("id")) {
                                    cityId = geoObj.getString("id")
                                    if (!TextUtils.isEmpty(cityId)) {
                                        getWeatherInfo(cityId)
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }).start()
    }

    /**
     * 获取天气数据
     * @param cityId
     */
    private fun getWeatherInfo(cityId : String) {
        val fetch = FetchWeather()
        fetch.perform(cityId, "all")
        fetch.onFetchWeatherListener = FetchWeather.OnFetchWeatherListener { result ->
            mUIHandler.post {
                if (!TextUtils.isEmpty(result)) {
                    val array = JSONArray(result)

                    var factTemp = ""
                    //实况
                    if (array.length() > 0) {
                        val obj1 = array.getJSONObject(0)
                        if (!obj1.isNull("l")) {
                            val l = obj1.getJSONObject("l")
                            if (!l.isNull("l7")) {
                                var time = l.getString("l7")
                                if (time != null) {
                                    time = sdf3.format(Date())+" "+time
                                    tvTime.text = "海南省气象台"+time + "发布"
                                }
                            }

                            if (!l.isNull("l5")) {
                                val pheCode = WeatherUtil.lastValue(l.getString("l5"))
                                var drawable : Drawable? = null
                                val current = Integer.parseInt(sdf2.format(Date()))
                                drawable = if (current in 6..17) {
                                    ContextCompat.getDrawable(this@MainActivity, R.drawable.phenomenon_drawable)
                                }else {
                                    ContextCompat.getDrawable(this@MainActivity, R.drawable.phenomenon_drawable_night)
                                }
                                drawable!!.level = pheCode.toInt()
                                ivPhe.background = drawable
                                tvPhe.text = getString(WeatherUtil.getWeatherId(pheCode.toInt()))
                            }

                            if (!l.isNull("l1")) {
                                factTemp = WeatherUtil.lastValue(l.getString("l1"))
                                tvTemp.text = factTemp
                            }

                            if (!l.isNull("l4")) {
                                val windDir = WeatherUtil.lastValue(l.getString("l4"))
                                if (!l.isNull("l3")) {
                                    val windForce = WeatherUtil.lastValue(l.getString("l3"))
                                    tvWind.text = getString(WeatherUtil.getWindDirection(windDir.toInt())) + " " +
                                            WeatherUtil.getFactWindForce(windForce.toInt())
                                }
                            }

                            if (!l.isNull("l2")) {
                                tvHumidity.text = "湿度"+" "+WeatherUtil.lastValue(l.getString("l2")) + getString(R.string.unit_percent)
                            }

                            if (!l.isNull("l10")) {
                                tvPressure.text = "气压"+" "+WeatherUtil.lastValue(l.getString("l10")) + getString(R.string.unit_hPa)
                            }
                        }
                    }

                    //预报
                    if (array.length() > 1) {
                        val obj2 = array.getJSONObject(1)
                        if (!obj2.isNull("f")) {
                            val f = obj2.getJSONObject("f")
                            if (!f.isNull("f1")) {
                                val f1 = f.getJSONArray("f1")
                                if (f1.length() > 0) {
                                    val weeklyObj = f1.getJSONObject(0)
                                    var lowTemp = weeklyObj.getString("fd").toInt()
                                    if (!TextUtils.isEmpty(factTemp) && factTemp.toInt() < lowTemp) {
                                        lowTemp = factTemp.toInt()
                                    }
                                    var highTemp = weeklyObj.getString("fc").toInt()
                                    if (!TextUtils.isEmpty(factTemp) && factTemp.toInt() > highTemp) {
                                        highTemp = factTemp.toInt()
                                    }
                                    tvForecast.text = highTemp.toString()+" ~ "+lowTemp.toString()+getString(R.string.unit_degree)
                                }
                            }
                        }
                    }

                    //aqi
                    if (array.length() > 4) {
                        val obj5 = array.getJSONObject(4)
                        if (!obj5.isNull("p")) {
                            val p = obj5.getJSONObject("p")
                            if (!p.isNull("p2")) {
                                val aqi = p.getString("p2")
                                tvAqi.text = "AQI" + " "+ WeatherUtil.getAqi(mContext, aqi.toInt()) + " " + aqi
                            }
                        }
                    }

                    refreshLayout.isRefreshing = false
                    reFact.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun initGridView() {
        channelList.clear()
        if (!intent.hasExtra("dataList")) {
            return
        }
        val dataList = intent.extras!!.getParcelableArrayList<ColumnData>("dataList")
        if (dataList.isEmpty()) {
            return
        }
        channelList.addAll(dataList)
        mAdapter = MainAdapter(mContext, channelList)
        gridView.adapter = mAdapter
        onLayoutMeasure()
        gridView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, arg2, l ->
            val dto = channelList[arg2]
            val intent : Intent?
            when(dto.id) {
                "586" -> {//灾害预警
                    intent = Intent(mContext, WarningActivity::class.java)
                    intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
//                    intent = Intent(mContext, PdfTitleActivity::class.java)
//                    intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
//                    dto.child.clear()
//                    for (i in 0 .. 2) {
//                        val data = ColumnData()
//                        when(i) {
//                            0 -> {
//                                data.name = "病虫害气象条件预报"
//                                data.dataUrl = ""
//                                data.id = "58601"
//                                dto.child.add(data)
//                            }
//                            1 -> {
//                                data.name = "气象灾害"
//                                data.dataUrl = ""
//                                data.id = "58602"
//                                dto.child.add(data)
//                            }
//                            2 -> {
//                                data.name = "农气灾害"
//                                data.dataUrl = ""
//                                data.id = "58603"
//                                dto.child.add(data)
//                            }
//                        }
//                    }
                    val bundle = Bundle()
                    bundle.putParcelable("data", dto)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
                "578" -> {//台风路径
                    intent = Intent(mContext, TyphoonRouteActivity::class.java)
                    intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                    startActivity(intent)
                }
                "613" -> {//实况资料
                    intent = Intent(mContext, FactActivity::class.java)
                    intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                    val bundle = Bundle()
                    bundle.putParcelable("data", dto)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
                else -> {//农气情报、生态气象、全省预报、灾害监测、橡胶气象、卫星云图
                    intent = Intent(mContext, PdfTitleActivity::class.java)
                    intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                    val bundle = Bundle()
                    bundle.putParcelable("data", dto)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * 判断navigation是否显示，重新计算页面布局
     */
    private fun onLayoutMeasure() {
        var statusBarHeight = -1;//状态栏高度
        //获取status_bar_height资源的ID
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        reTitle.measure(0, 0)
        val height1 = reTitle.measuredHeight
        reFact.measure(0, 0)
        val height2 = reFact.measuredHeight
        if (mAdapter != null) {
            mAdapter!!.setHeight(CommonUtil.heightPixels(this)-statusBarHeight-height1-height2)
            mAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(mContext, "再按一次退出"+getString(R.string.app_name), Toast.LENGTH_SHORT).show()
                mExitTime = System.currentTimeMillis()
                return true
            } else {
                finish()
            }
        }
        return false
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.ivControl -> startActivity(Intent(mContext, SettingActivity::class.java))
            R.id.llLocation -> startActivity(Intent(mContext, CityActivity::class.java))
            R.id.tvFifth -> {
                val intent = Intent(mContext, ForecastActivity::class.java)
                intent.putExtra("cityId", cityId)
                intent.putExtra("cityName", cityName)
                intent.putExtra("lat", lat)
                intent.putExtra("lng", lng)
                startActivity(intent)
            }
        }
    }

    //需要申请的所有权限
    private val allPermissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    //拒绝的权限集合
    private var deniedList: MutableList<String> = ArrayList()

    /**
     * 申请定位权限
     */
    private fun checkAuthority() {
        if (Build.VERSION.SDK_INT < 23) {
            refresh()
        } else {
            deniedList.clear()
            for (permission in allPermissions) {
                if (ContextCompat.checkSelfPermission(mContext!!, permission) !== PackageManager.PERMISSION_GRANTED) {
                    deniedList.add(permission)
                }
            }
            if (deniedList.isEmpty()) { //所有权限都授予
                refresh()
            } else {
                val permissions = deniedList.toTypedArray() //将list转成数组
                ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AuthorityUtil.AUTHOR_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocation()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    AuthorityUtil.intentAuthorSetting(this, "\"" + getString(R.string.app_name) + "\"" + "需要使用定位权限、存储权限，是否前往设置？")
                }
            }
        }
    }

}
