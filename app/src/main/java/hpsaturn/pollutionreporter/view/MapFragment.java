package hpsaturn.pollutionreporter.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import hpsaturn.pollutionreporter.R;
import hpsaturn.pollutionreporter.models.SensorData;

/**
 * Created by Antonio Vanegas @hpsaturn on 7/13/18.
 */
public class MapFragment extends Fragment {

    public static final String TAG = MapFragment.class.getSimpleName();
    private MapView mapView;
    private MyLocationNewOverlay mapLocationOverlay;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_osmap, container, false);
        getActivity().runOnUiThread(() -> setupMap(view));
        return view;
    }

    private void setupMap(View view) {

        mapView = view.findViewById(R.id.mapview);
        mapView.setClickable(true);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setMaxZoomLevel((double) 19);

        mapView.getController().setZoom((double)17); //set initial zoom-level, depends on your need
        //mapView.getController().setCenter(ONCATIVO);
        mapView.setUseDataConnection(true); //keeps the mapView from loading online tiles using network connection.
        mapView.setDrawingCacheEnabled(true);

        mapLocationOverlay = new MyLocationNewOverlay(mapView);
        mapLocationOverlay.enableMyLocation();
        mapLocationOverlay.enableFollowLocation();
        mapLocationOverlay.enableMyLocation();

        CompassOverlay compassOverlay = new CompassOverlay(getActivity(), mapView);
        compassOverlay.enableCompass();

        mapView.getOverlays().add(compassOverlay);
        mapView.getOverlays().add(mapLocationOverlay);
        mapView.buildDrawingCache();
        mapView.setEnabled(true);
    }

    public void enableMyLocation(){
        mapLocationOverlay.enableMyLocation();
        mapLocationOverlay.enableFollowLocation();
    }

    public void addMarker(SensorData data){
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(new GeoPoint(data.lat,data.lon));
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(startMarker);
    }
}
