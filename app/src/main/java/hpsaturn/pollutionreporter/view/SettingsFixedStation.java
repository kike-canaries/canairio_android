package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.preference.SwitchPreference;

import com.fonfon.geohash.GeoHash;
import com.hpsaturn.tools.Logger;

import org.apache.commons.lang3.math.NumberUtils;

import java.text.DecimalFormat;

import hpsaturn.pollutionreporter.Config;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.CommandConfig;
import hpsaturn.pollutionreporter.models.GeoConfig;
import hpsaturn.pollutionreporter.models.InfluxdbConfig;
import hpsaturn.pollutionreporter.models.ResponseConfig;
import hpsaturn.pollutionreporter.models.SensorConfig;
import hpsaturn.pollutionreporter.models.SensorName;
import hpsaturn.pollutionreporter.models.WifiConfig;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by Antonio Vanegas @hpsaturn on 2/17/19.
 */

public class SettingsFixedStation extends SettingsBaseFragment {

    public static final String TAG = SettingsFixedStation.class.getSimpleName();

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_fixed_station, rootKey);
    }

//    public void rebuildUI(){
//        getPreferenceScreen().removeAll();
//        addPreferencesFromResource(R.xml.settings_fixed_station);
//    }


    @Override
    protected void refreshUI(){
        Logger.i(TAG,"[Config] refreshUI");
        updateWifiSummary();
        lastLocation = SmartLocation.with(getActivity()).location().getLastLocation();
        updateLocationSummary();
        validateLocationSwitch();
    }

    @Override
    protected void onConfigRead(ResponseConfig config) {

        boolean notify_sync = false;

        if (config.wenb != getWifiSwitch().isChecked()) {
            notify_sync = true;
        }
        if (config.ienb != getInfluxDbSwitch().isChecked()) {
            notify_sync = true;
        }

        updateLocationSummary();
        validateLocationSwitch();

        if (notify_sync) {
            saveAllPreferences(config);
            printResponseConfig(config);
            updateSwitches(config);
            updateStatusSummary(true);
            updatePreferencesSummmary(config);
            Logger.v(TAG, "[Config] notify device sync complete");
//            getMain().showSnackMessage(R.string.msg_sync_complete);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (!onSensorReading) {

            if (key.equals(getString(R.string.key_setting_ssid))) {
                getWifiSwitch().setEnabled(isWifiSwitchFieldsValid());
            } else if (key.equals(getString(R.string.key_setting_enable_wifi))) {
                saveWifiConfig();
            } else if (key.equals(getString(R.string.key_setting_enable_ifx))) {
                saveInfluxDbConfig();
            } else if (key.equals(getString(R.string.key_setting_enable_location))) {
                saveLocation();
            }

        }
        else
            Logger.i(TAG,"skip onSharedPreferenceChanged because is in reading mode!");

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
            updateWifiSummary();
            sendSensorConfig(config);
        }
        else
            setWifiSwitch(false);
    }

    private boolean isWifiSwitchFieldsValid() {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_enable_wifi));
        String ssid = getSharedPreference(getString(R.string.key_setting_ssid));
        Logger.v(TAG, "[Config] values -> " + ssid );
        updateWifiSummary();
        return ssid.length() != 0;
    }

    private void setWifiSwitch(boolean checked) {
        SwitchPreference wifiSwitch = getWifiSwitch();
        wifiSwitch.setEnabled(isWifiSwitchFieldsValid());
        wifiSwitch.setChecked(checked);
        updateWifiSummary();
        enableWifiOnDevice(checked);
    }

    private void updateWifiSummary(){
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
     * InfluxDB switch
     **********************************************************************************************/

    private void saveInfluxDbConfig() {

        if (getInfluxDbSwitch().isChecked() && isInfluxDbSwitchFieldsValid()) {
            InfluxdbConfig config = new InfluxdbConfig();
            config.ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb),getString(R.string.key_ifxdb_default));;
            config.ifxip = getSharedPreference(getString(R.string.key_setting_ifxip),getString(R.string.key_ifxip_default));
            config.ifxpt = NumberUtils.toInt(getSharedPreference(getString(R.string.key_setting_ifxpt)),8086);
            config.ienb = true;
            sendSensorConfig(config);
            findPreference(R.string.key_setting_ifx_advanced).setEnabled(false);
        } else {
            findPreference(R.string.key_setting_ifx_advanced).setEnabled(true);
            setInfluxDbSwitch(false);
        }
    }

    private boolean isInfluxDbSwitchFieldsValid(){
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_ifxdb));
        String ifxdb = getSharedPreference(getString(R.string.key_setting_ifxdb),getString(R.string.key_ifxdb_default));
        String ifxip = getSharedPreference(getString(R.string.key_setting_ifxip),getString(R.string.key_ifxip_default));
        Logger.v(TAG, "[Config] values -> " + ifxdb +  " " + ifxip);
        return !(ifxdb.length() == 0 || ifxip.length() == 0);
    }

    private void setInfluxDbSwitch(boolean checked) {
        Logger.v(TAG, "[Config] InfluxDb switch check -> " + checked);
        SwitchPreference ifxdbSwitch = getInfluxDbSwitch();
        ifxdbSwitch.setEnabled(isInfluxDbSwitchFieldsValid());
        ifxdbSwitch.setChecked(checked);
        enableInfluxDbOnDevice(checked);
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
                    config.geo = GeoHash.fromLocation(lastLocation, Config.GEOHASHACCU).toString();
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
            DecimalFormat precision = new DecimalFormat("0.000");
            String accu = "Accu:" + (int) lastLocation.getAccuracy() + "m ";
            String lat = "(" + precision.format(lastLocation.getLatitude());
            String lon = "," + precision.format(lastLocation.getLongitude()) + ")";
            String geo = "\nGeohash tag: " + GeoHash.fromLocation(lastLocation, Config.GEOHASHACCU).toString();
            updateSummary(R.string.key_setting_enable_location,accu + lat + lon + geo);
        }
    }

    /***********************************************************************************************
     * Update methods
     **********************************************************************************************/


    private void updatePreferencesSummmary(ResponseConfig config) {
        if(config.ssid !=null)updateSummary(R.string.key_setting_ssid,config.ssid);
        updatePasswSummary(R.string.key_setting_pass);
    }

    private void updatePasswSummary(int key) {
        String passw = getSharedPreference(getString(key));
        if(passw.length()>0) updateSummary(key,R.string.msg_passw_seted);
        else updateSummary(key,R.string.msg_passw_unseted);
    }

    private void updateSwitches(SensorConfig config){
        updateSwitch(R.string.key_setting_enable_wifi,config.wenb);
        updateSwitch(R.string.key_setting_enable_ifx,config.ienb);
    }

    /***********************************************************************************************
     * Update Preferences methods
     **********************************************************************************************/

    private void saveAllPreferences(ResponseConfig config) {
        saveSharedPreference(R.string.key_setting_ssid, config.ssid);
        saveSharedPreference(R.string.key_setting_ifxdb, config.ifxdb);
        saveSharedPreference(R.string.key_setting_ifxip, config.ifxip);
    }

}
