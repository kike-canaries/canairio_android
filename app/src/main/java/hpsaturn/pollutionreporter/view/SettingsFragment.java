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

    private String ssid, pass, ifxdb, ifxip, ifxid, ifxtg;
    private int stime;
    private boolean onCredentialsChanged;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        ssid = getSharedPreference(getString(R.string.key_setting_ssid));
        pass = getSharedPreference(getString(R.string.key_setting_pass));
        ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
        ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
        ifxid = getSharedPreference(getString(R.string.key_setting_ifxid));
        ifxtg = getSharedPreference(getString(R.string.key_setting_ifxtg));
        stime = getCurrentStime();

        updateStimeSummary();
        updateWifiSwitch();
        updateIfxdbSwitch();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMain().getServiceManager().readSensorConfig();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.key_setting_stime))) {
            validateSensorSampleTime(sharedPreferences, key);
        }
        else if (key.equals(getString(R.string.key_setting_ssid))) {
            updateWifiSwitch();
        }
        else if (key.equals(getString(R.string.key_setting_pass))) {
            updateWifiSwitch();
        }
        else if (key.equals(getString(R.string.key_setting_enable_wifi))) {
            validateSensorWifiConfig(sharedPreferences, key);
        }
        else if (key.equals(getString(R.string.key_setting_enable_ifx))){
            validateInfluxConfig(sharedPreferences,key) ;
        }
        else
            updateIfxdbSwitch();
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
        getMain().getServiceManager().writeSensorConfig(config);
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

    private void validateSensorWifiConfig(SharedPreferences sharedPreferences, String key) {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_enable_wifi));

        SwitchPreferenceCompat switchPreference = findPreference(key);

        if(switchPreference.isChecked()) {
            String ssid = getSharedPreference(getString(R.string.key_setting_ssid));
            String pass = getSharedPreference(getString(R.string.key_setting_pass));
            getMain().showSnackMessage(R.string.msg_save_config);
            SensorConfig config = new SensorConfig();
            config.ssid = ssid;
            config.pass = pass;
            getMain().getServiceManager().writeSensorConfig(config);
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
        getMain().getServiceManager().writeSensorConfig(config);
    }

    private void updateWifiSwitch(){
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

    private void validateInfluxConfig(SharedPreferences sharedPreferences, String key) {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_ifxdb));

        SwitchPreferenceCompat switchPreference = findPreference(key);

        if(switchPreference.isChecked()) {

            ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
            ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
            ifxid = getSharedPreference(getString(R.string.key_setting_ifxid));
            ifxtg = getSharedPreference(getString(R.string.key_setting_ifxtg));

            getMain().showSnackMessage(R.string.msg_save_config);
            SensorConfig config = new SensorConfig();
            config.ifxdb = ifxdb;
            config.ifxip = ifxip;
            config.ifxid = ifxid;
            config.ifxtg = ifxtg;
            getMain().getServiceManager().writeSensorConfig(config);
        }
        else{
            // TODO: ???
        }
    }

    private void updateIfxdbSwitch(){
        SwitchPreferenceCompat ifxdbSwitch = findPreference(getString(R.string.key_setting_enable_ifx));
        String old_ifxdb = ifxdb;
        String old_ifxip = ifxip;
        String old_ifxid = ifxid;
        String old_ifxtg = ifxtg;
        ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb));
        ifxip = getSharedPreference(getString(R.string.key_setting_ifxip));
        ifxid = getSharedPreference(getString(R.string.key_setting_ifxid));
        ifxtg = getSharedPreference(getString(R.string.key_setting_ifxtg));

        ifxdbSwitch.setEnabled(!(ifxdb.length()==0 || ifxip.length()==0) || ifxid.length()==0);

        if(!(old_ifxdb.equals(ifxdb) && old_ifxip.equals(ifxip) && old_ifxid.equals(ifxid) && old_ifxtg.equals(ifxtg))) {
            ifxdbSwitch.setChecked(false);   // TODO: force user to enable again?
        }
    }

    public void configCallBack(SensorConfig config) {
        if (config != null) {
            Logger.i(TAG, "Ifxdb: " + config.ifxdb);
            Logger.i(TAG, "Ifxip: " + config.ifxip);
            Logger.i(TAG, "Ifxid: " + config.ifxid);
            Logger.i(TAG, "Ifxtg: " + config.ifxtg);
            Logger.i(TAG, "ssid:  " + config.ssid);
            Logger.i(TAG, "stime: " + config.stime);
            Logger.i(TAG, "wmac:  " + config.wmac);
            Logger.i(TAG, "wifien:" + config.wenb);
            getMain().showSnackMessage(R.string.msg_config_saved);
            updatePreferencesSummmary(config);
            saveAllPreferences(config);
        }
    }

    private void updatePreferencesSummmary(SensorConfig config) {
        Preference pref;
        pref = findPreference(getString(R.string.key_setting_ssid));
        pref.setSummary(config.ssid);
        pref = findPreference(getString(R.string.key_setting_ifxdb));
        pref.setSummary(config.ifxdb);
        pref = findPreference(getString(R.string.key_setting_ifxip));
        pref.setSummary(config.ifxip);
        pref = findPreference(getString(R.string.key_setting_ifxid));
        pref.setSummary(config.ifxid);
        pref = findPreference(getString(R.string.key_setting_ifxtg));
        pref.setSummary(config.ifxtg);
        pref = findPreference(getString(R.string.key_setting_stime));
        pref.setSummary("" + config.stime + " seconds");
        SwitchPreferenceCompat wifiSwitch = findPreference(getString(R.string.key_setting_enable_wifi));
        wifiSwitch.setChecked(config.wenb);
    }

    private void saveAllPreferences(SensorConfig config) {
        saveSharedPreference(R.string.key_setting_ssid,config.ssid);
        saveSharedPreference(R.string.key_setting_ifxdb,config.ifxdb);
        saveSharedPreference(R.string.key_setting_ifxip,config.ifxip);
        saveSharedPreference(R.string.key_setting_ifxid,config.ifxid);
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
