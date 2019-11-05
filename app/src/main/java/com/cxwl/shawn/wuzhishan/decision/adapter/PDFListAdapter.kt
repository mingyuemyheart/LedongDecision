package com.cxwl.shawn.wuzhishan.decision.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData

/**
 * pdf文档列表
 */
class PDFListAdapter constructor(context: Context, private val mArrayList: ArrayList<ColumnData>) : BaseAdapter() {
	
	private val mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

	class ViewHolder{
		var tvTitle : TextView? = null
		var tvTime : TextView? = null
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

	override fun getView(position: Int, convertView: View?, p2: ViewGroup?): View {
		val mHolder : ViewHolder
		val view : View
		if (convertView == null) {
			view = mInflater.inflate(R.layout.adapter_pdf_list, null)
			mHolder = ViewHolder()
			mHolder.tvTime = view.findViewById(R.id.tvTime)
			mHolder.tvTitle = view.findViewById(R.id.tvTitle)
			view.tag = mHolder
		}else {
			view = convertView
			mHolder = view.tag as ViewHolder
		}

		val dto = mArrayList[position]
		if (!TextUtils.isEmpty(dto.title)) {
			mHolder.tvTitle!!.text = dto.title
		}
		if (!TextUtils.isEmpty(dto.time)) {
			mHolder.tvTime!!.text = dto.time
		}
		return view
	}
}
