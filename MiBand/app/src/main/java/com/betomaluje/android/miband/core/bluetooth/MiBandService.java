package com.betomaluje.android.miband.core.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.betomaluje.android.miband.core.ActionCallback;
import com.betomaluje.android.miband.core.MiBand;
import com.betomaluje.android.miband.core.model.BatteryInfo;
import com.betomaluje.android.miband.core.model.VibrationMode;

/**
 * Created by betomaluje on 6/26/15.
 */
public class MiBandService extends Service {
    private final static String TAG = MiBandService.class.getSimpleName();

    private MiBand miBand;

    private boolean mStarted = false;

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
                    if (b.containsKey("color")) {

                        int color = b.getInt("color", 255);

                        //Log.i(TAG, "color received: " + color);

                        miBand.setLedColor(color);
                    }
                    break;
                case NotificationConstants.MI_BAND_VIBRATE:

                    miBand.startVibration(VibrationMode.VIBRATION_WITH_LED);

                    break;
                case NotificationConstants.MI_BAND_BATTERY:

                    miBand.getBatteryInfo(new ActionCallback() {
                        @Override
                        public void onSuccess(final Object data) {

                            BatteryInfo battery = (BatteryInfo) data;

                            broadcastUpdate(NotificationConstants.MI_BAND_BATTERY, battery);

                            //Log.e(TAG, "battery: " + battery.toString());
                        }

                        @Override
                        public void onFail(int errorCode, String msg) {
                            Log.e(TAG, "Fail battery: " + msg);
                        }
                    });
                    break;
            }
        }
    };

    private final BroadcastReceiver bluetoothStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                    Log.d(TAG, "Bluetooth switched off");
                    MiBand.disconnect();
                } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                    Log.d(TAG, "Bluetooth switched on, initialising");
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        //we are listening from activities
        LocalBroadcastManager.getInstance(MiBandService.this).registerReceiver(miBandReceiver, new IntentFilter(NotificationConstants.ACTION_MIBAND));

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStatusChangeReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Log.d(TAG, "Destroying MiBandService");

        unregisterReceiver(bluetoothStatusChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(miBandReceiver);
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

        switch (action) {
            case NotificationConstants.MI_BAND_CONNECT:
                if (!mStarted) {
                    mStarted = true;
                    connectMiBand();
                }
                break;
        }

        return START_STICKY;
    }

    private void connectMiBand() {
        miBand = MiBand.getInstance(MiBandService.this);
        if (!miBand.isConnected())
            miBand.connect(connectionAction);
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
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(NotificationConstants.ACTION_MIBAND_SERVICE);
        intent.putExtra("type", action);
        LocalBroadcastManager.getInstance(MiBandService.this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, int errorCode) {
        final Intent intent = new Intent(NotificationConstants.ACTION_MIBAND_SERVICE);
        intent.putExtra("type", action);
        intent.putExtra("errorCode", errorCode);
        LocalBroadcastManager.getInstance(MiBandService.this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, BatteryInfo batteryInfo) {
        final Intent intent = new Intent(NotificationConstants.ACTION_MIBAND_SERVICE);
        intent.putExtra("type", action);
        intent.putExtra("battery", batteryInfo);
        LocalBroadcastManager.getInstance(MiBandService.this).sendBroadcast(intent);
    }
}
