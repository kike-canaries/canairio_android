package hpsaturn.pollutionreporter.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

import com.jetbrains.handson.commons.models.SensorData;
import com.jetbrains.handson.commons.models.SensorTrack;

/**
 * Created by Antonio Vanegas @hpsaturn on 7/4/18.
 */
public class Storage {

    private static final String KEYS_TRACKS_PREFERENCES = "keys_tracks_preferences";
    private final String TAG = Storage.class.getSimpleName();

    public static void setSensorData(Context ctx, ArrayList<SensorData> items) {
        SharedPreferences preferences = ctx.getSharedPreferences(KEYS_TRACKS_PREFERENCES,0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Keys.SENSOR_DATA, new Gson().toJson(items));
        editor.apply();
    }

    public static ArrayList<SensorData> getSensorData(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(KEYS_TRACKS_PREFERENCES,0);
        String ringJson = preferences.getString(Keys.SENSOR_DATA, "");
        if (ringJson.equals("")) return new ArrayList<>();
        else {
            Type listType = new TypeToken<ArrayList<SensorData>>() {
            }.getType();
            return new Gson().fromJson(ringJson, listType);
        }
    }

    public static ArrayList<SensorTrack> getTracks(Context ctx) {
        try {
            SharedPreferences preferences = ctx.getSharedPreferences(KEYS_TRACKS_PREFERENCES,0);
            String ringJson = preferences.getString(Keys.SENSOR_TRACKS, "");
            if (ringJson.equals("")) return new ArrayList<>();
            else {
                Type listType = new TypeToken<ArrayList<SensorTrack>>() {
                }.getType();
                return new Gson().fromJson(ringJson, listType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveTrack(Context ctx, SensorTrack track){
        ArrayList<SensorTrack> tracks = getTracks(ctx);
        tracks.add(track);
        saveTracks(ctx,tracks);
    }

    public static void removeTrack(Context ctx, String id){
        ArrayList<SensorTrack> tracks = getTracks(ctx);
        Iterator<SensorTrack> it = tracks.iterator();
        while(it.hasNext()){
            SensorTrack track = it.next();
            if(track.name.equals(id))it.remove();
        }
        saveTracks(ctx,tracks);
    }

    public static SensorTrack getTrack(Context ctx, String id){
        ArrayList<SensorTrack> tracks = getTracks(ctx);
        Iterator<SensorTrack> it = tracks.iterator();
        while(it.hasNext()){
            SensorTrack track = it.next();
            if(track.name.equals(id))return track;
        }
        return null;
    }

    private static void saveTracks(Context ctx,ArrayList<SensorTrack>tracks){
        SharedPreferences preferences = ctx.getSharedPreferences(KEYS_TRACKS_PREFERENCES,0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Keys.SENSOR_TRACKS, new Gson().toJson(tracks));
        editor.apply();
    }

}
