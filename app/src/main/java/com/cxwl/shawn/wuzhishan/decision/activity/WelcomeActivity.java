package com.cxwl.shawn.wuzhishan.decision.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

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
 * 闪屏页
 */
public class WelcomeActivity extends BaseActivity {

    private Context mContext;
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
        final String url = "http://59.50.130.88:8888/decision-api/api/Json";
        try {
            JSONObject param = new JSONObject();
            param.put("command", "6001");
            JSONObject object = new JSONObject();
            object.put("username", userName);
            object.put("password", pwd);
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
                            Log.e("e", e.getMessage());
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
                                                                SharedPreferences.Editor editor = nameShare.edit();
                                                                editor.putString(CONST.UserInfo.uId, uid);
                                                                editor.putString(CONST.UserInfo.userName, userName);
                                                                editor.putString(CONST.UserInfo.passWord, pwd);
                                                                editor.apply();

                                                                CONST.USERNAME = userName;
                                                                CONST.PASSWORD = pwd;
                                                                CONST.UID = uid;

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

}
