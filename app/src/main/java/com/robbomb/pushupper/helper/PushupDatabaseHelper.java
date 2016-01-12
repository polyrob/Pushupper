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

    private static final String DB_NAME = "pushups.sqlite";
    private static final int VERSION = 1;

    private static final String TABLE_POOSH_LOG = "poosh_log";
    private static final String COLUMN_DATETIME = "datetime";
    private static final String COLUMN_REPS = "reps";
    private static final String COLUMN_WORKOUT_ID = "workout_id";

    private static final String TABLE_TARGETS = "targets";

    public PushupDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the table if it doesn't exist
        db.execSQL("create table " + TABLE_POOSH_LOG
                + " (_id INTEGER primary key autoincrement, "
                + COLUMN_WORKOUT_ID + " INTEGER, "
                + COLUMN_DATETIME + " TEXT, "
                + COLUMN_REPS + " INTEGER)");

        db.execSQL("create table " + TABLE_TARGETS
                + " (_id INTEGER primary key autoincrement, "
                + COLUMN_WORKOUT_ID + " INTEGER, "
                + COLUMN_DATETIME + " TEXT, "
                + COLUMN_REPS + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // We need not worry about this guy now, or probably ever.
    }

    public long insertPushupSet(LoggedSet set) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WORKOUT_ID, set.getWorkoutId());
        cv.put(COLUMN_DATETIME, DateHelper.format(set.getDateTime()));
        cv.put(COLUMN_REPS, set.getReps());
        return getWritableDatabase().insert(TABLE_POOSH_LOG, null, cv);
    }

    public List<LoggedSet> getLoggedPushups() {
        ArrayList<LoggedSet> loggedSets = new ArrayList<>();

        String selectQuery = "SELECT " + COLUMN_WORKOUT_ID + ", " + COLUMN_DATETIME + ", " + COLUMN_REPS + " FROM " + TABLE_POOSH_LOG;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            try {
                if (cursor.moveToFirst()) {
                    do {
                        LoggedSet loggedSet = new LoggedSet();
                        loggedSet.setWorkoutId(cursor.getInt(0));
                        String dateTimeString = cursor.getString(1);
                        DateTime setDate = DateHelper.parse(dateTimeString);
                        loggedSet.setDateTime(setDate);
                        loggedSet.setReps(cursor.getInt(2));
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

        String selectQuery = "SELECT " + COLUMN_WORKOUT_ID + ", " + COLUMN_DATETIME + ", " + COLUMN_REPS + " FROM " + TABLE_POOSH_LOG
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
                        loggedSet.setWorkoutId(cursor.getInt(0));
                        String dateTimeString = cursor.getString(1);
                        DateTime setDate = DateHelper.parse(dateTimeString);
                        loggedSet.setDateTime(setDate);
                        loggedSet.setReps(cursor.getInt(2));
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

    public void dropAll() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM " + TABLE_POOSH_LOG);
        db.execSQL("VACUUM");

        db.execSQL("DELETE FROM " + TABLE_TARGETS);
        db.execSQL("VACUUM");
    }


}