package com.betomaluje.miband.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.betomaluje.miband.AppUtils;

/**
 * Created by betomaluje on 7/6/15.
 */
public class MasterSQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "miband.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * WITHOUT ROWID is only available with sqlite 3.8.2, which is available
     * with Lollipop and later.
     *
     * @return the "WITHOUT ROWID" string or an empty string for pre-Lollipop devices
     */
    private String getWithoutRowId() {
        if (AppUtils.isRunningLollipopOrLater()) {
            return " WITHOUT ROWID;";
        }
        return "";
    }

    //Activities
    public String CREATE_ACTIVITIES_DB = "CREATE TABLE IF NOT EXISTS " + ActivitySQLite.TABLE_NAME + " ("
            + "timestamp INT, "
            + "provider TINYINT, "
            + "intensity SMALLINT, "
            + "steps TINYINT, "
            + "type TINYINT, "
            + "PRIMARY KEY (timestamp, provider) ON CONFLICT REPLACE) " + getWithoutRowId();

    public String DELETE_ACTIVITIES_DB = "DROP TABLE IF EXISTS " + ActivitySQLite.TABLE_NAME;

    public MasterSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ACTIVITIES_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_ACTIVITIES_DB);
        onCreate(db);
    }
}
