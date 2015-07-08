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

import com.betomaluje.android.miband.core.ActionCallback;
import com.betomaluje.android.miband.core.MiBand;
import com.betomaluje.android.miband.core.MiBandService;
import com.betomaluje.android.miband.core.bluetooth.MiBandWrapper;
import com.betomaluje.android.miband.core.bluetooth.NotificationConstants;
import com.betomaluje.android.miband.models.App;
import com.betomaluje.android.miband.sqlite.AppsSQLite;

import java.util.HashMap;

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
    public void onNotificationPosted(final StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        if (!shouldWeNotify(sbn)) return;

        /*
        We get the MiBand instance. If we are connected, we handle the status bar notification and send the info to the Mi Band.
        If we are not connected, we connect and wait for the success callback. Then we handle the status bar notification
         */
        MiBand miBand = MiBand.getInstance(NotificationListener.this);

        Log.i(TAG, "miBand connected: " + miBand.isConnected());

        if (!miBand.isConnected()) {
            miBand.connect(new ActionCallback() {
                @Override
                public void onSuccess(Object data) {
                    checkService();

                    handleNotification(sbn);
                }

                @Override
                public void onFail(int errorCode, String msg) {
                    Log.i(TAG, "onFail: " + msg);
                }
            });
        } else {
            checkService();

            handleNotification(sbn);
        }
    }

    private void checkService() {
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
            MiBand.initService(NotificationListener.this);
        }
    }

    private void handleNotification(StatusBarNotification sbn) {
        //only if we have a valid notification, we need to post it to Mi Band Service
        App app = AppsSQLite.getInstance(NotificationListener.this).getApp(sbn.getPackageName());

        HashMap<String, Integer> params = new HashMap<String, Integer>();

        params.put(NotificationConstants.KEY_COLOR_1, app.getColor());
        params.put(NotificationConstants.KEY_PAUSE_TIME, app.getPauseTime());
        params.put(NotificationConstants.KEY_ON_TIME, app.getOnTime());
        params.put(NotificationConstants.KEY_TIMES, app.getNotificationTimes());

        MiBand.sendAction(MiBandWrapper.ACTION_NOTIFY, params);

        MiBandWrapper.getInstance(NotificationListener.this).sendAction(MiBandWrapper.ACTION_REQUEST_CONNECTION);
    }

    private boolean shouldWeNotify(StatusBarNotification sbn) {
        String source = sbn.getPackageName();
        Notification notification = sbn.getNotification();

        Log.i(TAG, "Processing notification from source " + source);

        if ((notification.flags & Notification.FLAG_ONGOING_EVENT) == Notification.FLAG_ONGOING_EVENT) {
            return false;
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

            return false;
        }

        App app = AppsSQLite.getInstance(NotificationListener.this).getApp(source);

        return app != null && app.shouldWeNotify();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }
}
