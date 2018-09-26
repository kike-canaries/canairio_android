package hpsaturn.pollutionreporter.models;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */
public class SensorTrackInfo {

    private String name;
    private String date;
    private Location location;
    private String desc;
    private String deviceId;
    private int size;

    public SensorTrackInfo(SensorTrack track) {
       this.name = track.name;
       this.desc = track.desc;
       this.date = track.date;
       this.size = track.size;
       this.deviceId = track.deviceId;
       this.location = track.location;
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

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "name: "+name;
    }

}
