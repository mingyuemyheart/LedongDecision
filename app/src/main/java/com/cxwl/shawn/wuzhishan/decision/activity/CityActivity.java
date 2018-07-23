package com.cxwl.shawn.wuzhishan.decision.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.adapter.CityAdapter;
import com.cxwl.shawn.wuzhishan.decision.adapter.CityFragmentAdapter;
import com.cxwl.shawn.wuzhishan.decision.dto.CityDto;
import com.cxwl.shawn.wuzhishan.decision.manager.DBManager;
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
 * 城市选择
 */
public class CityActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext;
	private LinearLayout llBack,llGroup,llGridView;//返回按钮
	private TextView tvTitle,tvProvince,tvNational;
	private EditText etSearch;

	//搜索城市后的结果列表
	private ListView mListView;
	private CityAdapter cityAdapter;
	private List<CityDto> cityList = new ArrayList<>();

	//省内热门
	private GridView pGridView;
	private CityFragmentAdapter pAdapter;
	private List<CityDto> pList = new ArrayList<>();

	//全国热门
	private GridView nGridView;
	private CityFragmentAdapter nAdapter;
	private List<CityDto> nList = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		mContext = this;
		initWidget();
		initListView();
		initPGridView();
		initNGridView();
//		OkHttpList();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		etSearch = findViewById(R.id.etSearch);
		etSearch.addTextChangedListener(watcher);
		tvProvince = findViewById(R.id.tvProvince);
		tvProvince.setOnClickListener(this);
		tvNational = findViewById(R.id.tvNational);
		tvNational.setOnClickListener(this);
		llGroup = findViewById(R.id.llGroup);
		llGridView = findViewById(R.id.llGridView);
		llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("城市选择");
	}
	
	private TextWatcher watcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			if (arg0.toString() == null) {
				return;
			}

			cityList.clear();
			if (arg0.toString().trim().equals("")) {
				mListView.setVisibility(View.GONE);
				llGroup.setVisibility(View.VISIBLE);
				llGridView.setVisibility(View.VISIBLE);
			}else {
				mListView.setVisibility(View.VISIBLE);
				llGridView.setVisibility(View.GONE);
				llGroup.setVisibility(View.GONE);
				getCityInfo(arg0.toString().trim());
			}

		}
	};
	
	/**
	 * 迁移到天气详情界面
	 */
	private void intentWeatherDetail(CityDto data) {
		Intent intent = new Intent(mContext, ForecastActivity.class);
		intent.putExtra("cityName", data.disName);
		intent.putExtra("cityId", data.cityId);
		intent.putExtra("lat", data.lat);
		intent.putExtra("lng", data.lng);
		startActivity(intent);
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = findViewById(R.id.listView);
		cityAdapter = new CityAdapter(mContext, cityList);
		mListView.setAdapter(cityAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentWeatherDetail(cityList.get(arg2));
			}
		});
	}
	
	/**
	 * 初始化省内热门gridview
	 */
	private void initPGridView() {
		pList.clear();
		String[] stations = getResources().getStringArray(R.array.wuzhishan_hotCity);
		for (int i = 0; i < stations.length; i++) {
			String[] value = stations[i].split(",");
			CityDto dto = new CityDto();
			dto.cityId = value[0];
			dto.disName = value[1];
			dto.lat = Double.valueOf(value[3]);
			dto.lng = Double.valueOf(value[2]);
			pList.add(dto);
		}
		
		pGridView = findViewById(R.id.pGridView);
		pAdapter = new CityFragmentAdapter(mContext, pList);
		pGridView.setAdapter(pAdapter);
		pGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentWeatherDetail(pList.get(arg2));
			}
		});
	}
	
	/**
	 * 初始化全国热门
	 */
	private void initNGridView() {
		nList.clear();
		String[] stations = getResources().getStringArray(R.array.nation_hotCity);
		for (int i = 0; i < stations.length; i++) {
			String[] value = stations[i].split(",");
			CityDto dto = new CityDto();
			dto.cityId = value[0];
			dto.disName = value[1];
			dto.lat = Double.valueOf(value[2]);
			dto.lng = Double.valueOf(value[3]);
			nList.add(dto);
		}

		nGridView = findViewById(R.id.nGridView);
		nAdapter = new CityFragmentAdapter(mContext, nList);
		nGridView.setAdapter(nAdapter);
		nGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentWeatherDetail(nList.get(arg2));
			}
		});
	}
	
	/**
	 * 获取城市信息
	 */
	private void getCityInfo(String keyword) {
		cityList.clear();
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor;
		cursor = database.rawQuery("select * from "+DBManager.TABLE_NAME3+" where pro like "+"\"%"+keyword+"%\""+" or city like "+"\"%"+keyword+"%\""+" or dis like "+"\"%"+keyword+"%\"",null);
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			CityDto dto = new CityDto();
			dto.provinceName = cursor.getString(cursor.getColumnIndex("pro"));
			dto.cityName = cursor.getString(cursor.getColumnIndex("city"));
			dto.disName = cursor.getString(cursor.getColumnIndex("dis"));
			dto.cityId = cursor.getString(cursor.getColumnIndex("cid"));
			dto.lat = cursor.getDouble(cursor.getColumnIndex("lat"));
			dto.lng = cursor.getDouble(cursor.getColumnIndex("lng"));
			cityList.add(dto);
		}
		if (cityList.size() > 0 && cityAdapter != null) {
			cityAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.tvProvince:
			tvProvince.setTextColor(getResources().getColor(R.color.white));
			tvNational.setTextColor(getResources().getColor(R.color.colorPrimary));
			tvProvince.setBackgroundResource(R.drawable.corner_left_blue);
			tvNational.setBackgroundResource(R.drawable.corner_right_white);
			pGridView.setVisibility(View.VISIBLE);
			nGridView.setVisibility(View.GONE);
			break;
		case R.id.tvNational:
			tvProvince.setTextColor(getResources().getColor(R.color.colorPrimary));
			tvNational.setTextColor(getResources().getColor(R.color.white));
			tvProvince.setBackgroundResource(R.drawable.corner_left_white);
			tvNational.setBackgroundResource(R.drawable.corner_right_blue);
			pGridView.setVisibility(View.GONE);
			nGridView.setVisibility(View.VISIBLE);
			break;

		default:
			break;
		}
	}

	private void OkHttpList() {
		final String url = "http://restapi.amap.com/v3/config/district?key=a1a4b9ae34b547cbeacf5d84f7df0657&keywords=西藏&subdistrict=2&extensions=base";
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
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);
										JSONArray districts = obj.getJSONArray("districts");
										for (int i = 0; i < districts.length(); i++) {
											JSONObject itemObj = districts.getJSONObject(i);
											String name = itemObj.getString("name");
											String adcode = itemObj.getString("adcode");
											String c = itemObj.getString("center");
											if (!TextUtils.equals(c, ",")) {
												String[] center = itemObj.getString("center").split(",");
												double lat = Double.parseDouble(center[1]);
												double lng = Double.parseDouble(center[0]);
												String sql = String.format("update warning_id set lat = %s, lng = %s where wid = %s", lat, lng, adcode)+";";
												Log.e("districts", sql);
											}

											JSONArray districts2 = itemObj.getJSONArray("districts");
											for (int j = 0; j < districts2.length(); j++) {
												JSONObject itemObj2 = districts2.getJSONObject(j);
												String name2 = itemObj2.getString("name");
												String adcode2 = itemObj2.getString("adcode");
												String c2 = itemObj2.getString("center");
												if (!TextUtils.equals(c2, ",")) {
													String[] center2 = itemObj2.getString("center").split(",");
													double lat2 = Double.parseDouble(center2[1]);
													double lng2 = Double.parseDouble(center2[0]);
													String sql2 = String.format("update warning_id set lat = %s, lng = %s where wid = %s", lat2, lng2, adcode2)+";";
													Log.e("districts", sql2);
												}

												JSONArray districts3 = itemObj2.getJSONArray("districts");
												for (int m = 0; m < districts3.length(); m++) {
													JSONObject itemObj3 = districts3.getJSONObject(m);
													String name3 = itemObj3.getString("name");
													String adcode3 = itemObj3.getString("adcode");
													String c3 = itemObj3.getString("center");
													if (!TextUtils.equals(c3, ",")) {
														String[] center3 = itemObj3.getString("center").split(",");
														double lat3 = Double.parseDouble(center3[1]);
														double lng3 = Double.parseDouble(center3[0]);
														String sql3 = String.format("update warning_id set lat = %s, lng = %s where wid = %s", lat3, lng3, adcode3)+";";
														Log.e("districts", sql3);
													}
												}

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
