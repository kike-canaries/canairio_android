package hpsaturn.pollutionreporter.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Objects;

import androidx.preference.PreferenceManager;
import hpsaturn.pollutionreporter.BuildConfig;
import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.models.SensorTrackInfo;

/**
 * Created by Antonio Vanegas @hpsaturn on 7/13/18.
 */
public class MapFragment extends Fragment {

    public static final String TAG = MapFragment.class.getSimpleName();
    private MapView mapView;

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
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> setupMap(view));
    }

    private void setupMap(View view) {
        mapView = view.findViewById(R.id.mapview);
        mapView.setClickable(true);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setMaxZoomLevel((double) 19);

        mapView.getController().setZoom((double) 17); //set initial zoom-level, depends on your need
        mapView.setUseDataConnection(true); //keeps the mapView from loading online tiles using network connection.
        mapView.setEnabled(true);
    }

    public void addMarker(SensorTrackInfo trackInfo) {
        Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.map_mark_yellow, null);
        MarkerInfoWindow infoWindow = new MarkerInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, mapView);
        Marker pointMarker = new Marker(mapView);
        pointMarker.setTitle("" + trackInfo.getDate());
        SensorData lastSensorData = trackInfo.getLastSensorData();
        if(lastSensorData!=null) pointMarker.setSnippet("Last PM2.5: "+ lastSensorData.P25);
        pointMarker.setSubDescription("report: "+trackInfo.getSize()+ " points");
        pointMarker.setPosition(new GeoPoint(trackInfo.getLastLat(), trackInfo.getLastLon()));
        pointMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        pointMarker.setIcon(icon);
        pointMarker.setInfoWindow(infoWindow);
        mapView.getOverlays().add(pointMarker);
        mapView.getController().setCenter(new GeoPoint(trackInfo.getLastLat(),trackInfo.getLastLon()));
    }
}
