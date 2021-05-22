package hpsaturn.pollutionreporter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hpsaturn.tools.Logger;
import com.hpsaturn.tools.UITools;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

/**
 * Created by Antonio Vanegas @hpsaturn on 7/1/18.
 */
public abstract class BaseActivity extends RxAppCompatActivity {

    public static String TAG = BaseActivity.class.getSimpleName();

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
        else Logger.i(TAG,"[BLE] checkBluetoohtBle: ready!");
    }

    public void showFragment(Fragment fragment){
        if(fragment!=null) {
            try {
                Logger.d(TAG,"showFragment: "+fragment.getTag());
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.show(fragment).commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void hideFragment(Fragment fragment){
        if(fragment!=null) {
            try {
                Logger.d(TAG,"hideFragment: "+fragment.getTag());
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.hide(fragment).commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addFragment(Fragment fragment, String fragmentTag, boolean toStack) {
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_default, fragment, fragmentTag);
            if (toStack) ft.addToBackStack(fragmentTag);
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addFragmentPopup(Fragment fragment, String fragmentTag) {
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.enter,R.anim.exit,R.anim.pop_enter,R.anim.pop_exit);
            ft.add(R.id.content_subwindows, fragment, fragmentTag);
            ft.addToBackStack(fragmentTag);
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addInfoFragment(Fragment fragment, String fragmentTag) {
        try {
            Fragment frag = getSupportFragmentManager().findFragmentByTag(fragmentTag);
            if (frag != null)return;
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.enter,R.anim.exit,R.anim.pop_enter,R.anim.pop_exit);
            ft.add(R.id.content_subwindows, fragment, fragmentTag);
            ft.addToBackStack(fragmentTag);
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void replaceFragment(Fragment fragment, String fragmentTag, boolean toStack) {
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_default, fragment, fragmentTag);
            if (toStack) ft.addToBackStack(fragmentTag);
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void replaceFragment(Fragment fragment, String fragmentTag, boolean toStack, int content) {

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

    public void showDialog(Fragment fragment, String fragmentTag) {
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
        if(fragment!=null) {
            try {
                Logger.w(TAG, "removing fragment: " + fragment.getClass().getSimpleName());
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.remove(fragment).commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            if (match != null) return true;
            else return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

            case R.id.action_unpair:
                actionUnPair();
                break;

            case R.id.action_about:
                UITools.viewLink(this,getString(R.string.url_canairio_page));
                break;

            case R.id.action_feedback:
                UITools.viewLink(this,getString(R.string.url_canairio_feedback));
                break;


            case R.id.action_guide:
                UITools.viewLink(this,getString(R.string.url_oficial_guide_en));
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    abstract void actionUnPair();


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
