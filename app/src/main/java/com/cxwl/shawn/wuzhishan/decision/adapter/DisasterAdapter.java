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
import com.cxwl.shawn.wuzhishan.decision.dto.DisasterDto;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * 我上传的灾情反馈
 */
public class DisasterAdapter extends BaseAdapter {

	private String columnId;
	private LayoutInflater mInflater;
	private List<DisasterDto> mArrayList;

	private final class ViewHolder{
		ImageView imageView;
		TextView tvAddr,tvName,tvType,tvEvent,tvBorn,tvDisaster,tvTime;
	}

	public DisasterAdapter(Context context, List<DisasterDto> mArrayList, String columnId) {
		this.columnId = columnId;
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
			convertView = mInflater.inflate(R.layout.adapter_disaster, null);
			mHolder = new ViewHolder();
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.tvAddr = convertView.findViewById(R.id.tvAddr);
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			mHolder.tvType = convertView.findViewById(R.id.tvType);
			mHolder.tvEvent = convertView.findViewById(R.id.tvEvent);
			mHolder.tvBorn = convertView.findViewById(R.id.tvBorn);
			mHolder.tvDisaster = convertView.findViewById(R.id.tvDisaster);
			mHolder.tvTime = convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		DisasterDto dto = mArrayList.get(position);

		if (dto.imgList.size() > 0) {
			String imgUrl = dto.imgList.get(0);
			if (!TextUtils.isEmpty(imgUrl)) {
				Picasso.get().load(imgUrl).error(R.drawable.icon_no_pic).into(mHolder.imageView);
			} else {
				mHolder.imageView.setImageResource(R.drawable.icon_no_pic);
			}
		} else {
			mHolder.imageView.setImageResource(R.drawable.icon_no_pic);
		}

		mHolder.tvAddr.setText(!TextUtils.isEmpty(dto.ntAddr) ? "地址位置："+dto.ntAddr : "地址位置：");
		mHolder.tvName.setText(!TextUtils.isEmpty(dto.ntName) ? "农田名称："+dto.ntName : "农田名称：");
		mHolder.tvType.setText(!TextUtils.isEmpty(dto.ntType) ? "作物类型："+dto.ntType : "作物类型：");
		mHolder.tvEvent.setText(!TextUtils.isEmpty(dto.ntEvent) ? "农事活动："+dto.ntEvent : "农事活动：");
		mHolder.tvBorn.setText(!TextUtils.isEmpty(dto.ntBorn) ? "作物生育期："+dto.ntBorn : "作物生育期：");
		mHolder.tvDisaster.setText(!TextUtils.isEmpty(dto.ntDisaster) ? "灾害类型："+dto.ntDisaster : "灾害类型：");
		mHolder.tvTime.setText(!TextUtils.isEmpty(dto.time) ? "记载时间："+dto.time : "记载时间：");

		if (TextUtils.equals(columnId, "679")) {//农事记载
			mHolder.tvEvent.setVisibility(View.VISIBLE);
			mHolder.tvBorn.setVisibility(View.GONE);
			mHolder.tvDisaster.setVisibility(View.GONE);
		} else if (TextUtils.equals(columnId, "680")) {//灾情上报
			mHolder.tvEvent.setVisibility(View.GONE);
			mHolder.tvBorn.setVisibility(View.VISIBLE);
			mHolder.tvDisaster.setVisibility(View.VISIBLE);
		}

		return convertView;
	}

}
