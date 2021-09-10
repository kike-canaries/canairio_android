package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.hpsaturn.tools.Logger;

import org.apache.commons.lang3.math.NumberUtils;

import hpsaturn.pollutionreporter.R;
import com.jetbrains.handson.commons.models.ResponseConfig;

/**
 * Created by Antonio Vanegas @hpsaturn on 4/8/21.
 */
public class SettingsCustomInfluxDB extends SettingsBaseFragment {
    @Override
    protected void refreshUI() {
        updateInfluxSummary();
    }

    @Override
    protected void onConfigRead(ResponseConfig config) {
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

        if (notify_sync) {
            saveAllPreferences(config);
            printResponseConfig(config);
            updateInfluxSummary();
            Logger.v(TAG, "[Config] notify device sync complete");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!onSensorReading) {

            if (key.equals(getString(R.string.key_setting_ifxip))) {
                updateInfluxIPSummary();
            } else if (key.equals(getString(R.string.key_setting_ifxdb))) {
                updateInfluxDbSummmary();
            } else if (key.equals(getString(R.string.key_setting_ifxpt))) {
                updateInfluxPortSummary();
            }

        }
        else
            Logger.i(TAG,"skip onSharedPreferenceChanged because is in reading mode!");
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_custom_influxdb, rootKey);

    }

    private void saveAllPreferences(ResponseConfig config) {
        saveSharedPreference(R.string.key_setting_ifxdb, config.ifxdb);
        saveSharedPreference(R.string.key_setting_ifxip, config.ifxip);
        saveSharedPreference(R.string.key_setting_ifxpt, ""+config.ifxpt);
    }

    private String getInfluxDbDname() {
        return getSharedPreference(getString(R.string.key_setting_ifxdb));
    }

    private String getInfluxDbIP() {
        return getSharedPreference(getString(R.string.key_setting_ifxip));
    }

    private int getInfluxDbPort() {
        return NumberUtils.toInt(getSharedPreference(getString(R.string.key_setting_ifxpt)),8086);
    }

    private void updateInfluxSummary(){
        updateInfluxDbSummmary();
        updateInfluxIPSummary();
        updateInfluxPortSummary();
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
}
