package com.cxwl.shawn.wuzhishan.decision.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.dto.TyphoonDto;

import java.util.HashMap;
import java.util.List;

public class TyphoonNameAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<TyphoonDto> mArrayList;
	public HashMap<Integer, Boolean> isSelected = new HashMap<>();
	
	private final class ViewHolder{
		ImageView ivStatus;
		TextView tvName;
	}
	
	public TyphoonNameAdapter(Context context, List<TyphoonDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for (int i = 0; i < mArrayList.size(); i++) {
			isSelected.put(i, false);
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
		ViewHolder mHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_typhoon_name, null);
			mHolder = new ViewHolder();
			mHolder.ivStatus = convertView.findViewById(R.id.ivStatus);
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		TyphoonDto dto = mArrayList.get(position);
		if (TextUtils.isEmpty(dto.name) || TextUtils.equals(dto.name, "null")) {
			mHolder.tvName.setText(dto.code + " " + dto.enName);
		}else {
			mHolder.tvName.setText(dto.code + " " + dto.name + " " + dto.enName);
		}
		
		if (!isSelected.isEmpty() && isSelected.get(position) != null) {
			if (!isSelected.get(position)) {
				mHolder.ivStatus.setImageResource(R.drawable.bg_checkbox);
			}else {
				mHolder.ivStatus.setImageResource(R.drawable.bg_checkbox_selected);
			}
		}
		
		return convertView;
	}

}
