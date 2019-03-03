package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hpsaturn.tools.Logger;

import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;

/**
 * Created by Antonio Vanegas @hpsaturn on 2/17/19.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = SettingsFragment.class.getSimpleName();


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals(getString(R.string.key_setting_stime))){
            saveSensorSampleTime(sharedPreferences, key);
        }

    }

    private void saveSensorSampleTime(SharedPreferences sharedPreferences, String key){
        Logger.v(TAG,"[Config] "+getString(R.string.key_setting_stime));
        Preference pref = findPreference(key);
        int stime = 0;
        try {
            stime = Integer.parseInt(sharedPreferences.getString(key,"5"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if(stime>=5) {
            pref.setSummary(""+stime+" seconds");
            getMain().showSnackMessage(R.string.msg_save_device_setting);
            getMain().saveSampleTime(stime);
        }else{
            sharedPreferences.edit().putString(getString(R.string.key_setting_stime),"5").apply();
            pref.setSummary(R.string.summary_stime);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private MainActivity getMain() {
        return ((MainActivity) getActivity());
    }

}
