package x.datautil;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by phy on 2017/9/11.
 */

public class FileUtils {
    public static String BASE_PATH = "daaw/";//default path
    public static String DEFAULT_LOG_PATH = "daaw/log/";//default path
    public static String DEFAULT_IMG_PATH = "daaw/img/";//default path
    public static HashMap<String, Bitmap> cache = new HashMap<>();

    static {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + DEFAULT_IMG_PATH);
        file.mkdirs();
    }

    /**
     * read bitmap from local file.
     *
     * @param path
     * @return
     */
    public static Bitmap readImage(String path, int roughWidth, int roughHeight) {
        try {
            BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, ops);
            BitmapFactory.Options newOps = new BitmapFactory.Options();
            newOps.inTempStorage = new byte[24 * 1024];//24kb , 147*147=21609;
            if (ops.outWidth > 1440 || ops.outHeight > 2560) {
                int scaleW = 1, scaleH = 1;
                if (ops.outHeight > roughHeight) {
                    scaleH = (ops.outHeight / roughHeight);
                }
                if (ops.outWidth > roughWidth) {
                    scaleW = (Math.max(ops.outHeight, ops.outWidth) / roughWidth);
                }
                newOps.inSampleSize = Math.max(scaleH, scaleW);
            }
            Bitmap map = BitmapFactory.decodeFile(path, newOps);
            if (map != null) {
                if (map.getWidth() != roughWidth || map.getHeight() != roughHeight) {
                    Matrix matrix = new Matrix();
                    float scaleX = roughWidth * 1.0f / map.getWidth();
                    float scaleY = roughHeight * 1.0f / map.getHeight();
                    matrix.postScale(scaleX, scaleY);
                    Bitmap newMap = Bitmap.createBitmap(map, 0, 0, map.getWidth(), map.getHeight(), matrix, true);
                    if (map != newMap) {
                        map.recycle();
                    }
                    return newMap;
                }
            }
            return map;
        } catch (Exception e) {
            return null;
        }

        /*BitmapFactory.Options ops = new BitmapFactory.Options();
        ops.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, ops);
        BitmapFactory.Options newOps = new BitmapFactory.Options();
        newOps.inTempStorage = new byte[24 * 1024];//24kb , 147*147=21609;
        int scaleW=1, scaleH=1;
        if (ops.outHeight > roughHeight) {
            scaleH = (int) (ops.outHeight / roughHeight);
        }
        if (ops.outWidth > roughWidth) {
            scaleW = (int) (Math.max(ops.outHeight, ops.outWidth) / roughWidth);
        }
        newOps.inSampleSize = Math.max(scaleH,scaleW);

        return BitmapFactory.decodeFile(path, newOps);*/
    }

    /**
     * save bitmap to local file.
     *
     * @param bm
     * @param path
     * @return
     */
    public static boolean saveImage(Bitmap bm, String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 将文件保存在私有的目录中
     *
     * @param context
     * @param bm
     * @param fileName
     * @return
     */
    public static boolean saveImage(Context context, Bitmap bm, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Uri createUriByTime(Context context) {
        String fileName = new SimpleDateFormat("yyyyMMddHHmmsssss", Locale.US).format(System.currentTimeMillis()) + ".png";
        File picFile = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + DEFAULT_IMG_PATH + fileName);
        if (picFile.exists()) {
            picFile.delete();
        }
        Uri imgUri = Uri.fromFile(picFile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String pkgName = context.getApplicationContext().getPackageName();
            android.util.Log.e("TAG", "ProviderName:" + (pkgName + ".fileprovider"));
            imgUri = FileProvider.getUriForFile(context, pkgName + ".fileprovider", picFile);
        }
        return imgUri;
    }

    /**
     * @param orgUri  剪裁原图的Uri
     * @param desUri  剪裁后的图片的Uri
     * @param aspectX X方向的比例
     * @param aspectY Y方向的比例
     * @param width   剪裁图片的宽度
     * @param height  剪裁图片高度
     */
    public static Intent cropImageUri(Uri orgUri, Uri desUri, int aspectX, int aspectY, int width, int height) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        intent.setDataAndType(orgUri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", width);
        intent.putExtra("outputY", height);
        intent.putExtra("scale", true);
        //将剪切的图片保存到目标Uri中
        intent.putExtra(MediaStore.EXTRA_OUTPUT, desUri);
        intent.putExtra("return-data", false);
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra("noFaceDetection", true);
        return intent;
    }

    public static Bitmap readAssetsImage(Context context, String filePath, int roughWidth, int roughHeight) {
        Bitmap image = null;
        InputStream is = null;
        AssetManager am = context.getAssets();
        try {
            is = am.open(filePath);
            image = BitmapFactory.decodeStream(is);
            if (roughWidth != 0 && roughHeight != 0 && image != null) {
                if (image.getWidth() != roughWidth || image.getHeight() != roughHeight) {
                    Matrix matrix = new Matrix();
                    float scale = Math.max(roughWidth * 1.0f / image.getWidth(), roughHeight * 1.0f / image.getHeight());
                    matrix.postScale(scale, scale);
                    Bitmap newMap = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
                    android.util.Log.d("TAG", "srcW:" + image.getWidth() + "   srcH:" + image.getHeight() + "   roughWidth:" + roughWidth + "   roughHeight:" + roughHeight + "  scale:" + scale + "  srcSize:" + image.getByteCount() + "  desSize:" + newMap.getByteCount());
                    if (newMap != image) {
                        image.recycle();
                    }
                    return newMap;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
        return image;
    }
}
