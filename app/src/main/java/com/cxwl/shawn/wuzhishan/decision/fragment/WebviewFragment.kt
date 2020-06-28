package com.cxwl.shawn.wuzhishan.decision.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import kotlinx.android.synthetic.main.fragment_webview.*

/**
 * 通用网页
 */
class WebviewFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_webview, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebView()
    }

    /**
     * 初始化webview
     */
    private fun initWebView() {
        val url = arguments!!.getString(CONST.WEB_URL)
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

}


