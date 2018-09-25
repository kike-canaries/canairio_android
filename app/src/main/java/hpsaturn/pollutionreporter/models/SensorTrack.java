package hpsaturn.pollutionreporter.models;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */
public class SensorTrack {

    public String date;
    public Location location;
    private String desc;
    public String name;
    public int size;
    public ArrayList<SensorData> data;

    public SensorTrack() { }

    public String getDate() {
        return date;
    }

    public void setDate(String email) {
        this.date = email;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "name: "+name;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
