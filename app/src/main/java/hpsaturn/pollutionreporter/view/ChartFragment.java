package hpsaturn.pollutionreporter.view;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.hpsaturn.tools.DeviceUtil;
import com.hpsaturn.tools.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hpsaturn.pollutionreporter.Config;
import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.common.Storage;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.models.SensorTrack;
import hpsaturn.pollutionreporter.models.SensorTrackInfo;

/**
 * Created by Antonio Vanegas @hpsaturn on 6/30/18.
 */
public class ChartFragment extends Fragment {

    public static String TAG = ChartFragment.class.getSimpleName();

    @BindView(R.id.lc_measures)
    LineChart chart;

    @BindView(R.id.tv_chart_name)
    TextView chart_name;

    @BindView(R.id.tv_chart_date)
    TextView chart_date;

    @BindView(R.id.tv_chart_desc)
    TextView chart_desc;

    @BindView(R.id.tv_chart_loc)
    TextView chart_loc;

    @BindView(R.id.rl_separator)
    RelativeLayout rl_separator;

    LineDataSet PM25line;
    LineDataSet Templine;
    LineDataSet Humiline;
    LineDataSet CO2line;

    List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
    ArrayList<Integer> colors = new ArrayList<Integer>();

    private List<Entry> entriesPM25 = new ArrayList<Entry>();
    private List<Entry> entriesTemp = new ArrayList<Entry>();
    private List<Entry> entriesHumi = new ArrayList<Entry>();
    private List<Entry> entriesCO2  = new ArrayList<Entry>();

    private long referenceTimestamp;
    private boolean loadingData = true;

    private static final String KEY_RECORD_ID = "key_record_id";
    private String recordId;
    private SensorTrack track;


    public static ChartFragment newInstance() {
        return new ChartFragment();
    }

    public static ChartFragment newInstance(String recordId) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString(KEY_RECORD_ID,recordId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        ButterKnife.bind(this, view);

        Description description = new Description();
        description.setTextColor(getResources().getColor(R.color.black));
        description.setText(getString(R.string.app_name));
        description.setTextSize(12f);
        description.setTextAlign(Paint.Align.RIGHT);

        chart.setDescription(description);
        chart.setNoDataText(getString(R.string.msg_chart_loading));

        //Display the axis on the left (contains the labels 1*, 2* and so on)
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setEnabled(true);

        calculateReferenceTime();

        PM25line = getPM25LineDataSet(entriesPM25);
        Templine = getGenericLineDataSet(entriesTemp,R.color.red,"T",false);
        Humiline = getGenericLineDataSet(entriesHumi,R.color.blue,"H",false);
        CO2line = getGenericLineDataSet(entriesCO2,R.color.brown,"CO2",true);


        Bundle args = getArguments();
        if(args!=null){
            recordId = args.getString(KEY_RECORD_ID) ;
            Logger.i(TAG,"[CHART] recordId: "+recordId);
        }

        return view;
    }

    private LineDataSet getGenericLineDataSet(List<Entry> entry, int color, String label,boolean fill) {

        LineDataSet dataSet = new LineDataSet(entry,label);
        dataSet.setColor(getResources().getColor(color));
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setDrawFilled(fill);
        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        return dataSet;
    }

    private LineDataSet getPM25LineDataSet(List<Entry> entry) {

        LineDataSet dataSet = new LineDataSet(entry,"PM2.5");
        dataSet.setColor(getResources().getColor(R.color.green));
//        dataSet.setDrawFilled(true);
//        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.aqi_gradient_fill);
//        dataSet.setFillDrawable(drawable);
        dataSet.setHighlightEnabled(true);
//        dataSet.setCircleRadius(3.5f);
//        dataSet.setDrawCircleHole(false);
//        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
//        dataSet.setValueTextSize(11.0f);
//        dataSet.setValueTextColor(R.color.green);
        dataSet.setCircleRadius(4.0f);
        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSet.setLineWidth(1.5f);
//        dataSet.setValueTextSize(20f);
//        dataSet.enableDashedLine(6f, 18f, 0);

        return dataSet;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.i(TAG,"[CHART] starting load data thread..");
        requireActivity().runOnUiThread(this::loadData);
    }

