package hpsaturn.pollutionreporter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.scan_results)
    RecyclerView recyclerView;

    @BindView(R.id.tv_empty_list)
    TextView tvEmptyMsg;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;


    private RxBleClient rxBleClient;
    private Disposable scanDisposable;
    private ScanResultsAdapter resultsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        checkBluetoohtBle();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.tv_empty_list);
//        tv.setText(stringFromJNI());

        rxBleClient = AppData.getRxBleClient(this);
        configureResultList();

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
//        final Intent intent = new Intent(this, DeviceActivity.class);
//        intent.putExtra(DeviceActivity.EXTRA_MAC_ADDRESS, macAddress);
//        startActivity(intent);
        Log.i(TAG,"onAdapterItemClick: "+macAddress);
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

        switch (item.getItemId()){

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

    private void checkBluetoohtBle(){
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.

        // Initializes Bluetooth adapter.
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        else if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }
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
