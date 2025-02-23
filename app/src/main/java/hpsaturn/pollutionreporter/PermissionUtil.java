package hpsaturn.pollutionreporter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

public class PermissionUtil {
    private PermissionUtil() {}

    /**
     * Returns the location permissions required to access wifi SSIDs depending
     * on the respective Android version.
     */
    public static String[] getLocationPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) { // before android 9
            return new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
            };
        }
        return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static String[] getBackgroundLocationPermissions() {
        return new String[]{
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    public static String[] getForegroundLocationPermissions() {
        return new String[]{
                Manifest.permission.FOREGROUND_SERVICE_LOCATION,
        };
    }

    public static String[] getBluetoothPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            return new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
            };
        }
        else {
            return new String[]{
                    Manifest.permission.BLUETOOTH,
            };
        }
    }

    public static String[] getBluetoothScanPermission() {
        return new String[]{
                Manifest.permission.BLUETOOTH_SCAN,
        };
    }

    public static boolean hasStoragePermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        int permissionState = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasLocationPermission(@NonNull Context context) {
        for (String perm : PermissionUtil.getLocationPermissions()) {
            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    public static boolean hasForegroundLocationPermission(@NonNull Context context) {
        for (String perm : PermissionUtil.getForegroundLocationPermissions()) {
            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasBluetoothPermission(@NonNull Context context) {
        for (String perm : PermissionUtil.getBluetoothPermission()) {
            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    public static boolean hasBluetoothScanPermission(@NonNull Context context) {
        for (String perm : PermissionUtil.getBluetoothPermission()) {
            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasBackgroundLocationPermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            for (String perm : PermissionUtil.getBackgroundLocationPermissions()) {
                if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


}
