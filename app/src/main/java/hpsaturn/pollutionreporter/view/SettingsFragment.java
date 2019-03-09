package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
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

    private String ssid, pass;
    private int stime;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        ssid = getSharedPreference(getString(R.string.key_setting_ssid));
        pass = getSharedPreference(getString(R.string.key_setting_pass));
        stime = getCurrentStime();

        updateStimeSummary();
        updateWifiSwitch();
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
            saveSensorWifi(sharedPreferences, key);
        }
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

    private void saveSensorWifi(SharedPreferences sharedPreferences, String key) {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_stime));

        SwitchPreferenceCompat switchPreference = (SwitchPreferenceCompat) findPreference(key);

        if(switchPreference.isChecked()) {
            String ssid = getSharedPreference(getString(R.string.key_setting_ssid));
            String pass = getSharedPreference(getString(R.string.key_setting_pass));
            getMain().showSnackMessage(R.string.msg_save_config);
            SensorConfig config = new SensorConfig();
            config.ssid = ssid;
            config.pass = pass;
            switchPreference.setSwitchTextOn(R.string.summary_wifi_enable);
            getMain().getServiceManager().writeSensorConfig(config);
        }
        else{
            // TODO: ???
        }
    }

    private void updateWifiSwitch(){
        SwitchPreferenceCompat wifiSwitch = (SwitchPreferenceCompat) findPreference(getString(R.string.key_setting_enable_wifi));
        String old_ssid = ssid;
        String old_pass = pass;
        ssid = getSharedPreference(getString(R.string.key_setting_ssid));
        pass = getSharedPreference(getString(R.string.key_setting_pass));
        wifiSwitch.setEnabled(!(ssid.length()==0 || pass.length()==0));
        if(!(old_ssid.equals(ssid) && old_pass.equals(pass))) {
            wifiSwitch.setChecked(false);   // TODO: force user to enable again?
        }
    }

    public void configCallBack(SensorConfig config) {
        if (config != null) {
            Logger.i(TAG, "Ifxdb: " + config.ifxdb);
            Logger.i(TAG, "Ifxip: " + config.ifxip);
            Logger.i(TAG, "Ifxid: " + config.ifxid);
            Logger.i(TAG, "Ifxtg: " + config.ifxtg);
            Logger.i(TAG, "ssid: " + config.ssid);
            Logger.i(TAG, "stime: " + config.stime);
            getMain().showSnackMessage(R.string.msg_config_saved);
            updatePreferencesSummmary(config);
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
