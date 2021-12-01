package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.hpsaturn.tools.Logger;

import org.apache.commons.lang3.math.NumberUtils;

import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.HassIPConfig;
import hpsaturn.pollutionreporter.models.HassPasswConfig;
import hpsaturn.pollutionreporter.models.HassPortConfig;
import hpsaturn.pollutionreporter.models.HassUserConfig;
import hpsaturn.pollutionreporter.models.ResponseConfig;

/**
 * Created by Antonio Vanegas @hpsaturn on 4/8/21.
 */
public class SettingsAdvancedCloud extends SettingsBaseFragment {

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_custom_influxdb, rootKey);
    }

    @Override
    protected void refreshUI() {
        updateAllSummary();
    }

    @Override
    protected void onConfigRead(ResponseConfig config) {

        Logger.v(TAG, "[Config] reading config");
        boolean notify_sync = false;

        if (!config.ifxdb.equals(getInfluxDbDname())){
            notify_sync = true;
        }

        if (!config.ifxdb.equals(getInfluxDbIP())){
            notify_sync = true;
        }

        if (config.ifxpt != getInfluxDbPort()){
            notify_sync = true;
        }

        if (!config.hassip.equals(getHassIp())){
            notify_sync = true;
        }

        if (!config.hassusr.equals(getHassUser())){
            notify_sync = true;
        }

        if (!config.hasspsw.equals(getHassPassw())){
            notify_sync = true;
        }

        if (config.hasspt != getHassPort()){
            notify_sync = true;
        }

        if (notify_sync) {
            saveAllPreferences(config);
            printResponseConfig(config);
            updateAllSummary();
            Logger.v(TAG, "[Config] notify device sync complete");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!onSensorReading) {

            if (key.equals(getString(R.string.key_setting_hassip))) {
                saveDeviceHassIp(getHassIp());
            }
            else if (key.equals(getString(R.string.key_setting_hassusr))) {
                saveDeviceHassUser(getHassUser());
            }
            else if (key.equals(getString(R.string.key_setting_hasspsw))) {
                saveDeviceHassPassw(getHassPassw());
            }
            else if (key.equals(getString(R.string.key_setting_hasspt))) {
                saveDeviceHassPort(getHassPort());
            }
            updateAllSummary();
        }
        else
            Logger.i(TAG,"skip onSharedPreferenceChanged because is in reading mode!");
    }

    private void saveDeviceHassIp(String ip) {
        Logger.v(TAG, "[Config] sending Hass IP : "+ip);
        HassIPConfig config = new HassIPConfig();
        config.hassip = ip;
        sendSensorConfig(config);
    }

    private void saveDeviceHassUser(String user) {
        Logger.v(TAG, "[Config] sending Hass user : "+user);
        HassUserConfig config = new HassUserConfig();
        config.hassusr = user;
        sendSensorConfig(config);
    }

    private void saveDeviceHassPassw(String passw) {
        Logger.v(TAG, "[Config] sending Hass passw");
        HassPasswConfig config = new HassPasswConfig();
        config.hasspsw = passw;
        sendSensorConfig(config);
    }

    private void saveDeviceHassPort(int port) {
        Logger.v(TAG, "[Config] sending Hass port : "+port);
        HassPortConfig config = new HassPortConfig();
        config.hasspt = port;
        sendSensorConfig(config);
    }


    private void saveAllPreferences(ResponseConfig config) {
        saveSharedPreference(R.string.key_setting_ifxdb, config.ifxdb);
        saveSharedPreference(R.string.key_setting_ifxip, config.ifxip);
        saveSharedPreference(R.string.key_setting_ifxpt, ""+config.ifxpt);
        saveSharedPreference(R.string.key_setting_hassip, ""+config.hassip);
        saveSharedPreference(R.string.key_setting_hassusr, ""+config.hassusr);
        saveSharedPreference(R.string.key_setting_hasspsw, ""+config.hasspsw);
        saveSharedPreference(R.string.key_setting_hasspt, ""+config.hasspt);
    }

    private String getInfluxDbDname() {
        return getSharedPreference(R.string.key_setting_ifxdb);
    }

    private String getInfluxDbIP() {
        return getSharedPreference(R.string.key_setting_ifxip);
    }

    private int getInfluxDbPort() {
        return NumberUtils.toInt(getSharedPreference(getString(R.string.key_setting_ifxpt)),8086);
    }

    private String getHassIp() {
        return getSharedPreference(R.string.key_setting_hassip);
    }

    private String getHassUser() {
        return getSharedPreference(R.string.key_setting_hassusr);
    }

    private String getHassPassw() {
        return getSharedPreference(R.string.key_setting_hasspsw);
    }

    private int getHassPort() {
        return NumberUtils.toInt(getSharedPreference(getString(R.string.key_setting_hasspt)),1883);
    }



    private void updateAllSummary(){
        updateInfluxDbSummmary();
        updateInfluxIPSummary();
        updateInfluxPortSummary();
        updateHassIpSummmary();
        updateHassUserSummary();
        updateHassPortSummary();
    }

    private void updateInfluxDbSummmary() {
        updateSummary(R.string.key_setting_ifxdb);
    }

    private void updateInfluxIPSummary() {
        updateSummary(R.string.key_setting_ifxip);
    }

    private void updateInfluxPortSummary() {
        updateSummary(R.string.key_setting_ifxpt);
    }

    private void updateHassIpSummmary() {
        updateSummary(R.string.key_setting_hassip);
    }

    private void updateHassUserSummary() {
        updateSummary(R.string.key_setting_hassusr);
    }

    private void updateHassPortSummary() {
        updateSummary(R.string.key_setting_hasspt);
    }
}
