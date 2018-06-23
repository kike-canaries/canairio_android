package hpsaturn.pollutionreporter;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.intentfilter.androidpermissions.PermissionManager;
import com.jakewharton.rx.ReplayingShare;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

import static com.trello.rxlifecycle2.android.ActivityEvent.PAUSE;
import static java.util.Collections.singleton;

public class MainActivity extends RxAppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.scan_results)
    RecyclerView recyclerView;

    @BindView(R.id.tv_empty_list)
    TextView tvEmptyMsg;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.lc_measures)
    LineChart chart;

    private UUID serviceId = UUID.fromString("c8d1d262-861f-4082-947e-f383a259aaf3");
    private UUID characteristicUuid = UUID.fromString("b0f332a8-a5aa-4f3f-bb43-f99e7791ae01");

    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    private Observable<RxBleConnection> connectionObservable;
    private RxBleClient rxBleClient;
    private RxBleDevice bleDevice;
    private Disposable scanDisposable;
    private ScanResultsAdapter resultsAdapter;

    List<Entry> entries = new ArrayList<Entry>();

    private Handler mHandler = new Handler();
    private int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        checkBluetoohtBle();

        fab.setOnClickListener(view -> Snackbar.make(
                view,
                "Replace with your own action",
                Snackbar.LENGTH_LONG
        ).setAction("Action", null).show());

        checkForPermissions();
        rxBleClient = AppData.getRxBleClient(this);
        configureResultList();

        loadTestData();

        LineDataSet dataSet = new LineDataSet(entries, "Label");
        dataSet.setColor(R.color.colorPrimary);
        dataSet.setValueTextColor(R.color.colorPrimaryDark);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh

        mHandler.post(mDataRunnable);
    }

    private void configureResultList() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        resultsAdapter = new ScanResultsAdapter();
        recyclerView.setAdapter(resultsAdapter);
        resultsAdapter.setOnAdapterItemClickListener(view -> {
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
            final ScanResult itemAtPosition = resultsAdapter.getItemAtPosition(childAdapterPosition);
            onAdapterItemClick(itemAtPosition);
        });
    }

    private boolean isScanning() {
        return scanDisposable != null;
    }

    private void onAdapterItemClick(ScanResult scanResults) {
        final String macAddress = scanResults.getBleDevice().getMacAddress();
        Log.i(TAG, "onAdapterItemClick: " + macAddress);
        bleDevice = rxBleClient.getBleDevice(macAddress);
        connectionObservable = prepareConnectionObservable();

        if (isConnected()) {
            triggerDisconnect();
        } else {
            connectionObservable
                    .flatMapSingle(RxBleConnection::discoverServices)
                    .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(characteristicUuid))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(disposable -> Log.d(TAG, "doOnSubscribe"))
                    .subscribe(
                            characteristic -> {
//                                updateUI(characteristic);
                                Log.i(getClass().getSimpleName(), "Hey, connection has been established!");
                                setupNotification();
                            },
                            this::onConnectionFailure,
                            this::onConnectionFinished
                    );
        }
    }

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
        disconnectTriggerSubject.onNext(true);
    }

    private void onConnectionFailure(Throwable throwable) {
        Log.e(TAG, "onConnectionFailure");
        //noinspection ConstantConditions
//        Snackbar.make(findViewById(R.id.main), "Connection error: " + throwable, Snackbar.LENGTH_SHORT).show();
//        updateUI(null);
    }

    private void onConnectionFinished() {
        Log.w(TAG, "onConnectionFinished");
//        updateUI(null);
    }

    private void notificationHasBeenSetUp() {
        Log.i(TAG, "notificationHasBeenSetUp");
        //noinspection ConstantConditions
//        Snackbar.make(findViewById(R.id.main), "Notifications has been set up", Snackbar.LENGTH_SHORT).show();
    }

    private void onNotificationReceived(byte[] bytes) {
        Log.i(TAG,"onNotificationReceived: "+ new String(bytes));
        //noinspection ConstantConditions
//        Snackbar.make(findViewById(R.id.main), "Change: " + HexString.bytesToHex(bytes), Snackbar.LENGTH_SHORT).show();
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        //noinspection ConstantConditions
//        Snackbar.make(findViewById(R.id.main), "Notifications error: " + throwable, Snackbar.LENGTH_SHORT).show();
    }

    private void onScanFailure(Throwable throwable) {
        if (throwable instanceof BleScanException) {
            handleBleScanException((BleScanException) throwable);
        }
    }

    private void dispose() {
        scanDisposable = null;
        resultsAdapter.clearScanResults();
        updateButtonUIState();
    }

    private void handleBleScanException(BleScanException bleScanException) {
        final String text;

        switch (bleScanException.getReason()) {
            case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                text = "Bluetooth is not available";
                break;
            case BleScanException.BLUETOOTH_DISABLED:
                text = "Enable bluetooth and try again";
                break;
            case BleScanException.LOCATION_PERMISSION_MISSING:
                text = "On Android 6.0 location permission is required. Implement Runtime Permissions";
                break;
            case BleScanException.LOCATION_SERVICES_DISABLED:
                text = "Location services needs to be enabled on Android 6.0";
                break;
            case BleScanException.SCAN_FAILED_ALREADY_STARTED:
                text = "Scan with the same filters is already started";
                break;
            case BleScanException.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                text = "Failed to register application for bluetooth scan";
                break;
            case BleScanException.SCAN_FAILED_FEATURE_UNSUPPORTED:
                text = "Scan with specified parameters is not supported";
                break;
            case BleScanException.SCAN_FAILED_INTERNAL_ERROR:
                text = "Scan failed due to internal error";
                break;
            case BleScanException.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES:
                text = "Scan cannot start due to limited hardware resources";
                break;
            case BleScanException.UNDOCUMENTED_SCAN_THROTTLE:
                text = String.format(
                        Locale.getDefault(),
                        "Android 7+ does not allow more scans. Try in %d seconds",
                        secondsTill(bleScanException.getRetryDateSuggestion())
                );
                break;
            case BleScanException.UNKNOWN_ERROR_CODE:
            case BleScanException.BLUETOOTH_CANNOT_START:
            default:
                text = "Unable to start scanning";
                break;
        }
        Log.w("EXCEPTION", text, bleScanException);
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private long secondsTill(Date retryDateSuggestion) {
        return TimeUnit.MILLISECONDS.toSeconds(retryDateSuggestion.getTime() - System.currentTimeMillis());
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isScanning()) {
            /*
             * Stop scanning in onPause callback. You can use rxlifecycle for convenience. Examples are provided later.
             */
            scanDisposable.dispose();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {

            case R.id.action_settings:

                break;

            case R.id.action_scan:
                actionScan();
                break;
        }


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void actionScan() {
        if (isScanning()) {
            scanDisposable.dispose();
        } else {
            scanDisposable = rxBleClient.scanBleDevices(
                    new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                            .build(),
                    new ScanFilter.Builder()
//                            .setDeviceAddress("B4:99:4C:34:DC:8B")
//                            .setServiceUuid(new ParcelUuid(serviceId))
                            .setDeviceName("ESP32_HPMA115S0")
                            // add custom filters if needed
                            .build()
            )
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(this::dispose)
                    .subscribe(resultsAdapter::addScanResult, this::onScanFailure);
        }

        updateButtonUIState();
    }

    private void updateButtonUIState() {
        tvEmptyMsg.setText(isScanning() ? R.string.stop_scan : R.string.start_scan);
    }

    private void checkForPermissions() {
        PermissionManager permissionManager = PermissionManager.getInstance(this);
        permissionManager.checkPermissions(singleton(Manifest.permission.ACCESS_COARSE_LOCATION), new PermissionManager.PermissionRequestListener() {
            @Override
            public void onPermissionGranted() {
//                Toast.makeText(MainActivity.this, "Permissions Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied() {
//                Toast.makeText(MainActivity.this, "Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkBluetoohtBle() {
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.

        // Initializes Bluetooth adapter.
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        } else if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }
    }

    private void loadTestData(){
        Random rand = new Random();
        int  n = rand.nextInt(50) + 1;
        entries.add(new Entry(i++,n));
        Log.d(TAG,"size: "+entries.size());
    }

    private Runnable mDataRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"load more data..");
            loadTestData();
            LineDataSet dataSet = new LineDataSet(entries, "Label");
            dataSet.setColor(R.color.colorPrimary);
            dataSet.setValueTextColor(R.color.colorPrimaryDark);
            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.invalidate();
            mHandler.postDelayed(mDataRunnable,2000);
        }
    };

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mDataRunnable);
        super.onDestroy();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
