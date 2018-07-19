package hpsaturn.pollutionreporter.view;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new addDataTask().execute();
    }

    public void addData(int value){
        dataSet.addEntry(new Entry(i++,value));
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    private class addDataTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            loadData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            chart.invalidate();
            super.onPostExecute(aVoid);
        }
    }

    private void loadData() {
        ArrayList<SensorData> data = Storage.getData(getActivity());
        if(data.size()==0) addData(0);
        else{
            Logger.i(TAG,"[CHART] loading recorded data..");
            Iterator<SensorData> it = data.iterator();
            while (it.hasNext()){
                dataSet.addEntry(new Entry(i++,it.next().P25));
            }
            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
        }
    }

    private void loadTestData(){
        Random rand = new Random();
        int  n = rand.nextInt(50) + 1;
        addData(n);
        Logger.d(TAG,"size: "+entries.size());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void clearData() {
        Logger.w(TAG,"[CHART] clear recorded data and chart..");
        i=0;
        entries.clear();
        dataSet.clear();
        chart.clear();
        Storage.setData(getActivity(),new ArrayList<>());
    }
}
