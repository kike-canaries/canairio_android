package hpsaturn.pollutionreporter;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.gson.Gson;
import com.hpsaturn.tools.Logger;
import com.iamhabib.easy_preference.EasyPreference;
import com.polidea.rxandroidble2.RxBleClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.view.ChartFragment;
import hpsaturn.pollutionreporter.view.ScanFragment;

public class MainActivity extends BaseActivity implements BLEHandler.OnBLEConnectionListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    private ScanFragment scanFragment;
    private EasyPreference.Builder prefBuilder;
    private ChartFragment chartFragment;
    private RxBleClient rxBleClient;
    private BLEHandler bleHandler;


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
            showSnackMessage(getString(R.string.msg_device_connecting));
            String macAddress = prefBuilder.getString(Keys.DEVICE_ADDRESS,"");
            Logger.i(TAG, "deviceConnect to " + macAddress);
            bleHandler = new BLEHandler(this,macAddress,this);
            bleHandler.connect();
        }
    }

    @Override
    public void onConnectionSuccess() {
        showChartFragment();
    }

    @Override
    public void onConectionFailure() {
        showSnackMessage(getString(R.string.msg_device_disconnected));
        deviceConnect();
    }

    @Override
    public void onConnectionFinished() {

    }

    @Override
    public void onNotificationSetup() {

    }

    @Override
    public void onNotificationSetupFailure() {
        bleHandler.setupNotification();
    }

    @Override
    public void onNotificationReceived(byte[] bytes) {
        String strdata = new String(bytes);
        Logger.i(TAG, "data: " + strdata);
        SensorData data = new Gson().fromJson(strdata, SensorData.class);
        if(chartFragment!=null) chartFragment.addData(data.P25);
    }

    @Override
    void actionUnPair() {
        bleHandler.triggerDisconnect();
        prefBuilder.clearAll().save();
        removeFragment(chartFragment);
        showScanFragment();
    }

    @Override
    protected void onDestroy() {
        bleHandler.triggerDisconnect();
        super.onDestroy();
    }
}
