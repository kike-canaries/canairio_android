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

    public List<FragmentPickerInfo> getForecasts() {

        return Arrays.asList(
                new FragmentPickerInfo("Map", R.drawable.ic_picker_map),
                new FragmentPickerInfo("Data", R.drawable.ic_picker_chart),
                new FragmentPickerInfo("Reports", R.drawable.ic_picker_records),
                new FragmentPickerInfo("Public", R.drawable.ic_picker_records)
        );

    }
}
