package hpsaturn.pollutionreporter.bleservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.gson.Gson;
import com.hpsaturn.tools.Logger;
import com.iamhabib.easy_preference.EasyPreference;

import java.util.ArrayList;

import hpsaturn.pollutionreporter.AppData;
import hpsaturn.pollutionreporter.common.BLEHandler;
import hpsaturn.pollutionreporter.common.Keys;
import hpsaturn.pollutionreporter.common.Storage;
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

    private final int RETRY_POLICY   = 5;
    private int retry_connect        = 0;
    private int retry_notify_setup   = 0;

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


    /*********************************************************************
     * S E R V I C E  I N T E R F A C E
     *********************************************************************/

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


    /*********************************************************************
     * B L U E T O O T H   I N T E R F A C E
     *********************************************************************/

    private BLEHandler.OnBLEConnectionListener bleListener = new BLEHandler.OnBLEConnectionListener() {
        @Override
        public void onConnectionSuccess() {
            serviceManager.status(ServiceManager.STATUS_BLE_START);
            retry_connect=0;
        }

        @Override
        public void onConectionFailure() {
           serviceManager.status(ServiceManager.STATUS_BLE_FAILURE);
           if(retry_connect++<RETRY_POLICY) {
               Logger.w(TAG,"[BLE] retry connection on failure.."+retry_connect);
               startConnection();
           }
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
            Logger.e(TAG,"[BLE] onNotificationSetupFailure");
            if(retry_notify_setup++<RETRY_POLICY) {
                Logger.w(TAG,"[BLE] retry notify setup.."+retry_notify_setup);
                bleHandler.setupNotification();
            }
        }

        @Override
        public void onNotificationReceived(byte[] bytes) {
            if(isRecording)record(bytes);
            Logger.d(TAG,"[BLE] pushing data..");
            serviceManager.pushData(bytes);
            retry_notify_setup=0;
        }
    };

    private void record(byte[] bytes){
        String strdata = new String(bytes);
        Logger.i(TAG, "[BLE] data to record: " + strdata);
        SensorData item = new Gson().fromJson(strdata, SensorData.class);
        buffer.add(item);
        if(buffer.size()>10){
            Logger.i(TAG, "[BLE] saving buffer..");
            ArrayList<SensorData> data = Storage.getData(this);
            data.addAll(buffer);
            Logger.i(TAG, "[BLE] data size: "+data.size());
            Storage.setData(this,data);
            buffer.clear();
            Logger.i(TAG, "[BLE] saving buffer done.");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        serviceManager.unregister();
        super.onDestroy();
    }
}
