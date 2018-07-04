package hpsaturn.pollutionreporter.bleservice;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hpsaturn.tools.Logger;
import com.iamhabib.easy_preference.EasyPreference;

import java.lang.reflect.Type;
import java.util.ArrayList;

import hpsaturn.pollutionreporter.AppData;
import hpsaturn.pollutionreporter.BLEHandler;
import hpsaturn.pollutionreporter.Keys;
import hpsaturn.pollutionreporter.models.SensorData;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/17/17.
 */

public class ServiceBLE extends Service {

    private static final String TAG = ServiceBLE.class.getSimpleName();
    private EasyPreference.Builder prefBuilder;
    private BLEHandler bleHandler;
    private boolean isRecording;
    private ServiceManager serviceManager;

    private ArrayList<SensorData> buffer = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG,"[BLE] Creating Service container..");
        prefBuilder = AppData.getPrefBuilder(this);
        isRecording=prefBuilder.getBoolean(Keys.SENSOR_RECORD,false);
        serviceManager = new ServiceManager(this,managerListener);
    }

    private void startConnection(){
        if (prefBuilder.getBoolean(Keys.DEVICE_PAIR, false)) {
            if(bleHandler==null) {
                connect();
            }
            else if(bleHandler.isConnected())Logger.i(TAG,"[BLE] already connected!");
            else connect();
        }
        else Logger.w(TAG,"[BLE] not BLE connection (not MAC register)");
    }

    private void connect(){
        Logger.i(TAG, "[BLE] starting BLE connection..");
        String macAddress = prefBuilder.getString(Keys.DEVICE_ADDRESS, "");
        Logger.i(TAG, "[BLE] deviceConnect to " + macAddress);
        bleHandler = new BLEHandler(this, macAddress, bleListener);
        bleHandler.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG,"[BLE] onStartCommand");
        serviceManager.status(ServiceManager.STATUS_SERVICE_OK);
        if(isRecording)startConnection();
        return START_STICKY;
    }


    private ServiceInterface managerListener = new ServiceInterface() {
        @Override
        public void onServiceStatus(String status) {

        }

        @Override
        public void onServiceStart() {
            Logger.d(TAG,"[BLE] request service start..");
            startConnection();
        }

        @Override
        public void onServiceStop() {
            Logger.w(TAG,"[BLE] request service stop..");
            if(bleHandler!=null&&!isRecording)bleHandler.triggerDisconnect();
            else if (isRecording) Logger.w(TAG,"[BLE] isRecording override stop BLE service");
        }

        @Override
        public void onServiceData(byte[] bytes) {

        }

        @Override
        public void onSensorRecord() {
           isRecording=true;
        }

        @Override
        public void onSensorRecordStop() {
            isRecording=false;
        }
    };


    private BLEHandler.OnBLEConnectionListener bleListener = new BLEHandler.OnBLEConnectionListener() {
        @Override
        public void onConnectionSuccess() {
            serviceManager.status(ServiceManager.STATUS_BLE_START);
        }

        @Override
        public void onConectionFailure() {
           serviceManager.status(ServiceManager.STATUS_BLE_FAILURE);
           Logger.w(TAG,"[BLE] retry connection on failure..");
           startConnection(); // R E T R Y
        }

        @Override
        public void onConnectionFinished() {
            serviceManager.status(ServiceManager.STATUS_BLE_STOP);
        }

        @Override
        public void onNotificationSetup() {

        }

        @Override
        public void onNotificationSetupFailure() {
            bleHandler.setupNotification();
        }

        @Override
        public void onNotificationReceived(byte[] bytes) {
            if(isRecording)record(bytes);
            Logger.d(TAG,"[BLE] pushing data..");
            serviceManager.pushData(bytes);
        }
    };

    private void record(byte[] bytes){
        String strdata = new String(bytes);
        Logger.i(TAG, "[BLE] data to record: " + strdata);
        SensorData item = new Gson().fromJson(strdata, SensorData.class);
        buffer.add(item);
        if(buffer.size()>10){
            Logger.i(TAG, "[BLE] saving buffer..");
            ArrayList<SensorData> data = getData();
            data.addAll(buffer);
            Logger.i(TAG, "[BLE] data size: "+data.size());
            setData(data);
            buffer.clear();
            Logger.i(TAG, "[BLE] saving buffer done.");
        }
    }

    // TODO: unify the next methods to prefBuilder schema or database schema

    public void setData( ArrayList<SensorData> items) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Keys.SENSOR_DATA, new Gson().toJson(items));
        editor.commit();
    }

    public ArrayList<SensorData> getData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String ringJson = preferences.getString(Keys.SENSOR_DATA, "");
        if (ringJson.equals("")) return new ArrayList<>();
        else {
            Type listType = new TypeToken<ArrayList<SensorData>>() {
            }.getType();
            return new Gson().fromJson(ringJson, listType);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
