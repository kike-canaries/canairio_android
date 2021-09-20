package hpsaturn.pollutionreporter;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hpsaturn.tools.Logger;
import com.iamhabib.easy_preference.EasyPreference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hpsaturn.pollutionreporter.common.Keys;
import hpsaturn.pollutionreporter.models.ResponseConfig;
import hpsaturn.pollutionreporter.models.SensorData;
import hpsaturn.pollutionreporter.models.SensorTrackInfo;
import hpsaturn.pollutionreporter.service.RecordTrackInterface;
import hpsaturn.pollutionreporter.service.RecordTrackManager;
import hpsaturn.pollutionreporter.service.RecordTrackScheduler;
import hpsaturn.pollutionreporter.service.RecordTrackService;
import hpsaturn.pollutionreporter.view.ChartFragment;
import hpsaturn.pollutionreporter.view.DisclosureFragment;
import hpsaturn.pollutionreporter.view.MapFragment;
import hpsaturn.pollutionreporter.view.PickerFragmentAdapter;
import hpsaturn.pollutionreporter.view.PickerFragmentData;
import hpsaturn.pollutionreporter.view.PickerFragmentInfo;
import hpsaturn.pollutionreporter.view.PostsFragment;
import hpsaturn.pollutionreporter.view.RecordsFragment;
import hpsaturn.pollutionreporter.view.ScanFragment;
import hpsaturn.pollutionreporter.view.SettingsFixedStation;
import hpsaturn.pollutionreporter.view.SettingsFragment;
import hpsaturn.pollutionreporter.view.VariableFileterFragment;

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
    private EasyPreference.Builder prefBuilder;
    private ChartFragment chartFragment;
    private RecordTrackManager recordTrackManager;
    private MapFragment mapFragment;
    private RecordsFragment recordsFragment;
    private PostsFragment postsFragment;
    private SettingsFragment settingsFragment;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private boolean deviceConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startRecordTrackService();

        ButterKnife.bind(this);
        prefBuilder = AppData.getPrefBuilder(this);

        startDataBase();
        setSupportActionBar(toolbar);
        checkBluetoohtBle();
        setupUI();
        recordTrackManager = new RecordTrackManager(this, recordTrackListener);

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

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
        return prefBuilder.getBoolean(Keys.SENSOR_RECORD, false);
    }

    private void buttonRecordingAction () {
        if (isRecording()) {
            stopRecord();
        } else {
            startRecord();
        }

    }

    private View.OnClickListener onFabShareClickListener = view -> {
        if(recordsFragment!=null)recordsFragment.shareAction();
    };

    private void stopRecord() {
        showSnackMessage(R.string.msg_record_stop);
        prefBuilder.addBoolean(Keys.SENSOR_RECORD, false).save();
        recordTrackManager.serviceRecordStop();
        fabUpdateLayout();
    }

    private void startRecord() {
        showSnackMessage(R.string.msg_record);
        prefBuilder.addBoolean(Keys.SENSOR_RECORD, true).save();
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
        List<PickerFragmentInfo> pickerFragmentInfos = PickerFragmentData.get().getFragmentsInfo();
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
            case 0:
                fab.hide();
                hideFragment(postsFragment);
                hideFragment(recordsFragment);
                hideFragment(settingsFragment);
                if(isPaired())hideFragment(chartFragment);
                else hideFragment(scanFragment);
                showFragment(mapFragment);
                break;
            case 1:
                fab.hide();
                hideFragment(recordsFragment);
                hideFragment(mapFragment);
                hideFragment(settingsFragment);
                if(isPaired())hideFragment(chartFragment);
                else hideFragment(scanFragment);
                showFragment(postsFragment);
                postsFragment.refresh();
                break;
            case 2:
                fabUpdateLayout();
                hideFragment(postsFragment);
                hideFragment(mapFragment);
                hideFragment(recordsFragment);
                hideFragment(settingsFragment);
                if(isPaired()) showFragment(chartFragment);
                else showFragment(scanFragment);
                break;
            case 3:
                scrollToRecordsFragment();
                break;

            case 4:
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
        if (prefBuilder.getBoolean(Keys.SENSOR_RECORD, false)) {
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
        fab.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.color_state_record));
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
        if (prefBuilder.getBoolean(Keys.DEVICE_PAIR, false)) {
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
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void signInAnonymously() {
        Logger.d(TAG,"[FB] firebase auth anonymously");
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Logger.i(TAG, "[FB] signInAnonymously:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Logger.i(TAG, "[FB] user: "+user.getUid());
                    } else {
                        // If sign in fails, display a message to the user.
                        Logger.w(TAG, "[FB] signInAnonymously:failure"+task.getException());
                    }
                });
    }

    private View.OnClickListener onFabClickListener = view -> {
        if(!isGPSGranted()) showDisclosureFragment(R.string.msg_gps_title,R.string.msg_gps_desc,R.drawable.ic_bicycle);
        else startPermissionsGPSFlow();
    };

    public void showDisclosureFragment(int title, int desc, int img) {
        DisclosureFragment dialog = DisclosureFragment.newInstance(title,desc,img);
        showDialogFragment(dialog,DisclosureFragment.TAG);
    }

    public boolean isGPSGranted(){
        return prefBuilder.getBoolean(Keys.PERMISSION_GPS,false);
    }

    public boolean isBLEGranted(){
        return prefBuilder.getBoolean(Keys.PERMISSION_BLE,false);
    }

    public void startPermissionsBLEFlow() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH
                )
                .withListener(blePermissionListener)
                .check();

    }

    private final MultiplePermissionsListener blePermissionListener =  new MultiplePermissionsListener() {
        @Override
        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
            if(multiplePermissionsReport.areAllPermissionsGranted()) {
                Logger.i(TAG, "AllPermissionsGranted..");
                if(!isBLEGranted())prefBuilder.addBoolean(Keys.PERMISSION_BLE, true).save();
                if(scanFragment!=null)scanFragment.executeScan();
            }
        }

        @Override
        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
            permissionToken.continuePermissionRequest();
        }
    };

    public void startPermissionsGPSFlow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Dexter.withContext(this)
                    .withPermissions(
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .withListener(gpsPermissionListener)
                    .check();
        }
        else {
            Dexter.withContext(this)
                    .withPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .withListener(gpsPermissionListener)
                    .check();
        }
    }

    private final MultiplePermissionsListener gpsPermissionListener = new MultiplePermissionsListener() {
        @Override
        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
            if(multiplePermissionsReport.areAllPermissionsGranted()) {
                Logger.i(TAG, "AllPermissionsGranted..");
                if(!isGPSGranted())prefBuilder.addBoolean(Keys.PERMISSION_GPS, true).save();
                buttonRecordingAction();
            }
        }

        @Override
        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
            permissionToken.continuePermissionRequest();
        }
    };

    @Override
    void actionUnPair() {
        if(isRecording()){
            showSnackMessage(R.string.msg_record_stop_alert);
        } else {
            Logger.i(TAG, "[BLE] unpaired..");
            stopRecord();
            recordTrackManager.stop();
            prefBuilder.addString(Keys.DEVICE_ADDRESS,"").save();
            prefBuilder.addBoolean(Keys.DEVICE_PAIR, false).save();
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
       showDialogFragment(new VariableFileterFragment(),VariableFileterFragment.TAG);
    }

    @Override
    protected void onDestroy() {
        stopRecordTrackService();
        recordTrackManager.unregister();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null && currentUser.isAnonymous()) currentUser.delete();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) Logger.i(TAG,"[FB] current user: "+currentUser.getUid());
        else signInAnonymously();
        super.onStart();
    }

    private boolean isPaired(){
        return prefBuilder.getBoolean(Keys.DEVICE_PAIR, false);
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
}
