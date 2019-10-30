package com.cxwl.shawn.wuzhishan.decision.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.util.AuthorityUtil
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import kotlinx.android.synthetic.main.activity_pdf.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * PDF文档
 */
class PDFActivity : BaseActivity(), OnClickListener {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_pdf)
		initWidget()
		checkAuthority()
	}

	private fun initWidget() {
		llBack.setOnClickListener(this)

		val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
		if (!TextUtils.isEmpty(title)) {
			tvTitle.text = title
		}
	}

	private fun initPDFView() {
		pdfView.enableDoubletap(true)
		pdfView.enableSwipe(true)

		val pdfUrl = intent.getStringExtra(CONST.WEB_URL)
		OkHttpFile(pdfUrl)
	}

	private fun OkHttpFile(url : String) {
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {
				}
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val totalLength = response.body!!.contentLength()//获取文件大小
					val inputStream = response.body!!.byteStream()//获取输入流
					val buffer = ByteArray(1024)
					val files = File(Environment.getExternalStorageDirectory().toString()+"/HainanNQ")
					if (!files.exists()) {
						files.mkdirs()
					}
					val filePath = files.absolutePath+"/"+"1.pdf"
					var len = 0
					var sum = 0

					val fos = FileOutputStream(filePath)
					while (inputStream.read(buffer).apply { len = this } > 0) {
						fos.write(buffer, 0, len)
						sum += len

						val percent = (sum * 100 / totalLength).toInt()
						val msg = handler.obtainMessage(1001)
						msg.what = 1001
						msg.obj = filePath
						msg.arg1 = percent
						handler.sendMessage(msg)
					}
					fos.flush()
					fos.close()// 下载完成

				}
			})
		}).start()
	}

	private val handler : Handler = @SuppressLint("HandlerLeak")
	object : Handler() {
		override fun handleMessage(msg: Message?) {
			super.handleMessage(msg)
			when(msg!!.what) {
				1001 -> {
					if (tvPercent == null || pdfView == null) {
						return
					}
					val percent = msg.arg1
					tvPercent.text = percent.toString()+getString(R.string.unit_percent)
					if (percent >= 100) {
						tvPercent.visibility = View.GONE
						val filePath = msg.obj.toString()
						if (!TextUtils.isEmpty(filePath)) {
							val file = File(filePath)
							if (file.exists()) {
								pdfView.fromFile(file)
										.defaultPage(0)
										.scrollHandle(DefaultScrollHandle(this@PDFActivity))
										.load()
							}
						}
					}
				}
			}
		}
	}

	override fun onClick(view: View?) {
		when(view!!.id) {
			R.id.llBack -> finish()
		}
	}

	/**
	 * 申请权限
	 */
	private fun checkAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			initPDFView()
		} else {
			val checkSelfPermission = ContextCompat.checkSelfPermission(this@PDFActivity,
					Manifest.permission.WRITE_EXTERNAL_STORAGE)
			if (checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
				initPDFView()
			} else {
				ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), AuthorityUtil.AUTHOR_STORAGE)
			}
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			AuthorityUtil.AUTHOR_STORAGE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				initPDFView()
			} else {
				if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					AuthorityUtil.intentAuthorSetting(this, "\"" + getString(R.string.app_name) + "\"" + "需要使用存储权限，是否前往设置？")
				}
			}
		}
	}

}
