package hpsaturn.pollutionreporter.view;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;

import hpsaturn.pollutionreporter.R;

/**
 * created by antonio vanegas @hpsaturn on 7/23/18.
 */

public class PickerFragmentData {

    public static PickerFragmentData get() {
        return new PickerFragmentData();
    }

    private PickerFragmentData() {
    }

    public List<PickerFragmentInfo> getFragmentsInfo(Context ctx) {
        Resources res = ctx.getResources();
        return Arrays.asList(
                new PickerFragmentInfo(res.getString(R.string.title_icon_map), R.drawable.ic_picker_map),
                new PickerFragmentInfo(res.getString(R.string.title_icon_reports), R.drawable.ic_picker_cloud),
                new PickerFragmentInfo(res.getString(R.string.title_icon_mydevice), R.drawable.ic_picker_chart),
                new PickerFragmentInfo(res.getString(R.string.title_icon_myrecords), R.drawable.ic_picker_records),
                new PickerFragmentInfo(res.getString(R.string.title_icon_settings), R.drawable.ic_picker_settings)
        );

    }
}
