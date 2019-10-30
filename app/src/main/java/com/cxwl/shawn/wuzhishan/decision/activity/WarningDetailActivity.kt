package com.cxwl.shawn.wuzhishan.decision.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.dto.WarningDto
import kotlinx.android.synthetic.main.activity_warning_detail.*
import kotlinx.android.synthetic.main.layout_title.*

/**
 * 预警详情
 */
class WarningDetailActivity : BaseActivity(), OnClickListener {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_warning_detail)
		initWidget()
	}
	
	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		llBack.setOnClickListener(this)
		tvTitle.text = "预警详情"

		val data : WarningDto? = intent.getParcelableExtra<WarningDto>("data")
		if (data != null) {
			if (!TextUtils.isEmpty(data.name)) {
				tvName.text = data.name
			}
			if (!TextUtils.isEmpty(data.time)) {
				tvTime.text = data.time
			}
			if (!TextUtils.isEmpty(data.content)) {
				tvIntro.text = data.content
			}
		}
	}

	override fun onClick(view: View?) {
		when(view!!.id) {
			R.id.llBack -> finish()
		}
	}

}
