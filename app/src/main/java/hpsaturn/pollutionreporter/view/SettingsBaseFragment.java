package hpsaturn.pollutionreporter.view;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.github.pwittchen.reactivewifi.AccessRequester;
import com.github.pwittchen.reactivewifi.ReactiveWifi;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.hpsaturn.tools.Logger;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.util.ArrayList;
import java.util.List;

import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.common.Storage;
import hpsaturn.pollutionreporter.models.ResponseConfig;
import hpsaturn.pollutionreporter.models.SensorConfig;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.service.RecordTrackInterface;
import hpsaturn.pollutionreporter.service.RecordTrackManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Antonio Vanegas @hpsaturn on 3/31/21.
 */
public abstract class SettingsBaseFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String TAG = SettingsBaseFragment.class.getSimpleName();

    public Location lastLocation;
    public Snackbar snackBar;
    public boolean onSensorReading;
    private RecordTrackManager trackManager;
    private boolean deviceConnected;
    private Disposable wifiSubscription;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(trackManager==null) trackManager = new RecordTrackManager(getMain(), recordTrackListener);
        readSensorConfig();
    }

    public void readSensorConfig(){
        getMain().getRecordTrackManager().readSensorConfig();
    }

    /***********************************************************************************************
     * Sensor status section
     **********************************************************************************************/

    public void setStatusSwitch(boolean checked) {
        Logger.i(TAG,"setStatusSwitch: "+checked);
        SwitchPreference statusSwitch = getStatusSwitch();
        statusSwitch.setEnabled(checked);
        statusSwitch.setChecked(checked);
        updateStatusSummary(checked);
    }

    private SwitchPreference getStatusSwitch() {
        return findPreference(getString(R.string.key_device_status));
    }

    void updateStatusSummary(boolean status){
        updateSummary(R.string.key_device_status,
                status ? getString(R.string.summary_key_device_status_connected) :
                         getString(R.string.summary_key_device_status_disconnected));
    }

    void updateSummary(int key){
        String value = getSharedPreference(getString(key));
        updateSummary(key,value);
    }

    void updateSummary(int key, String msg){
        Preference pref = findPreference(getString(key));
        pref.setSummary(msg);
    }

    void updatePrefTitle(int key, String msg){
        Preference pref = findPreference(getString(key));
        pref.setTitle(msg);
    }

    void updateSummary(int key, int msg){
        Preference pref = findPreference(getString(key));
        pref.setSummary(getString(msg));
    }

    public void updateSwitch(int key, boolean checked){
        SwitchPreference mSwitch = findPreference(getString(key));
        mSwitch.setChecked(checked);
    }

    public void enableSwitch(int key, boolean enabled){
        SwitchPreference mSwitch = findPreference(getString(key));
        mSwitch.setEnabled(enabled);
    }

    Preference findPreference(int key) {
        return findPreference(getString(key));
    }

    MainActivity getMain() {
        return ((MainActivity) getActivity());
    }

    void printResponseConfig(ResponseConfig config) {
        Logger.i(TAG, "[Config] Callback values:");
        Logger.i(TAG, "[Config] dname:  " + config.dname);
        Logger.i(TAG, "[Config] stime:  " + config.stime);
        Logger.i(TAG, "[Config] stype:  " + config.stype);
        Logger.i(TAG, "[Config] ssid:   " + config.ssid);
        Logger.i(TAG, "[Config] ifxdb:  " + config.ifxdb);
        Logger.i(TAG, "[Config] ifxip:  " + config.ifxip);
        Logger.i(TAG, "[Config] ifxpt:  " + config.ifxpt);
        Logger.i(TAG, "[Config] -----------------------------");
        Logger.i(TAG, "[Config] wifien: " + config.wenb);
        Logger.i(TAG, "[Config] ifdben: " + config.ienb);
        Logger.i(TAG, "[Config] wifist: " + config.wsta);
        Logger.i(TAG, "[Config] wmac  : " + config.wmac);
        Logger.i(TAG, "[Config] -----------------------------");
        Logger.i(TAG, "[Config] vrev  : " + config.vrev);
        Logger.i(TAG, "[Config] vmac  : " + config.vmac);
        Logger.i(TAG, "[Config] vflv  : " + config.vflv);
        Logger.i(TAG, "[Config] vtag  : " + config.vtag);
        Logger.i(TAG, "[Config] lskey : " + config.lskey);
        Logger.i(TAG, "[Config] toffset : " + config.toffset);
        Logger.i(TAG, "[Config] sse   : " + config.sse);
        Logger.i(TAG, "[Config] dstime: " + config.deepSleep);
    }


    public void sendSensorConfig(SensorConfig config) {
        Logger.v(TAG, "[Config] writing sensor config: "+config.getClass().getName());
        getMain().getRecordTrackManager().writeSensorConfig(new Gson().toJson(config));
    }


    void saveSharedPreference(int key, String value) {
        saveSharedPreference(getString(key), value);
    }

    private void saveSharedPreference(String key, String value) {
        if(value!=null && value.length()!=0) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }

    private void saveSharedPreference(int key, boolean enable) {
        String skey = getString(key);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(skey, enable);
        editor.apply();
    }

    String getSharedPreference(int key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        return preferences.getString(getString(key), "");
    }

    String getSharedPreference(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        return preferences.getString(key, "");
    }

    String getSharedPreference(String key, String defvalue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        return preferences.getString(key, defvalue);
    }

    String getSharedPreference(int key, String defvalue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        return preferences.getString(getString(key), defvalue);
    }

    void clearSharedPreferences(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        preferences.edit().clear().apply();
    }

    private RecordTrackInterface recordTrackListener = new RecordTrackInterface() {
        @Override
        public void onServiceStatus(String status) {
            if (status.equals(RecordTrackManager.STATUS_BLE_START)) {
                deviceConnected = true;
            } else if (status.equals(RecordTrackManager.STATUS_SERVICE_OK)){
            } else if (status.equals(RecordTrackManager.STATUS_BLE_FAILURE)) {
                deviceConnected = false;
            }
            setStatusSwitch(deviceConnected);
        }

        @Override
        public void onServiceStart() {

        }

        @Override
        public void onServiceStop() {

        }

        @Override
        public void onSensorNotificationData(SensorData data) {

        }

        @Override
        public void onServiceRecord() {

        }

        @Override
        public void onServiceRecordStop() {

        }

        @Override
        public void onTracksUpdated() {

        }

        @Override
        public void requestSensorConfigRead() {

        }

        @Override
        public void requestSensorDataRead() {

        }

        @Override
        public void onSensorConfigRead(ResponseConfig config) {
            if (config != null) {
                onSensorReading = true;
                saveSharedPreference(R.string.key_setting_wmac, "" + config.wmac);
                FirebaseCrashlytics.getInstance().setCustomKey(getString(R.string.crashkey_device_name),""+config.dname);
                FirebaseCrashlytics.getInstance().setCustomKey(getString(R.string.crashkey_device_wmac),""+config.wmac);
                FirebaseCrashlytics.getInstance().setCustomKey(getString(R.string.crashkey_api_usr),""+config.apiusr);
                setStatusSwitch(true);
                onConfigRead(config);
            }
            onSensorReading = false;
        }

        @Override
        public void onSensorDataRead(SensorData data) {

        }

        @Override
        public void onSensorConfigWrite(String config) {

        }
    };

    /***********************************************************************************************
     * Background service for scan APs
     **********************************************************************************************/

    public void startWifiAccessPointsSubscription() {

        boolean fineLocationPermissionNotGranted =
                ActivityCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) != PERMISSION_GRANTED;
        boolean coarseLocationPermissionNotGranted =
                ActivityCompat.checkSelfPermission(getActivity(), ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED;

        if (fineLocationPermissionNotGranted && coarseLocationPermissionNotGranted) {
            return;
        }

        if (!AccessRequester.isLocationEnabled(getActivity())) {
            AccessRequester.requestLocationAccess(getActivity());
            return;
        }

        wifiSubscription = ReactiveWifi.observeWifiAccessPoints(getActivity())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::saveAccessPoints);
    }

    private void saveAccessPoints(List<ScanResult> scanResults) {
        final List<String> ssids = new ArrayList<>();

        for (ScanResult scanResult : scanResults) {
            ssids.add(scanResult.SSID);
        }
        if (!ssids.isEmpty() && wifiSubscription !=null) {
            Storage.setTempAPList(getActivity(), ssids);
            wifiSubscription.dispose();
            wifiSubscription = null;
        }
    }

    protected abstract void refreshUI();

    protected abstract void onConfigRead(ResponseConfig config);

    @Override
    public void onDestroy() {
        Logger.i(TAG, "[Config] onDestroy");
        if(trackManager!=null)trackManager.unregister();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        startWifiAccessPointsSubscription();
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        if (wifiSubscription != null ) wifiSubscription.dispose();
        super.onPause();
    }
}
