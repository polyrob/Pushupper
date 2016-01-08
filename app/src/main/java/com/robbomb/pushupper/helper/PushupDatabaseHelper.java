package com.robbomb.pushupper.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.robbomb.pushupper.model.LoggedSet;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NewRob on 1/4/2016.
 */
public class PushupDatabaseHelper extends SQLiteOpenHelper {
    //    public DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    private static final String DB_NAME = "pushups.sqlite";
    private static final int VERSION = 1;
    private static final String TABLE_PUSHUPS = "pushups";
    private static final String COLUMN_DATETIME = "datetime";
    private static final String COLUMN_REPS = "reps";

    public PushupDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the table if it doesn't exist
        db.execSQL("create table pushups (_id INTEGER primary key autoincrement, datetime TEXT, reps INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // We need not worry about this guy now, or probably ever.
    }

    public long insertPushupSet(LoggedSet set) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DATETIME, DateHelper.format(set.getDateTime()));
        cv.put(COLUMN_REPS, set.getReps());
        return getWritableDatabase().insert(TABLE_PUSHUPS, null, cv);
    }

    public List<LoggedSet> getLoggedPushups() {
        ArrayList<LoggedSet> loggedSets = new ArrayList<>();

        String selectQuery = "SELECT datetime, reps FROM " + TABLE_PUSHUPS;
        SQLiteDatabase db = this.getReadableDatabase();
        try {

            Cursor cursor = db.rawQuery(selectQuery, null);
            try {

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        LoggedSet loggedSet = new LoggedSet();
                        //only one column
                        String dateTimeString = cursor.getString(0);

                        DateTime setDate = DateHelper.parse(dateTimeString);
                        loggedSet.setDateTime(setDate);
                        loggedSet.setReps(cursor.getInt(1));
                        loggedSets.add(loggedSet);


                    } while (cursor.moveToNext());
                }

            } finally {
                try {
                    cursor.close();
                } catch (Exception ignore) {
                }
            }

        } finally {
            try {
                db.close();
            } catch (Exception ignore) {
            }
        }

        return loggedSets;
    }

    public ArrayList<LoggedSet> getPushupsForDate(DateTime date) {
        ArrayList<LoggedSet> loggedSets = new ArrayList<>();

        String dateString = DateHelper.format(date).substring(0, 10);

        String selectQuery = "SELECT datetime, reps FROM " + TABLE_PUSHUPS
                + " WHERE datetime LIKE '" + dateString + "%'";

        Log.i("PushupDatabaseHelper", selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        try {

            Cursor cursor = db.rawQuery(selectQuery, null);
            try {

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        LoggedSet loggedSet = new LoggedSet();
                        //only one column
                        String dateTimeString = cursor.getString(0);

                        DateTime setDate = DateHelper.parse(dateTimeString);
                        loggedSet.setDateTime(setDate);
                        loggedSet.setReps(cursor.getInt(1));
                        loggedSets.add(loggedSet);

                    } while (cursor.moveToNext());
                }

            } finally {
                try {
                    cursor.close();
                } catch (Exception ignore) {
                }
            }

        } finally {
            try {
                db.close();
            } catch (Exception ignore) {
            }
        }

        return loggedSets;
    }

    public void dropTable() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM pushups");
        db.execSQL("VACUUM");
//        db.execSQL("create table pushups (_id INTEGER primary key autoincrement, datetime TEXT, reps INTEGER)");
    }

}