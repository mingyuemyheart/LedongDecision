package com.cxwl.shawn.wuzhishan.decision.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.dto.ShawnRainDto;

import java.util.List;

/**
 * 实况资料-站点详情列表
 */
public class FactStationListAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<ShawnRainDto> mArrayList;
	
	private final class ViewHolder{
		TextView tvStationName,tvArea,tvValue;
	}
	
	public FactStationListAdapter(Context context, List<ShawnRainDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_fact_station_list, null);
			mHolder = new ViewHolder();
			mHolder.tvStationName = convertView.findViewById(R.id.tvStationName);
			mHolder.tvArea = convertView.findViewById(R.id.tvArea);
			mHolder.tvValue = convertView.findViewById(R.id.tvValue);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		ShawnRainDto dto = mArrayList.get(position);
		
		if (!TextUtils.isEmpty(dto.stationName)) {
			mHolder.tvStationName.setText(dto.stationName);
		}
		
		if (!TextUtils.isEmpty(dto.area)) {
			mHolder.tvArea.setText(dto.area);
		}
		
		mHolder.tvValue.setText(dto.value+"");
		
		if (position % 2 == 0) {
			convertView.setBackgroundColor(0xffeaeaea);
		}else {
			convertView.setBackgroundColor(0xfff5f5f5);
		}
		
		return convertView;
	}

}
