package com.betomaluje.android.miband.example;

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

import com.betomaluje.android.miband.example.models.App;
import com.betomaluje.android.miband.example.models.ShouldNotifyApp;
import com.betomaluje.android.miband.example.sqlite.AppsSQLite;
import com.betomaluje.miband.ActionCallback;
import com.betomaluje.miband.MiBand;
import com.betomaluje.miband.MiBandService;
import com.betomaluje.miband.bluetooth.MiBandWrapper;
import com.betomaluje.miband.bluetooth.NotificationConstants;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.util.ArrayList;

/**
 * Created by betomaluje on 6/30/15.
 */
public class NotificationListener extends NotificationListenerService {

    private final String TAG = getClass().getSimpleName();
    private MiBand miBand;
    private long lastNotificationMillis = -1;

    private ArrayList<ShouldNotifyApp> queueApps = new ArrayList<>();

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

        Log.i(TAG, "Processing notification from source " + sbn.getPackageName());

        /*
        We get the MiBand instance. If we are connected, we handle the status bar notification and send the info to the Mi Band.
        If we are not connected, we connect and wait for the success callback. Then we handle the status bar notification
         */
        miBand = MiBand.getInstance(NotificationListener.this);

        Log.i(TAG, "miBand connected: " + miBand.isConnected());

        if (!miBand.isConnected()) {
            miBand.connect(new ActionCallback() {
                @Override
                public void onSuccess(Object data) {
                    //checkService();

                    handleNotification(sbn);
                }

                @Override
                public void onFail(int errorCode, String msg) {
                    Log.i(TAG, "onFail: " + msg);
                }
            });
        } else {
            //checkService();

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

        //Log.i(TAG, "handleNotification: " + app.getName());

        miBand.notifyBand(app.getColor());

        /*
        //HashMap<String, Integer> params = new HashMap<String, Integer>();

        //params.put(NotificationConstants.KEY_COLOR, app.getColor());

        //MiBand.sendAction(MiBandWrapper.ACTION_NOTIFY, params);

        int vibrate_times = -1;
        int flash_time = -1;
        int pause_time = -1;

        if (b.containsKey(NotificationConstants.KEY_TIMES))
            vibrate_times = b.getInt(NotificationConstants.KEY_TIMES, 3);

        if (b.containsKey(NotificationConstants.KEY_ON_TIME))
            flash_time = b.getInt(NotificationConstants.KEY_ON_TIME, 250);

        if (b.containsKey(NotificationConstants.KEY_PAUSE_TIME))
            pause_time = b.getInt(NotificationConstants.KEY_PAUSE_TIME, 500);

        int color = b.getInt(NotificationConstants.KEY_COLOR, 255);

        if (vibrate_times == -1 || flash_time == -1 || pause_time == -1)
            miBand.notifyBand(color);
        else
            miBand.notifyBand(vibrate_times, flash_time, pause_time, color);
        */

        MiBandWrapper.getInstance(NotificationListener.this).sendAction(MiBandWrapper.ACTION_REQUEST_CONNECTION);
    }

    private boolean shouldWeNotify(StatusBarNotification sbn) {
        String source = sbn.getPackageName();
        Notification notification = sbn.getNotification();

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

        boolean passedTime = isAppInQueue(source);

        if(!passedTime) {
            if (lastNotificationMillis != -1) {
                if (Seconds.secondsBetween(new DateTime(lastNotificationMillis), new DateTime(System.currentTimeMillis())).getSeconds() < 5) {
                    passedTime = false;
                } else {
                    lastNotificationMillis = System.currentTimeMillis();
                }
            } else {
                lastNotificationMillis = System.currentTimeMillis();
            }
        }

        Log.i(TAG, "passedTime: " + passedTime);

        if (app != null) {
            Log.i(TAG, "app.shouldWeNotify(): " + app.shouldWeNotify());
        }

        return !passedTime && app != null && app.shouldWeNotify();
    }

    private boolean isAppInQueue(String source) {
        boolean isInQueue = false;

        for (ShouldNotifyApp app : queueApps) {
            if (app.packageName.equals(source)) {
                isInQueue = true;
                break;
            }
        }

        if (!isInQueue) {
            //we put it
            queueApps.add(new ShouldNotifyApp(source, queueApps));
        }

        Log.i(TAG, "source: " + source + " isAppInQueue: " + isInQueue);

        return isInQueue;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }
}
