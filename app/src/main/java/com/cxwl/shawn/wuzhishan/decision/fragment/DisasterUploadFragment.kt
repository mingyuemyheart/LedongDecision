package com.cxwl.shawn.wuzhishan.decision.fragment

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.Toast
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.cxwl.shawn.wuzhishan.decision.R
import com.cxwl.shawn.wuzhishan.decision.activity.SelectPictureActivity
import com.cxwl.shawn.wuzhishan.decision.adapter.DisasterUploadAdapter
import com.cxwl.shawn.wuzhishan.decision.common.CONST
import com.cxwl.shawn.wuzhishan.decision.dto.DisasterDto
import com.cxwl.shawn.wuzhishan.decision.util.AuthorityUtil
import com.cxwl.shawn.wuzhishan.decision.util.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_disaster_upload.*
import kotlinx.android.synthetic.main.layout_date.*
import kotlinx.android.synthetic.main.shawn_dialog_camera.view.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request.Builder
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONException
import org.json.JSONObject
import uk.co.senab.photoview.PhotoView
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener
import wheelview.NumericWheelAdapter
import wheelview.OnWheelScrollListener
import wheelview.WheelView
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 灾情反馈
 */
class DisasterUploadFragment : BaseFragment(), OnClickListener, AMapLocationListener {

