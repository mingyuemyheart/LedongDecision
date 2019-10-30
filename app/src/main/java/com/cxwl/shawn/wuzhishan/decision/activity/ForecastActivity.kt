package com.cxwl.shawn.wuzhishan.decision.activity

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.adapter.WeeklyForecastAdapter
import com.cxwl.shawn.wuzhishan.decision.dto.WeatherDto
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil
import com.cxwl.shawn.wuzhishan.decision.util.FetchWeather
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import com.cxwl.shawn.wuzhishan.decision.util.WeatherUtil
import com.cxwl.shawn.wuzhishan.decision.view.CubicView
import com.cxwl.shawn.wuzhishan.decision.view.WeeklyView
import kotlinx.android.synthetic.main.activity_forecast.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 天气预报
 * @author shawn_sun
 *
 */
class ForecastActivity : BaseActivity(), OnClickListener {

	private val mUIHandler : Handler = Handler()
	private val sdf1 = SimpleDateFormat("HH", Locale.CHINA)
	private val sdf2 = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
	private val sdf3 = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
	private var mAdapter : WeeklyForecastAdapter? = null
	private val weeklyList : ArrayList<WeatherDto> = ArrayList()
	private var lat : Double = 0.0
	private var lng : Double = 0.0
	private var l7 : String? = null; var l5 : String? = null;var l1 : String? = null;var l4 : String? = null;var l3 : String? = null;var l2 : String? = null;var l10 : String? = null//基本站
	private var nl7 : String? = null; var nl5 : String? = null;var nl1 : String? = null;var nl4 : String? = null;var nl3 : String? = null;var nl2 : String? = null;var nl10 : String? = null//最近站

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_forecast)
		showDialog()
		initWidget()
		initListView()
	}

	private fun initWidget() {
		llBack.setOnClickListener(this)
		tvTitle.text = "天气详情"
		tvTime.isFocusable = true
		tvTime.isFocusableInTouchMode = true
		tvTime.requestFocus()
		ivList.setOnClickListener(this)
		tvFact1.setOnClickListener(this)
		tvFact2.setOnClickListener(this)
		
		val cityName = intent.getStringExtra("cityName")
		if (!TextUtils.isEmpty(cityName)) {
			tvLocation.text = cityName
		}
		lat = intent.getDoubleExtra("lat", 0.0)
		lng = intent.getDoubleExtra("lng", 0.0)
		val cityId = intent.getStringExtra("cityId")
		if (!TextUtils.isEmpty(cityId)) {
			OkHttpWeatherInfo(cityId)
		}
	}

	/**
	 * 初始化listview
	 */
	private fun initListView() {
		mAdapter = WeeklyForecastAdapter(this, weeklyList)
		listView.adapter = mAdapter
	}
	
	/**
	 * 获取天气数据
	 */
	private fun OkHttpWeatherInfo(cityId : String) {
		Thread(Runnable {
			val url : String?
			if (cityId.startsWith("10131")) {
				llFactButton.visibility = View.VISIBLE
				if (lat != 0.0 && lng != 0.0) {
					url = String.format("http://data-fusion.tianqi.cn/datafusion/test?type=HN&ID=%s&lonlat=%s,%s", cityId, lng, lat)
				}else {
					url = "http://data-fusion.tianqi.cn/datafusion/test?type=HN&ID="+cityId
				}
			}else {
				llFactButton.visibility = View.GONE
				url = FetchWeather.weather2Url(cityId, "all")
			}
			OkHttpUtil.enqueue(Request.Builder().url(url!!).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
					if (url.startsWith("http://data-fusion.tianqi.cn/datafusion/")) {
						OkHttpWeatherInfo(FetchWeather.weather2Url(cityId, "all"))
					}
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					mUIHandler.post {
						if (!TextUtils.isEmpty(result)) {
							val array = JSONArray(result)

							val fact = array.getJSONObject(0)
							if (!fact.isNull("l")) {
								val obj = fact.getJSONObject("l")
								//实况信息
								if (!obj.isNull("l7")) {
									l7 = obj.getString("l7")
								}
								if (!obj.isNull("l5")) {
									l5 = obj.getString("l5")
								}
								if (!obj.isNull("l1")) {
									l1 = obj.getString("l1")
								}
								if (!obj.isNull("l4")) {
									l4 = obj.getString("l4")
									if (!obj.isNull("l3")) {
										l3 = obj.getString("l3")
									}
								}
								if (!obj.isNull("l2")) {
									l2 = obj.getString("l2")
								}
								if (!obj.isNull("l10")) {
									l10 = obj.getString("l10")
								}
							}

							if (array.length() > 6) {
								val nfact = array.getJSONObject(6)
								if (!nfact.isNull("nl")) {
									val obj = nfact.getJSONObject("nl")

									//实况信息
									if (!obj.isNull("l7")) {
										nl7 = obj.getString("l7");
									}
									if (!obj.isNull("l5")) {
										nl5 = obj.getString("l5");
									}
									if (!obj.isNull("l1")) {
										nl1 = obj.getString("l1");
									}
									if (!obj.isNull("l4")) {
										nl4 = obj.getString("l4");
										if (!obj.isNull("l3")) {
											nl3 = obj.getString("l3");
										}
									}
									if (!obj.isNull("l2")) {
										nl2 = obj.getString("l2");
									}

									if (!obj.isNull("l10")) {
										nl10 = obj.getString("l10");
									}
								}
							}

							switchHNFactData(true)

							//城市信息
							val city = array.getJSONObject(1)
							if (!city.isNull("f")) {
								val fObj = city.getJSONObject("f")

								val f0 = sdf3.format(sdf2.parse(fObj.getString("f0")))
								val time = sdf3.parse(f0).time
								val currentDate = sdf3.parse(sdf3.format(Date())).time

								if (!fObj.isNull("f1")) {
									weeklyList.clear()
									val currentTime = sdf1.format(Date().time)
									val hour = Integer.valueOf(currentTime)
									val f1 = fObj.getJSONArray("f1")
									for (i in 0 until f1.length()) {
										val dto = WeatherDto()
										val weeklyObj = f1.getJSONObject(i)
										//晚上
										dto.lowPheCode = weeklyObj.getString("fb").toInt()
										dto.lowPhe = getString(WeatherUtil.getWeatherId(weeklyObj.getString("fb").toInt()))
										dto.lowTemp = weeklyObj.getString("fd").toInt()

										//白天
										dto.highPheCode = weeklyObj.getString("fa").toInt()
										dto.highPhe = getString(WeatherUtil.getWeatherId(weeklyObj.getString("fa").toInt()))
										dto.highTemp = weeklyObj.getString("fc").toInt()

										if (hour in 6..18) {
											dto.windDir = weeklyObj.getString("fe").toInt()
											dto.windForce = weeklyObj.getString("fg").toInt()
											if (hour >= 17 || hour <= 7) {
												if (i <= 6) {
													dto.windForceString = dto.windForce.toString()+"级"
												}else {
													dto.windForceString = WeatherUtil.getDayWindForce(dto.windForce)
												}
											}else {
												if (i <= 2) {
													dto.windForceString = dto.windForce.toString()+"级"
												}else {
													dto.windForceString = WeatherUtil.getDayWindForce(dto.windForce)
												}
											}
										}else {
											dto.windDir = weeklyObj.getString("ff").toInt()
											dto.windForce = weeklyObj.getString("fh").toInt()
											if (hour >= 17 || hour <= 7) {
												if (i <= 6) {
													dto.windForceString = dto.windForce.toString()+"级"
												}else {
													dto.windForceString = WeatherUtil.getDayWindForce(dto.windForce)
												}
											}else {
												if (i <= 2) {
													dto.windForceString = dto.windForce.toString()+"级"
												}else {
													dto.windForceString = WeatherUtil.getDayWindForce(dto.windForce)
												}
											}
										}

										dto.date = sdf3.format(Date(time+1000*60*60*24*i))//日期
										if (currentDate > time) {
											dto.week = CommonUtil.getWeek(this@ForecastActivity, i-1)//星期几
										}else {
											dto.week = CommonUtil.getWeek(this@ForecastActivity, i)//星期几
										}

										weeklyList.add(dto)
									}

									if (mAdapter != null) {
										mAdapter!!.foreTime = time
										mAdapter!!.currentTime = currentDate
										mAdapter!!.notifyDataSetChanged()

										//一周预报曲线
										val weeklyView = WeeklyView(this@ForecastActivity)
										weeklyView.setData(weeklyList, time, currentDate)
										llContainer2.removeAllViews()
										llContainer2.addView(weeklyView, CommonUtil.widthPixels(this@ForecastActivity)*2, CommonUtil.dip2px(this@ForecastActivity, 360.0f).toInt())
									}

								}
							}

							//空气质量
							if (!array.isNull(4)) {
								val aqiObj = array.getJSONObject(4)
								if (!aqiObj.isNull("p")) {
									val itemObj = aqiObj.getJSONObject("p")
									if (!itemObj.isNull("p2")) {
										val aqi = itemObj.getString("p2")
										tvAqi.text = "AQI" + " "+ WeatherUtil.getAqi(this@ForecastActivity, aqi.toInt()) + " " + aqi
									}
								}
							}

							//逐小时预报信息
							val hour = array.getJSONObject(3)
							if (!hour.isNull("jh")) {
								val hourlyList : ArrayList<WeatherDto> = ArrayList()
								val jhArray = hour.getJSONArray("jh")
								for (i in 0 until jhArray.length()) {
									val itemObj = jhArray.getJSONObject(i)
									val dto = WeatherDto()
									dto.hourlyCode = itemObj.getString("ja").toInt()
									dto.hourlyTemp = itemObj.getString("jb").toFloat()
									dto.hourlyTime = itemObj.getString("jf")
									dto.hourlyWindDirCode = itemObj.getString("jc").toInt()
									dto.hourlyWindForceCode = itemObj.getString("jd").toInt()
									hourlyList.add(dto)
								}
								//逐小时预报信息
								val cubicView = CubicView(this@ForecastActivity)
								cubicView.setData(hourlyList)
								llContainer1.removeAllViews()
								llContainer1.addView(cubicView, CommonUtil.widthPixels(this@ForecastActivity)*2, CommonUtil.dip2px(this@ForecastActivity, 300.0f).toInt())
							}

							//海南逐小时预报信息
							if (array.length() > 5) {
								val hnHour = array.getJSONObject(5)
								if (!hnHour.isNull("njh")) {
									val hourlyList : ArrayList<WeatherDto> = ArrayList()
									val jhArray = hnHour.getJSONArray("njh")
									for (i in 0 until jhArray.length()) {
										val itemObj = jhArray.getJSONObject(i)
										val dto = WeatherDto()
										dto.hourlyCode = itemObj.getString("ja").toInt()
										dto.hourlyTemp = itemObj.getString("jb").toFloat()
										dto.hourlyTime = itemObj.getString("jf")
										dto.hourlyWindDirCode = itemObj.getString("jc").toInt()
										dto.hourlyWindForceCode = itemObj.getString("jd").toInt()
										hourlyList.add(dto)
									}
									//逐小时预报信息
									val cubicView = CubicView(this@ForecastActivity)
									cubicView.setData(hourlyList)
									llContainer1.removeAllViews()
									llContainer1.addView(cubicView, CommonUtil.widthPixels(this@ForecastActivity)*2, CommonUtil.dip2px(this@ForecastActivity, 300.0f).toInt())
								}
							}
						}
						cancelDialog()
						scrollView.visibility = View.VISIBLE
					}
				}
			})
		}).start()
	}

	/**
	 * 切换海南实况数据
	 */
	private fun switchHNFactData(flag : Boolean) {
		if (flag) {//基本站数据
			if (!TextUtils.isEmpty(l7)) {
				tvTime.text = l7 + "发布"
			}

			if (!TextUtils.isEmpty(l5)) {
				val currentTime = sdf1.format(Date().time)
				val hour = currentTime.toInt()
				val drawable : Drawable?
				if (hour in 6..17) {
					drawable = ContextCompat.getDrawable(this@ForecastActivity, R.drawable.phenomenon_drawable)
				}else {
					drawable = ContextCompat.getDrawable(this@ForecastActivity, R.drawable.phenomenon_drawable_night)
				}
				drawable!!.level = l5!!.toInt()
				ivPhe.background = drawable
				tvPhe.text = getString(WeatherUtil.getWeatherId(l5!!.toInt()))
			}

			if (!TextUtils.isEmpty(l1)) {
				tvTemperature.text = l1+"℃"
			}

			if (!TextUtils.isEmpty(l4) && !TextUtils.isEmpty(l3)) {
					tvWind.text = getString(WeatherUtil.getWindDirection(l4!!.toInt())) + " " +
							WeatherUtil.getFactWindForce(l3!!.toInt())
			}

			if (!TextUtils.isEmpty(l2)) {
				tvHumidity.text = "湿度" + " "+ l2 + "%"
			}

			if (!TextUtils.isEmpty(l10)) {
				tvPressure.text = "气压"+" "+l10 + getString(R.string.unit_hPa)
			}
		}else {//最近站数据
			if (!TextUtils.isEmpty(nl7)) {
				tvTime.text = nl7 + "发布"
			}

			if (!TextUtils.isEmpty(nl5)) {
				val currentTime = sdf1.format(Date().time)
				val hour = currentTime.toInt()
				val drawable : Drawable?
				if (hour in 6..17) {
					drawable = ContextCompat.getDrawable(this@ForecastActivity, R.drawable.phenomenon_drawable)
				}else {
					drawable = ContextCompat.getDrawable(this@ForecastActivity, R.drawable.phenomenon_drawable_night)
				}
				drawable!!.level = nl5!!.toInt()
				ivPhe.background = drawable
				tvPhe.text = getString(WeatherUtil.getWeatherId(nl5!!.toInt()))
			}

			if (!TextUtils.isEmpty(nl1)) {
				tvTemperature.text = nl1+"℃"
			}

			if (!TextUtils.isEmpty(nl4) && !TextUtils.isEmpty(nl3)) {
				tvWind.text = getString(WeatherUtil.getWindDirection(nl4!!.toInt())) + " " +
						WeatherUtil.getFactWindForce(nl3!!.toInt())
			}

			if (!TextUtils.isEmpty(nl2)) {
				tvHumidity.text = "湿度" + " "+ nl2 + "%"
			}

			if (!TextUtils.isEmpty(nl10)) {
				tvPressure.text = "气压"+" "+nl10 + getString(R.string.unit_hPa)
			}
		}
	}

	override fun onClick(view: View?) {
		when(view!!.id) {
			R.id.llBack -> finish()
			R.id.ivList -> {
				if (hScrollView2.visibility == View.VISIBLE) {
					ivList.setImageResource(R.drawable.iv_list)
					listView.visibility = View.VISIBLE
					hScrollView2.visibility = View.GONE
				}else {
					ivList.setImageResource(R.drawable.iv_trend)
					listView.visibility = View.GONE
					hScrollView2.visibility = View.VISIBLE
				}
			}
			R.id.tvFact1 -> {
				tvFact1.setBackgroundResource(R.drawable.btn_lb_corner_selected);
				tvFact2.setBackgroundResource(R.drawable.btn_rb_corner_unselected);
				switchHNFactData(true)
			}
			R.id.tvFact2 -> {
				tvFact1.setBackgroundResource(R.drawable.btn_lb_corner_unselected);
				tvFact2.setBackgroundResource(R.drawable.btn_rb_corner_selected);
				switchHNFactData(false)
			}
		}
	}

}
