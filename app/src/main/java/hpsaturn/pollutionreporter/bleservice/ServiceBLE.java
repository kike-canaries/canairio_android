package hpsaturn.pollutionreporter.bleservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.hpsaturn.tools.Logger;
import com.iamhabib.easy_preference.EasyPreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import hpsaturn.pollutionreporter.AppData;
import hpsaturn.pollutionreporter.common.BLEHandler;
import hpsaturn.pollutionreporter.common.Keys;
import hpsaturn.pollutionreporter.common.Storage;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.models.SensorTrack;

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

    private final int RETRY_POLICY = 5;
    private int retry_connect = 0;
    private int retry_notify_setup = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG, "[BLE] Creating Service container..");
        prefBuilder = AppData.getPrefBuilder(this);
        isRecording = prefBuilder.getBoolean(Keys.SENSOR_RECORD, false);
        serviceManager = new ServiceManager(this, managerListener);
        noticationChannelAPI26issue();
    }

    private void noticationChannelAPI26issue() {
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "preporter";
            String CHANNEL_NAME = "PollutionReporter";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();
            startForeground(1, notification);
        }
    }

    private void startConnection() {
        if (prefBuilder.getBoolean(Keys.DEVICE_PAIR, false)) {
            if (bleHandler == null) {
                connect();
            } else if (bleHandler.isConnected()) Logger.i(TAG, "[BLE] already connected!");
            else connect();
        } else Logger.w(TAG, "[BLE] not BLE connection (not MAC register)");
    }

    private void connect() {
        Logger.i(TAG, "[BLE] starting BLE connection..");
        String macAddress = prefBuilder.getString(Keys.DEVICE_ADDRESS, "");
        Logger.i(TAG, "[BLE] deviceConnect to " + macAddress);
        bleHandler = new BLEHandler(this, macAddress, bleListener);
        bleHandler.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "[BLE] onStartCommand");
        serviceManager.status(ServiceManager.STATUS_SERVICE_OK);
        if (isRecording) startConnection();
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
            Logger.d(TAG, "[BLE] request service start..");
            startConnection();
        }

        @Override
        public void onServiceStop() {
            Logger.w(TAG, "[BLE] request service stop..");
            if (bleHandler != null && !isRecording) bleHandler.triggerDisconnect();
            else if (isRecording) Logger.w(TAG, "[BLE] isRecording override stop BLE service");
        }

        @Override
        public void onServiceData(byte[] bytes) {

        }

        @Override
        public void onSensorRecord() {
            isRecording = true;
        }

        @Override
        public void onSensorRecordStop() {
            isRecording = false;
            saveTrack();
        }

        @Override
        public void onTracksUpdated() {

        }
    };


    /*********************************************************************
     * B L U E T O O T H   I N T E R F A C E
     *********************************************************************/

    private BLEHandler.OnBLEConnectionListener bleListener = new BLEHandler.OnBLEConnectionListener() {
        @Override
        public void onConnectionSuccess() {
            serviceManager.status(ServiceManager.STATUS_BLE_START);
            retry_connect = 0;
        }

        @Override
        public void onConectionFailure() {
            serviceManager.status(ServiceManager.STATUS_BLE_FAILURE);
            if (retry_connect++ < RETRY_POLICY) {
                Logger.w(TAG, "[BLE] retry connection on failure.." + retry_connect);
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
            Logger.e(TAG, "[BLE] onNotificationSetupFailure");
            if (retry_notify_setup++ < RETRY_POLICY) {
                Logger.w(TAG, "[BLE] retry notify setup.." + retry_notify_setup);
                bleHandler.setupNotification();
            }
        }

        @Override
        public void onNotificationReceived(byte[] bytes) {
            if (isRecording) record(bytes);
            Logger.d(TAG, "[BLE] pushing data..");
            serviceManager.pushData(bytes);
            retry_notify_setup = 0;
        }
    };

    private void record(byte[] bytes) {
        String strdata = new String(bytes);
        Logger.d(TAG, "[BLE] saving sensor data: " + strdata);
        SensorData item = new Gson().fromJson(strdata, SensorData.class);
        ArrayList<SensorData> data = Storage.getSensorData(this);
        item.timestamp = System.currentTimeMillis() / 1000;
        data.add(item);
        Logger.d(TAG, "[BLE] data size: " + data.size());
        Storage.setSensorData(this, data);
        Logger.d(TAG, "[BLE] saving sensor data done.");
    }

    private void saveTrack(){
        Logger.i(TAG, "[BLE] saving record track..");
        Storage.saveTrack(this,getLastTrack());
        Storage.setSensorData(this,new ArrayList<>()); // clear sensor data
        serviceManager.tracksUpdated();
        Logger.i(TAG, "[BLE] record track done.");
    }

    private SensorTrack getLastTrack(){
        ArrayList<SensorData> data = Storage.getSensorData(this);
        SensorTrack track = new SensorTrack();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd.kkmmss", Locale.ENGLISH);
        String formattedDate = df.format(c);
        track.setName(formattedDate);
        track.date = "points: "+data.size();
        track.data = data;
        return track;
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
