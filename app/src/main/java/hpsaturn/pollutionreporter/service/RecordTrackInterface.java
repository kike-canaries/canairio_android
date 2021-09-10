package hpsaturn.pollutionreporter.service;

import com.jetbrains.handson.commons.models.ResponseConfig;
import com.jetbrains.handson.commons.models.SensorData;

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

    void onSensorConfigRead(ResponseConfig config);

    void onSensorDataRead(SensorData data);

    void onSensorConfigWrite(String config);
}
