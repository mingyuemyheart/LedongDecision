package com.cxwl.shawn.wuzhishan.decision.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;

import java.util.List;

/**
 * 灾害监测-灾害种类
 */
public class DisasterMonitorAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<String> mArrayList;

	private final class ViewHolder{
		TextView tvTitle;
	}

	public DisasterMonitorAdapter(Context context, List<String> mArrayList) {
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			convertView = mInflater.inflate(R.layout.adapter_disaster_monitor, null);
			mHolder = new ViewHolder();
			mHolder.tvTitle = convertView.findViewById(R.id.tvTitle);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		String title = mArrayList.get(position);
		if (!TextUtils.isEmpty(title)) {
			mHolder.tvTitle.setText(title);
		}

		return convertView;
	}

}
