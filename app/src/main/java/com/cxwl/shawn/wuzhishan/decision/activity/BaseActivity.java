package com.cxwl.shawn.wuzhishan.decision.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.cxwl.shawn.wuzhishan.decision.view.MyDialog;

public class BaseActivity extends Activity {

    private MyDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 初始化dialog
     */
    public void showDialog() {
        if (mDialog == null) {
            mDialog = new MyDialog(this);
        }
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }
    public void cancelDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

}
