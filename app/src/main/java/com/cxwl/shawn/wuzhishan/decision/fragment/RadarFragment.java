package com.cxwl.shawn.wuzhishan.decision.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.dto.RadarDto;
import com.cxwl.shawn.wuzhishan.decision.manager.RadarManager;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;
import com.cxwl.shawn.wuzhishan.decision.view.PhotoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 雷达图、云图
 */
public class RadarFragment extends Fragment implements OnClickListener, RadarManager.RadarListener {
	
	private List<RadarDto> radarList = new ArrayList<>();
	private PhotoView imageView;
	private RadarManager mRadarManager;
	private RadarThread mRadarThread;
	private static final int HANDLER_SHOW_RADAR = 1;
	private static final int HANDLER_PROGRESS = 2;
	private static final int HANDLER_LOAD_FINISHED = 3;
	private static final int HANDLER_PAUSE = 4;
	private LinearLayout llSeekBar;
	private ImageView ivPlay;
	private SeekBar seekBar;
	private TextView tvTime,tvPercent;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM月dd日HH时mm分");
	private String id,baseUrl,index;
	private MyBroadCastReceiver mReceiver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_radar, null);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initBroadCast();
		initWidget(view);
	}

	private void initBroadCast() {
		this.index = getArguments().getString("index");
		mReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(RadarFragment.class.getName()+index);
		getActivity().registerReceiver(mReceiver, intentFilter);
	}

	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (TextUtils.equals(intent.getAction(), RadarFragment.class.getName()+index)) {
				OkHttpList(baseUrl);
			}
		}
	}

	/**
	 * 初始化控件
	 */
	private void initWidget(View view) {
		imageView = view.findViewById(R.id.imageView);
		imageView.setMaxScale(8f);
//		imageView.id = id;
		ivPlay = view.findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		seekBar = view.findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(seekbarListener);
		tvTime = view.findViewById(R.id.tvTime);
		llSeekBar = view.findViewById(R.id.llSeekBar);
		tvPercent = view.findViewById(R.id.tvPercent);

		mRadarManager = new RadarManager(getActivity());

		this.id = getArguments().getString(CONST.COLUMN_ID);
		this.baseUrl = getArguments().getString(CONST.WEB_URL);

		//默认第一个fragment加载
		if (TextUtils.equals(index, "0")) {
			OkHttpList(baseUrl);
		}
	}
	
	private OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.setCurrent(seekBar.getProgress());
				mRadarThread.stopTracking();
			}
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.startTracking();
			}
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		}
	};
	
	/**
	 * 获取雷达图片集信息
	 */
	private void OkHttpList(final String url) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								radarList.clear();
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);
										JSONArray array = new JSONArray(obj.getString("imgs"));
										for (int i = array.length()-1; i >= 0 ; i--) {
											JSONObject itemObj = array.getJSONObject(i);
											RadarDto dto = new RadarDto();
											dto.url = itemObj.getString("i");
											dto.time = itemObj.getString("n");
											dto.id = id;
											radarList.add(dto);
										}

										if (radarList.size() <= 0) {
											imageView.setImageResource(R.drawable.iv_no_pic);
											llSeekBar.setVisibility(View.GONE);
										}else {
											startDownLoadImgs(radarList);//开始下载
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
	
	private void startDownLoadImgs(List<RadarDto> list) {
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
		if (list.size() > 0) {
			mRadarManager.loadImagesAsyn(list, this, id);
		}
	}
	
	@Override
	public void onResult(int result, List<RadarDto> images) {
		mHandler.sendEmptyMessage(HANDLER_LOAD_FINISHED);
		if (result == RadarManager.RadarListener.RESULT_SUCCESSED) {
//			if (mRadarThread != null) {
//				mRadarThread.cancel();
//				mRadarThread = null;
//			}
//			if (images.size() > 0) {
//				mRadarThread = new RadarThread(images);
//				mRadarThread.start();
//			}
			for (int i = 0; i < images.size(); i++) {
				RadarDto dto = images.get(i);
				if (i == images.size()-1) {
					Message message = mHandler.obtainMessage();
					message.what = HANDLER_SHOW_RADAR;
					message.obj = dto;
					message.arg1 = images.size()-1;
					message.arg2 = images.size()-1;
					mHandler.sendMessage(message);
					break;
				}
			}
		}
	}
	
	private class RadarThread extends Thread {

		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private List<RadarDto> images;
		private int state;
		private int index;
		private int count;
		private boolean isTracking;
		
		private RadarThread(List<RadarDto> images) {
			this.images = images;
			this.count = images.size();
			this.index = 0;
			this.state = STATE_PLAYING;
			this.isTracking = false;
		}

		private int getCurrentState() {
			return state;
		}
		
		@Override
		public void run() {
			super.run();
			while (true) {
				if (state == STATE_CANCEL) {
					break;
				}
				if (state == STATE_PAUSE) {
					continue;
				}
				if (isTracking) {
					continue;
				}
				sendRadar();
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void sendRadar() {
			if (index >= count || index < 0) {
				index = 0;
				
				if (mRadarThread != null) {
					mRadarThread.pause();
					
					Message message = mHandler.obtainMessage();
					message.what = HANDLER_PAUSE;
					mHandler.sendMessage(message);
				}
			}else {
				RadarDto radar = images.get(index);
				Message message = mHandler.obtainMessage();
				message.what = HANDLER_SHOW_RADAR;
				message.obj = radar;
				message.arg1 = count - 1;
				message.arg2 = index ++;
				mHandler.sendMessage(message);
			}
		}

		private void cancel() {
			this.state = STATE_CANCEL;
		}
		private void pause() {
			this.state = STATE_PAUSE;
		}
		private void play() {
			this.state = STATE_PLAYING;
		}
		public void setCurrent(int index) {
			this.index = index;
		}
		public void startTracking() {
			isTracking = true;
		}
		public void stopTracking() {
			isTracking = false;
			if (this.state == STATE_PAUSE) {
				sendRadar();
			}
		}
	}

	@Override
	public void onProgress(String url, int progress) {
		Message msg = new Message();
		msg.obj = progress;
		msg.what = HANDLER_PROGRESS;
		mHandler.sendMessage(msg);
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case HANDLER_SHOW_RADAR: 
				if (msg.obj != null) {
					RadarDto radar = (RadarDto) msg.obj;
					Bitmap bitmap = BitmapFactory.decodeFile(radar.url);
					if (bitmap != null) {
						imageView.setImageBitmap(bitmap);
					}
					changeProgress(radar.time, msg.arg2, msg.arg1);
				}
				break;
			case HANDLER_PROGRESS:
				if (msg.obj != null) {
					int progress = (Integer) msg.obj;
					if (tvPercent != null) {
						tvPercent.setText(progress+getString(R.string.unit_percent));
					}
				}
				break;
			case HANDLER_LOAD_FINISHED:
				tvPercent.setVisibility(View.GONE);
				llSeekBar.setVisibility(View.VISIBLE);
				break;
			case HANDLER_PAUSE:
				if (ivPlay != null) {
					ivPlay.setImageResource(R.drawable.iv_play);
				}
				break;
			default:
				break;
			}
			
		};
	};
	
	private void changeProgress(String time, int progress, int max) {
		if (seekBar != null) {
			seekBar.setMax(max);
			seekBar.setProgress(progress);
		}
		try {
			tvTime.setText(sdf1.format(sdf2.parse(time)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ivPlay:
				if (mRadarThread != null) {
					if (mRadarThread.getCurrentState() == RadarThread.STATE_PAUSE) {
						mRadarThread.play();
						ivPlay.setImageResource(R.drawable.iv_pause);
					} else {
						mRadarThread.pause();
						ivPlay.setImageResource(R.drawable.iv_play);
					}
				}else {
					if (radarList.size() > 0) {
						mRadarThread = new RadarThread(radarList);
						mRadarThread.start();
						ivPlay.setImageResource(R.drawable.iv_pause);
					}
				}
				break;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
		if (mReceiver != null) {
			getActivity().unregisterReceiver(mReceiver);
		}
	}

}
