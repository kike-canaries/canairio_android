package hpsaturn.pollutionreporter.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.hpsaturn.tools.FileTools;
import com.hpsaturn.tools.Logger;
import com.hpsaturn.tools.UITools;
import com.iamhabib.easy_preference.EasyPreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import hpsaturn.pollutionreporter.AppData;
import hpsaturn.pollutionreporter.Config;
import hpsaturn.pollutionreporter.common.BLEHandler;
import hpsaturn.pollutionreporter.common.Keys;
import hpsaturn.pollutionreporter.common.Storage;
import hpsaturn.pollutionreporter.models.ResponseConfig;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.models.SensorTrack;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/17/17.
 */

public class RecordTrackService extends Service {

    private static final String TAG = RecordTrackService.class.getSimpleName();
    private static final boolean VERBOSE = Config.DEBUG && false;
    private EasyPreference.Builder prefBuilder;
    private BLEHandler bleHandler;
    private boolean isRecording;
    private RecordTrackManager recordTrackManager;

    private final int RETRY_POLICY = 5;
    private final int MAX_POINTS_SAVING = 3000;

    private int retry_connect = 0;
    private int retry_notify_setup = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG, "[BLE] Creating Service container..");
        prefBuilder = AppData.getPrefBuilder(this);
        isRecording = prefBuilder.getBoolean(Keys.SENSOR_RECORD, false);
        recordTrackManager = new RecordTrackManager(this, managerListener);
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
            }
            else if (bleHandler.isConnected()) {
                Logger.i(TAG, "[BLE] already connected!");
                recordTrackManager.status(RecordTrackManager.STATUS_BLE_START);
            }
            else connect();
        } else Logger.w(TAG, "[BLE] not BLE connection (not MAC register)");
    }

    private void connect() {
        Logger.i(TAG, "[BLE] starting BLE connection..");
        String macAddress = prefBuilder.getString(Keys.DEVICE_ADDRESS, "");
        Logger.i(TAG, "[BLE] deviceConnect to " + macAddress);
        bleHandler = new BLEHandler(this, macAddress, bleListener);
        bleHandler.connect();
        localitationConfig();
    }

    private void localitationConfig () {
        SmartLocation.with(this)
                .location()
                .config(LocationParams.NAVIGATION)
                .start(onLocationListener);
    }

    private OnLocationUpdatedListener onLocationListener = location -> {
        if (VERBOSE) {
            Logger.i(TAG, "[BLE][LOC] onLocationUpdated");
            Logger.i(TAG, "[BLE][LOC] accuracy: " + location.getAccuracy());
            Logger.i(TAG, "[BLE][LOC] coords  : " + location.getLatitude() + "," + location.getLongitude());
            Logger.i(TAG, "[BLE][LOC] speed: " + location.getSpeed());
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "[BLE] onStartCommand");
        recordTrackManager.status(RecordTrackManager.STATUS_SERVICE_OK);
        startConnection();
        if (isRecording) {
            return START_STICKY;
        } else {
            return START_NOT_STICKY;
        }
    }


    /*********************************************************************
     * S E R V I C E  I N T E R F A C E
     *********************************************************************/

    private RecordTrackInterface managerListener = new RecordTrackInterface() {
        @Override
        public void onServiceStatus(String status) {

        }

        @Override
        public void onServiceStart() {
        }

        @Override
        public void onServiceStop() {
            stopService();
        }

        @Override
        public void onSensorNotificationData(SensorData data) {

        }

        @Override
        public void onServiceRecord() {
            isRecording = true;
        }

        @Override
        public void onServiceRecordStop() {
            isRecording = false;
            saveTrack();
        }

        @Override
        public void onTracksUpdated() {
        }

        @Override
        public void requestSensorConfigRead() {
            if (bleHandler != null) bleHandler.readSensorConfig();
        }

        @Override
        public void requestSensorDataRead() {
            if (bleHandler != null) bleHandler.readSensorData();
        }

        @Override
        public void onSensorConfigRead(ResponseConfig config) {

        }

        @Override
        public void onSensorDataRead(SensorData data) {

        }

        @Override
        public void onSensorConfigWrite(String config) {
            if (bleHandler != null) bleHandler.writeSensorConfig(config.getBytes());
        }

    };

    private void killSerivices(){
        Logger.w(TAG, "[BLE] stoping location and recording service..");
        SmartLocation.with(RecordTrackService.this).location().stop();
        RecordTrackScheduler.stopSheduleService(this);
        this.stopSelf();
    }

    private void stopService() {
        Logger.w(TAG, "[BLE] request service stop..");
        if (bleHandler != null && !isRecording) {
            Logger.w(TAG, "[BLE] kill service..");
            bleHandler.triggerDisconnect();
            bleHandler=null;
            killSerivices();
        }
        else if (isRecording){
            Logger.w(TAG, "[BLE] isRecording override stop BLE service");
        }
        else {
            Logger.w(TAG, "[BLE] any device is connected, stop services..");
            killSerivices();
        }
    }


    /*********************************************************************
     * B L U E T O O T H   I N T E R F A C E
     *********************************************************************/

    private BLEHandler.OnBLEConnectionListener bleListener = new BLEHandler.OnBLEConnectionListener() {
        @Override
        public void onConnectionSuccess() {
            recordTrackManager.status(RecordTrackManager.STATUS_BLE_START);
            retry_connect = 0;
        }

        @Override
        public void onConectionFailure() {
            Logger.e(TAG, "[BLE] onConectionFailure");
            recordTrackManager.status(RecordTrackManager.STATUS_BLE_FAILURE);
        }

        @Override
        public void onConnectionFinished() {
            recordTrackManager.status(RecordTrackManager.STATUS_BLE_STOP);
        }

        @Override
        public void onNotificationSetup() {

        }

        @Override
        public void onNotificationSetupFailure() {
            Logger.e(TAG, "[BLE] onNotificationSetupFailure");
        }

        @Override
        public void onNotificationReceived(byte[] bytes) {
            if (bleHandler != null) bleHandler.readSensorData();
            else Logger.w(TAG, "[BLE] bleHandeler is null");
            retry_notify_setup = 0;
        }

        @Override
        public void onSensorConfigRead(byte[] bytes) {
            String strdata = new String(bytes);
            ResponseConfig config = new Gson().fromJson(strdata, ResponseConfig.class);
            recordTrackManager.responseSensorConfig(config);
        }

        @Override
        public void onSensorDataRead(byte[] bytes) {
            SensorData data = getSensorData(bytes);
            if (isRecording) record(data);
            recordTrackManager.responseSensorData(data);
        }

        @Override
        public void onReadFailure() {
            Logger.e(TAG, "[BLE] onReadFailure");
        }

        @Override
        public void onWriteFailure() {

        }
    };

    private SensorData getSensorData(byte[] bytes) {
        String strdata = new String(bytes);
        SensorData data = new Gson().fromJson(strdata, SensorData.class);
        data.timestamp = System.currentTimeMillis() / 1000;
        Location lastLocation = SmartLocation.with(this).location().getLastLocation();
        if (lastLocation != null) {
            data.lat = lastLocation.getLatitude();
            data.lon = lastLocation.getLongitude();
        }
        else {
            Logger.w(TAG, "[BLE] failed on getLastLocation!i");
            UITools.showToast(this,"Getting GPS localization failed!" );
        }

        return data;
    }

    private void record(SensorData point) {
        ArrayList<SensorData> data = Storage.getSensorData(this);
        Logger.d(TAG, "[BLE] saving point with coords: " + point.lat + "," + point.lon);
        data.add(point);
        Logger.d(TAG, "[BLE] track data size: " + data.size());
        Storage.setSensorData(this, data);
        Logger.d(TAG, "[BLE] saving track data done.");
        if (data.size()==MAX_POINTS_SAVING){
            Logger.d(TAG, "[BLE] saving partial track..");
            saveTrack();
        }
    }

    private void saveTrack() {
        Logger.i(TAG, "[BLE] saving record track..");
        SensorTrack lastTrack = getLastTrack();
        Storage.saveTrack(this, lastTrack);
        saveTrackOnSD(lastTrack);
        Storage.setSensorData(this, new ArrayList<>()); // clear sensor data
        recordTrackManager.tracksUpdated();
        Logger.i(TAG, "[BLE] record track done.");
    }

    private void saveTrackOnSD(SensorTrack track) {
        Logger.i(TAG, "[BLE] saving track on SD..");
        String data = new Gson().toJson(track);
        new FileTools.saveDownloadFile(
                data.getBytes(),
                "canairio",
                track.name + ".json"
        ).execute();
    }

    private SensorTrack getLastTrack() {
        ArrayList<SensorData> data = Storage.getSensorData(this);
        SensorTrack track = new SensorTrack();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat dfName = new SimpleDateFormat("yyyyMMddkkmmss", Locale.ENGLISH);
        SimpleDateFormat dfDate = new SimpleDateFormat("LLL, EE dd", Locale.ENGLISH);
        String nameDate = dfName.format(c);
        String date = dfDate.format(c);
        track.setName(nameDate);
        track.size = data.size();
        track.date = date;
        track.data = data;
        if (data.size() > 0) {
            SensorData lastSensorData = data.get(data.size() - 1);
            track.lastSensorData = lastSensorData;
            track.lastLat = lastSensorData.lat;
            track.lastLon = lastSensorData.lon;
        }
        return track;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (bleHandler != null) {
            bleHandler.triggerDisconnect();
            bleHandler = null;
        }
        recordTrackManager.unregister();
        super.onDestroy();
    }
}
