package com.cxwl.shawn.wuzhishan.decision.common;

import android.app.Activity;
import android.app.Application;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MyApplication extends Application {

	private static Map<String,Activity> destoryMap = new HashMap<>();

	@Override
	public void onCreate() {
		super.onCreate();
	}

	/**
	 * 添加到销毁队列
	 * @param activity 要销毁的activity
	 */
	public static void addDestoryActivity(Activity activity,String activityName) {
		destoryMap.put(activityName,activity);
	}

	/**
	 *销毁指定Activity
	 */
	public static void destoryActivity() {
		Set<String> keySet=destoryMap.keySet();
		for (String key:keySet){
			destoryMap.get(key).finish();
		}
	}

}
