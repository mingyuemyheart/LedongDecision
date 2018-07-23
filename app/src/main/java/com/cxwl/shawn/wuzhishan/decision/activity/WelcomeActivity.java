package com.cxwl.shawn.wuzhishan.decision.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData;
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;

/**
 * 闪屏页
 */
public class WelcomeActivity extends BaseActivity {

    private Context mContext;
    private MyBroadCastReceiver mRecerver;
    private List<ColumnData> dataList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mContext = this;

        //点击Home键后再点击APP图标，APP重启而不是回到原来界面
        if (!isTaskRoot()) {
            finish();
            return;
        }
        //点击Home键后再点击APP图标，APP重启而不是回到原来界面

        //判断显示春天或者秋天背景
        SimpleDateFormat sdf1 = new SimpleDateFormat("MMdd");
        ImageView ivLogo = findViewById(R.id.ivLogo);
        int current = Integer.parseInt(sdf1.format(new Date()));
        if (current >= 301 && current <= 831) {
            ivLogo.setImageResource(R.drawable.bg_welcome);
        }else {
            ivLogo.setImageResource(R.drawable.bg_login);
        }

        initBroadCast();
    }

    private void initBroadCast() {
        mRecerver = new MyBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");//网络状态监听
        registerReceiver(mRecerver, intentFilter);
    }

    private class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                switch (networkInfo.getType()) {
                    case TYPE_MOBILE:
                        delayIntent();
//						Toast.makeText(context, "正在使用2G/3G/4G网络", Toast.LENGTH_SHORT).show();
                        break;
                    case TYPE_WIFI:
                        delayIntent();
//						Toast.makeText(context, "正在使用wifi上网", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            } else {
                Toast.makeText(context, "当前无网络连接", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRecerver != null) {
            unregisterReceiver(mRecerver);
        }
    }

    private void delayIntent() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences nameShare = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
                String userName = nameShare.getString(CONST.UserInfo.userName, "");
                String pwd = nameShare.getString(CONST.UserInfo.passWord, "");
                CONST.USERNAME = userName;
                CONST.PASSWORD = pwd;
                if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(pwd)) {
                    OkHttpLogin(userName, pwd);
                }else {
                    startActivity(new Intent(mContext, LoginActivity.class));
                    finish();
                }
            }
        }, 2000);
    }

    /**
     * 登录
     */
    private void OkHttpLogin(final String userName, final String pwd) {
        final String url = "http://decision-admin.tianqi.cn/home/Work/login";
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("username", userName);
        builder.add("password", pwd);
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
                                                                SharedPreferences.Editor editor = nameShare.edit();
                                                                editor.putString(CONST.UserInfo.uId, uid);
                                                                editor.putString(CONST.UserInfo.userName, userName);
                                                                editor.putString(CONST.UserInfo.passWord, pwd);
                                                                editor.apply();

                                                                CONST.USERNAME = userName;
                                                                CONST.PASSWORD = pwd;
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
                                                    cancelDialog();
                                                    startActivity(new Intent(mContext, LoginActivity.class));
                                                    finish();
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

}