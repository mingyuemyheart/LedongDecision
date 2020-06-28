package com.cxwl.shawn.wuzhishan.decision.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import kotlinx.android.synthetic.main.activity_webview.*
import kotlinx.android.synthetic.main.layout_title2.*

/**
 * 通用网页
 */
class WebviewActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        initWidget()
        initWebView()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
    }

    /**
     * 初始化webview
     */
    private fun initWebView() {
        val url = intent.getStringExtra(CONST.WEB_URL)
        if (TextUtils.isEmpty(url)) {
            return
        }
        val webSettings = webView.settings
        //支持javascript
        webSettings.javaScriptEnabled = true
        // 设置可以支持缩放
        webSettings.setSupportZoom(true)
        // 设置出现缩放工具
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        //扩大比例的缩放
        webSettings.useWideViewPort = true
        //自适应屏幕
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webSettings.loadWithOverviewMode = true
        webView.loadUrl(url)
        webView.webChromeClient = object : WebChromeClient() {
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, itemUrl: String): Boolean {
                webView.loadUrl(itemUrl)
                return true
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (webView != null) {
            webView!!.reload()
        }
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.llBack -> finish()
        }
    }

}


