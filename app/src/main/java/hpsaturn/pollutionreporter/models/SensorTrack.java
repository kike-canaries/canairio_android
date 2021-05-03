package hpsaturn.pollutionreporter.models;

import android.location.Location;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */
public class SensorTrack {

    public String date;
    public String desc;
    public String name;
    public int size;
    public float kms;
    public double lastLat;
    public double lastLon;
    public SensorData lastSensorData;
    public ArrayList<SensorData> data;
    public String deviceId;

    public SensorTrack() { }

    public String getDate() {
        return date;
    }

    public void setDate(String email) {
        this.date = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }


    @Override
    public String toString() {
        return "name: "+name;
    }

}
