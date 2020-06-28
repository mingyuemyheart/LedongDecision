package com.cxwl.shawn.wuzhishan.decision.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.adapter.DisasterMonitorAdapter
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_select_date.view.*
import kotlinx.android.synthetic.main.dialog_select_string.view.*
import kotlinx.android.synthetic.main.fragment_disaster_monitor_day.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import wheelview.NumericWheelAdapter
import wheelview.OnWheelScrollListener
import wheelview.WheelView
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 灾害监测-逐日监测
 */
class DisasterMonitorDayFragment : Fragment(), OnClickListener {

    private val sdf2 = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private var startTimeCheck: String? = null
    private val dataMap: MutableMap<String, JSONObject> = LinkedHashMap()
    private val types: MutableList<String> = ArrayList() //灾害种类
    private val citys: MutableList<String> = ArrayList() //选择市县

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_disaster_monitor_day, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRefreshLayout()
        initWidget()
    }

    /**
     * 初始化下拉刷新布局
     */
    private fun initRefreshLayout() {
        refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4)
        refreshLayout.setProgressViewEndTarget(true, 400)
        refreshLayout.post { refreshLayout.isRefreshing = true }
        refreshLayout.isEnabled = false
    }

    private fun refresh() {
        types.clear()
        citys.clear()
        dataMap.clear()
        if (!refreshLayout!!.isRefreshing) {
            refreshLayout!!.isRefreshing = true
        }
        okHttpCitys()
    }

    private fun initWidget() {
        tvCheck.setOnClickListener(this)
        llDate.setOnClickListener(this)
        llCity.setOnClickListener(this)
        llType.setOnClickListener(this)
        startTimeCheck = sdf2.format(Date().time - 1000 * 60 * 60 * 24)
        tvStartDay.text = startTimeCheck
        tvCityName.text = "海南省"
        refresh()
    }

    private var dialogView: View? = null
    private fun selectDateDialog() {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        dialogView = inflater.inflate(R.layout.dialog_select_date, null)
        dialogView!!.tvMessage.text = "选择查询日期"
        initWheelView()
        val dialog = Dialog(activity, R.style.CustomProgressDialog)
        dialog.setContentView(dialogView)
        dialog.show()
        dialogView!!.llPositive.setOnClickListener { arg0: View? ->
            dialog.dismiss()
            setTextViewValue()
        }
        dialogView!!.llNegative.setOnClickListener { arg0: View? -> dialog.dismiss() }
    }

    private fun initWheelView() {
        val c = Calendar.getInstance()
        val curYear = c[Calendar.YEAR]
        val curMonth = c[Calendar.MONTH] + 1 //通过Calendar算出的月数要+1
        val curDate = c[Calendar.DATE]
        val curHour = c[Calendar.HOUR_OF_DAY]
        val curMinute = c[Calendar.MINUTE]
        val numericWheelAdapter1 = NumericWheelAdapter(activity, 1950, curYear)
        numericWheelAdapter1.setLabel(getString(R.string.year))
        dialogView!!.year.viewAdapter = numericWheelAdapter1
        dialogView!!.year.isCyclic = false //是否可循环滑动
        dialogView!!.year.addScrollingListener(scrollListener)
        val numericWheelAdapter2 = NumericWheelAdapter(activity, 1, 12, "%02d")
        numericWheelAdapter2.setLabel(getString(R.string.month))
        dialogView!!.month.viewAdapter = numericWheelAdapter2
        dialogView!!.month.isCyclic = false
        dialogView!!.month.addScrollingListener(scrollListener)
        initDay(curYear, curMonth)
        dialogView!!.day.isCyclic = false
        val numericWheelAdapter3 = NumericWheelAdapter(activity, 1, 23, "%02d")
        numericWheelAdapter3.setLabel(getString(R.string.hour))
        dialogView!!.hour.viewAdapter = numericWheelAdapter3
        dialogView!!.hour.isCyclic = false
        dialogView!!.hour.addScrollingListener(scrollListener)
        dialogView!!.hour.visibility = View.GONE
        val numericWheelAdapter4 = NumericWheelAdapter(activity, 1, 59, "%02d")
        numericWheelAdapter4.setLabel(getString(R.string.minute))
        dialogView!!.minute.viewAdapter = numericWheelAdapter4
        dialogView!!.minute.isCyclic = false
        dialogView!!.minute.addScrollingListener(scrollListener)
        dialogView!!.minute.visibility = View.GONE
        dialogView!!.year.visibleItems = 7
        dialogView!!.month.visibleItems = 7
        dialogView!!.day.visibleItems = 7
        dialogView!!.hour.visibleItems = 7
        dialogView!!.minute.visibleItems = 7
        dialogView!!.year.currentItem = curYear - 1950
        dialogView!!.month.currentItem = curMonth - 1
        dialogView!!.day.currentItem = curDate - 1
        dialogView!!.hour.currentItem = curHour - 1
        dialogView!!.minute.currentItem = curMinute
    }

    private val scrollListener: OnWheelScrollListener = object : OnWheelScrollListener {
        override fun onScrollingStarted(wheel: WheelView) {}
        override fun onScrollingFinished(wheel: WheelView) {
            val nYear = dialogView!!.year!!.currentItem + 1950 //年
            val nMonth: Int = dialogView!!.month.currentItem + 1 //月
            initDay(nYear, nMonth)
        }
    }

    /**
     *
     */
    private fun initDay(arg1: Int, arg2: Int) {
        val numericWheelAdapter = NumericWheelAdapter(activity, 1, getDay(arg1, arg2), "%02d")
        numericWheelAdapter.setLabel(getString(R.string.day))
        dialogView!!.day.viewAdapter = numericWheelAdapter
    }

    /**
     * @param year
     * @param month
     * @return
     */
    private fun getDay(year: Int, month: Int): Int {
        val day: Int
        val flag: Boolean = when (year % 4) {
            0 -> true
            else -> false
        }
        day = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            2 -> if (flag) 29 else 28
            else -> 30
        }
        return day
    }

    /**
     *
     */
    private fun setTextViewValue() {
        val yearStr = (dialogView!!.year!!.currentItem + 1950).toString()
        val monthStr = if (dialogView!!.month.currentItem + 1 < 10) "0" + (dialogView!!.month.currentItem + 1) else (dialogView!!.month.currentItem + 1).toString()
        val dayStr = if (dialogView!!.day.currentItem + 1 < 10) "0" + (dialogView!!.day.currentItem + 1) else (dialogView!!.day.currentItem + 1).toString()
        val hourStr = if (dialogView!!.hour.currentItem + 1 < 10) "0" + (dialogView!!.hour.currentItem + 1) else (dialogView!!.hour.currentItem + 1).toString()
        val minuteStr = if (dialogView!!.minute.currentItem + 1 < 10) "0" + (dialogView!!.minute.currentItem + 1) else (dialogView!!.minute.currentItem + 1).toString()
        val time = yearStr + getString(R.string.year) + monthStr + getString(R.string.month) + dayStr + getString(R.string.day) + " " + hourStr + getString(R.string.hour)
        startTimeCheck = "$yearStr-$monthStr-$dayStr"
        tvStartDay!!.text = startTimeCheck
    }

    private fun okHttpCitys() {
        Thread(Runnable {
            val url = "http://59.50.130.88:8888/decision-admin/ny/gezrdz"
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
                                val array = JSONArray(result)
                                for (i in 0 until array.length()) {
                                    val itemObj = array.getJSONObject(i)
                                    if (!itemObj.isNull("CityName")) {
                                        val cityName = itemObj.getString("CityName")
                                        if (!TextUtils.isEmpty(cityName)) {
                                            citys.add(cityName)
                                        }
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
            okHttpList()
        }).start()
    }

    private fun okHttpList() {
        Thread(Runnable {
            var dataUrl = "http://59.50.130.88:8888/decision-admin/ny/dayzk?T=%s&C&CityName=%s"
            when(arguments!!.getString(CONST.COLUMN_ID)) {
                "58601" -> dataUrl = "http://59.50.130.88:8888/decision-admin/ny/getbch?type=2&cname=&cdate=%s&cityn=%s&days="//病虫害气象条件预报
                "58602" -> dataUrl = "http://59.50.130.88:8888/decision-admin/ny/getnqzh?type=1&cname=&cdate=%s&cityn=%s&days="//气象灾害
                "58603" -> dataUrl = "http://59.50.130.88:8888/decision-admin/ny/getnqzh?type=2&cname=&cdate=%s&cityn=%s&days="//农气灾害
                "671" -> dataUrl = "http://59.50.130.88:8888/decision-admin/ny/dayzk?T=%s&C&CityName=%s"//逐日监测
            }
            val url = String.format(dataUrl, startTimeCheck, tvCityName.text.toString())
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    activity!!.runOnUiThread {
                        refreshLayout!!.isRefreshing = false
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val array = JSONArray(result)
                                for (i in 0 until array.length()) {
                                    val itemObj = array.getJSONObject(i)
                                    if (!itemObj.isNull("CriterName")) {
                                        val criterName = itemObj.getString("CriterName")
                                        if (!TextUtils.isEmpty(criterName)) {
                                            if (dataMap.containsKey(criterName)) {
                                                if (!itemObj.isNull("DAYS")) {
                                                    if (TextUtils.equals(itemObj.getString("DAYS"), "7")) {//7天预报图
                                                        dataMap[criterName]!!.put("Url7", itemObj.getString("Url"))
                                                    }
                                                }
                                            } else {
                                                dataMap[criterName] = itemObj
                                                types.add(criterName)
                                            }
                                            if (i <= 1) {
                                                setValue(criterName)
                                            }
                                        }
                                    }
                                }
                                if (types.size <= 0) {
                                    scrollView.visibility = View.GONE
                                    tvPrompt.visibility = View.VISIBLE
                                } else {
                                    scrollView.visibility = View.VISIBLE
                                    tvPrompt.visibility = View.GONE
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

    /**
     * 灾害种类
     */
    private fun dialogList() {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_select_string, null)
        dialogView.tvContent.text = "选择灾害种类"
        val dialog = Dialog(activity, R.style.CustomProgressDialog)
        dialog.setContentView(dialogView)
        dialog.show()
        val listView = dialogView.findViewById<ListView>(R.id.listView)
        val monitorAdapter = DisasterMonitorAdapter(activity, types)
        listView.adapter = monitorAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView: AdapterView<*>?, view: View?, i: Int, l: Long ->
            dialog.dismiss()
            setValue(types[i])
        }
    }

    private fun setValue(criterName: String) {
        llContainer.removeAllViews()
        if (!TextUtils.isEmpty(criterName)) {
            tvType.text = criterName
            if (dataMap.containsKey(criterName)) {
                val obj = dataMap[criterName]
                if (obj != null) {
                    try {
                        if (!obj.isNull("CriterionInfo")) {
                            tvInfo.text = "说明\n${obj.getString("CriterionInfo")}"
                        } else if (!obj.isNull("CDATE")) {
                            tvInfo.text = "时间\n${obj.getString("CDATE")}"
                        }
                        if (!obj.isNull("Mark")) {
                            tvMark.text = "说明\n${obj.getString("Mark")}"
                        }
                        if (!obj.isNull("DAYS")) {
                            if (!obj.isNull("Url")) {
                                val textView = TextView(activity)
                                textView.text = "三天预报图"
                                textView.setTextColor(ContextCompat.getColor(activity!!, R.color.text_color3))
                                textView.setPadding(0, CommonUtil.dip2px(activity, 10f).toInt(), 0, CommonUtil.dip2px(activity, 10f).toInt())
                                llContainer.addView(textView)

                                val imageView = ImageView(activity)
                                imageView.adjustViewBounds = true
                                Picasso.get().load(obj.getString("Url")).error(R.drawable.icon_no_pic).into(imageView)
                                llContainer.addView(imageView)
                            }
                            if (!obj.isNull("Url7")) {
                                val textView = TextView(activity)
                                textView.text = "七天预报图"
                                textView.setTextColor(ContextCompat.getColor(activity!!, R.color.text_color3))
                                textView.setPadding(0, CommonUtil.dip2px(activity, 30f).toInt(), 0, CommonUtil.dip2px(activity, 10f).toInt())
                                llContainer.addView(textView)

                                val imageView = ImageView(activity)
                                imageView.adjustViewBounds = true
                                Picasso.get().load(obj.getString("Url7")).error(R.drawable.icon_no_pic).into(imageView)
                                llContainer.addView(imageView)
                            }
                        } else {
                            if (!obj.isNull("Url")) {
                                val imageView = ImageView(activity)
                                imageView.adjustViewBounds = true
                                Picasso.get().load(obj.getString("Url")).error(R.drawable.icon_no_pic).into(imageView)
                                llContainer.addView(imageView)
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * 选择市县
     */
    private fun dialogCity() {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_select_string, null)
        dialogView.tvContent.text = "选择市县"
        val dialog = Dialog(activity, R.style.CustomProgressDialog)
        dialog.setContentView(dialogView)
        dialog.show()
        val listView = dialogView.findViewById<ListView>(R.id.listView)
        val monitorAdapter = DisasterMonitorAdapter(activity, citys)
        listView.adapter = monitorAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView: AdapterView<*>?, view: View?, i: Int, l: Long ->
            dialog.dismiss()
            tvCityName.text = citys[i]
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llDate -> selectDateDialog()
            R.id.llCity -> dialogCity()
            R.id.tvCheck -> refresh()
            R.id.llType -> dialogList()
        }
    }

}
