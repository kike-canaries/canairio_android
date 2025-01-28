package hpsaturn.pollutionreporter.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.fonfon.geohash.GeoHash;
import com.google.gson.Gson;
import com.hpsaturn.tools.FileTools;
import com.hpsaturn.tools.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import hpsaturn.pollutionreporter.Config;
import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.common.BLEHandler;
import hpsaturn.pollutionreporter.common.Keys;
import hpsaturn.pollutionreporter.common.Storage;
import hpsaturn.pollutionreporter.models.ResponseConfig;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.models.SensorTrack;
import hpsaturn.pollutionreporter.models.TrackStatus;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/17/17.
 */

public class RecordTrackService extends Service {

    private static final String TAG = RecordTrackService.class.getSimpleName();
    private static final boolean VERBOSE = Config.DEBUG && false;
    private BLEHandler bleHandler;
    private boolean isRecording;
    private RecordTrackManager recordTrackManager;

    private final int MAX_POINTS_SAVING = 3000;
    private float trackDistance = 0;
    private SensorData previousPoint;
    private long trackStartTime;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG, "[BLE] Creating Service container..");
        isRecording = Storage.getBoolean(Keys.SENSOR_RECORD, false, this);
        if (isRecording) restoreValues();
        recordTrackManager = new RecordTrackManager(this, managerListener);
        notificationSetup();
    }

    private void startConnection() {
        if (Storage.getBoolean(Keys.DEVICE_PAIR, false, this)) {
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
        String macAddress = Storage.getString(Keys.DEVICE_ADDRESS, "", this);
        Logger.i(TAG, "[BLE] deviceConnect to " + macAddress);
        bleHandler = new BLEHandler(this, macAddress, bleListener);
        bleHandler.connect();
        locationConfig();
    }

    private void locationConfig() {
        try {
            SmartLocation.with(this)
                    .location()
                    .config(LocationParams.NAVIGATION)
                    .start(onLocationListener);
        } catch (Exception e) {
            Logger.w(TAG, "[BLE][LOC] locationConfig failed: "+e.getMessage());
        }
    }

    private final OnLocationUpdatedListener onLocationListener = location -> {
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
            Logger.v(TAG, "[TRACK] starting recording");
            trackStartTime = System.currentTimeMillis()/1000;
            isRecording = true;
            notificationSetup();
        }

        @Override
        public void onServiceRecordStop() {
            if(isRecording){
                saveTrack();
                isRecording = false;
                trackDistance = 0;
                previousPoint = null;
                notificationSetup();
                Logger.v(TAG, "[TRACK] recording stop");
            }
            else
                Logger.w(TAG, "[TRACK] skipping stop recording because is not recording");

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
            if (bleHandler != null) bleHandler.readSensorData();
        }

        @Override
        public void onNotificationSetupFailure() {
            Logger.e(TAG, "[BLE] onNotificationSetupFailure");
        }

        @Override
        public void onNotificationReceived(byte[] bytes) {
            if (bleHandler != null) bleHandler.readSensorData();
            else Logger.w(TAG, "[BLE] bleHandeler is null");
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
            data.spd = ((int)((lastLocation.getSpeed()*18000)/5))/1000; // m/s to km/h (removed extra)
            bleHandler.writeTrackStatus(getTrackStatus(data));
            data.pdist = trackDistance;
        }
        else {
            Logger.w(TAG, "[TRACK] failed on getLastLocation!");
        }

        return data;
    }

    private byte[] getTrackStatus(SensorData point) {
        TrackStatus status = new TrackStatus();
        status.spd = point.spd;
        if (isRecording && previousPoint != null) {
            float[] results = new float[3];
            Location.distanceBetween(previousPoint.lat,previousPoint.lon,point.lat,point.lon,results);
            trackDistance = trackDistance + ((int)(results[0]*1000))/1000; // remove extra precision
            status.kms = trackDistance/1000;
            int[] time = getTrackTime(System.currentTimeMillis()/1000);
            status.hrs = time[0];
            status.min = time[1];
            status.seg = time[2];
        }
        return new Gson().toJson(status).getBytes();

    }

    private void record(SensorData point) {
        previousPoint = point;
        ArrayList<SensorData> data = Storage.getSensorData(this);
        Logger.i(TAG, "[TRACK] saving point sensor: " + point.dsl);
        Logger.i(TAG, "[TRACK] saving point coords: " + point.lat + "," + point.lon);
        Logger.i(TAG, "[TRACK] saving point P25: " + point.P25);
        Logger.i(TAG, "[TRACK] saving point CO2: " + point.CO2);
        data.add(point);
        Logger.i(TAG, "[TRACK] track data size: " + data.size());
        Storage.setSensorData(this, data);
        Logger.i(TAG, "[TRACK] saving track data done.");
        if (data.size()==MAX_POINTS_SAVING){
            Logger.v(TAG, "[TRACK] saving partial track..");
            saveTrack();
        }
    }

    private void restoreValues() {
        ArrayList<SensorData> data = Storage.getSensorData(this);
        if(data.isEmpty()) return;
        if(trackStartTime==0) trackStartTime = data.get(0).timestamp; // restore after service crash
        if(trackDistance==0) trackDistance = data.get(data.size()-1).pdist;
    }

    private void saveTrack() {
        Logger.i(TAG, "[TRACK] saving record track..");
        SensorTrack lastTrack = getLastTrack();
        Storage.saveTrack(this, lastTrack);
        Logger.i(TAG,"[TRACK] track: "+new Gson().toJson(lastTrack));
        saveTrackOnSD(lastTrack);
        Storage.setSensorData(this, new ArrayList<>()); // clear sensor data
        recordTrackManager.tracksUpdated();
        Logger.i(TAG, "[TRACK] record track done.");
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
        track.kms = trackDistance/1000;
        if(previousPoint!=null){
            int[] ints = getTrackTime(previousPoint.timestamp);
            track.hours = ints[0];
            track.mins  = ints[1];
            track.secs  = ints[2];
        }
        track.data = data;
        if (data.size() > 0) {
            SensorData lastSensorData = data.get(data.size() - 1);
            track.lastSensorData = lastSensorData;
            track.lastLat = lastSensorData.lat;
            track.lastLon = lastSensorData.lon;
            Location lastLocation = SmartLocation.with(this).location().getLastLocation();
            try {
                track.geohash = GeoHash.fromLocation(lastLocation, Config.GEOHASHACCU).toString();
            }
            catch (Exception e) {
                Logger.w(TAG, "[TRACK] geohash from location fails!");
            }
        }
        printTrack(track);
        return track;
    }

    private void printTrack(SensorTrack track) {
        Logger.i(TAG, "[TRACK] name: "+track.name);
        Logger.i(TAG, "[TRACK] date: "+track.date);
        Logger.i(TAG, "[TRACK] size: "+track.size);
        Logger.i(TAG, "[TRACK] kms: "+track.kms);
        Logger.i(TAG, "[TRACK] hrs: "+track.hours);
        Logger.i(TAG, "[TRACK] min: "+track.mins);
        Logger.i(TAG, "[TRACK] seg: "+track.secs);
        Logger.i(TAG, "[TRACK] lat: "+track.lastLat);
        Logger.i(TAG, "[TRACK] lon: "+track.lastLon);
        Logger.i(TAG, "[TRACK] geo: "+track.geohash);
    }


    private void saveTrackOnSD(SensorTrack track) {
        Logger.i(TAG, "[TRACK] saving track on SD..");
        String data = new Gson().toJson(track);
        new FileTools.saveDownloadFile(
                this,
                data.getBytes(),
                "canairio",
                track.name + ".json"
        ).execute();
    }

    private int [] getTrackTime(long ts) {
        long diffTime = ts - trackStartTime;

        int hours = (int) diffTime / 3600;
        int remainder = (int) diffTime - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        Logger.v(TAG,"[TRACK] track time: "+ints[0]+":"+ints[1]+":"+ints[2]);
        return ints;
    }

    private void notificationSetup() {
        PendingIntent pendingIntent = null;
        Intent notificationIntent = new Intent(this, MainActivity.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        }
        else
        {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ID = "canairio";
            String CHANNEL_NAME = "CanAirIO service";
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    isRecording ? NotificationManager.IMPORTANCE_HIGH : NotificationManager.IMPORTANCE_LOW
            );
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("CanAirIO")
                    .setContentText("Device is " + (isRecording ? "recording" : "connected"))
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentIntent(pendingIntent)
                    .setPriority(isRecording ? NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_LOW)
                    .build();
            startForeground(15, notification);
        }
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
