package hpsaturn.pollutionreporter.bleservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by Antonio Vanegas @hpsaturn on 7/2/18.
 */
public class ServiceManager {

    public static String TAG = ServiceManager.class.getSimpleName();

    private Context ctx;
    private ServiceInterface listener;
    private String action_start;
    private String action_stop;
    private String action_push;
    private String action_status;

    private static String KEY_SERVICE_DATA = "KEY_SERVICE_DATA";
    private static String KEY_SERVICE_STATUS = "KEY_SERVICE_STATUS";

    public static String STATUS_BLE_START   = "STATUS_BLE_START";
    public static String STATUS_BLE_STOP    = "STATUS_BLE_STOP";
    public static String STATUS_BLE_FAILURE = "STATUS_BLE_FAILURE";
    public static String STATUS_SERVICE_OK  = "STATUS_SERVICE_OK";

    public ServiceManager(Context ctx, ServiceInterface listener) {

        this.ctx = ctx;
        this.listener = listener;

        IntentFilter intentFilter = new IntentFilter();
        action_start   = "ACTION_SERVICE_START";
        action_stop    = "ACTION_SERVICE_STOP";
        action_push    = "ACTION_SERVICE_PUSH";
        action_status  = "ACTION_SERVICE_STATUS";

        intentFilter.addAction(action_start);
        intentFilter.addAction(action_stop);
        intentFilter.addAction(action_push);
        intentFilter.addAction(action_status);

        ctx.registerReceiver(mReceiver,intentFilter);
    }

    public void start(){
        Intent intent = new Intent(action_start);
        ctx.sendBroadcast(intent);
    }

    public void stop(){
        Intent intent = new Intent(action_stop);
        ctx.sendBroadcast(intent);
    }

    public void pushData(byte[] bytes){
        Intent intent = new Intent(action_push);
        intent.putExtra(KEY_SERVICE_DATA,bytes);
        ctx.sendBroadcast(intent);
    }

    public void status(String status){
        Intent intent = new Intent(action_status);
        intent.putExtra(KEY_SERVICE_STATUS,status);
        ctx.sendBroadcast(intent);
    }

    public void unregister() {
        ctx.unregisterReceiver(mReceiver);
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if(action.equals(action_start)) {

                listener.onServiceStart();

            } else if(action.equals(action_stop)) {

                listener.onServiceStop();

            } else if(action.equals(action_status)) {

                String status = intent.getExtras().getString(KEY_SERVICE_STATUS);
                listener.onServiceStatus(status);

            } else if(action.equals(action_push)) {

                byte[] bytes = intent.getExtras().getByteArray(KEY_SERVICE_DATA);
                listener.onServiceData(bytes);

            }

        }
    };
}
