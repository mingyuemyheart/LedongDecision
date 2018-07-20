package com.cxwl.shawn.wuzhishan.decision.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cxwl.shawn.wuzhishan.decision.R;
import com.cxwl.shawn.wuzhishan.decision.common.CONST;
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData;
import com.cxwl.shawn.wuzhishan.decision.fragment.PdfListFragment;
import com.cxwl.shawn.wuzhishan.decision.view.MainViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * 带有标签页的pdf文档界面
 * @author shawn_sun
 *
 */

public class PdfTitleActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private LinearLayout llContainer = null;
	private LinearLayout llContainer1 = null;
	private MainViewPager viewPager = null;
	private List<Fragment> fragments = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pdf_title);
		mContext = this;
		initWidget();
		initViewPager();
	}
	
	private void initWidget() {
		llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = findViewById(R.id.tvTitle);
		llContainer = findViewById(R.id.llContainer);
		llContainer1 = findViewById(R.id.llContainer1);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}

	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		if (getIntent().hasExtra("data")) {
			ColumnData data = getIntent().getParcelableExtra("data");
			if (data != null) {
				List<ColumnData> columnList = new ArrayList<>();
				columnList.addAll(data.child);
				int columnSize = columnList.size();
				if (columnSize <= 1) {
					llContainer.setVisibility(View.GONE);
					llContainer1.setVisibility(View.GONE);
				}
				DisplayMetrics dm = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(dm);
				llContainer.removeAllViews();
				llContainer1.removeAllViews();
				for (int i = 0; i < columnSize; i++) {
					ColumnData dto = columnList.get(i);

					TextView tvName = new TextView(mContext);
					tvName.setGravity(Gravity.CENTER);
					tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
					tvName.setPadding(0, (int)(dm.density*10), 0, (int)(dm.density*10));
					tvName.setOnClickListener(new MyOnClickListener(i));
					if (i == 0) {
						tvName.setTextColor(getResources().getColor(R.color.colorPrimary));
					}else {
						tvName.setTextColor(getResources().getColor(R.color.text_color3));
					}
					if (!TextUtils.isEmpty(dto.name)) {
						tvName.setText(dto.name);
					}
					LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					params.weight = 1.0f;
					if (columnSize == 1) {
						params.width = dm.widthPixels;
					}else if (columnSize == 2) {
						params.width = dm.widthPixels/2;
					}else if (columnSize == 3) {
						params.width = dm.widthPixels/3;
					}else {
						params.width = dm.widthPixels/4;
					}
					tvName.setLayoutParams(params);
					llContainer.addView(tvName, i);

					TextView tvBar = new TextView(mContext);
					tvBar.setGravity(Gravity.CENTER);
					tvBar.setOnClickListener(new MyOnClickListener(i));
					if (i == 0) {
						tvBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
					}else {
						tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
					}
					LayoutParams params1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					params1.weight = 1.0f;
					if (columnSize == 1) {
						params1.width = dm.widthPixels;
					}else if (columnSize == 2) {
						params1.width = dm.widthPixels/2-(int)(dm.density*20);
					}else if (columnSize == 3) {
						params1.width = dm.widthPixels/3-(int)(dm.density*20);
					}else {
						params1.width = dm.widthPixels/4-(int)(dm.density*20);
					}
					params1.height = (int) (dm.density*2);
					params1.setMargins((int)(dm.density*10), 0, (int)(dm.density*10), 0);
					tvBar.setLayoutParams(params1);
					llContainer1.addView(tvBar, i);

					PdfListFragment fragment = new PdfListFragment();
					Bundle bundle = new Bundle();
					bundle.putString(CONST.WEB_URL, dto.dataUrl);
					fragment.setArguments(bundle);
					fragments.add(fragment);
				}

				viewPager = findViewById(R.id.viewPager);
				viewPager.setSlipping(true);//设置ViewPager是否可以滑动
				viewPager.setOffscreenPageLimit(fragments.size());
				viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
				viewPager.setAdapter(new MyPagerAdapter());
			}
		}
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			if (llContainer != null) {
				for (int i = 0; i < llContainer.getChildCount(); i++) {
					TextView tvName = (TextView) llContainer.getChildAt(i);
					if (i == arg0) {
						tvName.setTextColor(getResources().getColor(R.color.colorPrimary));
					}else {
						tvName.setTextColor(getResources().getColor(R.color.text_color3));
					}
				}
			}
			
			if (llContainer1 != null) {
				for (int i = 0; i < llContainer1.getChildCount(); i++) {
					TextView tvBar = (TextView) llContainer1.getChildAt(i);
					if (i == arg0) {
						tvBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
					}else {
						tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
					}
				}
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}
	
	/**
	 * 头标点击监听
	 * @author shawn_sun
	 */
	private class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			if (viewPager != null) {
				viewPager.setCurrentItem(index, true);
			}
		}
	}
	
	/**
	 * @ClassName: MyPagerAdapter
	 * @Description: TODO填充ViewPager的数据适配器
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:37:47
	 *
	 */
	private class MyPagerAdapter extends PagerAdapter {
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(fragments.get(position).getView());
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = fragments.get(position);
			if (!fragment.isAdded()) { // 如果fragment还没有added
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.add(fragment, fragment.getClass().getSimpleName());
				ft.commit();
				/**
				 * 在用FragmentTransaction.commit()方法提交FragmentTransaction对象后
				 * 会在进程的主线程中,用异步的方式来执行。
				 * 如果想要立即执行这个等待中的操作,就要调用这个方法(只能在主线程中调用)。
				 * 要注意的是,所有的回调和相关的行为都会在这个调用中被执行完成,因此要仔细确认这个方法的调用位置。
				 */
				getFragmentManager().executePendingTransactions();
			}

			if (fragment.getView().getParent() == null) {
				container.addView(fragment.getView()); // 为viewpager增加布局
			}
			return fragment.getView();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}

}
