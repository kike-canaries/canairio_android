package hpsaturn.pollutionreporter.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import hpsaturn.pollutionreporter.models.SensorData;

/**
 * Created by Antonio Vanegas @hpsaturn on 7/4/18.
 */
public class Storage {

    private final String TAG = Storage.class.getSimpleName();

    public static void setData(Context ctx, ArrayList<SensorData> items) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Keys.SENSOR_DATA, new Gson().toJson(items));
        editor.apply();
    }

    public static ArrayList<SensorData> getData(Context ctx) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        String ringJson = preferences.getString(Keys.SENSOR_DATA, "");
        if (ringJson.equals("")) return new ArrayList<>();
        else {
            Type listType = new TypeToken<ArrayList<SensorData>>() {
            }.getType();
            return new Gson().fromJson(ringJson, listType);
        }
    }

}
