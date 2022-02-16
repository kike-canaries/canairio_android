package hpsaturn.pollutionreporter.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;

/**
 * Created by Antonio Vanegas @hpsaturn on 5/22/21.
 */
public class ShareDialogFragment extends DialogFragment {

    public static final String TAG = ShareDialogFragment.class.getSimpleName();

    EditText metadata;

    public static ShareDialogFragment newInstance() {
        ShareDialogFragment dialog = new ShareDialogFragment();
        return dialog;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.share_dialog_input, container, false);
        Button mButtonShare = (Button) v.findViewById(R.id.bt_share_dialog_share);
        Button mButtonExport = (Button) v.findViewById(R.id.bt_share_dialog_export);
        metadata = (EditText) v.findViewById(R.id.et_share_dialog_meta);
        mButtonShare.setOnClickListener(onClickShareListener);
        mButtonExport.setOnClickListener(onClickExportListener);
        return v;

    }

    private final View.OnClickListener onClickShareListener = view -> {
        getMain().shareAction(metadata.getText().toString(),true);
        getDialog().dismiss();
    };

    private final View.OnClickListener onClickExportListener = view -> {
        getMain().shareAction(metadata.getText().toString(),false);
        getDialog().dismiss();
    };


    private MainActivity getMain() {
        return ((MainActivity) getActivity());
    }

}
