package hpsaturn.pollutionreporter.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import hpsaturn.pollutionreporter.models.SensorTrack;
import hpsaturn.pollutionreporter.models.SensorData;

/**
 * Created by Antonio Vanegas @hpsaturn on 7/4/18.
 */
public class Storage {

    private final String TAG = Storage.class.getSimpleName();

    public static void setSensorData(Context ctx, ArrayList<SensorData> items) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Keys.SENSOR_DATA, new Gson().toJson(items));
        editor.apply();
    }

    public static ArrayList<SensorData> getSensorData(Context ctx) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        String ringJson = preferences.getString(Keys.SENSOR_DATA, "");
        if (ringJson.equals("")) return new ArrayList<>();
        else {
            Type listType = new TypeToken<ArrayList<SensorData>>() {
            }.getType();
            return new Gson().fromJson(ringJson, listType);
        }
    }

    public static ArrayList<SensorTrack> getTracks(Context ctx) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        String ringJson = preferences.getString(Keys.SENSOR_TRACKS, "");
        if (ringJson.equals("")) return new ArrayList<>();
        else {
            Type listType = new TypeToken<ArrayList<SensorTrack>>() {
            }.getType();
            return new Gson().fromJson(ringJson, listType);
        }
    }

    public static void saveLastTrack(Context ctx){
        ArrayList<SensorData> data = getSensorData(ctx);
        ArrayList<SensorTrack> tracks = getTracks(ctx);
        SensorTrack track = new SensorTrack();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH);
        String formattedDate = df.format(c);
        track.setName(formattedDate);
        track.data = data;
        tracks.add(track);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Keys.SENSOR_TRACKS, new Gson().toJson(tracks));
        editor.apply();
        // CLEAR sensor data
        setSensorData(ctx,new ArrayList<>());
    }

}
