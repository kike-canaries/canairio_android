package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.text.DecimalFormat;

import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.ApiConfig;
import hpsaturn.pollutionreporter.models.CommandConfig;
import hpsaturn.pollutionreporter.models.GeoConfig;
import hpsaturn.pollutionreporter.models.InfluxdbConfig;
import hpsaturn.pollutionreporter.models.ResponseConfig;
import hpsaturn.pollutionreporter.models.SampleConfig;
import hpsaturn.pollutionreporter.models.SensorConfig;
import hpsaturn.pollutionreporter.models.SensorName;
import hpsaturn.pollutionreporter.models.WifiConfig;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by Antonio Vanegas @hpsaturn on 2/17/19.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    private String  ifxdb, ifxip;
    private boolean onInfluxDBConfigChanged;
    private boolean onAPIConfigChanged;
    private Location lastLocation;
    private Snackbar snackBar;
    private ResponseConfig lastDeviceConfig;

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

//        sname = getSharedPreference(getString(R.string.key_setting_dname));
//        apiusr = getSharedPreference(getString(R.string.key_setting_apiusr));
//        apipss = getSharedPreference(getString(R.string.key_setting_apipss));
//        apisrv = getSharedPreference(getString(R.string.key_setting_apisrv));
//        apiuri = getSharedPreference(getString(R.string.key_setting_apiuri));
//        ssid = getSharedPreference(getString(R.string.key_setting_ssid));
//        pass = getSharedPreference(getString(R.string.key_setting_pass));
        ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
        ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
//        stime = getCurrentStime();

        String feedback = getSharedPreference(getString(R.string.key_send_feedback));

//        feedback.(preference -> {
//            UITools.viewLink(getMain(),getString(R.string.url_canairio_feedback));
//            return true;
//        });

        rebuildUI();
    }


    public void rebuildUI(){
        getPreferenceScreen().removeAll();
        addPreferencesFromResource(R.xml.settings);
        refreshUI();
    }

    public void refreshUI(){
        updateSensorNameSummary();
        updateStimeSummary();
        updateLocationSummary();
        updateApiHostSummary();
        updateApiUriSummary();
//        validateWifiSwitch();
//        validateApiSwitch();
        validateIfxdbSwitch();
        validateLocationSwitch();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMain().getRecordTrackManager().readSensorConfig();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.key_setting_dname))) {
            saveSensorName(getSensorName());
        } else if (key.equals(getString(R.string.key_setting_stime))) {
            validateSensorSampleTime();
        } else if (key.equals(getString(R.string.key_setting_ssid))) {
            saveWifiConfig();
        } else if (key.equals(getString(R.string.key_setting_pass))) {
            saveWifiConfig();
        } else if (key.equals(getString(R.string.key_setting_enable_wifi))) {
            saveWifiConfig();
        } else if (key.equals(getString(R.string.key_setting_apiusr))) {
            saveApiConfig();
        } else if (key.equals(getString(R.string.key_setting_apipss))) {
            saveApiConfig();
        } else if (key.equals(getString(R.string.key_setting_enable_api))) {
            saveApiConfig();
        } else if (key.equals(getString(R.string.key_setting_enable_ifx))) {
            saveInfluxConfig(key);
        } else if (key.equals(getString(R.string.key_setting_enable_reboot))) {
            performRebootDevice();
        } else if (key.equals(getString(R.string.key_setting_enable_clear))) {
            performClearDevice();
        } else if (key.equals(getString(R.string.key_setting_enable_location))) {
            saveLocation();
        }

