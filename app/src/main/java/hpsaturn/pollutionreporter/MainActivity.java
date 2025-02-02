package hpsaturn.pollutionreporter;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hpsaturn.tools.Logger;
import com.hpsaturn.tools.UITools;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hpsaturn.pollutionreporter.common.Keys;
import hpsaturn.pollutionreporter.common.Storage;
import hpsaturn.pollutionreporter.models.ResponseConfig;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.models.SensorTrackInfo;
import hpsaturn.pollutionreporter.models.WifiConfig;
import hpsaturn.pollutionreporter.service.RecordTrackInterface;
import hpsaturn.pollutionreporter.service.RecordTrackManager;
import hpsaturn.pollutionreporter.service.RecordTrackScheduler;
import hpsaturn.pollutionreporter.service.RecordTrackService;
import hpsaturn.pollutionreporter.view.AboutFragment;
import hpsaturn.pollutionreporter.view.ChartFragment;
import hpsaturn.pollutionreporter.view.DisclosureFragment;
import hpsaturn.pollutionreporter.view.MapFragment;
import hpsaturn.pollutionreporter.view.PickerFragmentAdapter;
import hpsaturn.pollutionreporter.view.PickerFragmentData;
import hpsaturn.pollutionreporter.view.PickerFragmentInfo;
import hpsaturn.pollutionreporter.view.PostsFragment;
import hpsaturn.pollutionreporter.view.RecordsFragment;
import hpsaturn.pollutionreporter.view.ScanAccesPointFragment;
import hpsaturn.pollutionreporter.view.ScanFragment;
import hpsaturn.pollutionreporter.view.SettingsFixedStation;
import hpsaturn.pollutionreporter.view.SettingsFragment;
import hpsaturn.pollutionreporter.view.ShareDialogFragment;
import hpsaturn.pollutionreporter.view.VariableFilterFragment;

;

/**
 * Created by Antonio Vanegas @hpsaturn on 6/11/18.
 */

