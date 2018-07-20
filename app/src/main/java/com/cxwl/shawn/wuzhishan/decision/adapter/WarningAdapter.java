package com.cxwl.shawn.wuzhishan.decision.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.dto.WarningDto;
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 预警信息
 */

public class WarningAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<WarningDto> mArrayList;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private boolean isMarkerCell;
	
	private final class ViewHolder {
		ImageView imageView;//预警icon
		TextView tvName;//预警信息名称
		TextView tvTime;//时间
	}
	
	private ViewHolder mHolder = null;
	
	public WarningAdapter(Context context, List<WarningDto> mArrayList, boolean isMarkerCell) {
		mContext = context;
		this.isMarkerCell = isMarkerCell;
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
			if (isMarkerCell == false) {
				convertView = mInflater.inflate(R.layout.adapter_warning, null);
			}else {
				convertView = mInflater.inflate(R.layout.adapter_warning_marker_info, null);
			}
			mHolder = new ViewHolder();
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			mHolder.tvTime = convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		WarningDto dto = mArrayList.get(position);
		
        Bitmap bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+dto.color+CONST.imageSuffix);
		if (bitmap == null) {
			bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+dto.color+CONST.imageSuffix);
		}
		mHolder.imageView.setImageBitmap(bitmap);
		
		if (!TextUtils.isEmpty(dto.name)) {
			if (isMarkerCell == false) {
				mHolder.tvName.setText(dto.name);
			}else {
				if (dto.name.contains("解除")) {
					mHolder.tvName.setText(dto.name.replace("解除", "解除"+"\n"));
				}else if (dto.name.contains("发布")) {
					mHolder.tvName.setText(dto.name.replace("发布", "发布"+"\n"));
				}
			}
		}
		
		try {
			mHolder.tvTime.setText(sdf2.format(sdf1.parse(dto.time)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}

}
