package com.cxwl.shawn.wuzhishan.decision.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * 主界面
 */
public class MainAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<ColumnData> mArrayList;
	public int height;
	
	private final class ViewHolder{
		TextView tvName;
		ImageView icon;
	}
	
	public MainAdapter(Context context, List<ColumnData> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_main, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			mHolder.icon = convertView.findViewById(R.id.icon);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		try {
			ColumnData dto = mArrayList.get(position);

			if (!TextUtils.isEmpty(dto.name)) {
				mHolder.tvName.setText(dto.name);
			}

			if (!TextUtils.isEmpty(dto.icon)) {
				Picasso.with(mContext).load(dto.icon).into(mHolder.icon);
			}

			if (height > 0) {
				AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, height/3);
				convertView.setLayoutParams(params);
				notifyDataSetChanged();
			}

		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return convertView;
	}

}
