package com.cxwl.shawn.wuzhishan.decision.adapter;

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData
import com.squareup.picasso.Picasso

/**
 * 主界面
 */
class MainAdapter constructor(context: Context?, private var mArrayList: ArrayList<ColumnData>) : BaseAdapter() {

	private val mContext : Context = context!!
	private val mInflater : LayoutInflater? = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
	open var height = 0
	
	class ViewHolder{
		var tvName : TextView? = null
		var icon : ImageView? = null
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
			view = mInflater!!.inflate(R.layout.adapter_main, null)
			mHolder = ViewHolder()
			mHolder.tvName = view.findViewById(R.id.tvName)
			mHolder.icon = view.findViewById(R.id.icon)
			view.tag = mHolder
		}else {
			view = convertView
			mHolder = view.tag as ViewHolder
		}

		val dto = mArrayList[position]

		mHolder.tvName!!.text = dto.name

//			if (!TextUtils.isEmpty(dto.icon)) {
//				Picasso.get().load(dto.icon).into(mHolder.icon);
//			}else {
		Picasso.get().load(String.format("http://decision-admin.tianqi.cn/Public/images/hnny/%s.png", dto.id)).into(mHolder.icon)
//			}

		if (height > 0) {
			val params = AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, height/3)
			view.layoutParams = params
			notifyDataSetChanged()
		}

		return view
	}

}
