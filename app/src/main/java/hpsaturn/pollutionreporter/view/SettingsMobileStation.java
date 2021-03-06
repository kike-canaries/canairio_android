package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;

import com.hpsaturn.tools.Logger;

import org.apache.commons.lang3.math.NumberUtils;

import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.CommandConfig;
import hpsaturn.pollutionreporter.models.OffsetConfig;
import hpsaturn.pollutionreporter.models.ResponseConfig;
import hpsaturn.pollutionreporter.models.SampleConfig;
import hpsaturn.pollutionreporter.models.SensorConfig;
import hpsaturn.pollutionreporter.models.SensorType;

/**
 * Created by Antonio Vanegas @hpsaturn on 3/31/21.
 */
public class SettingsMobileStation extends SettingsBaseFragment{

    public static final String TAG = SettingsMobileStation.class.getSimpleName();

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_mobile_station, rootKey);
    }

    @Override
    protected void refreshUI() {
        updateStimeSummary();
        updateTempOffsetSummary();
    }

    @Override
    protected void onConfigRead(ResponseConfig config) {

        boolean notify_sync = false;

        if (!getStimeFormat(config.stime).equals(getStimeFormat(getCurrentStime()))){
            notify_sync = true;
        }
        if (config.stype != getSensorType()) {
            if (config.stype < 0) updateSensorTypeSummary(0);
            else updateSensorTypeSummary((config.stype));
        }
        if (config.denb != getDebugEnableSwitch().isChecked()) {
            notify_sync = true;
        }
        if (config.i2conly != getI2CForcedSwitch().isChecked()) {
            notify_sync = true;
        }
        if ((int)config.toffset != (int)getCurrentTempOffset()){
            notify_sync = true;
        }

        if (notify_sync) {
            saveAllPreferences(config);
            updateStatusSummary(true);
            updateSwitches(config);
            updatePreferencesSummmary(config);
            Logger.v(TAG, "[Config] notify device sync complete");
            printResponseConfig(config);
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
            else if (key.equals(getString(R.string.key_setting_force_i2c_sensors))) {
                performForceI2CSensos();
            }
            else if (key.equals(getString(R.string.key_setting_enable_reboot))) {
                performRebootDevice();
            }
            else if (key.equals(getString(R.string.key_setting_enable_clear))) {
                performClearDevice();
            }
            else if (key.equals(getString(R.string.key_setting_debug_enable))) {
                performEnableDebugMode();
            }
            else if (key.equals(getString(R.string.key_setting_temp_offset))) {
                saveTempeOffset(getCurrentTempOffset());
            }
        }
        else
            Logger.i(TAG,"skip onSharedPreferenceChanged because is in reading mode!");

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
        return NumberUtils.toInt(getSharedPreference(getString(R.string.key_setting_stime)),0);
    }

    private void resetStimeValue(int stime) {
        saveSharedPreference(R.string.key_setting_stime, "" + stime);
        updateStimeSummary();
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
     * Temperature offset handlers
     *********************************************************************************************/

    private float getCurrentTempOffset() {
        return NumberUtils.toFloat(getSharedPreference(getString(R.string.key_setting_temp_offset)),0);
    }

    private void saveTempeOffset(float offset) {
        Logger.v(TAG, "[Config] sending temperature offset : "+offset);
        OffsetConfig config = new OffsetConfig();
        config.toffset = offset;
        updateSummary(R.string.key_setting_temp_offset,""+offset);
        sendSensorConfig(config);
    }

    private void resetTempOffsetValue(float offset) {
        saveSharedPreference(R.string.key_setting_temp_offset, "" + offset);
        updateSummary(R.string.key_setting_temp_offset,""+offset);
    }

    private void updateTempOffsetSummary() {
        updateSummary(R.string.key_setting_temp_offset,""+getCurrentTempOffset());
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

    /***********************************************************************************************
     * Forced i2c sensors only
     *********************************************************************************************/

    private void performForceI2CSensos() {
        SwitchPreference i2cSwitch = findPreference(getString(R.string.key_setting_force_i2c_sensors));
        savei2cOnlyFlag(i2cSwitch.isChecked());
        snackBar = getMain().getSnackBar(R.string.bt_device_clear, R.string.bt_device_reboot_action, view -> {
            sendSensorReboot();
            Handler handler = new Handler();
            handler.postDelayed(() -> getMain().finish(), 3000);
        });
        snackBar.show();
    }

    private void savei2cOnlyFlag(boolean enable) {
        Logger.v(TAG, "[Config] forced i2c sensors : "+enable);
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "i2c";
        config.i2conly = enable;
        sendSensorConfig(config);
    }

    /***********************************************************************************************
     * Sensor tools
     **********************************************************************************************/

    private void performRebootDevice() {
        SwitchPreference rebootSwitch = findPreference(getString(R.string.key_setting_enable_reboot));
        if (!rebootSwitch.isChecked()) {
            if(snackBar!=null)snackBar.dismiss();
        } else {
            snackBar = getMain().getSnackBar(R.string.bt_device_reboot, R.string.bt_device_reboot_action, view -> {
                rebootSwitch.setChecked(false);
                sendSensorReboot();
                getMain().showSnackMessageSlow(R.string.msg_device_reboot);
                Handler handler = new Handler();
                handler.postDelayed(() -> getMain().finish(), 3000);
            });
            snackBar.show();
        }
    }

    private void sendSensorReboot() {
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "rbt";
        sendSensorConfig(config);
    }

    private void performClearDevice() {
        SwitchPreference clearSwitch = findPreference(getString(R.string.key_setting_enable_clear));
        if (!clearSwitch.isChecked()) {
            if(snackBar!=null)snackBar.dismiss();
        } else {
            snackBar = getMain().getSnackBar(R.string.bt_device_clear, R.string.bt_device_clear_action, view -> {
                clearSwitch.setChecked(false);
                CommandConfig config = new CommandConfig();
                config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
                config.act = "cls";
                getMain().showSnackMessageSlow(R.string.msg_device_clear);
                sendSensorConfig(config);
                clearSharedPreferences();
                Handler handler = new Handler();
                handler.postDelayed(() -> getMain().finish(), 3000);
            });
            snackBar.show();
        }
    }

    private void performEnableDebugMode() {
        SwitchPreference debugEnableSwitch = getDebugEnableSwitch();
        enableDebugMode(debugEnableSwitch.isChecked());
    }

    private void enableDebugMode(boolean enable) {
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "dst";
        config.denb = enable;
        sendSensorConfig(config);
    }

    /***********************************************************************************************
     * Update Preferences methods
     **********************************************************************************************/

    private void saveAllPreferences(ResponseConfig config) {
        resetStimeValue(config.stime);
        resetTempOffsetValue(config.toffset);
    }

    private void updateSwitches(SensorConfig config){
        updateSwitch(R.string.key_setting_debug_enable,config.denb);
        updateSwitch(R.string.key_setting_force_i2c_sensors,config.i2conly);
    }

    private SwitchPreference getDebugEnableSwitch() {
        return findPreference(getString(R.string.key_setting_debug_enable));
    }

    private SwitchPreference getI2CForcedSwitch() {
        return findPreference(getString(R.string.key_setting_force_i2c_sensors));
    }


}
