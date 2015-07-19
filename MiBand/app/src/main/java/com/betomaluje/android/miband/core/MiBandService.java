package com.betomaluje.android.miband.core;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.betomaluje.android.miband.R;
import com.betomaluje.android.miband.activities.MainActivity;
import com.betomaluje.android.miband.core.bluetooth.NotificationConstants;
import com.betomaluje.android.miband.core.model.BatteryInfo;
import com.betomaluje.android.miband.core.model.VibrationMode;

/**
 * Created by betomaluje on 6/26/15.
 */
public class MiBandService extends Service {
    private final String TAG = MiBandService.class.getSimpleName();
    public static final int ONGOING_NOTIFICATION_ID = 2121;

    public MiBand miBand;

    private BroadcastReceiver miBandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();

            String action = b.getString("type");

            //Log.i(TAG, "action received: " + action);
            //Log.i(TAG, "miBand.isConnected(): " + miBand.isConnected());

            switch (action) {
                case NotificationConstants.MI_BAND_CONNECT:
                    connectMiBand();
                    break;
                case NotificationConstants.MI_BAND_LIGHTS:
                    if (b.containsKey(NotificationConstants.KEY_COLOR)) {

                        int color = b.getInt(NotificationConstants.KEY_COLOR, 255);

                        if (b.containsKey(NotificationConstants.KEY_TIMES)) {
                            //with flashing
                            int flash_time = b.getInt(NotificationConstants.KEY_TIMES, 3);
                            int pause_time = b.getInt(NotificationConstants.KEY_PAUSE_TIME, 1000);

                            miBand.setLedColor(flash_time, color, pause_time);
                        } else {
                            //normal flashing
                            miBand.setLedColor(color);
                        }
                    }
                    break;
                case NotificationConstants.MI_BAND_VIBRATE_WITH_LED:

                    miBand.startVibration(VibrationMode.VIBRATION_WITH_LED);

                    break;
                case NotificationConstants.MI_BAND_VIBRATE_UNTIL_CALL_STOP:

                    miBand.startVibration(VibrationMode.VIBRATION_UNTIL_CALL_STOP);

                    break;
                case NotificationConstants.MI_BAND_VIBRATE_WITHOUT_LED:

                    miBand.startVibration(VibrationMode.VIBRATION_WITHOUT_LED);

                    break;
                case NotificationConstants.MI_BAND_VIBRATE_CUSTOM:

                    int times = b.getInt(NotificationConstants.KEY_TIMES, 3);
                    int on_time = b.getInt(NotificationConstants.KEY_ON_TIME, 250);
                    int off_time = b.getInt(NotificationConstants.KEY_PAUSE_TIME, 500);

                    miBand.customVibration(times, on_time, off_time);

                    break;
                case NotificationConstants.MI_BAND_NEW_NOTIFICATION:
                    //if (b.containsKey(NotificationConstants.KEY_COLOR) && b.containsKey(NotificationConstants.KEY_PAUSE_TIME)) {
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
                    //}

                    break;
                case NotificationConstants.MI_BAND_BATTERY:

                    miBand.getBatteryInfo(new ActionCallback() {
                        @Override
                        public void onSuccess(final Object data) {

                            BatteryInfo battery = (BatteryInfo) data;

                            broadcastUpdate(NotificationConstants.MI_BAND_BATTERY, battery);
                        }

                        @Override
                        public void onFail(int errorCode, String msg) {
                            Log.e(TAG, "Fail battery: " + msg);
                        }
                    });
                    break;
                case NotificationConstants.MI_BAND_START_SYNC:
                    miBand.startListeningSync();
                    break;
                case NotificationConstants.MI_BAND_STOP_SYNC:
                    if (miBand.isSyncNotification())
                        miBand.stopListeningSync();
                case NotificationConstants.MI_BAND_REQUEST_CONNECTION:
                    broadcastUpdate(NotificationConstants.MI_BAND_REQUEST_CONNECTION, miBand != null && miBand.isConnected());
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        //we are listening from activities
        LocalBroadcastManager.getInstance(MiBandService.this).registerReceiver(miBandReceiver, new IntentFilter(NotificationConstants.ACTION_MIBAND));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "Destroying MiBandService");

        LocalBroadcastManager.getInstance(MiBandService.this).unregisterReceiver(miBandReceiver);

        MiBand.dispose();
        broadcastUpdate(NotificationConstants.MI_BAND_DISCONNECT);
        broadcastUpdate("CANCEL_WATER");

        //cancel notification
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(ONGOING_NOTIFICATION_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            Log.i(TAG, "no intent");
            return START_NOT_STICKY;
        }

        String action = intent.getAction();

        if (action == null) {
            Log.i(TAG, "no action");
            return START_NOT_STICKY;
        }

        createOngoingNotification();

        connectMiBand();

        return START_STICKY;
    }

    private void createOngoingNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(MiBandService.this)
                        .setSmallIcon(R.drawable.notification_template_icon_bg)
                        .setContentTitle("Mi Band Service")
                        .setOngoing(true)
                        .setContentText("Click to close");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(MiBandService.this, MainActivity.class);

        resultIntent.putExtra("service", true);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(MiBandService.this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, mBuilder.build());
    }

    private void connectMiBand() {
        miBand = MiBand.getInstance(MiBandService.this);
        if (!miBand.isConnected()) {
            miBand.connect(connectionAction);
        } else {
            Log.d(TAG, "Already connected with Mi Band!");
            broadcastUpdate(NotificationConstants.MI_BAND_CONNECT);
        }

    }

    private ActionCallback connectionAction = new ActionCallback() {
        @Override
        public void onSuccess(Object data) {
            Log.d(TAG, "Connected with Mi Band!");
            broadcastUpdate(NotificationConstants.MI_BAND_CONNECT);
        }

        @Override
        public void onFail(int errorCode, String msg) {
            Log.d(TAG, "Connection failed: " + msg);

            broadcastUpdate(NotificationConstants.MI_BAND_DISCONNECT, errorCode);
            //miBand.connect(connectionAction);
        }
    };

    public void broadcastUpdate(final String action) {
        final Intent intent = new Intent(NotificationConstants.ACTION_MIBAND_SERVICE);
        intent.putExtra("type", action);
        LocalBroadcastManager.getInstance(MiBandService.this).sendBroadcast(intent);
    }

    public void broadcastUpdate(final String action, int errorCode) {
        final Intent intent = new Intent(NotificationConstants.ACTION_MIBAND_SERVICE);
        intent.putExtra("type", action);
        intent.putExtra("errorCode", errorCode);
        LocalBroadcastManager.getInstance(MiBandService.this).sendBroadcast(intent);
    }

    public void broadcastUpdate(final String action, BatteryInfo batteryInfo) {
        final Intent intent = new Intent(NotificationConstants.ACTION_MIBAND_SERVICE);
        intent.putExtra("type", action);
        intent.putExtra("battery", batteryInfo);
        LocalBroadcastManager.getInstance(MiBandService.this).sendBroadcast(intent);
    }

    public void broadcastUpdate(final String action, boolean isConnected) {
        final Intent intent = new Intent(NotificationConstants.ACTION_MIBAND_SERVICE);
        intent.putExtra("type", action);
        intent.putExtra("isConnected", isConnected);
        LocalBroadcastManager.getInstance(MiBandService.this).sendBroadcast(intent);
    }
}
