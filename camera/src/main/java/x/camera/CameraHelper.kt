package x.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import x.datautil.FileUtils

class CameraHelper(private val mContext:Activity,private val onCameraListener: OnCameraListener) {
    companion object {
        const val SYS_INTENT_CAPTURE = 1999
        const val SYS_INTENT_CROP_IMG = 2000
    }


    interface OnCameraListener {
        fun onGetCameraRes(bitmap: Bitmap)
    }
//    internal var onCameraListener: OnCameraListener? = null
//    fun setOnCameraListener(listener: OnCameraListener) {
//        onCameraListener = listener
//    }
    private var srcUri: Uri?=null
    private var cropUri: Uri?=null
    private var resWidth: Int = 0
    private var resHeight:Int = 0

    fun startCamera(resWidth: Int, resHeight: Int) {
        this.resWidth = resWidth
        this.resHeight = resHeight
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(mContext.packageManager) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            srcUri = FileUtils.createUriByTime(mContext)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, srcUri)
            mContext.startActivityForResult(intent, SYS_INTENT_CAPTURE)

        }
    }

    internal fun grandUriPermission(context: Context, intent: Intent, uri: Uri) {
        val resolveInfos = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resolveInfos != null && resolveInfos.size != 0) {
            val it = resolveInfos.iterator()
            while (it.hasNext()) {
                val info = it.next()
                val packageName = info.activityInfo.packageName
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }
    }

    fun onCameraActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == SYS_INTENT_CROP_IMG) {
            try {
                val ops = BitmapFactory.Options()
                ops.outWidth = resWidth
                ops.outHeight = resHeight
                val bitmap = BitmapFactory.decodeStream(mContext.contentResolver.openInputStream(cropUri), null, ops)
                if (bitmap != null) {
                        onCameraListener.onGetCameraRes(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else if (requestCode == SYS_INTENT_CAPTURE) {
            cropUri = FileUtils.createUriByTime(mContext)
            val cropIntent = FileUtils.cropImageUri(srcUri, cropUri, 1, 1, resWidth, resHeight)
            grandUriPermission(mContext, cropIntent, cropUri!!)
            mContext.startActivityForResult(cropIntent, SYS_INTENT_CROP_IMG)
        }
    }
}