package com.cxwl.shawn.wuzhishan.decision.activity

import android.app.Activity
import android.os.Bundle
import com.cxwl.shawn.wuzhishan.decision.view.MyDialog

open class BaseActivity : Activity() {

    private var mDialog : MyDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * 初始化dialog
     */
    fun showDialog() {
        if (mDialog == null) {
            mDialog = MyDialog(this)
        }
        if (!mDialog!!.isShowing) {
            mDialog!!.show()
        }
    }
    fun cancelDialog() {
        if (mDialog != null) {
            mDialog!!.dismiss()
        }
    }

}
