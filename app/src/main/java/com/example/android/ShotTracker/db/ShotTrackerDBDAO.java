package com.example.android.ShotTracker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.SQLException;

/**
 * Generic Database Access Object class.
 * Implementes CRUD methods for database tables.
 */
public class ShotTrackerDBDAO {

    //\todo change every ID to long in every DB object and in the DataBaseHelper (table creation statements)
    protected SQLiteDatabase database;
    private DataBaseHelper dbHelper;
    private Context mContext;


    public ShotTrackerDBDAO(Context context) {
        this.mContext = context;
        dbHelper = DataBaseHelper.getHelper(mContext);
        open();
    }

    public void open() throws SQLException {
        if (dbHelper == null)
            dbHelper = DataBaseHelper.getHelper(mContext);
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
        database = null;
    }
}
