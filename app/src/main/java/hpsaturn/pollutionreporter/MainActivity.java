package hpsaturn.pollutionreporter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.gson.Gson;
import com.hpsaturn.tools.BuildConfig;
import com.hpsaturn.tools.Logger;
import com.iamhabib.easy_preference.EasyPreference;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hpsaturn.pollutionreporter.bleservice.ServiceBLE;
import hpsaturn.pollutionreporter.bleservice.ServiceInterface;
import hpsaturn.pollutionreporter.bleservice.ServiceManager;
import hpsaturn.pollutionreporter.bleservice.ServiceScheduler;
import hpsaturn.pollutionreporter.common.Keys;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.view.ChartFragment;
import hpsaturn.pollutionreporter.view.FragmentPickerInfo;
import hpsaturn.pollutionreporter.view.FragmentPickerAdapter;
import hpsaturn.pollutionreporter.view.MapFragment;
import hpsaturn.pollutionreporter.view.RecordsFragment;
import hpsaturn.pollutionreporter.view.ScanFragment;
import hpsaturn.pollutionreporter.view.FragmentPickerData;

/**
 * Created by Antonio Vanegas @hpsaturn on 6/11/18.
 */

public class MainActivity extends BaseActivity implements
        DiscreteScrollView.ScrollStateChangeListener<FragmentPickerAdapter.ViewHolder>,
        DiscreteScrollView.OnItemChangedListener<FragmentPickerAdapter.ViewHolder> {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.fragment_picker)
    DiscreteScrollView fragmentPicker;

    private ScanFragment scanFragment;
    private EasyPreference.Builder prefBuilder;
    private ChartFragment chartFragment;
    private ServiceManager serviceManager;
    private MapFragment mapFragment;
    private RecordsFragment recordsFragment;
    private boolean withoutDevice = BuildConfig.withoutDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBleService();

        ButterKnife.bind(this);
        prefBuilder = AppData.getPrefBuilder(this);

        setSupportActionBar(toolbar);
        checkBluetoohtBle();
        setupUI();

        serviceManager = new ServiceManager(this, serviceListener);
        deviceConnect();
    }


    private ServiceInterface serviceListener = new ServiceInterface() {

        @Override
        public void onServiceStatus(String status) {

            if (status.equals(ServiceManager.STATUS_BLE_START)) {
                fragmentPicker.scrollToPosition(1);
                showFragment(chartFragment);
            } else if (status.equals(ServiceManager.STATUS_BLE_FAILURE)) {
                showSnackMessage(R.string.msg_device_reconnecting);
            }
        }

        @Override
        public void onServiceStart() {
        }

        @Override
        public void onServiceStop() {

        }

        @Override
        public void onServiceData(byte[] bytes) {
            refreshUI();
            String strdata = new String(bytes);
            Logger.i(TAG, "data: " + strdata);
            SensorData data = new Gson().fromJson(strdata, SensorData.class);
            if (chartFragment != null) chartFragment.addData(data.P25);
        }

        @Override
        public void onSensorRecord() {

        }

        @Override
        public void onSensorRecordStop() {

        }
    };

    private View.OnClickListener onFabClickListener = view -> {
        if (prefBuilder.getBoolean(Keys.SENSOR_RECORD, false)) {
            stopRecord();
        } else {
            startRecord();
        }
    };

    private void stopRecord() {
        showSnackMessageSlow(R.string.msg_record_stop);
        prefBuilder.addBoolean(Keys.SENSOR_RECORD, false).save();
        refreshUI();
        serviceManager.sensorRecordStop();
    }

    private void startRecord() {
        showSnackMessageSlow(R.string.msg_record);
        prefBuilder.addBoolean(Keys.SENSOR_RECORD, true).save();
        refreshUI();
        serviceManager.sensorRecord();
    }

    private void setupUI() {
        fab.setOnClickListener(onFabClickListener);
        checkForPermissions();
        if (!prefBuilder.getBoolean(Keys.DEVICE_PAIR, false)&&!withoutDevice) {
            fab.setVisibility(View.INVISIBLE);
            addScanFragment();
            showFragment(scanFragment);
        } else {
            setupAppFragments();
        }
    }

    public void setupAppFragments(){
        addRecordsFragment();
        addMapFragment();
        addChartFragment();
        showFragment(chartFragment);
        setupFragmentPicker();
    }

    private void setupFragmentPicker() {
        List<FragmentPickerInfo> fragmentPickerInfos = FragmentPickerData.get().getForecasts();
        fragmentPicker.setVisibility(View.VISIBLE);
        fragmentPicker.setSlideOnFling(true);
        fragmentPicker.setAdapter(new FragmentPickerAdapter(fragmentPickerInfos));
        fragmentPicker.addOnItemChangedListener(this);
        fragmentPicker.addScrollStateChangeListener(this);
        fragmentPicker.scrollToPosition(1);
        fragmentPicker.setItemTransitionTimeMillis(100);
        fragmentPicker.setItemTransformer(new ScaleTransformer.Builder().setMinScale(0.8f).build());
    }

    private void refreshUI() {
        fab.setVisibility(View.VISIBLE);
        if (prefBuilder.getBoolean(Keys.SENSOR_RECORD, false)) {
            fab.setBackgroundTintList(getColorStateList(R.color.color_state_record_stop));
            fab.setImageDrawable(getDrawable(R.drawable.ic_stop_white_24dp));
        } else {
            fab.setBackgroundTintList(getColorStateList(R.color.color_state_record));
            fab.setImageDrawable(getDrawable(R.drawable.ic_record_white_24dp));
        }
    }

    private void addChartFragment() {
        if (chartFragment == null) chartFragment = ChartFragment.newInstance();
        addFragment(chartFragment, ChartFragment.TAG, false);
    }

    private void addScanFragment() {
        if (scanFragment == null) scanFragment = ScanFragment.newInstance();
        addFragment(scanFragment, ScanFragment.TAG, false);
    }

    private void addMapFragment() {
        if (mapFragment == null) mapFragment = MapFragment.newInstance();
        addFragment(mapFragment, MapFragment.TAG, false);
    }

    private void addRecordsFragment() {
        if (recordsFragment == null) recordsFragment = RecordsFragment.newInstance();
        addFragment(recordsFragment, RecordsFragment.TAG, false);
    }

    public void removeScanFragment() {
        if (scanFragment != null) removeFragment(scanFragment);
    }

    private void showSnackMessage(int id) {
        Snackbar.make(coordinatorLayout, getString(id), Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private void showSnackMessageSlow(int id) {
        Snackbar.make(coordinatorLayout, getString(id), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    private void startBleService() {
        Intent newIntent = new Intent(this, ServiceBLE.class);
        startService(newIntent);
        ServiceScheduler.startScheduleService(this, Config.DEFAULT_INTERVAL);
    }

    public void deviceConnect() {
        if (prefBuilder.getBoolean(Keys.DEVICE_PAIR, false)) {
            showSnackMessage(R.string.msg_device_connecting);
            serviceManager.start();
        }
    }

    @Override
    void onPermissionGranted() {
       if(mapFragment!=null)mapFragment.enableMyLocation();
    }

    @Override
    void actionUnPair() {
        Logger.i(TAG, "[BLE] unpaired..");
        stopRecord();
        serviceManager.stop();
        prefBuilder.clearAll().save();
        if (chartFragment != null) chartFragment.clearData();
        removeFragment(chartFragment);
        removeFragment(mapFragment);
        removeFragment(recordsFragment);
        fab.setVisibility(View.INVISIBLE);
        fragmentPicker.setVisibility(View.INVISIBLE);
        addScanFragment();
    }

    @Override
    protected void onDestroy() {
        serviceManager.stop();
        serviceManager.unregister();
        super.onDestroy();
    }

    @Override
    public void onCurrentItemChanged(@Nullable FragmentPickerAdapter.ViewHolder viewHolder, int position) {
        Logger.d(TAG, "onCurrentItemChanged: " + position);
        switch (position) {
            case 0:
                hideFragment(recordsFragment);
                hideFragment(chartFragment);
                showFragment(mapFragment);
                break;
            case 1:
                hideFragment(mapFragment);
                hideFragment(recordsFragment);
                showFragment(chartFragment);
                break;
            case 2:
                hideFragment(mapFragment);
                hideFragment(chartFragment);
                showFragment(recordsFragment);
                break;
        }
        viewHolder.showText();
    }

    @Override
    public void onScrollStart(@NonNull FragmentPickerAdapter.ViewHolder holder, int adapterPosition) {
        Logger.d(TAG, "onScrollStart");
        holder.hideText();
    }

    @Override
    public void onScrollEnd(@NonNull FragmentPickerAdapter.ViewHolder currentItemHolder, int adapterPosition) {
        Logger.d(TAG, "onScrollEnd");
    }

    @Override
    public void onScroll(float pos, int index, int newIndex, @Nullable FragmentPickerAdapter.ViewHolder holder, @Nullable FragmentPickerAdapter.ViewHolder newHolder) {

    }
}
