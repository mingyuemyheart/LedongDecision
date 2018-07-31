package com.cxwl.shawn.wuzhishan.decision.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.dto.WeatherDto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * 15天预报
 */
public class WeeklyForecastAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<WeatherDto> mArrayList;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd", Locale.CHINA);
	public long foreTime, currentTime;
	
	private final class ViewHolder{
		TextView tvWeek,tvDate,tvHighPhe,tvLowPhe,tvHighTemp,tvLowTemp;
		ImageView ivHighPhe,ivLowPhe;
	}
	
	public WeeklyForecastAdapter(Context context, List<WeatherDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_forecast, null);
			mHolder = new ViewHolder();
			mHolder.tvWeek = convertView.findViewById(R.id.tvWeek);
			mHolder.tvDate = convertView.findViewById(R.id.tvDate);
			mHolder.tvHighPhe = convertView.findViewById(R.id.tvHighPhe);
			mHolder.ivHighPhe = convertView.findViewById(R.id.ivHighPhe);
			mHolder.tvHighTemp = convertView.findViewById(R.id.tvHighTemp);
			mHolder.tvLowPhe = convertView.findViewById(R.id.tvLowPhe);
			mHolder.ivLowPhe = convertView.findViewById(R.id.ivLowPhe);
			mHolder.tvLowTemp = convertView.findViewById(R.id.tvLowTemp);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		WeatherDto dto = mArrayList.get(position);

		String week;
		if (currentTime > foreTime) {
			if (position == 0) {
				week = "昨天";
			}else if (position == 1) {
				week = "今天";
			}else if (position == 2) {
				week = "明天";
			}else {
				week = dto.week;
			}
		}else {
			if (position == 0) {
				week = "今天";
			}else if (position == 1) {
				week = "明天";
			}else {
				week = dto.week;
			}
		}
		mHolder.tvWeek.setText(week);

		try {
			mHolder.tvDate.setText(sdf2.format(sdf1.parse(dto.date)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		mHolder.tvLowPhe.setText(dto.lowPhe);
		mHolder.tvLowTemp.setText(dto.lowTemp+"℃");
		Drawable ld = mContext.getResources().getDrawable(R.drawable.phenomenon_drawable_night);
		ld.setLevel(dto.lowPheCode);
		mHolder.ivLowPhe.setBackground(ld);
		
		mHolder.tvHighPhe.setText(dto.highPhe);
		mHolder.tvHighTemp.setText(dto.highTemp+"℃");
		Drawable hd = mContext.getResources().getDrawable(R.drawable.phenomenon_drawable);
		hd.setLevel(dto.highPheCode);
		mHolder.ivHighPhe.setBackground(hd);
		
		return convertView;
	}

}
