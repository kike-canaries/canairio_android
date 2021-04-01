package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;

import com.hpsaturn.tools.Logger;

import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.ResponseConfig;
import hpsaturn.pollutionreporter.models.SampleConfig;
import hpsaturn.pollutionreporter.models.SensorType;

/**
 * Created by Antonio Vanegas @hpsaturn on 3/31/21.
 */
public class SettingsFragment extends SettingsBaseFragment {

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_general, rootKey);
    }

    @Override
    protected void refreshUI() {
        updateStimeSummary();
    }

    @Override
    protected void onConfigRead(ResponseConfig config) {

        Logger.d(TAG, "[Config] onConfigCallBack");

        printResponseConfig(config);

        boolean notify_sync = false;

        if (!getStimeFormat(config.stime).equals(getStimeFormat(getCurrentStime()))){
            notify_sync = true;
        }
        if (config.stype != getSensorType()) {
            if (config.stype < 0) updateSensorTypeSummary(0);
            else updateSensorTypeSummary((config.stype));
        }

        setStatusSwitch(true);

        if (notify_sync) {
            saveAllPreferences(config);
            updateStatusSummary(true);
            updatePreferencesSummmary(config);
            Logger.v(TAG, "[Config] notify device sync complete");
            getMain().showSnackMessage(R.string.msg_sync_complete);
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (!onSensorReading) {

            if (key.equals(getString(R.string.key_setting_stime))) {
                validateSensorSampleTime();
            }
            else if (key.equals(getString(R.string.key_setting_dtype))) {
                sendSensorTypeConfig();
            }
            else if (key.equals(getString(R.string.key_setting_vars))) {
                getMain().selectedVarsUpdated();
            }

        }
        else
            Logger.i(TAG,"skyp onSharedPreferenceChanged because is in reading mode!");

    }

    /***********************************************************************************************
     * Sample time handlers
     **********************************************************************************************/

    private void validateSensorSampleTime() {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_stime));
        if (getCurrentStime() >= 5) {
            saveSensorSampleTime(getCurrentStime());
        }
    }

    private void saveSensorSampleTime(int time) {
        Logger.v(TAG, "[Config] sending sensor time: "+time);
        SampleConfig config = new SampleConfig();
        config.stime = time;
        updateSummary(R.string.key_setting_stime,getStimeFormat(config.stime));
        sendSensorConfig(config);
    }

    private int getCurrentStime() {
        try {
            return Integer.parseInt(getSharedPreference(getString(R.string.key_setting_stime)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void resetStimeValue(int stime) {
        saveSharedPreference(R.string.key_setting_stime, "" + stime);
        updateStimeSummary();
    }

    /***********************************************************************************************
     * Sensor type section
     **********************************************************************************************/

    private void sendSensorTypeConfig() {
        Logger.v(TAG, "[Config] validating->" + getString(R.string.key_setting_dtype));
        getMain().showSnackMessage(R.string.msg_reboot_manually);
        SensorType config = new SensorType();
        config.stype = getSensorType();
        sendSensorConfig(config);
    }

    private void updateSensorTypeSummary(int value) {
        ListPreference sizePreference = findPreference(getString(R.string.key_setting_dtype));
        sizePreference.setValueIndex(value);
    }

    private int getSensorType() {
        try {
            return Integer.parseInt(getSharedPreference(getString(R.string.key_setting_dtype)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    String getStimeFormat(int time){
        return "" + time + " seconds";
    }

    private void updateStimeSummary() {
        updateSummary(R.string.key_setting_stime,getStimeFormat(getCurrentStime()));
    }

    private void updatePreferencesSummmary(ResponseConfig config) {
        if(config.stime>0)updateSummary(R.string.key_setting_stime, getStimeFormat(config.stime));
    }


    /***********************************************************************************************
     * Update Preferences methods
     **********************************************************************************************/

    private void saveAllPreferences(ResponseConfig config) {
        resetStimeValue(config.stime);
    }

}
