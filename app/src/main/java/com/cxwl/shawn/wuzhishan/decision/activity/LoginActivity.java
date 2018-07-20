package com.cxwl.shawn.wuzhishan.decision.activity;

/**
 * 登录界面
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Build;
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
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;
import com.cxwl.shawn.wuzhishan.decision.util.sofia.Sofia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 登录
 */
public class LoginActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext = null;
	private EditText etUserName,etPwd;
	private TextView tvLogin;
	private ImageView ivLogo;
	private List<ColumnData> dataList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mContext = this;
		Sofia.with(this)
				.invasionStatusBar()//设置顶部状态栏缩进
				.statusBarBackground(Color.TRANSPARENT);//设置状态栏颜色
		initWidget();
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		etUserName = findViewById(R.id.etUserName);
		etPwd = findViewById(R.id.etPwd);
		tvLogin = findViewById(R.id.tvLogin);
		tvLogin.setOnClickListener(this);
        ivLogo = findViewById(R.id.ivLogo);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width/2, LinearLayout.LayoutParams.WRAP_CONTENT);
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

		final String url = "http://decision-admin.tianqi.cn/home/Work/login";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("username", etUserName.getText().toString());
		builder.add("password", etPwd.getText().toString());
		builder.add("appid", CONST.APPID);
		builder.add("device_id", Build.DEVICE+ Build.SERIAL);
		builder.add("platform", "android");
		builder.add("os_version", android.os.Build.VERSION.RELEASE);
		builder.add("software_version", CommonUtil.getVersion(mContext));
		builder.add("mobile_type", android.os.Build.MODEL);
		builder.add("address", "");
		builder.add("lat", "0");
		builder.add("lng", "0");
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
													JSONArray array = object.getJSONArray("column");
													dataList.clear();
													for (int i = 0; i < array.length(); i++) {
														JSONObject obj = array.getJSONObject(i);
														ColumnData data = new ColumnData();
														if (!obj.isNull("id")) {
															data.columnId = obj.getString("id");
														}
														if (!obj.isNull("localviewid")) {
															data.id = obj.getString("localviewid");
														}
														if (!obj.isNull("name")) {
															data.name = obj.getString("name");
														}
														if (!obj.isNull("icon")) {
															data.icon = obj.getString("icon");
														}
														if (!obj.isNull("icon2")) {
															data.icon2 = obj.getString("icon2");
														}
														if (!obj.isNull("showtype")) {
															data.showType = obj.getString("showtype");
														}
														if (!obj.isNull("dataurl")) {
															data.dataUrl = obj.getString("dataurl");
														}
														if (!obj.isNull("child")) {
															JSONArray childArray = obj.getJSONArray("child");
															for (int j = 0; j < childArray.length(); j++) {
																JSONObject childObj = childArray.getJSONObject(j);
																ColumnData dto = new ColumnData();
																if (!childObj.isNull("id")) {
																	dto.columnId = childObj.getString("id");
																}
																if (!childObj.isNull("localviewid")) {
																	dto.id = childObj.getString("localviewid");
																}
																if (!childObj.isNull("name")) {
																	dto.name = childObj.getString("name");
																}
																if (!childObj.isNull("icon")) {
																	dto.icon = childObj.getString("icon");
																}
																if (!childObj.isNull("icon2")) {
																	dto.icon2 = childObj.getString("icon2");
																}
																if (!childObj.isNull("showtype")) {
																	dto.showType = childObj.getString("showtype");
																}
																if (!childObj.isNull("dataurl")) {
																	dto.dataUrl = childObj.getString("dataurl");
																}
																data.child.add(dto);
															}
														}
														dataList.add(data);
													}

													if (!object.isNull("info")) {
														JSONObject obj = new JSONObject(object.getString("info"));
														if (!obj.isNull("id")) {
															String uid = obj.getString("id");
															if (!TextUtils.isEmpty(uid)) {
																//把用户信息保存在sharedPreferance里
																SharedPreferences nameShare = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
																Editor editor = nameShare.edit();
																editor.putString(CONST.UserInfo.uId, uid);
																editor.putString(CONST.UserInfo.userName, etUserName.getText().toString());
																editor.putString(CONST.UserInfo.passWord, etPwd.getText().toString());
																editor.commit();

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
												}else {
													//失败
													if (!object.isNull("msg")) {
														final String msg = object.getString("msg");
														if (msg != null) {
															Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
														}
														cancelDialog();
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
