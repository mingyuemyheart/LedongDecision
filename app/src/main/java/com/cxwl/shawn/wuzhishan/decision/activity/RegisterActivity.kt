package com.cxwl.shawn.wuzhishan.decision.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern

/**
 * 用户注册
 */
class RegisterActivity : BaseActivity(), OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        initWidget()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.text = "用户注册"
        tvRegister.setOnClickListener(this)
    }

    private fun register() {
        if (checkInfo()) {
            showDialog()
            okHttpRegister()
        }
    }

    /**
     * 判断是否为数字格式不限制位数
     */
    private fun isNumber(input: String): Boolean {
        return Pattern.compile("[0-9]*").matcher(input).matches()
    }

    private fun isCharacter(input: String): Boolean {
        return Pattern.compile("[a-z]*").matcher(input).matches()
    }

    /**
     * 验证用户信息
     */
    private fun checkInfo(): Boolean {
        if (TextUtils.isEmpty(etUserName!!.text.toString())) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(etPwd.text.toString())) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etPwd.text.toString().length <= 8) {
            Toast.makeText(this, "密码要求8位以上字母、数字、特殊字符组合", Toast.LENGTH_SHORT).show()
            return false
        }
        if (isNumber(etPwd.text.toString()) || isCharacter(etPwd.text.toString())) {
            Toast.makeText(this, "密码要求8位以上字母、数字、特殊字符组合", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(etPwdConfirm.text.toString())) {
            Toast.makeText(this, "请再次输入密码", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!TextUtils.equals(etPwd.text.toString(), etPwdConfirm.text.toString())) {
            Toast.makeText(this, "密码不一致", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(etPhone!!.text.toString())) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(etUnit.text.toString())) {
            Toast.makeText(this, "请输入所在单位", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(etEmail.text.toString())) {
            Toast.makeText(this, "请输入邮箱", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**
     * 用户注册
     */
    private fun okHttpRegister() {
        val url = "http://59.50.130.88:8888/decision-admin/ny/useradd?username=${etUserName!!.text}&pwd=${etPwd.text}&department=${etUnit.text}&email=${etEmail.text}&mobile=${etPhone.text}"
        Log.e("url", url)
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        cancelDialog()
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val `object` = JSONObject(result)
                                if (!`object`.isNull("code")) {
                                    val status = `object`.getInt("code")
                                    if (status == 200) { //成功
                                        val intent = Intent()
                                        intent.putExtra("userName", etUserName!!.text.toString())
                                        intent.putExtra("pwd", etPwd.text.toString())
                                        setResult(RESULT_OK, intent)
                                        finish()
                                    } else {
                                        //失败
                                        if (!`object`.isNull("msg")) {
                                            val msg = `object`.getString("msg")
                                            if (msg != null) {
                                                Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }).start()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
            R.id.tvRegister -> register()
        }
    }
	
}
