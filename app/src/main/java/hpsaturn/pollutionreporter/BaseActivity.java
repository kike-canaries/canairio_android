package hpsaturn.pollutionreporter;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.hpsaturn.tools.Logger;
import com.intentfilter.androidpermissions.PermissionManager;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import static java.util.Collections.singleton;

/**
 * Created by Antonio Vanegas @hpsaturn on 7/1/18.
 */
public class BaseActivity extends RxAppCompatActivity {

    public static String TAG = BaseActivity.class.getSimpleName();


    public void checkForPermissions() {
        PermissionManager permissionManager = PermissionManager.getInstance(this);
        permissionManager.checkPermissions(singleton(Manifest.permission.ACCESS_COARSE_LOCATION), new PermissionManager.PermissionRequestListener() {
            @Override
            public void onPermissionGranted() { }

            @Override
            public void onPermissionDenied() { }
        });
    }

    public void checkBluetoohtBle() {
        // Use this check to determine whether BLE is supported on the device.
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        } else if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }
    }


    public void showFragment(Fragment fragment, String fragmentTag, boolean toStack) {

        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_default, fragment, fragmentTag);
            if (toStack) ft.addToBackStack(fragmentTag);
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showFragment(Fragment fragment, String fragmentTag, boolean toStack, int content) {

        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(content, fragment, fragmentTag);
            if (toStack) ft.addToBackStack(fragmentTag);
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showFragmentFull(Fragment fragment, String fragmentTag, boolean toStack) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_default, fragment, fragmentTag);
        if (toStack) ft.addToBackStack(fragmentTag);
        ft.commitAllowingStateLoss();

    }

    public void showDialog(Fragment fragment, String fragmentTag){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(fragment, fragmentTag);
        ft.show(fragment);
        ft.commitAllowingStateLoss();
    }

    public void popBackStackSecure(String TAG) {
        try {
            Logger.d(TAG, "popBackStackSecure to: " + TAG);
            getSupportFragmentManager().popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void popBackLastFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            Logger.d(TAG, "onBackPressed popBackStack for:" + getLastFragmentName());
            getSupportFragmentManager().popBackStack();
        }
    }


    public void removeFragment(Fragment fragment) {
        try {
            Logger.w(TAG, "removing fragment: " + fragment.getClass().getSimpleName());
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getLastFragmentName() {
        try {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) return "";
            FragmentManager fm = getSupportFragmentManager();
            return fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean isFragmentInStack(String tag) {
        try {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) return false;
            FragmentManager fm = getSupportFragmentManager();
            Fragment match = fm.findFragmentByTag(tag);
            if (match!=null)return true;
            else return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void showSnackLong(String msg) {
        Snackbar.make(this.getCurrentFocus(), msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public void showSnackLong(int msg) {
        Snackbar.make(this.getCurrentFocus(), msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    /***********************************************
     * MENU OPTIONS
     ***********************************************/

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
                break;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
