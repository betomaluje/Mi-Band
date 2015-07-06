package com.betomaluje.android.miband.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by betomaluje on 7/6/15.
 */
public class MasterSQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "miband.db";
    private static final int DATABASE_VERSION = 1;

    //User
    public String CREATE_APPS_DB = "CREATE TABLE IF NOT EXISTS " + AppsSQLite.TABLE_NAME + " ("
            + "id INTEGER NOT NULL PRIMARY KEY,"
            + "name TEXT NOT NULL,"
            + "source TEXT NOT NULL,"
            + "color INTEGER NOT NULL,"
            + "notify INTEGER NOT NULL DEFAULT 0,"
            + "pause_time INTEGER NOT NULL DEFAULT 500,"
            + "start_time INTEGER,"
            + "end_time INTEGER,"
            + "UNIQUE(source) ON CONFLICT REPLACE);";

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
