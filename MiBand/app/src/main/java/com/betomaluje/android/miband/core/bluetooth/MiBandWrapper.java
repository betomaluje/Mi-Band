package com.betomaluje.android.miband.core.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by betomaluje on 6/28/15.
 */
public class MiBandWrapper {

    //private final String TAG = getClass().getSimpleName();

    private Context context;

    public static final int ACTION_CONNECT = 0;
    public static final int ACTION_DISCONNECT = 1;
    public static final int ACTION_LIGHTS = 2;
    public static final int ACTION_VIBRATE_WITH_LED = 3;
    public static final int ACTION_VIBRATE_UNTIL_CALL_STOP = 4;
    public static final int ACTION_VIBRATE_WITHOUT_LED = 5;
    public static final int ACTION_BATTERY = 6;
    public static final int ACTION_REQUEST_CONNECTION = 7;

    private static MiBandWrapper instance;

    public synchronized static MiBandWrapper getInstance(Context context) {
        if (instance == null)
            instance = new MiBandWrapper(context);

        return instance;
    }

    private MiBandWrapper(Context context) {
        this.context = context;
    }

    public void sendAction(final int action) {
        final Intent intent = new Intent(NotificationConstants.ACTION_MIBAND);
        intent.putExtra("type", getIntentAction(action));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void sendAction(final int action, HashMap<String, ? extends Object> params) {
        final Intent intent = new Intent(NotificationConstants.ACTION_MIBAND);
        intent.putExtra("type", getIntentAction(action));

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

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private String getIntentAction(final int action) {
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
            case ACTION_VIBRATE_WITH_LED:
                sentAction = NotificationConstants.MI_BAND_VIBRATE_WITH_LED;
                break;
            case ACTION_VIBRATE_UNTIL_CALL_STOP:
                sentAction = NotificationConstants.MI_BAND_VIBRATE_UNTIL_CALL_STOP;
                break;
            case ACTION_VIBRATE_WITHOUT_LED:
                sentAction = NotificationConstants.MI_BAND_VIBRATE_WITHOUT_LED;
                break;
            case ACTION_BATTERY:
                sentAction = NotificationConstants.MI_BAND_BATTERY;
                break;
            case ACTION_REQUEST_CONNECTION:
                sentAction = NotificationConstants.MI_BAND_REQUEST_CONNECTION;
                break;
        }

        //Log.i(TAG, "sending action: " + sentAction + " to MiBandService");

        return sentAction;
    }

}
