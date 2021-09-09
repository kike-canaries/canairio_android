package hpsaturn.pollutionreporter.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.hpsaturn.tools.Logger;

import java.util.Set;

import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;

/**
 * Created by Antonio Vanegas @hpsaturn on 5/22/21.
 */
public class VariableFileterFragment extends DialogFragment {

    public static final String TAG = VariableFileterFragment.class.getSimpleName();


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        Set<String> values = preferences.getStringSet(getString(R.string.key_setting_vars), null);

        String[] options = getResources().getStringArray(R.array.pref_vars_values);

        boolean[] selected = new boolean[options.length];

        for (int i = 0 ; i < options.length ; i++) {
            selected[i] = values.contains(options[i]);
        }

        builder.setMultiChoiceItems(R.array.pref_vars_entries, selected, this::setVaribleFilter);

        return builder.create();
    }

    private void setVaribleFilter(DialogInterface dialog, int item, boolean isChecked) {
        Logger.i(TAG, "item: " + item + " isChecked: "+isChecked);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        Set<String> values = preferences.getStringSet(getString(R.string.key_setting_vars), null);

        String[] options = getResources().getStringArray(R.array.pref_vars_values);

        if(isChecked) values.add(options[item]);
        else values.remove(options[item]);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(getString(R.string.key_setting_vars),values);
        editor.apply();
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG,"onDestroy");
        getMain().selectedVarsUpdated();
        super.onDestroy();
    }

    private MainActivity getMain() {
        return ((MainActivity) getActivity());
    }

}
