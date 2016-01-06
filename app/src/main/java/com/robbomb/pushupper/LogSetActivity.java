package com.robbomb.pushupper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.robbomb.pushupper.helper.DateHelper;
import com.robbomb.pushupper.helper.PushupDatabaseHelper;
import com.robbomb.pushupper.model.PushupSet;

import org.joda.time.DateTime;

import java.util.List;

public class LogSetActivity extends AppCompatActivity {
    private static final String TAG = "LogSetActivity";

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
        setContentView(R.layout.activity_log_set);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });


        helper = new PushupDatabaseHelper(getApplicationContext());
        pushupsOwed = (TextView) findViewById(R.id.pushupsOwed);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(Constants.MAX_REP_SELECT);
        numberPicker.setWrapSelectorWheel(false);


        /* see if we have pref data - ie, already did initial setup */
        SharedPreferences prefs = getPreferences(0);
        dayOneReps = prefs.getInt(Constants.TARGET_REPS, 0);
        if (dayOneReps > 0) {
            dayOneDate = DateHelper.parse(prefs.getString(Constants.TARGET_DAY, ""));
            Log.i("MainActivity", "found pref data: " + dayOneReps + ", " + dayOneDate.toString());
            /* get all of our pushups and do the calculations for today */
            List<PushupSet> pushups = helper.getPushupsForDate(DateTime.now());
            int pushupsLoggedToday = 0;
            for (PushupSet set : pushups) {
                pushupsLoggedToday += set.getReps();
            }

            TextView pushupsToday = (TextView) findViewById(R.id.pushupsToday);
            pushupsToday.setText(String.valueOf(pushupsLoggedToday));

            /* set the numberpicker to the smaller of the last set or the remaining pushups */
            int lastSet = pushups.size() > 0 ? pushups.get(pushups.size() - 1).getReps() : 0;
            numberPicker.setValue(lastSet);

            /* set progress bar */
//            Handler progressBarHandler = new Handler();
//            final int finalPushupsLoggedToday = pushupsLoggedToday;
//            progressBarHandler.post(new Runnable() {
//                public void run() {
//                    progressBar.setProgress(finalPushupsLoggedToday);
//                    progressBar.setMax(neededToday);
//                }
//            });


        } else {
            /* no preferences and thus not target created */

        }

        Button buttonSubmit = (Button) findViewById(R.id.button_submit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitSet();
            }

        });

        initializeFields();
    }

    private void submitSet() {
        PushupSet set = new PushupSet();
        int reps = numberPicker.getValue();
        set.setReps(reps);
        set.setDateTime(DateTime.now());

        try {
            long returnRecords = helper.insertPushupSet(set);
            Log.i(TAG, "set submitted, return from helper: " + returnRecords);

            Intent intentData = new Intent();
            intentData.putExtra(Constants.REPS, reps);
            setResult(Constants.LOG_SUCCESS, intentData);
        } catch (Exception e) {
            e.printStackTrace();
            setResult(Constants.LOG_FAIL);
        }

        finish();
    }


    private void initializeFields() {
        TextView headerDate = (TextView) findViewById(R.id.header_date);
        headerDate.setText(DateTime.now().toString(DateHelper.humanFormat));

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
}
