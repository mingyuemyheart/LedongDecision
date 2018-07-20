package com.cxwl.shawn.wuzhishan.decision.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.common.MyApplication;
import com.cxwl.shawn.wuzhishan.decision.manager.DataCleanManager;
import com.cxwl.shawn.wuzhishan.decision.util.AutoUpdateUtil;
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private LinearLayout llBack,llFeedBack,llClearCache,llVersion;
    private TextView tvTitle,tvCache,tvVersion,tvLogout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mContext = this;
        initWidget();
    }

    private void initWidget() {
        llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("设置");
        llFeedBack = findViewById(R.id.llFeedBack);
        llFeedBack.setOnClickListener(this);
        llClearCache = findViewById(R.id.llClearCache);
        llClearCache.setOnClickListener(this);
        llVersion = findViewById(R.id.llVersion);
        llVersion.setOnClickListener(this);
        tvCache = findViewById(R.id.tvCache);
        tvVersion = findViewById(R.id.tvVersion);
        tvLogout = findViewById(R.id.tvLogout);
        tvLogout.setOnClickListener(this);

        try {
            String cache = DataCleanManager.getCacheSize(mContext);
            if (cache != null) {
                tvCache.setText(cache);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvVersion.setText(CommonUtil.getVersion(mContext));

    }

    /**
     * 清除缓存
     */
    private void dialogCache() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_cache, null);
        TextView tvContent = view.findViewById(R.id.tvContent);
        TextView tvNegtive = view.findViewById(R.id.tvNegtive);
        TextView tvPositive = view.findViewById(R.id.tvPositive);

        final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
        dialog.setContentView(view);
        dialog.show();

        tvContent.setText("确定要清除缓存？");
        tvNegtive.setText("取消");
        tvPositive.setText("确定");
        tvNegtive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });

        tvPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                DataCleanManager.clearCache(mContext);
                try {
                    String cache = DataCleanManager.getCacheSize(mContext);
                    if (cache != null) {
                        tvCache.setText(cache);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 退出登录
     */
    private void dialogLogout() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_cache, null);
        TextView tvContent = view.findViewById(R.id.tvContent);
        TextView tvNegtive = view.findViewById(R.id.tvNegtive);
        TextView tvPositive = view.findViewById(R.id.tvPositive);

        final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
        dialog.setContentView(view);
        dialog.show();

        tvContent.setText("确定要退出登录？");
        tvNegtive.setText("取消");
        tvPositive.setText("确定");
        tvNegtive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });

        tvPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();

                SharedPreferences sharedPreferences = mContext.getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                MyApplication.destoryActivity();
                Intent intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.llFeedBack:
                startActivity(new Intent(mContext, FeedbackActivity.class));
                break;
            case R.id.llClearCache:
                dialogCache();
                break;
            case R.id.llVersion:
                AutoUpdateUtil.checkUpdate(SettingActivity.this, mContext, "95", getString(R.string.app_name), false);
                break;
            case R.id.tvLogout:
                dialogLogout();
                break;
        }
    }

}
