package com.cxwl.shawn.wuzhishan.decision.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 灾情反馈
 */
public class DisasterDto implements Parcelable {

    public String id, disasterName,disasterType;//对应预警类型名称,预警类型，如11B09
    public String title,aoiName,addr,content,time,imgUrl,imageName,latlon,miao;
    public boolean isSelected;
    public boolean isLastItem;//为了区分添加按钮
    public List<String> imgList = new ArrayList<>();//图片集合
    public String userName,mobile,uName,gzType,gzTime;
    public String fileType,filePath;//1图片、2视频、3音频、4文档、5文件夹
    public long fileSize;//文件大小
    public String ntAddr,ntName,ntType,ntEvent,ntBorn,ntDisaster,ntTime;//农田

    public DisasterDto() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.disasterName);
        dest.writeString(this.disasterType);
        dest.writeString(this.title);
        dest.writeString(this.aoiName);
        dest.writeString(this.addr);
        dest.writeString(this.content);
        dest.writeString(this.time);
        dest.writeString(this.imgUrl);
        dest.writeString(this.imageName);
        dest.writeString(this.latlon);
        dest.writeString(this.miao);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isLastItem ? (byte) 1 : (byte) 0);
        dest.writeStringList(this.imgList);
        dest.writeString(this.userName);
        dest.writeString(this.mobile);
        dest.writeString(this.uName);
        dest.writeString(this.gzType);
        dest.writeString(this.gzTime);
        dest.writeString(this.fileType);
        dest.writeString(this.filePath);
        dest.writeLong(this.fileSize);
        dest.writeString(this.ntAddr);
        dest.writeString(this.ntName);
        dest.writeString(this.ntType);
        dest.writeString(this.ntEvent);
        dest.writeString(this.ntBorn);
        dest.writeString(this.ntDisaster);
        dest.writeString(this.ntTime);
    }

    protected DisasterDto(Parcel in) {
        this.id = in.readString();
        this.disasterName = in.readString();
        this.disasterType = in.readString();
        this.title = in.readString();
        this.aoiName = in.readString();
        this.addr = in.readString();
        this.content = in.readString();
        this.time = in.readString();
        this.imgUrl = in.readString();
        this.imageName = in.readString();
        this.latlon = in.readString();
        this.miao = in.readString();
        this.isSelected = in.readByte() != 0;
        this.isLastItem = in.readByte() != 0;
        this.imgList = in.createStringArrayList();
        this.userName = in.readString();
        this.mobile = in.readString();
        this.uName = in.readString();
        this.gzType = in.readString();
        this.gzTime = in.readString();
        this.fileType = in.readString();
        this.filePath = in.readString();
        this.fileSize = in.readLong();
        this.ntAddr = in.readString();
        this.ntName = in.readString();
        this.ntType = in.readString();
        this.ntEvent = in.readString();
        this.ntBorn = in.readString();
        this.ntDisaster = in.readString();
        this.ntTime = in.readString();
    }

    public static final Creator<DisasterDto> CREATOR = new Creator<DisasterDto>() {
        @Override
        public DisasterDto createFromParcel(Parcel source) {
            return new DisasterDto(source);
        }

        @Override
        public DisasterDto[] newArray(int size) {
            return new DisasterDto[size];
        }
    };
}
