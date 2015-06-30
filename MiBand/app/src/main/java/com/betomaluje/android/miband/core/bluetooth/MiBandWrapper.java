package com.betomaluje.android.miband.core.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by betomaluje on 6/28/15.
 */
public class MiBandWrapper {

    private final String TAG = getClass().getSimpleName();

    private Context context;

    public static final int ACTION_CONNECT = 0;
    public static final int ACTION_DISCONNECT = 1;
    public static final int ACTION_LIGHTS = 2;
    public static final int ACTION_VIBRATE = 3;
    public static final int ACTION_BATTERY = 4;

    private static MiBandWrapper instance;

    public static MiBandWrapper getInstance(Context context) {
        if (instance == null)
            instance = new MiBandWrapper(context);

        return instance;
    }

    private MiBandWrapper(Context context) {
        this.context = context;
    }

    public void sendAction(final int action) {

        String sentAction = "";

        switch (action) {
            case ACTION_CONNECT:
                sentAction = NotificationConstants.MI_BAND_CONNECT;
                break;
            case ACTION_DISCONNECT:
                sentAction = NotificationConstants.MI_BAND_DISCONNECT;
                break;
            case ACTION_LIGHTS:
                sentAction = NotificationConstants.MI_BAND_LIGHTS;
                break;
            case ACTION_VIBRATE:
                sentAction = NotificationConstants.MI_BAND_VIBRATE;
                break;
            case ACTION_BATTERY:
                sentAction = NotificationConstants.MI_BAND_BATTERY;
                break;
        }

        //Log.i(TAG, "sending action: " + sentAction + " to MiBandService");

        final Intent intent = new Intent(NotificationConstants.ACTION_MIBAND);
        intent.putExtra("type", sentAction);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void sendAction(final int action, HashMap<String, ? extends Object> params) {
        String sentAction = "";

        switch (action) {
            case ACTION_CONNECT:
                sentAction = NotificationConstants.MI_BAND_CONNECT;
                break;
            case ACTION_DISCONNECT:
                sentAction = NotificationConstants.MI_BAND_DISCONNECT;
                break;
            case ACTION_LIGHTS:
                sentAction = NotificationConstants.MI_BAND_LIGHTS;
                break;
            case ACTION_VIBRATE:
                sentAction = NotificationConstants.MI_BAND_VIBRATE;
                break;
            case ACTION_BATTERY:
                sentAction = NotificationConstants.MI_BAND_BATTERY;
                break;
        }

        final Intent intent = new Intent(NotificationConstants.ACTION_MIBAND);
        intent.putExtra("type", sentAction);

        for (Map.Entry<String, ? extends Object> entry : params.entrySet()) {
            Object o = entry.getValue();

            if (o instanceof String) {
                //Log.i(TAG, "adding String param: " + entry.getKey() + ": " + (String) entry.getValue());
                intent.putExtra(entry.getKey(), (String) entry.getValue());
            } else if (o instanceof Integer) {
                //Log.i(TAG, "adding Integer param: " + entry.getKey() + ": " + (Integer) entry.getValue());
                intent.putExtra(entry.getKey(), (Integer) entry.getValue());
            }
        }

        //Log.i(TAG, "sending action: " + sentAction + " to MiBandService");

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
