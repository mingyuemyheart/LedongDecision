package com.cxwl.shawn.wuzhishan.decision.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.dto.DisasterDto;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * 选择图片
 */
public class ShawnSelectPictureAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater mInflater;
	private List<DisasterDto> mArrayList;
	private RelativeLayout.LayoutParams params;

	private final class ViewHolder{
		ImageView imageView,imageView1;
		RelativeLayout reBg;
	}

	public ShawnSelectPictureAdapter(Context context, List<DisasterDto> mArrayList) {
		this.context = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		params = new RelativeLayout.LayoutParams(width/4, width/4);
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_select_picture, null);
			mHolder = new ViewHolder();
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.imageView1 = convertView.findViewById(R.id.imageView1);
			mHolder.reBg = convertView.findViewById(R.id.reBg);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		DisasterDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.imgUrl)) {
			File file = new File(dto.imgUrl);
			if (file.exists()) {
				Picasso.get().load(file).centerCrop().resize(200, 200).into(mHolder.imageView);
				mHolder.imageView.setLayoutParams(params);
			}
		}
		
		if (dto.isSelected) {
			mHolder.imageView1.setImageResource(R.drawable.shawn_bg_checkbox_selected);
			mHolder.reBg.setBackgroundColor(0x60000000);
		}else {
			mHolder.imageView1.setImageResource(R.drawable.shawn_bg_checkbox);
			mHolder.reBg.setBackgroundColor(Color.TRANSPARENT);
		}
		mHolder.reBg.setLayoutParams(params);
		
		return convertView;
	}

}
