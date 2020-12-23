package phy.jsf

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.hardware.Camera
import android.hardware.Camera.AutoFocusCallback
import android.hardware.Camera.PreviewCallback
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import com.dtr.zbar.scan.CameraManager
import com.dtr.zbar.scan.CameraPreview
import com.yanzhenjie.permission.Permission
import net.sourceforge.zbar.Config
import net.sourceforge.zbar.Image
import net.sourceforge.zbar.ImageScanner
import net.sourceforge.zbar.Symbol
import x.datautil.L
import x.frame.BaseActivity
import x.frame.BaseFragment
import x.permission.PermissionManager
import java.io.IOException

class QR_ScanFragment: BaseFragment(), BaseActivity.OnAction {
    override fun onGetAction(intent: Intent?) {
        TODO("Not yet implemented")
    }

    //完整代码见library :CaptureActivity
    var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var autoFocusHandler: Handler? = null
    private var mCameraManager: CameraManager? = null

    private var scanPreview: FrameLayout? = null
    private var scanContainer: RelativeLayout? = null
    private var scanCropView: RelativeLayout? = null
    private var scanLine: ImageView? = null

    private var mCropRect: Rect? = null
    private var mConvertCropRect: Rect? = null
    private var previewing = true
    private var mImageScanner: ImageScanner? = null
    var mediaPlayer: MediaPlayer? = null
    val EXTRA_SCAN_RESULT = "extra_scan_result"
//    public final static String EXTRA_IF_RECEIVER = "extra_if_receiver";

    //    public final static String EXTRA_IF_RECEIVER = "extra_if_receiver";
    val REQUEST_QR_SCAN = 1000
    var retry = 0
    val EXTRA_IF_EXPRESS = "extra_express"
    val EXTRA_IF_NEED_MMC_BLE_BATTERY = "extra_need_mmc_ble_battery"

    //    ExecutorService mService;
    var threadFlag = true //若解析成功,确保只进行一次后续操作

    companion object{
        init{
            System.loadLibrary("iconv")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.qr_scanner, container, false)
        setView(root)
        initMediaPlayer(mActivity)
        L.e("QR_ScanFragment,create view...")
        initCamera()
//        requestCamera()
        return root
    }

    /**
     * 需要在fragment可见的时候刷新状态的操作都可以放在这里
     */
    override fun onVisible() {
        super.onVisible()
        startScanAnimation(scanLine)
        L.e("QR_ScanFragment,onVisible...")
        initCamera()
        threadFlag = true
    }

    fun requestCamera(){
        mActivity.requestPermission(object : PermissionManager.PermissionRes {
            override fun onSucceed() {
                initCamera()
            }
            override fun onFailed() {
                L.e("Camera permission request failed.")
            }
        }, Permission.Group.CAMERA, Permission.Group.STORAGE)
    }

    override fun onHide() {
        super.onHide()
        L.e("hide..")
        releaseCamera()
    }

    private fun setView(root:View) {
        val tv_center = root.findViewById(R.id.tv_center) as TextView
        tv_center.setText(R.string.scan_prompt)

        scanPreview = root.findViewById(R.id.capture_preview) as FrameLayout?
        scanContainer = root.findViewById(R.id.container) as RelativeLayout?
        scanCropView = root.findViewById(R.id.capture_crop_view) as RelativeLayout?
        scanLine = root.findViewById(R.id.capture_scan_line) as ImageView?
    }

    private fun releaseCamera() {
        previewing = false
        autoFocusHandler!!.removeCallbacks(doAutoFocus)
        if (mCamera != null) {
            mCamera!!.setPreviewCallback(null)
            mCamera!!.stopPreview()
            mCamera!!.release()
            mCamera = null
        }
        mCameraManager!!.closeDriver()
        Log.e("TAG", "release Camera.")
    }

