package hpsaturn.pollutionreporter.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;
import com.hpsaturn.tools.Logger;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import hpsaturn.pollutionreporter.BuildConfig;
import hpsaturn.pollutionreporter.Config;
import hpsaturn.pollutionreporter.MainActivity;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.api.AqicnApiManager;
import hpsaturn.pollutionreporter.api.AqicnDataResponse;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.models.SensorTrackInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Antonio Vanegas @hpsaturn on 7/13/18.
 */
public class MapFragment extends Fragment {

    public static final String TAG = MapFragment.class.getSimpleName();
    private static MapView mapView;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_osmap, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupMap(view);
        requireActivity().runOnUiThread(this::loadLastTracks);
    }

    private void setupMap(View view) {
        mapView = view.findViewById(R.id.mapview);
        mapView.setClickable(true);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);

        mapView.setMultiTouchControls(true);
        mapView.setMaxZoomLevel((double) 19);

        mapView.getController().setZoom((double) 17); //set initial zoom-level, depends on your need
        mapView.setUseDataConnection(true); //keeps the mapView from loading online tiles using network connection.
        mapView.setEnabled(true);
        (mapView.getTileProvider().getTileCache()).getProtectedTileComputers().clear();
    }

    private void loadLastTracks() {
        Query query = getMain().getDatabase().child(Config.FB_TRACKS_INFO).orderByKey().limitToLast(150);
        query.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                Iterable<DataSnapshot> data = task.getResult().getChildren();
                for (DataSnapshot datum : data) {
                    SensorTrackInfo track = datum.getValue(SensorTrackInfo.class);
                    if (track.getSize() > 0 && track.getSize() <= 5000) addMarker(track);
                }
                Logger.v(TAG,"markers count:"+mapView.getOverlays().size());
            }
        });

    }

    public void addMarker(SensorTrackInfo trackInfo) {
        if(mapView!=null && !mapView.isShown()){
            try {
                Marker pointMarker = new Marker(mapView);
                pointMarker.setOnMarkerClickListener((marker, mapView) -> {
                    Logger.d(TAG, "OnMarkerClickListener => " + trackInfo.getName());
                    getMain().showTrackInfoFragment(trackInfo.getName());
                    return true;
                });

                SensorData lastSensorData = trackInfo.getLastSensorData();

                Drawable icon;
                if(lastSensorData!=null && lastSensorData.P25 > 20)
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.map_mark_red, null);
                else
                    icon = ResourcesCompat.getDrawable(getResources(), R.drawable.map_mark_yellow, null);

                pointMarker.setPosition(new GeoPoint(trackInfo.getLastLat(), trackInfo.getLastLon()));
                pointMarker.setIcon(icon);

                mapView.getOverlays().add(pointMarker);
                mapView.getController().setCenter(new GeoPoint(trackInfo.getLastLat(),trackInfo.getLastLon()));
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadAqicnData() {
        AqicnApiManager.getInstance().getDataFromHere(
                new Callback<AqicnDataResponse>() {
                    @Override
                    public void onResponse(Call<AqicnDataResponse> call, Response<AqicnDataResponse> response) {
                        if(response!=null) Logger.v(TAG,"[API] AQICN response: "+response.body().status);
                        else Logger.e(TAG,"[API] AQICN response: null");
                    }

                    @Override
                    public void onFailure(Call<AqicnDataResponse> call, Throwable t) {
                        Logger.e(TAG,"[API] AQICN response error: "+t.getMessage());
                        Logger.e(TAG,"[API] AQICN"+t.getLocalizedMessage());
                    }
                });
    }

    private MainActivity getMain() {
        return ((MainActivity) getActivity());
    }


}
