package com.betomaluje.android.miband.example;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;

import com.betomaluje.miband.ActionCallback;
import com.betomaluje.miband.MiBand;

import java.util.Calendar;

/**
 * Created by betomaluje on 7/4/15.
 */
public class WaterScheduler {

    private static final String TAG = WaterScheduler.class.getSimpleName();

    private static Context context;
    private static PendingIntent pendingIntent;

    private static AlarmManager alarmManager;

    private static WaterScheduler instance;

    private boolean alreadyScheduled = false;

    //every 45 minutes
    private long nextUpdate = (1000 * 60 * 45);

    public interface ScheduleCallback {
        void OnScheduleNext();

        void OnScheduleTomorrow();
    }

    public static WaterScheduler getInstance(Context context) {
        if (instance == null) {
            instance = new WaterScheduler(context);
        } else {
            WaterScheduler.context = context;
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }

        return instance;
    }

    public WaterScheduler(Context context) {
        WaterScheduler.context = context;
        Intent alarmIntent = new Intent(context, WaterReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void notify(final ScheduleCallback scheduleCallback) {
        if (isAlarmForToday()) {
            final MiBand miBand = MiBand.getInstance(context);

            Log.i(TAG, "miBand connected: " + miBand.isConnected());

            if (!miBand.isConnected()) {
                miBand.connect(new ActionCallback() {
                    @Override
                    public void onSuccess(Object data) {

                        if (scheduleCallback != null)
                            scheduleCallback.OnScheduleNext();
                    }

                    @Override
                    public void onFail(int errorCode, String msg) {
                        Log.i(TAG, "onFail: " + msg);
                    }
                });
            } else {

                if (scheduleCallback != null)
                    scheduleCallback.OnScheduleNext();
            }
        } else {
            Log.i(TAG, "notify after time! Scheduling new alarm!");
            scheduleForTomorrow(scheduleCallback);
        }
    }

    public void schedule(final ScheduleCallback scheduleCallback) {
        alarmManager.cancel(pendingIntent);

        if (isAlarmForToday()) {
            //we schedule the alarm for the next X minutes, repeating every X minutes

            if (alarmManager == null) {
                Log.i(TAG, "AlarmManager null :(");
                return;
            }

            //we cancel any previous alarm
            alarmManager.cancel(pendingIntent);

            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextUpdate, nextUpdate, pendingIntent);

            if (scheduleCallback != null)
                scheduleCallback.OnScheduleNext();
        } else {
            Log.i(TAG, "schedule after time! Scheduling new alarm!");
            scheduleForTomorrow(scheduleCallback);
        }
    }

    private void scheduleForTomorrow(final ScheduleCallback scheduleCallback) {
        if (alarmManager == null) {
            Log.i(TAG, "AlarmManager null :(");
            return;
        }

        if (!alreadyScheduled) {
            alreadyScheduled = true;

            //we cancel any previous alarm
            alarmManager.cancel(pendingIntent);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 8);

            //exactly at 8 am for tomorrow
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), nextUpdate, pendingIntent);

            if (scheduleCallback != null)
                scheduleCallback.OnScheduleTomorrow();
        }
    }

    private boolean isAlarmForToday() {
        long currentTimeMillis = System.currentTimeMillis();

        long nextUpdateTimeMillis = currentTimeMillis + nextUpdate;
        Time nextUpdateTime = new Time();
        nextUpdateTime.set(nextUpdateTimeMillis);

        Log.v(TAG, "water time: " + nextUpdateTime.hour);

        //only between 8 am and 18 pm
        return nextUpdateTime.hour > 7 && nextUpdateTime.hour < 18;
    }
}
