package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.location.Location;

import com.google.android.material.snackbar.Snackbar;
import com.hpsaturn.tools.Logger;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import hpsaturn.pollutionreporter.R;

/**
 * Created by Antonio Vanegas @hpsaturn on 3/31/21.
 */
public abstract class SettingsBaseFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{

    public Location lastLocation;
    public Snackbar snackBar;
    public boolean onSensorReading;

}
