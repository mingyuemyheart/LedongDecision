package com.cxwl.shawn.wuzhishan.decision.common;

import com.cxwl.shawn.wuzhishan.decision.R;

public class CONST {

    public static String APPID = "32";
    public static double DEFAULT_LAT = 19.20;
    public static double DEFAULT_LNG = 109.81;
    public static final String imageSuffix = ".png";//图标后缀名

    //本地保存用户信息参数
    public static String USERINFO = "userInfo";//userInfo sharedPreferance名称
    public static class UserInfo {
        public static final String uId = "uId";
        public static final String userName = "uName";
        public static final String passWord = "pwd";
    }

    public static String UID;//uid
    public static String USERNAME;//用户名
    public static String PASSWORD;//用户密码

    //下拉刷新progresBar四种颜色
    public static final int color1 = R.color.refresh_color1;
    public static final int color2 = R.color.refresh_color2;
    public static final int color3 = R.color.refresh_color3;
    public static final int color4 = R.color.refresh_color4;

    //预警颜色对应规则
    public static String[] blue = {"01", "_blue"};
    public static String[] yellow = {"02", "_yellow"};
    public static String[] orange = {"03", "_orange"};
    public static String[] red = {"04", "_red"};

    //showType类型，区分本地类或者图文
    public static final String DOCUMENT = "document";
    public static final String WARNING = "warning";

    public static final String COLUMN_ID = "column_id";//栏目id
    public static final String WEB_URL = "web_Url";//网页地址的标示
    public static final String ACTIVITY_NAME = "activity_name";//界面名称

}