//        refreshUI();
    }

    /***********************************************************************************************
     * Sensor name section
     **********************************************************************************************/

    private void saveSensorName(String name) {
        if(name.length() > 0 ) {
            saveSharedPreference(R.string.key_setting_dname,name);
            SensorName config = new SensorName();
            config.dname = name;
            getMain().getRecordTrackManager().writeSensorConfig(new Gson().toJson(config));
        }else  {
            resetSensorName();
        }
        updateSensorNameSummary();
    }

    String getSensorName() {
        return getSharedPreference(getString(R.string.key_setting_dname));
    }

    void resetSensorName() {
        if (lastDeviceConfig!=null && lastDeviceConfig.dname.length() > 0) {
            saveSensorName(lastDeviceConfig.dname);
            rebuildUI();
        }
    }


    private void updateSensorNameSummary() {
        updateSummary(R.string.key_setting_dname);
    }

    /***********************************************************************************************
     * Sample time handlers
     **********************************************************************************************/

    private void validateSensorSampleTime() {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_stime));
        if (getCurrentStime() >= 5) {
            saveSensorSampleTime(getCurrentStime());
        }else if (lastDeviceConfig!=null) {
            resetStimeValue(lastDeviceConfig.stime);
        }
    }

    private void saveSensorSampleTime(int time) {
        Logger.v(TAG, "[Config] sending sensor time: "+time);
        SampleConfig config = new SampleConfig();
        config.stime = time;
        updateSummary(R.string.key_setting_stime,getStimeFormat(config.stime));
        getMain().getRecordTrackManager().writeSensorConfig(new Gson().toJson(config));
    }

    private int getCurrentStime() {
        try {
            return Integer.parseInt(getSharedPreference(getString(R.string.key_setting_stime)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void resetStimeValue(int stime) {
        saveSharedPreference(R.string.key_setting_stime, "" + stime);
        rebuildUI();
    }

    String getStimeFormat(int time){
        return "" + time + " seconds";
    }

    private void updateStimeSummary() {
        updateSummary(R.string.key_setting_stime,getStimeFormat(getCurrentStime()));
    }


    /***********************************************************************************************
     * Validate Switches
     **********************************************************************************************/

    private void validateIfxdbSwitch() {
        SwitchPreference ifxdbSwitch = findPreference(getString(R.string.key_setting_enable_ifx));
        String old_ifxdb = ifxdb;
        String old_ifxip = ifxip;
        ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
        ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
        updateIfxdbSummmary();

        ifxdbSwitch.setEnabled(!(ifxdb.length() == 0 || ifxip.length() == 0));

        if (!(old_ifxdb.equals(ifxdb) && old_ifxip.equals(ifxip))) {
            ifxdbSwitch.setChecked(false);   // TODO: force user to enable again?
            onInfluxDBConfigChanged = true;
        }
    }

    private void validateLocationSwitch() {
        SwitchPreference locationSwitch = findPreference(getString(R.string.key_setting_enable_location));
        locationSwitch.setEnabled(lastLocation!=null);
    }

    /***********************************************************************************************
     * Save Switches
     **********************************************************************************************/

    private void saveWifiConfig() {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_enable_wifi));

        if (getWifiSwitch().isChecked()) {
            String ssid = getSharedPreference(getString(R.string.key_setting_ssid));
            String pass = getSharedPreference(getString(R.string.key_setting_pass));
            if(ssid.length()==0 || pass.length() == 0) return;
            WifiConfig config = new WifiConfig();
            config.ssid = ssid;
            config.pass = pass;
            config.wenb = true;
            Logger.v(TAG, "[Config] writing wifi credentials..");
            getMain().getRecordTrackManager().writeSensorConfig(new Gson().toJson(config));
        }
        else
            setWifiSwitch(false);
    }

    private void setWifiSwitch(boolean checked) {
        SwitchPreference wifiSwitch = getWifiSwitch();
        String ssid = getSharedPreference(getString(R.string.key_setting_ssid));
        String pass = getSharedPreference(getString(R.string.key_setting_pass));
        updateSummary(R.string.key_setting_ssid);
        wifiSwitch.setEnabled(!(ssid.length() == 0 || pass.length() == 0));
        wifiSwitch.setChecked(checked);
        enableWifiOnDevice(checked);
    }

    private SwitchPreference getWifiSwitch() {
        return findPreference(getString(R.string.key_setting_enable_wifi));
    }

    private void enableWifiOnDevice(boolean enable) {
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "wst";
        config.wenb = enable;
        getMain().getRecordTrackManager().writeSensorConfig(new Gson().toJson(config));
    }

    private void saveApiConfig() {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_enable_api));

        if (getApiSwitch().isChecked()) {
            String api_usr = getSharedPreference(getString(R.string.key_setting_apiusr));
            String api_pss = getSharedPreference(getString(R.string.key_setting_apipss));
            String api_srv = getSharedPreference(getString(R.string.key_setting_apisrv));
            String api_uri = getSharedPreference(getString(R.string.key_setting_apiuri));
            if(api_usr.length() == 0 || api_pss.length() == 0 || api_srv.length() == 0) return;
            ApiConfig config = new ApiConfig();
            config.apiusr = api_usr;
            config.apipss = api_pss;
            config.apisrv = api_srv;
            config.apiuri = api_uri;
            config.apiprt = 80;  // TODO: sending via UI
            config.aenb = true;
            Logger.v(TAG, "[Config] writing API credentials..");
            getMain().getRecordTrackManager().writeSensorConfig(new Gson().toJson(config));
        } else
            setApiSwitch(false);
    }

    private void setApiSwitch(boolean checked) {
        SwitchPreference apiSwitch = getApiSwitch();
        String apiusr = getSharedPreference(getString(R.string.key_setting_apiusr));
        String apipss = getSharedPreference(getString(R.string.key_setting_apipss));
        String apisrv = getSharedPreference(getString(R.string.key_setting_apisrv));
        String apiuri = getSharedPreference(getString(R.string.key_setting_apiuri));

        updateApiUsrSummmary();
        updateApiUriSummary();
        updateApiHostSummary();

        apiSwitch.setEnabled(!(apiusr.length() == 0 || apipss.length() == 0));
        apiSwitch.setChecked(checked);
        enableApiOnDevice(checked);
    }

    private SwitchPreference getApiSwitch() {
        return findPreference(getString(R.string.key_setting_enable_api));
    }

    private void enableApiOnDevice(boolean enable) {
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "ast";
        config.aenb = enable;
        getMain().getRecordTrackManager().writeSensorConfig(new Gson().toJson(config));
    }

    private void saveInfluxConfig(String key) {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_ifxdb));
        SwitchPreference switchPreference = findPreference(key);

        if (switchPreference.isChecked()) {
            String ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
            String ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
            if(ifxdb.length() == 0 || ifxip.length() == 0) return;
            getMain().showSnackMessage(R.string.msg_save_config_ifx);
            InfluxdbConfig config = new InfluxdbConfig();
            config.ifxdb = ifxdb;
            config.ifxip = ifxip;
            Logger.v(TAG, "[Config] writing InfluxDb settings..");
            getMain().getRecordTrackManager().writeSensorConfig(new Gson().toJson(config));
        } else if (!onInfluxDBConfigChanged) {
            disableInfluxDB();
        } else {
            Logger.d(TAG, "[Config] onInfluxDBConfigChanged skip disable Influx.");
            onInfluxDBConfigChanged = false;
        }
    }

    private void disableInfluxDB() {
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "ist";
        config.ienb = false;
        getMain().getRecordTrackManager().writeSensorConfig(new Gson().toJson(config));
    }

    /***********************************************************************************************
     * Misc preferences section
     **********************************************************************************************/

    private void saveLocation() {
        SwitchPreference locationSwitch = findPreference(getString(R.string.key_setting_enable_location));
        if(lastLocation != null) {
            if(locationSwitch.isChecked()) {
                snackBar = getMain().getSnackBar(R.string.msg_set_current_location, R.string.bt_location_save_action, view -> {
                    getMain().showSnackMessage(R.string.msg_save_location);
                    GeoConfig config = new GeoConfig();
                    config.lat = lastLocation.getLatitude();
                    config.lon = lastLocation.getLongitude();
                    config.alt = lastLocation.getAltitude();
                    config.spd = lastLocation.getSpeed();
                    getMain().getRecordTrackManager().writeSensorConfig(new Gson().toJson(config));
                    Handler handler = new Handler();
                    handler.postDelayed(() -> locationSwitch.setChecked(false), 2000);
                });
                snackBar.show();
            }
            else{
                snackBar.dismiss();
            }
        }
        else {
            getMain().showSnackMessage(R.string.msg_save_location_failed);
        }
        updateLocationSummary();
    }

    private void updateLocationSummary() {
        if (lastLocation != null) {
            Preference pref;
            pref = findPreference(getString(R.string.key_setting_enable_location));
            DecimalFormat precision = new DecimalFormat("0.000");
            String accu = "Accu:" + (int) lastLocation.getAccuracy() + "m ";
            String lat = "(" + precision.format(lastLocation.getLatitude());
            String lon = "," + precision.format(lastLocation.getLongitude()) + ")";
            pref.setSummary(accu + lat + lon);
        }
    }

    private void performRebootDevice() {
        SwitchPreference rebootSwitch = findPreference(getString(R.string.key_setting_enable_reboot));
        if (!rebootSwitch.isChecked()) {
            if(snackBar!=null)snackBar.dismiss();
        } else {
            snackBar = getMain().getSnackBar(R.string.bt_device_reboot, R.string.bt_device_reboot_action, view -> {
                rebootSwitch.setChecked(false);
                CommandConfig config = new CommandConfig();
                config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
                config.act = "rbt";
                getMain().showSnackMessageSlow(R.string.msg_device_reboot);
                getMain().getRecordTrackManager().writeSensorConfig(new Gson().toJson(config));
                Handler handler = new Handler();
                handler.postDelayed(() -> getMain().finish(), 3000);
            });
            snackBar.show();
        }
    }

    private void performClearDevice() {
        SwitchPreference clearSwitch = findPreference(getString(R.string.key_setting_enable_clear));
        if (!clearSwitch.isChecked()) {
            if(snackBar!=null)snackBar.dismiss();
        } else {
            snackBar = getMain().getSnackBar(R.string.bt_device_clear, R.string.bt_device_clear_action, view -> {
                clearSwitch.setChecked(false);
                CommandConfig config = new CommandConfig();
                config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
                config.act = "cls";
                getMain().showSnackMessageSlow(R.string.msg_device_clear);
                getMain().getRecordTrackManager().writeSensorConfig(new Gson().toJson(config));
                clearSharedPreferences();
                Handler handler = new Handler();
                handler.postDelayed(() -> getMain().finish(), 3000);
            });
            snackBar.show();
        }
    }

    /***********************************************************************************************
     * Update methods
     **********************************************************************************************/

    public void configCallBack(ResponseConfig config) {
        if (config != null) {
            printResponseConfig(config);
            lastDeviceConfig = config;

            FirebaseCrashlytics.getInstance().setCustomKey(getString(R.string.crashkey_device_name),""+config.dname);
            FirebaseCrashlytics.getInstance().setCustomKey(getString(R.string.crashkey_device_wmac),""+config.wmac);
            FirebaseCrashlytics.getInstance().setCustomKey(getString(R.string.crashkey_api_usr),""+config.apiusr);

            boolean notify_sync = false;

            if (!getStimeFormat(config.stime).equals(getStimeFormat(getCurrentStime()))){
                resetStimeValue(config.stime);
                notify_sync = true;
            }
            if (!config.dname.equals(getSensorName())){
                saveSensorName(config.dname);
                notify_sync = true;
            }
            if (config.wenb != getWifiSwitch().isChecked()) setWifiSwitch(config.wenb);
//            if (config.ienb != getWifiSwitch().isChecked()) setWifiSwitch(config.wenb);
            if (config.aenb != getApiSwitch().isChecked()) setApiSwitch(config.wenb);

            if (notify_sync) {
                getMain().showSnackMessage(R.string.msg_sync_complete);
                rebuildUI();
            }
//            updatePreferencesSummmary(config);
//            updateSwitches(config);
            saveAllPreferences(config);
//            rebuildUI();
        }
    }

    private void printResponseConfig(ResponseConfig config) {
        Logger.i(TAG, "[Config] callback values:");
        Logger.i(TAG, "dname: " + config.dname);
        Logger.i(TAG, "stime: " + config.stime);
        Logger.i(TAG, "stype: " + config.stype);
        Logger.i(TAG, "ssid:  " + config.ssid);
        Logger.i(TAG, "wifien:" + config.wenb);
        Logger.i(TAG, "ifxdb: " + config.ifxdb);
        Logger.i(TAG, "ifxip: " + config.ifxip);
        Logger.i(TAG, "ifxen: " + config.ienb);
        Logger.i(TAG, "apiusr:" + config.apiusr);
        Logger.i(TAG, "apisrv:" + config.apisrv);
        Logger.i(TAG, "apiuri:" + config.apiuri);
        Logger.i(TAG, "apiprt:" + config.apiprt);
        Logger.i(TAG, "apien: " + config.aenb);
        Logger.i(TAG, "wmac:  " + config.wmac);
        Logger.i(TAG, "lskey: " + config.lskey);
        Logger.i(TAG, "wsta:  " + config.wsta);
    }


    private void updatePreferencesSummmary(ResponseConfig config) {
        if(config.dname !=null)updateSummary(R.string.key_setting_dname,config.dname);
        if(config.apiusr !=null)updateSummary(R.string.key_setting_apiusr,config.apiusr);
        if(config.apisrv !=null)updateSummary(R.string.key_setting_apisrv,config.apisrv);
        if(config.apiuri !=null)updateSummary(R.string.key_setting_apiuri,config.apiuri);
        if(config.ssid !=null)updateSummary(R.string.key_setting_ssid,config.ssid);
        if(config.ifxdb !=null)updateSummary(R.string.key_setting_ifxdb,config.ifxdb);
        if(config.ifxip !=null)updateSummary(R.string.key_setting_ifxip,config.ifxip);
        if(config.stime>0)updateSummary(R.string.key_setting_stime, getStimeFormat(config.stime));
        updateLocationSummary();
    }

    private void updateSummary(int key){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getMain());
        String skey = getString(key);
        Preference pref = findPreference(skey);
        String value = sharedPref.getString(skey, "");
        pref.setSummary(value);
    }

    private void updateSummary(int key, String msg){
        Preference pref = findPreference(getString(key));
        pref.setSummary(msg);
    }

    private void updateIfxdbSummmary() {
        Preference pref;
        pref = findPreference(getString(R.string.key_setting_ifxdb));
        pref.setSummary(ifxdb);
        pref = findPreference(getString(R.string.key_setting_ifxip));
        pref.setSummary(ifxip);
    }

    private void updateApiUsrSummmary() {
        updateSummary(R.string.key_setting_apiusr);
    }

    private void updateApiHostSummary() {
        updateSummary(R.string.key_setting_apisrv);
    }

    private void updateApiUriSummary() {
        updateSummary(R.string.key_setting_apiuri);
    }

    private void updateSwitch(int key,boolean enable){
        SwitchPreference mSwitch = findPreference(getString(key));
        mSwitch.setChecked(enable);
    }

    private void updateSwitches(SensorConfig config){
        updateSwitch(R.string.key_setting_enable_wifi,config.wenb);
        updateSwitch(R.string.key_setting_enable_ifx,config.ienb);
        updateSwitch(R.string.key_setting_enable_api,config.aenb);
    }

    /***********************************************************************************************
     * Update Preferences methods
     **********************************************************************************************/

    private void saveAllPreferences(ResponseConfig config) {
//        saveSharedPreference(R.string.key_setting_dname, config.dname);
//        saveSharedPreference(R.string.key_setting_ssid, config.ssid);
//        saveSharedPreference(R.string.key_setting_ifxdb, config.ifxdb);
//        saveSharedPreference(R.string.key_setting_ifxip, config.ifxip);
//        saveSharedPreference(R.string.key_setting_stime, "" + config.stime);
        saveSharedPreference(R.string.key_setting_wmac, "" + config.wmac);
//        saveSharedPreference(R.string.key_setting_enable_wifi,config.wenb);
//        saveSharedPreference(R.string.key_setting_enable_ifx,config.ienb);
//        saveSharedPreference(R.string.key_setting_enable_api,config.aenb);
    }

    private void saveSharedPreference(int key, String value) {
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

    private String getSharedPreference(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        return preferences.getString(key, "");
    }

    private void clearSharedPreferences(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        preferences.edit().clear().apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        lastLocation = SmartLocation.with(getActivity()).location().getLastLocation();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        refreshUI();
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private MainActivity getMain() {
        return ((MainActivity) getActivity());
    }

}
