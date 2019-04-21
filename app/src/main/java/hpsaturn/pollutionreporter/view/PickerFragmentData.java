package hpsaturn.pollutionreporter.view;

import java.util.Arrays;
import java.util.List;

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

    public List<PickerFragmentInfo> getFragmentsInfo() {

        return Arrays.asList(
                new PickerFragmentInfo("Reports", R.drawable.ic_picker_map),
                new PickerFragmentInfo("Public", R.drawable.ic_picker_cloud),
                new PickerFragmentInfo("MyDevice", R.drawable.ic_picker_chart),
                new PickerFragmentInfo("MyRecords", R.drawable.ic_picker_records),
                new PickerFragmentInfo("Settings", R.drawable.ic_picker_settings)
        );

    }
}
