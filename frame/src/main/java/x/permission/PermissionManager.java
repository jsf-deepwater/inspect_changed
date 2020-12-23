package x.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.AppOpsManagerCompat;
import androidx.core.content.ContextCompat;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;
import com.yanzhenjie.permission.SettingService;

import java.util.Arrays;
import java.util.List;

import x.frame.R;

public class PermissionManager {
    public final static int REQUEST_SETTINGS = 3824;//系统设置

    Activity mContext;
    String[][] requestPermissions;
    PermissionRes permissionRes;
//    SettingDialog settingDialog;

    public PermissionManager(Activity mContext) {
        this.mContext = mContext;
    }

    public void setPermissions(String[]... permissions) {
        requestPermissions = permissions;
    }

    public void requestPermission() {
        AndPermission.with(mContext).runtime().permission(requestPermissions).rationale(mRationale)
                .onGranted(new Action<List<String>>() {

                    @Override
                    public void onAction(List<String> permissions) {
                        if (permissionRes != null) {
                            permissionRes.onSucceed();
                        }
                    }
                }).onDenied(new Action<List<String>>() {
            @Override
            public void onAction(List<String> permissions) {
                StringBuilder pers = new StringBuilder();
                for (String str : permissions) {
                    pers.append(str + "  ");
                }
                if (AndPermission.hasAlwaysDeniedPermission(mContext, permissions)) {
                    showErrorDialog(false, permissions);
                } else {
                    permissionRes.onFailed();
                }
            }
        }).start();
    }

    Rationale mRationale = new Rationale<List<String>>() {
        @Override
        public void showRationale(Context context, List<String> permissions, RequestExecutor executor) {
            showRationaleDialog(context, permissions, executor);
        }
    };

    void showRationaleDialog(final Context context, final List<String> permissions, final RequestExecutor executor) {
        Dialog mRationaleDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        builder.setTitle(context.getString(R.string.permission_warning));
        builder.setMessage(context.getString(R.string.permission_requestPermission_c, Permission.transformText(context, permissions)));
        builder.setNegativeButton(context.getString(R.string.permission_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                executor.cancel();
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(context.getString(R.string.permission_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                executor.execute();
                dialog.dismiss();
            }
        });
        mRationaleDialog = builder.create();
        mRationaleDialog.show();
    }

    void showErrorDialog(boolean ifSuccess, final List<String> permissions) {
        Dialog mSettingServiceDialog;
        final SettingService settingService = AndPermission.permissionSetting(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        builder.setTitle(mContext.getString(R.string.permission_warning));
        builder.setMessage(mContext.getString(ifSuccess ? R.string.permission_requestPermission_b : R.string.permission_requestPermission_a, Permission.transformText(mContext, permissions)));
        builder.setNegativeButton(mContext.getString(R.string.permission_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                settingService.cancel();
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(mContext.getString(R.string.permission_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                settingService.execute(REQUEST_SETTINGS);
                dialog.dismiss();
            }
        });
        mSettingServiceDialog = builder.create();
        mSettingServiceDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SETTINGS) {//在系统设置中设置了权限
            Log.e("TAG", "PermissionManager.onActivityResult  resultCode:" + resultCode);
            boolean res = true;
            for (String[] strs : requestPermissions) {
                if (!hasPermission(mContext, strs)) {
                    Log.e("Permission", "request system settings for permission,but have no permissions.");
                    res = false;
                }
            }
            if (permissionRes != null) {
                if (!res) {
                    //exit.
                    permissionRes.onFailed();
                } else {
                    permissionRes.onSucceed();
                }
            }
        }
    }

    /**
     * 请求权限的最终结果
     */
    public interface PermissionRes {
        void onSucceed();

        void onFailed();
    }

    public void setPermissionRes(PermissionRes permissionRes) {
        this.permissionRes = permissionRes;
    }

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context     {@link Context}.
     * @param permissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean hasPermission(@NonNull Context context, @NonNull String... permissions) {
        return hasPermission(context, Arrays.asList(permissions));
    }

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context     {@link Context}.
     * @param permissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean hasPermission(@NonNull Context context, @NonNull List<String> permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        for (String permission : permissions) {
            String op = AppOpsManagerCompat.permissionToOp(permission);
            if (TextUtils.isEmpty(op)) continue;
            int result = AppOpsManagerCompat.noteProxyOp(context, op, context.getPackageName());
            if (result == AppOpsManagerCompat.MODE_IGNORED) return false;
            result = ContextCompat.checkSelfPermission(context, permission);
            if (result != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }
}
