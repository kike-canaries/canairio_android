package hpsaturn.pollutionreporter.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hpsaturn.tools.Logger;
import com.hpsaturn.tools.UITools;
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
import hpsaturn.pollutionreporter.AppData;
import hpsaturn.pollutionreporter.ScanResultsAdapter;
import hpsaturn.pollutionreporter.R;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by Antonio Vanegas @hpsaturn on 6/30/18.
 */
public class ScanFragment extends Fragment {

    public static String TAG = ScanFragment.class.getSimpleName();

    @BindView(R.id.rv_devices)
    RecyclerView recyclerView;

    @BindView(R.id.tv_devices_empty_list)
    TextView tvEmptyMsg;

    private Disposable scanDisposable;
    private RxBleClient rxBleClient;
    private ScanResultsAdapter resultsAdapter;


    public static ScanFragment newInstance() {
        ScanFragment fragment = new ScanFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_devices,container,false);
        ButterKnife.bind(this,view);

        rxBleClient = AppData.getRxBleClient(getActivity());
        Logger.i(TAG,"onCreateView: configureResultList..");
        configureResultList();
        Logger.i(TAG,"onCreateView: actionScan..");
        actionScan();

        return view;
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
                            .setDeviceName(getString(R.string.ble_device_name))
                            // add custom filters if needed
                            .build()
            )
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(this::dispose)
                    .subscribe(resultsAdapter::addScanResult, this::onScanFailure);
        }

//        updateButtonUIState();
    }


    private void configureResultList() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerLayoutManager);
        resultsAdapter = new ScanResultsAdapter();
        recyclerView.setAdapter(resultsAdapter);
        resultsAdapter.setOnAdapterItemClickListener((view, position) -> onAdapterItemClick(
                resultsAdapter.getItemAtPosition(position))
        );
    }


    private void onAdapterItemClick(ScanResult scanResults) {
        final String macAddress = scanResults.getBleDevice().getMacAddress();
        Logger.i(TAG, "onAdapterItemClick: " + macAddress);
//        bleDevice = rxBleClient.getBleDevice(macAddress);
//        connectionObservable = prepareConnectionObservable();
    }

    private boolean isScanning() {
        return scanDisposable != null;
    }

    private void onScanFailure(Throwable throwable) {
        Logger.e(TAG,"onScanFailure");
        if (throwable instanceof BleScanException) {
            handleBleScanException((BleScanException) throwable);
        }
    }

    private void dispose() {
        Logger.i(TAG,"dispose");
        scanDisposable = null;
        resultsAdapter.clearScanResults();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (isScanning()) scanDisposable.dispose();
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
        Logger.w(TAG,"EXCEPTION: "+text+" "+bleScanException.getMessage());
        UITools.showToast(getActivity(),text);
    }

    private long secondsTill(Date retryDateSuggestion) {
        return TimeUnit.MILLISECONDS.toSeconds(retryDateSuggestion.getTime() - System.currentTimeMillis());
    }

}
