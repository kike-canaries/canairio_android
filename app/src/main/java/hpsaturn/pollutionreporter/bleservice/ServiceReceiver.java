package hpsaturn.pollutionreporter.bleservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hpsaturn.tools.Logger;

/**
 * Created by Antonio Vanegas @hpsaturn on 3/24/17.
 */

public class ServiceReceiver extends BroadcastReceiver {
    public static final String TAG = ServiceReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d(TAG,"StartServiceReceiver: onReceive");
        Intent service = new Intent(context, ServiceBLE.class);
        context.startService(service);
    }

}
