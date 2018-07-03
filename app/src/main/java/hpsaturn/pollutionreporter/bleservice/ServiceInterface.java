package hpsaturn.pollutionreporter.bleservice;

/**
 * Created by Antonio Vanegas @hpsaturn on 7/2/18.
 */
public interface ServiceInterface {

    void onServiceStatus(String status);

    void onServiceStart();

    void onServiceStop();

    void onServiceData(byte[] bytes);

}
