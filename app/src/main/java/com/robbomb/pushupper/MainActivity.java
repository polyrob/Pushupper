package com.robbomb.pushupper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.robbomb.pushupper.helper.DateHelper;
import com.robbomb.pushupper.helper.PushupDatabaseHelper;
import com.robbomb.pushupper.model.AppData;
import com.robbomb.pushupper.model.LoggedSet;
import com.robbomb.pushupper.model.Target;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String PREFERENCE = "POOSHIT";
    private static String TAG = "MainActivity";

    private AppData data;
    private PushupDatabaseHelper helper;
    private CaldroidFragment caldroidFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
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


    private void initPrefs(AppData data) {
        Log.d(TAG, "initPrefs()");
        SharedPreferences prefs = getSharedPreferences(PREFERENCE, MODE_PRIVATE);
        data.setFirstTime(prefs.getBoolean(getString(R.string.first_time), Boolean.TRUE));
//        int dayOneReps = prefs.getInt(Constants.TARGET_REPS, 0);
//        if (dayOneReps > 0) {
//            DateTime dayOneDate = DateHelper.parse(prefs.getString(Constants.TARGET_DAY, ""));
//            data.setDayOneReps(dayOneReps);
//            data.setDayOneDate(dayOneDate);
//        }
    }


    private void loadHistoricalData(AppData data) {
        Log.d(TAG, "loadHistoricalData()");
        List<LoggedSet> allHistory = helper.getLoggedPushups();
        data.setHistory(allHistory);

        List<LoggedSet> todays = helper.getPushupsForDate(DateTime.now());
        data.setTodays(todays);

        /* save the last reps to pre-populate NumberPicker */
        if (allHistory.size() > 0) {
            LoggedSet lastSet = allHistory.get(allHistory.size() - 1);
            data.setLastReps(lastSet.getReps());
        }

        ArrayList<Target> targets = helper.getTargetsForWorkout(Constants.DEFAULT_WORKOUT_ID);
        if (targets.size() > 0) {
            data.setTargets(targets);
            Target lastTarget = targets.get(targets.size() - 1);
            data.setCurrentTargetId(lastTarget.getTargetId());
        }

        /* process targets */

    }

    private void updateStats(AppData data) {
        Log.d(TAG, "updateStats()");
        TextView stat_total = (TextView) findViewById(R.id.stat_total);
        TextView stat_today = (TextView) findViewById(R.id.stat_today);
        TextView stat_remaining = (TextView) findViewById(R.id.stat_remaining);

        List<LoggedSet> history = data.getHistory();
        int allTime = 0;
        for (LoggedSet set : history) {
            allTime += set.getReps();
        }
        stat_total.setText(String.valueOf(allTime));

        int neededToday = 0;
        if (data.getTargets() != null) {
            Target currentTarget = data.getCurrentTarget();
            neededToday = DateHelper.getDaysBetween(currentTarget.getDateTime(), DateTime.now()) + currentTarget.getReps();
        }
        stat_today.setText(String.valueOf(neededToday));

        /* calc remaining */
        int todays = 0;
        for (LoggedSet set : data.getTodays()) {
            todays += set.getReps();
        }
        data.setRepsToday(todays);
        data.setRepsRemaining(neededToday - todays);
        if (data.getRepsRemaining() > 0) {
            stat_remaining.setTextColor(ContextCompat.getColor(this, R.color.status_under));
        } else {
            stat_remaining.setTextColor(stat_remaining.getTextColors().getDefaultColor());
        }

        stat_remaining.setText(String.valueOf(data.getRepsRemaining()));
    }


    public void initCalendar(AppData data) {
        Log.d(TAG, "initCalendar() - " + Thread.currentThread().getName());
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
                Toast.makeText(view.getContext(), sb.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        caldroidFragment.refreshView();
    }

    private void processCalendarGoals(CaldroidFragment caldroidFragment, AppData data) {
        DateTime startDate = data.getFirstTarget() == null ? DateTime.now() : data.getFirstTarget().getDateTime();
        startDate = startDate.withTimeAtStartOfDay();
        int daysToProcess = DateHelper.getDaysBetween(startDate, DateTime.now());

        Target currentTarget = null;
        Iterator<Target> targetIterator = null;

        if (data.getTargets() != null && data.getTargets().size() > 0) {
            targetIterator = data.getTargets().iterator();
            currentTarget = targetIterator.next();
        }
        Target nextTarget = null;

        if (data.getTargets() != null && data.getTargets().size() > 1) {
            nextTarget = targetIterator.next();
        }

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

                while (nextTarget != null && (nextTarget.getDateTime().withTimeAtStartOfDay().isEqual(date) || nextTarget.getDateTime().withTimeAtStartOfDay().isBefore(date)) ) {
                    /* the next target is in play */
                    currentTarget = nextTarget;
                    if (targetIterator.hasNext()) {
                        nextTarget = targetIterator.next();
                    } else {
                        nextTarget = null;
                    }
                }

                int daysFromCurrentTarget = DateHelper.getDaysBetween(currentTarget.getDateTime(), date);
                int repsNeeded = currentTarget.getReps() + daysFromCurrentTarget;
                if (repsNeeded - pushupsDone > 0) {
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
                if (data.getCurrentTarget() == null) {
                    showTargetNeededDialog();
                } else {
                    Intent logSetIntent = new Intent(MainActivity.this, LogSetActivity.class);
                    logSetIntent.putExtra(Constants.LAST_REP, data.getLastReps());
                    logSetIntent.putExtra(Constants.DONE_TODAY, data.getRepsToday());
                    logSetIntent.putExtra(Constants.REPS_REMAINING, data.getRepsRemaining());
                    logSetIntent.putExtra(Constants.TARGET, data.getCurrentTargetId());
                    startActivityForResult(logSetIntent, 0);
                }
            }
        });
    }

    private void showTargetNeededDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.target_needed_title));
        builder.setMessage(getString(R.string.target_needed_msg));
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
        Log.d(TAG, "onActivityResult, requestCode: " + requestCode + ", resultCode: " + resultCode);
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
        Log.d(TAG, "showNewTargetDialog()");
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
                int reps = targetPicker.getValue();

                Target target = new Target();
                target.setWorkoutId(Constants.DEFAULT_WORKOUT_ID);
                target.setDateTime(DateTime.now());
                target.setReps(reps);

                helper.insertTarget(target);

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
        Log.d(TAG, "onNavigationItemSelected(), " + item.getItemId());
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_about) {
            showAbout();
        } else if (id == R.id.action_help) {
            showHelp(false);
        } else if (id == R.id.action_change_target) {
            changeTarget();
        } else if (id == R.id.action_clear_data) {
            clearData();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Menu Items
     */

    private void showHelp(final boolean getTarget) {
        Log.d(TAG, "showHelp()");
        LayoutInflater inflater = LayoutInflater.from(this);
        View helpLayout = inflater.inflate(R.layout.help_layout, null);

        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setView(helpLayout);
        db.setTitle(getString(R.string.help_dialog_title));
        db.setPositiveButton(getString(R.string.help_dialog_okay), new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (getTarget) showNewTargetDialog();
                    }
                });
        AlertDialog dialog = db.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        getSharedPreferences(PREFERENCE, MODE_PRIVATE).edit()
                .putBoolean(getString(R.string.first_time), false).commit();
    }


    private void changeTarget() {
        Log.d(TAG, "changeTarget()");
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
                int reps = targetPicker.getValue();

                Target target = new Target();
                target.setWorkoutId(Constants.DEFAULT_WORKOUT_ID);
                target.setDateTime(DateTime.now());
                target.setReps(reps);

                helper.insertTarget(target);

                initPrefs(data);
                loadActivity();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void clearData() {
        Log.d(TAG, "clearData()");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.clear_dialog_title));
        builder.setMessage(getString(R.string.clear_dialog_message));
        builder.setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
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
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    private void showAbout() {
        Log.d(TAG, "about()");
        LayoutInflater inflater = LayoutInflater.from(this);
        View helpLayout = inflater.inflate(R.layout.about_layout, null);

        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setView(helpLayout);
        db.setTitle(getString(R.string.about_dialog_title));
        db.setPositiveButton(getString(R.string.about_dialog_okay), new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = db.create();
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

}
