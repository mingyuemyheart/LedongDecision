package com.cxwl.shawn.wuzhishan.decision.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.dto.CityDto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;

public class CommonUtil {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static float dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dpValue * scale;
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static float px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return pxValue / scale;
    }

    public static int widthPixels(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int heightPixels(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     * @param context
     * @return true 表示开启
     */
    public static boolean isLocationOpen(final Context context) {
        LocationManager locationManager  = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public static int statusBarHeight(Context context) {
        int statusBarHeight = -1;//状态栏高度
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * 获取底部导航栏高度
     * @param context
     * @return
     */
    public static int navigationBarHeight(Context context) {
        int navigationBarHeight = -1;//状态栏高度
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            navigationBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return navigationBarHeight;
    }

    /**
     * 从Assets中读取图片
     */
    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 读取assets下文件
     * @param fileName
     * @return
     */
    public static String getFromAssets(Context context, String fileName) {
        String Result = "";
        try {
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result;
    }

    /**
     * 回执区域
     * @param context
     * @param aMap
     */
    public static void drawAllDistrict(Context context, AMap aMap, List<Polyline> polyLineList) {
        if (aMap == null) {
            return;
        }
        String result = CommonUtil.getFromAssets(context, "json/hnGeo.json");
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject obj = new JSONObject(result);
                if (!obj.isNull("polyline")) {
                    String[] polylines = obj.getString("polyline").split("\\|");
                    for (int i = 0; i < polylines.length; i++) {
                        PolylineOptions polylineOption = new PolylineOptions();
                        polylineOption.width(2).color(0xff999999);
                        String[] array = polylines[i].split(";");
                        for (int j = 0; j < array.length; j++) {
                            String[] latLng = array[j].split(",");
                            double lng = Double.valueOf(latLng[0]);
                            double lat = Double.valueOf(latLng[1]);
                            polylineOption.add(new LatLng(lat, lng));
                            polylineOption.zIndex(1000);
                        }
                        Polyline polyLine = aMap.addPolyline(polylineOption);
                        polyLineList.add(polyLine);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 回执区域
     * @param context
     * @param aMap
     */
    public static void drawWarningDistrict(Context context, AMap aMap, String cityName, int color) {
        if (aMap == null) {
            return;
        }
        String result = CommonUtil.getFromAssets(context, "json/hai_nan.geo.json");
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject obj = new JSONObject(result);
                JSONArray array = obj.getJSONArray("features");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject itemObj = array.getJSONObject(i);

                    JSONObject properties = itemObj.getJSONObject("properties");
                    String name = properties.getString("name");
                    if (TextUtils.equals(cityName, name)) {
//						JSONArray cp = properties.getJSONArray("cp");
//						for (int m = 0; m < cp.length(); m++) {
//							double lat = cp.getDouble(1);
//							double lng = cp.getDouble(0);
//
//							LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//							View view = inflater.inflate(R.layout.rainfall_fact_marker_view2, null);
//							TextView tvName = (TextView) view.findViewById(R.id.tvName);
//							if (!TextUtils.isEmpty(name)) {
//								tvName.setText(name);
//							}
//							MarkerOptions options = new MarkerOptions();
//							options.anchor(0.5f, 0.5f);
//							options.position(new LatLng(lat, lng));
//							options.icon(BitmapDescriptorFactory.fromView(view));
//							aMap.addMarker(options);
//						}

                        JSONObject geometry = itemObj.getJSONObject("geometry");
                        JSONArray coordinates = geometry.getJSONArray("coordinates");
                        for (int m = 0; m < coordinates.length(); m++) {
                            JSONArray array2 = coordinates.getJSONArray(m);
                            PolygonOptions polylineOption = new PolygonOptions();
                            polylineOption.fillColor(color);
                            polylineOption.strokeColor(color).strokeWidth(6);
                            for (int j = 0; j < array2.length(); j++) {
                                JSONArray itemArray = array2.getJSONArray(j);
                                double lng = itemArray.getDouble(0);
                                double lat = itemArray.getDouble(1);
                                polylineOption.add(new LatLng(lat, lng));
                            }
                            aMap.addPolygon(polylineOption);
                        }
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据当前时间获取日期，格式为MM/dd
     * @param i (+1为后一天，-1为前一天，0表示当天)
     * @return
     */
    public static String getDate(int i) {
        String date = null;

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day+i);

        if (c.get(Calendar.MONTH) == 0) {
            date = "01";
        }else if (c.get(Calendar.MONTH) == 1) {
            date = "02";
        }else if (c.get(Calendar.MONTH) == 2) {
            date = "03";
        }else if (c.get(Calendar.MONTH) == 3) {
            date = "04";
        }else if (c.get(Calendar.MONTH) == 4) {
            date = "05";
        }else if (c.get(Calendar.MONTH) == 5) {
            date = "06";
        }else if (c.get(Calendar.MONTH) == 6) {
            date = "07";
        }else if (c.get(Calendar.MONTH) == 7) {
            date = "08";
        }else if (c.get(Calendar.MONTH) == 8) {
            date = "09";
        }else if (c.get(Calendar.MONTH) == 9) {
            date = "10";
        }else if (c.get(Calendar.MONTH) == 10) {
            date = "11";
        }else if (c.get(Calendar.MONTH) == 11) {
            date = "12";
        }

        return date+"/"+c.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 根据当前时间获取星期几
     * @param context
     * @param i (+1为后一天，-1为前一天，0表示当天)
     * @return
     */
    public static String getWeek(Context context, int i) {
        String week = null;

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day+i);

        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            week = "周日";
        }else if (c.get(Calendar.DAY_OF_WEEK) == 2) {
            week = "周一";
        }else if (c.get(Calendar.DAY_OF_WEEK) == 3) {
            week = "周二";
        }else if (c.get(Calendar.DAY_OF_WEEK) == 4) {
            week = "周三";
        }else if (c.get(Calendar.DAY_OF_WEEK) == 5) {
            week = "周四";
        }else if (c.get(Calendar.DAY_OF_WEEK) == 6) {
            week = "周五";
        }else if (c.get(Calendar.DAY_OF_WEEK) == 7) {
            week = "周六";
        }

        return week;
    }

}