    private fun initCamera() {
        L.e("initCamera")
        previewing = true
        mImageScanner = ImageScanner()
        mImageScanner!!.setConfig(0, Config.X_DENSITY, 3)
        mImageScanner!!.setConfig(0, Config.Y_DENSITY, 3)
        mImageScanner!!.enableCache(true)
        if (autoFocusHandler == null) {
            autoFocusHandler = Handler()
        }
        if (mCameraManager == null) {
            mCameraManager = CameraManager(mActivity)
        }
        try {
            mCameraManager!!.openDriver()
            mCamera = mCameraManager!!.camera
            mPreview = CameraPreview(mActivity, mCamera, previewCb, autoFocusCB)
            scanPreview!!.addView(mPreview)
            startScanAnimation(scanLine)
        } catch (e: Exception) {
            e.printStackTrace()
            if (retry < 5) {
                retry++
                Log.e("TAG", "camera error, retry.")
                releaseCamera()
                initCamera()
            } else {
                e.printStackTrace()
                Toast.makeText(mActivity.applicationContext, R.string.open_camera_faild, Toast.LENGTH_SHORT).show()
                mActivity.finish()
            }
        }
        Log.e("TAG", "Camera:" + (mCamera != null))
    }

    fun startScanAnimation(v: View?) {
        //type为Animation.RELATIVE_TO_PARENT时候,取值相对于parent的中心位置
        val top = -0.25f
        var bottom = 0.65f

        val animation = TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, top, Animation.RELATIVE_TO_PARENT,
                bottom)
        animation.duration = 3000
        animation.repeatCount = -1
        animation.repeatMode = Animation.REVERSE
        v!!.clearAnimation()
        v.startAnimation(animation)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        L.e("destroy view.")
        releaseCamera()
        releaseMediaPlayer()
        System.gc()
    }

    // Mimic continuous auto-focusing
    val autoFocusCB:AutoFocusCallback = AutoFocusCallback { success, camera -> autoFocusHandler!!.postDelayed(doAutoFocus, 1000) }

    private val doAutoFocus:Runnable = Runnable {
        if (previewing) {
            try {
                //频繁打开关闭相机的时候,偶尔会遇到 java.lang.RuntimeException: autoFocus failed
                mCamera!!.autoFocus(autoFocusCB)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var previewCb = PreviewCallback { data, camera ->
        if (!threadFlag) {
            return@PreviewCallback
        }
        val size: Camera.Size
        size = try {
            /**
             * 不停地快速打开关闭相机的时候,偶尔会遇到这个异常,似乎相机的state is bad.
             * java.lang.RuntimeException: getParameters failed (empty parameters)
             */
            /**
             * 不停地快速打开关闭相机的时候,偶尔会遇到这个异常,似乎相机的state is bad.
             * java.lang.RuntimeException: getParameters failed (empty parameters)
             */
            /**
             * 不停地快速打开关闭相机的时候,偶尔会遇到这个异常,似乎相机的state is bad.
             * java.lang.RuntimeException: getParameters failed (empty parameters)
             */
            /**
             * 不停地快速打开关闭相机的时候,偶尔会遇到这个异常,似乎相机的state is bad.
             * java.lang.RuntimeException: getParameters failed (empty parameters)
             */
            camera.parameters.previewSize
        } catch (e: Exception) {
            e.printStackTrace()
            return@PreviewCallback
        }
        if (mCropRect == null) {
            initCrop()
        }
        val barcode = Image(size.width, size.height, "Y800")
        barcode.data = data
        if (mConvertCropRect == null) {
            mConvertCropRect = convertCrop(mCropRect)
        }
        barcode.setCrop(mConvertCropRect!!.left, mConvertCropRect!!.top, mConvertCropRect!!.width(),
                mConvertCropRect!!.height())
        /**
         * 主要时间消耗 :ImageScanner.scanImage(barcode);
         */
        /**
         * 主要时间消耗 :ImageScanner.scanImage(barcode);
         */
        /**
         * 主要时间消耗 :ImageScanner.scanImage(barcode);
         */
        /**
         * 主要时间消耗 :ImageScanner.scanImage(barcode);
         */
        val res = mImageScanner!!.scanImage(barcode)
        var result: String? = null
        barcode.destroy()
        if (res != 0) {
            val syms = mImageScanner!!.results
            for (sym in syms) {
                if (sym.type == Symbol.QRCODE) {
                    result = sym.data
                    if (!TextUtils.isEmpty(result)) {
                        Log.e("TAG", "get result:$result")
                        synchronized(mActivity) {
                            if (threadFlag) {
                                threadFlag = false
                                setResult(result)
                            }
                        }
                        break
                    }
                }
            }
            syms.destroy()
        }
    }

    /**
     * 这里为了提高效率,不翻转相机数据,那么截取的区域的位置需要从竖屏的位置转换成横屏的位置
     *
     * @return
     */
    fun convertCrop(src: Rect?): Rect? {
        val cameraHeight = mCameraManager!!.cameraResolution.y
        val rect = Rect()
        rect.left = src!!.top
        rect.right = rect.left + src.height()
        rect.top = cameraHeight - (src.left + src.width())
        rect.bottom = rect.top + src.width()
        return rect
    }

    fun setResult(result: String?) {
        if (!TextUtils.isEmpty(result)) {
            L.e("get scan res")
            releaseCamera()
            beepAndVibrate()
            try {
                Log.e("TAG", "scan:$result")
                Toast.makeText(mActivity,result,Toast.LENGTH_LONG).show()
//                mActivity.finish()
                var context=mActivity as MainActivity
                context.setScanRes(result!!)
            } catch (e: Exception) {
                e.printStackTrace()
                L.e("Scan err")
                mActivity.finish()
            }
        }
    }

    /**
     * 初始化截取的矩形区域
     */
    private fun initCrop() {
        //布局是竖屏,camera参数时横屏,所以这里要倒转过来
        val cameraWidth = mCameraManager!!.cameraResolution.y
        val cameraHeight = mCameraManager!!.cameraResolution.x

        /** 获取布局中扫描框的位置信息  */
        val location = IntArray(2)
        scanCropView!!.getLocationInWindow(location)
        val cropLeft = location[0]
        val cropTop = location[1] - getStatusBarHeight()
        val cropWidth = scanCropView!!.width
        val cropHeight = scanCropView!!.height

        /** 获取布局容器的宽高  */
        val containerWidth = scanContainer!!.width
        val containerHeight = scanContainer!!.height

        /** 计算最终截取的矩形的左上角顶点x坐标(屏幕位置与相机尺寸的映射)  */
        val x = cropLeft * cameraWidth / containerWidth

        /** 计算最终截取的矩形的左上角顶点y坐标  */
        val y = cropTop * cameraHeight / containerHeight

        /** 计算最终截取的矩形的宽度  */
        val width = cropWidth * cameraWidth / containerWidth

        /** 计算最终截取的矩形的高度  */
        val height = cropHeight * cameraHeight / containerHeight
        /** 生成最终的截取的矩形  */
        mCropRect = Rect(x, y, width + x, height + y)
    }

    private fun getStatusBarHeight(): Int {
        try {
            val c = Class.forName("com.android.internal.R\$dimen")
            val obj = c.newInstance()
            val field = c.getField("status_bar_height")
            val x = field[obj].toString().toInt()
            return resources.getDimensionPixelSize(x)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initMediaPlayer(activity: Context) {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        // When the beep has finished playing, rewind to queue up another one.
        mediaPlayer!!
                .setOnCompletionListener { player -> player.seekTo(0) }
        val file = activity.resources.openRawResourceFd(
                R.raw.beep)
        try {
            mediaPlayer!!.setDataSource(file.fileDescriptor,
                    file.startOffset, file.length)
            file.close()
            mediaPlayer!!.prepare()
        } catch (ioe: IOException) {
            mediaPlayer = null
        }
    }

    fun beepAndVibrate() {
        mediaPlayer!!.start()
        val vibrator = mActivity
                .getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(500)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_QR_SCAN) {
            Log.e("TAG", "QR_ScanFragment,onActivityResult ok.")
//            mActivity.setResult(RESULT_OK, data)
//            mActivity.finish()
        }
    }
}