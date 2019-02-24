package hpsaturn.pollutionreporter.common;

import android.content.Context;

import com.hpsaturn.tools.Logger;
import com.jakewharton.rx.ReplayingShare;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.UUID;

import hpsaturn.pollutionreporter.AppData;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
    private UUID charactDataUuid = UUID.fromString("b0f332a8-a5aa-4f3f-bb43-f99e7791ae01");
    private UUID charactConfigUuid = UUID.fromString("b0f332a8-a5aa-4f3f-bb43-f99e7791ae02");

    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    private Observable<RxBleConnection> connectionObservable;
    private Disposable scanDisposable;
    private RxBleClient rxBleClient;
    private RxBleDevice bleDevice;

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

    }


    public void connect() {
        rxBleClient = AppData.getRxBleClient(ctx);
        bleDevice = rxBleClient.getBleDevice(address);
        connectionObservable = prepareConnectionObservable();

        if (isConnected()) {
            triggerDisconnect();
        } else {
            try {
                connectionObservable
                        .flatMapSingle(RxBleConnection::discoverServices)
                        .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(charactDataUuid))
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(disposable -> Logger.d(TAG, "doOnSubscribe"))
                        .subscribe(
                                characteristic -> {
                                    Logger.i(TAG, "Connection has been established.");
                                    setupNotification();
                                    listener.onConnectionSuccess();
                                },
                                this::onConnectionFailure,
                                this::onConnectionFinished
                        );
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
            connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.setupNotification(charactDataUuid))
                    .doOnNext(notificationObservable -> { notificationHasBeenSetUp(); })
                    .flatMap(notificationObservable -> notificationObservable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure);
        }
    }

    public void readConfig(){
        bleDevice.establishConnection(false)
                .flatMapSingle(rxBleConnection -> rxBleConnection.readCharacteristic(charactConfigUuid))
                .subscribe(
                        characteristicValue -> {
                            // Read characteristic value.
                        },
                        throwable -> {
                            // Handle an error here.
                        }
                );
    }

    public boolean isConnected() {
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    public void triggerDisconnect() {
        Logger.w(TAG, "triggerDisconnect..");
        disconnectTriggerSubject.onNext(true);
    }

    private void onNotificationReceived(byte[] bytes) {
        Logger.d(TAG, "onNotificationReceived");
        listener.onNotificationReceived(bytes);
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        Logger.e(TAG, "onNotificationSetupFailure");
        listener.onNotificationSetupFailure();
    }

    private void onConnectionFailure(Throwable throwable) {
        Logger.e(TAG, "onConnectionFailure");
        listener.onConectionFailure();
    }

    private void onConnectionFinished() {
        Logger.w(TAG, "onConnectionFinished");
        listener.onConnectionFinished();
    }

    private void notificationHasBeenSetUp() {
        Logger.i(TAG, "notificationHasBeenSetUp");
        listener.onNotificationSetup();
    }

}
