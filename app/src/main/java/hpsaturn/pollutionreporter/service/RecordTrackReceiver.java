package hpsaturn.pollutionreporter.service;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;

import com.hpsaturn.tools.Logger;

import hpsaturn.pollutionreporter.PermissionUtil;

/**
 * Created by Antonio Vanegas @hpsaturn on 3/24/17.
 */

public class RecordTrackReceiver extends BroadcastReceiver {
    public static final String TAG = RecordTrackReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Logger.d(TAG, "[BLE] StartServiceReceiver: onReceive..");
            Intent service = new Intent(context, RecordTrackService.class);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    String permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
//                    int res = context.checkCallingOrSelfPermission(permission);
//                    boolean cp_bg_loc = (res == PackageManager.PERMISSION_GRANTED);
//                    if (!cp_bg_loc) Logger.w(TAG, "[BLE] StartServiceReceiver: FAILED!");
//                    if (!cp_bg_loc) return;
//                }
//                ContextCompat.startForegroundService(context, service);
//            } else {
//                context.startService(service);
//            }
//            if (PermissionUtil.hasForegroundLocationPermission(context)).
                context.startService(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
