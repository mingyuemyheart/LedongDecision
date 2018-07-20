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

public class DialogDetailAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<ShawnRainDto> mArrayList;
	
	private final class ViewHolder{
		TextView tvTime;
	}
	
	private ViewHolder mHolder = null;
	
	public DialogDetailAdapter(Context context, List<ShawnRainDto> mArrayList) {
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
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.dialog_detail_cell, null);
			mHolder = new ViewHolder();
			mHolder.tvTime = convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		ShawnRainDto dto = mArrayList.get(position);
		
		if (!TextUtils.isEmpty(dto.timeString)) {
			mHolder.tvTime.setText(dto.timeString);
		}
		
		return convertView;
	}

}
