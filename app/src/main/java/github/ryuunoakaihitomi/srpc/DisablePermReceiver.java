package github.ryuunoakaihitomi.srpc;

import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DisablePermReceiver extends BroadcastReceiver {

    private static final String TAG = "DisablePermReceiver";

    public static Intent start(Context context, int uid) {
        Intent i = new Intent(context, DisablePermReceiver.class);
        i.putExtra(Intent.EXTRA_UID, uid);
        return i;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
        Log.d(TAG, "onReceive: uid = " + uid);
        for (String pkg : context.getPackageManager().getPackagesForUid(uid)) {
            Utils.setStoragePermission(context, pkg, DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED);
        }
        context.getSystemService(NotificationManager.class).cancel(uid);
    }
}
