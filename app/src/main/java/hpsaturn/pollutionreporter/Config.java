package hpsaturn.pollutionreporter;

/**
 * Created by Antonio Vanegas @hpsaturn on 6/22/18.
 */
public class Config {

    public static final boolean DEBUG = true;
    // Record Service whatchdog polling
    public static final long DEFAULT_INTERVAL = 5 * 1000;  // seconds
    public static final int TIME_AFTER_START = 5; // after boot starting time

    public static final String FB_TRACKS_INFO = "tracks_info";
    public static final String FB_TRACKS_DATA = "tracks_data";
    public static final int GEOHASHACCU = 7; // max GeoHash precision
}
