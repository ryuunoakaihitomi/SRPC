package github.ryuunoakaihitomi.srpc;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Intent.ACTION_PACKAGE_ADDED;
import static android.content.Intent.EXTRA_REPLACING;
import static android.content.Intent.EXTRA_UID;
import static android.os.Build.VERSION_CODES.M;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class PackageAddedReceiver extends BroadcastReceiver {

    private static final String TAG = "PackageAddedReceiver";

    @Override
    public void onReceive(Context c, Intent intent) {
        if (!ACTION_PACKAGE_ADDED.equals(intent.getAction())) return;
        if (!intent.getBooleanExtra(EXTRA_REPLACING, false)) {

            int uid = intent.getIntExtra(EXTRA_UID, -1);
            Log.d(TAG, "onReceive: uid = " + uid);

            if (shouldShowNotice(c, uid)) {
                PendingIntent pi = PendingIntent.getBroadcast(c, 0, DisablePermReceiver.start(c, uid), FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT);
                Notification.Action action = new Notification.Action.Builder(null, c.getString(R.string.disable), pi).build();
                Notification notification = new Notification.Builder(c)
                        .setSmallIcon(android.R.drawable.btn_plus)
                        .setContentTitle(c.getString(R.string.noti_title_new_pkg_added))
                        .setContentText(c.getString(R.string.noti_content_new_pkg_added))
                        .addAction(action)
                        .setFullScreenIntent(pi, true)
                        .build();
                c.getSystemService(NotificationManager.class).notify(uid, notification);
            }
        }
    }

    private boolean shouldShowNotice(Context context, int uid) {
        if (!Utils.isDeviceOwner(context)) {
            return false;
        }
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_key_notify_new_pkg), false)) {
            return false;
        }
        PackageManager pm = context.getPackageManager();
        for (String pkg : pm.getPackagesForUid(uid)) {
            try {
                if (pm.getApplicationInfo(pkg, 0).targetSdkVersion >= M && Utils.hasStoragePermission(pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS))) {
                    return true;
                }
            } catch (PackageManager.NameNotFoundException ignore) {
            }
        }
        return false;
    }
}
