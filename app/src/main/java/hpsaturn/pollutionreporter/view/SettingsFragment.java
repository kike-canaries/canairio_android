package hpsaturn.pollutionreporter.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;

import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.ResponseConfig;

/**
 * Created by Antonio Vanegas @hpsaturn on 3/31/21.
 */
public class SettingsFragment extends SettingsBaseFragment {


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_general, rootKey);
    }

    @Override
    protected void refreshUI() {

    }

    @Override
    protected void onConfigRead(ResponseConfig config) {

    }

}
