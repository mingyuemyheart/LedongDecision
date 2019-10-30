package com.cxwl.shawn.wuzhishan.decision.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.common.MyApplication
import com.cxwl.shawn.wuzhishan.decision.manager.DataCleanManager
import com.cxwl.shawn.wuzhishan.decision.util.AutoUpdateUtil
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.dialog_cache.view.*
import kotlinx.android.synthetic.main.layout_title.*

/**
 * 设置界面
 */
class SettingActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        initWidget()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        llFeedBack.setOnClickListener(this)
        llClearCache.setOnClickListener(this)
        llVersion.setOnClickListener(this)
        tvLogout.setOnClickListener(this)
        llAbout.setOnClickListener(this)

        tvTitle.text = "设置"
        tvUserName.text = CONST.USERNAME
        tvCache.text = DataCleanManager.getCacheSize(this)
        tvVersion.text = CommonUtil.getVersion(this)
    }

    /**
     * 清除缓存
     */
    @SuppressLint("InflateParams")
    private fun dialogCache() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val view = inflater!!.inflate(R.layout.dialog_cache, null)

        val dialog = Dialog(this, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.show()

        view.tvContent.text = "确定要清除缓存？"
        view.tvNegtive.text = "取消"
        view.tvPositive.text = "确定"
        view.tvNegtive.setOnClickListener { dialog.dismiss() }

        view.tvPositive.setOnClickListener {
            dialog.dismiss()
            DataCleanManager.clearCache(this@SettingActivity)
            tvCache.text = DataCleanManager.getCacheSize(this@SettingActivity)
        }
    }

    /**
     * 退出登录
     */
    @SuppressLint("InflateParams")
    private fun dialogLogout() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val view = inflater!!.inflate(R.layout.dialog_cache, null)

        val dialog = Dialog(this, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.show()

        view.tvContent.text = "确定要退出登录？"
        view.tvNegtive.text = "取消"
        view.tvPositive.text = "确定"
        view.tvNegtive.setOnClickListener { dialog.dismiss() }

        view.tvPositive.setOnClickListener {
            dialog.dismiss()
            val  sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            MyApplication.destoryActivity()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.llBack -> finish()
            R.id.llFeedBack -> startActivity(Intent(this, FeedbackActivity::class.java))
            R.id.llAbout -> {}
            R.id.llClearCache -> dialogCache()
            R.id.llVersion -> AutoUpdateUtil.checkUpdate(this, this, "120", getString(R.string.app_name), false)
            R.id.tvLogout -> dialogLogout()
        }
    }

}
