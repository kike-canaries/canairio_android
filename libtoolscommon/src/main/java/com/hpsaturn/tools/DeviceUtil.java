package com.hpsaturn.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;


/**
 * Maintainer by Antonio Vanegas @hpsaturn on 4/3/17.
 */

public class DeviceUtil {

    private static final String WIFI_INTERFACE = "wlan0";
    private static final String ETHERNET_INTERFACE = "eth0";
    private static final String TAG = DeviceUtil.class.getSimpleName();

    /**
     * this method get the IMEI of device mobile.
     *
     * @param context {@link android.content.Context}.
     * @return IMEI {@link String}.
     */
    public static String getIMEI(Context context) {
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Logger.e(TAG,"getIMEI permission error, add TELEPHONY_SERVICE permission");
                return null;
            }
            return manager.getDeviceId();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * this method get Serial ID {@linkplain android.provider.Settings.Secure}
     *
     * @return id ANDROID_ID
     */
    public static String getSerialID() {
        try {
            return Build.SERIAL;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDeviceName() {
        return android.os.Build.MODEL;
    }

     /*
     * Return OS build version: a static function
     */
    public static String getAndroidBuildVersion() {
        return Build.VERSION.RELEASE;
    }

    /*
     * Return Java memory info
     */
    public static long getRuntimeMemorySize() {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     * Check if the device has Internet connection.
     *
     * @param context- application context.
     * @return true if connected, false otherwise.
     */
    public static boolean isConnected(Context context) {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * this method hide virtual keyboard.
     *
     * @param activity {@link android.app.Activity}.
     */
    public static void hideKeyBoard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);

    }

    /**
     * This method convert from dp to pixel.
     *
     * @param dp .
     * @return pixel.
     */
    public static int convertDpToPixel(float dp, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }


    /**
     * This method get wifi MAC Address.
     *
     * @param context .
     * @return Wifi Mac Address.
     */
    public static String getWifiMac(Context context) {
        String macAddress = null;
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wInfo = wifiManager.getConnectionInfo();
            macAddress = wInfo.getMacAddress();
            return macAddress;
        } catch (NullPointerException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();

        }
        return macAddress;
    }

    /**
     * This method get wifi Network name.
     *
     * @param context
     * @return Wifi network name.
     */
    public static String getWifiName(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    return wifiInfo.getSSID();
                }
            }
        }
        return "None";
    }

    public static void getSizeFile() {
        File dir = Environment.getExternalStorageDirectory();  // this is point to main directory at sdcard -> /mnt/storage
        //File dir = Environment.getExternalStorageDirectory() + "/myimagesdirectory";

        File[] files = dir.listFiles();
        for (File f : files) {
            int size = (int) (f.getTotalSpace() - f.getFreeSpace()) / 1024;
            Log.i("*******Name ", "" + f.getName());
            Log.i("*******SIZE ", "" + size);
        }
    }

    /**
     * this method get type mobile network or wifi network name.
     *
     * @param mContext
     * @return String type mobile network or wifi network name
     */
    public static String getNetWork(Context mContext) {
        String network = "unknown";

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                network = activeNetwork.getTypeName()+" "+activeNetwork.getExtraInfo();
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                network = activeNetwork.getTypeName()+" "+activeNetwork.getExtraInfo();
            }
        } else {
            if(network.isEmpty()) network = "unknown"; // not connected to the internet
        }
        network = network.replaceAll("\"" + "", "");
        Logger.d(TAG,"-->Network: "+network);
        return network;

    }


    public static String getMobileNetwork(Context mContext) {
        TelephonyManager mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO rev. B";
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "IDEN";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return "Unknown";
        }

        return null;
    }


    /**
     * This method get version name app.
     *
     * @return version name app
     */

    public static String getVersionName(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * This method get SDK API level
     *
     * @return sdk api level
     */
    public static int getAPILevel() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * this method return number version  Android OS
     *
     * @return String number
     */
    public static String getVersionOS() {
        return Build.VERSION.RELEASE;
    }

    /**
     * this method return model hardware
     *
     * @return String modelo hardware
     */

    public static String getHardware() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * capitalize
     * @param s
     * @return
     */

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /**
     * This method set Brigthness in the screen .
     *
     * @param mActivity
     * @param level
     */
    public static void setWindowBright(Activity mActivity, boolean level) {
        WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        if (level) {
            params.screenBrightness = 0.1f;
        } else {
            params.screenBrightness = 0.9f;

        }
        mActivity.getWindow().setAttributes(params);
    }


    /**
     * @param mActivity
     */
    public static void unlockScreen(Activity mActivity) {
        Window window = mActivity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }


    /**
     * this method return level bright
     */
    public static int getLevelBrightness(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * @return device - device Id
     */
    public static String getDeviceId(Context context) {
        String id = "";
        try {
            String macAddress = getMACAddress(WIFI_INTERFACE);
            if (!macAddress.isEmpty()) {
                id = macAddress.replaceAll(":", "");
            } else {
                macAddress = getMACAddress(ETHERNET_INTERFACE);
                if (!macAddress.isEmpty()) {
                    id = macAddress.replaceAll(":", "");
                } else {
                    id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        return id;
    }


    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    Logger.d(TAG, "-->interface: " + intf.getName());
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx = 0; idx < mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
                return buf.toString().toLowerCase();
            }
        } catch (Exception ex) {
            Logger.e(TAG, ex.getMessage());
        }
        return "";
    }

    public static String getAndroidDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * this method read configuration file system
     *
     * @param filePath
     * @return
     * @throws java.io.IOException
     */
    public static String loadFileAsString(String filePath) throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    /**
     * this method get ethernet mac address
     *
     * @return mac address
     */
    public static String getEthernetMacAddress() {
        String mac = null;
        try {
            mac = loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NullPointerException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mac;
    }


    /**
     * this method scale the bitmap to big screens
     *
     * @param image
     * @return Bitmap - {@linkplain Bitmap}
     */
    public static Bitmap scaleWithAspectRatio(Bitmap image, Context context) {
        int imaheVerticalAspectRatio, imageHorizontalAspectRatio;
        float bestFitScalingFactor = 0;
        float percesionValue = (float) 0.2;

        //getAspect Ratio of Image
        int imageHeight = (int) (Math.ceil((double) image.getHeight() / 100) * 100);
        int imageWidth = (int) (Math.ceil((double) image.getWidth() / 100) * 100);
        int GCD = BigInteger.valueOf(imageHeight).gcd(BigInteger.valueOf(imageWidth)).intValue();
        imaheVerticalAspectRatio = imageHeight / GCD;
        imageHorizontalAspectRatio = imageWidth / GCD;

        //getContainer Dimensions
        int displayWidth = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
        int displayHeight = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();


        int leftMargin = 0;
        int rightMargin = 0;
        int topMargin = 0;
        int bottomMargin = 0;
        int containerWidth = displayWidth - (leftMargin + rightMargin);
        int containerHeight = displayHeight - (topMargin + bottomMargin);


        //iterate to get bestFitScaleFactor per constraints
        while ((imageHorizontalAspectRatio * bestFitScalingFactor <= containerWidth) &&
                (imaheVerticalAspectRatio * bestFitScalingFactor <= containerHeight)) {
            bestFitScalingFactor += percesionValue;
        }

        //return bestFit bitmap
        int bestFitHeight = (int) (imaheVerticalAspectRatio * bestFitScalingFactor);
        int bestFitWidth = (int) (imageHorizontalAspectRatio * bestFitScalingFactor);

        image = Bitmap.createScaledBitmap(image, bestFitWidth, bestFitHeight, true);

        //Position the bitmap centre of the container
        int leftPadding = (containerWidth - image.getWidth()) / 2;
        int topPadding = (containerHeight - image.getHeight()) / 2;
        Bitmap backDrop = Bitmap.createBitmap(containerWidth, containerHeight, Bitmap.Config.RGB_565);
        Canvas can = new Canvas(backDrop);
        can.drawBitmap(image, leftPadding, topPadding, null);

        return backDrop;
    }

    /**
     * this method return phone type
     *
     * @return phone type - display model
     */
    public static String getPhoneType(Context context) {
        String id = null;
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            id = String.valueOf(manager.getPhoneType());
        } catch (NullPointerException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public static String getGitTAG(String tag) {
        return "["+tag+"]["+ BuildConfig.GitBranch+"]["+BuildConfig.GitHash+"]";
    }

    public static int getVersionCode(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getDeviceInfo(Context context) {
        String output = "";
        output=output+"Target:\t"+BuildConfig.EnvTarget;
        output=output+"\nDebug:\t"+BuildConfig.isLoggerEnable;
        output=output+"\nBranch:\t"+BuildConfig.GitBranch;
        output=output+"\nhash:\t"+BuildConfig.GitHash;
        output=output+"\nID:\t"+getDeviceId(context);
        output=output+"\nRevision:\t"+getVersionCode(context);
        output=output+"\nVersion:\t"+getVersionName(context);
        output=output+"\nAPI:\t"+getAPILevel();
        output=output+"\nHardware:\t"+getHardware();
        output=output+"\nOS:\t"+getVersionOS();
        output=output+"\nWifi:\t"+getWifiName(context);
        output=output+"\nWifiMac:\t"+getWifiMac(context);
        output=output+"\nNetMac:\t"+getMACAddress("eth0");
        return output;
    }
}
