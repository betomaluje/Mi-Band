package com.betomaluje.android.miband;

import android.app.ActivityManager;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.betomaluje.android.miband.core.MiBand;
import com.betomaluje.android.miband.core.MiBandService;
import com.betomaluje.android.miband.core.bluetooth.MiBandWrapper;
import com.betomaluje.android.miband.core.bluetooth.NotificationConstants;

/**
 * Created by betomaluje on 6/30/15.
 */
public class NotificationListener extends NotificationListenerService {

    private final String TAG = getClass().getSimpleName();

    private BroadcastReceiver miBandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle b = intent.getExtras();

            String action = b.getString("type");

            if (action.equals(NotificationConstants.MI_BAND_DISCONNECT)) {
                stopSelf();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(NotificationListener.this).registerReceiver(miBandReceiver, new IntentFilter(NotificationConstants.ACTION_MIBAND_SERVICE));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(miBandReceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

         /*
        * return early if BluetoothCommunicationService is not running,
        * else the service would get started every time we get a notification.
        * unfortunately we cannot enable/disable NotificationListener at runtime like we do with
        * broadcast receivers because it seems to invalidate the permissions that are
        * necessary for NotificationListener
        */
        boolean isServiceRunning = false;
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MiBandService.class.getName().equals(service.service.getClassName())) {
                isServiceRunning = true;
                break;
            }
        }

        Log.i(TAG, "isServiceRunning: " + isServiceRunning);

        //if the service is not running, we start it
        if (!isServiceRunning) {
            MiBand.getInstance(NotificationListener.this);
            MiBand.init(NotificationListener.this);
        }

        /*
        //check if user has the option "when screen on" also checked
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPrefs.getBoolean("notifications_generic_whenscreenon", false)) {
            PowerManager powermanager = (PowerManager) getSystemService(POWER_SERVICE);
            if (powermanager.isScreenOn()) {
                return;
            }
        }
        */

        String source = sbn.getPackageName();
        Notification notification = sbn.getNotification();

        if ((notification.flags & Notification.FLAG_ONGOING_EVENT) == Notification.FLAG_ONGOING_EVENT) {
            return;
        }

        /* do not display messages from "android"
         * This includes keyboard selection message, usb connection messages, etc
         * Hope it does not filter out too much, we will see...
         */

        if (source.equals("android") ||
                source.equals("com.android.systemui") ||
                source.equals("com.android.dialer") ||
                source.equals("com.android.mms") ||
                source.equals("com.cyanogenmod.eleven") ||
                source.equals("com.fsck.k9") ||
                source.startsWith("com.motorola")) {
            return;
        }

        //if we have a valid notification, we need to post it to Mi Band Service
        Log.i(TAG, "Processing notification from source " + source);
        MiBand.sendAction(MiBandWrapper.ACTION_VIBRATE_WITH_LED);

        /*
        Bundle extras = notification.extras;
        //String title = extras.getCharSequence(Notification.EXTRA_TITLE).toString();
        String content = null;
        if (extras.containsKey(Notification.EXTRA_TEXT)) {
            CharSequence contentCS = extras.getCharSequence(Notification.EXTRA_TEXT);
            if (contentCS != null) {
                content = contentCS.toString();
            }
        }
        */
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }
}
