package hpsaturn.pollutionreporter.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ListView;

import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.hpsaturn.tools.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.common.Storage;

/**
 * Created by Antonio Vanegas @hpsaturn on 5/22/21.
 */
public class ScanAccesPointFragment extends DialogFragment {

    public static final String TAG = ScanAccesPointFragment.class.getSimpleName();

    String ssids[];

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.title_access_points_dialog));

        List<String> ssids_list = Storage.getTempAPList(getActivity());

        int count = 0;
        ssids = new String[ssids_list.size()];
        Iterator<String> it = ssids_list.iterator();
        while (it.hasNext()) ssids[count++]=it.next();

        builder.setSingleChoiceItems(ssids,-1,this::setVaribleFilter);

        return builder.create();
    }

    private void setVaribleFilter(DialogInterface dialogInterface, int i) {
        Logger.d(TAG,"selected SSID: "+ssids[i]);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.key_setting_ssid),ssids[i]);
        editor.apply();
        getMain().updatePreferencesSSID(ssids[i]);
        dismiss();
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG,"onDestroy");
        super.onDestroy();
    }

    private MainActivity getMain() {
        return ((MainActivity) getActivity());
    }

}
