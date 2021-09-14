package com.jetbrains.handson.commons.models;

/**
 * Created by Antonio Vanegas @hpsaturn on 10/20/15.
 */
public class SensorTrackInfo {

    private String name;
    private String date;
    private String desc;
    private String deviceId;
    private double lastLat;
    private double lastLon;
    private SensorData lastSensorData;
    private int size;
    private float kms;

    public SensorTrackInfo() {
    }

    public SensorTrackInfo(SensorTrack track) {
       this.name = track.name;
       this.desc = track.desc;
       this.date = track.date;
       this.size = track.size;
       this.kms  = track.kms;
       this.deviceId = track.deviceId;
       this.lastLat = track.lastLat;
       this.lastLon = track.lastLon;
       this.lastSensorData = track.lastSensorData;
    }

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

    public int getSize() {
        return size;
    }

    public double getLastLat() {
        return lastLat;
    }

    public double getLastLon() {
        return lastLon;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public SensorData getLastSensorData() {
        return lastSensorData;
    }

    public float getDistance() {
        return kms;
    }
}
