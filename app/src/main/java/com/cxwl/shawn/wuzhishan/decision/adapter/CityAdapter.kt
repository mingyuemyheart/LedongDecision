package com.cxwl.shawn.wuzhishan.decision.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.dto.CityDto

class CityAdapter constructor(context : Context?, private val mArrayList : ArrayList<CityDto>): BaseAdapter() {

    private var mInflater : LayoutInflater? = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?

    override fun getCount(): Int = mArrayList.size

    override fun getItem(p0: Int): Any = p0

    override fun getItemId(p0: Int): Long = p0.toLong()

    class ViewHolder {
        var tvName : TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder : ViewHolder
        val view : View
        if (convertView == null) {
            view = mInflater!!.inflate(R.layout.adapter_city_search, null)
            viewHolder = ViewHolder()
            viewHolder.tvName = view.findViewById(R.id.tvName)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val dto = mArrayList[position]
        if (TextUtils.equals(dto.provinceName, dto.cityName))  {
            viewHolder.tvName!!.text = dto.cityName + "-" +dto.disName
        }else {
            viewHolder.tvName!!.text = dto.provinceName + "-" + dto.cityName + "-" +dto.disName
        }

        return view
    }

}