package com.cxwl.shawn.wuzhishan.decision.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 登录
 */
public class LoginActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext;
	private EditText etUserName,etPwd;
	private List<ColumnData> dataList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mContext = this;
//		Sofia.with(this)
//				.invasionStatusBar()//设置顶部状态栏缩进
//				.statusBarBackground(Color.TRANSPARENT);//设置状态栏颜色
		initWidget();
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		etUserName = findViewById(R.id.etUserName);
		etPwd = findViewById(R.id.etPwd);
		TextView tvLogin = findViewById(R.id.tvLogin);
		tvLogin.setOnClickListener(this);
		ImageView ivLogo = findViewById(R.id.ivLogo);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width/3, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        ivLogo.setLayoutParams(params);

	}

	/**
	 * 登录
	 */
	private void OkHttpLogin() {
		if (TextUtils.isEmpty(etUserName.getText().toString())) {
			Toast.makeText(mContext, "请输入用户名", Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(etPwd.getText().toString())) {
			Toast.makeText(mContext, "请输入密码", Toast.LENGTH_SHORT).show();
			return;
		}

		showDialog();

		final String url = "http://59.50.130.88:8888/decision-api/api/Json";
		try {
			JSONObject param = new JSONObject();
			param.put("command", "6001");
			JSONObject object = new JSONObject();
			object.put("username", etUserName.getText().toString());
			object.put("password", etPwd.getText().toString());
			object.put("type", "1");
			param.put("object", object);
			String json = param.toString();
			final RequestBody body = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
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
											if (!object.isNull("status")) {
												String status  = object.getString("status");
												if (TextUtils.equals(status, "true")) {//成功

													if (!object.isNull("object")) {
														JSONObject obj = new JSONObject(object.getString("object"));

														JSONArray array = obj.getJSONArray("channels");
														dataList.clear();
														for (int i = 0; i < array.length(); i++) {
															JSONObject itemObj = array.getJSONObject(i);
															ColumnData data = new ColumnData();
															if (!itemObj.isNull("id")) {
																data.id = itemObj.getString("id");
															}
															if (!itemObj.isNull("title")) {
																data.name = itemObj.getString("title");
															}
															if (!itemObj.isNull("icon")) {
																data.icon = itemObj.getString("icon");
															}
															if (!itemObj.isNull("columnType")) {
																data.showType = itemObj.getString("columnType");
															}
															if (!itemObj.isNull("dataUrl")) {
																data.dataUrl = itemObj.getString("dataUrl");
															}
															if (!itemObj.isNull("child")) {
																JSONArray childArray = itemObj.getJSONArray("child");
																for (int j = 0; j < childArray.length(); j++) {
																	JSONObject childObj = childArray.getJSONObject(j);
																	ColumnData dto = new ColumnData();
																	if (!childObj.isNull("id")) {
																		dto.id = childObj.getString("id");
																	}
																	if (!childObj.isNull("title")) {
																		dto.name = childObj.getString("title");
																	}
																	if (!childObj.isNull("icon")) {
																		dto.icon = childObj.getString("icon");
																	}
																	if (!childObj.isNull("columnType")) {
																		dto.showType = childObj.getString("columnType");
																	}
																	if (!childObj.isNull("dataUrl")) {
																		dto.dataUrl = childObj.getString("dataUrl");
																	}
																	if (!childObj.isNull("child")) {
																		JSONArray childArray2 = childObj.getJSONArray("child");
																		for (int m = 0; m < childArray2.length(); m++) {
																			JSONObject childObj2 = childArray2.getJSONObject(m);
																			ColumnData dto2 = new ColumnData();
																			if (!childObj2.isNull("id")) {
																				dto2.id = childObj2.getString("id");
																			}
																			if (!childObj2.isNull("title")) {
																				dto2.name = childObj2.getString("title");
																			}
																			if (!childObj2.isNull("icon")) {
																				dto2.icon = childObj2.getString("icon");
																			}
																			if (!childObj2.isNull("columnType")) {
																				dto2.showType = childObj2.getString("columnType");
																			}
																			if (!childObj2.isNull("dataUrl")) {
																				dto2.dataUrl = childObj2.getString("dataUrl");
																			}
																			dto.child.add(dto2);
																		}
																	}
																	data.child.add(dto);
																}
															}
															dataList.add(data);
														}

														if (!obj.isNull("uid")) {
															String uid = obj.getString("uid");
															if (!TextUtils.isEmpty(uid)) {
																//把用户信息保存在sharedPreferance里
																SharedPreferences nameShare = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
																Editor editor = nameShare.edit();
																editor.putString(CONST.UserInfo.uId, uid);
																editor.putString(CONST.UserInfo.userName, etUserName.getText().toString());
																editor.putString(CONST.UserInfo.passWord, etPwd.getText().toString());
																editor.apply();

																CONST.USERNAME = etUserName.getText().toString();
																CONST.PASSWORD = etPwd.getText().toString();
																CONST.UID = uid;

																cancelDialog();
																Intent intent = new Intent(mContext, MainActivity.class);
																Bundle bundle = new Bundle();
																bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) dataList);
																intent.putExtras(bundle);
																startActivity(intent);
																finish();

															}
														}

													}
												}
											}
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}
								}
							});
						}
					});
				}
			}).start();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tvLogin:
				OkHttpLogin();
				break;

		default:
			break;
		}
	}

}
