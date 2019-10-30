package com.cxwl.shawn.wuzhishan.decision.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.activity.WarningDetailActivity
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.dto.WarningDto
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil

/**
 * 预警信息
 */
class WarningAdapter constructor(private var context: Context, private var mArrayList: ArrayList<String>?, dataList : ArrayList<WarningDto>): BaseAdapter() {
	
	private var mInflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
	private var dataList : ArrayList<WarningDto>? = dataList

	class ViewHolder {
		var tvCity : TextView? = null
		var llContainer : LinearLayout? = null
	}

	override fun getCount(): Int = mArrayList!!.size

	override fun getItem(p0: Int): Any = p0

	override fun getItemId(p0: Int): Long = p0.toLong()

	@SuppressLint("InflateParams")
	override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
		val viewHolder : ViewHolder
		val view : View
		if (convertView == null) {
			view = mInflater.inflate(R.layout.adapter_warning, null)
			viewHolder = ViewHolder()
			viewHolder.llContainer = view.findViewById(R.id.llContainer)
			viewHolder.tvCity = view.findViewById(R.id.tvCity)
			view.tag = viewHolder
		} else {
			view = convertView
			viewHolder = view.tag as ViewHolder
		}

		val name: String = mArrayList!![position]
		viewHolder.tvCity!!.text = name

		if (viewHolder.llContainer != null) {
			viewHolder.llContainer!!.removeAllViews()
			for (i in dataList!!.indices) {
				val data = dataList!![i]
				if (TextUtils.equals(name, data.w2)) {
					val imageView = ImageView(context)
					val params = LinearLayout.LayoutParams(CommonUtil.dip2px(context, 20f).toInt(), CommonUtil.dip2px(context, 20f).toInt())
					params.rightMargin = 10
					imageView.layoutParams = params
					var bitmap = CommonUtil.getImageFromAssetsFile(context, "warning/"+data.type+data.color+CONST.imageSuffix)
					if (bitmap == null) {
						bitmap = CommonUtil.getImageFromAssetsFile(context, "warning/" + "default" + data.color + CONST.imageSuffix)
					}
					imageView.setImageBitmap(bitmap)
					viewHolder.llContainer!!.addView(imageView)

					imageView.setOnClickListener {
						val intent = Intent(context, WarningDetailActivity::class.java)
						val bundle = Bundle()
						bundle.putParcelable("data", data)
						intent.putExtras(bundle)
						context.startActivity(intent)
					}
				}
			}
		}

		return view
	}

}
