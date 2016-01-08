package com.robbomb.pushupper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.robbomb.pushupper.helper.DateHelper;
import com.robbomb.pushupper.helper.PushupDatabaseHelper;
import com.robbomb.pushupper.model.PushupData;
import com.robbomb.pushupper.model.PushupSet;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";

    private PushupData data;
    private CaldroidFragment caldroidFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        setContentView(R.layout.activity_main);

        data = new PushupData();
        boolean hasProfile = initPrefs(data);
        if (hasProfile) {
            loadHistoricalData(data);
            updateStats(data);
            initCalendar(data);
            initLogSetButtons();
        } else {
            showFirstTimeDialog();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu()");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_clear_data:
                clearData();
                return true;
            case R.id.action_about:
                return true;
        }
        return false;
    }

    private void updateStats(PushupData data) {
        Log.i(TAG, "updateStats()");
        TextView stat_total = (TextView) findViewById(R.id.stat_total);
        TextView stat_today = (TextView) findViewById(R.id.stat_today);
        TextView stat_remaining = (TextView) findViewById(R.id.stat_remaining);

        List<PushupSet> history = data.getHistory();
        int allTime = 0;
        for (PushupSet set : history) {
            allTime += set.getReps();
        }
        stat_total.setText(String.valueOf(allTime));

        int neededToday = DateHelper.getDaysBetween(data.getDayOneDate(), DateTime.now()) + data.getDayOneReps();
        stat_today.setText(String.valueOf(neededToday));

        /* calc remaining */
        int todays = 0;
        for (PushupSet set : data.getTodays()) {
            todays += set.getReps();
        }
        data.setRepsToday(todays);
        data.setRepsRemaining(neededToday - todays);

        stat_remaining.setText(String.valueOf(data.getRepsRemaining()));
    }

    private void loadHistoricalData(PushupData data) {
        PushupDatabaseHelper helper = new PushupDatabaseHelper(getApplicationContext());
        List<PushupSet> allHistory = helper.getLoggedPushups();
        data.setHistory(allHistory);

        List<PushupSet> todays = helper.getPushupsForDate(DateTime.now());
        data.setTodays(todays);

        /* save the last reps to pre-populate NumberPicker */
        if (allHistory.size() > 0) {
            PushupSet lastSet = allHistory.get(allHistory.size() - 1);
            data.setLastReps(lastSet.getReps());
        }
    }

    private boolean initPrefs(PushupData data) {
        /* see if we have pref data - ie, already did initial setup */
        SharedPreferences prefs = getPreferences(0);
        int dayOneReps = prefs.getInt(Constants.TARGET_REPS, 0);
        if (dayOneReps > 0) {
            DateTime dayOneDate = DateHelper.parse(prefs.getString(Constants.TARGET_DAY, ""));
            data.setDayOneReps(dayOneReps);
            data.setDayOneDate(dayOneDate);
            return true;
        }
        return false;
    }


    public void initCalendar(PushupData data) {
        Log.i(TAG, "initCalendar() - " + Thread.currentThread().getName());
        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);
        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();

        t.replace(R.id.cal_container, caldroidFragment);
        t.commit();

        processCalendarGoals(caldroidFragment, data);

        caldroidFragment.setCaldroidListener(new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                Log.i(TAG, "Date clicked, " + date.toString());
            }
        });

        caldroidFragment.refreshView();
    }

    private void processCalendarGoals(CaldroidFragment caldroidFragment, PushupData data) {
        PushupDatabaseHelper helper = new PushupDatabaseHelper(getApplicationContext());
        DateTime startDate = data.getDayOneDate();

        int daysToProcess = DateHelper.getDaysBetween(startDate, DateTime.now());
        for (int i = 0; i <= daysToProcess; i++) {
            DateTime date = startDate.plusDays(i);

            /* check status for date */
            ArrayList<PushupSet> sets = helper.getPushupsForDate(date);
            if (sets.size() == 0) {
                caldroidFragment.setBackgroundResourceForDate(R.color.status_missed, date.toDate());
                continue;
            } else {
                int pushupsDone = 0;
                for (PushupSet set : sets) {
                    pushupsDone += set.getReps();
                }
                int neededToday = data.getDayOneReps() + i;
                if (neededToday - pushupsDone > 0) {
                    caldroidFragment.setBackgroundResourceForDate(R.color.status_under, date.toDate());
                    continue;
                }
                caldroidFragment.setBackgroundResourceForDate(R.color.status_met, date.toDate());
            }
        }
    }

    private void initLogSetButtons() {
        Button logSetButton = (Button) findViewById(R.id.btn_log_set);
        logSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logSetIntent = new Intent(MainActivity.this, LogSetActivity.class);
                logSetIntent.putExtra(Constants.LAST_REP, data.getLastReps());
                logSetIntent.putExtra(Constants.DONE_TODAY, data.getRepsToday());
                logSetIntent.putExtra(Constants.REPS_REMAINING, data.getRepsRemaining());
                startActivityForResult(logSetIntent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnData) {
        super.onActivityResult(requestCode, resultCode, returnData);
        Log.i(TAG, "onActivityResult, requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (Constants.LOG_SUCCESS == resultCode) {
            String toastText = "Added " + returnData.getExtras().getInt(Constants.REPS) + " reps";
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();

            loadHistoricalData(data);
            updateStats(data);

            Handler handler = new Handler(getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    processCalendarGoals(caldroidFragment, data);
                    caldroidFragment.refreshView();
                }
            });

        } else if (Constants.LOG_FAIL == resultCode) {
            String toastText = "An error occurred logging your reps.";
            Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method to present the "first time" dialog in order to determine the starting date and target reps
     */
    private void showFirstTimeDialog() {
        Log.i(TAG, "showFirstTimeDialog()");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Target!");

        LayoutInflater inflater = LayoutInflater.from(this);
        View firstTimeLayout = inflater.inflate(R.layout.first_time_layout, null);
        builder.setView(firstTimeLayout);
        final NumberPicker targetPicker = (NumberPicker) firstTimeLayout.findViewById(R.id.firstTimeNumberPicker);

        targetPicker.setMinValue(1);
        targetPicker.setMaxValue(500);
        targetPicker.setValue(40);
        targetPicker.setWrapSelectorWheel(false);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int dayOneReps = targetPicker.getValue();

                /* save preferences */
                SharedPreferences settings = getPreferences(0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(Constants.TARGET_DAY, DateHelper.format(DateTime.now()));
                editor.putInt(Constants.TARGET_REPS, dayOneReps);
                editor.commit();

                /* now we can proceed with doing inits */
                initPrefs(data);
                updateStats(data);
                initCalendar(data);
                initLogSetButtons();
            }
        });

        builder.show();


    }

    private void clearData() {
        Log.i(TAG, "clearData()");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wipe Data");
        builder.setMessage("Are you sure you want to clear all data and start over?");
        builder.setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PushupDatabaseHelper helper = new PushupDatabaseHelper(getApplicationContext());
                        helper.dropTable();
                        SharedPreferences settings = getPreferences(0);
                        settings.edit().clear().commit();
                        recycleActivity();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });
        builder.create().show();

    }

    private void recycleActivity() {
        // http://stackoverflow.com/questions/3053761/reload-activity-in-android
        finish();
        startActivity(getIntent());
    }
}
