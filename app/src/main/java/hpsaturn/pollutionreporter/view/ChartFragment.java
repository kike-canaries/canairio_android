package hpsaturn.pollutionreporter.view;

import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.hpsaturn.tools.DeviceUtil;
import com.hpsaturn.tools.Logger;
import com.hpsaturn.tools.UITools;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static String TAG_INFO = "INFO"+ChartFragment.class.getSimpleName();

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

    private long referenceTimestamp;

    private boolean loadingData = true;  // it's true for block data from real time chart

    private static final String KEY_RECORD_ID = "key_record_id";
    private String recordId;
    private SensorTrack track;

    private List<ChartVar> variables = new ArrayList<>();

    private List<ILineDataSet> dataSets = new ArrayList<>();

    private Map<String,String> maplabels = new HashMap<>();

    private List<GeoPoint> geoPoints = new ArrayList<>();
    private MapView mapView;

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

        // Try to load old track (records view)
        View view;
        Bundle args = getArguments();
        if(args!=null){
            recordId = args.getString(KEY_RECORD_ID) ;
            Logger.i(TAG,"[CHART] recordId: "+recordId);
            view = inflater.inflate(R.layout.fragment_chart, container, false);
            mapView = view.findViewById(R.id.mapview);
        }else{
            view = inflater.inflate(R.layout.fragment_chart_realtime, container, false);
        }

        ButterKnife.bind(this, view);

        Description description = new Description();
        description.setTextColor(getResources().getColor(R.color.black));
        description.setText(getString(R.string.app_name));
        description.setTextSize(16f);
        description.setTextAlign(Paint.Align.RIGHT);

        chart.setDescription(description);
        chart.setNoDataText(getString(R.string.msg_chart_loading));

        // Display the axis on the left (contains the labels 1*, 2* and so on)
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setEnabled(true);
        // Time reference for XAxis
        calculateReferenceTime();
        // Current variables supported
        String[] labels = getResources().getStringArray(R.array.pref_vars_entries);
        String[] types = getResources().getStringArray(R.array.pref_vars_values);
        // Hash map of types and labels (it will not change)
        for (int i = 0; i < labels.length ; i++){
            maplabels.put(types[i],labels[i]);
        }

        chart_name.setOnClickListener(onChartIdClickListener);

        return view;
    }

    private void setupMap() {
        mapView.setVisibility(View.VISIBLE);
        mapView.setClickable(true);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setMultiTouchControls(true);
        mapView.setMaxZoomLevel((double) 19);
        mapView.getController().setZoom((double) 17); //set initial zoom-level, depends on your need
        mapView.setUseDataConnection(true); //keeps the mapView from loading online tiles using network connection.
        mapView.setEnabled(true);
        (mapView.getTileProvider().getTileCache()).getProtectedTileComputers().clear();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.i(TAG,"[CHART] starting load data thread..");
        loadSelectedVariables();
        if(recordId!=null) requireActivity().runOnUiThread(this::setupMap);
    }

    /**
     * initialization of data for chart and map
     * load data of current recording track or old recorded track
     */
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
                setTrackDescription(track,false);
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
                            setTrackDescription(track, true);
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

    public void loadSelectedVariables(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getMain());
        Set<String> values = preferences.getStringSet(getString(R.string.key_setting_vars), null);

        if(values == null ) return;
        Logger.i(TAG,"[CHART] loadSelectedVariables");
        variables.clear();
        Logger.i(TAG, "[CHART] selected values:");

        for (String type : values) {
            ChartVar var = new ChartVar(getContext(), type, maplabels.get(type));
            variables.add(var);
            Logger.i(TAG, "[CHART]"+type);
        }

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


    /**
     *  Add data from previous data (recorded track for example)
      * @param data
     */
    private void addData(ArrayList<SensorData> data){
        if(data==null){
            chart.setNoDataText("No data.");
            loadingData = false;
            return;
        }
        else if (!data.isEmpty()) {
            Iterator<SensorData> it = data.iterator();
            int count = 0;
            while (it.hasNext() && count++ <=5000) {
                SensorData d = it.next();
                long time = d.timestamp - referenceTimestamp;
                addValue(time,d);
            }

            refreshDataSets();
        }
        else if (recordId==null) getMain().readSensorData(); // refresh after settings issue #97

        loadingData = false;
    }

    private void addValue(long time, SensorData data) {
        Iterator<ChartVar> it = variables.iterator();
        while (it.hasNext()){
            ChartVar var = it.next();
            var.addValue(time,data);
            boolean loadMap = recordId != null && (var.type.equals("P25") || var.type.equals("CO2"));
            if(loadMap)addMapSegment(var,data);
        }

    }

    private void refreshDataSets() {
        dataSets.clear();

        Iterator<ChartVar> it = variables.iterator();

        while (it.hasNext()){
            ChartVar var = it.next();
            var.refresh();
            dataSets.add(var.dataSet);
        }

        LineData linedata = new LineData(dataSets);

        chart.setData(linedata);
        chart.notifyDataSetChanged();
        chart.invalidate();

        if(recordId!=null)updateMap();
    }

    /**
     * Add external data to fragment (real time visualization)
     * @param data
     */
    public void addData(SensorData data) {
        if (!loadingData) {
            Long currentTime = System.currentTimeMillis() / 1000;
            long time = currentTime - referenceTimestamp;
            addValue(time,data);
            refreshDataSets();
        }
        else
            Logger.v(TAG,"addData skip, in loading data.");
    }

    private void addMapSegment(ChartVar var, SensorData data) {
        geoPoints.add(new GeoPoint(data.lat,data.lon));
        if(geoPoints.size()>1){
            Polyline line = new Polyline();   //see note below!
            List<GeoPoint> segment = new ArrayList<>();
            segment.add(geoPoints.get(geoPoints.size()-2));
            segment.add(geoPoints.get(geoPoints.size()-1));
            line.setPoints(segment);
            line.getOutlinePaint().setColor(var.colors.get(var.colors.size()-1));
            line.getOutlinePaint().setStrokeWidth(18F);
            line.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
            line.getOutlinePaint().setAntiAlias(true);
            mapView.getOverlayManager().add(line);
        }
    }

    private void updateMap() {
        if(geoPoints.size()>1) {
            BoundingBox center = BoundingBox.fromGeoPoints(geoPoints);
            mapView.zoomToBoundingBox(center, false);
        }
    }

    private void setTrackDescription(SensorTrack track, boolean isPublishedData){
        chart_name.setText("ID:"+track.getName());
        if(!isPublishedData) {
            chart_name.setClickable(false);
            chart_name.setTextColor(getResources().getColor(R.color.black));
        }
        chart_date.setText(track.getDate());
        chart_desc.setText("Points: "+track.size);
        if (track.kms != 0 || track.hours !=0 || track.mins !=0) {
            String time = String.format("%02d:%02d:%02d",track.hours,track.mins,track.secs);
            chart_loc.setText("Track: "+track.kms+" Kms ("+time+")");
        }
        else
            chart_loc.setVisibility(View.GONE);

        rl_separator.setVisibility(View.VISIBLE);
    }


    private View.OnClickListener onChartIdClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(recordId!=null) UITools.viewLink(
                    getActivity(),
                    getString(R.string.url_chart_get_data)+recordId+"?output=json"
            );
        }
    };

    @Override
    public void onDestroyView() {
        Logger.w(TAG, "[CHART] onDestroyView");
        getMain().disableShareButton();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void clearData() {
        Logger.w(TAG, "[CHART] clear recorded data and chart..");
        Iterator<ChartVar> it = variables.iterator();
        while (it.hasNext()){
            it.next().clear();
        }
        chart.clear();
        Storage.setSensorData(getActivity(), new ArrayList<>());
        calculateReferenceTime();
    }

    public void shareAction(){
        if(recordId!=null && track!=null) {
            Logger.i(TAG,"publis track..");
            getMain().showSnackMessage(R.string.msg_chart_sharing);
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
