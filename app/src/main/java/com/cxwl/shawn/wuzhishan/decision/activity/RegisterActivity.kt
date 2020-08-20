package com.cxwl.shawn.wuzhishan.decision.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.adapter.DisasterMonitorAdapter
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.dialog_select_string.view.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern

/**
 * 用户注册
 */
class RegisterActivity : BaseActivity(), OnClickListener {

    private val units: MutableList<String> = ArrayList()//公司类型
    private val citys: MutableList<String> = ArrayList()//选择市县

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
        tvUnitType.setOnClickListener(this)
        tvCityName.setOnClickListener(this)
        tvRegister.setOnClickListener(this)

        okHttpUnit()
        okHttpCitys()
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
        if (TextUtils.isEmpty(etUnit.text.toString())) {
            Toast.makeText(this, "请输入公司名称，无公司请写无", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(tvUnitType!!.text.toString())) {
            Toast.makeText(this, "请选择公司类型", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(tvCityName.text.toString())) {
            Toast.makeText(this, "请选择所在市县", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(etPhone!!.text.toString())) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**
     * 用户注册
     */
    private fun okHttpRegister() {
        val url = "http://59.50.130.88:8888/decision-admin/ny/useradd?username=${etUserName!!.text}&pwd=${etPwd.text}&department=${etUnit.text}&email=${tvUnitType.text}&mobile=${etPhone.text}&sx=${tvCityName.text}"
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

    /**
     * 选择公司
     */
    private fun dialogUnit() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_select_string, null)
        dialogView.tvContent.text = "选择公司类型"
        val dialog = Dialog(this, R.style.CustomProgressDialog)
        dialog.setContentView(dialogView)
        dialog.show()
        val listView = dialogView.findViewById<ListView>(R.id.listView)
        val monitorAdapter = DisasterMonitorAdapter(this, units)
        listView.adapter = monitorAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView: AdapterView<*>?, view: View?, i: Int, l: Long ->
            dialog.dismiss()
            tvUnitType.text = units[i]
        }
    }

    /**
     * 获取公司类型
     */
    private fun okHttpUnit() {
        units.clear()
        Thread(Runnable {
            val url = "http://decision-admin.tianqi.cn/Home/work2019/hnny_getgslx"
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val array = JSONArray(result)
                                for (i in 0 until array.length()) {
                                    val itemObj = array.getJSONObject(i)
                                    if (!itemObj.isNull("GName")) {
                                        val unitName = itemObj.getString("GName")
                                        if (!TextUtils.isEmpty(unitName)) {
                                            if (i == 0) {
                                                tvUnitType.text = unitName
                                            }
                                            units.add(unitName)
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

    private fun okHttpCitys() {
        citys.clear()
        Thread(Runnable {
            val url = "http://59.50.130.88:8888/decision-admin/ny/gezrdz"
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val array = JSONArray(result)
                                for (i in 0 until array.length()) {
                                    val itemObj = array.getJSONObject(i)
                                    if (!itemObj.isNull("CityName")) {
                                        val cityName = itemObj.getString("CityName")
                                        if (!TextUtils.isEmpty(cityName)) {
                                            if (i == 0) {
                                                tvCityName.text = cityName
                                            }
                                            citys.add(cityName)
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

    /**
     * 选择市县
     */
    private fun dialogCity() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_select_string, null)
        dialogView.tvContent.text = "选择市县"
        val dialog = Dialog(this, R.style.CustomProgressDialog)
        dialog.setContentView(dialogView)
        dialog.show()
        val listView = dialogView.findViewById<ListView>(R.id.listView)
        val monitorAdapter = DisasterMonitorAdapter(this, citys)
        listView.adapter = monitorAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView: AdapterView<*>?, view: View?, i: Int, l: Long ->
            dialog.dismiss()
            tvCityName.text = citys[i]
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
            R.id.tvUnitType -> dialogUnit()
            R.id.tvCityName -> {
                dialogCity()
            }
            R.id.tvRegister -> register()
        }
    }
	
}
