package com.cxwl.shawn.wuzhishan.decision.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.dto.WarningDto;

/**
 * 预警详情
 */
public class WarningDetailActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle,tvName,tvTime,tvIntro;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_warning_detail);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("预警详情");
		tvName = findViewById(R.id.tvName);
		tvTime = findViewById(R.id.tvTime);
		tvIntro = findViewById(R.id.tvIntro);

		WarningDto data = getIntent().getParcelableExtra("data");
		if (data != null) {
			if (!TextUtils.isEmpty(data.name)) {
				tvName.setText(data.name);
			}
			if (!TextUtils.isEmpty(data.time)) {
				tvTime.setText(data.time);
			}
			if (!TextUtils.isEmpty(data.content)) {
				tvIntro.setText(data.content);
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}

}
