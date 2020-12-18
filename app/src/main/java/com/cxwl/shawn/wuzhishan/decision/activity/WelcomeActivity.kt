package com.cxwl.shawn.wuzhishan.decision.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.text.TextUtils
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException


class WelcomeActivity : BaseActivity() {

    private var mContext : Context? = null
    private var dataList : ArrayList<ColumnData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        mContext = this

        //点击Home键后再点击APP图标，APP重启而不是回到原来界面
        if (!isTaskRoot) {
            finish()
            return
        }
        //点击Home键后再点击APP图标，APP重启而不是回到原来界面

        Handler().postDelayed({
            val nameShare : SharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE)
            val userName : String? = nameShare.getString(CONST.UserInfo.userName, "")
            val pwd : String? = nameShare.getString(CONST.UserInfo.passWord, "")
            CONST.USERNAME = userName
            CONST.PASSWORD = pwd
            if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(pwd)) {
                okHttpLogin(userName, pwd)
            } else {
                startActivity(Intent(mContext, LoginActivity::class.java))
                finish()
            }
        }, 2000)

    }

    private fun okHttpLogin(userName : String?, pwd : String?) {
        val url = "http://59.50.130.88:8888/pyapi170/ld/login"
        val param  = JSONObject()
        param.put("command", "6001")
        val param1 = JSONObject()
        param1.put("username", userName)
        param1.put("password", pwd)
        param1.put("type", "1")
        param.put("object", param1)
        val json : String = param.toString()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, json)
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().post(body).url(url).build(), object : Callback {
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

                                if (!obj.isNull("uid")) {
                                    val uid = obj.getString("uid")
                                    if (!TextUtils.isEmpty(uid)) {
                                        //把用户信息保存在sharedPreferance里
                                        val nameShare = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE)
                                        val editor = nameShare.edit()
                                        editor.putString(CONST.UserInfo.uId, uid)
                                        editor.putString(CONST.UserInfo.userName, userName)
                                        editor.putString(CONST.UserInfo.passWord, pwd)
                                        editor.apply()

                                        CONST.USERNAME = userName
                                        CONST.PASSWORD = pwd
                                        CONST.UID = uid

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

}