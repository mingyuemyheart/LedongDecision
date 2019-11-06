package com.cxwl.shawn.wuzhishan.decision.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.dto.RiceRaiseDto
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 生态气象-水稻长势
 */
class RiceRaiseAdapter constructor(context: Context, private var mArrayList : ArrayList<RiceRaiseDto>?): BaseAdapter() {

	private val mInflater : LayoutInflater? = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
	private val sdf1 : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0", Locale.CHINA)
	private val sdf2 : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

	class ViewHolder{
		var tvStation : TextView? = null
		var tvCropClass : TextView? = null
		var tvCropName : TextView? = null
		var tvCropType : TextView? = null
		var tvCropMature : TextView? = null
		var tvCropDev : TextView? = null
		var tvv56007 : TextView? = null
		var tvv56006 : TextView? = null
		var tvv56008 : TextView? = null
		var tvv56005 : TextView? = null
		var tvv56004 : TextView? = null
		var tvTime : TextView? = null
	}

	override fun getItem(p0: Int): Any {
		return p0
	}

	override fun getItemId(p0: Int): Long {
		return p0.toLong()
	}

	override fun getCount(): Int {
		return mArrayList!!.size
	}

	override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
		val mHolder : ViewHolder
		val view : View
		if (convertView == null) {
			view = mInflater!!.inflate(R.layout.adapter_rice_raise, null)
			mHolder = ViewHolder()
			mHolder.tvStation = view.findViewById(R.id.tvStation)
			mHolder.tvCropClass = view.findViewById(R.id.tvCropClass)
			mHolder.tvCropName = view.findViewById(R.id.tvCropName)
			mHolder.tvCropType = view.findViewById(R.id.tvCropType)
			mHolder.tvCropMature = view.findViewById(R.id.tvCropMature)
			mHolder.tvCropDev = view.findViewById(R.id.tvCropDev)
			mHolder.tvv56007 = view.findViewById(R.id.tvv56007)
			mHolder.tvv56006 = view.findViewById(R.id.tvv56006)
			mHolder.tvv56008 = view.findViewById(R.id.tvv56008)
			mHolder.tvv56005 = view.findViewById(R.id.tvv56005)
			mHolder.tvv56004 = view.findViewById(R.id.tvv56004)
			mHolder.tvTime = view.findViewById(R.id.tvTime)
			view.tag = mHolder
		}else {
			view = convertView
			mHolder = view.tag as ViewHolder
		}

		val dto = mArrayList!![position]

		mHolder.tvStation!!.text = dto.C_Stat_Name
		mHolder.tvCropClass!!.text = dto.C_Crop
		mHolder.tvCropName!!.text = dto.C_CropName
		mHolder.tvCropType!!.text = dto.C_CropVirteties
		mHolder.tvCropMature!!.text = dto.C_CropMature
		mHolder.tvCropDev!!.text = dto.C_CorpDev
		mHolder.tvv56007!!.text = dto.v56007
		mHolder.tvv56006!!.text = dto.v56006
		mHolder.tvv56008!!.text = dto.v56008
		mHolder.tvv56005!!.text = dto.v56005
		mHolder.tvv56004!!.text = dto.v56004
		mHolder.tvTime!!.text = sdf2.format(sdf1.parse(dto.D5603))

		return view
	}

}
