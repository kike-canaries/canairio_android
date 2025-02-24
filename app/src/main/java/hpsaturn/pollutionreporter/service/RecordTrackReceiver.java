package hpsaturn.pollutionreporter.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.hpsaturn.tools.Logger;

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
            context.startService(service);
        } catch (Exception e) {
            Logger.w(TAG, "[BLE] StartServiceReceiver: FAILED!");
            throw new RuntimeException(e);
        }
    }
}
