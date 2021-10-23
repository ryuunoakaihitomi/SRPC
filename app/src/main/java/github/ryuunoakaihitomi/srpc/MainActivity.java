package github.ryuunoakaihitomi.srpc;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Convert2Lambda")
public class MainActivity extends Activity {

    private DevicePolicyManager dpm;
    private final ComponentName r = new ComponentName(BuildConfig.APPLICATION_ID, Utils.CLASS_ADMIN_RECEIVER.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_title);
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!Utils.isDeviceOwner(this)) {
            menu.findItem(R.id.menu_disable_admin).setVisible(false);
            menu.findItem(R.id.menu_pref).setEnabled(false);
        }
        return true;
    }

    @SuppressLint("QueryPermissionsNeeded")
    @Override
    protected void onStart() {
        super.onStart();

        dpm = getSystemService(DevicePolicyManager.class);

        ListView listView = findViewById(R.id.list);
        TextView tips = findViewById(R.id.tips);

        if (!Utils.isDeviceOwner(this)) {
            tips.setVisibility(View.VISIBLE);
            final String command = "adb shell dpm set-device-owner " + r.flattenToShortString();
            tips.setText(R.string.device_admin_note);
            tips.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = getSystemService(ClipboardManager.class);
                    clipboard.setPrimaryClip(ClipData.newPlainText(null, command));
                    Toast.makeText(getApplication(), command, Toast.LENGTH_SHORT).show();
                }
            });
            listView.setVisibility(View.GONE);

        } else {

            PackageManager pm = getPackageManager();
            List<ItemInfo> itemInfoList = new ArrayList<>();
            boolean appListed = false;

            for (PackageInfo pkgInfo : pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
                ApplicationInfo appInfo = pkgInfo.applicationInfo;

                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && appInfo.targetSdkVersion >= Build.VERSION_CODES.M) {

                    if (!Utils.hasStoragePermission(pkgInfo)) continue;

                    ItemInfo item = new ItemInfo();
                    String packageName = pkgInfo.packageName;
                    item.icon = appInfo.loadIcon(pm);
                    item.packageName = packageName;
                    item.label = pkgInfo.applicationInfo.loadLabel(pm);
                    item.rState = getStoragePermissionGrantState(packageName, false);
                    item.wState = getStoragePermissionGrantState(packageName, true);
                    itemInfoList.add(item);
                    appListed = true;
                }
            }

            if (!appListed) {
                tips.setVisibility(View.VISIBLE);
                tips.setText(R.string.empty_app_list_note);
                return;
            }

            ItemInfoAdapter adapter = new ItemInfoAdapter(this, itemInfoList);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ItemInfo info = itemInfoList.get(position);
                    String packageName = info.packageName;

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(info.label)
                            .setSingleChoiceItems(R.array.perm_state, getStoragePermissionGrantState(packageName, false), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Utils.setStoragePermission(getApplication(), packageName, which);
                                    info.rState = getStoragePermissionGrantState(packageName, false);
                                    info.wState = getStoragePermissionGrantState(packageName, true);
                                    adapter.notifyDataSetChanged();
                                    dialog.cancel();
                                }
                            }).show();
                }
            });
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    String packageName = itemInfoList.get(position).packageName;
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", packageName, null);
                    intent.setData(uri);
                    startActivity(intent);
                    return true;
                }
            });
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pref:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.menu_disable_admin:
                new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault)
                        .setTitle(R.string.alert_title_disable_admin)
                        .setMessage(R.string.alert_msg_disable_admin)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Utils.isDeviceOwner(getApplication())) {
                                    dpm.clearDeviceOwnerApp(BuildConfig.APPLICATION_ID);
                                }
                                dpm.removeActiveAdmin(r);
                                finish();
                            }
                        })
                        .show();
                break;
            case R.id.menu_about:
                final String url = "https://github.com/ryuunoakaihitomi/srpc";
                Intent i = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
                try {
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Browser.sendString(this, url);
                }
                break;
        }
        return true;
    }

    private int getStoragePermissionGrantState(String pkgName, boolean isWritePerm) {
        return dpm.getPermissionGrantState(r, pkgName, isWritePerm ? WRITE_EXTERNAL_STORAGE : READ_EXTERNAL_STORAGE);
    }
}
