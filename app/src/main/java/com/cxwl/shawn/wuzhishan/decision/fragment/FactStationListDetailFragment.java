package com.cxwl.shawn.wuzhishan.decision.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.dto.ShawnRainDto;
import com.cxwl.shawn.wuzhishan.decision.view.RainView;
import com.cxwl.shawn.wuzhishan.decision.view.TemperatureView;
import com.cxwl.shawn.wuzhishan.decision.view.WindView;

import java.util.ArrayList;
import java.util.List;

/**
 * 实况资料-曲线图
 */
public class FactStationListDetailFragment extends Fragment {
	
	private LinearLayout llContainer1;
	private TextView tvPrompt;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_fact_station_list_detail, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	private void initWidget(View view) {
		llContainer1 = view.findViewById(R.id.llContainer1);
		tvPrompt = view.findViewById(R.id.tvPrompt);
		
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		
		int index = getArguments().getInt("index", 0);
		
		List<ShawnRainDto> tempList = new ArrayList<>(getArguments().<ShawnRainDto>getParcelableArrayList("dataList"));
		List<ShawnRainDto> dataList = new ArrayList<>();
		dataList.clear();
		for (int i = 0; i < tempList.size(); i++) {
			ShawnRainDto dto = tempList.get(i);
			if (index == 0) {
				if (dto.factRain != 999999) {
					dataList.add(dto);
				}
			}else if (index == 1) {
				if (dto.factTemp != 999999) {
					dataList.add(dto);
				}
			}else if (index == 2) {
				if (dto.factWind != 999999) {
					dataList.add(dto);
				}
			}
		}
		if (dataList.size() > 0) {
			if (index == 0) {
				RainView rainView = new RainView(getActivity());
				rainView.setData(dataList);
				llContainer1.removeAllViews();
				int viewWidth;
				if (dataList.size() <= 25) {
					viewWidth = width*2;
				}else {
					viewWidth = width*4;
				}
				llContainer1.addView(rainView, viewWidth, LinearLayout.LayoutParams.MATCH_PARENT);
			}else if (index == 1) {
				TemperatureView temperatureView = new TemperatureView(getActivity());
				temperatureView.setData(dataList);
				llContainer1.removeAllViews();
				int viewWidth;
				if (dataList.size() <= 25) {
					viewWidth = width*2;
				}else {
					viewWidth = width*4;
				}
				llContainer1.addView(temperatureView, viewWidth, LinearLayout.LayoutParams.MATCH_PARENT);
			}else if (index == 2) {
				WindView windView = new WindView(getActivity());
				windView.setData(dataList);
				llContainer1.removeAllViews();
				int viewWidth;
				if (dataList.size() <= 25) {
					viewWidth = width*2;
				}else {
					viewWidth = width*4;
				}
				llContainer1.addView(windView, viewWidth, LinearLayout.LayoutParams.MATCH_PARENT);
			}
			
		}else {
			tvPrompt.setVisibility(View.VISIBLE);
		}
	}
	
}
