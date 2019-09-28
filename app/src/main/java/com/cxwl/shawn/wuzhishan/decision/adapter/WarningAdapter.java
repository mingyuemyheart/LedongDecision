package com.cxwl.shawn.wuzhishan.decision.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.activity.WarningDetailActivity;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.dto.WarningDto;
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil;

import java.util.List;

/**
 * 预警信息
 */
public class WarningAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<String> mArrayList;
	private List<WarningDto> dataList;

	private final class ViewHolder {
		TextView tvCity;
		LinearLayout llContainer;
	}
	
	public WarningAdapter(Context context, List<String> mArrayList, List<WarningDto> dataList) {
		mContext = context;
		this.mArrayList = mArrayList;
		this.dataList = dataList;
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
			mHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.adapter_warning, null);
			mHolder.llContainer = convertView.findViewById(R.id.llContainer);
			mHolder.tvCity = convertView.findViewById(R.id.tvCity);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		String name = mArrayList.get(position);
		mHolder.tvCity.setText(name);

		if (mHolder.llContainer != null) {
			mHolder.llContainer.removeAllViews();
			for (int i = 0; i < dataList.size(); i++) {
				final WarningDto data = dataList.get(i);
				if (TextUtils.equals(name, data.w2)) {
					ImageView imageView = new ImageView(mContext);
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)CommonUtil.dip2px(mContext, 30), (int)CommonUtil.dip2px(mContext, 30));
					params.rightMargin = 10;
					imageView.setLayoutParams(params);
					Bitmap bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+data.type+data.color+CONST.imageSuffix);
					if (bitmap == null) {
						bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+data.color+CONST.imageSuffix);
					}
					imageView.setImageBitmap(bitmap);
					mHolder.llContainer.addView(imageView);

					imageView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							Intent intent = new Intent(mContext, WarningDetailActivity.class);
							Bundle bundle = new Bundle();
							bundle.putParcelable("data", data);
							intent.putExtras(bundle);
							mContext.startActivity(intent);
						}
					});
				}
			}
		}

		return convertView;
	}

}
