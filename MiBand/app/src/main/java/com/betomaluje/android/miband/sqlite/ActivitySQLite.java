package com.betomaluje.android.miband.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.betomaluje.android.miband.models.ActivityData;

import java.util.ArrayList;

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
            Log.e(TAG, "Activity " + timestamp + " insertada con éxito!");
            db.close();
            return true;
        } else {
            db.close();
            return false;
        }
    }

    public ArrayList<ActivityData> getActivitiesSample(int timestamp_from, int timestamp_to, byte provider) {
        if (timestamp_to == -1) {
            timestamp_to = Integer.MAX_VALUE; // dont know what happens when I use more than max of a signed int
        }

        MasterSQLiteHelper helperDB = new MasterSQLiteHelper(context);
        SQLiteDatabase db = helperDB.getReadableDatabase();

        ArrayList<ActivityData> allActivities = new ArrayList<ActivityData>();

        //String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY notify DESC, name ASC";
        String query = "SELECT  * FROM " + TABLE_NAME + " WHERE (provider=" + provider + " AND timestamp>=" + timestamp_from
                + " AND timestamp<=" + timestamp_to + ") ORDER BY timestamp";

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

    private ActivityData cursorToActivity(Cursor cursor) {

        int timestamp = cursor.getInt(0);
        byte provider = (byte) cursor.getInt(1);
        short intensity = (byte) cursor.getInt(2);
        byte steps = (byte) cursor.getInt(3);
        byte type = (byte) cursor.getInt(4);

        return new ActivityData(timestamp, provider, intensity, steps, type);
    }
}
