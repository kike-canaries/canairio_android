package hpsaturn.pollutionreporter.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.hpsaturn.tools.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.common.Storage;
import hpsaturn.pollutionreporter.models.SensorData;

/**
 * Created by Antonio Vanegas @hpsaturn on 6/30/18.
 */
public class ChartFragment extends Fragment {

    public static String TAG = ChartFragment.class.getSimpleName();

    @BindView(R.id.lc_measures)
    LineChart chart;

    private List<Entry> entries = new ArrayList<Entry>();
    private LineDataSet dataSet;
    private int i;
    private long referenceTimestamp;
    private boolean loadingData = true;


    public static ChartFragment newInstance() {
        ChartFragment fragment = new ChartFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        ButterKnife.bind(this, view);

        chart.setDescription(getString(R.string.app_name));

        calculateReferenceTime();

        dataSet = new LineDataSet(entries, getString(R.string.label_pm25));
        dataSet.setColor(R.color.colorPrimary);
        dataSet.setHighlightEnabled(true);
        dataSet.setValueTextColor(R.color.colorPrimaryDark);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().runOnUiThread(this::loadData);
    }

    private void calculateReferenceTime(){
        ArrayList<SensorData> data = Storage.getData(getActivity());
        if (data.isEmpty()) {
            referenceTimestamp = System.currentTimeMillis() / 1000;
        } else {
            referenceTimestamp = data.get(0).timestamp;
        }
        AxisValueFormatter xAxisFormatter = new HourAxisValueFormatter(referenceTimestamp);
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(xAxisFormatter);
    }

    public void addData(int value) {
        if (!loadingData) {
            Long currentTime = System.currentTimeMillis() / 1000;
            Long time = currentTime - referenceTimestamp;
            dataSet.addEntry(new Entry(time, value));
            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.invalidate();
        }
    }

    private void loadData() {
        loadingData = true;
        ArrayList<SensorData> data = Storage.getData(getActivity());
        if (data.isEmpty()) addData(0);
        else {
            Logger.i(TAG, "[CHART] loading recorded data..");
            Iterator<SensorData> it = data.iterator();
            while (it.hasNext()) {
                SensorData value = it.next();
                Long time = value.timestamp - referenceTimestamp;
                dataSet.addEntry(new Entry(time, value.P25));
            }
            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.invalidate();
        }
        loadingData = false;
    }

    private void loadTestData() {
        Random rand = new Random();
        int n = rand.nextInt(50) + 1;
        addData(n);
        Logger.d(TAG, "size: " + entries.size());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        calculateReferenceTime();
    }

    public void clearData() {
        Logger.w(TAG, "[CHART] clear recorded data and chart..");
        entries.clear();
        dataSet.clear();
        chart.clear();
        Storage.setData(getActivity(), new ArrayList<>());
        calculateReferenceTime();
    }
}