    private var mAdapter: DisasterUploadAdapter? = null
    private val dataList: MutableList<DisasterDto> = ArrayList()
    private val sdf1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private var columnId = "679"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_disaster_upload, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
        initWheelView()
    }

    private fun initWidget() {
        tvSubmit.setOnClickListener(this)
        tvNegtive.setOnClickListener(this)
        tvPositive.setOnClickListener(this)
        tvTime.setOnClickListener(this)

        columnId = arguments!!.getString(CONST.COLUMN_ID)
        if (TextUtils.equals(columnId, "679")) {//农事记载
            bornStr.visibility = View.GONE
            etBorn.visibility = View.GONE
            divider4.visibility = View.GONE
            disasterStr.visibility = View.GONE
            etDisaster.visibility = View.GONE
            divider5.visibility = View.GONE
        } else if (TextUtils.equals(columnId, "680")) {//灾情上报
            eventStr.visibility = View.GONE
            etEvent.visibility = View.GONE
            divider3.visibility = View.GONE
        }

        tvTime.text = sdf1.format(Date())
        startLocation()
        initGridView()
    }

    /**
     * 开始定位
     */
    private fun startLocation() {
        val mLocationOption = AMapLocationClientOption() //初始化定位参数
        val mLocationClient = AMapLocationClient(activity) //初始化定位
        mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.isNeedAddress = true //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.isOnceLocation = true //设置是否只定位一次,默认为false
        mLocationOption.isMockEnable = false //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.interval = 2000 //设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption) //给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this)
        mLocationClient.startLocation() //启动定位
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (amapLocation != null && amapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
            tvAddr.text = if (amapLocation.province.contains(amapLocation.city)) {
                amapLocation.city + amapLocation.district + amapLocation.street + amapLocation.aoiName
            } else {
                amapLocation.province + amapLocation.city + amapLocation.district + amapLocation.street + amapLocation.aoiName
            }
        }
    }

    private fun addLastElement() {
        val dto = DisasterDto()
        dto.isLastItem = true
        dataList.add(dto)
    }

    private fun initGridView() {
        addLastElement()
        mAdapter = DisasterUploadAdapter(activity, dataList)
        gridView.adapter = mAdapter
        gridView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val data = dataList[position]
            if (data.isLastItem) { //点击添加按钮
                selectCamera()
            } else {
                val imgList: ArrayList<String> = ArrayList()
                for (i in dataList.indices) {
                    val d = dataList[i]
                    if (!d.isLastItem) {
                        imgList.add(d.imgUrl)
                    }
                }
                initViewPager(position, imgList)
                if (clViewPager!!.visibility == View.GONE) {
                    scaleExpandAnimation(clViewPager)
                    clViewPager!!.visibility = View.VISIBLE
                    tvCount.text = (position + 1).toString() + "/" + imgList.size
                }
            }
        }
    }

    /**
     * 上传图片
     */
    private fun okHttpPostImgs() {
        showDialog()
        val url = "http://59.50.130.88:8888/decision-admin/ny/filesUpload"
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        if (dataList.size > 1) {
            for (i in dataList.indices) {
                val dto = dataList[i]
                if (!TextUtils.isEmpty(dto.imgUrl) && !dto.isLastItem) {
                    val imgFile = File(compressBitmap(dto.imgUrl))
                    if (imgFile.exists()) {
                        builder.addFormDataPart("myfiles", imgFile.name, imgFile.asRequestBody("image/*".toMediaTypeOrNull()))
                    }
                }
            }
        }
        val body: RequestBody = builder.build()
        Thread(Runnable {
            OkHttpUtil.enqueue(Builder().url(url).post(body).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    okHttpPost("")
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    activity!!.runOnUiThread {
                        try {
                            val obj = JSONObject(result)
                            if (!obj.isNull("data")) {
                                val imgs = obj.getString("data")
                                okHttpPost(imgs)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        }).start()
    }

    /**
     * 灾情反馈
     */
    private fun okHttpPost(imgs: String) {
        showDialog()
        var url = String.format("http://59.50.130.88:8888/decision-admin/ny/addnsjz?" +
                "wz=%s&ntname=%s&zwtype=%s&nshd=%s&tpurl=%s&jltime=%s", tvAddr.text, etName.text, etType.text, etEvent.text, imgs, tvTime.text)
        if (TextUtils.equals(columnId, "679")) {//农事记载
            url = String.format("http://59.50.130.88:8888/decision-admin/ny/addnsjz?" +
                    "wz=%s&ntname=%s&zwtype=%s&nshd=%s&tpurl=%s&jltime=%s", tvAddr.text, etName.text, etType.text, etEvent.text, imgs, tvTime.text)
        } else if (TextUtils.equals(columnId, "680")) {//灾情上报
            url = String.format("http://59.50.130.88:8888/decision-admin/ny/addzqsb?" +
                    "id=123&wz=%s&ntname=%s&zwtype=%s&zwsyq=%s&zhlx=%s&tpurl=%s&jltime=%s", tvAddr.text, etName.text, etType.text, etBorn.text, etDisaster.text, imgs, tvTime.text)
        }
        Thread(Runnable {
            OkHttpUtil.enqueue(Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    activity!!.runOnUiThread {
                        cancelDialog()
                        try {
                            val obj = JSONObject(result)
                            if (!obj.isNull("code")) {
                                val code = obj.getString("code")
                                if (TextUtils.equals(code, "200")) {
                                    Toast.makeText(activity, "提交成功！", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        }).start()
    }

    private fun compressBitmap(imgPath: String): String? {
        var newPath: String? = null
        try {
            val files = File(CONST.SDCARD_PATH)
            if (!files.exists()) {
                files.mkdirs()
            }
            val bitmap = getSmallBitmap(imgPath)
            newPath = files.absolutePath + "/" + System.currentTimeMillis() + ".jpg"
            val fos = FileOutputStream(newPath)
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return newPath
    }

    /**
     * 根据路径获得图片信息并按比例压缩，返回bitmap
     */
    private fun getSmallBitmap(filePath: String?): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true //只解析图片边沿，获取宽高
        BitmapFactory.decodeFile(filePath, options)
        // 计算缩放比
        options.inSampleSize = calculateInSampleSize(options, 720, 1080)
        // 完整解析图片返回bitmap
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(filePath, options)
    }


    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvTime, R.id.tvNegtive -> bootTimeLayoutAnimation()
            R.id.tvPositive -> {
                bootTimeLayoutAnimation()
                setTextViewValue()
            }
            R.id.tvSubmit -> {
                if (TextUtils.isEmpty(etName.text.toString())) {
                    Toast.makeText(activity, "请输入农田名称", Toast.LENGTH_SHORT).show();
                    return
                }
                if (dataList.size > 1) {
                    okHttpPostImgs()
                } else {
                    okHttpPost("")
                }
            }
        }
    }

    private var cameraFile: File? = null
    private fun intentCamera() {
        val files = File(CONST.SDCARD_PATH)
        if (!files.exists()) {
            files.mkdirs()
        }
        cameraFile = File(CONST.SDCARD_PATH + "/" + System.currentTimeMillis() + ".jpg")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(activity!!, "${activity!!.packageName}.fileprovider", cameraFile!!)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            uri = Uri.fromFile(cameraFile)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, 1002)
    }

    private fun intentAlbum() {
        val intent = Intent(activity, SelectPictureActivity::class.java)
        intent.putExtra("count", dataList.size - 1)
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1001 -> if (data != null) {
                    val bundle = data.extras
                    if (bundle != null) {
                        if (dataList.size <= 1) {
                            dataList.removeAt(0)
                        } else {
                            dataList.removeAt(dataList.size - 1)
                        }
                        val list: ArrayList<DisasterDto> = bundle.getParcelableArrayList("dataList")
                        dataList.addAll(list)
                        addLastElement()
                        if (dataList.size >= 7) {
                            dataList.removeAt(dataList.size - 1)
                        }
                        if (mAdapter != null) {
                            mAdapter!!.notifyDataSetChanged()
                        }
                    }
                }
                1002 -> {
                    var fis: FileInputStream? = null
                    try {
                        fis = FileInputStream(cameraFile)
                        val bitmap = BitmapFactory.decodeStream(fis)
                        //						ivMyPhoto.setImageBitmap(bitmap);
                        if (dataList.size <= 1) {
                            dataList.removeAt(0)
                        } else {
                            dataList.removeAt(dataList.size - 1)
                        }
                        val dto = DisasterDto()
                        dto.imageName = ""
                        dto.imgUrl = cameraFile!!.absolutePath
                        dataList.add(dto)
                        addLastElement()
                        if (dataList.size >= 7) {
                            dataList.removeAt(dataList.size - 1)
                        }
                        if (mAdapter != null) {
                            mAdapter!!.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            fis?.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化viewPager
     */
    private fun initViewPager(current: Int, list: ArrayList<String>) {
        val imageArray = arrayOfNulls<ImageView>(list.size)
        for (i in list.indices) {
            val imgUrl = list[i]
            if (!TextUtils.isEmpty(imgUrl)) {
                val imageView = ImageView(activity)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                val bitmap = BitmapFactory.decodeFile(imgUrl)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                    imageArray[i] = imageView
                }
            }
        }
        val myViewPagerAdapter = MyViewPagerAdapter(imageArray)
        viewPager.adapter = myViewPagerAdapter
        viewPager.currentItem = current
        viewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(arg0: Int) {
                tvCount.text = (arg0 + 1).toString() + "/" + list.size
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
            override fun onPageScrollStateChanged(arg0: Int) {}
        })
    }

    private inner class MyViewPagerAdapter(private val mImageViews: Array<ImageView?>) : PagerAdapter() {
        override fun getCount(): Int {
            return mImageViews.size
        }

        override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
            return arg0 === arg1
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(mImageViews[position])
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val photoView = PhotoView(container.context)
            val drawable = mImageViews[position]!!.drawable
            photoView.setImageDrawable(drawable)
            container.addView(photoView, 0)
            photoView.onPhotoTapListener = OnPhotoTapListener { view, v, v1 ->
                scaleColloseAnimation(clViewPager)
                clViewPager.visibility = View.GONE
            }
            return photoView
        }

    }

    /**
     * 放大动画
     * @param view
     */
    private fun scaleExpandAnimation(view: View?) {
        val animationSet = AnimationSet(true)
        val scaleAnimation = ScaleAnimation(0f, 1.0f, 0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleAnimation.interpolator = LinearInterpolator()
        scaleAnimation.duration = 300
        animationSet.addAnimation(scaleAnimation)
        val alphaAnimation = AlphaAnimation(0f, 1.0f)
        alphaAnimation.duration = 300
        animationSet.addAnimation(alphaAnimation)
        view!!.startAnimation(animationSet)
    }

    /**
     * 缩小动画
     * @param view
     */
    private fun scaleColloseAnimation(view: View?) {
        val animationSet = AnimationSet(true)
        val scaleAnimation = ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleAnimation.interpolator = LinearInterpolator()
        scaleAnimation.duration = 300
        animationSet.addAnimation(scaleAnimation)
        val alphaAnimation = AlphaAnimation(1.0f, 0f)
        alphaAnimation.duration = 300
        animationSet.addAnimation(alphaAnimation)
        view!!.startAnimation(animationSet)
    }

    /**
     * 选择相机或相册
     */
    private fun selectCamera() {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.shawn_dialog_camera, null)
        val dialog = Dialog(activity, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.show()
        view.tvCamera.setOnClickListener {
            dialog.dismiss()
            checkCameraAuthority()
        }
        view.tvAlbum.setOnClickListener {
            dialog.dismiss()
            checkStorageAuthority()
        }
    }

    /**
     * 申请相机权限
     */
    private fun checkCameraAuthority() {
        if (Build.VERSION.SDK_INT < 23) {
            intentCamera()
        } else {
            if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(Manifest.permission.CAMERA)
                ActivityCompat.requestPermissions(activity!!, permissions, AuthorityUtil.AUTHOR_CAMERA)
            } else {
                intentCamera()
            }
        }
    }

    /**
     * 申请存储权限
     */
    private fun checkStorageAuthority() {
        if (Build.VERSION.SDK_INT < 23) {
            intentAlbum()
        } else {
            if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(activity!!, permissions, AuthorityUtil.AUTHOR_STORAGE)
            } else {
                intentAlbum()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AuthorityUtil.AUTHOR_CAMERA -> if (grantResults.isNotEmpty()) {
                var isAllGranted = true //是否全部授权
                for (gResult in grantResults) {
                    if (gResult != PackageManager.PERMISSION_GRANTED) {
                        isAllGranted = false
                        break
                    }
                }
                if (isAllGranted) { //所有权限都授予
                    intentCamera()
                } else { //只要有一个没有授权，就提示进入设置界面设置
                    AuthorityUtil.intentAuthorSetting(activity, "\"" + getString(R.string.app_name) + "\"" + "需要使用您的相机权限，是否前往设置？")
                }
            } else {
                for (permission in permissions) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission!!)) {
                        AuthorityUtil.intentAuthorSetting(activity, "\"" + getString(R.string.app_name) + "\"" + "需要使用您的相机权限，是否前往设置？")
                        break
                    }
                }
            }
            AuthorityUtil.AUTHOR_STORAGE -> if (grantResults.isNotEmpty()) {
                var isAllGranted = true //是否全部授权
                for (gResult in grantResults) {
                    if (gResult != PackageManager.PERMISSION_GRANTED) {
                        isAllGranted = false
                        break
                    }
                }
                if (isAllGranted) { //所有权限都授予
                    intentAlbum()
                } else { //只要有一个没有授权，就提示进入设置界面设置
                    AuthorityUtil.intentAuthorSetting(activity, "\"" + getString(R.string.app_name) + "\"" + "需要使用您的存储权限，是否前往设置？")
                }
            } else {
                for (permission in permissions) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission!!)) {
                        AuthorityUtil.intentAuthorSetting(activity, "\"" + getString(R.string.app_name) + "\"" + "需要使用您的存储权限，是否前往设置？")
                        break
                    }
                }
            }
        }
    }

    private fun initWheelView() {
        val c = Calendar.getInstance()
        val curYear = c[Calendar.YEAR]
        val curMonth = c[Calendar.MONTH] + 1 //通过Calendar算出的月数要+1
        val curDate = c[Calendar.DATE]
        val curHour = c[Calendar.HOUR_OF_DAY]
        val curMinute = c[Calendar.MINUTE]
        val curSecond = c[Calendar.SECOND]
        val numericWheelAdapter1 = NumericWheelAdapter(activity, 1950, curYear)
        numericWheelAdapter1.setLabel("年")
        year.viewAdapter = numericWheelAdapter1
        year.isCyclic = false //是否可循环滑动
        year.addScrollingListener(scrollListener)
        year.visibleItems = 7
        val numericWheelAdapter2 = NumericWheelAdapter(activity, 1, 12, "%02d")
        numericWheelAdapter2.setLabel("月")
        month.viewAdapter = numericWheelAdapter2
        month.isCyclic = false
        month.addScrollingListener(scrollListener)
        month.visibleItems = 7
        initDay(curYear, curMonth)
        day.isCyclic = false
        day.visibleItems = 7
        val numericWheelAdapter3 = NumericWheelAdapter(activity, 1, 23, "%02d")
        numericWheelAdapter3.setLabel("时")
        hour.viewAdapter = numericWheelAdapter3
        hour.isCyclic = false
        hour.addScrollingListener(scrollListener)
        hour.visibleItems = 7
        hour.visibility = View.VISIBLE
        val numericWheelAdapter4 = NumericWheelAdapter(activity, 1, 59, "%02d")
        numericWheelAdapter4.setLabel("分")
        minute.viewAdapter = numericWheelAdapter4
        minute.isCyclic = false
        minute.addScrollingListener(scrollListener)
        minute.visibleItems = 7
        minute.visibility = View.VISIBLE
        val numericWheelAdapter5 = NumericWheelAdapter(activity, 1, 59, "%02d")
        numericWheelAdapter5.setLabel("秒")
        second.viewAdapter = numericWheelAdapter5
        second.isCyclic = false
        second.addScrollingListener(scrollListener)
        second.visibleItems = 7
        second.visibility = View.VISIBLE
        year.currentItem = curYear - 1950
        month.currentItem = curMonth - 1
        day.currentItem = curDate - 1
        hour.currentItem = curHour - 1
        minute.currentItem = curMinute - 1
        second.currentItem = curSecond - 1
    }

    private val scrollListener: OnWheelScrollListener = object : OnWheelScrollListener {
        override fun onScrollingStarted(wheel: WheelView) {}
        override fun onScrollingFinished(wheel: WheelView) {
            val nYear = year!!.currentItem + 1950 //年
            val nMonth: Int = month.currentItem + 1 //月
            initDay(nYear, nMonth)
        }
    }

    /**
     */
    private fun initDay(arg1: Int, arg2: Int) {
        val numericWheelAdapter = NumericWheelAdapter(activity, 1, getDay(arg1, arg2), "%02d")
        numericWheelAdapter.setLabel("日")
        day.viewAdapter = numericWheelAdapter
    }

    /**
     *
     * @param year
     * @param month
     * @return
     */
    private fun getDay(year: Int, month: Int): Int {
        var day = 30
        var flag = false
        flag = when (year % 4) {
            0 -> true
            else -> false
        }
        day = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            2 -> if (flag) 29 else 28
            else -> 30
        }
        return day
    }

    /**
     */
    private fun setTextViewValue() {
        val yearStr = (year!!.currentItem + 1950).toString()
        val monthStr = if (month.currentItem + 1 < 10) "0" + (month.currentItem + 1) else (month.currentItem + 1).toString()
        val dayStr = if (day.currentItem + 1 < 10) "0" + (day.currentItem + 1) else (day.currentItem + 1).toString()
        val hourStr = if (hour.currentItem + 1 < 10) "0" + (hour.currentItem + 1) else (hour.currentItem + 1).toString()
        val minuteStr = if (minute.currentItem + 1 < 10) "0" + (minute.currentItem + 1) else (minute.currentItem + 1).toString()
        val secondStr = if (second.currentItem + 1 < 10) "0" + (second.currentItem + 1) else (second.currentItem + 1).toString()
        tvTime.text = "$yearStr-$monthStr-$dayStr $hourStr:$minuteStr:$secondStr"
    }

    private fun bootTimeLayoutAnimation() {
        if (layoutDate!!.visibility == View.GONE) {
            timeLayoutAnimation(true, layoutDate)
            layoutDate!!.visibility = View.VISIBLE
        } else {
            timeLayoutAnimation(false, layoutDate)
            layoutDate!!.visibility = View.GONE
        }
    }

    /**
     * 时间图层动画
     * @param flag
     * @param view
     */
    private fun timeLayoutAnimation(flag: Boolean, view: View?) {
        //列表动画
        val animationSet = AnimationSet(true)
        val animation: TranslateAnimation = if (!flag) {
            TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1f)
        } else {
            TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1f,
                    Animation.RELATIVE_TO_SELF, 0f)
        }
        animation.duration = 400
        animationSet.addAnimation(animation)
        animationSet.fillAfter = true
        view!!.startAnimation(animationSet)
        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                view.clearAnimation()
            }
        })
    }

}
