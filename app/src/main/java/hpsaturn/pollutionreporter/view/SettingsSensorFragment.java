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
import androidx.preference.ListPreference;
import androidx.preference.Preference;
//import androidx.preference.PreferenceFragmentCompat;
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
import hpsaturn.pollutionreporter.models.SensorType;
import hpsaturn.pollutionreporter.models.WifiConfig;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by Antonio Vanegas @hpsaturn on 2/17/19.
 */

public class SettingsSensorFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = SettingsSensorFragment.class.getSimpleName();

    private Location lastLocation;
    private Snackbar snackBar;
    private ResponseConfig lastDeviceConfig;

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

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
        updateInfluxDbSummmary();
        updateInfluxPortSummary();
        validateLocationSwitch();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        readSensorConfig();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.key_setting_dname))) {
            saveSensorName(getSensorName());
        } else if (key.equals(getString(R.string.key_setting_stime))) {
            validateSensorSampleTime();
        } else if (key.equals(getString(R.string.key_setting_dtype))) {
            sendSensorTypeConfig();
        } else if (key.equals(getString(R.string.key_setting_ssid))) {
            getWifiSwitch().setEnabled(isWifiSwitchFieldsValid());
        } else if (key.equals(getString(R.string.key_setting_pass))) {
            getWifiSwitch().setEnabled(isWifiSwitchFieldsValid());
        } else if (key.equals(getString(R.string.key_setting_enable_wifi))) {
            saveWifiConfig();
        } else if (key.equals(getString(R.string.key_setting_apiusr))) {
            getApiSwitch().setEnabled(isApiSwitchFieldsValid());
        } else if (key.equals(getString(R.string.key_setting_apipss))) {
            getApiSwitch().setEnabled(isApiSwitchFieldsValid());
        } else if (key.equals(getString(R.string.key_setting_apisrv))) {
            getApiSwitch().setEnabled(isApiSwitchFieldsValid());
        } else if (key.equals(getString(R.string.key_setting_enable_api))) {
            saveApiConfig();
        } else if (key.equals(getString(R.string.key_setting_ifxdb))) {
            getInfluxDbSwitch().setEnabled(isInfluxDbSwitchFieldsValid());
        } else if (key.equals(getString(R.string.key_setting_ifxip))) {
            getInfluxDbSwitch().setEnabled(isInfluxDbSwitchFieldsValid());
        } else if (key.equals(getString(R.string.key_setting_enable_ifx))) {
            saveInfluxDbConfig();
        } else if (key.equals(getString(R.string.key_setting_enable_reboot))) {
            performRebootDevice();
        } else if (key.equals(getString(R.string.key_setting_enable_clear))) {
            performClearDevice();
        } else if (key.equals(getString(R.string.key_setting_enable_location))) {
            saveLocation();
        }

    }


    /***********************************************************************************************
     * Sensor name section
     **********************************************************************************************/

    private void saveSensorName(String name) {
        if(name.length() > 0 ) {
            saveSharedPreference(R.string.key_setting_dname,name);
            SensorName config = new SensorName();
            config.dname = name;
            sendSensorConfig(config);
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

    private void sendSensorTypeConfig() {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_dtype));
        getMain().showSnackMessage(R.string.msg_reboot_manually);
        SensorType config = new SensorType();
        config.stype = getSensorType();
        sendSensorConfig(config);
    }

    private void updateSensorTypeSummary(int value) {
        ListPreference sizePreference = findPreference(getString(R.string.key_setting_dtype));
        sizePreference.setValueIndex(value);
    }

    private int getSensorType() {
        try {
            return Integer.parseInt(getSharedPreference(getString(R.string.key_setting_dtype)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
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
        sendSensorConfig(config);
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
     * Wifi switch
     **********************************************************************************************/

    private void saveWifiConfig() {

        if (getWifiSwitch().isChecked() && isWifiSwitchFieldsValid()) {
            WifiConfig config = new WifiConfig();
            config.ssid = getSharedPreference(getString(R.string.key_setting_ssid));
            config.pass = getSharedPreference(getString(R.string.key_setting_pass));
            config.wenb = true;
            sendSensorConfig(config);
        }
        else
            setWifiSwitch(false);
    }

    private boolean isWifiSwitchFieldsValid() {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_enable_wifi));
        String ssid = getSharedPreference(getString(R.string.key_setting_ssid));
        String pass = getSharedPreference(getString(R.string.key_setting_pass));
        Logger.v(TAG, "[Config] values -> " + ssid );
        wifiSummaryUpdate();
        return !(ssid.length() == 0 || pass.length() == 0);
    }

    private void setWifiSwitch(boolean checked) {
        SwitchPreference wifiSwitch = getWifiSwitch();
        wifiSwitch.setEnabled(isWifiSwitchFieldsValid());
        wifiSwitch.setChecked(checked);
        wifiSummaryUpdate();
        enableWifiOnDevice(checked);
    }

    private void wifiSummaryUpdate(){
        updateSummary(R.string.key_setting_ssid);
        updatePasswSummary(R.string.key_setting_pass);
    }

    private SwitchPreference getWifiSwitch() {
        return findPreference(getString(R.string.key_setting_enable_wifi));
    }

    private void enableWifiOnDevice(boolean enable) {
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "wst";
        config.wenb = enable;
        sendSensorConfig(config);
    }

    /***********************************************************************************************
     * API switch
     **********************************************************************************************/

    private void saveApiConfig() {

        if (getApiSwitch().isChecked() && isApiSwitchFieldsValid()) {
            ApiConfig config = new ApiConfig();
            config.apiusr = getSharedPreference(getString(R.string.key_setting_apiusr));;
            config.apipss = getSharedPreference(getString(R.string.key_setting_apipss));
            config.apisrv = getSharedPreference(getString(R.string.key_setting_apisrv));
            config.apiuri = getSharedPreference(getString(R.string.key_setting_apiuri));
            config.apiprt = 80;  // TODO: sending via UI
            config.aenb = true;
            sendSensorConfig(config);
        } else
            setApiSwitch(false);
    }

    private boolean isApiSwitchFieldsValid(){
        Logger.v(TAG, "[Config] validating ->" + getString(R.string.key_setting_enable_api));
        String api_usr = getSharedPreference(getString(R.string.key_setting_apiusr));
        String api_pss = getSharedPreference(getString(R.string.key_setting_apipss));
        String api_srv = getSharedPreference(getString(R.string.key_setting_apisrv));
        Logger.v(TAG, "[Config] values -> " + api_usr +  " " + api_pss + " " + api_srv);
        apiSummaryUpdate();
        return !(api_usr.length() == 0 || api_pss.length() == 0 || api_srv.length() == 0);
    }

    private void setApiSwitch(boolean checked) {
        Logger.v(TAG, "[Config] API switch check -> " + checked);
        SwitchPreference apiSwitch = getApiSwitch();
        apiSwitch.setEnabled(isApiSwitchFieldsValid());
        apiSwitch.setChecked(checked);
        apiSummaryUpdate();
        enableApiOnDevice(checked);
    }

    private void apiSummaryUpdate(){
        updateApiUsrSummmary();
        updateApiUriSummary();
        updateApiHostSummary();
        updatePasswSummary(R.string.key_setting_apipss);
    }

    private SwitchPreference getApiSwitch() {
        return findPreference(getString(R.string.key_setting_enable_api));
    }

    private void enableApiOnDevice(boolean enable) {
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "ast";
        config.aenb = enable;
        sendSensorConfig(config);
    }

    /***********************************************************************************************
     * InfluxDB switch
     **********************************************************************************************/

    private void saveInfluxDbConfig() {

        if (getInfluxDbSwitch().isChecked() && isInfluxDbSwitchFieldsValid()) {
            InfluxdbConfig config = new InfluxdbConfig();
            config.ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
            config.ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
            config.ienb = true;
            sendSensorConfig(config);
        } else
            setInfluxDbSwitch(false);
    }

    private boolean isInfluxDbSwitchFieldsValid(){
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_ifxdb));
        String ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
        String ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
        updateInfluxSummary();
        Logger.v(TAG, "[Config] values -> " + ifxdb +  " " + ifxip);
        return !(ifxdb.length() == 0 || ifxip.length() == 0);
    }

    private void setInfluxDbSwitch(boolean checked) {
        Logger.v(TAG, "[Config] InfluxDb switch check -> " + checked);
        SwitchPreference ifxdbSwitch = getInfluxDbSwitch();
        ifxdbSwitch.setEnabled(isInfluxDbSwitchFieldsValid());
        ifxdbSwitch.setChecked(checked);
        updateInfluxSummary();
        enableInfluxDbOnDevice(checked);
    }

    private void updateInfluxSummary(){
        updateInfluxDbSummmary();
        updateInfluxPortSummary();
    }

    private void enableInfluxDbOnDevice(boolean enable) {
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "ist";
        config.ienb = enable;
        sendSensorConfig(config);
    }

    private SwitchPreference getInfluxDbSwitch() {
        return findPreference(getString(R.string.key_setting_enable_ifx));
    }


    /***********************************************************************************************
     * Misc preferences section
     **********************************************************************************************/

    private void validateLocationSwitch() {
        SwitchPreference locationSwitch = findPreference(getString(R.string.key_setting_enable_location));
        locationSwitch.setEnabled(lastLocation!=null);
    }

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
                    sendSensorConfig(config);
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
                sendSensorReboot();
                getMain().showSnackMessageSlow(R.string.msg_device_reboot);
                Handler handler = new Handler();
                handler.postDelayed(() -> getMain().finish(), 3000);
            });
            snackBar.show();
        }
    }

    private void sendSensorReboot() {
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "rbt";
        sendSensorConfig(config);
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
                sendSensorConfig(config);
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

    private void sendSensorConfig(SensorConfig config) {
        Logger.v(TAG, "[Config] writing sensor config: "+config.getClass().getName());
        getMain().getRecordTrackManager().writeSensorConfig(new Gson().toJson(config));
    }

    private void readSensorConfig(){
        getMain().getRecordTrackManager().readSensorConfig();
    }

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
            if (config.wenb != getWifiSwitch().isChecked()) {
                setWifiSwitch(config.wenb);
                notify_sync = true;
            }
            if (config.ienb != getInfluxDbSwitch().isChecked()) {
                setInfluxDbSwitch(config.ienb);
                notify_sync = true;
            }
            if (config.aenb != getApiSwitch().isChecked()) {
                setApiSwitch(config.aenb);
                notify_sync = true;
            }
            if (config.stype != getSensorType()) {
                if (config.stype < 0) updateSensorTypeSummary(0);
                else updateSensorTypeSummary((config.stype));
            }

            updatePreferencesSummmary(config);
            saveAllPreferences(config);
            updateSwitches(config);

            if (notify_sync) {
                Logger.v(TAG, "[Config] notify device sync complete");
                getMain().showSnackMessage(R.string.msg_sync_complete);
                rebuildUI();
            }
        }
    }

    private void printResponseConfig(ResponseConfig config) {
        Logger.i(TAG, "[Config] Callback values:");
        Logger.i(TAG, "[Config] dname:  " + config.dname);
        Logger.i(TAG, "[Config] stime:  " + config.stime);
        Logger.i(TAG, "[Config] stype:  " + config.stype);
        Logger.i(TAG, "[Config] ssid:   " + config.ssid);
        Logger.i(TAG, "[Config] ifxdb:  " + config.ifxdb);
        Logger.i(TAG, "[Config] ifxip:  " + config.ifxip);
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
        Logger.i(TAG, "[Config] lskey : " + config.lskey);
    }


    private void updatePreferencesSummmary(ResponseConfig config) {
        if(config.dname !=null)updateSummary(R.string.key_setting_dname,config.dname);
        if(config.apiusr !=null)updateSummary(R.string.key_setting_apiusr,config.apiusr);
        if(config.apisrv !=null)updateSummary(R.string.key_setting_apisrv,config.apisrv);
        if(config.apiuri !=null)updateSummary(R.string.key_setting_apiuri,config.apiuri);
        if(config.ssid !=null)updateSummary(R.string.key_setting_ssid,config.ssid);
        if(config.ifxdb !=null)updateSummary(R.string.key_setting_ifxdb,config.ifxdb);
        if(config.ifxip !=null)updateSummary(R.string.key_setting_ifxip,config.ifxip);
//        if(config.stype != -1)updateSummary(R.string.key_setting_dtype,);
        if(config.stime>0)updateSummary(R.string.key_setting_stime, getStimeFormat(config.stime));
        updatePasswSummary(R.string.key_setting_pass);
        updatePasswSummary(R.string.key_setting_apipss);
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

    private void updateSummary(int key, int msg){
        Preference pref = findPreference(getString(key));
        pref.setSummary(getString(msg));
    }

    private void updatePasswSummary(int key) {
        String passw = getSharedPreference(getString(key));
        if(passw.length()>0) updateSummary(key,R.string.msg_passw_seted);
        else updateSummary(key,R.string.msg_passw_unseted);
    }

    private void updateInfluxDbSummmary() {
        updateSummary(R.string.key_setting_ifxdb);
    }

    private void updateInfluxPortSummary() {
        updateSummary(R.string.key_setting_ifxip);
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
        saveSharedPreference(R.string.key_setting_ssid, config.ssid);
        saveSharedPreference(R.string.key_setting_ifxdb, config.ifxdb);
        saveSharedPreference(R.string.key_setting_ifxip, config.ifxip);
        saveSharedPreference(R.string.key_setting_apiusr, config.apiusr);
        saveSharedPreference(R.string.key_setting_apisrv, config.apisrv);
        saveSharedPreference(R.string.key_setting_wmac, "" + config.wmac);
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
