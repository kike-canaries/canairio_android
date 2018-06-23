package com.hpsaturn.tools;

import android.util.Log;


public class Logger {

    public static void v(String TAG, String message) {
        if (BuildConfig.isLoggerEnable) {
            Log.v(TAG, message);
        }
    }

    public static void d(String TAG, String message) {
        if (BuildConfig.isLoggerEnable) {
            Log.d(TAG, message);
        }
    }

    public static void i(String TAG, String message) {
        if (BuildConfig.isLoggerEnable) {
            Log.i(TAG, message);
        }
    }

    public static void w(String TAG, String message) {
        if (BuildConfig.isLoggerEnable) {
            Log.w(TAG, message);
        }
    }

    public static void e(String TAG, String message) {
        if (BuildConfig.isLoggerEnable) {
            Log.e(TAG, message);
        }
    }

    public static void wtf(String TAG, String message) {
        if (BuildConfig.isLoggerEnable) {
            Log.wtf(TAG, message);
        }
    }
}
