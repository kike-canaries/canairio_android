package hpsaturn.pollutionreporter.view;

import java.util.Arrays;
import java.util.List;

import hpsaturn.pollutionreporter.R;

/**
 * created by antonio vanegas @hpsaturn on 7/23/18.
 */

public class FragmentPickerData {

    public static FragmentPickerData get() {
        return new FragmentPickerData();
    }

    private FragmentPickerData() {
    }

    public List<FragmentPickerInfo> getFragmentsInfo() {

        return Arrays.asList(
                new FragmentPickerInfo("Reports", R.drawable.ic_picker_map),
                new FragmentPickerInfo("Public", R.drawable.ic_picker_cloud),
                new FragmentPickerInfo("MyDevice", R.drawable.ic_picker_chart),
                new FragmentPickerInfo("MyRecords", R.drawable.ic_picker_records)
        );

    }
}
