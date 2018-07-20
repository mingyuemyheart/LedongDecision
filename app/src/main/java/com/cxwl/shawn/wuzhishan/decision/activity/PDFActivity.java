package com.cxwl.shawn.wuzhishan.decision.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * PDF文档
 */

public class PDFActivity extends BaseActivity implements OnClickListener {

	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle,tvPercent;
	private PDFView pdfView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pdf);
		mContext = this;
		initWidget();
		initPDFView();
	}

	private void initWidget() {
		llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		tvPercent = findViewById(R.id.tvPercent);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
	}

	// 根据Unicode编码完美的判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    // 完整的判断中文汉字和符号
    private String isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
            	try {
					strName = strName.replace(c+"", URLEncoder.encode(c+"", "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
            }
        }
		return strName;
    }

	private void initPDFView() {
		pdfView = findViewById(R.id.pdfView);
		pdfView.enableDoubletap(true);
		pdfView.enableSwipe(true);

		String pdfUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (TextUtils.isEmpty(pdfUrl)) {
			return;
		}else {
			pdfUrl = isChinese(pdfUrl);
		}

		asynLoadPdf(pdfUrl);
	}

	/**
	 * 异步下载pdf文件
	 * @param pdfUrl pdf地址
	 */
	private void asynLoadPdf(String pdfUrl) {
		AsynLoadTask task = new AsynLoadTask(new AsynLoadCompleteListener() {
			@Override
			public void loadComplete(File file) {
				if (file != null) {
					tvPercent.setVisibility(View.GONE);
					pdfView.fromFile(file)
							.defaultPage(0)
							.scrollHandle(new DefaultScrollHandle(PDFActivity.this))
							.load();
				}
			}
		}, pdfUrl);
        task.execute();
	}

	private interface AsynLoadCompleteListener {
		void loadComplete(File file);
	}

	private class AsynLoadTask extends AsyncTask<Void, File, File> {

		private String url;
		private AsynLoadCompleteListener completeListener;

		private AsynLoadTask(AsynLoadCompleteListener completeListener, String url) {
			this.url = url;
			this.completeListener = completeListener;
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(File... values) {
		}

		@Override
		protected File doInBackground(Void... params) {
			return downLoadPdf(url);
		}

		@Override
		protected void onPostExecute(File file) {
			if (completeListener != null) {
				completeListener.loadComplete(file);
            }
		}
	}

	/**
	 * 下载pdf文件
	 * @param pdfUrl
	 * @return
	 */
	private File downLoadPdf(String pdfUrl) {
		try {
			URL u = new URL(pdfUrl);
			File file = new File(Environment.getExternalStorageDirectory()+"/Wuzhishan");
			if (!file.exists()) {
				file.mkdirs();
			}
			String path = file.getAbsolutePath()+"/"+"1.pdf";
			byte[] buffer = new byte[1024 * 8];
			int read;
			int ava = 0;
//			long start = System.currentTimeMillis();
			HttpURLConnection urlcon = (HttpURLConnection) u.openConnection();
			double fileLength = (double) urlcon.getContentLength();
			BufferedInputStream bin = new BufferedInputStream(u.openStream());
			BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(path));
			while ((read = bin.read(buffer)) > -1) {
				bout.write(buffer, 0, read);
				ava += read;
				int percent = (int) Math.floor((ava / fileLength * 100));
				Message msg = new Message();
				msg.what = 0;
				msg.obj = percent;
				handler.sendMessage(msg);
//				dialog.setProgress(a);
//				long speed = ava / (System.currentTimeMillis() - start);
			}
			bout.flush();
			bout.close();
			return new File(path);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				tvPercent.setText(msg.obj+"%");
			}
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}
}
