package com.cxwl.shawn.wuzhishan.decision.activity

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.adapter.CityAdapter
import com.cxwl.shawn.wuzhishan.decision.adapter.CityFragmentAdapter
import com.cxwl.shawn.wuzhishan.decision.dto.CityDto
import com.cxwl.shawn.wuzhishan.decision.manager.DBManager
import kotlinx.android.synthetic.main.activity_city.*
import kotlinx.android.synthetic.main.activity_main.tvTitle
import kotlinx.android.synthetic.main.layout_title.*

class CityActivity : BaseActivity(), View.OnClickListener {

    //搜索城市后的结果列表
    private var cityAdapter : CityAdapter? = null
    private val cityList : ArrayList<CityDto> = ArrayList()

    //省内热门
    private val pList : ArrayList<CityDto> = ArrayList()

    //全国热门
    private val nList : ArrayList<CityDto> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city)
        initWidget()
        initListView()
        initPGridView()
        initNGridView()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvProvince.setOnClickListener(this)
        tvNational.setOnClickListener(this)
        tvTitle.text = "城市选择"
        etSearch.addTextChangedListener(watcher)
    }

    private val watcher = object : TextWatcher {
        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
        override fun afterTextChanged(arg0: Editable) {
            cityList.clear()
            if (TextUtils.isEmpty(arg0.toString().trim())) {
                listView.visibility = View.GONE
                llGridView.visibility = View.VISIBLE
            } else {
                listView.visibility = View.VISIBLE
                llGridView.visibility = View.GONE
                getCityInfo(arg0.toString().trim())
            }
        }
    }

    /**
     * 迁移到天气详情界面
     */
    private fun intentWeatherDetail(data : CityDto) {
        val intent = Intent(this, ForecastActivity::class.java)
        intent.putExtra("cityName", data.disName)
        intent.putExtra("cityId", data.cityId)
        intent.putExtra("lat", data.lat)
        intent.putExtra("lng", data.lng)
        startActivity(intent)
    }

    private fun initListView() {
        cityAdapter = CityAdapter(this, cityList)
        listView.adapter = cityAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener {
            _, _, arg2, _ -> intentWeatherDetail(cityList[arg2])
        }
    }

    /**
     * 初始化省内热门gridview
     */
    private fun initPGridView() {
        pList.clear()
        val stations = resources.getStringArray(R.array.wuzhishan_hotCity)
        for (i in 0 until stations.size) {
            val value = stations[i].split(",")
            val dto = CityDto()
            dto.cityId = value[0]
            dto.disName = value[1]
            dto.lat = value[3].toDouble()
            dto.lng = value[2].toDouble()
            pList.add(dto)
        }

        val pAdapter = CityFragmentAdapter(this, pList)
        pGridView.adapter = pAdapter
        pGridView.onItemClickListener = AdapterView.OnItemClickListener {
            _, _, p2, _ -> intentWeatherDetail(pList[p2])
        }
    }

    /**
     * 初始化全国热门
     */
    private fun initNGridView() {
        nList.clear()
        val stations = resources.getStringArray(R.array.nation_hotCity)
        for (i in stations.indices) {
            val value = stations[i].split(",")
            val dto = CityDto()
            dto.cityId = value[0]
            dto.disName = value[1]
            dto.lat = value[2].toDouble()
            dto.lng = value[3].toDouble()
            nList.add(dto)
        }

        val nAdapter = CityFragmentAdapter(this, nList)
        nGridView.adapter = nAdapter
        nGridView.onItemClickListener = AdapterView.OnItemClickListener {
            _, _, p2, _ -> intentWeatherDetail(nList[p2])
        }
    }

    /**
     * 获取城市信息
     */
    private fun getCityInfo(keyword : String) {
        cityList.clear()
        val dbManager = DBManager(this)
        dbManager.openDateBase()
        val database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null)
        val cursor = database.rawQuery("select * from "+DBManager.TABLE_NAME3+" where pro like "+"\"%"+keyword+"%\""+" or city like "+"\"%"+keyword+"%\""+" or dis like "+"\"%"+keyword+"%\"",null)
        for (i in 0 until cursor.count) {
            cursor.moveToPosition(i)
            val dto = CityDto()
            dto.provinceName = cursor.getString(cursor.getColumnIndex("pro"))
            dto.cityName = cursor.getString(cursor.getColumnIndex("city"))
            dto.disName = cursor.getString(cursor.getColumnIndex("dis"))
            dto.cityId = cursor.getString(cursor.getColumnIndex("cid"))
            dto.lat = cursor.getDouble(cursor.getColumnIndex("lat"))
            dto.lng = cursor.getDouble(cursor.getColumnIndex("lng"))
            cityList.add(dto)
        }
        cursor.close()
        dbManager.closeDatabase()
        if (cityAdapter != null) {
            cityAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.llBack -> finish()
            R.id.tvProvince -> {
                tvProvince.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvNational.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                tvProvince.setBackgroundResource(R.drawable.corner_left_blue)
                tvNational.setBackgroundResource(R.drawable.corner_right_white)
                pGridView.visibility = View.VISIBLE
                nGridView.visibility = View.GONE
            }
            R.id.tvNational -> {
                tvProvince.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                tvNational.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvProvince.setBackgroundResource(R.drawable.corner_left_white)
                tvNational.setBackgroundResource(R.drawable.corner_right_blue)
                pGridView.visibility = View.GONE
                nGridView.visibility = View.VISIBLE
            }
        }
    }

}
