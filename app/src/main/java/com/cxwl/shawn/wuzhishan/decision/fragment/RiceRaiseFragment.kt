package com.cxwl.shawn.wuzhishan.decision.fragment;

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.adapter.DisasterMonitorAdapter
import com.cxwl.shawn.wuzhishan.decision.adapter.RiceRaiseAdapter
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.dto.RiceRaiseDto
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import kotlinx.android.synthetic.main.dialog_select_date.*
import kotlinx.android.synthetic.main.dialog_select_date.view.*
import kotlinx.android.synthetic.main.dialog_select_string.view.*
import kotlinx.android.synthetic.main.fragment_rice_raise.*
import kotlinx.android.synthetic.main.fragment_rice_raise.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import wheelview.NumericWheelAdapter
import wheelview.OnWheelScrollListener
import wheelview.WheelView
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.fragment_rice_raise.view.listView as listView1

/**
 * 生态气象-水稻长势
 * @author shawn_sun
 */
class RiceRaiseFragment : Fragment(), OnClickListener {
	
	private var sdf2 : SimpleDateFormat = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
	private var startDate : String? = null
	private var endDate : String? = null
	private var stationName : String? = null
	private var stationNames : ArrayList<String> = ArrayList()
	private var mAdapter : RiceRaiseAdapter? = null
	private var dataList : ArrayList<RiceRaiseDto> = ArrayList()
	private val mHandler : Handler = Handler()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_rice_raise, null)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
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
		refreshLayout.isEnabled = false
	}

	private fun refresh() {
		dataList.clear()
		okHttpList()
	}

	private fun initWidget() {
		llStartDate.setOnClickListener(this)
		llEndDate.setOnClickListener(this)
		llStationName.setOnClickListener(this)
		tvCheck.setOnClickListener(this)

		val calendar : Calendar = Calendar.getInstance()
		calendar.add(Calendar.DAY_OF_MONTH, -30)
		startDate = sdf2.format(calendar.time)
		tvStartDate.text = startDate!!.substring(0, 4)+"-"+startDate!!.substring(4, 6)+"-"+startDate!!.substring(6, 8)
		endDate = sdf2.format(Date())
		tvEndDate.text = endDate!!.substring(0, 4)+"-"+endDate!!.substring(4, 6)+"-"+endDate!!.substring(6, 8)

		okHttpStations()
	}

	private fun initListView() {
		mAdapter = RiceRaiseAdapter(activity!!, dataList)
		listView.adapter = mAdapter
	}

	/**
	 * 获取站点信息
	 */
	private fun okHttpStations() {
		Thread(Runnable {
			val url = "http://59.50.130.88:8888/decision-admin/ny/getzd"
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					mHandler.post {
						if (!TextUtils.isEmpty(result)) {
							val array = JSONArray(result)
							stationNames.clear()
							for (i in 0 until array.length()) {
								val itemObj = array.getJSONObject(i)
								if (!itemObj.isNull("C_CorpDev")) {
									stationNames.add(itemObj.getString("C_CorpDev"))
								}
							}
							if (stationNames.size > 0) {
								stationName = stationNames[0]
								tvStationName.text = stationName
								refresh()
							}
						}
					}
				}
			})
		}).start()
	}

	private fun selectStationDialog() {
		val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
		val dialogView = inflater.inflate(R.layout.dialog_select_string, null)
		dialogView.tvContent.text = "选择站点"

		val dialog = Dialog(activity!!, R.style.CustomProgressDialog)
		dialog.setContentView(dialogView)
		dialog.show()

		val mAdapter = DisasterMonitorAdapter(activity, stationNames)
		dialogView.listView.adapter = mAdapter
		dialogView.listView.setOnItemClickListener { adapterView, view, i, l ->
			dialog.dismiss()
			stationName = stationNames[i]
			tvStationName.text = stationName
		}
	}

	private fun selectDateDialog(isStartDate : Boolean) {
		val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
		val dialogView = inflater.inflate(R.layout.dialog_select_date, null)
		dialogView.tvMessage.text = if (isStartDate) "选择开始时间" else "选择结束时间"

		initWheelView(dialogView)

		val dialog = Dialog(activity!!, R.style.CustomProgressDialog)
		dialog.setContentView(dialogView)
		dialog.show()

		dialogView.llPositive.setOnClickListener {
			dialog.dismiss()
			setTextViewValue(dialogView, isStartDate)
		}
		dialogView.llNegative.setOnClickListener {
			dialog.dismiss()
		}
	}

	private fun initWheelView(dialogView : View) {
		val c = Calendar.getInstance()
		val curYear = c.get(Calendar.YEAR)
		val curMonth = c.get(Calendar.MONTH) + 1//通过Calendar算出的月数要+1
		val curDate = c.get(Calendar.DATE)
		val curHour = c.get(Calendar.HOUR_OF_DAY)
		val curMinute = c.get(Calendar.MINUTE)

		val year = dialogView.findViewById<WheelView>(R.id.year)
		val numericWheelAdapter1 = NumericWheelAdapter(activity,1950, curYear)
		numericWheelAdapter1.setLabel(getString(R.string.year))
		year.viewAdapter = numericWheelAdapter1
		year.isCyclic = false//是否可循环滑动
		year.addScrollingListener(object : OnWheelScrollListener {
			override fun onScrollingStarted(wheel: WheelView?) {
			}
			override fun onScrollingFinished(wheel: WheelView?) {
				val nYear = year.currentItem + 1950//年
				val nMonth = month.currentItem + 1//月
				initDay(dialogView,nYear,nMonth)
			}
		})

		val month = dialogView.findViewById<WheelView>(R.id.month)
		val numericWheelAdapter2 = NumericWheelAdapter(activity,1, 12, "%02d")
		numericWheelAdapter2.setLabel(getString(R.string.month))
		month.viewAdapter = numericWheelAdapter2
		month.isCyclic = false
		year.addScrollingListener(object : OnWheelScrollListener {
			override fun onScrollingStarted(wheel: WheelView?) {
			}
			override fun onScrollingFinished(wheel: WheelView?) {
				val nYear = year.currentItem + 1950//年
				val nMonth = month.currentItem + 1//月
				initDay(dialogView,nYear,nMonth)
			}
		})

		val day = dialogView.findViewById<WheelView>(R.id.day)
		initDay(dialogView,curYear,curMonth)

		val hour = dialogView.findViewById<WheelView>(R.id.hour)
		val numericWheelAdapter3 = NumericWheelAdapter(activity,1, 23, "%02d")
		numericWheelAdapter3.setLabel(getString(R.string.hour))
		hour.viewAdapter = numericWheelAdapter3
		hour.isCyclic = false
		hour.visibility = View.GONE
		hour.addScrollingListener(object : OnWheelScrollListener {
			override fun onScrollingStarted(wheel: WheelView?) {
			}
			override fun onScrollingFinished(wheel: WheelView?) {
				val nYear = year.currentItem + 1950//年
				val nMonth = month.currentItem + 1//月
				initDay(dialogView,nYear,nMonth)
			}
		})

		val minute = dialogView.findViewById<WheelView>(R.id.minute)
		val numericWheelAdapter4 = NumericWheelAdapter(activity,1, 59, "%02d")
		numericWheelAdapter4.setLabel(getString(R.string.minute))
		minute.viewAdapter = numericWheelAdapter4
		minute.isCyclic = false
		minute.visibility = View.GONE
		minute.addScrollingListener(object : OnWheelScrollListener {
			override fun onScrollingStarted(wheel: WheelView?) {
			}
			override fun onScrollingFinished(wheel: WheelView?) {
				val nYear = year.currentItem + 1950//年
				val nMonth = month.currentItem + 1//月
				initDay(dialogView,nYear,nMonth)
			}
		})

		year.visibleItems = 7
		month.visibleItems = 7
		day.visibleItems = 7
		hour.visibleItems = 7
		minute.visibleItems = 7

		year.currentItem = curYear - 1950
		month.currentItem = curMonth - 1
		day.currentItem = curDate - 1
		hour.currentItem = curHour - 1
		minute.currentItem = curMinute
	}

	private fun initDay(dialogView : View, year : Int, month : Int) {
		val numericWheelAdapter = NumericWheelAdapter(activity,1, getDay(year, month), "%02d")
		numericWheelAdapter.setLabel(getString(R.string.day))
		dialogView.day.viewAdapter = numericWheelAdapter
		dialogView.day.isCyclic = false
	}

	/**
	 * @param year
	 * @param month
	 * @return
	 */
	private fun getDay(year : Int, month : Int): Int {
		val flag : Boolean = when(year % 4) {
			0 -> true
			else -> false
		}

		return when(month) {
			1,3,5,7,8,10,12 -> 31
			2 -> if (flag) 29 else 28
			else -> 30
		}
	}

	private fun setTextViewValue(dialogView : View, isStartDate : Boolean) {
		val yearStr = (dialogView.year.currentItem+1950).toString()
		val monthStr = if ((dialogView.month.currentItem+1)<10) "0"+(dialogView.month.currentItem+1).toString() else (dialogView.month.currentItem+1).toString()
		val dayStr = if ((dialogView.day.currentItem+1)<10) "0"+(dialogView.day.currentItem+1).toString() else (dialogView.day.currentItem+1).toString()
//		String hourStr = String.valueOf(((hour.getCurrentItem()+1) < 10) ? "0" + (hour.getCurrentItem()+1) : (hour.getCurrentItem()+1));
//		String minuteStr = String.valueOf(((minute.getCurrentItem()+1) < 10) ? "0" + (minute.getCurrentItem()+1) : (minute.getCurrentItem()+1));

		if (isStartDate) {
			startDate = yearStr+monthStr+dayStr
			tvStartDate.text = yearStr+"-"+monthStr+"-"+dayStr
		}else {
			endDate = yearStr+monthStr+dayStr
			tvEndDate.text = yearStr+"-"+monthStr+"-"+dayStr
		}
	}

	private fun okHttpList() {
		val start = sdf2.parse(startDate).time
		val end = sdf2.parse(endDate).time
		if (start >= end) {
			Toast.makeText(activity, "结束时间不能小于开始时间", Toast.LENGTH_SHORT).show()
			return
		}
		if (TextUtils.isEmpty(stationName)) {
			Toast.makeText(activity, "请选择站点", Toast.LENGTH_SHORT).show()
			return
		}

		if (!refreshLayout.isRefreshing) {
			refreshLayout.isRefreshing = true
		}
		Thread(Runnable {
			val url = String.format("http://59.50.130.88:8888/decision-admin/ny/sdzs?ST=%s&ET=%s&DC=%s", startDate, endDate, stationName)
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					mHandler.post {
						refreshLayout.isRefreshing = false
						if (!TextUtils.isEmpty(result)) {
							val array = JSONArray(result)
							for (i in 0 until array.length()) {
								val itemObj = array.getJSONObject(i)
								val dto = RiceRaiseDto()
								dto.C_Stat_Name = itemObj.getString("C_Stat_Name")
								dto.C_Crop = itemObj.getString("C_Crop")
								dto.C_CropName = itemObj.getString("C_CropName")
								dto.C_CropVirteties = itemObj.getString("C_CropVirteties")
								dto.C_CropMature = itemObj.getString("C_CropMature")
								dto.C_CorpDev = itemObj.getString("C_CorpDev")
								dataList.add(dto)
							}
							if (mAdapter != null) {
								mAdapter!!.notifyDataSetChanged()
							}
						}
					}
				}
			})
		}).start()
	}

	override fun onClick(v: View?) {
		when(v!!.id) {
			R.id.llStartDate -> selectDateDialog(true)
			R.id.llEndDate -> selectDateDialog(false)
			R.id.llStationName -> selectStationDialog()
			R.id.tvCheck -> refresh()
		}
	}

}
