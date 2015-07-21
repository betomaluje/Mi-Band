package com.betomaluje.android.miband.example;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by betomaluje on 6/26/15.
 */
public class MiBandApplication extends Application {

    private static MiBandApplication context;

    private static from which;

    public static enum from {
        SERVICE, ACTIVITY
    }

    public static from getFrom() {
        return which;
    }

    public static void setFrom(from which) {
        MiBandApplication.which = which;
    }

    public MiBandApplication() {
        context = this;
    }

    public static Context getContext() {
        return context;
    }

    public static boolean supportsBluetoothLE() {
        return MiBandApplication.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static boolean isRunningLollipopOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

}
