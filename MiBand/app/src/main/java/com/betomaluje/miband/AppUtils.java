package com.betomaluje.miband;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by betomaluje on 7/19/15.
 */
public class AppUtils {

    public static boolean supportsBluetoothLE(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static boolean isRunningLollipopOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

}
