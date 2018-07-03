package hpsaturn.pollutionreporter.bleservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.hpsaturn.tools.Logger;
import com.iamhabib.easy_preference.EasyPreference;

import hpsaturn.pollutionreporter.AppData;
import hpsaturn.pollutionreporter.BLEHandler;
import hpsaturn.pollutionreporter.Keys;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/17/17.
 */

public class ServiceBLE extends Service {

    private static final String TAG = ServiceBLE.class.getSimpleName();
    private EasyPreference.Builder prefBuilder;
    private BLEHandler bleHandler;
    private boolean isDevicePaired;
    private ServiceManager serviceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG,"[BLE] Creating Service container..");
        prefBuilder = AppData.getPrefBuilder(this);
        serviceManager = new ServiceManager(this,managerListener);
    }

    private void startConnection(){
        if (prefBuilder.getBoolean(Keys.DEVICE_PAIR, false)) {
            Logger.i(TAG,"[BLE] starting BLE connection..");
            String macAddress = prefBuilder.getString(Keys.DEVICE_ADDRESS,"");
            Logger.i(TAG, "[BLE] deviceConnect to " + macAddress);
            bleHandler = new BLEHandler(this,macAddress,bleListener);
            bleHandler.connect();
        }
        else Logger.w(TAG,"[BLE] not BLE connection (not MAC register)");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG,"[BLE] onStartCommand");
        serviceManager.status(ServiceManager.STATUS_SERVICE_OK);
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
            if(bleHandler!=null)bleHandler.triggerDisconnect();
        }

        @Override
        public void onServiceData(byte[] bytes) {

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
            serviceManager.pushData(bytes);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
