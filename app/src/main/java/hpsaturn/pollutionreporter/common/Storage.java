package hpsaturn.pollutionreporter.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hpsaturn.tools.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.models.SensorTrack;

/**
 * Created by Antonio Vanegas @hpsaturn on 7/4/18.
 */
public class Storage {

    private static final String KEYS_TRACKS_PREFERENCES = "keys_tracks_preferences";
    private static final String TAG = Storage.class.getSimpleName();

    public static void setSensorData(Context ctx, ArrayList<SensorData> items) {
        if (ctx == null) {
            Logger.e(TAG, "Storage setSensorData context is null");
            return;
        }

        try {
            SharedPreferences preferences = ctx.getSharedPreferences(KEYS_TRACKS_PREFERENCES,0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Keys.SENSOR_DATA, new Gson().toJson(items));
            editor.apply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<SensorData> getSensorData(Context ctx) {
        if (ctx == null)
            return new ArrayList<>();

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

    public static void setTempAPList(Context ctx, List<String> ssids) {
        SharedPreferences preferences = ctx.getSharedPreferences(KEYS_TRACKS_PREFERENCES,0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Keys.TEMP_AP_LIST, new Gson().toJson(ssids));
        editor.apply();
    }

    public static List<String> getTempAPList(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(KEYS_TRACKS_PREFERENCES,0);
        String ringJson = preferences.getString(Keys.TEMP_AP_LIST, "");
        if (ringJson.equals("")) return new ArrayList<>();
        else {
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            return new Gson().fromJson(ringJson, listType);
        }
    }

    public static boolean getBoolean(String key, boolean value, Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(KEYS_TRACKS_PREFERENCES,0);
        return preferences.getBoolean(key, value);
    }

    public static void addBoolean(String key, boolean value, Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(KEYS_TRACKS_PREFERENCES,0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void addString(String key, String value, Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(KEYS_TRACKS_PREFERENCES,0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(String key, String value, Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(KEYS_TRACKS_PREFERENCES,0);
        return preferences.getString(key, value);
    }
}
