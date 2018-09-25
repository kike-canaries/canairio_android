package hpsaturn.pollutionreporter.models;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */
public class SensorTrack {

    public String date;
    public Location location;
    public String name;
    public ArrayList<SensorData> data;

    public SensorTrack() {
    }

    public SensorTrack(String name, String date, Location location) {
        this.name = name;
        this.date = date;
        this.location = location;
    }

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

}
