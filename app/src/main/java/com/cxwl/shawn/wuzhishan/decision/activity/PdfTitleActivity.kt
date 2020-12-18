package com.cxwl.shawn.wuzhishan.decision.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.dto.ColumnData
import com.cxwl.shawn.wuzhishan.decision.fragment.*
import com.cxwl.shawn.wuzhishan.decision.util.CommonUtil
import kotlinx.android.synthetic.main.activity_pdf_title.*
import kotlinx.android.synthetic.main.layout_title.*

/**
 * 带有标签页的pdf文档界面
 * @author shawn_sun
 *
 */
class PdfTitleActivity : FragmentActivity(), OnClickListener {

	private val fragments : ArrayList<Fragment> = ArrayList()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_pdf_title)
		initWidget()
		initViewPager()
	}

	private fun initWidget() {
		llBack.setOnClickListener(this)

		val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
		if (!TextUtils.isEmpty(title)) {
			tvTitle.text = title
		}
	}
	
	/**
	 * 初始化viewPager
	 */
	private fun initViewPager() {
		if (intent.hasExtra("data")) {
			val data = intent.getParcelableExtra<ColumnData>("data")
			if (data != null) {
				if (TextUtils.equals(data.id, "678")) {//农情上报
					tvControl.visibility = View.VISIBLE
					tvControl.text = "历史查询"
					tvControl.setOnClickListener(this)
					tvControl.tag = "679"//农事记载
				}
				val columnList : ArrayList<ColumnData> = ArrayList(data.child)
				if (columnList.size == 0) {
					columnList.add(data)
				}
				if (columnList.size <= 1) {
					llContainer.visibility = View.GONE
					llContainer1.visibility = View.GONE
				}
				llContainer.removeAllViews()
				llContainer1.removeAllViews()
				val width = CommonUtil.widthPixels(this)
				for (i in 0 until columnList.size) {
					val dto = columnList[i]

					val tvName = TextView(this)
					tvName.gravity = Gravity.CENTER
					tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f)
					tvName.setPadding(0, CommonUtil.dip2px(this, 10.0f).toInt(), 0, CommonUtil.dip2px(this, 10.0f).toInt())
					tvName.setOnClickListener {
						if (viewPager != null) {
							viewPager.setCurrentItem(i, true)
						}
					}
					if (i == 0) {
						tvName.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
					}else {
						tvName.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
					}
					if (!TextUtils.isEmpty(dto.name)) {
						tvName.text = dto.name
					}
					tvName.tag = dto.id

					tvName.measure(0, 0)
					val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
					params.width = tvName.measuredWidth
					params.setMargins(CommonUtil.dip2px(this, 10f).toInt(), 0, CommonUtil.dip2px(this, 10f).toInt(), 0)
					tvName.layoutParams = params
					llContainer.addView(tvName, i)

					val tvBar = TextView(this)
					tvBar.gravity = Gravity.CENTER
					tvBar.setOnClickListener {
						if (viewPager != null) {
							viewPager.setCurrentItem(i, true)
						}
					}
					if (i == 0) {
						tvBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
					}else {
						tvBar.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
					}
					val params1 = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
					params1.width = tvName.measuredWidth
					params1.height = CommonUtil.dip2px(this, 2.0f).toInt()
					params1.setMargins(CommonUtil.dip2px(this, 10.0f).toInt(), 0, CommonUtil.dip2px(this, 10.0f).toInt(), 0)
					tvBar.layoutParams = params1
					llContainer1.addView(tvBar, i)

					var fragment : Fragment? = null
					if (TextUtils.equals(dto.id, "673")) {
						//生态气象-水稻长势
						fragment = RiceRaiseFragment()
					}else if (TextUtils.equals(dto.id, "674")) {
						//生态气象-作物适应性
						fragment = EcologicalCropFragment()
					}else if (TextUtils.equals(dto.id, "650")) {
						//全省预报-市县预报
						fragment = ProvinceFragment()
					}else if (TextUtils.equals(dto.id, "651")) {
						//全省预报-2小时降水预测
						fragment = MinuteFragment()
					}else if (TextUtils.equals(dto.id, "58601") || TextUtils.equals(dto.id, "58602")|| TextUtils.equals(dto.id, "58603") || TextUtils.equals(dto.id, "671")) {
						//灾害预警-病虫害气象条件预报、气象灾害、农气灾害
						//灾害监测-逐日监测
						fragment = DisasterMonitorDayFragment()
					}else if (TextUtils.equals(dto.id, "672")) {
						//灾害监测-逐周监测
						fragment = DisasterMonitorWeekFragment()
					}else if (TextUtils.equals(dto.id, "641") || TextUtils.equals(dto.id, "642") || TextUtils.equals(dto.id, "685")
							|| TextUtils.equals(dto.id, "609") || TextUtils.equals(dto.id, "610") || TextUtils.equals(dto.id, "611") || TextUtils.equals(dto.id, "639") || TextUtils.equals(dto.id, "629")) {
						//雷达图、卫星云图
						fragment = RadarFragment()
					}else if (TextUtils.equals(dto.id, "679")) {
						//农情上报-农事记载
						fragment = DisasterUploadFragment()
					}else if (TextUtils.equals(dto.id, "680")) {
						//农情上报-灾情上报
						fragment = DisasterUploadFragment()
					}else if (TextUtils.equals(dto.showType, CONST.DOCUMENT)) {
						fragment = PdfListFragment()
					} else if (TextUtils.equals(dto.showType, CONST.URL_DATA)) {
						fragment = WebviewFragment()
					}
					val bundle = Bundle()
					bundle.putString(CONST.WEB_URL, dto.dataUrl)
					bundle.putString(CONST.COLUMN_ID, dto.id)
					if (fragment != null) {
						fragment.arguments = bundle
						fragments.add(fragment)
					}
				}

				if (TextUtils.equals(data.id, "649")) {//全省预报
					viewPager.setSlipping(false)
				}else {
					viewPager.setSlipping(true)
				}
				viewPager.offscreenPageLimit = fragments.size
				viewPager.adapter = MyPagerAdapter(supportFragmentManager, fragments)
				viewPager.setOnPageChangeListener(object : OnPageChangeListener {
					override fun onPageScrollStateChanged(p0: Int) {
					}
					override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
					}
					override fun onPageSelected(arg0: Int) {
						if (llContainer != null) {
							for (i in 0 until llContainer.childCount) {
								val tvName = llContainer.getChildAt(i) as TextView
								if (i == arg0) {
									tvName.setTextColor(ContextCompat.getColor(this@PdfTitleActivity, R.color.colorPrimary))
									tvControl.tag = tvName.tag
								} else {
									tvName.setTextColor(ContextCompat.getColor(this@PdfTitleActivity, R.color.text_color3))
								}
							}
							if (llContainer.childCount > 4) {
								hScrollView1.smoothScrollTo(width / 4 * arg0, 0)
							}
						}

						if (llContainer1 != null) {
							for (i in 0 until llContainer1.childCount) {
								val tvBar = llContainer1.getChildAt(i) as TextView
								if (i == arg0) {
									tvBar.setBackgroundColor(ContextCompat.getColor(this@PdfTitleActivity, R.color.colorPrimary))
								} else {
									tvBar.setBackgroundColor(ContextCompat.getColor(this@PdfTitleActivity, R.color.transparent))
								}
							}
						}
					}
				})
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
	class MyPagerAdapter(fm: FragmentManager, fs : ArrayList<Fragment>) : FragmentStatePagerAdapter(fm) {

		private val fragments : ArrayList<Fragment> = fs

		init {
			notifyDataSetChanged()
		}

		override fun getCount(): Int {
			return fragments.size
		}

		override fun getItem(arg0: Int): Fragment {
			return fragments[arg0]
		}

		override fun getItemPosition(`object`: Any): Int {
			return PagerAdapter.POSITION_NONE
		}
	}

	override fun onClick(view: View?) {
		when(view!!.id) {
			R.id.llBack -> finish()
			R.id.tvControl -> {
				val intent = Intent(this, DisasterActivity::class.java)
				intent.putExtra(CONST.COLUMN_ID, tvControl.tag.toString())
				if (TextUtils.equals(tvControl.tag.toString(), "679")) {//农事记载
					intent.putExtra(CONST.ACTIVITY_NAME, "农事记载${tvControl.text}")
				} else if (TextUtils.equals(tvControl.tag.toString(), "680")) {//灾情上报
					intent.putExtra(CONST.ACTIVITY_NAME, "灾情上报${tvControl.text}")
				}
				startActivity(intent)
			}
		}
	}

}
