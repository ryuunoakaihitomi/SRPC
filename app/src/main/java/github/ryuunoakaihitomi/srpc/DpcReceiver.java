package github.ryuunoakaihitomi.srpc;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class DpcReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        if (Utils.isDeviceOwner(context)) {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(launchIntent);
        }
    }
}
