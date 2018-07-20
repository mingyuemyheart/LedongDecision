package com.cxwl.shawn.wuzhishan.decision.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;


public class ShawnRainDto implements Parcelable {

	public String id;
	public String dataUrl;
	public String timeString;
	public String timeParams;
	public String stationCode;
	public String stationName;
	public String area;
	public double val;
	public String cityName;
	public double lng;
	public double lat;
	public String title;
	public String icon1, icon2;
	public List<ShawnRainDto> itemList = new ArrayList<ShawnRainDto>();
	
	public String rainLevel;
	public String count;
	public List<ShawnRainDto> areaList = new ArrayList<ShawnRainDto>();//地图下方列表
	
	public float factRain;//实况降水
	public float factTemp;//实况温度
	public float factWind;//实况风速
	public String factTime;
	public float x = 0;
	public float y = 0;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.id);
		dest.writeString(this.dataUrl);
		dest.writeString(this.timeString);
		dest.writeString(this.timeParams);
		dest.writeString(this.stationCode);
		dest.writeString(this.stationName);
		dest.writeString(this.area);
		dest.writeDouble(this.val);
		dest.writeString(this.cityName);
		dest.writeDouble(this.lng);
		dest.writeDouble(this.lat);
		dest.writeString(this.title);
		dest.writeString(this.icon1);
		dest.writeString(this.icon2);
		dest.writeTypedList(this.itemList);
		dest.writeString(this.rainLevel);
		dest.writeString(this.count);
		dest.writeTypedList(this.areaList);
		dest.writeFloat(this.factRain);
		dest.writeFloat(this.factTemp);
		dest.writeFloat(this.factWind);
		dest.writeString(this.factTime);
		dest.writeFloat(this.x);
		dest.writeFloat(this.y);
	}

	public ShawnRainDto() {
	}

	protected ShawnRainDto(Parcel in) {
		this.id = in.readString();
		this.dataUrl = in.readString();
		this.timeString = in.readString();
		this.timeParams = in.readString();
		this.stationCode = in.readString();
		this.stationName = in.readString();
		this.area = in.readString();
		this.val = in.readDouble();
		this.cityName = in.readString();
		this.lng = in.readDouble();
		this.lat = in.readDouble();
		this.title = in.readString();
		this.icon1 = in.readString();
		this.icon2 = in.readString();
		this.itemList = in.createTypedArrayList(ShawnRainDto.CREATOR);
		this.rainLevel = in.readString();
		this.count = in.readString();
		this.areaList = in.createTypedArrayList(ShawnRainDto.CREATOR);
		this.factRain = in.readFloat();
		this.factTemp = in.readFloat();
		this.factWind = in.readFloat();
		this.factTime = in.readString();
		this.x = in.readFloat();
		this.y = in.readFloat();
	}

	public static final Creator<ShawnRainDto> CREATOR = new Creator<ShawnRainDto>() {
		@Override
		public ShawnRainDto createFromParcel(Parcel source) {
			return new ShawnRainDto(source);
		}

		@Override
		public ShawnRainDto[] newArray(int size) {
			return new ShawnRainDto[size];
		}
	};
}
