package com.betomaluje.android.miband.example.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.betomaluje.android.miband.example.models.App;

import java.util.ArrayList;

/**
 * Created by betomaluje on 7/6/15.
 */
public class AppsSQLite {

    private final String TAG = getClass().getSimpleName();
    public static final String TABLE_NAME = "Apps";

    private Context context;

    private static AppsSQLite instance;

    public static AppsSQLite getInstance(Context context) {
        if (instance == null)
            instance = new AppsSQLite(context);

        return instance;
    }

    public AppsSQLite(Context context) {
        this.context = context;
    }

    public boolean doesTableExists() {
        MasterSQLiteHelper helperDB = new MasterSQLiteHelper(context);
        SQLiteDatabase db = helperDB.getReadableDatabase();

        String query = "SELECT 1 FROM " + TABLE_NAME;

        Cursor cursor = db.rawQuery(query, null);

        boolean doesExist = cursor.moveToFirst();

        cursor.close();
        db.close();

        return doesExist;
    }

    public boolean saveApp(String name, String source, int color, boolean shouldWeNotify, int pauseTime) {
        return saveApp(name, source, color, shouldWeNotify, pauseTime, -1, -1);
    }

    public boolean saveApp(String name, String source, int color, boolean shouldWeNotify, int pauseTime, int startTime, int endTime) {
        MasterSQLiteHelper helperDB = new MasterSQLiteHelper(context);
        SQLiteDatabase db = helperDB.getWritableDatabase();

        //Log.e(TAG, "saving App " + source);

        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("source", source);
        cv.put("color", color);
        cv.put("notify", shouldWeNotify ? 1 : 0);
        cv.put("pause_time", pauseTime);
        cv.put("start_time", startTime);
        cv.put("end_time", endTime);

        if (db.insert(TABLE_NAME, null, cv) != -1) {
            //Log.e(TAG, "App " + source + " insertada con éxito!");
            db.close();
            return true;
        } else {
            db.close();
            return false;
        }
    }

    public boolean deleteApp(String source) {
        //Log.e(TAG, "deleting app: " + source);
        MasterSQLiteHelper helperDB = new MasterSQLiteHelper(context);
        SQLiteDatabase db = helperDB.getReadableDatabase();
        if (db.delete(TABLE_NAME, "source='" + source + "'", null) > 0) {
            //Log.e(TAG, "App " + source + " eliminada");
            db.close();
            return true;
        } else {
            db.close();
            return false;
        }
    }

    public boolean updateApp(App app) {
        return updateApp(app.getSource(), app.getColor(), app.isNotify(), app.getPauseTime(),
                app.getOnTime(), app.getNotificationTimes(), app.getStartTime(), app.getEndTime());
    }

    public boolean updateApp(String source, int color, boolean shouldWeNotify, int pauseTime, int onTime, int notificationTime, int startTime, int endTime) {
        MasterSQLiteHelper helperDB = new MasterSQLiteHelper(context);
        SQLiteDatabase db = helperDB.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("color", color);
        cv.put("notify", shouldWeNotify ? 1 : 0);
        cv.put("pause_time", pauseTime);
        cv.put("on_time", Math.min(onTime, 500)); //maximum of 500 milliseconds
        cv.put("notification_time", notificationTime);
        cv.put("start_time", startTime);
        cv.put("end_time", endTime);

        if (db.update(TABLE_NAME, cv, "source='" + source + "'", null) != -1) {
            //Log.e(TAG, "App " + source + " actualizada con éxito!");

            db.close();
            return true;
        } else {
            db.close();
            return false;
        }
    }

    public App getApp(String source) {
        //Log.i(TAG, "retrieving app: " + source);

        MasterSQLiteHelper helperDB = new MasterSQLiteHelper(context);
        SQLiteDatabase db = helperDB.getReadableDatabase();

        App to_return = null;

        String query = "SELECT * FROM " + TABLE_NAME + " WHERE source='" + source + "'";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            to_return = cursorToApp(cursor);
        }

        cursor.close();
        db.close();

        return to_return;
    }

    public ArrayList<App> getApps() {
        MasterSQLiteHelper helperDB = new MasterSQLiteHelper(context);
        SQLiteDatabase db = helperDB.getReadableDatabase();

        ArrayList<App> apps = new ArrayList<>();

        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY notify DESC, name ASC";

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            apps.add(cursorToApp(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return apps;
    }

    private App cursorToApp(Cursor cursor) {
        App app = new App();
        app.setName(cursor.getString(0));
        app.setSource(cursor.getString(1));
        app.setColor(cursor.getInt(2));
        app.setNotify(cursor.getInt(3));
        app.setPauseTime(cursor.getInt(4));
        app.setOnTime(cursor.getInt(5));
        app.setNotificationTimes(cursor.getInt(6));
        app.setStartTime(cursor.getInt(7));
        app.setEndTime(cursor.getInt(8));
        return app;
    }
}
