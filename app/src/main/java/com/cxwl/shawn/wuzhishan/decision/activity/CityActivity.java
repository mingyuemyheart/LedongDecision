package com.cxwl.shawn.wuzhishan.decision.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.List;

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
}
