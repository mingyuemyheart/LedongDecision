package com.cxwl.shawn.wuzhishan.decision.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.activity.PDFActivity;
import com.cxwl.shawn.wuzhishan.decision.adapter.PDFListAdapter;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData;
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * pdf文档列表
 * @author shawn_sun
 *
 */

public class PdfListFragment extends Fragment {
	
	private ListView listView = null;
	private PDFListAdapter mAdapter = null;
	private List<ColumnData> mList = new ArrayList<>();
	private TextView tvPrompt = null;
	private int page = 1, totalPage = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_pdf_list, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initListView(view);
		refresh();
	}

	private void refresh() {
		page = 1;
		String url = getArguments().getString(CONST.WEB_URL);
		if (!TextUtils.isEmpty(url)) {
			String[] array = url.split("/");
			url = url.replace("/"+array[array.length-1], "/"+page);
		}
		OkHttpList(url);
	}

	private void onload() {
		page++;
		if (page > totalPage) {//最后一页
			return;
		}
		String url = getArguments().getString(CONST.WEB_URL);
		if (!TextUtils.isEmpty(url)) {
			String[] array = url.split("/");
			url = url.replace("/"+array[array.length-1], "/"+page);
		}
		OkHttpList(url);
	}
	
	private void initListView(View view) {
		tvPrompt = view.findViewById(R.id.tvPrompt);

		listView = view.findViewById(R.id.listView);
		mAdapter = new PDFListAdapter(getActivity(), mList);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColumnData dto = mList.get(arg2);
				Intent intent = new Intent(getActivity(), PDFActivity.class);
				intent.putExtra(CONST.ACTIVITY_NAME, dto.title);
				intent.putExtra(CONST.WEB_URL, dto.detailUrl);
				startActivity(intent);
			}
		});
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && view.getLastVisiblePosition() == view.getCount() - 1) {
					onload();
                }
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
	}

	private void OkHttpList(final String url) {
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
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("totalPage")) {
											totalPage = Integer.parseInt(obj.getString("totalPage"));
										}
										if (!obj.isNull("products")) {
											JSONArray array = obj.getJSONArray("products");
											for (int i = 0; i < array.length(); i++) {
												ColumnData dto = new ColumnData();
												JSONObject itemObj = array.getJSONObject(i);
												if (!itemObj.isNull("title")) {
													dto.title = itemObj.getString("title");
												}
												if (!itemObj.isNull("publicTime")) {
													dto.time = itemObj.getString("publicTime");
												}
												if (!itemObj.isNull("filePath")) {
													dto.detailUrl = itemObj.getString("filePath");
												}
												mList.add(dto);
											}

											if (mAdapter != null) {
												mAdapter.notifyDataSetChanged();
											}

											if (mList.size() == 0) {
												tvPrompt.setVisibility(View.VISIBLE);
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
	
}
