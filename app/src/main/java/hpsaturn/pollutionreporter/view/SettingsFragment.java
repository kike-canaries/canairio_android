package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.hpsaturn.tools.UITools;
import com.takisoft.preferencex.PreferenceFragmentCompat;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.text.DecimalFormat;

import com.hpsaturn.tools.Logger;
import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.SensorConfig;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by Antonio Vanegas @hpsaturn on 2/17/19.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    private String sname, ssid, pass, ifxdb, ifxip, apiusr, apipss, apisrv, apiuri;
    private int stime;
    private boolean onWifiConfigChanged;
    private boolean onInfluxDBConfigChanged;
    private boolean onAPIConfigChanged;
    private Location lastLocation;
    private Snackbar snackBar;

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        sname = getSharedPreference(getString(R.string.key_setting_dname));
        apiusr = getSharedPreference(getString(R.string.key_setting_apiusr));
        apipss = getSharedPreference(getString(R.string.key_setting_apipss));
        apisrv = getSharedPreference(getString(R.string.key_setting_apisrv));
        apiuri = getSharedPreference(getString(R.string.key_setting_apiuri));
        ssid = getSharedPreference(getString(R.string.key_setting_ssid));
        pass = getSharedPreference(getString(R.string.key_setting_pass));
        ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
        ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
        stime = getCurrentStime();

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
        validateWifiSwitch();
        validateApiSwitch();
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
            validateSensorName(sharedPreferences, key);
        } else if (key.equals(getString(R.string.key_setting_stime))) {
            validateSensorSampleTime(sharedPreferences, key);
        } else if (key.equals(getString(R.string.key_setting_ssid))) {
            validateWifiSwitch();
        } else if (key.equals(getString(R.string.key_setting_pass))) {
            validateWifiSwitch();
        } else if (key.equals(getString(R.string.key_setting_enable_wifi))) {
            saveWifiConfig(sharedPreferences, key);
        } else if (key.equals(getString(R.string.key_setting_apiusr))) {
            validateApiSwitch();
        } else if (key.equals(getString(R.string.key_setting_apipss))) {
            validateApiSwitch();
        } else if (key.equals(getString(R.string.key_setting_enable_api))) {
            saveApiConfig(key);
        } else if (key.equals(getString(R.string.key_setting_enable_ifx))) {
            saveInfluxConfig(key);
        } else if (key.equals(getString(R.string.key_setting_enable_reboot))) {
            performRebootDevice();
        } else if (key.equals(getString(R.string.key_setting_enable_clear))) {
            performClearDevice();
        } else if (key.equals(getString(R.string.key_setting_enable_location))) {
            saveLocation();
        }

        refreshUI();
    }

    /***********************************************************************************************
     * Sensor name section
     **********************************************************************************************/

    private void validateSensorName(SharedPreferences sharedPreferences, String key) {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_dname));
        String old_sname = sname;
        sname = getSharedPreference(getString(R.string.key_setting_dname));
        if (!old_sname.equals(sname)) {
            saveSensorName(sname);
        }
        updateSensorNameSummary();
    }

    private void saveSensorName(String name) {
        if(name.length() == 0 ) return;
        getMain().showSnackMessage(R.string.msg_save_config);
        SensorConfig config = new SensorConfig();
        config.dname = name;
        getMain().getRecordTrackManager().writeSensorConfig(config);
    }


    private void updateSensorNameSummary() {
        updateSummary(R.string.key_setting_dname);
    }

    /***********************************************************************************************
     * Sample time handlers
     **********************************************************************************************/

    private void validateSensorSampleTime(SharedPreferences sharedPreferences, String key) {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_stime));
        int old_stime = stime;
        stime = getCurrentStime();
        if (old_stime != stime && stime >= 5) {
            saveSensorSampleTime(stime);
        } else if (old_stime != stime) {
            saveSharedPreference(key, "" + old_stime);
        }
        updateStimeSummary();
    }

    private void saveSensorSampleTime(int time) {
        getMain().showSnackMessage(R.string.msg_save_config);
        SensorConfig config = new SensorConfig();
        config.stime = time;
        getMain().getRecordTrackManager().writeSensorConfig(config);
    }

    private int getCurrentStime() {
        try {
            return Integer.parseInt(getSharedPreference(getString(R.string.key_setting_stime)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void updateStimeSummary() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getMain());
        String key = getString(R.string.key_setting_stime);
        Preference pref = findPreference(key);
        String stime = sharedPref.getString(key, "");
        pref.setSummary("" + stime + " seconds");
    }


    /***********************************************************************************************
     * Validate Switches
     **********************************************************************************************/

    private void validateWifiSwitch() {
        SwitchPreference wifiSwitch = findPreference(getString(R.string.key_setting_enable_wifi));
        String old_ssid = ssid;
        String old_pass = pass;
        ssid = getSharedPreference(getString(R.string.key_setting_ssid));
        pass = getSharedPreference(getString(R.string.key_setting_pass));
        updateSummary(R.string.key_setting_ssid);
        wifiSwitch.setEnabled(!(ssid.length() == 0 || pass.length() == 0));

        if (!(old_ssid.equals(ssid) && old_pass.equals(pass))) {
            wifiSwitch.setChecked(false);   // TODO: force user to enable again
            onWifiConfigChanged = true;
        }
    }

    private void validateApiSwitch() {
        SwitchPreference apiSwitch = findPreference(getString(R.string.key_setting_enable_api));
        String old_apiusr = apiusr;
        String old_apipss = apipss;

        apiusr = getSharedPreference(getString(R.string.key_setting_apiusr));
        apipss = getSharedPreference(getString(R.string.key_setting_apipss));
        apisrv = getSharedPreference(getString(R.string.key_setting_apisrv));
        apiuri = getSharedPreference(getString(R.string.key_setting_apiuri));

        updateApiUsrSummmary();
        updateApiUriSummary();
        updateApiHostSummary();

        apiSwitch.setEnabled(!(apiusr.length() == 0 || apipss.length() == 0));

        if (!(old_apiusr.equals(apiusr) && old_apipss.equals(apipss))) {
            apiSwitch.setChecked(false);   // TODO: force user to enable again
            onAPIConfigChanged = true;
        }
    }

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

    private void saveWifiConfig(SharedPreferences sharedPreferences, String key) {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_enable_wifi));

        SwitchPreference switchPreference = findPreference(key);

        if (switchPreference.isChecked()) {
            String ssid = getSharedPreference(getString(R.string.key_setting_ssid));
            String pass = getSharedPreference(getString(R.string.key_setting_pass));
            if(ssid.length()==0 || pass.length() == 0) return;
            getMain().showSnackMessage(R.string.msg_save_config_wfi);
            SensorConfig config = new SensorConfig();
            config.ssid = ssid;
            config.pass = pass;
            Logger.v(TAG, "[Config] writing wifi credentials..");
            getMain().getRecordTrackManager().writeSensorConfig(config);
        } else if (!onWifiConfigChanged) {
            enableWifiOnDevice(false);
        } else {
            Logger.d(TAG, "[Config] onWifiConfigChanged skip disable wifi.");
            onWifiConfigChanged = false;
        }
    }

    private void enableWifiOnDevice(boolean enable) {
        SensorConfig config = new SensorConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "wst";
        config.wenb = enable;
        getMain().getRecordTrackManager().writeSensorConfig(config);
    }

    private void saveApiConfig(String key) {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_enable_api));

        SwitchPreference switchPreference = findPreference(key);

        if (switchPreference.isChecked()) {
            String api_usr = getSharedPreference(getString(R.string.key_setting_apiusr));
            String api_pss = getSharedPreference(getString(R.string.key_setting_apipss));
            String api_srv = getSharedPreference(getString(R.string.key_setting_apisrv));
            String api_uri = getSharedPreference(getString(R.string.key_setting_apiuri));
            if(api_usr.length() == 0 || api_pss.length() == 0 || api_srv.length() == 0) return;
            getMain().showSnackMessage(R.string.msg_save_config_api);
            SensorConfig config = new SensorConfig();
            config.apiusr = api_usr;
            config.apipss = api_pss;
            config.apisrv = api_srv;
            config.apiuri = api_uri;
            config.apiprt = 80;  // TODO: sending via UI
            Logger.v(TAG, "[Config] writing API credentials..");
            getMain().getRecordTrackManager().writeSensorConfig(config);
        } else if (!onAPIConfigChanged) {
            disableApi();
        } else {
            Logger.d(TAG, "[Config] onAPIConfigChanged skip disable API.");
            onAPIConfigChanged = false;
        }
    }

    private void disableApi() {
        SensorConfig config = new SensorConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "ast";
        config.aenb = false;
        getMain().getRecordTrackManager().writeSensorConfig(config);
    }

    private void saveInfluxConfig(String key) {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_ifxdb));
        SwitchPreference switchPreference = findPreference(key);

        if (switchPreference.isChecked()) {
            String ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
            String ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
            if(ifxdb.length() == 0 || ifxip.length() == 0) return;
            getMain().showSnackMessage(R.string.msg_save_config_ifx);
            SensorConfig config = new SensorConfig();
            config.ifxdb = ifxdb;
            config.ifxip = ifxip;
            Logger.v(TAG, "[Config] writing InfluxDb settings..");
            getMain().getRecordTrackManager().writeSensorConfig(config);
        } else if (!onInfluxDBConfigChanged) {
            disableInfluxDB();
        } else {
            Logger.d(TAG, "[Config] onInfluxDBConfigChanged skip disable Influx.");
            onInfluxDBConfigChanged = false;
        }
    }

    private void disableInfluxDB() {
        SensorConfig config = new SensorConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "ist";
        config.ienb = false;
        getMain().getRecordTrackManager().writeSensorConfig(config);
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
                    SensorConfig config = new SensorConfig();
                    config.lat = lastLocation.getLatitude();
                    config.lon = lastLocation.getLongitude();
                    config.alt = lastLocation.getAltitude();
                    config.spd = lastLocation.getSpeed();
                    getMain().getRecordTrackManager().writeSensorConfig(config);
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
                SensorConfig config = new SensorConfig();
                config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
                config.act = "rbt";
                getMain().showSnackMessageSlow(R.string.msg_device_reboot);
                getMain().getRecordTrackManager().writeSensorConfig(config);
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
                SensorConfig config = new SensorConfig();
                config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
                config.act = "cls";
                getMain().showSnackMessageSlow(R.string.msg_device_clear);
                getMain().getRecordTrackManager().writeSensorConfig(config);
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

    public void configCallBack(SensorConfig config) {
        if (config != null) {
            Logger.i(TAG, "dname: " + config.dname);
            Logger.i(TAG, "ifxdb: " + config.ifxdb);
            Logger.i(TAG, "ifxip: " + config.ifxip);
            Logger.i(TAG, "ssid:  " + config.ssid);
            Logger.i(TAG, "stime: " + config.stime);
            Logger.i(TAG, "wmac:  " + config.wmac);
            Logger.i(TAG, "wifien:" + config.wenb);
            Logger.i(TAG, "apien: " + config.aenb);
            Logger.i(TAG, "ifxen: " + config.ienb);
            Logger.i(TAG, "apiusr:" + config.apiusr);
            Logger.i(TAG, "apisrv:" + config.apisrv);
            Logger.i(TAG, "apiuri:" + config.apiuri);
            Logger.i(TAG, "apiprt:" + config.apiprt);


            FirebaseCrashlytics.getInstance().setCustomKey(getString(R.string.crashkey_device_name),""+config.dname);
            FirebaseCrashlytics.getInstance().setCustomKey(getString(R.string.crashkey_device_wmac),""+config.wmac);
            FirebaseCrashlytics.getInstance().setCustomKey(getString(R.string.crashkey_api_usr),""+config.apiusr);

            updatePreferencesSummmary(config);
            updateSwitches(config);
            saveAllPreferences(config);
            rebuildUI();
        }
    }

    private void updatePreferencesSummmary(SensorConfig config) {
        if(config.dname !=null)updateSummary(R.string.key_setting_dname,config.dname);
        if(config.apiusr !=null)updateSummary(R.string.key_setting_apiusr,config.apiusr);
        if(config.apisrv !=null)updateSummary(R.string.key_setting_apisrv,config.apisrv);
        if(config.apiuri !=null)updateSummary(R.string.key_setting_apiuri,config.apiuri);
        if(config.ssid !=null)updateSummary(R.string.key_setting_ssid,config.ssid);
        if(config.ifxdb !=null)updateSummary(R.string.key_setting_ifxdb,config.ifxdb);
        if(config.ifxip !=null)updateSummary(R.string.key_setting_ifxip,config.ifxip);
        if(config.stime>0)updateSummary(R.string.key_setting_stime,"" + config.stime + " seconds");
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
        Preference pref;
        pref = findPreference(getString(R.string.key_setting_apiusr));
        pref.setSummary(apiusr);
    }

    private void updateApiHostSummary() {
        Preference pref;
        pref = findPreference(getString(R.string.key_setting_apisrv));
        pref.setSummary(apisrv);
    }

    private void updateApiUriSummary() {
        Preference pref;
        pref = findPreference(getString(R.string.key_setting_apiuri));
        pref.setSummary(apiuri);
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

    private void saveAllPreferences(SensorConfig config) {
        saveSharedPreference(R.string.key_setting_dname, config.dname);
        saveSharedPreference(R.string.key_setting_ssid, config.ssid);
        saveSharedPreference(R.string.key_setting_ifxdb, config.ifxdb);
        saveSharedPreference(R.string.key_setting_ifxip, config.ifxip);
        saveSharedPreference(R.string.key_setting_stime, "" + config.stime);
        saveSharedPreference(R.string.key_setting_wmac, "" + config.wmac);
        saveSharedPreference(R.string.key_setting_enable_wifi,config.wenb);
        saveSharedPreference(R.string.key_setting_enable_ifx,config.ienb);
        saveSharedPreference(R.string.key_setting_enable_api,config.aenb);
    }

    private void saveSharedPreference(int key, String value) {
        saveSharedPreference(getString(key), value);
    }

    private void saveSharedPreference(String key, String value) {
        if(value.length()!=0) {
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
