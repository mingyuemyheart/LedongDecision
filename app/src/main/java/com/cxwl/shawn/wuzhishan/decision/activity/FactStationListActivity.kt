package com.cxwl.shawn.wuzhishan.decision.activity;

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.adapter.FactStationListAdapter
import com.cxwl.shawn.wuzhishan.decision.dto.ShawnRainDto
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import kotlinx.android.synthetic.main.activity_fact_station_list.*
import kotlinx.android.synthetic.main.layout_title.*
import net.sourceforge.pinyin4j.PinyinHelper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * 实况资料-站点列表
 */
class FactStationListActivity : BaseActivity(), OnClickListener {
	
	private var mContext : Context? = null
	private var b1 = false
	private var b2 = false
	private var b3 = false//false为将序，true为升序
	private var mAdapter : FactStationListAdapter? = null
	private val realDatas : ArrayList<ShawnRainDto> = ArrayList()
	private var childId : String? = null
	private val mUIHandler : Handler = Handler()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_fact_station_list)
		mContext = this
		initWidget()
		initListView()
	}

	private fun initWidget() {
		llBack.setOnClickListener(this)
		tvTitle.text = "详情数据"
		ll1.setOnClickListener(this)
		ll2.setOnClickListener(this)
		ll3.setOnClickListener(this)

		childId = intent.extras.getString("childId")
		
		if (intent.hasExtra("realDatas")) {
			val title = intent.getStringExtra("title")
			val stationName = intent.getStringExtra("stationName")
			val area = intent.getStringExtra("area")
			val value = intent.getStringExtra("val")
			if (!TextUtils.isEmpty(title)) {
				tvPrompt.text = title
			}
			if (!TextUtils.isEmpty(stationName)) {
				tv1.text = stationName
			}
			if (!TextUtils.isEmpty(area)) {
				tv2.text = area
			}
			if (!TextUtils.isEmpty(value)) {
				tv3.text = value
			}

			val list : ArrayList<ShawnRainDto> = intent.extras.getParcelableArrayList("realDatas")
			realDatas.clear()
			if (TextUtils.equals(childId, "658")) {//最低温
				for (i in list.size-1 downTo 0) {
					realDatas.add(list[i])
				}
			}else {
				realDatas.addAll(list)
			}
		}else {
			val area = if (TextUtils.isEmpty(intent.getStringExtra("area"))) "" else intent.getStringExtra("area")
			val startTime = if (TextUtils.isEmpty(intent.getStringExtra("startTime"))) "" else intent.getStringExtra("startTime")
			val endTime = if (TextUtils.isEmpty(intent.getStringExtra("endTime"))) "" else intent.getStringExtra("endTime")
			val childId = if (TextUtils.isEmpty(intent.getStringExtra("childId"))) "" else intent.getStringExtra("childId")
			val url = "http://59.50.130.88:8888/decision-admin/dates/getcitid?city=$area&start=$startTime&end=$endTime&cid=$childId"
			okHttpList(url, childId)
		}

		if (TextUtils.equals(childId, "658")) {//最低温
			iv3.setImageResource(if (!b3) R.drawable.arrow_up else R.drawable.arrow_down)
		}else {
			iv3.setImageResource(if (!b3) R.drawable.arrow_down else R.drawable.arrow_up)
		}
		iv3.visibility = View.VISIBLE
	}
	
	private fun initListView() {
		mAdapter = FactStationListAdapter(mContext, realDatas)
		listView.adapter = mAdapter
		listView.onItemClickListener = OnItemClickListener { p0, p1, arg2, p3 ->
			val dto = realDatas[arg2]
			val intent = Intent(mContext, FactStationListDetailActivity::class.java)
			val bundle = Bundle()
			bundle.putParcelable("data", dto)
			bundle.putString("childId", childId)
			intent.putExtras(bundle)
			startActivity(intent)
		}
	}
	
	private fun okHttpList(url : String, childId : String) {
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
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

							if (!obj.isNull("th")) {
								val itemObj = obj.getJSONObject("th")
								if (!itemObj.isNull("stationName")) {
									val stationName = itemObj.getString("stationName")
									if (!TextUtils.isEmpty(stationName)) {
										tv1.text = stationName
									}
								}
								if (!itemObj.isNull("area")) {
									val area = itemObj.getString("area")
									if (!TextUtils.isEmpty(area)) {
										tv2.text = area
									}
								}
								if (!itemObj.isNull("val")) {
									val value = itemObj.getString("val")
									if (!TextUtils.isEmpty(value)) {
										tv3.text = value
									}
								}
							}

							if (!obj.isNull("title")) {
								val title = obj.getString("title")
								if (!TextUtils.isEmpty(title)) {
									tvPrompt.text = title
								}
							}

							if (!obj.isNull("list")) {
								realDatas.clear()
								val array = JSONArray(obj.getString("list"))
								if (TextUtils.equals(childId, "638")) {//最低温
									for (i in array.length()-1 downTo 0) {
										val itemObj = array.getJSONObject(i)
										val dto = ShawnRainDto()
										if (!itemObj.isNull("stationCode")) {
											dto.stationCode = itemObj.getString("stationCode")
										}
										if (!itemObj.isNull("stationName")) {
											dto.stationName = itemObj.getString("stationName")
										}
										if (!itemObj.isNull("area")) {
											dto.area = itemObj.getString("area")
										}
										if (!itemObj.isNull("val")) {
											dto.value = itemObj.getDouble("val")
										}

										if (!TextUtils.isEmpty(dto.stationName) && !TextUtils.isEmpty(dto.area)) {
											realDatas.add(dto)
										}
									}
								}else {
									for (i in 0 until array.length()) {
										val itemObj = array.getJSONObject(i)
										val dto = ShawnRainDto()
										if (!itemObj.isNull("stationCode")) {
											dto.stationCode = itemObj.getString("stationCode")
										}
										if (!itemObj.isNull("stationName")) {
											dto.stationName = itemObj.getString("stationName")
										}
										if (!itemObj.isNull("area")) {
											dto.area = itemObj.getString("area")
										}
										if (!itemObj.isNull("val")) {
											dto.value = itemObj.getDouble("val")
										}

										if (!TextUtils.isEmpty(dto.stationName) && !TextUtils.isEmpty(dto.area)) {
											realDatas.add(dto)
										}
									}
								}
								if (mAdapter != null) {
									mAdapter!!.notifyDataSetChanged()
								}
							}
						}
					}
				}
			})
		}).start()
	}

	 // 返回中文的首字母  
    private fun getPinYinHeadChar(str : String) : String {
		 var convert : String? = null
		 var size = str.length
		 if (size >= 2) {
			 size = 2
		 }
		 for (j in 0 until size) {
			 val word = str[j]
			 val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word)
			 if (pinyinArray != null) {
				 convert += pinyinArray[0][0]
			 } else {
				 convert += word
			 }
		 }
		 return convert!!
	}

	override fun onClick(v: View) {
		when (v.id) {
			R.id.llBack -> finish()
			R.id.ll1 -> {
				if (b1) {//升序
					b1 = false
					iv1.setImageResource(R.drawable.arrow_up)
					iv1.visibility = View.VISIBLE
					iv2.visibility = View.INVISIBLE
					iv3.visibility = View.INVISIBLE
					realDatas.sortWith(Comparator { arg0, arg1 ->
						if (TextUtils.isEmpty(arg0.stationName) || TextUtils.isEmpty(arg1.stationName)) {
							0
						} else {
							getPinYinHeadChar(arg0.stationName).compareTo(getPinYinHeadChar(arg1.stationName))
						}
					})
				} else {//将序
					b1 = true
					iv1.setImageResource(R.drawable.arrow_down)
					iv1.visibility = View.VISIBLE
					iv2.visibility = View.INVISIBLE
					iv3.visibility = View.INVISIBLE
					realDatas.sortWith(Comparator { arg0, arg1 ->
						if (TextUtils.isEmpty(arg0.stationName) || TextUtils.isEmpty(arg1.stationName)) {
							-1
						} else {
							getPinYinHeadChar(arg1.stationName).compareTo(getPinYinHeadChar(arg0.stationName))
						}
					})
				}
				if (mAdapter != null) {
					mAdapter!!.notifyDataSetChanged()
				}
			}
			R.id.ll2 -> {
				if (b2) {//升序
					b2 = false
					iv2.setImageResource(R.drawable.arrow_up)
					iv1.visibility = View.INVISIBLE
					iv2.visibility = View.VISIBLE
					iv3.visibility = View.INVISIBLE
					realDatas.sortWith(Comparator { arg0, arg1 ->
						if (TextUtils.isEmpty(arg0.area) || TextUtils.isEmpty(arg1.area)) {
							0
						} else {
							getPinYinHeadChar(arg0.area).compareTo(getPinYinHeadChar(arg1.area))
						}
					})
				} else {//将序
					b2 = true
					iv2.setImageResource(R.drawable.arrow_down)
					iv1.visibility = View.INVISIBLE
					iv2.visibility = View.VISIBLE
					iv3.visibility = View.INVISIBLE
					realDatas.sortWith(Comparator { arg0, arg1 ->
						if (TextUtils.isEmpty(arg0.area) || TextUtils.isEmpty(arg1.area)) {
							-1
						} else {
							getPinYinHeadChar(arg1.area).compareTo(getPinYinHeadChar(arg0.area))
						}
					})
				}
				if (mAdapter != null) {
					mAdapter!!.notifyDataSetChanged()
				}
			}
			R.id.ll3 -> {
				if (b3) {//升序
					b3 = false
					iv3.setImageResource(R.drawable.arrow_up)
					iv1.visibility = View.INVISIBLE
					iv2.visibility = View.INVISIBLE
					iv3.visibility = View.VISIBLE
					realDatas.sortWith(Comparator { arg0, arg1 -> java.lang.Double.valueOf(arg0.value).compareTo(java.lang.Double.valueOf(arg1.value)) })
				} else {//将序
					b3 = true
					iv3.setImageResource(R.drawable.arrow_down)
					iv1.visibility = View.INVISIBLE
					iv2.visibility = View.INVISIBLE
					iv3.visibility = View.VISIBLE
					realDatas.sortWith(Comparator { arg0, arg1 -> java.lang.Double.valueOf(arg1.value).compareTo(java.lang.Double.valueOf(arg0.value)) })
				}
				if (mAdapter != null) {
					mAdapter!!.notifyDataSetChanged()
				}
			}
		}
	}
	
}
