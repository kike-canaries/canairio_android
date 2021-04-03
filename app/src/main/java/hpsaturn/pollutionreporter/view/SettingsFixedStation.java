package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.SwitchPreference;

import com.hpsaturn.tools.Logger;

import java.text.DecimalFormat;

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

    public void rebuildUI(){
        getPreferenceScreen().removeAll();
        addPreferencesFromResource(R.xml.settings_fixed_station);
    }

    public void refreshUI(){
        Logger.i(TAG,"[Config] refreshUI");
        updateSensorNameSummary();
        updateWifiSummary();
        updateInfluxDbSummmary();
        updateInfluxPortSummary();
        updateLocationSummary();
        lastLocation = SmartLocation.with(getActivity()).location().getLastLocation();
        validateLocationSwitch();
    }

    @Override
    protected void onConfigRead(ResponseConfig config) {
        Logger.d(TAG, "[Config] onConfigCallBack");

        printResponseConfig(config);

        boolean notify_sync = false;

        if (getSensorName().length() > 0 && !getSensorName().equals(config.dname)){
            notify_sync = true;
        }
        if (config.wenb != getWifiSwitch().isChecked()) {
            notify_sync = true;
        }
        if (!config.ifxdb.equals(getInfluxDbDname())){
            notify_sync = true;
        }
        if (config.ienb != getInfluxDbSwitch().isChecked()) {
            notify_sync = true;
        }

        updateLocationSummary();
        validateLocationSwitch();

        if (notify_sync) {
            saveAllPreferences(config);
            updateSwitches(config);
            rebuildUI();
            updateStatusSummary(true);
            updatePreferencesSummmary(config);
            Logger.v(TAG, "[Config] notify device sync complete");
            getMain().showSnackMessage(R.string.msg_sync_complete);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (!onSensorReading) {

            if (key.equals(getString(R.string.key_setting_dname))) {
                saveSensorName(getSensorName());
            } else if (key.equals(getString(R.string.key_setting_ssid))) {
                getWifiSwitch().setEnabled(isWifiSwitchFieldsValid());
            } else if (key.equals(getString(R.string.key_setting_pass))) {
                getWifiSwitch().setEnabled(isWifiSwitchFieldsValid());
            } else if (key.equals(getString(R.string.key_setting_enable_wifi))) {
                saveWifiConfig();
            } else if (key.equals(getString(R.string.key_setting_ifxdb))) {
                getInfluxDbSwitch().setEnabled(isInfluxDbSwitchFieldsValid());
            } else if (key.equals(getString(R.string.key_setting_ifxip))) {
                getInfluxDbSwitch().setEnabled(isInfluxDbSwitchFieldsValid());
            } else if (key.equals(getString(R.string.key_setting_enable_ifx))) {
                saveInfluxDbConfig();
            } else if (key.equals(getString(R.string.key_setting_enable_location))) {
                saveLocation();
            }

        }
        else
            Logger.i(TAG,"skyp onSharedPreferenceChanged because is in reading mode!");

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
        }
        updateSensorNameSummary();
    }

    String getSensorName() {
        return getSharedPreference(getString(R.string.key_setting_dname));
    }

    private void updateSensorNameSummary() {
        updateSummary(R.string.key_setting_dname);
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
        String pass = getSharedPreference(getString(R.string.key_setting_pass));
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

    private String getInfluxDbDname() {
        return getSharedPreference(getString(R.string.key_setting_ifxdb));
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
            DecimalFormat precision = new DecimalFormat("0.000");
            String accu = "Accu:" + (int) lastLocation.getAccuracy() + "m ";
            String lat = "(" + precision.format(lastLocation.getLatitude());
            String lon = "," + precision.format(lastLocation.getLongitude()) + ")";
            updateSummary(R.string.key_setting_enable_location,accu + lat + lon);
        }
    }

    /***********************************************************************************************
     * Update methods
     **********************************************************************************************/


    private void updatePreferencesSummmary(ResponseConfig config) {
        if(config.dname !=null)updateSummary(R.string.key_setting_dname,config.dname);
        if(config.ssid !=null)updateSummary(R.string.key_setting_ssid,config.ssid);
        if(config.ifxdb !=null)updateSummary(R.string.key_setting_ifxdb,config.ifxdb);
        if(config.ifxip !=null)updateSummary(R.string.key_setting_ifxip,config.ifxip);
        updatePasswSummary(R.string.key_setting_pass);

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

    private void updateSwitch(int key,boolean enable){
        SwitchPreference mSwitch = findPreference(getString(key));
        mSwitch.setChecked(enable);
    }

    private void updateSwitches(SensorConfig config){
        updateSwitch(R.string.key_setting_enable_wifi,config.wenb);
        updateSwitch(R.string.key_setting_enable_ifx,config.ienb);
    }

    /***********************************************************************************************
     * Update Preferences methods
     **********************************************************************************************/

    private void saveAllPreferences(ResponseConfig config) {
        saveSharedPreference(R.string.key_setting_dname, config.dname);
        saveSharedPreference(R.string.key_setting_ssid, config.ssid);
        saveSharedPreference(R.string.key_setting_ifxdb, config.ifxdb);
        saveSharedPreference(R.string.key_setting_ifxip, config.ifxip);
        saveSharedPreference(R.string.key_setting_wmac, "" + config.wmac);
    }

}
