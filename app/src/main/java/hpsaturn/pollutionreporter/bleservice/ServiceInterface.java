package hpsaturn.pollutionreporter.bleservice;

import hpsaturn.pollutionreporter.models.SensorData;

/**
 * Created by Antonio Vanegas @hpsaturn on 7/2/18.
 */
public interface ServiceInterface {

    void onServiceStatus(String status);

    void onServiceStart();

    void onServiceStop();

    void onServiceData(SensorData data);

    void onSensorRecord();

    void onSensorRecordStop();

    void onTracksUpdated();
}