public class MainActivity extends BaseActivity implements
        DiscreteScrollView.ScrollStateChangeListener<PickerFragmentAdapter.ViewHolder>,
        DiscreteScrollView.OnItemChangedListener<PickerFragmentAdapter.ViewHolder>,
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

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
    private ChartFragment chartFragment;
    private RecordTrackManager recordTrackManager;
    private MapFragment mapFragment;
    private RecordsFragment recordsFragment;
    private PostsFragment postsFragment;
    private SettingsFragment settingsFragment;
    private DatabaseReference mDatabase;

    private boolean deviceConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startRecordTrackService();

        ButterKnife.bind(this);

        startDataBase();
        setSupportActionBar(toolbar);
        checkBLE();
        setupUI();
        recordTrackManager = new RecordTrackManager(this, recordTrackListener);

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        checkPreviousPermission();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestForegroundPermission();
        }

    }

    private void setupUI() {
        fab.setOnClickListener(onFabClickListener);
        setupAppFragments();
        if(isPaired())fab.show();
    }

    private RecordTrackInterface recordTrackListener = new RecordTrackInterface() {

        @Override
        public void onServiceStatus(String status) {
            if (status.equals(RecordTrackManager.STATUS_BLE_START)) {
                deviceConnected = true;
            } else if (status.equals(RecordTrackManager.STATUS_SERVICE_OK)){
            } else if (status.equals(RecordTrackManager.STATUS_BLE_FAILURE)) {
                showSnackMessage(R.string.msg_device_reconnecting);
                deviceConnected = false;
            }
        }

        @Override
        public void onServiceStart() {
        }

        @Override
        public void onServiceStop() {

        }

        @Override
        public void onSensorNotificationData(SensorData data) {

        }

        @Override
        public void onServiceRecord() {

        }

        @Override
        public void onServiceRecordStop() {

        }

        @Override
        public void onTracksUpdated() {
            if(recordsFragment!=null)recordsFragment.loadData();
        }

        @Override
        public void requestSensorConfigRead() {

        }

        @Override
        public void requestSensorDataRead() {

        }

        @Override
        public void onSensorConfigRead(ResponseConfig config) {
        }

        @Override
        public void onSensorDataRead(SensorData data) {
            if (chartFragment != null)chartFragment.addData(data);
        }

        @Override
        public void onSensorConfigWrite(String config) {

        }
    };

    public boolean isRecording(){
        return Storage.getBoolean(Keys.SENSOR_RECORD, false, this);
    }

    private void buttonRecordingAction () {
        if (isRecording()) {
            stopRecord();
        } else {
            startRecord();
        }

    }

    private View.OnClickListener onFabShareClickListener = view -> {
        ShareDialogFragment dialog = ShareDialogFragment.newInstance();
        showDialogFragment(dialog,ShareDialogFragment.TAG);
    };

    public void shareAction(String metadata, boolean isShare){
        if(recordsFragment!=null)recordsFragment.shareAction(metadata,isShare);
    }

    private void stopRecord() {
        showSnackMessage(R.string.msg_record_stop);
        Storage.addBoolean(Keys.SENSOR_RECORD, false, this);
        recordTrackManager.serviceRecordStop();
        fabUpdateLayout();
    }

    private void startRecord() {
        showSnackMessage(R.string.msg_record);
        Storage.addBoolean(Keys.SENSOR_RECORD, true, this);
        recordTrackManager.serviceRecord();
        fabUpdateLayout();
    }

    public void setupAppFragments(){
        Logger.i(TAG, "setupAppFragments");
        addPostsFragment();
        addRecordsFragment();
        addMapFragment();
        addSettingsFragment();
        if(isPaired())addChartFragment();
        else addScanFragment();
        //showFragment(chartFragment);
        setupFragmentPicker();
    }

    private void setupFragmentPicker() {
        Context ctx = getApplicationContext();
        List<PickerFragmentInfo> pickerFragmentInfos = PickerFragmentData.get().getFragmentsInfo(ctx);
        fragmentPicker.setVisibility(View.VISIBLE);
        fragmentPicker.setSlideOnFling(true);
        fragmentPicker.setAdapter(new PickerFragmentAdapter(pickerFragmentInfos));
        fragmentPicker.addOnItemChangedListener(this);
        fragmentPicker.addScrollStateChangeListener(this);
        if(isPaired())fragmentPicker.scrollToPosition(2);
        else fragmentPicker.scrollToPosition(1);
        fragmentPicker.setItemTransitionTimeMillis(100);
        fragmentPicker.setItemTransformer(new ScaleTransformer.Builder().setMinScale(0.8f).build());
    }

    private void fragmentPickerHide(){
        fragmentPicker.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCurrentItemChanged(@Nullable PickerFragmentAdapter.ViewHolder viewHolder, int position) {
        // TODO: Refactor to dinamic plus fragment to scroll view
        Logger.d(TAG, "onCurrentItemChanged: " + position);
        switch (position) {
            case 0: // map fragment
                fab.hide();
                hideFragment(postsFragment);
                hideFragment(recordsFragment);
                hideFragment(settingsFragment);
                if(isPaired())hideFragment(chartFragment);
                else hideFragment(scanFragment);
                showFragment(mapFragment);
                break;
            case 1: // post fragment
                fab.hide();
                hideFragment(recordsFragment);
                hideFragment(mapFragment);
                hideFragment(settingsFragment);
                if(isPaired())hideFragment(chartFragment);
                else hideFragment(scanFragment);
                showFragment(postsFragment);
                postsFragment.refresh();
                break;
            case 2: // scan fragment/chart fragment
                fabUpdateLayout();
                hideFragment(postsFragment);
                hideFragment(mapFragment);
                hideFragment(recordsFragment);
                hideFragment(settingsFragment);
                if(isPaired()) showFragment(chartFragment);
                else showFragment(scanFragment);
                break;
            case 3: // records fragment
                scrollToRecordsFragment();
                break;

            case 4:  // settings fragment
                fab.hide();
                hideFragment(postsFragment);
                hideFragment(mapFragment);
                hideFragment(recordsFragment);
                if(isPaired())hideFragment(chartFragment);
                else hideFragment(scanFragment);
                showFragment(settingsFragment);
                fragmentPickerHide();
                recordTrackManager.readSensorConfig();
                break;

        }
        viewHolder.showText();
    }

    private void scrollToRecordsFragment() {
        fab.hide();
        hideFragment(postsFragment);
        hideFragment(mapFragment);
        hideFragment(settingsFragment);
        if(isPaired())hideFragment(chartFragment);
        else hideFragment(scanFragment);
        showFragment(recordsFragment);
    }

    private void fabUpdateLayout() {
        fab.hide();
        if (Storage.getBoolean(Keys.SENSOR_RECORD, false, this)) {
            fab.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.color_state_record_stop));
            fab.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_stop_white_24dp));
        } else {
            fab.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.color_state_record));
            fab.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_record_white_24dp));
        }
        if(isPaired())fab.show();
    }

    public void enableShareButton (){
        fab.show();
        fab.setOnClickListener(onFabShareClickListener);
        fab.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.color_state_share));
        fab.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_share));
    }

    public void disableShareButton (){
        if(recordsFragment!=null)recordsFragment.setIsShowingData(false);
        fab.setOnClickListener(onFabClickListener);
        fab.hide();
    }

    private void addChartFragment() {
        if (chartFragment == null) chartFragment = ChartFragment.newInstance();
        if (chartFragment.isAdded()) return;
        addFragment(chartFragment, ChartFragment.TAG, false);
    }

    private void addScanFragment() {
        if (scanFragment == null) scanFragment = ScanFragment.newInstance();
        if (scanFragment.isAdded()) return;
        addFragment(scanFragment, ScanFragment.TAG, false);
    }

    private void addMapFragment() {
        if (mapFragment == null) mapFragment = MapFragment.newInstance();
        if (mapFragment.isAdded()) return;
        addFragment(mapFragment, MapFragment.TAG, false);
    }

    private void addRecordsFragment() {
        if (recordsFragment == null) recordsFragment = RecordsFragment.newInstance();
        if (recordsFragment.isAdded()) return;
        addFragment(recordsFragment, RecordsFragment.TAG, false);
    }

    private void addSettingsFragment() {
        if (settingsFragment == null) settingsFragment = new SettingsFragment();
        if (settingsFragment.isAdded()) return;
        addFragment(settingsFragment, SettingsFixedStation.TAG, false);
    }

    private void addPostsFragment() {
        if (postsFragment == null) postsFragment = PostsFragment.newInstance();
        if (postsFragment.isAdded()) return;
        addFragment(postsFragment, PostsFragment.TAG, false);
    }

    public void removeScanFragment() {
        if (scanFragment != null) removeFragment(scanFragment);
        addChartFragment();
        fabUpdateLayout();
    }

    public void showSnackMessage(int id) {
        Snackbar.make(coordinatorLayout, getString(id), Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public void showSnackMessageSlow(int id) {
        Snackbar.make(coordinatorLayout, getString(id), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    public Snackbar getSnackBar(int id, int action, View.OnClickListener listener) {
        return Snackbar.make(coordinatorLayout, getString(id), Snackbar.LENGTH_INDEFINITE)
                .setAction(action, listener)
                .setActionTextColor(getResources().getColor(R.color.white));
    }

    public void startRecordTrackService() {
        Logger.v(TAG,"starting RecordTrackService..");
        Intent newIntent = new Intent(this, RecordTrackService.class);
        startService(newIntent);
        RecordTrackScheduler.startScheduleService(this, Config.DEFAULT_INTERVAL);
    }

    public void stopRecordTrackService() {
        Logger.v(TAG,"stoping RecordTrackService..");
        recordTrackManager.stop();
    }

    public void deviceConnect() {
        if (Storage.getBoolean(Keys.DEVICE_PAIR, false, this)) {
            startRecordTrackService();
            showSnackMessage(R.string.msg_device_connecting);
        }
    }

    public void  readSensorData() {
       if(recordTrackManager!=null)recordTrackManager.readSensorData();
    }

    public DatabaseReference getDatabase() {
        return mDatabase;
    }

    private void startDataBase(){
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private View.OnClickListener onFabClickListener = view -> {
        if(!PermissionUtil.hasBackgroundLocationPermission(this))
            showDisclosureFragment(R.string.msg_gps_title,R.string.msg_gps_desc,R.drawable.ic_bicycle);
        else buttonRecordingAction();
    };

    public void showDisclosureFragment(int title, int desc, int img) {
        Logger.i(TAG,"showDisclosureFragment..");
        DisclosureFragment dialog = DisclosureFragment.newInstance(title,desc,img);
        showDialogFragment(dialog,DisclosureFragment.TAG);
    }

    public void showAboutFragment() {
        AboutFragment about = new AboutFragment();
        showDialogFragment(about,AboutFragment.TAG);
    }

    public boolean isStorageGranted(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        int permissionState = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permissionState == PackageManager.PERMISSION_GRANTED;
//        return Storage.getBoolean(Keys.PERMISSION_STORAGE,false);
    }

    public boolean isGPSGranted(){
        return Storage.getBoolean(Keys.PERMISSION_BACKGROUND,false, this);
    }

    public boolean isBLEGranted(){
        return Storage.getBoolean(Keys.PERMISSION_BLE,false, this);
    }

    public void checkPreviousPermission() {
        boolean cp_fine_location = PermissionUtil.hasLocationPermission(this);
        boolean cp_bluetooth = PermissionUtil.hasBluetoothPermission(this);
        if ((isBLEGranted() && isGPSGranted())  && (!cp_fine_location || !cp_bluetooth)) {
            showDisclosureFragment(R.string.msg_ble_title,R.string.msg_ble_desc,R.drawable.ic_cpu);
        }
    }

    public void requestAllFilesAccessPermission() {
        Logger.i(TAG, "[PERM] starting Storage permission flow");
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        try {
            ComponentName componentName = intent.resolveActivity(getPackageManager());
            if (componentName != null) {
                // Launch "Allow all files access?" dialog.
                startActivity(intent);
                return;
            }
            Log.w(TAG, "[PERM] Request all files access not supported");
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "[PERM] Request all files access not supported", e);
        }
        Toast.makeText(this, R.string.msg_external_storage_manage_failed, Toast.LENGTH_LONG).show();
    }

    /**
     * Permission check and request functions
     */
    public void requestLocationPermission() {
        Log.i(TAG, "[PERM] requestLocationPermission..");
        ActivityCompat.requestPermissions(this,
                PermissionUtil.getLocationPermissions(),
                Config.PermissionRequestType.LOCATION.ordinal());
    }

    /**
     * Permission check and request functions
     */
    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    public void requestForegroundPermission() {
        Log.i(TAG, "[PERM] requestForegroundPermission..");
        ActivityCompat.requestPermissions(this,
                PermissionUtil.getForegroundLocationPermissions(),
                Config.PermissionRequestType.FOREGROUND_LOCATION.ordinal());
    }

    /**
     * Permission check and request functions
     */
    public void requestBluetoothScanPermission() {
        Log.i(TAG, "[PERM] requestBluetoothScanPermission..");
        ActivityCompat.requestPermissions(this,
                PermissionUtil.getBluetoothScanPermission(),
                Config.PermissionRequestType.BLUETOOTH_SCAN.ordinal());
    }

    /**
     * Permission check and request functions
     */
    public void requestBluetoothPermission() {
        Log.i(TAG, "[PERM] requestBluetoothPermission..");
        ActivityCompat.requestPermissions(this,
                PermissionUtil.getBluetoothPermission(),
                Config.PermissionRequestType.BLUETOOTH.ordinal());
    }

    public void requestBackgroundPermission() {
        Log.i(TAG, "[PERM] requestBackgroundPermission..");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this,
                    PermissionUtil.getBackgroundLocationPermissions(),
                    Config.PermissionRequestType.LOCATION_BACKGROUND.ordinal());
        }
    }

    private void requestStoragePermission() {
        Log.i(TAG, "[PERM] requestStoragePermission..");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestAllFilesAccessPermission();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Config.PermissionRequestType.STORAGE.ordinal());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (Config.PermissionRequestType.values()[requestCode]) {
            case LOCATION:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "[PERM] User denied foreground location permission");
                    break;
                }
                Log.i(TAG, "[PERM] User granted foreground location permission");
                performBLEScan();
                break;
            case LOCATION_BACKGROUND:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "[PERM] User denied background location permission");
                    break;
                }
                Log.i(TAG, "[PERM] User granted background location permission");
                Storage.addBoolean(Keys.PERMISSION_BACKGROUND, true, this);
                buttonRecordingAction();
                break;
            case STORAGE:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Storage.addBoolean(Keys.PERMISSION_STORAGE, false, this);
                    Log.w(TAG, "[PERM] User denied WRITE_EXTERNAL_STORAGE permission.");
                } else {
                    Log.i(TAG, "[PERM] User granted WRITE_EXTERNAL_STORAGE permission.");
                    Storage.addBoolean(Keys.PERMISSION_STORAGE, true, this);
                }
                break;
            case BLUETOOTH:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "[PERM] User denied bluetooth connect");
                    break;
                }
                Log.i(TAG, "[PERM] User granted bluetooth connect permission");
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R)
                    requestBluetoothScanPermission();
                else
                    requestLocationPermission();
            case BLUETOOTH_SCAN:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "[PERM] User denied scan nearby devices");
                    break;
                }
                Log.i(TAG, "[PERM] User granted scan nearby devices permission");
                Storage.addBoolean(Keys.PERMISSION_BLE, true, this);
                requestLocationPermission();

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void performBLEScan() {
        if(!isBLEGranted())Storage.addBoolean(Keys.PERMISSION_BLE, true, this);
        if(scanFragment!=null)scanFragment.executeScan();
    }

    @Override
    void actionUnPair() {
        if(isRecording()){
            showSnackMessage(R.string.msg_record_stop_alert);
        } else {
            Logger.i(TAG, "[BLE] unpaired..");
            stopRecord();
            recordTrackManager.stop();
            Storage.addString(Keys.DEVICE_ADDRESS,"", this);
            Storage.addBoolean(Keys.DEVICE_PAIR, false, this);
            fab.hide();
            if (chartFragment != null) chartFragment.clearData();
            removeFragment(chartFragment);
            addScanFragment();
            fragmentPicker.scrollToPosition(2);
            showFragment(scanFragment);
        }
    }

    @Override
    void actionVarFilter() {
        showDialogFragment(new VariableFilterFragment(), VariableFilterFragment.TAG);
    }

    @Override
    void actionShowAbout(){
        showAboutFragment();
    }

    @Override
    protected void onDestroy() {
        stopRecordTrackService();
        recordTrackManager.unregister();
        super.onDestroy();
    }

    private boolean isPaired(){
        return Storage.getBoolean(Keys.DEVICE_PAIR, false, this);
    }

    public boolean isDeviceConnected() {
        return deviceConnected;
    }

    @Override
    public void onBackPressed() {
        if(settingsFragment !=null && settingsFragment.isVisible()){
            fragmentPicker.setVisibility(View.VISIBLE);
            fragmentPicker.scrollToPosition(3);
            scrollToRecordsFragment();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onScrollStart(@NonNull PickerFragmentAdapter.ViewHolder holder, int adapterPosition) {
        Logger.d(TAG, "onScrollStart");
        holder.hideText();
    }

    @Override
    public void onScrollEnd(@NonNull PickerFragmentAdapter.ViewHolder currentItemHolder, int adapterPosition) {
        Logger.d(TAG, "onScrollEnd");
    }

    @Override
    public void onScroll(float pos, int index, int newIndex, @Nullable PickerFragmentAdapter.ViewHolder holder, @Nullable PickerFragmentAdapter.ViewHolder newHolder) {

    }

    public void addTrackToMap(SensorTrackInfo trackInfo) {
        if(mapFragment!=null)mapFragment.addMarker(trackInfo);
    }

    public RecordTrackManager getRecordTrackManager() {
        return recordTrackManager;
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        Logger.d(TAG, "onPreferenceStartFragment");
        return false;
    }

    public void selectedVarsUpdated() {
        if (chartFragment!=null)chartFragment.loadSelectedVariables();
        ChartFragment infoFragment = (ChartFragment) getFragmentInStack(ChartFragment.TAG_INFO);
        if (infoFragment!=null)infoFragment.loadSelectedVariables();
    }

    public void showTrackInfoFragment(String trackId) {
        ChartFragment chart = ChartFragment.newInstance(trackId);
        addInfoFragment(chart,ChartFragment.TAG_INFO);
    }

    public void showAccessPointsDialog() {
        Logger.d(TAG,"showAccessPointsDialog..");
        showDialogFragment(new ScanAccesPointFragment(),ScanAccesPointFragment.TAG);
    }

    public void updatePreferencesSSID(String ssid) {
        WifiConfig config = new WifiConfig();
        config.ssid = ssid;
        if (settingsFragment != null) settingsFragment.sendSensorConfig(config);
    }

    public void shareViaIntent(String trackJson) {
        UITools.shareText(this,trackJson,getString(R.string.title_share_dialog));
    }
}
