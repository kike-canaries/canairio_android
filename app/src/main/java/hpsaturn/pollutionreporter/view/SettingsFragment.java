package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hpsaturn.tools.Logger;

import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.SensorConfig;

/**
 * Created by Antonio Vanegas @hpsaturn on 2/17/19.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    private String sname, ssid, pass, ifxdb, ifxip, ifxtg, apiusr, apipss;
    private int stime;
    private boolean onCredentialsChanged;
    private boolean onInfluxDBConfigChanged;
    private boolean onAPIConfigChanged;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        sname = getSharedPreference(getString(R.string.key_setting_dname));
        apiusr = getSharedPreference(getString(R.string.key_setting_apiusr));
        apipss = getSharedPreference(getString(R.string.key_setting_apipss));
        ssid = getSharedPreference(getString(R.string.key_setting_ssid));
        pass = getSharedPreference(getString(R.string.key_setting_pass));
        ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
        ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
        ifxtg = getSharedPreference(getString(R.string.key_setting_ifxtg));
        stime = getCurrentStime();

        updateSensorNameSummary();
        updateStimeSummary();
        validateWifiSwitch();
        validateIfxdbSwitch();
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
        }
        else if (key.equals(getString(R.string.key_setting_stime))) {
            validateSensorSampleTime(sharedPreferences, key);
        }
        else if (key.equals(getString(R.string.key_setting_ssid))) {
            validateWifiSwitch();
        }
        else if (key.equals(getString(R.string.key_setting_pass))) {
            validateWifiSwitch();
        }
        else if (key.equals(getString(R.string.key_setting_enable_wifi))) {
            saveWifiConfig(sharedPreferences, key);
        }
        else if (key.equals(getString(R.string.key_setting_apiusr))) {
            validateApiSwitch();
        }
        else if (key.equals(getString(R.string.key_setting_apipss))) {
            validateApiSwitch();
        }
        else if (key.equals(getString(R.string.key_setting_enable_api))) {
            saveApiConfig(sharedPreferences, key);
        }
        else if (key.equals(getString(R.string.key_setting_enable_ifx))){
            saveInfluxConfig(sharedPreferences,key) ;
        }
        else
            validateIfxdbSwitch();
    }

    private void validateSensorName(SharedPreferences sharedPreferences, String key){
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_dname));
        String old_sname = sname;
        sname = getSharedPreference(getString(R.string.key_setting_dname));
        if(!old_sname.equals(sname)){
           saveSensorName(sname);
        }
        updateStimeSummary();
    }

    private void saveSensorName(String name){
        getMain().showSnackMessage(R.string.msg_save_config);
        SensorConfig config = new SensorConfig();
        config.dname = name;
        getMain().getRecordTrackManager().writeSensorConfig(config);
    }


    private void updateSensorNameSummary(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getMain());
        String key = getString(R.string.key_setting_dname);
        Preference pref = findPreference(key);
        String dname = sharedPref.getString(key,"");
        pref.setSummary(dname);
    }

    private void validateSensorSampleTime(SharedPreferences sharedPreferences, String key) {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_stime));
        int old_stime = stime;
        stime = getCurrentStime();
        if (old_stime != stime && stime >= 5) {
            saveSensorSampleTime(stime);
        } else if (old_stime != stime){
            saveSharedPreference(key, "" + old_stime);
        }
        updateStimeSummary();
    }

    private void saveSensorSampleTime(int time){
        getMain().showSnackMessage(R.string.msg_save_config);
        SensorConfig config = new SensorConfig();
        config.stime = time;
        getMain().getRecordTrackManager().writeSensorConfig(config);
    }

    private int getCurrentStime() {
        try {
            return Integer.parseInt(getSharedPreference(getString(R.string.key_setting_stime)));
        } catch (NumberFormatException e) { e.printStackTrace(); }
        return 0;
    }

    private void updateStimeSummary(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getMain());
        String key = getString(R.string.key_setting_stime);
        Preference pref = findPreference(key);
        String stime = sharedPref.getString(key,"");
        pref.setSummary("" + stime + " seconds");
    }

    private void saveWifiConfig(SharedPreferences sharedPreferences, String key) {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_enable_wifi));

        SwitchPreferenceCompat switchPreference = findPreference(key);

        if(switchPreference.isChecked()) {
            String ssid = getSharedPreference(getString(R.string.key_setting_ssid));
            String pass = getSharedPreference(getString(R.string.key_setting_pass));
            getMain().showSnackMessage(R.string.msg_save_config);
            SensorConfig config = new SensorConfig();
            config.ssid = ssid;
            config.pass = pass;
            Logger.v(TAG, "[Config] writing wifi credentials..");
            getMain().getRecordTrackManager().writeSensorConfig(config);
        }
        else if (!onCredentialsChanged){
            disableWifiOnDevice();
        }
        else {
            Logger.d(TAG,"[Config] onCredentialsChanged skip disable wifi.");
            onCredentialsChanged=false;
        }
    }

    private void disableWifiOnDevice() {
        SensorConfig config = new SensorConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "wst";
        config.wenb = false;
        getMain().getRecordTrackManager().writeSensorConfig(config);
    }

    private void validateWifiSwitch(){
        SwitchPreferenceCompat wifiSwitch = findPreference(getString(R.string.key_setting_enable_wifi));
        String old_ssid = ssid;
        String old_pass = pass;
        ssid = getSharedPreference(getString(R.string.key_setting_ssid));
        pass = getSharedPreference(getString(R.string.key_setting_pass));

        wifiSwitch.setEnabled(!(ssid.length()==0 || pass.length()==0));

        if(!(old_ssid.equals(ssid) && old_pass.equals(pass))) {
            onCredentialsChanged = true;
            wifiSwitch.setChecked(false);   // TODO: force user to enable again
        }
    }

    private void validateApiSwitch(){
        SwitchPreferenceCompat apiSwitch = findPreference(getString(R.string.key_setting_enable_api));
        String old_apiusr = apiusr;
        String old_apipss = apipss;
        apiusr = getSharedPreference(getString(R.string.key_setting_apiusr));
        apipss = getSharedPreference(getString(R.string.key_setting_apipss));

        updateApiSummmary();

        apiSwitch.setEnabled(!(apiusr.length()==0 || apipss.length()==0));

        if(!(old_apiusr.equals(apiusr) && old_apipss.equals(apipss))) {
            apiSwitch.setChecked(false);   // TODO: force user to enable again
            onAPIConfigChanged = true;
        }
    }

    private void saveApiConfig(SharedPreferences sharedPreferences, String key) {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_enable_api));

        SwitchPreferenceCompat switchPreference = findPreference(key);

        if(switchPreference.isChecked()) {
            String api_usr = getSharedPreference(getString(R.string.key_setting_apiusr));
            String api_pss = getSharedPreference(getString(R.string.key_setting_apipss));
            getMain().showSnackMessage(R.string.msg_save_config);
            SensorConfig config = new SensorConfig();
            config.apiusr = api_usr;
            config.apipss = api_pss;
            Logger.v(TAG, "[Config] writing API credentials..");
            getMain().getRecordTrackManager().writeSensorConfig(config);
        }
        else if (!onAPIConfigChanged) {
            disableApi();
        }
        else {
            Logger.d(TAG,"[Config] onAPIConfigChanged skip disable API.");
            onAPIConfigChanged=false;
        }
    }

    private void disableApi() {
        SensorConfig config = new SensorConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "ast";
        config.aenb = false;
        getMain().getRecordTrackManager().writeSensorConfig(config);
    }


    private void saveInfluxConfig(SharedPreferences sharedPreferences, String key) {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_ifxdb));
        SwitchPreferenceCompat switchPreference = findPreference(key);

        if(switchPreference.isChecked()) {

            ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
            ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
            ifxtg = getSharedPreference(getString(R.string.key_setting_ifxtg));

            getMain().showSnackMessage(R.string.msg_save_config);
            SensorConfig config = new SensorConfig();
            config.ifxdb = ifxdb;
            config.ifxip = ifxip;
            config.ifxtg = ifxtg;
            Logger.v(TAG, "[Config] writing InfluxDb settings..");
            getMain().getRecordTrackManager().writeSensorConfig(config);
        }
        else if (!onInfluxDBConfigChanged){
            disableInfluxDB();
        }
        else {
            Logger.d(TAG,"[Config] onInfluxDBConfigChanged skip disable Influx.");
            onInfluxDBConfigChanged=false;
        }
    }

    private void validateIfxdbSwitch(){
        SwitchPreferenceCompat ifxdbSwitch = findPreference(getString(R.string.key_setting_enable_ifx));
        String old_ifxdb = ifxdb;
        String old_ifxip = ifxip;
        String old_ifxtg = ifxtg;
        ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
        ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
        ifxtg = getSharedPreference(getString(R.string.key_setting_ifxtg));
        updateIfxdbSummmary();

        ifxdbSwitch.setEnabled(!(ifxdb.length()==0 || ifxip.length()==0));

        if(!(old_ifxdb.equals(ifxdb) && old_ifxip.equals(ifxip) && old_ifxtg.equals(ifxtg))) {
            ifxdbSwitch.setChecked(false);   // TODO: force user to enable again?
            onInfluxDBConfigChanged = true;
        }
    }

    private void disableInfluxDB() {
        SensorConfig config = new SensorConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "ist";
        config.ienb = false;
        getMain().getRecordTrackManager().writeSensorConfig(config);
    }

    public void configCallBack(SensorConfig config) {
        if (config != null) {
            Logger.i(TAG, "dname: " + config.dname);
            Logger.i(TAG, "ifxdb: " + config.ifxdb);
            Logger.i(TAG, "ifxip: " + config.ifxip);
            Logger.i(TAG, "ifxtg: " + config.ifxtg);
            Logger.i(TAG, "ssid:  " + config.ssid);
            Logger.i(TAG, "stime: " + config.stime);
            Logger.i(TAG, "wmac:  " + config.wmac);
            Logger.i(TAG, "wifien:" + config.wenb);
            Logger.i(TAG, "apiusr: " + config.apiusr);
            getMain().showSnackMessage(R.string.msg_config_saved);
            updatePreferencesSummmary(config);
            saveAllPreferences(config);
        }
    }

    private void updatePreferencesSummmary(SensorConfig config) {
        Preference pref;
        pref = findPreference(getString(R.string.key_setting_dname));
        pref.setSummary(config.dname);
        pref = findPreference(getString(R.string.key_setting_apiusr));
        pref.setSummary(config.apiusr);
        pref = findPreference(getString(R.string.key_setting_ssid));
        pref.setSummary(config.ssid);
        pref = findPreference(getString(R.string.key_setting_ifxdb));
        pref.setSummary(config.ifxdb);
        pref = findPreference(getString(R.string.key_setting_ifxip));
        pref.setSummary(config.ifxip);
        pref = findPreference(getString(R.string.key_setting_ifxtg));
        pref.setSummary(config.ifxtg);
        pref = findPreference(getString(R.string.key_setting_stime));
        pref.setSummary("" + config.stime + " seconds");
        SwitchPreferenceCompat wifiSwitch = findPreference(getString(R.string.key_setting_enable_wifi));
        wifiSwitch.setChecked(config.wenb);
    }

    private void updateIfxdbSummmary() {
        Preference pref;
        pref = findPreference(getString(R.string.key_setting_ifxdb));
        pref.setSummary(ifxdb);
        pref = findPreference(getString(R.string.key_setting_ifxip));
        pref.setSummary(ifxip);
        pref = findPreference(getString(R.string.key_setting_ifxtg));
        pref.setSummary(ifxtg);
    }

    private void updateApiSummmary() {
        Preference pref;
        pref = findPreference(getString(R.string.key_setting_apiusr));
        pref.setSummary(apiusr);
    }

    private void saveAllPreferences(SensorConfig config) {
        saveSharedPreference(R.string.key_setting_dname,config.dname);
        saveSharedPreference(R.string.key_setting_ssid,config.ssid);
        saveSharedPreference(R.string.key_setting_ifxdb,config.ifxdb);
        saveSharedPreference(R.string.key_setting_ifxip,config.ifxip);
        saveSharedPreference(R.string.key_setting_ifxtg,config.ifxtg);
        saveSharedPreference(R.string.key_setting_stime,""+config.stime);
        saveSharedPreference(R.string.key_setting_wmac,""+config.wmac);
    }

    public void saveSharedPreference(int key, String value){
        saveSharedPreference(getString(key),value);
    }

    public void saveSharedPreference(String key, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void saveSharedPreference(String key, boolean enable) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, enable);
        editor.apply();
    }

    public String getSharedPreference(String key){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        return preferences.getString(key,"");
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
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
