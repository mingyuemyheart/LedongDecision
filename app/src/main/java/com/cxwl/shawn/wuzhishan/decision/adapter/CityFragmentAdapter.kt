package com.cxwl.shawn.wuzhishan.decision.adapter;

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.dto.CityDto

/**
 * 城市列表
 */
class CityFragmentAdapter constructor(context: Context?, private val mArrayList: ArrayList<CityDto>) : BaseAdapter() {
	
	private val mInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

	class ViewHolder{
		var tvName : TextView? = null
	}

	override fun getItem(p0: Int): Any {
		return p0
	}

	override fun getItemId(p0: Int): Long {
		return p0.toLong()
	}

	override fun getCount(): Int {
		return mArrayList.size
	}

	override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
		val mHolder : ViewHolder
		val view : View
		if (convertView == null) {
			view = mInflater.inflate(R.layout.adapter_city_nation, null)
			mHolder = ViewHolder()
			mHolder.tvName = view.findViewById(R.id.tvName)
			view.tag = mHolder
		}else {
			view = convertView
			mHolder = view.tag as ViewHolder
		}

		val dto = mArrayList[position]
		mHolder.tvName!!.text = dto.disName

		return view
	}

}
