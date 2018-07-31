package com.cxwl.shawn.wuzhishan.decision.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.shawn.wuzhishan.decision.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 自动更新
 * @author shawn_sun
 *
 */
public class AutoUpdateUtil {
	
	private static Context mContext = null;
	private static String appName = null;
	private static boolean flag = true;
	
	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	private static int getVersionCode(Context context) {
	    try {
	        PackageManager manager = context.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
	        return info.versionCode;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0;
	    }
	}

	/**
	 * 检查更新
	 * @param context
	 * @param app_id
	 * @param is_flag true为主界面自己请求，false为个人点击获取
	 */
	public static void checkUpdate(final Activity activity, Context context, String app_id, String app_name, boolean is_flag) {
		mContext = context;
		appName = app_name;
		flag = is_flag;
		if (TextUtils.isEmpty(app_id)) {
			Toast.makeText(context, "The app_id is empty", Toast.LENGTH_SHORT).show();
			return;
		}
		final String url = "https://app.tianqi.cn/update/check";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("app_id", app_id);
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new okhttp3.Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);
										UpdateDto dto = new UpdateDto();
										if (!obj.isNull("version")) {
											dto.version = obj.getString("version");
										}
										if (!obj.isNull("update_info")) {
											dto.update_info = obj.getString("update_info");
										}
										if (!obj.isNull("dl_url")) {
											dto.dl_url = obj.getString("dl_url");
										}
										if (!obj.isNull("versionCode")) {
											dto.versionCode = obj.getInt("versionCode");
										}

										//检查版本不一样时候才更新
										if (dto.versionCode > getVersionCode(mContext)) {
											Message msg = new Message();
											msg.what = 1000;
											msg.obj = dto;
											handler.sendMessage(msg);
										}else {
											if (!flag) {
												Toast.makeText(mContext, "已经是最新版本", Toast.LENGTH_SHORT).show();
											}
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}
	
	private static class UpdateDto {
		public String version = "";
		public String update_info = "";
		public String dl_url = "";
		public int versionCode = 0;
	}

	@SuppressLint("HandlerLeak")
	private static Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1000:
				UpdateDto dto = (UpdateDto) msg.obj;
				updateDialog(dto);
				break;

			default:
				break;
			}
		};
	};

	private static void updateDialog(final UpdateDto dto) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_auto_update, null);
		TextView tvTitle = view.findViewById(R.id.tvTitle);
		TextView tvContent = view.findViewById(R.id.tvContent);
		TextView tvNegtive = view.findViewById(R.id.tvNegtive);
		TextView tvPositive = view.findViewById(R.id.tvPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		tvTitle.setText("发现新版本");
		if (!TextUtils.isEmpty(dto.version)) {
			tvContent.setText("更新版本："+dto.version+"\n"+dto.update_info);
		}
		tvNegtive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		tvPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				if (!TextUtils.isEmpty(dto.dl_url)) {
					new Thread() {
						public void run() {
							intoDownloadManager(dto.dl_url);
						}
					}.start();
				}
			}
		});
	}

	private static void intoDownloadManager(String dl_url) {
		DownloadManager dManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		Uri uri = Uri.parse(dl_url);
		Request request = new Request(uri);
		// 设置下载路径和文件名
		String filename = dl_url.substring(dl_url.lastIndexOf("/") + 1);//获取文件名称
		request.setDestinationInExternalPublicDir("download", filename);
		request.setDescription(appName);
		request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		request.setMimeType("application/vnd.android.package-archive");
		// 设置为可被媒体扫描器找到
		request.allowScanningByMediaScanner();
		// 设置为可见和可管理
		request.setVisibleInDownloadsUi(true);
		long refernece = dManager.enqueue(request);
//		// 把当前下载的ID保存起来
		SharedPreferences sPreferences = mContext.getSharedPreferences("downloadplato", 0);
		sPreferences.edit().putLong("plato", refernece).apply();

	} 

}
