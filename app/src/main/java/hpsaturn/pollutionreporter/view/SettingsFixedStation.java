package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.fonfon.geohash.GeoHash;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.hpsaturn.tools.Logger;
import com.hpsaturn.tools.UITools;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.TimerTask;

import hpsaturn.pollutionreporter.Config;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.common.Keys;
import hpsaturn.pollutionreporter.models.CommandConfig;
import hpsaturn.pollutionreporter.models.GeoConfig;
import hpsaturn.pollutionreporter.models.InfluxdbConfig;
import hpsaturn.pollutionreporter.models.ResponseConfig;
import hpsaturn.pollutionreporter.models.SensorConfig;
import hpsaturn.pollutionreporter.models.WifiConfig;
import hpsaturn.pollutionreporter.common.Storage;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by Antonio Vanegas @hpsaturn on 2/17/19.
 */

public class SettingsFixedStation extends SettingsBaseFragment {

    public static final String TAG = SettingsFixedStation.class.getSimpleName();

    private String currentGeoHash = "";

    private Handler mHandler = new Handler();
    private UpdateTimeTask mUpdateTimeTask = new UpdateTimeTask();


    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_fixed_station, rootKey);
        ssidListenerInit();
        launchWorldMapInit();
        launchAnaireInit();
    }

    @Override
    protected void refreshUI(){
        Logger.i(TAG,"[Config] refreshUI");
        updateWifiSummary();
        lastLocation = SmartLocation.with(getActivity()).location().getLastLocation();
        updateLocationSummary(lastLocation,currentGeoHash);
        updateAnaireSummary();
        validateLocationSwitch();
    }

    @Override
    protected void onConfigRead(ResponseConfig config) {

        Logger.v(TAG, "[Config] reading config");
        boolean notify_sync = false;

        if (config.wenb != getWifiSwitch().isChecked()) {
            notify_sync = true;
        }
        if (config.ienb != getInfluxDbSwitch().isChecked()) {
            notify_sync = true;
        }
        if (config.geo == null ) {
            enableSwitch(R.string.key_setting_enable_ifx,false);
        }
        if (config.geo != null && config.geo.length() == 0 ) {
            enableSwitch(R.string.key_setting_enable_ifx,false);
        }
        else if (config.geo != null) {
            enableSwitch(R.string.key_setting_enable_ifx, true);
            currentGeoHash = config.geo;
        }
        if (config.anaireid != null && !config.anaireid.equals(getSharedPreference(R.string.key_anaire_id))){
            notify_sync = true;
        }

        refreshUI();
        updateWifiSummary();
        updateWifiSummary(config.wsta);
        printResponseConfig(config);

        if (notify_sync) {
            Logger.v(TAG, "[Config] rebuild UI");
            saveAllPreferences(config);
            updateSwitches(config);
            refreshUI();
            updateStatusSummary(true);
            updatePreferencesSummmary(config);
            Logger.v(TAG, "[Config] notify device sync complete");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (!onSensorReading) {

            if (key.equals(getString(R.string.key_setting_ssid))) {
                getWifiSwitch().setEnabled(isWifiSSIDValid());
            } else if (key.equals(getString(R.string.key_setting_enable_wifi))) {
                saveWifiConfig();
            } else if (key.equals(getString(R.string.key_setting_enable_ifx))) {
                saveInfluxDbConfig();
            } else if (key.equals(getString(R.string.key_setting_enable_location))) {
                saveLocation();
            }

        }
        else
            Logger.i(TAG,"[Config] skip onSharedPreferenceChanged because is in reading mode!");

    }

    /***********************************************************************************************
     * Wifi switch
     **********************************************************************************************/

    private void ssidListenerInit() {
        Preference sendFeedback = findPreference(getString(R.string.key_setting_ssid));
        assert sendFeedback != null;
        sendFeedback.setOnPreferenceClickListener(preference -> {
            getMain().showAccessPointsDialog();
            getWifiSwitch().setChecked(false);
            return true;
        });
    }

    private void saveWifiConfig() {

        if (getWifiSwitch().isChecked() && isWifiSSIDValid()) {
            WifiConfig config = new WifiConfig();
            config.ssid = getSharedPreference(getString(R.string.key_setting_ssid));
            config.pass = getSharedPreference(getString(R.string.key_setting_pass));
            config.wenb = true;
            updateWifiSummary();
            sendSensorConfig(config);
            mUpdateTimeTask.run(); // config read for update status
        }
        else
            setWifiSwitch(false);
    }

    private boolean isWifiSSIDValid() {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_enable_wifi));
        String ssid = getSharedPreference(getString(R.string.key_setting_ssid));
        Logger.v(TAG, "[Config] values -> " + ssid );
        return ssid.length() != 0;
    }

    private void setWifiSwitch(boolean checked) {
        SwitchPreference wifiSwitch = getWifiSwitch();
        wifiSwitch.setEnabled(isWifiSSIDValid());
        wifiSwitch.setChecked(checked);
        updateWifiSummary();
        enableWifiOnDevice(checked);
    }

    private void updateWifiSummary(){
        updateSummary(R.string.key_setting_ssid);
        updatePasswSummary(R.string.key_setting_pass);
        if(getWifiSwitch().isChecked())
            updatePrefTitle(R.string.key_setting_enable_wifi,getString(R.string.title_disable_wifi));
        else {
            updatePrefTitle(R.string.key_setting_enable_wifi, getString(R.string.title_save_wifi_settings));
            updateSummary(R.string.key_setting_enable_wifi,R.string.msg_status_disconnected);
        }
    }

    private void updateWifiSummary(boolean status){
        if (status)
            updateSummary(R.string.key_setting_enable_wifi,R.string.msg_status_connected);
        else
            updateSummary(R.string.key_setting_enable_wifi,R.string.msg_status_disconnected);
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
            config.ifxdb = getSharedPreference(R.string.key_setting_ifxdb,getString(R.string.key_ifxdb_default));;
            config.ifxip = getSharedPreference(R.string.key_setting_ifxip,getString(R.string.key_ifxip_default));
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
        String ifxdb = getSharedPreference(R.string.key_setting_ifxdb,getString(R.string.key_ifxdb_default));
        String ifxip = getSharedPreference(R.string.key_setting_ifxip,getString(R.string.key_ifxip_default));
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
     * Send feedback section
     **********************************************************************************************/

    private void launchWorldMapInit() {
        Preference stationsMap = findPreference(getString(R.string.key_fixed_stations_map));

        assert stationsMap != null;
        stationsMap.setOnPreferenceClickListener(preference -> {
            UITools.viewLink(getActivity(),getString(R.string.url_canairio_worldmap));
            return true;
        });
    }

    private void launchAnaireInit() {
        Preference anaireLaunch = findPreference(getString(R.string.key_anaire_id));

        assert anaireLaunch != null;
        anaireLaunch.setOnPreferenceClickListener(preference -> {
            String anaireId = getSharedPreference(R.string.key_anaire_id);
            String url = getString(R.string.url_anaire_realtime)+anaireId+"/";
            Logger.v(TAG, "[Config] Anaire time serie: " + url);

            UITools.viewLink(getActivity(),url);
            return true;
        });
    }

    private void updateAnaireSummary(){
        String anaireId = getSharedPreference(R.string.key_anaire_id);
        String summary = getString(R.string.summary_anaire_timeseries)+" "+anaireId;
        updateSummary(R.string.key_anaire_id,summary);
    }

    /***********************************************************************************************
     * InfluxDb Geohash parameter
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
        updateLocationSummary(lastLocation, currentGeoHash);
    }

    private void updateLocationSummary(Location location, String geoHash) {
        if (location != null) {
            SwitchPreference ifxdbSwitch = getInfluxDbSwitch();
            String summary = "";
            if (geoHash.length() == 0 ) {
                summary = summary+"none.";
                ifxdbSwitch.setSummary(R.string.summary_ifx_nogeohash);
            }
            else if (geoHash.length() >=2 ) {
                try {
                    summary = summary + geoHash;
                    ifxdbSwitch.setSummary(R.string.summary_key_enable_ifx_ready);
                    String name = geoHash.substring(0,3);
                    String flavor = Storage.getString(Keys.DEVICE_FLAVOR,"", requireContext());
                    if (flavor.length()>6) flavor = flavor.substring(0,7);
                    name = name + flavor;
                    name = name + Storage.getString(Keys.DEVICE_ADDRESS,"", requireContext()).substring(10);
                    name = name.replace("_","");
                    name = name.replace(":","");
                    name = name.toUpperCase();
                    name = getString(R.string.summary_fixed_stations_map)+" "+name;
                    updateSummary(R.string.key_fixed_stations_map,name);
                } catch (Exception e) {
                    String flavor = Storage.getString(Keys.DEVICE_FLAVOR,"", requireContext());
                    FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
                    crashlytics.setCustomKey(Keys.DEVICE_FLAVOR,flavor);
                    crashlytics.setCustomKey("geohash",geoHash);
                    e.printStackTrace();
                }
            }
            String geo = " (new: " + GeoHash.fromLocation(location, Config.GEOHASHACCU).toString()+")";
            summary = summary + geo;
            updateSummary(R.string.key_setting_enable_location,summary);
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
        saveSharedPreference(R.string.key_anaire_id, config.anaireid);
    }

    class UpdateTimeTask extends TimerTask {
        private int counter = 0;
        public void run() {
            int retries = 12;
            Logger.i(TAG,"[Config] wifi switch task: read sensor config "+counter+"/"+retries);
            readSensorConfig();
            if(counter++> retries) {
                counter = 0;
                this.cancel();
            }
            else mHandler.postDelayed(this,5000);
        }
    }

    @Override
    public void onDestroy() {
        Logger.i(TAG,"[Config] onDestroy");
        try {
            mHandler.removeCallbacks(mUpdateTimeTask);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

}
