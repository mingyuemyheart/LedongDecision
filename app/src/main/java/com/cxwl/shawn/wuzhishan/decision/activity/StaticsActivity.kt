package com.cxwl.shawn.wuzhishan.decision.activity;

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.OnMapClickListener
import com.amap.api.maps.AMap.OnMarkerClickListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.animation.ScaleAnimation
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.dto.WeatherStaticsDto
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import com.cxwl.shawn.wuzhishan.decision.util.SecretUrlUtil
import com.cxwl.shawn.wuzhishan.decision.view.CircularProgressBar
import kotlinx.android.synthetic.main.activity_statics.*
import kotlinx.android.synthetic.main.activity_statics.view.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 天气统计
 */
class StaticsActivity : BaseActivity(), OnClickListener, OnMarkerClickListener, OnMapClickListener {
	
	private var aMap : AMap? = null
	private val dataList : ArrayList<WeatherStaticsDto> = ArrayList()
	private val markerList : ArrayList<Marker> = ArrayList()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_statics)
		initWidget()
		initMap(savedInstanceState)
	}

	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		llBack.setOnClickListener(this)
		tvTitle.text = "天气统计"
	}
	
	/**
	 * 初始化地图
	 */
	private fun initMap(bundle : Bundle?) {
		mapView.onCreate(bundle)
		if (aMap == null) {
			aMap = mapView.map
		}
		aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(19.05, 109.83),8.0f))
		aMap!!.uiSettings.isZoomControlsEnabled = false
		aMap!!.uiSettings.isRotateGesturesEnabled = false
		aMap!!.setOnMarkerClickListener(this)
		aMap!!.setOnMapClickListener(this)
		aMap!!.setOnMapLoadedListener {
			okHttpStations()
		}
		tvMapNumber.text = aMap!!.mapContentApprovalNumber
	}

	private fun okHttpStations() {
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().url(SecretUrlUtil.statistic()).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					if (!TextUtils.isEmpty(result)) {
						dataList.clear()
						parseStationInfo(result, "level1", dataList)
						parseStationInfo(result, "level2", dataList)
						parseStationInfo(result, "level3", dataList)
						addMarker(dataList)
					}
				}
			})
		}).start()
	}
	
	/**
	 * 解析数据
	 */
	private fun parseStationInfo(result : String, level : String, list : ArrayList<WeatherStaticsDto>) {
		val obj = JSONObject(result)
		if (!obj.isNull(level)) {
			val array = JSONArray(obj.getString(level))
			for (i in 0 until array.length()) {
				val dto = WeatherStaticsDto()
				val itemObj = array.getJSONObject(i)
				if (!itemObj.isNull("name")) {
					dto.name = itemObj.getString("name")
				}
				if (!itemObj.isNull("stationid")) {
					dto.stationId = itemObj.getString("stationid")
				}
				if (!itemObj.isNull("level")) {
					dto.level = itemObj.getString("level")
				}
				if (!itemObj.isNull("areaid")) {
					dto.areaId = itemObj.getString("areaid")
				}
				if (!itemObj.isNull("lat")) {
					dto.latitude = itemObj.getString("lat")
				}
				if (!itemObj.isNull("lon")) {
					dto.longitude = itemObj.getString("lon")
				}
				if (!TextUtils.isEmpty(dto.areaId) && dto.areaId.startsWith("10131")) {
					list.add(dto)
				}
			}
		}
	}
	
	/**
	 * 给marker添加文字
	 * @param name 城市名称
	 * @return
	 */
	private fun getTextBitmap(name : String) : View {
		val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
		val view = inflater.inflate(R.layout.marker_statistic, null)
		var cityName : String = name
		if (!TextUtils.isEmpty(name) && name.length > 2) {
			cityName = name.substring(0, 2)+"\n"+name.substring(2, name.length)
		}
		view.tvName.text = cityName
		return view
	}
	
	private fun markerExpandAnimation(marker : Marker) {
		val animation = ScaleAnimation(0.0f,1.0f,0.0f,1.0f)
		animation.setInterpolator(LinearInterpolator())
		animation.setDuration(300)
		marker.setAnimation(animation)
		marker.startAnimation()
	}
	
	private fun markerColloseAnimation(marker: Marker) {
		val animation = ScaleAnimation(1.0f,0.0f,1.0f,0.0f)
		animation.setInterpolator(LinearInterpolator())
		animation.setDuration(300)
		marker.setAnimation(animation)
		marker.startAnimation()
	}
	
	private fun removeMarkers() {
		for (i in markerList.indices) {
			val marker = markerList[i]
			markerColloseAnimation(marker)
			marker.remove()
		}
		markerList.clear()
	}
	
	/**
	 * 添加marker
	 */
	private fun addMarker(list : ArrayList<WeatherStaticsDto>) {
		if (list.isEmpty()) {
			return
		}
		for (i in list.indices) {
			val dto = list[i]
			val options = MarkerOptions()
			options.title(dto.areaId)
			options.anchor(0.5f, 0.5f)
			options.position(LatLng(dto.latitude.toDouble(), dto.longitude.toDouble()))
			options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.name)))
			val marker = aMap!!.addMarker(options)
			markerList.add(marker)
			markerExpandAnimation(marker)
		}
	}

	override fun onMapClick(p0: LatLng?) {
		if (reDetail.visibility == View.VISIBLE) {
			hideAnimation(reDetail)
		}
	}

	/**
	 * 向上弹出动画
	 * @param layout
	 */
	private fun showAnimation(layout : View) {
		val animation = TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 1.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f)
		animation.duration = 300
		layout.startAnimation(animation)
		layout.visibility = View.VISIBLE
	}
	
	/**
	 * 向下隐藏动画
	 * @param layout
	 */
	private fun hideAnimation(layout : View) {
		val animation = TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
				TranslateAnimation.RELATIVE_TO_SELF, 1.0f)
		animation.duration = 300
		layout.startAnimation(animation)
		layout.visibility = View.GONE
	}

	override fun onMarkerClick(marker: Marker): Boolean {
		showAnimation(reDetail)
		var name : String? = null
		var stationId : String? = null
		for (i in dataList.indices) {
			val dto = dataList[i]
			if (TextUtils.equals(marker.title, dto.areaId)) {
				stationId = dto.stationId
				name = dto.name
				break
			}
		}
		tvName.text = name + " " + stationId
		tvDetail.text = ""
		progressBar.visibility = View.VISIBLE
		reContent.visibility = View.INVISIBLE

		val url = SecretUrlUtil.statisticDetail(stationId)
		okHttpDetail(url)
		return true
	}

	private fun okHttpDetail(url : String) {
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					runOnUiThread {
						progressBar.visibility = View.INVISIBLE
						reContent.visibility = View.VISIBLE
						if (!TextUtils.isEmpty(result)) {
							val obj = JSONObject(result)
							val sdf = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
							val sdf2 = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
							val startTime = sdf2.format(sdf.parse(obj.getString("starttime")))
							val endTime = sdf2.format(sdf.parse(obj.getString("endtime")))

							var no_rain_lx = obj.getInt("no_rain_lx").toString()//连续没雨天数
							if (TextUtils.equals(no_rain_lx, "-1")) {
								no_rain_lx = getString(R.string.no_statics)
							}else {
								no_rain_lx += "天"
							}

							var mai_lx = obj.getInt("mai_lx").toString()//连续霾天数
							if (TextUtils.equals(mai_lx, "-1")) {
								mai_lx = getString(R.string.no_statics)
							}else {
								mai_lx += "天"
							}

							var highTemp : String? = null//高温
							var lowTemp : String? = null//低温
							var highWind : String? = null//最大风速
							var highRain : String? = null//最大降水量

							if (!obj.isNull("count")) {
								val array = JSONArray(obj.getString("count"))
								val itemObj0 = array.getJSONObject(0)//温度
								val itemObj1 = array.getJSONObject(1)//降水
								val itemObj5 = array.getJSONObject(5)//风速

								if (!itemObj0.isNull("max") && !itemObj0.isNull("min")) {
									highTemp = itemObj0.getString("max")
									highTemp = if (TextUtils.equals(highTemp, "-1.0")) {
										getString(R.string.no_statics)
									}else {
										"$highTemp℃"
									}
									lowTemp = itemObj0.getString("min")
									lowTemp = if (TextUtils.equals(lowTemp, "-1.0")) {
										getString(R.string.no_statics)
									}else {
										"$lowTemp℃"
									}
								}
								if (!itemObj1.isNull("max")) {
									highRain = itemObj1.getString("max")
									highRain = if (TextUtils.equals(highRain, "-1.0")) {
										getString(R.string.no_statics)
									}else {
										highRain+"mm"
									}
								}
								if (!itemObj5.isNull("max")) {
									highWind = itemObj5.getString("max")
									highWind = if (TextUtils.equals(highWind, "-1.0")) {
										getString(R.string.no_statics)
									}else {
										highWind+"m/s"
									}
								}
							}

							if (startTime != null && endTime != null && highTemp != null && lowTemp != null && highWind != null && highRain != null) {
								val buffer = StringBuffer()
								buffer.append(getString(R.string.from)).append(startTime)
								buffer.append(getString(R.string.to)).append(endTime)
								buffer.append("：\n")
								buffer.append(getString(R.string.highest_temp)).append(highTemp).append("，")
								buffer.append(getString(R.string.lowest_temp)).append(lowTemp).append("，")
								buffer.append(getString(R.string.max_speed)).append(highWind).append("，")
								buffer.append(getString(R.string.max_fall)).append(highRain).append("，")
								buffer.append(getString(R.string.lx_no_fall)).append(no_rain_lx).append("，")
								buffer.append(getString(R.string.lx_no_mai)).append(mai_lx).append("。")

								val builder = SpannableStringBuilder(buffer.toString())
								val builderSpan1 = ForegroundColorSpan(ContextCompat.getColor(this@StaticsActivity, R.color.builder))
								val builderSpan2 = ForegroundColorSpan(ContextCompat.getColor(this@StaticsActivity, R.color.builder))
								val builderSpan3 = ForegroundColorSpan(ContextCompat.getColor(this@StaticsActivity, R.color.builder))
								val builderSpan4 = ForegroundColorSpan(ContextCompat.getColor(this@StaticsActivity, R.color.builder))
								val builderSpan5 = ForegroundColorSpan(ContextCompat.getColor(this@StaticsActivity, R.color.builder))
								val builderSpan6 = ForegroundColorSpan(ContextCompat.getColor(this@StaticsActivity, R.color.builder))

								builder.setSpan(builderSpan1, 29, 29+highTemp.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
								builder.setSpan(builderSpan2, 29+highTemp.length+6, 29+highTemp.length+6+lowTemp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
								builder.setSpan(builderSpan3, 29+highTemp.length+6+lowTemp.length+6, 29+highTemp.length+6+lowTemp.length+6+highWind.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
								builder.setSpan(builderSpan4, 29+highTemp.length+6+lowTemp.length+6+highWind.length+7, 29+highTemp.length+6+lowTemp.length+6+highWind.length+7+highRain.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
								builder.setSpan(builderSpan5, 29+highTemp.length+6+lowTemp.length+6+highWind.length+7+highRain.length+8, 29+highTemp.length+6+lowTemp.length+6+highWind.length+7+highRain.length+8+no_rain_lx.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
								builder.setSpan(builderSpan6, 29+highTemp.length+6+lowTemp.length+6+highWind.length+7+highRain.length+8+no_rain_lx.length+6, 29+highTemp.length+6+lowTemp.length+6+highWind.length+7+highRain.length+8+no_rain_lx.length+6+mai_lx.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
								tvDetail.text = builder

								val start = sdf2.parse(startTime).time
								val end = sdf2.parse(endTime).time
								val dayCount = ((end - start) / (1000*60*60*24)).toFloat() + 1
								if (!obj.isNull("tqxxcount")) {
									val array = JSONArray(obj.getString("tqxxcount"))
									for (i in 0 until array.length()) {
										val itemObj = array.getJSONObject(i)
										val name = itemObj.getString("name")
										val value = itemObj.getInt("value")
										if (i == 0) {
											if (value == -1) {
												tvBar1.text = name + "\n" + "--"
												animate(bar1,  0.0f, 1000)
												bar1.progress = 0.0f
											}else {
												tvBar1.text = name + "\n" + value + "天"
												animate(bar1, -value/dayCount, 1000)
												bar1.progress = -value/dayCount
											}
										}else if (i == 1) {
											if (value == -1) {
												tvBar2.text = name + "\n" + "--"
												animate(bar2,  0.0f, 1000)
												bar2.progress = 0.0f
											}else {
												tvBar2.text = name + "\n" + value + "天"
												animate(bar2,  -value/dayCount, 1000)
												bar2.progress = -value/dayCount
											}
										}else if (i == 2) {
											if (value == -1) {
												tvBar3.text = name + "\n" + "--"
												animate(bar3,  0.0f, 1000)
												bar3.progress = 0.0f
											}else {
												tvBar3.text = name + "\n" + value + "天"
												animate(bar3,  -value/dayCount, 1000)
												bar3.progress = -value/dayCount
											}
										}else if (i == 3) {
											if (value == -1) {
												tvBar4.text = name + "\n" + "--"
												animate(bar4,  0.0f, 1000)
												bar4.progress = 0.0f
											}else {
												tvBar4.text = name + "\n" + value + "天"
												animate(bar4,  -value/dayCount, 1000)
												bar4.progress = -value/dayCount
											}
										}else if (i == 4) {
											if (value == -1) {
												tvBar5.text = name + "\n" + "--"
												animate(bar5,  0.0f, 1000)
												bar5.progress = 0.0f
											}else {
												tvBar5.text = name + "\n" + value + "天"
												animate(bar5,  -value/dayCount, 1000)
												bar5.progress = -value/dayCount
											}
										}
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
	 * 进度条动画
	 * @param progressBar
	 * @param listener
	 * @param progress
	 * @param duration
	 */
	private fun animate(progressBar : CircularProgressBar, progress : Float, duration : Long) {
		val mProgressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", progress)
		mProgressBarAnimator.duration = duration
		mProgressBarAnimator.addListener(object : AnimatorListener{
			override fun onAnimationRepeat(p0: Animator?) {
			}
			override fun onAnimationEnd(p0: Animator?) {
				progressBar.progress = progress
			}
			override fun onAnimationCancel(p0: Animator?) {
			}
			override fun onAnimationStart(p0: Animator?) {
			}
		})
		mProgressBarAnimator.reverse()
		mProgressBarAnimator.addUpdateListener { p0 -> progressBar.progress = p0!!.animatedValue as Float }
		mProgressBarAnimator.start()
	}
	
	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (reDetail.visibility == View.VISIBLE) {
				hideAnimation(reDetail)
				return false
			} else {
				finish()
			}
		}
		return super.onKeyDown(keyCode, event)
	}

	override fun onClick(view: View?) {
		when(view!!.id) {
			R.id.llBack -> {
				if (reDetail.visibility == View.VISIBLE) {
					hideAnimation(reDetail)
				} else {
					finish()
				}
			}
		}
	}

	override fun onResume() {
		super.onResume()
		if (mapView != null) {
			mapView.onResume()
		}
	}

	override fun onPause() {
		super.onPause()
		if (mapView != null) {
			mapView.onPause()
		}
	}

	override fun onSaveInstanceState(outState: Bundle?) {
		super.onSaveInstanceState(outState)
		if (mapView != null) {
			mapView.onSaveInstanceState(outState)
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		if (mapView != null) {
			mapView.onDestroy()
		}
	}

}
