package x.frame;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.yanzhenjie.permission.Permission;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import x.camera.CameraHelper;
import x.datautil.L;
import x.dialog.AlertDialog;
import x.permission.PermissionManager;

import static android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT;


//import x.permission.PermissionManager;

//import x.permission.PermissionManager;

//import com.yanzhenjie.permission.Permission;


public abstract class BaseActivity extends AppCompatActivity {
    public final static String ACTION_LOGOUT = "BaseActivity.ACTION_LOGOUT";
    public final static String EXTRA_IF_LOGIN_INVALID = "BaseActivity.EXTRA_IF_LOGIN_INVALID";
    public final static String ACTION_REFRESH_FOR_DATE_CHANGED = "BaseActivity.ACTION_REFRESH_FOR_DATE_CHANGED";
    /**
     * Settings.commonInfo  在某些情况下变成了 null ，怀疑是系统绕过了 MainActivity 造成的
     */
    final static String SAVED_INSTANCE_STATE_COMMONINFO = "SAVED_INSTANCE_STATE_COMMONINFO";
    protected static WeakReference<BaseActivity> mActivityReference;
    /**
     * 这里的 context 原本应该是注册 receiver 时所在的 activity ,只是如果 HttpSender 获取 LocalBroadcastManager 的时候，
     * 使用的是一个全局的ApplicationContext:(this.mContext = mContext.getApplicationContext();).
     * 所以接收到消息的时候，context也是ApplicationContext.
     */
    BroadcastReceiver mPublicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_LOGOUT.equals(intent.getAction())) {
                boolean showToast = intent.getBooleanExtra(EXTRA_IF_LOGIN_INVALID, false);
                if (mActivityReference != null) {
                    BaseActivity mActivity = mActivityReference.get();
                    if (mActivity != null) {
                        mActivityReference.clear();
                        mActivity.logout(showToast);
                    }
                }
            }
        }
    };
    public InputMethodManager imm;
    FrameLayout customViewContainer;
    BaseFragment curFragment;
    PermissionManager manager;
    AlertDialog dialogBleEnableFailed;
    AlertDialog dialogNoNetwork;
    CameraHelper mCameraHelper;
    Class<? extends BaseFragment> firstFragment;
    /**
     * 使用这个list在当前架构下仍然存在无法修改页面title的问题,如果是在一个需要严格设定title的地方,不可以这么用
     */
    ArrayList<BaseFragment> fragmentList = new ArrayList<>(2);
    public ActivitySecureHandler mSecureHandler;

    static ArrayList<BaseActivity> activityList = new ArrayList();

    public void logout(boolean showToast) {
        for (BaseActivity activity : activityList) {
            if (!this.equals(activity)) {
                activity.finish();
            }
        }
        //throw new RuntimeException("Need interface.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSoftInput();
        try {
        //    MobclickAgent.onResume(this);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
        //    MobclickAgent.onPause(this);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
       // outState.putParcelable(SAVED_INSTANCE_STATE_COMMONINFO, Settings.commonInfo);
    }
    //protected abstract View onLoadView(Bundle savedInstanceState);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int flag = getWindow().getDecorView().getSystemUiVisibility();
            /**
             * see {@link <a href="https://developer.android.com/reference/android/R.attr.html#windowLightStatusBar"></a>}
             */
            //修改statusbar字体颜色
            flag |= (WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            //设置沉浸式
            flag |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().getDecorView().setSystemUiVisibility(flag);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        mActivityReference = new WeakReference(this);
        mSecureHandler = new ActivitySecureHandler(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LOGOUT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mPublicReceiver, filter);
        if (savedInstanceState != null) {
           // if (savedInstanceState.containsKey(SAVED_INSTANCE_STATE_COMMONINFO) && Settings.commonInfo == null) {
            //    Settings.commonInfo = savedInstanceState.getParcelable(SAVED_INSTANCE_STATE_COMMONINFO);
            //}
        }
        activityList.add(this);
    }

    public BaseFragment getCurFragment() {
        return curFragment;
    }

    /**
     * 会造成 top navigation bar 的颜色与 status bar 不一致
     */
    void setStatusTop() {
        int statusBarHeight;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        } else {
            statusBarHeight = getResources().getDimensionPixelSize(R.dimen.default_statusbar_height);
        }
        getWindow().getDecorView().setPadding(0, statusBarHeight, 0, 0);
    }

    /**
     * 修改status bar 的字体颜色
     */

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void processLollipopAbove() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        int flag = getWindow().getDecorView().getSystemUiVisibility();
        boolean lightStatusBar = true;
        if (lightStatusBar) {
            /**
             * see {@link <a href="https://developer.android.com/reference/android/R.attr.html#windowLightStatusBar"></a>}
             */
            //修改statusbar字体颜色
            flag |= (WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            //设置沉浸式。
            flag |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        }
        getWindow().getDecorView().setSystemUiVisibility(flag);
//        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    protected BaseFragment showFragment(Class<? extends BaseFragment> clazz, Intent intent) {
        return showFragment(clazz, intent, true);
    }

    /**
     * @param clazz
     * @param intent
     * @param allowBackStock 现在没时间处理这个参数
     */
    protected BaseFragment showFragment(Class<? extends BaseFragment> clazz, Intent intent, boolean allowBackStock) {
        View container = this.findViewById(R.id.fm_container);
        if(container==null){
            throw new RuntimeException("Need a FrameLayout which id is fm_container to load fragment.");
        }
        if (firstFragment == null) {//记录第一个fragment，方便回退
            firstFragment = clazz;
        }
        FragmentManager fManager = getSupportFragmentManager();
        BaseFragment nextFragment = (BaseFragment) fManager.findFragmentByTag(clazz.getName());
        if (nextFragment == null) {
            nextFragment = (BaseFragment) Fragment.instantiate(this, clazz.getName(), null);
        }

        if(intent!=null){
//            Intent intent=new Intent();
//            intent.putExtras(bundle);
            nextFragment.onGetAction(intent);
        }
        //        if (curFragment != nextFragment) {
        FragmentTransaction ft = fManager.beginTransaction();
        if (curFragment != null) {
            ft.hide(curFragment);
        }
        if (!nextFragment.isAdded()) {
            ft.add(R.id.fm_container, nextFragment, clazz.getName());
        } else {
            ft.show(nextFragment);
        }
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
        curFragment = nextFragment;
        fragmentList.add(curFragment);
//        }
        return curFragment;
    }

    /**
     * fragment回滚
     *
     * @param clazz 回滚到这个fragment 就结束,@null 默认回滚到第一个fragment就结束
     */
    public void getBackStack(@Nullable Class<? extends Fragment> clazz) {
        Fragment fm = null;
        FragmentManager manager = getSupportFragmentManager();
        if (clazz != null) {
            fm = manager.findFragmentByTag(clazz.getName());
        } else if (firstFragment != null) {
            fm = manager.findFragmentByTag(firstFragment.getName());
        }
        if (fm == null || (fm != null && fm.isVisible())) {//没有fragment或者是第一个入栈的fragment都结束
            fragmentList.clear();
            finish();
        } else {
            if (!fragmentList.isEmpty()) {
                fragmentList.remove(fragmentList.size() - 1);
                if (!fragmentList.isEmpty()) {
                    curFragment = fragmentList.get(fragmentList.size() - 1);
                }
                Log.e("TAG", "curFragment:" + curFragment.getClass().getName());
            }
            manager.popBackStack();
        }
        if(curFragment!=null){Log.e("TAG", "curFragment:222" + curFragment.getClass().getName());}
    }

    public void hideSoftInput() {
        if (imm != null && imm.isActive() && getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void showSoftInput(EditText view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, SHOW_IMPLICIT);
        }
    }

    @Override
    public void onBackPressed() {
        hideSoftInput();
        getBackStack(null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mPublicReceiver);
        Log.e("TAG", "onDestroy:" + this.getClass().getSimpleName());
        activityList.remove(this);
    }

    /**
     * 返回键
     *
     * @param back
     */
    public void onBackPressed(View back) {
        onBackPressed();
    }

    public interface OnAction {
        void onGetAction(Intent intent);
    }

    /**
     * 权限
     *
     * @param mListener
     */
    public void requestPermission(PermissionManager.PermissionRes mListener, String[]... permissions) {
        manager = new PermissionManager(this);
        //Permission.LOCATION, Permission.STORAGE, Permission.CAMERA, Permission.CALENDAR
        manager.setPermissions(permissions);
        manager.setPermissionRes(mListener);
        manager.requestPermission();
    }

    void showBleEnableFailedDialog() {
        if (dialogBleEnableFailed == null) {
            dialogBleEnableFailed = new AlertDialog(this, R.style.base_dialog);
            dialogBleEnableFailed.setTitle(R.string.warning);
            dialogBleEnableFailed.setMessage(R.string.ble_unavailable);
//            dialogBleEnableFailed.setCanceledOnTouchOutside(false);
//            dialogBleEnableFailed.setCancelable(false);
            dialogBleEnableFailed.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.get_it), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    finish();
                }
            });
        }
        dialogBleEnableFailed.show();
    }

    void showNoNetworkDialog(final boolean finish) {
        if (dialogNoNetwork == null) {
            dialogNoNetwork = new AlertDialog(this, R.style.base_dialog);
            dialogNoNetwork.setTitle(R.string.warning);
            dialogNoNetwork.setMessage(R.string.no_network);
            dialogNoNetwork.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.get_it), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (finish) {
                        finish();
                    }
                }
            });
        }
        if (!dialogNoNetwork.isShowing()) dialogNoNetwork.show();
    }

    /**
     * 后期有时间修改模式,这里不能随意打开蓝牙
     */
    void checkBlePermission() {
        L.e("BleController checkBlePermission,Context:" + this.getLocalClassName());
        requestPermission(new PermissionManager.PermissionRes() {
            @Override
            public void onSucceed() {
                L.e("Permission Grant:Permission.LOCATION");

            }

            @Override
            public void onFailed() {
                L.e("Permission Denied:Permission.LOCATION");
                finish();
            }
        }, Permission.Group.LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("TAG", "onActivityResult:" + requestCode);
        switch (requestCode) {
            case PermissionManager.REQUEST_SETTINGS:
                if (manager != null) manager.onActivityResult(requestCode, resultCode, data);
                break;
            case CameraHelper.SYS_INTENT_CAPTURE:
            case CameraHelper.SYS_INTENT_CROP_IMG:
                if(mCameraHelper!=null)mCameraHelper.onCameraActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    /**
     * for handler msg.
     */
    protected void handleMessage(Message msg) {
    }

    public final static class ActivitySecureHandler extends Handler {
        protected WeakReference<BaseActivity> mReference;

        public ActivitySecureHandler(BaseActivity activity) {
            mReference = new WeakReference(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mReference != null) {
                BaseActivity activity = mReference.get();
                if (activity != null && !activity.isFinishing()) {
                    activity.handleMessage(msg);
                }
            }
        }
    }


/*************************** for camera **************************/
//    final static int SYS_INTENT_CAPTURE=1999;
//    final static int SYS_INTENT_CROP_IMG=2000;

//    public interface OnCameraListener{
//        void onGetCameraRes(Bitmap bitmap);
//    }
//
//    OnCameraListener onCameraListener;
//
//    public void setOnCameraListener(OnCameraListener listener){
//        onCameraListener=listener;
//    }
//    Uri srcUri;
//    Uri cropUri;
//    int resWidth,resHeight;
    public void requestCamera(final int resWidth, final int resHeight, CameraHelper.OnCameraListener mListener){
        mCameraHelper=new CameraHelper(this,mListener);
        requestPermission(new PermissionManager.PermissionRes() {
            @Override
            public void onSucceed() {
                mCameraHelper.startCamera(resWidth,resHeight);
            }

            @Override
            public void onFailed() {

            }
        }, Permission.Group.CAMERA, Permission.Group.STORAGE);
    }


//    private void startCamera() {
//        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if(intent.resolveActivity(this.getPackageManager())!=null){
//            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            }
//            srcUri= FileUtils.createUriByTime(this);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT,srcUri);
//            startActivityForResult(intent,SYS_INTENT_CAPTURE);
//
//        }
//    }

//    void grandPermission(Context context, Intent intent, Uri uri){
//        List<ResolveInfo> resolveInfos=context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//        if(resolveInfos!=null&&resolveInfos.size()!=0){
//            Iterator<ResolveInfo> it=resolveInfos.iterator();
//            while(it.hasNext()){
//                ResolveInfo info=it.next();
//                String packageName=info.activityInfo.packageName;
//                context.grantUriPermission(packageName,uri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            }
//        }
//    }

//    public void onCameraActivityResult(int requestCode ,int resultCode,Intent data){
//        if(resultCode!=RESULT_OK){return;}
//        if(requestCode==SYS_INTENT_CROP_IMG){
//            try{
//                BitmapFactory.Options ops=new BitmapFactory.Options();
//                ops.outWidth=resWidth;
//                ops.outHeight= resHeight;
//                Bitmap bitmap=BitmapFactory.decodeStream(getContentResolver().openInputStream(cropUri),null,ops);
//                if(bitmap!=null){
//                    if(onCameraListener!=null){
//                        onCameraListener.onGetCameraRes(bitmap);
//                    }
//                }
//            }catch (Exception e){e.printStackTrace();}
//        }else if(requestCode==SYS_INTENT_CAPTURE){
//            cropUri=FileUtils.createUriByTime(this);
//            Intent cropIntent=FileUtils.cropImageUri(srcUri,cropUri,1,1,resWidth,resHeight);
//            grandPermission(this,cropIntent,cropUri);
//            startActivityForResult(cropIntent,SYS_INTENT_CROP_IMG);
//        }
//    }
}
