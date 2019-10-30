package com.cxwl.shawn.wuzhishan.decision.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.*
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData
import com.cxwl.shawn.wuzhishan.decision.dto.WarningDto
import com.cxwl.shawn.wuzhishan.decision.fragment.PdfListFragment
import com.cxwl.shawn.wuzhishan.decision.fragment.WarningFragment
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import kotlinx.android.synthetic.main.activity_warning.*
import kotlinx.android.synthetic.main.layout_title.*
import kotlinx.android.synthetic.main.marker_warning_icon.*
import kotlinx.android.synthetic.main.marker_warning_icon.view.*
import kotlinx.android.synthetic.main.marker_warning_info.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * 预警信息
 * @author shawn_sun
 *
 */
class WarningActivity : FragmentActivity(), OnClickListener, OnMapClickListener,
        OnMarkerClickListener, InfoWindowAdapter {
	
	private var isExpand : Boolean = false
	private var aMap : AMap? = null
	private val dataList : ArrayList<WarningDto> = ArrayList()
	private var selectMarker : Marker? = null
	private val fragments : ArrayList<Fragment> = ArrayList()
	private var data : ColumnData? = null
	private val markers : ArrayList<Marker> = ArrayList()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_warning)
		initAmap(savedInstanceState)
		initWidget()
	}

	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		llBack.setOnClickListener(this)
		ivExpand.setOnClickListener(this)

		val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
		if (!TextUtils.isEmpty(title)) {
			tvTitle.text = title
		}

		data = intent.getParcelableExtra("data")
		if (data != null) {
			val dto = data!!.child[0]
			if (TextUtils.equals(dto.showType, CONST.WARNING)) {
				okHttpWarning(dto.dataUrl)
			}
			initViewPager(data)
		}
    }

	/**
	 * 初始化高德地图
	 */
	private fun initAmap(bundle : Bundle?) {
		mapView.onCreate(bundle)
		if (aMap == null) {
			aMap = mapView.map
		}
		
		val centerLatLng = LatLng(CONST.DEFAULT_LAT, CONST.DEFAULT_LNG)
		aMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 8.0f))
		aMap!!.uiSettings.isMyLocationButtonEnabled = false// 设置默认定位按钮是否显示
		aMap!!.uiSettings.isZoomControlsEnabled = false
		aMap!!.uiSettings.isRotateGesturesEnabled = false
		aMap!!.setOnMapClickListener(this)
		aMap!!.setOnMarkerClickListener(this)
		aMap!!.setInfoWindowAdapter(this)

		tvMapNumber.text = aMap!!.mapContentApprovalNumber
	}

	/**
	 * 获取预警信息
	 */
	private fun okHttpWarning(url : String) {
		if (TextUtils.isEmpty(url)) {
			return
		}
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					if (!TextUtils.isEmpty(result)) {
						val obj = JSONObject(result)
						if (!obj.isNull("w")) {
							val jsonArray = obj.getJSONArray("w")
							dataList.clear()
							for (i in 0 until jsonArray.length()) {
								val dto = WarningDto()
								val itemObj = jsonArray.getJSONObject(i)
								val w1 = itemObj.getString("w1")
								dto.w2 = itemObj.getString("w2")
								val w4 = itemObj.getString("w4")
								val w5 = itemObj.getString("w5")
								val w6 = itemObj.getString("w6")
								val w7 = itemObj.getString("w7")
								val w8 = itemObj.getString("w8")
								val w9 = itemObj.getString("w9")
								dto.w11 = itemObj.getString("w11")

								dto.name = w1+dto.w2+"发布"+w5+w7+"预警"
								dto.time = w8
								dto.type = "icon_warning_$w4"
								dto.color = w6
								dto.content = w9

								val names = resources.getStringArray(R.array.district_name);
								for (j in names.indices) {
									val itemArray = names[j].split(",")
									val value = if (!TextUtils.isEmpty(dto.w2)) {
										dto.w2
									}else {
										dto.w11
									}
									if (value.contains(itemArray[0]) || itemArray[0].contains(value)) {
										if (!TextUtils.isEmpty(itemArray[2]) && !TextUtils.isEmpty(itemArray[1])) {
											dto.lat = itemArray[2]
											dto.lng = itemArray[1]
											break
										}
									}
								}
								dataList.add(dto)
							}
							addMarkerAndDrawDistrict()
						}
					}
				}
			})
		}).start()
	}

	private fun removeMarkers() {
		for (i in markers.indices) {
			val marker = markers[i]
			marker.remove()
		}
		markers.clear()
	}

	@SuppressLint("InflateParams")
	private fun addMarkerAndDrawDistrict() {
		removeMarkers()
		val inflater : LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
		for (i in dataList.indices) {
			val dto = dataList[i]
			if (TextUtils.isEmpty(dto.lat) || TextUtils.isEmpty(dto.lng)) {
				return
			}
			val options = MarkerOptions()
			if (!TextUtils.isEmpty(dto.w2)) {
				options.title(dto.w2)
			}else {
				options.title(dto.w11)
			}
			options.position(LatLng(dto.lat.toDouble(), dto.lng.toDouble()))

			val markerView = inflater.inflate(R.layout.marker_warning_icon, null)

			var bitmap = CommonUtil.getImageFromAssetsFile(this,"warning/"+dto.type+dto.color+CONST.imageSuffix);
			if (bitmap == null) {
				bitmap = CommonUtil.getImageFromAssetsFile(this,"warning/"+"default"+dto.color+CONST.imageSuffix);
			}
			markerView.ivMarker.setImageBitmap(bitmap)
			val params = markerView.ivMarker.layoutParams
			val title = options.title
			if ("琼州海峡".contains(title) || "本岛西部".contains(title) || "本岛南部".contains(title)
					|| "本岛东部".contains(title) || "北部湾北部".contains(title) || "北部湾南部".contains(title)
					|| "中沙附近".contains(title) || "西沙附近".contains(title) || "南沙附近".contains(title)) {
				params.width = CommonUtil.dip2px(this, 30.0f).toInt()
				params.height = CommonUtil.dip2px(this, 30.0f).toInt()
			}else {
				params.width = CommonUtil.dip2px(this, 20.0f).toInt()
				params.height = CommonUtil.dip2px(this, 30.0f).toInt()
			}
			markerView.ivMarker.layoutParams = params
			options.icon(BitmapDescriptorFactory.fromView(markerView))
			val marker = aMap!!.addMarker(options)
			markers.add(marker)
		}

		dataList.sortWith(Comparator { a, b -> a.color.compareTo(b.color) })

		val map : HashMap<String, WarningDto> = HashMap()
		var color = 0
		for (i in dataList.indices) {
			val data = dataList[i]
			var name = data.w2
			if (TextUtils.isEmpty(data.w2)) {
				name = data.w11
			}
			if (map.containsKey(name)) {
				val c = data.color
				if (!TextUtils.isEmpty(c)) {
					if (color <= c.toInt()) {
						color = c.toInt()
						map[name] = data
					}
				}
			}else {
				map[name] = data
				color = 0
			}
		}

		for ((key, value) in map) {
			val c = value.color
			if (!TextUtils.isEmpty(c)) {
				var color2 = 0
				when {
					TextUtils.equals(c, "01") -> color2 = ContextCompat.getColor(this, R.color.blue)
					TextUtils.equals(c, "02") -> color2 = ContextCompat.getColor(this, R.color.yellow)
					TextUtils.equals(c, "03") -> color2 = ContextCompat.getColor(this, R.color.orange)
					TextUtils.equals(c, "04") -> color2 = ContextCompat.getColor(this, R.color.red)
				}
				var districtName = value.w2
				if (!TextUtils.isEmpty(districtName)) {
					when {
						districtName.contains("陵水") -> districtName = "陵水黎族自治县"
						districtName.contains("昌江") -> districtName = "昌江黎族自治县"
						districtName.contains("白沙") -> districtName = "白沙黎族自治县"
						districtName.contains("琼中") -> districtName = "琼中黎族苗族自治县"
						districtName.contains("乐东") -> districtName = "乐东黎族自治县"
						districtName.contains("保亭") -> districtName = "保亭黎族苗族自治县"
					}
					CommonUtil.drawWarningDistrict(applicationContext, aMap, districtName, color2)
				}
			}
		}
	}

	override fun onMapClick(p0: LatLng?) {
		if (selectMarker != null) {
			selectMarker!!.hideInfoWindow()
		}
	}

	override fun onMarkerClick(marker: Marker?): Boolean {
		selectMarker = marker
		marker!!.showInfoWindow()
		return true
	}

	@SuppressLint("InflateParams")
	override fun getInfoContents(marker: Marker?): View {
		val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
		val view = inflater.inflate(R.layout.marker_warning_info, null)
		view.tvName.text = marker!!.title

		val tempList : ArrayList<WarningDto> = ArrayList()
		for (i in dataList.indices) {
			val dto = dataList[i]
			var title = dto.w2
			if (TextUtils.isEmpty(title)) {
				title = dto.w11
			}
			if (title.contains(marker.title) || marker.title.contains(title)) {
				tempList.add(dto)
			}
		}

		llContainer.removeAllViews()
		for (i in tempList.indices) {
			val data = tempList[i]
			var bitmap = CommonUtil.getImageFromAssetsFile(this,"warning/"+data.type+data.color+CONST.imageSuffix)
			if (bitmap == null) {
				bitmap = CommonUtil.getImageFromAssetsFile(this,"warning/"+"default"+data.color+CONST.imageSuffix)
			}
			val imageView = ImageView(this)
			imageView.setImageBitmap(bitmap)
			val params = LinearLayout.LayoutParams(CommonUtil.dip2px(this, 30.0f).toInt(), CommonUtil.dip2px(this, 30.0f).toInt())
			params.setMargins(0, 0, 15, 0);
			imageView.layoutParams = params
			imageView.tag = i.toString()
			llContainer.addView(imageView)

			imageView.setOnClickListener {
				val intent = Intent(this, WarningDetailActivity::class.java)
				val bundle = Bundle()
				bundle.putParcelable("data", data)
				intent.putExtras(bundle)
				startActivity(intent)
			}
		}

		return view
	}

	override fun getInfoWindow(marker: Marker?): View {
		return null!!
	}

	/**
	 * 初始化viewPager
	 */
	private fun initViewPager(data : ColumnData?) {
		val columnList : ArrayList<ColumnData> = ArrayList(data!!.child)
		val columnSize = columnList.size
		if (columnSize <= 1) {
			llContainer.visibility = View.GONE
			llContainer1.visibility = View.GONE
		}
		llContainer.removeAllViews()
		llContainer1.removeAllViews()
		val width = CommonUtil.widthPixels(this)
		for (i in 0 until columnSize) {
			val dto = columnList[i]

			val tvName = TextView(this)
			tvName.gravity = Gravity.CENTER
			tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f)
			tvName.setPadding(0, CommonUtil.dip2px(this, 10.0f).toInt(), 0, CommonUtil.dip2px(this, 10.0f).toInt())
			tvName.setOnClickListener {
				if (viewPager != null) {
					viewPager.setCurrentItem(i, true)
				}
			}
			if (i == 0) {
				tvName.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
			}else {
				tvName.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
			}
			if (!TextUtils.isEmpty(dto.name)) {
				tvName.text = dto.name
			}
			val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
			params.weight = 1.0f
			when (columnSize) {
				1 -> params.width = width
				2 -> params.width = width/2
				3 -> params.width = width/3
				else -> params.width = width/4
			}
			tvName.layoutParams = params
			llContainer.addView(tvName, i)

			val tvBar = TextView(this)
			tvBar.gravity = Gravity.CENTER
			tvBar.setOnClickListener {
				if (viewPager != null) {
					viewPager.setCurrentItem(i, true)
				}
			}
			if (i == 0) {
				tvBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
			}else {
				tvBar.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
			}
			val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
			params1.weight = 1.0f
			when (columnSize) {
				1 -> params1.width = width
				2 -> params1.width = width/2-CommonUtil.dip2px(this, 20.0f).toInt()
				3 -> params1.width = width/3-CommonUtil.dip2px(this, 20.0f).toInt()
				else -> params1.width = width/4-CommonUtil.dip2px(this, 20.0f).toInt()
			}
			params1.height = CommonUtil.dip2px(this, 2.0f).toInt()
			params1.setMargins(CommonUtil.dip2px(this, 10.0f).toInt(), 0, CommonUtil.dip2px(this, 10.0f).toInt(), 0)
			tvBar.layoutParams = params1
			llContainer1.addView(tvBar, i)

			var fragment : Fragment? = null
			if (TextUtils.equals(dto.showType, CONST.DOCUMENT)) {
				fragment = PdfListFragment()
			}else if (TextUtils.equals(dto.showType, CONST.WARNING)) {
				fragment = WarningFragment()
			}
			val bundle = Bundle()
			bundle.putString(CONST.WEB_URL, dto.dataUrl)
			fragment!!.arguments = bundle
			fragments.add(fragment)
		}

		viewPager.setSlipping(true)
		viewPager.offscreenPageLimit = fragments.size
		viewPager.adapter = MyPagerAdapter(supportFragmentManager, fragments)
		viewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
			override fun onPageScrollStateChanged(p0: Int) {
			}
			override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
			}
			override fun onPageSelected(arg0: Int) {
				if (llContainer != null) {
					for (i in 0 until llContainer.childCount) {
						val tvName = llContainer.getChildAt(i) as TextView
						if (i == arg0) {
							tvName.setTextColor(ContextCompat.getColor(this@WarningActivity, R.color.colorPrimary))
						} else {
							tvName.setTextColor(ContextCompat.getColor(this@WarningActivity, R.color.text_color3))
						}
					}
					if (llContainer.childCount > 4) {
						hScrollView1.smoothScrollTo(width / 4 * arg0, 0)
					}
				}

				if (llContainer1 != null) {
					for (i in 0 until llContainer1.childCount) {
						val tvBar = llContainer1.getChildAt(i) as TextView
						if (i == arg0) {
							tvBar.setBackgroundColor(ContextCompat.getColor(this@WarningActivity, R.color.colorPrimary))
						} else {
							tvBar.setBackgroundColor(ContextCompat.getColor(this@WarningActivity, R.color.transparent))
						}
					}
				}

				val dto = data.child[arg0]
				if (TextUtils.equals(dto.showType, CONST.WARNING)) {
					okHttpWarning(dto.dataUrl)
				}
			}
		})
	}

	/**
	 * @ClassName: MyPagerAdapter
	 * @Description: TODO填充ViewPager的数据适配器
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:37:47
	 *
	 */
	class MyPagerAdapter(fm: FragmentManager, fs : ArrayList<Fragment>) : FragmentStatePagerAdapter(fm) {

		private val fragments : ArrayList<Fragment> = fs

		init {
			notifyDataSetChanged()
		}

		override fun getCount(): Int {
			return fragments.size
		}

		override fun getItem(arg0: Int): Fragment {
			return fragments[arg0]
		}

		override fun getItemPosition(`object`: Any): Int {
			return PagerAdapter.POSITION_NONE
		}
	}
	
	override fun onClick(view: View?) {
		when(view!!.id) {
			R.id.llBack -> finish()
			R.id.ivExpand -> {
				val params = mapView.layoutParams
				if (!isExpand) {
					ivExpand.setImageResource(R.drawable.iv_collose)
					params.width = LayoutParams.MATCH_PARENT
					params.height = LayoutParams.MATCH_PARENT
					isExpand = true
				}else {
					ivExpand.setImageResource(R.drawable.iv_expand)
					params.width = LayoutParams.MATCH_PARENT
					params.height = CommonUtil.dip2px(this, 300.0f).toInt()
					isExpand = false
				}
				mapView.layoutParams = params
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
