package com.cxwl.shawn.wuzhishan.decision.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.dto.DisasterDto;
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * 灾情反馈
 */
public class DisasterUploadAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater mInflater;
	private List<DisasterDto> mArrayList;
	private RelativeLayout.LayoutParams params;

	private final class ViewHolder{
		ImageView imageView,ivDelete;
	}

	public DisasterUploadAdapter(Context context, List<DisasterDto> mArrayList) {
		this.context = context;
		int itemWidth = (CommonUtil.widthPixels(context) - (int)CommonUtil.dip2px(context, 24f)) / 3;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		params = new RelativeLayout.LayoutParams(itemWidth, itemWidth);
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder mHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_disaster_upload, null);
			mHolder = new ViewHolder();
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.ivDelete = convertView.findViewById(R.id.ivDelete);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		DisasterDto dto = mArrayList.get(position);

		if (!dto.isLastItem) {
			if (!TextUtils.isEmpty(dto.imgUrl)) {
				File file = new File(dto.imgUrl);
				if (file.exists()) {
					Picasso.get().load(file).centerCrop().resize(200, 200).into(mHolder.imageView);
					mHolder.imageView.setPadding(0,0,0,0);
					mHolder.imageView.setLayoutParams(params);
				}
			}
			mHolder.ivDelete.setVisibility(View.VISIBLE);
			mHolder.ivDelete.setTag(position);
		}else {
			mHolder.imageView.setImageResource(R.drawable.shawn_icon_plus);
			mHolder.imageView.setLayoutParams(params);
			mHolder.ivDelete.setVisibility(View.INVISIBLE);
		}

		mHolder.ivDelete.setOnClickListener(v -> {
			mArrayList.remove(position);
			boolean isLastItem = false;
			for (int i = 0; i < mArrayList.size(); i++) {
				DisasterDto d = mArrayList.get(i);
				if (d.isLastItem) {
					isLastItem = true;
				}
			}
			if (!isLastItem) {
				DisasterDto data = new DisasterDto();
				data.isLastItem = true;
				mArrayList.add(data);
			}
			notifyDataSetChanged();
		});

		return convertView;
	}

}
