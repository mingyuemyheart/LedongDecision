package com.cxwl.shawn.wuzhishan.decision.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.dto.CityDto;

import java.util.List;

public class CityFragmentAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<CityDto> mArrayList;
	
	private final class ViewHolder{
		TextView tvName;
	}
	
	private ViewHolder mHolder = null;
	
	public CityFragmentAdapter(Context context, List<CityDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_city_nation, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		CityDto dto = mArrayList.get(position);

		if (TextUtils.equals(dto.cityId, "101310222")) {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
		}else {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.text_color3));
		}
		mHolder.tvName.setText(dto.disName);
		
		return convertView;
	}

}
