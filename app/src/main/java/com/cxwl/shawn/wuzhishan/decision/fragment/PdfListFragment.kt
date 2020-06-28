package com.cxwl.shawn.wuzhishan.decision.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView.OnItemClickListener
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.activity.PDFActivity
import com.cxwl.shawn.wuzhishan.decision.activity.WebviewActivity
import com.cxwl.shawn.wuzhishan.decision.adapter.PDFListAdapter
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_pdf_list.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * pdf文档列表
 * @author shawn_sun
 */
class PdfListFragment : Fragment() {

    private var mAdapter: PDFListAdapter? = null
    private val dataList: ArrayList<ColumnData> = ArrayList()
    private var page = 1
    private var totalPage = 1
    private val mUIHandler: Handler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pdf_list, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRefreshLayout()
        initListView()
        refresh()
    }

    /**
     * 初始化下拉刷新布局
     */
    private fun initRefreshLayout() {
        refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4)
        refreshLayout.setProgressViewEndTarget(true, 400)
        refreshLayout.isRefreshing = true
        refreshLayout.setOnRefreshListener {
            refresh()
        }
    }

    private fun refresh() {
        page = 1
        var url = arguments!!.getString(CONST.WEB_URL)
        if (!TextUtils.isEmpty(url)) {
            val array = url.split("/")
            url = url.replace("/" + array[array.size - 1], "/$page")
        }
        dataList.clear()
        okHttpList(url)
    }

    private fun onload() {
        page++
        if (page > totalPage) {//最后一页
            return
        }
        var url = arguments!!.getString(CONST.WEB_URL)
        if (!TextUtils.isEmpty(url)) {
            val array = url.split("/")
            url = url.replace("/" + array[array.size - 1], "/$page")
        }
        okHttpList(url)
    }

    private fun initListView() {
        mAdapter = PDFListAdapter(activity!!, dataList)
        listView.adapter = mAdapter
        listView.onItemClickListener = OnItemClickListener { adapterView, view, i, l ->
            val dto = dataList[i]
            val intent = if (dto.detailUrl.endsWith(".pdf") || dto.detailUrl.endsWith(".PDF")) {
                Intent(activity, PDFActivity::class.java)
            } else {
                Intent(activity, WebviewActivity::class.java)
            }
            intent.putExtra(CONST.ACTIVITY_NAME, dto.title)
            intent.putExtra(CONST.WEB_URL, dto.detailUrl)
            startActivity(intent)
        }
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && view.lastVisiblePosition == view.count - 1) {
                    onload()
                }
            }
            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {}
        })
    }

    private fun okHttpList(url: String) {
        if (TextUtils.isEmpty(url)) {
            tvPrompt.visibility = View.VISIBLE
            refreshLayout.isRefreshing = false
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
                    mUIHandler.post {
                        refreshLayout.isRefreshing = false
                        if (!TextUtils.isEmpty(result)) {
                            val obj = JSONObject(result)
                            if (!obj.isNull("totalPage")) {
                                totalPage = Integer.parseInt(obj.getString("totalPage"));
                            }
                            if (!obj.isNull("products")) {
                                val array = obj.getJSONArray("products")
                                for (i in 0 until array.length()) {
                                    val dto = ColumnData()
                                    val itemObj = array.getJSONObject(i)
                                    if (!itemObj.isNull("title")) {
                                        dto.title = itemObj.getString("title")
                                    }
                                    if (!itemObj.isNull("publicTime")) {
                                        dto.time = itemObj.getString("publicTime")
                                    }
                                    if (!itemObj.isNull("filePath")) {
                                        dto.detailUrl = itemObj.getString("filePath")
                                    }
                                    dataList.add(dto)
                                }

                                if (mAdapter != null) {
                                    mAdapter!!.notifyDataSetChanged()
                                }
                                if (dataList.size == 0) {
                                    tvPrompt.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }
            })
        }).start()
    }

}
