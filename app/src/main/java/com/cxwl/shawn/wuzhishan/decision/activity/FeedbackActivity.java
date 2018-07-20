package com.cxwl.shawn.wuzhishan.decision.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 意见反馈
 */
public class FeedbackActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext;
	private LinearLayout llBack;
	private TextView tvTitle,tvControl;
	private EditText etContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);
		mContext = this;
		initWidget();
	}
	
	private void initWidget() {
		llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("意见反馈");
		tvControl = findViewById(R.id.tvControl);
		tvControl.setText("提交");
		tvControl.setVisibility(View.VISIBLE);
		tvControl.setOnClickListener(this);
		etContent = findViewById(R.id.etContent);
	}
	
	/**
	 * 意见反馈
	 */
	private void OkHttpFeedback() {
		final String url = "http://decision-admin.tianqi.cn/home/Work/request";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("uid", CONST.UID);
		builder.add("appid", CONST.APPID);
		builder.add("content", etContent.getText().toString());
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("status")) {
												String status  = object.getString("status");
												if (TextUtils.equals(status, "1")) {//成功
													Toast.makeText(mContext, "提交成功，感谢您的宝贵建议！", Toast.LENGTH_SHORT).show();
													finish();
												}else {
													//失败
													if (!object.isNull("msg")) {
														String msg = object.getString("msg");
														if (msg != null) {
															Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
														}
													}
												}
											}
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
								cancelDialog();
							}
						});
					}
				});
			}
		}).start();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				finish();
				break;
			case R.id.tvControl:
				if (TextUtils.isEmpty(etContent.getText().toString())) {
					return;
				}
				showDialog();
				OkHttpFeedback();
				break;
		}
	}

}
