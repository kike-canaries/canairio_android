package hpsaturn.pollutionreporter.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.hpsaturn.tools.DeviceUtil;

import hpsaturn.pollutionreporter.R;

/**
 * Created by izel on 9/11/15.
 */
public class AboutFragment extends DialogFragment {
    public static final String TAG = AboutFragment.class.getSimpleName();

    private ScrollView _sv_about;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.about, container, false);
        TextView aboutText = view.findViewById(R.id.tv_about_version_revision);

        aboutText.setText(
                String.format(getString(R.string.about_licence_l1),
                        DeviceUtil.getVersionName(requireActivity()),
                        DeviceUtil.getVersionCode(requireActivity())+""
                )
        );

        _sv_about = view.findViewById(R.id.sv_about);
        Animation translatebu= AnimationUtils.loadAnimation(getActivity(), R.anim.about);
        _sv_about.startAnimation(translatebu);

        return view;
    }

}
