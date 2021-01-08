package com.cxwl.shawn.wuzhishan.decision.fragment;

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.adapter.WarningAdapter
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.dto.WarningDto
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_warning.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.LinkedHashMap

class WarningFragment : Fragment() {

	private var mAdapter : WarningAdapter? = null
	private val dataList : ArrayList<WarningDto> = ArrayList()
	private val cityNames : ArrayList<String> = ArrayList()
	private val mUIHandler : Handler = Handler()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_warning, null)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initListView()
		refresh()
	}
	
	private fun refresh() {
		val url : String? = arguments!!.getString(CONST.WEB_URL)
		if (!TextUtils.isEmpty(url)) {
			okHttpWarning(url)
		}
	}

	private fun initListView() {
		mAdapter = WarningAdapter(activity!!, cityNames, dataList)
		listView.adapter = mAdapter
	}

	/**
	 * 获取预警信息
	 */
	private fun okHttpWarning(url : String?) {
		if (TextUtils.isEmpty(url)) {
			return
		}
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().url(url!!).build(), object : Callback {
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
							if (!obj.isNull("w")) {
								val jsonArray = obj.getJSONArray("w")
								val map : LinkedHashMap<String, String> = LinkedHashMap()
								val length = if (jsonArray.length() > 50) 50 else jsonArray.length()
								dataList.clear()
								for (i in 0 until length) {
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

									if (!TextUtils.isEmpty(dto.w11)) {
										dto.w2 = dto.w2+dto.w11
									}

									dto.name = w1+dto.w2+"发布$w5$w7"+"预警"
									dto.time = w8
									dto.type = "icon_warning_$w4"
									dto.color = w6
									dto.content = w9

									dataList.add(dto)

									if (!TextUtils.isEmpty(dto.w2)) {
										map[dto.w2] = dto.w2
									}
								}

								cityNames.clear()
								for ((key) in map) {
									cityNames.add(key)
								}

								if (mAdapter != null) {
									mAdapter!!.notifyDataSetChanged()
								}
							}
						}

						if (cityNames.size == 0) {
							tvPrompt.visibility = View.VISIBLE
						}else {
							tvPrompt.visibility = View.GONE
						}
					}
				}
			})
		}).start()
	}
	
}
