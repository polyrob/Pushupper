package com.robbomb.pushupper.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.robbomb.pushupper.model.LoggedSet;
import com.robbomb.pushupper.model.Target;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NewRob on 1/4/2016.
 */
public class PushupDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "PushupDatabaseHelper";

    private static final String DB_NAME = "pushups.sqlite";
    private static final int VERSION = 1;

    private static final String TABLE_POOSH_LOG = "poosh_log";
    private static final String COLUMN_DATETIME = "datetime";
    private static final String COLUMN_REPS = "reps";

    private static final String TABLE_TARGETS = "targets";
    private static final String COLUMN_TARGET_ID = "target_id";

    private static final String TABLE_WORKOUTS = "workouts";
    private static final String COLUMN_WORKOUT_ID = "workout_id";
    private static final String COLUMN_WORKOUT_TYPE = "type";


    public PushupDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the table if it doesn't exist
        db.execSQL("create table " + TABLE_POOSH_LOG
                + " (_id INTEGER primary key autoincrement, "
                + COLUMN_TARGET_ID + " INTEGER, "
                + COLUMN_DATETIME + " TEXT, "
                + COLUMN_REPS + " INTEGER)");

        db.execSQL("create table " + TABLE_TARGETS
                + " (" + COLUMN_TARGET_ID + " INTEGER primary key autoincrement, "
                + COLUMN_DATETIME + " TEXT, "
                + COLUMN_REPS + " INTEGER)");

        db.execSQL("create table " + TABLE_WORKOUTS
                + " (" + COLUMN_WORKOUT_ID + " INTEGER primary key autoincrement, "
                + COLUMN_WORKOUT_TYPE + " TEXT)");

        /* add default workout */
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WORKOUT_TYPE, "Default");
        db.insert(TABLE_WORKOUTS, null, cv);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // We need not worry about this guy now, or probably ever.
    }

    public long insertPushupSet(LoggedSet set) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TARGET_ID, set.getTargetId());
        cv.put(COLUMN_DATETIME, DateHelper.format(set.getDateTime()));
        cv.put(COLUMN_REPS, set.getReps());
        return getWritableDatabase().insert(TABLE_POOSH_LOG, null, cv);
    }

    public List<LoggedSet> getLoggedPushups() {
        ArrayList<LoggedSet> loggedSets = new ArrayList<>();

        String selectQuery = "SELECT " + COLUMN_TARGET_ID + ", " + COLUMN_DATETIME + ", " + COLUMN_REPS + " FROM " + TABLE_POOSH_LOG;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            try {
                if (cursor.moveToFirst()) {
                    do {
                        LoggedSet loggedSet = new LoggedSet();
                        loggedSet.setTargetId(cursor.getInt(0));
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

        String selectQuery = "SELECT " + COLUMN_TARGET_ID + ", " + COLUMN_DATETIME + ", " + COLUMN_REPS + " FROM " + TABLE_POOSH_LOG
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
                        loggedSet.setTargetId(cursor.getInt(0));
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


    /* TARGET OPERATIONS */
    public ArrayList<Target> getTargetsForWorkout(int workoutId) {
        ArrayList<Target> targets = new ArrayList<>();

        String selectQuery = "SELECT " + COLUMN_TARGET_ID + ", " + COLUMN_DATETIME + ", " + COLUMN_REPS + " FROM " + TABLE_TARGETS
                + " WHERE " + COLUMN_WORKOUT_ID + " = " + workoutId;

        Log.i("PushupDatabaseHelper", selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        try {

            Cursor cursor = db.rawQuery(selectQuery, null);
            try {

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        Target target = new Target();
                        target.setTargetId(cursor.getInt(0));
                        String dateTimeString = cursor.getString(1);
                        DateTime setDate = DateHelper.parse(dateTimeString);
                        target.setDateTime(setDate);
                        target.setReps(cursor.getInt(2));
                        targets.add(target);

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

        return targets;
    }

    public long insertTarget(Target target) {
        Log.i(TAG, "inserting new target, " + target);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WORKOUT_ID, target.getWorkoutId());
        cv.put(COLUMN_DATETIME, DateHelper.format(target.getDateTime()));
        cv.put(COLUMN_REPS, target.getReps());
        return getWritableDatabase().insert(TABLE_TARGETS, null, cv);
    }


    /* WORKOUT OPERATIONS */
    public long insertWorkout(String type) {
        Log.i(TAG, "inserting new workout, " + type);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_WORKOUT_TYPE, type);
        return getWritableDatabase().insert(TABLE_WORKOUTS, null, cv);
    }

    /* ERASE EVERYTHING! */
    public void dropAll() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM " + TABLE_POOSH_LOG);
        db.execSQL("VACUUM");

        db.execSQL("DELETE FROM " + TABLE_TARGETS);
        db.execSQL("VACUUM");

        db.execSQL("DELETE FROM " + TABLE_WORKOUTS);
        db.execSQL("VACUUM");
        insertWorkout("Default");
    }


}