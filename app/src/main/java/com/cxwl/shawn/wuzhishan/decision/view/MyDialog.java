package com.cxwl.shawn.wuzhishan.decision.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import com.cxwl.shawn.wuzhishan.decision.R;

public class MyDialog extends Dialog {

	
	public MyDialog(Context context) {
		super(context);
	}
	
	public MyDialog(Context context, String msg) {
		super(context);
	}
	
	public void setStyle(int featureId) {
		requestWindowFeature(featureId);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawableResource(R.color.transparent);
		setContentView(R.layout.layout_loading);
	}
	
}
