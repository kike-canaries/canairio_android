package hpsaturn.pollutionreporter.view;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;

import com.github.pwittchen.reactivewifi.AccessRequester;
import com.github.pwittchen.reactivewifi.ReactiveWifi;
import com.hpsaturn.tools.DeviceUtil;
import com.hpsaturn.tools.Logger;
import com.hpsaturn.tools.UITools;
import com.iamhabib.easy_preference.EasyPreference;

import java.util.ArrayList;
import java.util.List;

import hpsaturn.pollutionreporter.AppData;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.common.Keys;
import hpsaturn.pollutionreporter.common.Storage;
import hpsaturn.pollutionreporter.models.ResponseConfig;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Antonio Vanegas @hpsaturn on 3/31/21.
 */
public class SettingsFragment extends SettingsBaseFragment {


    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_general, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        infoPreferenceInit();
        sendFeedbackInit();
        saveAppVersionString();
    }

    @Override
    protected void refreshUI() {

    }

    @Override
    protected void onConfigRead(ResponseConfig config) {

        boolean notify_sync = false;

        saveDeviceInfoString(config);

        if (notify_sync) {
            saveAllPreferences(config);
            printResponseConfig(config);
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
            Logger.i(TAG,"skip onSharedPreferenceChanged because is in reading mode!");

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
        EasyPreference.Builder prefBuilder = AppData.getPrefBuilder(getContext());
        prefBuilder.addString(Keys.DEVICE_FLAVOR,config.vflv).save();
    }

    private void saveAppVersionString() {
        String info = "rev"+DeviceUtil.getVersionCode(getContext())+" ("
                + DeviceUtil.getDeviceName() + ")";
        updatePrefTitle(R.string.key_appversion,"CanAirIO v"+DeviceUtil.getVersionName(getContext()));
        updateSummary(R.string.key_appversion,info);
        saveSharedPreference(R.string.key_appversion,info);
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
