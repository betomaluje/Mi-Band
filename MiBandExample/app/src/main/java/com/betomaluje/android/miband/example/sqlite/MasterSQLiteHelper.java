package com.betomaluje.android.miband.example.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.betomaluje.android.miband.example.MiBandApplication;

/**
 * Created by betomaluje on 7/6/15.
 */
public class MasterSQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "miband_example.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * WITHOUT ROWID is only available with sqlite 3.8.2, which is available
     * with Lollipop and later.
     *
     * @return the "WITHOUT ROWID" string or an empty string for pre-Lollipop devices
     */
    private String getWithoutRowId() {
        if (MiBandApplication.isRunningLollipopOrLater()) {
            return " WITHOUT ROWID;";
        }
        return "";
    }

    //Apps
    public String CREATE_APPS_DB = "CREATE TABLE IF NOT EXISTS " + AppsSQLite.TABLE_NAME + " ("
            + "name TEXT NOT NULL,"
            + "source TEXT NOT NULL,"
            + "color INT NOT NULL,"
            + "notify INT NOT NULL DEFAULT 0,"
            + "pause_time INT NOT NULL DEFAULT 250,"
            + "on_time INT NOT NULL DEFAULT 250,"
            + "notification_time INT NOT NULL DEFAULT 3,"
            + "start_time INT,"
            + "end_time INT,"
            + "PRIMARY KEY (source) ON CONFLICT REPLACE) " + getWithoutRowId();

    public String DELETE_APPS_DB = "DROP TABLE IF EXISTS " + AppsSQLite.TABLE_NAME;

    public MasterSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_APPS_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_APPS_DB);
        onCreate(db);
    }
}
