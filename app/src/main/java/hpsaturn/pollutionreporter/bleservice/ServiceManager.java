package hpsaturn.pollutionreporter.bleservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.gson.Gson;
import com.hpsaturn.tools.Logger;

import hpsaturn.pollutionreporter.models.SensorConfig;
import hpsaturn.pollutionreporter.models.SensorData;

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
    private String action_service_record;
    private String action_service_record_stop;
    private String action_sensor_read_config;
    private String action_sensor_read_data;
    private String action_sensor_write_config;
    private String action_tracks_updated;
    private String response_sensor_config;

    private static final String KEY_SERVICE_DATA = "KEY_SERVICE_DATA";
    private static final String KEY_SERVICE_STATUS = "KEY_SERVICE_STATUS";
    private static final String KEY_SENSOR_CONFIG_WRITE = "KEY_SENSOR_CONFIG_WRITE";
    private static final String KEY_SENSOR_CONFIG_RESPONSE = "KEY_SENSOR_CONFIG_RESPONSE";

    public static final String STATUS_BLE_START   = "STATUS_BLE_START";
    public static final String STATUS_BLE_STOP    = "STATUS_BLE_STOP";
    public static final String STATUS_BLE_FAILURE = "STATUS_BLE_FAILURE";
    public static final String STATUS_SERVICE_OK  = "STATUS_SERVICE_OK";

    public ServiceManager(Context ctx, ServiceInterface listener) {

        this.ctx = ctx;
        this.listener = listener;

        try {
            IntentFilter intentFilter = new IntentFilter();
            action_start   = "ACTION_SERVICE_START";
            action_stop    = "ACTION_SERVICE_STOP";
            action_push    = "ACTION_SERVICE_PUSH";
            action_status  = "ACTION_SERVICE_STATUS";

            action_service_record = "ACTION_SERVICE_RECORD";
            action_service_record_stop = "ACTION_SERVICE_RECORD_STOP";
            action_tracks_updated      = "ACTION_TRACKS_UPDATED";
            action_sensor_read_config  = "ACTION_SENSOR_READ_CONFIG";
            action_sensor_read_data    = "ACTION_SENSOR_READ_DATA";
            action_sensor_write_config = "ACTION_SENSOR_WRITE_CONFIG";
            response_sensor_config  = "RESPONSE_SENSOR_CONFIG";

            intentFilter.addAction(action_start);
            intentFilter.addAction(action_stop);
            intentFilter.addAction(action_push);
            intentFilter.addAction(action_status);
            intentFilter.addAction(action_service_record);
            intentFilter.addAction(action_service_record_stop);
            intentFilter.addAction(action_tracks_updated);
            intentFilter.addAction(action_sensor_read_config);
            intentFilter.addAction(action_sensor_read_data);
            intentFilter.addAction(action_sensor_write_config);
            intentFilter.addAction(response_sensor_config);

            ctx.registerReceiver(mReceiver,intentFilter);

        } catch (Exception e) {
            Logger.w(TAG,e.getMessage());
            e.printStackTrace();
        }
    }

    public void start(){
        Intent intent = new Intent(action_start);
        ctx.sendBroadcast(intent);
    }

    public void stop(){
        Intent intent = new Intent(action_stop);
        ctx.sendBroadcast(intent);
    }

    public void sensorNotificationData(SensorData data){
        Intent intent = new Intent(action_push);
        intent.putExtra(KEY_SERVICE_DATA,new Gson().toJson(data));
        ctx.sendBroadcast(intent);
    }

    public void status(String status){
        Intent intent = new Intent(action_status);
        intent.putExtra(KEY_SERVICE_STATUS,status);
        ctx.sendBroadcast(intent);
    }

    public void serviceRecord() {
        Intent intent = new Intent(action_service_record);
        ctx.sendBroadcast(intent);
    }

    public void serviceRecordStop() {
        Intent intent = new Intent(action_service_record_stop);
        ctx.sendBroadcast(intent);
    }

    public void tracksUpdated() {
        Intent intent = new Intent(action_tracks_updated);
        ctx.sendBroadcast(intent);
    }

    public void readSensorConfig() {
        Intent intent = new Intent(action_sensor_read_config);
        ctx.sendBroadcast(intent);
    }

    public void responseSensorConfig(SensorConfig config){
        Intent intent = new Intent(response_sensor_config);
        intent.putExtra(KEY_SENSOR_CONFIG_RESPONSE,new Gson().toJson(config));
        ctx.sendBroadcast(intent);
    }

    public void readSensorData() {
        Intent intent = new Intent(action_sensor_read_data);
        ctx.sendBroadcast(intent);
    }

    public void writeSensorConfig(SensorConfig config) {
        Intent intent = new Intent(action_sensor_write_config);
        intent.putExtra(KEY_SENSOR_CONFIG_WRITE,new Gson().toJson(config));
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

                String data = intent.getExtras().getString(KEY_SERVICE_DATA);
                listener.onSensorNotificationData(new Gson().fromJson(data,SensorData.class));

            } else if(action.equals(action_service_record)) {

                listener.onServiceRecord();

            } else if(action.equals(action_service_record_stop)) {

                listener.onServiceRecordStop();

            } else if(action.equals(action_tracks_updated)) {

                listener.onTracksUpdated();

            } else if(action.equals(action_sensor_read_config)) {

                listener.requestSensorConfigRead();

            } else if(action.equals(response_sensor_config)) {

                String config = intent.getExtras().getString(KEY_SENSOR_CONFIG_RESPONSE);
                listener.onSensorConfigRead(new Gson().fromJson(config, SensorConfig.class));

            } else if(action.equals(action_sensor_read_data)) {

                listener.requestSensorDataRead();

            } else if(action.equals(action_sensor_write_config)) {

                String config = intent.getExtras().getString(KEY_SENSOR_CONFIG_WRITE);
                listener.onSensorConfigWrite(new Gson().fromJson(config, SensorConfig.class));

            }

        }
    };

}
