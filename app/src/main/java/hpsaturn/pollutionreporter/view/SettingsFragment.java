package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.os.Bundle;
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


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        updateStimeSummary();
        updateWifiSwitch();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.key_setting_stime))) {
            saveSensorSampleTime(sharedPreferences, key);
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

    private void saveSensorSampleTime(SharedPreferences sharedPreferences, String key) {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_stime));
        int stime = 0;
        try {
            stime = Integer.parseInt(sharedPreferences.getString(key, "5"));
        } catch (NumberFormatException e) { e.printStackTrace(); }
        if (stime >= 5) {
            getMain().showSnackMessage(R.string.msg_save_device_setting);
            SensorConfig config = new SensorConfig();
            config.stime = stime;
            getMain().getServiceManager().writeSensorConfig(config);
        } else {
            saveSharedPreference(key, "5");
            getMain().showSnackMessage(R.string.msg_stime_error);
        }
        updateStimeSummary();
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

            getMain().showSnackMessage(R.string.msg_save_device_setting);
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
        String ssid = getSharedPreference(getString(R.string.key_setting_ssid));
        String pass = getSharedPreference(getString(R.string.key_setting_pass));
        wifiSwitch.setEnabled(!(ssid.length()==0 || pass.length()==0));
    }

    public void configCallBack(SensorConfig config) {
        if (config != null) {
            Logger.i(TAG, "Ifxdb: " + config.ifxdb);
            Logger.i(TAG, "Ifxip: " + config.ifxip);
            Logger.i(TAG, "Ifxid: " + config.ifxid);
            Logger.i(TAG, "Ifxtg: " + config.ifxtg);
            Logger.i(TAG, "ssid: " + config.ssid);
            Logger.i(TAG, "stime: " + config.stime);
        }
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
