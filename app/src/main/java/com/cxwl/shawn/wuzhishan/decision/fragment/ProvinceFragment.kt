package com.cxwl.shawn.wuzhishan.decision.fragment

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.OnMarkerClickListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.activity.ForecastActivity
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.dto.WeatherDto
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_province.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 全省预报
 */
class ProvinceFragment : BaseFragment(), OnMarkerClickListener {

    private var aMap: AMap? = null
    private val dataList: MutableList<WeatherDto> = ArrayList()
    private val sdf1 = SimpleDateFormat("HH", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("yyyy年MM月dd日HH时", Locale.CHINA)
    private val adcodePolylines: ArrayList<Polyline> = ArrayList() //行政区划边界线


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_province, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showDialog()
        initMap(savedInstanceState)
        initWidget()
    }

    private fun initWidget() {
        tvMapNumber.text = aMap!!.mapContentApprovalNumber
        okHttpStations()
    }

    private fun initMap(bundle: Bundle?) {
        mapView.onCreate(bundle)
        if (aMap == null) {
            aMap = mapView.map
        }
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(19.05, 109.83), 8.0f))
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.setOnMarkerClickListener(this)
        aMap!!.setOnMapLoadedListener { CommonUtil.drawAllDistrict(activity, aMap, adcodePolylines) }
    }

    /**
     * 获取城市的位置信息
     */
    private fun okHttpStations() {
        val url = arguments!!.getString(CONST.WEB_URL)
        if (TextUtils.isEmpty(url)) {
            return
        }
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    activity!!.runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                dataList.clear()
                                val obj = JSONObject(result)
                                if (!obj.isNull("list")) {
                                    val array = obj.getJSONArray("list")
                                    var i = 0
                                    while (i < array.length()) {
                                        val itemObj = array.getJSONObject(i)
                                        val dto = WeatherDto()
                                        if (!itemObj.isNull("name")) {
                                            dto.cityName = itemObj.getString("name")
                                        }
                                        if (!itemObj.isNull("city_id")) {
                                            dto.cityId = itemObj.getString("city_id")
                                        }
                                        if (!itemObj.isNull("geo")) {
                                            val geoArray = itemObj.getJSONArray("geo")
                                            dto.lat = geoArray.getDouble(1)
                                            dto.lng = geoArray.getDouble(0)
                                        }
                                        dataList.add(dto)
                                        i++
                                    }
                                }
                                if (dataList.size > 0) {
                                    var i = 0
                                    while (i < dataList.size) {
                                        val dto = dataList[i]
                                        getAllWeather(dto)
                                        i++
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        cancelDialog()
                    }
                }
            })
        }).start()
    }

    //获取所有天气信息
    private fun getAllWeather(dto: WeatherDto) {
        Thread(Runnable {
            val url = "http://hainan.welife100.com/Public/hnfusion?areaid=${dto.cityId}"
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    if (!TextUtils.isEmpty(result)) {
                        try {
                            val array = JSONArray(result)

                            //实况信息
                            if (dto.cityId.startsWith("101310101")) {
                                val fact = array.getJSONObject(0)
                                if (!fact.isNull("l")) {
                                    val lObj = fact.getJSONObject("l")
                                    if (!lObj.isNull("l13")) {
                                        val time = lObj.getString("l13")
                                        if (!TextUtils.isEmpty(time)) {
                                            try {
                                                tvTitle!!.text = sdf3.format(sdf2.parse(time)) + "发布的市县未来24小时预报"
                                            } catch (e: ParseException) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                            }

                            //逐小时预报信息
                            val hour = array.getJSONObject(3)
                            if (!hour.isNull("jh")) {
                                val jhArray = hour.getJSONArray("jh")
                                val itemObj = jhArray.getJSONObject(0)
                                dto.factPheCode = itemObj.getString("ja")
                                dto.factTemp = itemObj.getString("jb")
                                addMarkers(dto)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        }).start()
    }

    private fun addMarkers(dto: WeatherDto) {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val options = MarkerOptions()
        options.title(dto.cityId)
        options.snippet(dto.cityName)
        options.position(LatLng(dto.lat, dto.lng))
        val mView = inflater.inflate(R.layout.marker_province_station, null)
        val tvName = mView.findViewById<TextView>(R.id.tvName)
        val tvTemp = mView.findViewById<TextView>(R.id.tvTemp)
        val ivPhe = mView.findViewById<ImageView>(R.id.ivPhe)
        if (dto.cityName != null) {
            tvName.text = dto.cityName
        }
        if (dto.factTemp != null) {
            tvTemp.text = "${dto.factTemp}℃"
        }
        if (dto.factPheCode != null) {
            val currentTime = sdf1.format(Date().time)
            val hour = Integer.valueOf(currentTime)
            val drawable: Drawable
            drawable = if (hour in 6..17) {
                resources.getDrawable(R.drawable.phenomenon_drawable)
            } else {
                resources.getDrawable(R.drawable.phenomenon_drawable_night)
            }
            drawable.level = Integer.valueOf(dto.factPheCode)
            ivPhe.background = drawable
        }
        options.icon(BitmapDescriptorFactory.fromView(mView))
        aMap!!.addMarker(options)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val intent = Intent(activity, ForecastActivity::class.java)
        intent.putExtra("cityId", marker.title)
        intent.putExtra("cityName", marker.snippet)
        intent.putExtra("lat", marker.position.latitude)
        intent.putExtra("lng", marker.position.longitude)
        startActivity(intent)
        return true
    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        if (mapView != null) {
            mapView!!.onResume()
        }
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView!!.onPause()
        }
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        if (mapView != null) {
            mapView!!.onDestroy()
        }
    }

}
