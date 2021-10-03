package github.ryuunoakaihitomi.srpc;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class Utils {

    private static final String TAG = "Utils";

    public static final Class<?> CLASS_ADMIN_RECEIVER = DpcReceiver.class;

    private Utils() {
    }

    /**
     * @see PackageManager#GET_PERMISSIONS
     */
    public static boolean hasStoragePermission(PackageInfo info) {
        if (info.requestedPermissions == null) return false;
        List<String> reqPermList = Arrays.asList(info.requestedPermissions);
        return reqPermList.contains(READ_EXTERNAL_STORAGE) || reqPermList.contains(WRITE_EXTERNAL_STORAGE);
    }

    /**
     * @param state @link DevicePolicyManager#PermissionGrantState
     */
    public static void setStoragePermission(Context context, String pkg, int state) {
        DevicePolicyManager dpm = context.getSystemService(DevicePolicyManager.class);
        ComponentName receiver = new ComponentName(context, CLASS_ADMIN_RECEIVER);
        boolean rResult = dpm.setPermissionGrantState(receiver, pkg, READ_EXTERNAL_STORAGE, state);
        boolean wResult = dpm.setPermissionGrantState(receiver, pkg, WRITE_EXTERNAL_STORAGE, state);
        Log.d(TAG, "setStoragePermission: " + pkg + ": " + state + " |R: " + rResult + " W: " + wResult);
    }

    public static boolean isDeviceOwner(Context context) {
        return context.getSystemService(DevicePolicyManager.class).isDeviceOwnerApp(BuildConfig.APPLICATION_ID);
    }
}
