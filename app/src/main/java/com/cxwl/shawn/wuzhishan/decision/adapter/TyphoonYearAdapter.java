package com.cxwl.shawn.wuzhishan.decision.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.dto.TyphoonDto;

import java.util.HashMap;
import java.util.List;

public class TyphoonYearAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<TyphoonDto> mArrayList;
	public HashMap<Integer, Boolean> isSelected = new HashMap<>();
	
	private final class ViewHolder{
		TextView tvYear;
		RelativeLayout reLayout;
	}
	
	private ViewHolder mHolder = null;
	
	public TyphoonYearAdapter(Context context, List<TyphoonDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for (int i = 0; i < mArrayList.size(); i++) {
			if (i == 0) {
				isSelected.put(i, true);
			}else {
				isSelected.put(i, false);
			}
		}
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
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.typhoon_year_cell, null);
			mHolder = new ViewHolder();
			mHolder.tvYear = convertView.findViewById(R.id.tvYear);
			mHolder.reLayout = convertView.findViewById(R.id.reLayout);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		TyphoonDto dto = mArrayList.get(position);
		mHolder.tvYear.setText(String.valueOf(dto.yearly)+"年");
		
		if (!isSelected.isEmpty()) {
			if (isSelected.get(position) == false) {
				mHolder.reLayout.setBackgroundResource(R.drawable.bg_typhoon_year);
			}else {
				mHolder.reLayout.setBackgroundResource(R.drawable.bg_typhoon_year_press);
			}
		}
		
		return convertView;
	}

}
