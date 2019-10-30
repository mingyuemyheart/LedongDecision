package com.cxwl.shawn.wuzhishan.decision.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.dto.RiceRaiseDto;

import java.util.List;

/**
 * 生态气象-水稻长势
 */
public class RiceRaiseAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<RiceRaiseDto> mArrayList;

	private final class ViewHolder{
		TextView tvStationName,tvCropClass,tvCropName,tvCropType,tvCropMature,tvCropDev;
	}

	public RiceRaiseAdapter(Context context, List<RiceRaiseDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_rice_raise, null);
			mHolder = new ViewHolder();
			mHolder.tvStationName = convertView.findViewById(R.id.tvStationName);
			mHolder.tvCropClass = convertView.findViewById(R.id.tvCropClass);
			mHolder.tvCropName = convertView.findViewById(R.id.tvCropName);
			mHolder.tvCropType = convertView.findViewById(R.id.tvCropType);
			mHolder.tvCropMature = convertView.findViewById(R.id.tvCropMature);
			mHolder.tvCropDev = convertView.findViewById(R.id.tvCropDev);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		RiceRaiseDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.C_Stat_Name)) {
			mHolder.tvStationName.setText(dto.C_Stat_Name);
		}
		if (!TextUtils.isEmpty(dto.C_Crop)) {
			mHolder.tvCropClass.setText(dto.C_Crop);
		}
		if (!TextUtils.isEmpty(dto.C_CropName)) {
			mHolder.tvCropName.setText(dto.C_CropName);
		}
		if (!TextUtils.isEmpty(dto.C_CropVirteties)) {
			mHolder.tvCropType.setText(dto.C_CropVirteties);
		}
		if (!TextUtils.isEmpty(dto.C_CropMature)) {
			mHolder.tvCropMature.setText(dto.C_CropMature);
		}
		if (!TextUtils.isEmpty(dto.C_CorpDev)) {
			mHolder.tvCropDev.setText(dto.C_CorpDev);
		}

		return convertView;
	}

}
