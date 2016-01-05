package com.robbomb.pushupper;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.robbomb.pushupper.helper.DateHelper;
import com.robbomb.pushupper.helper.PushupDatabaseHelper;
import com.robbomb.pushupper.model.PushupSet;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int MAX_SET = 200;
    public static final String TARGET_REPS = "targetReps";
    public static final String TARGET_DAY = "targetDay";
    public DateTime now;
    private PushupDatabaseHelper helper;
    private NumberPicker numberPicker;
    private ProgressBar progressBar;
    private TextView pushupsOwed;
    private int dayOneReps;
    private DateTime dayOneDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        now = DateTime.now(); // let's just get this once and save it.
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        helper = new PushupDatabaseHelper(getApplicationContext());
        pushupsOwed = (TextView) findViewById(R.id.pushupsOwed);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(MAX_SET);
        numberPicker.setWrapSelectorWheel(false);


        /* see if we have pref data - ie, already did initial setup */
        SharedPreferences prefs = getPreferences(0);
        dayOneReps = prefs.getInt(TARGET_REPS, 0);
        if (dayOneReps > 0) {
            dayOneDate = DateHelper.parse(prefs.getString(TARGET_DAY, ""));
            Log.i("MainActivity", "found pref data: " + dayOneReps + ", " + dayOneDate.toString());
            /* get all of our pushups and do the calculations for today */
            List<PushupSet> pushups = helper.getPushupsForDate(DateTime.now());
            int pushupsLoggedToday = 0;
            for (PushupSet set : pushups) {
                pushupsLoggedToday += set.getReps();
            }
            Log.i("main activity", pushups.size() + ", " + pushupsLoggedToday);

            TextView pushupsToday = (TextView) findViewById(R.id.pushupsToday);
            pushupsToday.setText(String.valueOf(pushupsLoggedToday));

            final int neededToday = getPushupsNeededToday(dayOneDate, dayOneReps, now);
            int remaining = neededToday - pushupsLoggedToday;
            pushupsOwed.setText(String.valueOf(remaining));

            /* set the numberpicker to the smaller of the last set or the remaining pushups */
            int defaultReps = Math.min(pushups.get(pushups.size()-1).getReps(), remaining);
            numberPicker.setValue(defaultReps);

            /* set progress bar */
            Handler progressBarHandler = new Handler();
            final int finalPushupsLoggedToday = pushupsLoggedToday;
            progressBarHandler .post(new Runnable() {
                public void run() {
                    progressBar.setProgress(finalPushupsLoggedToday);
                    progressBar.setMax(neededToday);
                }
            });

            Log.i("progress bar", pushupsLoggedToday + ", " + progressBar.getProgress() + ", " + progressBar.getMax());

        } else {
            /* no preferences and thus not target created */
            showFirstTimeDialog();
        }


        Button buttonSubmit = (Button) findViewById(R.id.button_submit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushupSet set = new PushupSet();
                set.setReps(numberPicker.getValue());
                set.setDateTime(DateTime.now());

                helper.insertPushupSet(set);
                Toast toast = Toast.makeText(v.getContext(), "Pushup Set Added!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        Button buttonTest = (Button) findViewById(R.id.button_testdb);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DateTime yesterday = DateTime.now();
                List<PushupSet> pushups = helper.getPushupsForDate(yesterday);
                Toast toast = Toast.makeText(v.getContext(), "number of records: " + pushups.size(), Toast.LENGTH_LONG);
                toast.show();
            }
        });

        Button buttonTestYesterday = (Button) findViewById(R.id.button_test_yesterday);
        buttonTestYesterday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTime yesterday = DateTime.now().minusDays(1);
                List<PushupSet> pushups = helper.getPushupsForDate(yesterday);
                Toast toast = Toast.makeText(v.getContext(), "number of records: " + pushups.size(), Toast.LENGTH_LONG);
                toast.show();
            }
        });

        initializeFields();
    }

    protected int getPushupsNeededToday(DateTime dayOneDate, int dayOneReps, DateTime now) {
        int daysBetween = Days.daysBetween(now.withTimeAtStartOfDay(), dayOneDate.withTimeAtStartOfDay()).getDays();
        int calcDays = dayOneReps + daysBetween;
        return calcDays;
    }



    private void initializeFields() {
        TextView headerDate = (TextView) findViewById(R.id.header_date);
        headerDate.setText(DateTime.now().toString(DateHelper.humanFormat));

    }

    public void onSubmitPushups(View v) {
        PushupSet set = new PushupSet();
        set.setReps(numberPicker.getValue());
        set.setDateTime(DateTime.now());

        helper.insertPushupSet(set);
        Toast toast = Toast.makeText(this, "Pushup Set Added!", Toast.LENGTH_SHORT);
        toast.show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * Method to present the "first time" dialog in order to determine the starting date and target reps
     */
    private void showFirstTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Target!");

        LayoutInflater inflater = LayoutInflater.from(this);
        View firstTimeLayout = inflater.inflate(R.layout.first_time_layout, null);
        builder.setView(firstTimeLayout);
        final NumberPicker numberPicker = (NumberPicker) firstTimeLayout.findViewById(R.id.firstTimeNumberPicker);

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(500);
        numberPicker.setValue(40);
        numberPicker.setWrapSelectorWheel(false);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dayOneReps = numberPicker.getValue();
                pushupsOwed.setText(String.valueOf(dayOneReps));

                /* save preferences */
                SharedPreferences settings = getPreferences(0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(TARGET_DAY, DateHelper.format(DateTime.now()));
                editor.putInt(TARGET_REPS, dayOneReps);
                editor.commit();
            }
        });

        builder.show();
    }
}
