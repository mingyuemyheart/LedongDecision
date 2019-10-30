package com.cxwl.shawn.wuzhishan.decision.activity

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class FeedbackActivity : BaseActivity(), View.OnClickListener {

    private val mUIHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        initWidget()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.text = "意见反馈"
        tvControl.text = "提交"
        tvControl.visibility = View.VISIBLE
        tvControl.setOnClickListener(this)
    }

    private fun OkHttpFeedback() {
        val url = "http://decision-admin.tianqi.cn/home/Work/request"
        val builder = FormBody.Builder()
        builder.add("uid", CONST.UID)
        builder.add("appid", CONST.APPID)
        builder.add("content", etContent.text.toString())
        val body = builder.build()
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().post(body).url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    mUIHandler.post { cancelDialog() }
                    val result = response.body!!.string()
                    if (!TextUtils.isEmpty(result)) {
                        val obj = JSONObject(result)
                        if (!obj.isNull("status")) {
                            val status = obj.getString("status")
                            if (TextUtils.equals(status, "1")) {//成功
                                mUIHandler.post {
                                    Toast.makeText(this@FeedbackActivity, "提交成功，感谢您的宝贵建议！", Toast.LENGTH_SHORT).show()
                                }
                                finish()
                            }else {
                                //失败
                                if (!obj.isNull("msg")) {
                                    val msg = obj.getString("msg")
                                    if (msg != null) {
                                        mUIHandler.post {
                                            Toast.makeText(this@FeedbackActivity, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }).start()
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.llBack -> finish()
            R.id.tvControl -> {
                if (TextUtils.isEmpty(etContent.text.toString())) {
                    return
                }
                showDialog()
                OkHttpFeedback()
            }
        }
    }

}