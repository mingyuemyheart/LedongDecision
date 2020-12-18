package com.cxwl.shawn.wuzhishan.decision.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class LoginActivity : BaseActivity(), View.OnClickListener {

    private var mContext : Context? = null
    private val dataList : ArrayList<ColumnData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mContext = this
        initWidget()
    }

    private fun initWidget() {
        tvLogin.setOnClickListener(this)
        tvRegister.setOnClickListener(this)

        val param = LinearLayout.LayoutParams(CommonUtil.widthPixels(this)/3, LinearLayout.LayoutParams.WRAP_CONTENT)
        param.gravity = Gravity.CENTER_HORIZONTAL
        ivLogo.layoutParams = param
    }

    private fun okHttpLogin() {
        if (TextUtils.isEmpty(etUserName.text.toString())) {
            Toast.makeText(mContext, "请输入用户名", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(etPwd.text.toString())) {
            Toast.makeText(mContext, "请输入密码", Toast.LENGTH_SHORT).show()
            return
        }

        showDialog()
        val url = "http://59.50.130.88:8888/pyapi170/ld/login"
        val param  = JSONObject()
        param.put("command", "6001")
        val param1 = JSONObject()
        param1.put("username", etUserName.text.toString())
        param1.put("password", etPwd.text.toString())
        param1.put("type", "1")
        param.put("object", param1)
        val json : String = param.toString()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, json)
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().post(body).url(url).build(), object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    if (!TextUtils.isEmpty(result)) {
                        val objects = JSONObject(result)
                        if (!objects.isNull("status")) {
                            val status = objects.getString("status")
                            if (TextUtils.equals(status, "true")) {//成功
                                val obj  = JSONObject(objects.getString("object"))
                                if (!obj.isNull("channels")) {
                                    val array = obj.getJSONArray("channels")
                                    dataList.clear()
                                    for (i in 0 until array.length()) {
                                        val itemObj = array.getJSONObject(i)
                                        val data = ColumnData()
                                        if (!itemObj.isNull("id")) {
                                            data.id = itemObj.getString("id")
                                        }
                                        if (!itemObj.isNull("title")) {
                                            data.name = itemObj.getString("title")
                                        }
                                        if (!itemObj.isNull("icon")) {
                                            data.icon = itemObj.getString("icon")
                                        }
                                        if (!itemObj.isNull("columnType")) {
                                            data.showType = itemObj.getString("columnType")
                                        }
                                        if (!itemObj.isNull("dataUrl")) {
                                            data.dataUrl = itemObj.getString("dataUrl")
                                            if (!data.dataUrl.startsWith("http")) {
                                                if (!itemObj.isNull("listUrl")) {
                                                    data.dataUrl = itemObj.getString("listUrl")
                                                }
                                            }
                                        }
                                        if (!itemObj.isNull("child")) {
                                            val childArray = itemObj.getJSONArray("child")
                                            for (j in 0 until childArray.length()) {
                                                val childObj = childArray.getJSONObject(j)
                                                val dto = ColumnData()
                                                if (!childObj.isNull("id")) {
                                                    dto.id = childObj.getString("id")
                                                }
                                                if (!childObj.isNull("title")) {
                                                    dto.name = childObj.getString("title")
                                                }
                                                if (!childObj.isNull("icon")) {
                                                    dto.icon = childObj.getString("icon")
                                                }
                                                if (!childObj.isNull("columnType")) {
                                                    dto.showType = childObj.getString("columnType")
                                                }
                                                if (!childObj.isNull("dataUrl")) {
                                                    dto.dataUrl = childObj.getString("dataUrl")
                                                    if (!dto.dataUrl.startsWith("http")) {
                                                        if (!childObj.isNull("listUrl")) {
                                                            dto.dataUrl = childObj.getString("listUrl")
                                                        }
                                                    }
                                                }
                                                if (!childObj.isNull("child")) {
                                                    val childArray2 = childObj.getJSONArray("child")
                                                    for (m in 0 until childArray2.length()) {
                                                        val childObj2 = childArray2.getJSONObject(m)
                                                        val dto2 = ColumnData()
                                                        if (!childObj2.isNull("id")) {
                                                            dto2.id = childObj2.getString("id")
                                                        }
                                                        if (!childObj2.isNull("title")) {
                                                            dto2.name = childObj2.getString("title")
                                                        }
                                                        if (!childObj2.isNull("icon")) {
                                                            dto2.icon = childObj2.getString("icon")
                                                        }
                                                        if (!childObj2.isNull("columnType")) {
                                                            dto2.showType = childObj2.getString("columnType")
                                                        }
                                                        if (!childObj2.isNull("dataUrl")) {
                                                            dto2.dataUrl = childObj2.getString("dataUrl")
                                                            if (!dto2.dataUrl.startsWith("http")) {
                                                                if (!childObj2.isNull("listUrl")) {
                                                                    dto2.dataUrl = childObj2.getString("listUrl")
                                                                }
                                                            }
                                                        }

                                                        //过滤掉实况资料里部分内容
//                                                    if (!TextUtils.equals(dto2.id, "631") && !TextUtils.equals(dto2.id, "648")
//                                                            && !TextUtils.equals(dto2.id, "657") && !TextUtils.equals(dto2.id, "658")
//                                                            && !TextUtils.equals(dto2.id, "634") && !TextUtils.equals(dto2.id, "655")) {
                                                        dto.child.add(dto2)
//                                                    }
                                                    }
                                                }
                                                data.child.add(dto)
                                            }
                                        }
                                        dataList.add(data)
                                    }
                                }

                                if (!obj.isNull("uid")) {
                                    val uid = obj.getString("uid")
                                    if (!TextUtils.isEmpty(uid)) {
                                        //把用户信息保存在sharedPreferance里
                                        val nameShare = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE)
                                        val editor = nameShare.edit()
                                        editor.putString(CONST.UserInfo.uId, uid)
                                        editor.putString(CONST.UserInfo.userName, etUserName.text.toString())
                                        editor.putString(CONST.UserInfo.passWord, etPwd.text.toString())
                                        editor.apply()

                                        CONST.USERNAME = etUserName.text.toString()
                                        CONST.PASSWORD = etPwd.text.toString()
                                        CONST.UID = uid

                                        cancelDialog()
                                        val intent = Intent(mContext, MainActivity::class.java)
                                        val bundle = Bundle()
                                        bundle.putParcelableArrayList("dataList", dataList as ArrayList<out Parcelable>)
                                        intent.putExtras(bundle)
                                        startActivity(intent)
                                        finish()

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
            R.id.tvLogin -> okHttpLogin()
            R.id.tvRegister -> startActivityForResult(Intent(this, RegisterActivity::class.java), 1001)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1001 -> {
                    if (data != null) {
                        if (data.extras != null) {
                            val bundle = data.extras
                            val userName = bundle.getString("userName")
                            val pwd = bundle.getString("pwd")
                            etUserName.setText(userName)
                            etPwd.setText(pwd)
                            okHttpLogin()
                        }
                    }
                }
            }
        }
    }

}