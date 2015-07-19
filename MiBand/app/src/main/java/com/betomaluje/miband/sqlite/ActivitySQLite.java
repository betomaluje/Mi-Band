package com.betomaluje.miband.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.betomaluje.miband.DateUtils;
import com.betomaluje.miband.models.ActivityData;
import com.betomaluje.miband.models.ActivityKind;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by betomaluje on 7/9/15.
 */
public class ActivitySQLite {

    private final String TAG = getClass().getSimpleName();
    public static final String TABLE_NAME = "Activities";

    private Context context;

    private static ActivitySQLite instance;

    public static ActivitySQLite getInstance(Context context) {
        if (instance == null)
            instance = new ActivitySQLite(context);

        return instance;
    }

    public ActivitySQLite(Context context) {
        this.context = context;
    }

    public boolean saveActivity(int timestamp, byte provider, short intensity, byte steps, byte type) {
        MasterSQLiteHelper helperDB = new MasterSQLiteHelper(context);
        SQLiteDatabase db = helperDB.getWritableDatabase();

        Log.e(TAG, "saving Activity " + timestamp);

        ContentValues cv = new ContentValues();
        cv.put("timestamp", timestamp);
        cv.put("provider", provider);
        cv.put("intensity", intensity);
        cv.put("steps", steps);
        cv.put("type", type);

        if (db.insert(TABLE_NAME, null, cv) != -1) {

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp);

            Log.e(TAG, "Activity " + DateUtils.convertString(cal) + " insertada con éxito!");
            db.close();
            return true;
        } else {
            db.close();
            return false;
        }
    }

    public ArrayList<ActivityData> getSleepSamples(int timestamp_from, int timestamp_to) {
        return getActivitiesSample(timestamp_from, timestamp_to, ActivityKind.TYPE_SLEEP);
    }

    public ArrayList<ActivityData> getActivitySamples(int timestamp_from, int timestamp_to) {
        return getActivitiesSample(timestamp_from, timestamp_to, ActivityKind.TYPE_ACTIVITY);
    }

    public ArrayList<ActivityData> getAllActivitiesSamples(int timestamp_from, int timestamp_to) {
        return getActivitiesSample(timestamp_from, timestamp_to, ActivityKind.TYPE_ALL);
    }

    /**
     * Returns all available activity samples from between the two timestamps (inclusive), of the given
     * provided and type(s).
     * @param timestamp_from : time in millis from date
     * @param timestamp_to : time in millis to date
     * @param activityTypes combination of #TYPE_DEEP_SLEEP, #TYPE_LIGHT_SLEEP, #TYPE_ACTIVITY
     * @return
     */
    private ArrayList<ActivityData> getActivitiesSample(long timestamp_from, long timestamp_to, int activityTypes) {
        if (timestamp_to == -1) {
            timestamp_to = Integer.MAX_VALUE; // dont know what happens when I use more than max of a signed int
        }

        MasterSQLiteHelper helperDB = new MasterSQLiteHelper(context);
        SQLiteDatabase db = helperDB.getReadableDatabase();

        ArrayList<ActivityData> allActivities = new ArrayList<ActivityData>();

        String query = "SELECT  * FROM " + TABLE_NAME + " WHERE (timestamp>=" + timestamp_from
                + " AND timestamp<=" + timestamp_to
                + getWhereClauseFor(activityTypes)
                + ") ORDER BY timestamp";

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            allActivities.add(cursorToActivity(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return allActivities;
    }

    public ArrayList<ActivityData> getAllActivities() {
        MasterSQLiteHelper helperDB = new MasterSQLiteHelper(context);
        SQLiteDatabase db = helperDB.getReadableDatabase();

        ArrayList<ActivityData> allActivities = new ArrayList<ActivityData>();

        String query = "SELECT  * FROM " + TABLE_NAME + " ORDER BY timestamp";

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            allActivities.add(cursorToActivity(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return allActivities;
    }

    private String getWhereClauseFor(int activityTypes) {
        if (activityTypes == ActivityKind.TYPE_ALL) {
            return ""; // no further restriction
        }

        StringBuilder builder = new StringBuilder(" AND (");
        byte[] dbActivityTypes = ActivityKind.mapToDBActivityTypes(activityTypes);
        for (int i = 0; i < dbActivityTypes.length; i++) {
            builder.append(" type=").append(dbActivityTypes[i]);
            if (i + 1 < dbActivityTypes.length) {
                builder.append(" OR ");
            }
        }
        builder.append(')');
        return builder.toString();
    }

    private ActivityData cursorToActivity(Cursor cursor) {

        int timestamp = cursor.getInt(0);
        byte provider = (byte) cursor.getInt(1);
        short intensity = (byte) cursor.getInt(2);
        byte steps = (byte) cursor.getInt(3);
        byte type = (byte) cursor.getInt(4);

        return new ActivityData(timestamp, provider, intensity, steps, type);
    }
}
