package hpsaturn.pollutionreporter.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hpsaturn.tools.DeviceUtil;
import com.hpsaturn.tools.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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


    private List<Entry> entries = new ArrayList<Entry>();
    private LineDataSet dataSet;
    private int i;
    private long referenceTimestamp;
    private boolean loadingData = true;

    public static final String KEY_RECORD_ID = "key_record_id";
    private String recordId;
    private SensorTrack track;

    public static ChartFragment newInstance() {
        ChartFragment fragment = new ChartFragment();
        return fragment;
    }

    public static ChartFragment newInstance(String recordId) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString(KEY_RECORD_ID,recordId);
        fragment.setArguments(args);
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

        Bundle args = getArguments();
        if(args!=null){
            recordId = args.getString(KEY_RECORD_ID) ;
            Logger.i(TAG,"[CHART] recordId: "+recordId);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().runOnUiThread(this::loadData);
    }

    private void calculateReferenceTime(){
        ArrayList<SensorData> data = Storage.getSensorData(getActivity());
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
                DatabaseReference mDataBase = FirebaseDatabase.getInstance().getReference("tracks_data");
                DatabaseReference trackRef = mDataBase.child(recordId);
                trackRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Logger.i(TAG,"[CHART] loading track data from firebase..");
                        SensorTrack track = dataSnapshot.getValue(SensorTrack.class);
                        if(track!=null){
                            loadChart(track.data);
                            setTrackDescription(track);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
        loadChart(data);
    }

    private void loadChart(ArrayList<SensorData> data){
        if (data.isEmpty()) addData(0);
        else {
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

    private void setTrackDescription(SensorTrack track){
        chart_name.setText(track.getName());
        chart_date.setText(track.getDate());
        chart_desc.setText(""+ track.size+" points");
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
        calculateReferenceTime();
    }

    public void clearData() {
        Logger.w(TAG, "[CHART] clear recorded data and chart..");
        entries.clear();
        dataSet.clear();
        chart.clear();
        Storage.setSensorData(getActivity(), new ArrayList<>());
        calculateReferenceTime();
    }

    public void shareAction(){
        if(recordId!=null && track!=null) {
            Logger.i(TAG,"publis track..");
            track.deviceId = DeviceUtil.getDeviceId(getActivity());
            getMain().getDatabase().child("tracks_data").child(track.name).setValue(track);
            getMain().getDatabase().child("tracks_info").child(track.name).setValue(new SensorTrackInfo(track));
            getMain().popBackLastFragment();
        }
    }

    private MainActivity getMain(){
        return (MainActivity)getActivity();
    }

}
