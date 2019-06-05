package hpsaturn.pollutionreporter.common;

import android.content.Context;

import com.hpsaturn.tools.Logger;
import com.jakewharton.rx.ReplayingShare;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import org.reactivestreams.Subscription;

import java.util.UUID;

import hpsaturn.pollutionreporter.AppData;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Antonio Vanegas @hpsaturn on 7/1/18.
 */

public class BLEHandler {

    private static String TAG = BLEHandler.class.getSimpleName();

    private Context ctx;
    private OnBLEConnectionListener listener;
    private String address;

    private UUID serviceId = UUID.fromString("c8d1d262-861f-4082-947e-f383a259aaf3");
    private UUID charactSensorDataUuid = UUID.fromString("b0f332a8-a5aa-4f3f-bb43-f99e7791ae01");
    private UUID charactConfigUuid = UUID.fromString("b0f332a8-a5aa-4f3f-bb43-f99e7791ae02");

    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    private Subscription connectionSubscription;
    private Observable<RxBleConnection> connectionObservable;
    private Disposable scanDisposable;
    private RxBleClient rxBleClient;
    private RxBleDevice bleDevice;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public BLEHandler(Context ctx, String address, OnBLEConnectionListener listener) {
        this.ctx = ctx;
        this.address = address;
        this.listener = listener;
    }

    public interface OnBLEConnectionListener {

        void onConnectionSuccess();

        void onConectionFailure();

        void onConnectionFinished();

        void onNotificationSetup();

        void onNotificationSetupFailure();

        void onNotificationReceived(byte[] bytes);

        void onSensorConfigRead(byte[] bytes);

        void onSensorDataRead(byte[] bytes);

        void onReadFailure();

        void onWriteFailure();
    }


    public void connect() {
        rxBleClient = AppData.getRxBleClient(ctx);
        bleDevice = rxBleClient.getBleDevice(address);
        connectionObservable = prepareConnectionObservable();


        if (isConnected()) {
            triggerDisconnect();
        } else {
            try {
                final Disposable connectionDisposable = connectionObservable
                        .flatMapSingle(RxBleConnection::discoverServices)
                        .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(charactSensorDataUuid))
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(disposable -> Logger.d(TAG, "doOnSubscribe"))
                        .subscribe(
                                characteristic -> {
                                    Logger.v(TAG, "[BLE] Connection has been established.");
                                    setupNotification();
                                    listener.onConnectionSuccess();
                                },
                                this::onConnectionFailure,
                                this::onConnectionFinished
                        );
                compositeDisposable.add(connectionDisposable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Observable<RxBleConnection> prepareConnectionObservable() {
        return bleDevice
                .establishConnection(false)
                .takeUntil(disconnectTriggerSubject)
                .compose(ReplayingShare.instance());
    }

    public void setupNotification() {
        if (isConnected()) {
            final Disposable disposable = connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.setupNotification(charactSensorDataUuid))
                    .doOnNext(notificationObservable -> { notificationHasBeenSetUp(); })
                    .flatMap(notificationObservable -> notificationObservable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure);
            compositeDisposable.add(disposable);
        }
    }

    public void readSensorConfig(){
        if (isConnected()) {
            final Disposable disposable = connectionObservable
                    .firstOrError()
                    .flatMap(rxBleConnection -> rxBleConnection.readCharacteristic(charactConfigUuid))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onSensorConfigRead, this::onReadFailure);
            compositeDisposable.add(disposable);
        }
    }

    public void readSensorData(){
        if (isConnected()) {
            final Disposable disposable = connectionObservable
                    .firstOrError()
                    .flatMap(rxBleConnection -> rxBleConnection.readCharacteristic(charactSensorDataUuid))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onSensorDataRead, this::onReadFailure);
            compositeDisposable.add(disposable);
        }
    }

    public void writeSensorConfig(byte[] bytes){
        Logger.v(TAG,"[BLE] writing sensor config -> "+new String(bytes));
        if (isConnected()) {
            final Disposable disposable = connectionObservable
                    .firstOrError()
                    .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(charactConfigUuid, bytes))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onWriteSuccess, this::onWriteFailure );

            compositeDisposable.add(disposable);
        }
    }

    public boolean isConnected() {
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    public void triggerDisconnect() {
        Logger.w(TAG, "[BLE] triggerDisconnect..");
        compositeDisposable.clear();
        disconnectTriggerSubject.onNext(true);
    }

    private void onNotificationReceived(byte[] bytes) {
        Logger.v(TAG, "[BLE] onNotificationReceived->"+new String(bytes));
        listener.onNotificationReceived(bytes);
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        Logger.e(TAG, "[BLE] onNotificationSetupFailure: "+throwable.getMessage());
        listener.onNotificationSetupFailure();
    }

    private void onConnectionFailure(Throwable throwable) {
        Logger.e(TAG, "[BLE] onConnectionFailure: "+throwable.getMessage());
        listener.onConectionFailure();
    }

    private void onConnectionFinished() {
        Logger.w(TAG, "[BLE] onConnectionFinished");
        listener.onConnectionFinished();
    }

    private void notificationHasBeenSetUp() {
        Logger.i(TAG, "[BLE] notificationHasBeenSetUp");
        listener.onNotificationSetup();
    }

    private void onSensorConfigRead(byte[] bytes){
        Logger.v(TAG,"[BLE] onSensorConfigRead->"+new String(bytes));
        listener.onSensorConfigRead(bytes);
    }

    private void onSensorDataRead(byte[] bytes){
        Logger.v(TAG,"[BLE] onSensorDataRead->"+new String(bytes));
        listener.onSensorDataRead(bytes);
    }

    private void onReadFailure(Throwable throwable) {
        Logger.e(TAG,"[BLE] onReadFailure: " + throwable.getMessage());
        listener.onReadFailure();
    }

    private void onWriteSuccess(byte[] bytes) {
        Logger.v(TAG,"[BLE] onWriteSuccess->"+new String(bytes));
        Logger.i(TAG,"[BLE] reading new config..");
        readSensorConfig();
    }

    private void onWriteFailure(Throwable throwable) {
        Logger.e(TAG,"[BLE] onWriteFailure: " + throwable.getMessage());
        listener.onWriteFailure();
    }

}
