package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hpsaturn.tools.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import hpsaturn.pollutionreporter.Keys;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.SensorData;

/**
 * Created by Antonio Vanegas @hpsaturn on 6/30/18.
 */
public class ChartFragment extends Fragment{

    public static String TAG = ChartFragment.class.getSimpleName();

    @BindView(R.id.lc_measures)
    LineChart chart;

    private List<Entry> entries = new ArrayList<Entry>();
    private LineDataSet dataSet;
    private int i;


    public static  ChartFragment newInstance(){
        ChartFragment fragment = new ChartFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chart,container,false);
        ButterKnife.bind(this,view);

        Description description = chart.getDescription();
        description.setText(getString(R.string.app_name));
        dataSet = new LineDataSet(entries,getString(R.string.label_pm25));
        dataSet.setColor(R.color.colorPrimary);
        dataSet.setHighlightEnabled(true);
        dataSet.setValueTextColor(R.color.colorPrimaryDark);

        loadData();
        return view;
    }

    private void loadData() {
        Logger.i(TAG,"[CHART] loading recorded data..");
        ArrayList<SensorData> data = getData();
        if(data.size()==0) addData(0);
        else{
            Iterator<SensorData> it = data.iterator();
            while (it.hasNext())addData(it.next().P25);
        }
    }

    private void loadTestData(){
        Random rand = new Random();
        int  n = rand.nextInt(50) + 1;
        addData(n);
        Logger.d(TAG,"size: "+entries.size());
    }

    public void addData(int value){
        dataSet.addEntry(new Entry(i++,value));
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    private ArrayList<SensorData> getData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String ringJson = preferences.getString(Keys.SENSOR_DATA, "");
        if (ringJson.equals("")) return new ArrayList<>();
        else {
            Type listType = new TypeToken<ArrayList<SensorData>>() {
            }.getType();
            return new Gson().fromJson(ringJson, listType);
        }
    }

    public void setData( ArrayList<SensorData> items) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Keys.SENSOR_DATA, new Gson().toJson(items));
        editor.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void clearData() {
        Logger.w(TAG,"[CHART] clear recorded data and chart..");
        entries.clear();
        dataSet.clear();
        chart.clear();
        setData(new ArrayList<>());
    }
}
