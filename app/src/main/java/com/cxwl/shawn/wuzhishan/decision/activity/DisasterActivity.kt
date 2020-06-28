package com.cxwl.shawn.wuzhishan.decision.activity;

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.adapter.DisasterAdapter
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.dto.DisasterDto
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import kotlinx.android.synthetic.main.activity_disaster.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * 灾情列表
 */
class DisasterActivity : BaseActivity(), OnClickListener {

	private var columnId = "679"//农事记载
	private var mAdapter: DisasterAdapter? = null
	private val dataList: MutableList<DisasterDto> = ArrayList()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_disaster)
		initRefreshLayout()
		initWidget()
		initListView()
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private fun initRefreshLayout() {
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4)
		refreshLayout.setProgressViewEndTarget(true, 400)
		refreshLayout.isRefreshing = true
		refreshLayout.setOnRefreshListener { refresh() }
	}

	private fun refresh() {
		dataList.clear()
		okHttpList()
	}

	private fun initWidget() {
		llBack.setOnClickListener(this)
		tvSearch.setOnClickListener(this)
		val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
		if (!TextUtils.isEmpty(title)) {
			tvTitle.text = title
		}
		columnId = intent.getStringExtra(CONST.COLUMN_ID)
		refresh()
	}

	private fun initListView() {
		mAdapter = DisasterAdapter(this, dataList, columnId)
		listView.adapter = mAdapter
//		listView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
//			val data = dataList[position]
//			val intent = Intent(this@DisasterActivity, DisasterDetailActivity::class.java)
//			val bundle = Bundle()
//			bundle.putParcelable("data", data)
//			intent.putExtras(bundle)
//			startActivityForResult(intent, 1001)
//		}
	}

	private fun okHttpList() {
		refreshLayout!!.visibility = View.VISIBLE
		var url = "http://59.50.130.88:8888/decision-admin/ny/getnsjz?ntname=&wz="
		if (TextUtils.equals(columnId, "679")) {//农事记载
			url = String.format("http://59.50.130.88:8888/decision-admin/ny/getnsjz?ntname=%s&wz=%s", etName.text, etAddr.text)
		} else if (TextUtils.equals(columnId, "680")) {//灾情上报
			url = String.format("http://59.50.130.88:8888/decision-admin/ny/getzqsb?ntname=%s&wz=%s", etName.text, etAddr.text)
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
					runOnUiThread {
						refreshLayout!!.isRefreshing = false
						if (!TextUtils.isEmpty(result)) {
							try {
								val array = JSONArray(result)
								for (i in 0 until array.length()) {
									val itemObj = array.getJSONObject(i)
									val dto = DisasterDto()
									if (!itemObj.isNull("id")) {
										dto.id = itemObj.getString("id")
									}
									if (!itemObj.isNull("wz")) {
										dto.ntAddr = itemObj.getString("wz")
									}
									if (!itemObj.isNull("ntname")) {
										dto.ntName = itemObj.getString("ntname")
									}
									if (!itemObj.isNull("zwtype")) {
										dto.ntType = itemObj.getString("zwtype")
									}
									if (!itemObj.isNull("nshd")) {
										dto.ntEvent = itemObj.getString("nshd")
									}
									if (!itemObj.isNull("zwsyq")) {
										dto.ntBorn = itemObj.getString("zwsyq")
									}
									if (!itemObj.isNull("zhlx")) {
										dto.ntDisaster = itemObj.getString("zhlx")
									}
									if (!itemObj.isNull("jltime")) {
										dto.time = itemObj.getString("jltime")
									}
									if (!itemObj.isNull("tpurl")) {
										val imgArray = itemObj.getJSONArray("tpurl")
										for (j in 0 until imgArray.length()) {
											dto.imgList.add(imgArray.getString(j))
										}
									}
									dataList.add(dto)
								}
								if (mAdapter != null) {
									mAdapter!!.notifyDataSetChanged()
								}
							} catch (e: JSONException) {
								e.printStackTrace()
							}
						}
					}
				}
			})
		}).start()
	}

	override fun onClick(v: View) {
		when (v.id) {
			R.id.llBack -> finish()
			R.id.tvSearch -> {
				dataList.clear()
				okHttpList()
			}
		}
	}

}
