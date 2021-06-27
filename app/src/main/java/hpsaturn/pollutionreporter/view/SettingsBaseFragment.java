package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.hpsaturn.tools.Logger;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.ResponseConfig;
import hpsaturn.pollutionreporter.models.SensorConfig;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.service.RecordTrackInterface;
import hpsaturn.pollutionreporter.service.RecordTrackManager;

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
        updateSummary(R.string.key_device_status, status ? "Connected":"Disconnected");
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

    public void updateSwitch(int key, boolean enable){
        SwitchPreference mSwitch = findPreference(getString(key));
        mSwitch.setChecked(enable);
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
        Logger.i(TAG, "[Config] apiusr: " + config.apiusr);
        Logger.i(TAG, "[Config] apisrv: " + config.apisrv);
        Logger.i(TAG, "[Config] apiuri: " + config.apiuri);
        Logger.i(TAG, "[Config] apiprt: " + config.apiprt);
        Logger.i(TAG, "[Config] -----------------------------");
        Logger.i(TAG, "[Config] wifien: " + config.wenb);
        Logger.i(TAG, "[Config] ifdben: " + config.ienb);
        Logger.i(TAG, "[Config] apien : " + config.aenb);
        Logger.i(TAG, "[Config] -----------------------------");
        Logger.i(TAG, "[Config] wifist: " + config.wsta);
        Logger.i(TAG, "[Config] wmac  : " + config.wmac);
        Logger.i(TAG, "[Config] vrev  : " + config.vrev);
        Logger.i(TAG, "[Config] vmac  : " + config.vmac);
        Logger.i(TAG, "[Config] vflv  : " + config.vflv);
        Logger.i(TAG, "[Config] vtag  : " + config.vtag);
        Logger.i(TAG, "[Config] lskey : " + config.lskey);
        Logger.i(TAG, "[Config] toffset : " + config.toffset);
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

    String getSharedPreference(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        return preferences.getString(key, "");
    }

    String getSharedPreference(String key, String defvalue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        return preferences.getString(key, defvalue);
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
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