    private void calculateReferenceTime(){
        ArrayList<SensorData> data = Storage.getSensorData(getActivity());
        if (data.isEmpty()) {
            referenceTimestamp = System.currentTimeMillis() / 1000;
        } else {
            referenceTimestamp = data.get(0).timestamp;
        }
        HourAxisValueFormatter xAxisFormatter = new HourAxisValueFormatter(referenceTimestamp);
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(xAxisFormatter);
    }


    public void addData(SensorData data) {
        if (!loadingData) {
            Long currentTime = System.currentTimeMillis() / 1000;
            long time = currentTime - referenceTimestamp;
            addValue(time,data);
            refreshDataSets();
        }
    }

    private void addData(ArrayList<SensorData> data){
        if(data==null)return;
        else if (!data.isEmpty()) {
            Iterator<SensorData> it = data.iterator();
            while (it.hasNext()) {
                SensorData d = it.next();
                long time = d.timestamp - referenceTimestamp;
                addValue(time,d);
            }

            refreshDataSets();
        }
        loadingData = false;
    }

    private void addValue(long time, SensorData data) {
        PM25line.addEntry(new Entry(time, data.P25));
        Templine.addEntry(new Entry(time, data.tmp));
        Humiline.addEntry(new Entry(time, data.hum));
        CO2line.addEntry(new Entry(time, data.CO2));

        if (data.P25 <= 20) colors.add(getResources().getColor(R.color.green));
        if (data.P25 > 20 && data.P25 <= 90) colors.add(getResources().getColor(R.color.yellow));
        if (data.P25 > 90 && data.P25 <= 120) colors.add(getResources().getColor(R.color.orange));
        if (data.P25 > 120 ) colors.add(getResources().getColor(R.color.red));
    }

    private void refreshDataSets() {
        dataSets.clear();

        PM25line.setCircleColors(colors);

        dataSets.add(PM25line);
        dataSets.add(Templine);
        dataSets.add(Humiline);
        dataSets.add(CO2line);

        LineData linedata = new LineData(dataSets);

        chart.setData(linedata);
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private void loadData() {
        Logger.i(TAG,"[CHART] loading data..");
        loadingData = true;
        ArrayList<SensorData> data = new ArrayList<>();
        if(recordId==null) {
            Logger.i(TAG,"[CHART] loading current data in storage..");
            data = Storage.getSensorData(getActivity());
        }
        else {
            track = Storage.getTrack(getActivity(), recordId);
            if(track!=null) {
                Logger.i(TAG,"[CHART] loading track data from storage..");
                data = track.data;
                setTrackDescription(track);
                getMain().enableShareButton();
            }
            else{
                Logger.i(TAG,"[CHART] loading track from firebase..");
                DatabaseReference trackRef = getMain().getDatabase().child(Config.FB_TRACKS_DATA).child(recordId);
                trackRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        SensorTrack track = dataSnapshot.getValue(SensorTrack.class);
                        if(track!=null){
                            Logger.i(TAG,"[CHART] loading track on chart..");
                            addData(track.data);
                            setTrackDescription(track);
                        }
                        else{
                            Logger.e(TAG,"[CHART] onDataChange getValue is null");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Logger.e(TAG,"[CHART] onCancelled, databaseError: "+databaseError.getDetails());
                    }
                });
            }
        }
        addData(data);
    }

    private void setTrackDescription(SensorTrack track){
        chart_name.setText(track.getName());
        chart_date.setText(track.getDate());
        chart_desc.setText(""+track.size+" points");
        rl_separator.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        getMain().disableShareButton();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void clearData() {
        Logger.w(TAG, "[CHART] clear recorded data and chart..");
        colors.clear();
        PM25line.clear();
        Templine.clear();
        Humiline.clear();
        CO2line.clear();
        chart.clear();
        Storage.setSensorData(getActivity(), new ArrayList<>());
        calculateReferenceTime();
    }

    public void shareAction(){
        if(recordId!=null && track!=null) {
            Logger.i(TAG,"publis track..");
            track.deviceId = DeviceUtil.getDeviceId(getActivity());
            getMain().getDatabase().child(Config.FB_TRACKS_DATA).child(track.name).setValue(track);
            getMain().getDatabase().child(Config.FB_TRACKS_INFO).child(track.name).setValue(new SensorTrackInfo(track));
            getMain().popBackLastFragment();
        }
    }

    private MainActivity getMain(){
        return (MainActivity)getActivity();
    }

}
