package hpsaturn.pollutionreporter;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.gson.Gson;
import com.hpsaturn.tools.Logger;
import com.iamhabib.easy_preference.EasyPreference;
import com.jakewharton.rx.ReplayingShare;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.view.ChartFragment;
import hpsaturn.pollutionreporter.view.ScanFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

import static com.trello.rxlifecycle2.android.ActivityEvent.PAUSE;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    private UUID serviceId = UUID.fromString("c8d1d262-861f-4082-947e-f383a259aaf3");
    private UUID characteristicUuid = UUID.fromString("b0f332a8-a5aa-4f3f-bb43-f99e7791ae01");

    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    private Observable<RxBleConnection> connectionObservable;
    private RxBleDevice bleDevice;
    private Disposable scanDisposable;
    private ScanResultsAdapter resultsAdapter;
    private ScanFragment scanFragment;
    private EasyPreference.Builder prefBuilder;
    private ChartFragment chartFragment;
    private RxBleClient rxBleClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        prefBuilder = AppData.getPrefBuilder(this);

        setSupportActionBar(toolbar);
        checkBluetoohtBle();
        setupUI();
        deviceConnect();

    }

    private View.OnClickListener onFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        }
    };

    private void setupUI() {
        fab.setOnClickListener(onFabClickListener);
        fab.setVisibility(View.INVISIBLE); // TODO: I need work on it
        checkForPermissions();
        if (!prefBuilder.getBoolean(Keys.DEVICE_PAIR, false)) {
            fab.setVisibility(View.INVISIBLE);
            showScanFragment();
        }
    }

    private void showChartFragment() {
        if (chartFragment == null) chartFragment = ChartFragment.newInstance();
        if (!chartFragment.isVisible()) showFragment(chartFragment, ChartFragment.TAG, false);
    }

    private void showScanFragment() {
        if (scanFragment == null) scanFragment = ScanFragment.newInstance();
        if (!scanFragment.isVisible()) showFragment(scanFragment, ScanFragment.TAG, false);
    }

    public void removeScanFragment(){
        if (scanFragment != null)removeFragment(scanFragment);
    }

    private void showSnackMessage(String msg) {
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public void deviceConnect() {
        if (prefBuilder.getBoolean(Keys.DEVICE_PAIR, false)) {
            String macAddress = prefBuilder.getString(Keys.DEVICE_ADDRESS,"");
            Logger.i(TAG, "deviceConnect to " + macAddress);
            rxBleClient = AppData.getRxBleClient(this);
            bleDevice = rxBleClient.getBleDevice(macAddress);
            connectionObservable = prepareConnectionObservable();

            if (isConnected()) {
                triggerDisconnect();
            } else {
                connectionObservable
                        .flatMapSingle(RxBleConnection::discoverServices)
                        .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(characteristicUuid))
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(disposable -> Logger.d(TAG, "doOnSubscribe"))
                        .subscribe(
                                characteristic -> {
//                                updateUI(characteristic);
                                    Logger.i(TAG, "connection has been established.");
                                    setupNotification();
                                    showChartFragment();
                                },
                                this::onConnectionFailure,
                                this::onConnectionFinished
                        );
            }
        }
    }

//
//    private void onAdapterItemClick(ScanResult scanResults) {

//    }

    private void setupNotification() {
        if (isConnected()) {
            connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.setupNotification(characteristicUuid))
                    .doOnNext(notificationObservable -> runOnUiThread(this::notificationHasBeenSetUp))
                    .flatMap(notificationObservable -> notificationObservable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure);
        }
    }

    private Observable<RxBleConnection> prepareConnectionObservable() {
        return bleDevice
                .establishConnection(false)
                .takeUntil(disconnectTriggerSubject)
                .compose(bindUntilEvent(PAUSE))
                .compose(ReplayingShare.instance());
    }

    private boolean isConnected() {
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void triggerDisconnect() {
        Logger.i(TAG, "triggerDisconnect..");
        disconnectTriggerSubject.onNext(true);
    }

    private void onConnectionFailure(Throwable throwable) {
        Logger.e(TAG, "onConnectionFailure");
        showSnackMessage(getString(R.string.msg_device_disconnected));
        deviceConnect();
    }

    private void onConnectionFinished() {
        Logger.w(TAG, "onConnectionFinished");
    }

    private void notificationHasBeenSetUp() {
        Logger.i(TAG, "notificationHasBeenSetUp");
    }

    private void onNotificationReceived(byte[] bytes) {
        String strdata = new String(bytes);
        Logger.i(TAG, "onNotificationReceived: " + strdata);
        SensorData data = new Gson().fromJson(strdata, SensorData.class);
        if(chartFragment!=null) chartFragment.addData(data.P25);
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        Logger.e(TAG, "onNotificationSetupFailure");
        setupNotification();
    }

    @Override
    void actionUnPair() {
        triggerDisconnect();
        prefBuilder.clearAll().save();
        removeFragment(chartFragment);
        showScanFragment();
    }
}
