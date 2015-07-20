package com.betomaluje.android.miband.example;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;


import com.betomaluje.android.miband.example.activities.MainServiceActivity;
import com.betomaluje.miband.MiBand;
import com.betomaluje.miband.bluetooth.MiBandWrapper;

/**
 * Created by betomaluje on 7/3/15.
 */
public class WaterReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {

        WaterScheduler.getInstance(context).notify(new WaterScheduler.ScheduleCallback() {
            @Override
            public void OnScheduleNext() {
                broadcastWater();

                createNotification("Drink refreshing water!", context);
            }

            @Override
            public void OnScheduleTomorrow() {
                createNotification("Next Water alarm set for tomorrow at 8 am", context);
            }
        });
    }

    public void createNotification(String text, Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification_template_icon_bg)
                        .setContentTitle("Water notification")
                        .setContentText(text);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainServiceActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainServiceActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_ONE_SHOT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());
    }

    private void broadcastWater() {
        Log.i(TAG, "sending water notification to band");
        MiBand.sendAction(MiBandWrapper.ACTION_VIBRATE_WITHOUT_LED);
    }
}
