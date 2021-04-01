package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.hpsaturn.tools.Logger;
import com.hpsaturn.tools.UITools;

import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.ResponseConfig;

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
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        infoPreferenceInit();
        sendFeedbackInit();
    }

    @Override
    protected void onConfigRead(ResponseConfig config) {

        Logger.d(TAG, "[Config] onConfigCallBack");

        printResponseConfig(config);

        boolean notify_sync = false;

        saveDeviceInfoString(config);
        setStatusSwitch(true);

        if (notify_sync) {
            saveAllPreferences(config);
            updateStatusSummary(true);
            Logger.v(TAG, "[Config] notify device sync complete");
            getMain().showSnackMessage(R.string.msg_sync_complete);
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (!onSensorReading) {

            if (key.equals(getString(R.string.key_setting_vars))) {
                getMain().selectedVarsUpdated();
            }

        }
        else
            Logger.i(TAG,"skyp onSharedPreferenceChanged because is in reading mode!");

    }


    /***********************************************************************************************
     * Device info
     **********************************************************************************************/

    private void infoPreferenceInit() {
        Preference infoPreference = findPreference(getString(R.string.key_device_info));

        assert infoPreference != null;
        infoPreference.setOnPreferenceClickListener(preference -> {
            readSensorConfig();
            return true;
        });
    }

    private void saveDeviceInfoString(ResponseConfig config) {
        String info = "MAC:"
                + config.vmac
                +"\nFirmware: "+config.vflv+" rev"+config.vrev+" ("+config.vtag+")"
                +"\n[WiFi:"+(config.wenb ? "On" : "Off")+"][IFDB:"+(config.ienb ? "On" : "Off")
                +"][GW:"+(config.wsta ? "On" : "Off")+"]";
        if(config.vrev<774)info="\n!!YOUR FIRMWARE IS OUTDATED!!\n\n"+info;
        updateSummary(R.string.key_device_info,info);
        saveSharedPreference(R.string.key_device_info,info);
    }

    /***********************************************************************************************
     * Send feedback section
     **********************************************************************************************/

    private void sendFeedbackInit() {
        Preference sendFeedback = findPreference(getString(R.string.key_send_feedback));

        assert sendFeedback != null;
        sendFeedback.setOnPreferenceClickListener(preference -> {
            UITools.viewLink(getActivity(),getString(R.string.url_github_android_app_issues));
            return true;
        });
    }

    /***********************************************************************************************
     * Update Preferences methods
     **********************************************************************************************/

    private void saveAllPreferences(ResponseConfig config) {
        saveDeviceInfoString(config);
    }

}
