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
import hpsaturn.pollutionreporter.models.AltitudeOffsetConfig;
import hpsaturn.pollutionreporter.models.CommandConfig;
import hpsaturn.pollutionreporter.models.SeaLevelConfig;
import hpsaturn.pollutionreporter.models.TempOffsetConfig;
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
        updateSeaLevelSummary();
        updateAltitudeOffsetSummary();
    }

    @Override
    protected void onConfigRead(ResponseConfig config) {
        if (config.stype != getSensorType()) {
            if (config.stype < 0) updateSensorTypeSummary(0);
            else updateSensorTypeSummary((config.stype));
        }
        saveAllPreferences(config);
        updateStatusSummary(true);
        printResponseConfig(config);
        updatePreferencesSummmary(config);
        refreshUI();
        updateSwitches(config);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (!onSensorReading && key != null) {

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
            else if (key.equals(getString(R.string.key_setting_send_co2_calibration))) {
                performCO2Calibration();
            }
            else if (key.equals(getString(R.string.key_setting_enable_clear))) {
                performClearDevice();
            }
            else if (key.equals(getString(R.string.key_setting_debug_enable))) {
                performEnableDebugMode();
            }
            else if (key.equals(getString(R.string.key_setting_solarstation_enable))) {
                performSaveSolarMode();
            }
            else if (key.equals(getString(R.string.key_setting_temp_offset))) {
                saveTempOffset(getCurrentTempOffset());
            }
            else if (key.equals(getString(R.string.key_setting_altitude_offset))) {
                saveAltitudeOffset(getCurrentAltitudeOffset());
            }
            else if (key.equals(getString(R.string.key_setting_sealevel))) {
                saveSeaLevel(getCurrentSeaLevel());
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

    private void saveTempOffset(float offset) {
        Logger.v(TAG, "[Config] sending temperature offset : "+offset);
        TempOffsetConfig config = new TempOffsetConfig();
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
     * Altitude offset handlers
     *********************************************************************************************/

    private float getCurrentAltitudeOffset() {
        return NumberUtils.toFloat(getSharedPreference(getString(R.string.key_setting_altitude_offset)),0);
    }

    private void saveAltitudeOffset(float offset) {
        Logger.v(TAG, "[Config] sending altitude offset : "+offset);
        AltitudeOffsetConfig config = new AltitudeOffsetConfig();
        config.altoffset = offset;
        updateSummary(R.string.key_setting_altitude_offset,""+offset);
        sendSensorConfig(config);
    }

    private void resetSeaLevelValue(float sealevel) {
        saveSharedPreference(R.string.key_setting_sealevel, "" + sealevel);
        updateAltitudeOffsetSummary();
    }

    private void updateAltitudeOffsetSummary() {
        float altitude = getCurrentAltitudeOffset();
        if (altitude == 0)
            updateSummary(R.string.key_setting_altitude_offset,R.string.summary_altitude_offset);
        else
            updateSummary(R.string.key_setting_altitude_offset,""+getCurrentAltitudeOffset());
    }

    /***********************************************************************************************
     * Sealevel Altitude
     *********************************************************************************************/

    private void resetAltitudeOffsetValue(float offset) {
        saveSharedPreference(R.string.key_setting_altitude_offset, "" + offset);
        updateAltitudeOffsetSummary();
    }

    private void updateSeaLevelSummary() {
        float sealevel = getCurrentAltitudeOffset();
        updateSummary(R.string.key_setting_sealevel,""+getCurrentSeaLevel());
    }


    private void saveSeaLevel(float sealevel) {
        Logger.v(TAG, "[Config] sending sealevel: "+ sealevel);
        SeaLevelConfig config = new SeaLevelConfig();
        config.sealevel = sealevel;
        updateSummary(R.string.key_setting_sealevel,""+sealevel);
        sendSensorConfig(config);
    }

    private float getCurrentSeaLevel() {
        return NumberUtils.toFloat(getSharedPreference(getString(R.string.key_setting_sealevel)), 1013.25F);
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
     * Solar Station Mode
     *********************************************************************************************/

    private void performSaveSolarMode() {
        SwitchPreference solarSwitch = findPreference(getString(R.string.key_setting_solarstation_enable));
        saveSolarModeFlag(solarSwitch.isChecked());
    }

    private void saveSolarModeFlag(boolean enable) {
        Logger.v(TAG, "[Config] solar station mode: "+enable);
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "sse";
        config.sse = enable;
        sendSensorConfig(config);
    }

    /***********************************************************************************************
     * CO2 calibration command
     **********************************************************************************************/

    private void performCO2Calibration() {
        SwitchPreference co2CalibrationSwitch = findPreference(getString(R.string.key_setting_send_co2_calibration));
        if (!co2CalibrationSwitch.isChecked()) {
            if(snackBar!=null)snackBar.dismiss();
        } else {
            snackBar = getMain().getSnackBar(R.string.bt_device_co2_calibration, R.string.bt_device_co2_calibration_action, view -> {
                co2CalibrationSwitch.setChecked(false);
                sendCO2CalibrationCommand();
                getMain().showSnackMessageSlow(R.string.msg_device_calibrate_msg);
            });
            snackBar.show();
        }
    }

    private void sendCO2CalibrationCommand() {
        CommandConfig config = new CommandConfig();
        config.cmd = getSharedPreference(getString(R.string.key_setting_wmac));
        config.act = "clb";
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
                sendSensorConfig(config);
                getMain().showSnackMessageSlow(R.string.msg_device_clear);
                Handler handler = new Handler();
                handler.postDelayed(() -> clearDevice(), 3000);
            });
            snackBar.show();
        }
    }

    private void clearDevice() {
        clearSharedPreferences();
        getMain().finish();
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
        resetSeaLevelValue(config.sealevel);
        resetAltitudeOffsetValue(config.altoffset);
    }

    private void updateSwitches(SensorConfig config){
        updateSwitch(R.string.key_setting_debug_enable,config.denb);
        updateSwitch(R.string.key_setting_force_i2c_sensors,config.i2conly);
        updateSwitch(R.string.key_setting_solarstation_enable,config.sse);
    }

    private SwitchPreference getDebugEnableSwitch() {
        return findPreference(getString(R.string.key_setting_debug_enable));
    }

    private SwitchPreference getI2CForcedSwitch() {
        return findPreference(getString(R.string.key_setting_force_i2c_sensors));
    }

}
