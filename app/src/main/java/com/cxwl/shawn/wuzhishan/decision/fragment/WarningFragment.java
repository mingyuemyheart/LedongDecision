package com.cxwl.shawn.wuzhishan.decision.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.adapter.WarningAdapter;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.dto.WarningDto;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class WarningFragment extends Fragment {

	private WarningAdapter mAdapter;
	private List<WarningDto> dataList = new ArrayList<>();
	private List<String> cityNames = new ArrayList<>();
	private TextView tvPrompt = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_warning, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initListView(view);
		refresh();
	}

	private void refresh() {
		String url = getArguments().getString(CONST.WEB_URL);
		if (!TextUtils.isEmpty(url)) {
			OkHttpWarning(url);
		}
	}

	private void initListView(View view) {
		tvPrompt = view.findViewById(R.id.tvPrompt);

		ListView listView = view.findViewById(R.id.listView);
		mAdapter = new WarningAdapter(getActivity(), cityNames, dataList);
		listView.setAdapter(mAdapter);
	}

	/**
	 * 获取预警信息
	 */
	private void OkHttpWarning(final String url) {
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
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
										if (!object.isNull("w")) {
											JSONArray jsonArray = object.getJSONArray("w");
											Map<String, String> map = new HashMap<>();
											int length = jsonArray.length();
											if (length > 50) {
												length = 50;
											}
											dataList.clear();
											for (int i = 0; i < length; i++) {
												WarningDto dto = new WarningDto();
												JSONObject itemObj = jsonArray.getJSONObject(i);
												String w1 = itemObj.getString("w1");
												dto.w2 = itemObj.getString("w2");
												String w4 = itemObj.getString("w4");
												String w5 = itemObj.getString("w5");
												String w6 = itemObj.getString("w6");
												String w7 = itemObj.getString("w7");
												String w8 = itemObj.getString("w8");
												String w9 = itemObj.getString("w9");
												dto.w11 = itemObj.getString("w11");

												dto.name = w1+dto.w2+"发布"+w5+w7+"预警";
												dto.time = w8;
												dto.type = "icon_warning_"+w4;
												dto.color = w6;
												dto.content = w9;

												dataList.add(dto);

												if (!TextUtils.isEmpty(dto.w2)) {
													map.put(dto.w2, dto.w2);
												}
											}

											cityNames.clear();
											for (Map.Entry<String, String > entry : map.entrySet()) {
												cityNames.add(entry.getKey());
											}

											if (mAdapter != null) {
												mAdapter.notifyDataSetChanged();
											}
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}

								if (cityNames.size() == 0) {
									tvPrompt.setVisibility(View.VISIBLE);
								}else {
									tvPrompt.setVisibility(View.GONE);
								}
							}
						});
					}
				});
			}
		}).start();
	}
	
}
