package com.robbomb.pushupper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.robbomb.pushupper.helper.DateHelper;
import com.robbomb.pushupper.helper.PushupDatabaseHelper;
import com.robbomb.pushupper.model.AppData;
import com.robbomb.pushupper.model.LoggedSet;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String PREFERENCE = "POOSHIT";
    private static String TAG = "MainActivity";

    private AppData data;
    private PushupDatabaseHelper helper;
    private CaldroidFragment caldroidFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        setContentView(R.layout.main_activity);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        helper = new PushupDatabaseHelper(getApplicationContext());
        data = new AppData();

        initPrefs(data);
        loadActivity();
        if (data.isFirstTime()) showHelp(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void loadActivity() {
        loadHistoricalData(data);
        updateStats(data);
        initCalendar(data);
        initLogSetButtons();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_stats:
//                showStats();
//                return true;
            case R.id.action_clear_data:
                clearData();
                return true;
            case R.id.action_about:
                showAbout();
                return true;
        }
        return true;
    }


    private void updateStats(AppData data) {
        Log.i(TAG, "updateStats()");
        TextView stat_total = (TextView) findViewById(R.id.stat_total);
        TextView stat_today = (TextView) findViewById(R.id.stat_today);
        TextView stat_remaining = (TextView) findViewById(R.id.stat_remaining);

        List<LoggedSet> history = data.getHistory();
        int allTime = 0;
        for (LoggedSet set : history) {
            allTime += set.getReps();
        }
        stat_total.setText(String.valueOf(allTime));

        Log.i(TAG, data.getDayOneDate() + " - " + data.getDayOneReps());
        int neededToday = 0;
        if (data.getDayOneDate() != null) {
            neededToday = DateHelper.getDaysBetween(data.getDayOneDate(), DateTime.now()) + data.getDayOneReps();
        }
        stat_today.setText(String.valueOf(neededToday));

        /* calc remaining */
        int todays = 0;
        for (LoggedSet set : data.getTodays()) {
            todays += set.getReps();
        }
        data.setRepsToday(todays);
        data.setRepsRemaining(neededToday - todays);

        stat_remaining.setText(String.valueOf(data.getRepsRemaining()));
    }

    private void loadHistoricalData(AppData data) {
        List<LoggedSet> allHistory = helper.getLoggedPushups();
        data.setHistory(allHistory);

        List<LoggedSet> todays = helper.getPushupsForDate(DateTime.now());
        data.setTodays(todays);

        /* save the last reps to pre-populate NumberPicker */
        if (allHistory.size() > 0) {
            LoggedSet lastSet = allHistory.get(allHistory.size() - 1);
            data.setLastReps(lastSet.getReps());
        }
    }

    private void initPrefs(AppData data) {
        /* see if we have pref data - ie, already did initial setup */
        SharedPreferences prefs = getSharedPreferences(PREFERENCE, MODE_PRIVATE);
        data.setFirstTime(prefs.getBoolean(getString(R.string.first_time), Boolean.TRUE));
        Log.i(TAG, "first time value from initPrefs: " + data.isFirstTime());
        int dayOneReps = prefs.getInt(Constants.TARGET_REPS, 0);
        if (dayOneReps > 0) {
            DateTime dayOneDate = DateHelper.parse(prefs.getString(Constants.TARGET_DAY, ""));
            data.setDayOneReps(dayOneReps);
            data.setDayOneDate(dayOneDate);
        }
    }


    public void initCalendar(AppData data) {
        Log.i(TAG, "initCalendar() - " + Thread.currentThread().getName());
        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
//        args.putInt(CaldroidFragment.THEME_RESOURCE, R.style.CaldroidDefaultDark);
        caldroidFragment.setArguments(args);
        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();

        t.replace(R.id.cal_container, caldroidFragment);
        t.commit();

        processCalendarGoals(caldroidFragment, data);

        caldroidFragment.setCaldroidListener(new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                DateTime dateTime = new DateTime(date);
                ArrayList<LoggedSet> setList = helper.getPushupsForDate(dateTime);
                int sets = setList.size();
                int reps = 0;
                for (LoggedSet set : setList) {
                    reps += set.getReps();
                }
                StringBuilder sb = new StringBuilder(dateTime.toString("MMM d"));
                sb.append(": ").append(sets).append(" sets, ").append(reps).append(" reps");
                Toast.makeText(view.getContext(), sb.toString(), Toast.LENGTH_LONG).show();
//                Toast toast = new Toast(getApplicationContext());
//                toast.setText(sb.toString());
//                toast.setDuration(Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//                toast.show();
            }
        });

        caldroidFragment.refreshView();
    }

    private void processCalendarGoals(CaldroidFragment caldroidFragment, AppData data) {
        DateTime startDate = data.getDayOneDate() == null ? DateTime.now() : data.getDayOneDate();

        int daysToProcess = DateHelper.getDaysBetween(startDate, DateTime.now());
        for (int i = 0; i <= daysToProcess; i++) {
            DateTime date = startDate.plusDays(i);

            /* check status for date */
            ArrayList<LoggedSet> sets = helper.getPushupsForDate(date);
            if (sets.size() == 0) {
                caldroidFragment.setBackgroundResourceForDate(R.color.status_missed, date.toDate());
                continue;
            } else {
                int pushupsDone = 0;
                for (LoggedSet set : sets) {
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
                if (data.getDayOneDate() == null || data.getDayOneReps() == 0) {
                    showTargetNeededDialog();
                } else {
                    Intent logSetIntent = new Intent(MainActivity.this, LogSetActivity.class);
                    logSetIntent.putExtra(Constants.LAST_REP, data.getLastReps());
                    logSetIntent.putExtra(Constants.DONE_TODAY, data.getRepsToday());
                    logSetIntent.putExtra(Constants.REPS_REMAINING, data.getRepsRemaining());
                    startActivityForResult(logSetIntent, 0);
                }
            }
        });
    }

    private void showTargetNeededDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Target Needed!");
        builder.setMessage("Please use the navigation drawer at the left to choose a new target before logging your workouts.");
        builder.setPositiveButton("Got It!", new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
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
    private void showNewTargetDialog() {
        Log.i(TAG, "showNewTargetDialog()");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Target!");

        LayoutInflater inflater = LayoutInflater.from(this);
        View newTarget = inflater.inflate(R.layout.first_time_layout, null);
        builder.setView(newTarget);
        final NumberPicker targetPicker = (NumberPicker) newTarget.findViewById(R.id.firstTimeNumberPicker);

        targetPicker.setMinValue(10);
        targetPicker.setMaxValue(1000);
        targetPicker.setValue(40);
        targetPicker.setWrapSelectorWheel(false);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int dayOneReps = targetPicker.getValue();

                /* save preferences */
                SharedPreferences.Editor editor = getSharedPreferences(PREFERENCE, MODE_PRIVATE).edit();
                editor.putString(Constants.TARGET_DAY, DateHelper.format(DateTime.now()));
                editor.putInt(Constants.TARGET_REPS, dayOneReps);
                editor.commit();

                /* now we can proceed with doing inits */
                initPrefs(data);
                loadActivity();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    private void recycleActivity() {
        // http://stackoverflow.com/questions/3053761/reload-activity-in-android
        finish();
        startActivity(getIntent());
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_about) {
            showAbout();
        } else if (id == R.id.action_help) {
            showHelp(false);
//        } else if (id == R.id.action_stats) {
//            showStats();
        } else if (id == R.id.action_clear_data) {
            clearData();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showHelp(final boolean getTarget) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View helpLayout = inflater.inflate(R.layout.help_layout, null);

        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setView(helpLayout);
        db.setTitle("Welcome to Poosh It");
        db.setPositiveButton("Got It!", new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (getTarget) showNewTargetDialog();
                    }
                });
        AlertDialog dialog = db.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


        Log.i(TAG, "showHelp - setting first_time boolean to false in settings");
        getSharedPreferences(PREFERENCE, MODE_PRIVATE).edit()
                .putBoolean(getString(R.string.first_time), false).commit();
    }




    /* Menu Items */

//    private void showStats() {
//        Intent intent = new Intent(MainActivity.this, StatsActivity.class);
//        intent.putExtra(Constants.DATA, data);
//        startActivity(intent);
//    }

    private void clearData() {
        Log.i(TAG, "clearData()");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wipe Data");
        builder.setMessage("Are you sure you want to clear all data and start over?");
        builder.setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        helper.dropAll();
                        SharedPreferences.Editor prefs = getSharedPreferences(PREFERENCE, MODE_PRIVATE).edit();
                        prefs.remove(Constants.TARGET_DAY);
                        prefs.remove(Constants.TARGET_REPS);
                        prefs.commit();
                        data = new AppData();
                        loadActivity();
                        showNewTargetDialog();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    private void showAbout() {
        Log.i(TAG, "about()");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.about_desc);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.create().show();
    }

}
