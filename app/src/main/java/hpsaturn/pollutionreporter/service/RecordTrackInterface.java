package hpsaturn.pollutionreporter.service;

import hpsaturn.pollutionreporter.models.SensorConfig;
import hpsaturn.pollutionreporter.models.SensorData;

/**
 * Created by Antonio Vanegas @hpsaturn on 7/2/18.
 */
public interface RecordTrackInterface {

    void onServiceStatus(String status);

    void onServiceStart();

    void onServiceStop();

    void onSensorNotificationData(SensorData data);

    void onServiceRecord();

    void onServiceRecordStop();

    void onTracksUpdated();

    void requestSensorConfigRead();

    void requestSensorDataRead();

    void onSensorConfigRead(SensorConfig config);

    void onSensorDataRead(SensorData data);

    void onSensorConfigWrite(SensorConfig config);
}
