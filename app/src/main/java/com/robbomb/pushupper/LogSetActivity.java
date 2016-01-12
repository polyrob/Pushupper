package com.robbomb.pushupper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.robbomb.pushupper.helper.DateHelper;
import com.robbomb.pushupper.helper.PushupDatabaseHelper;
import com.robbomb.pushupper.model.LoggedSet;

import org.joda.time.DateTime;

public class LogSetActivity extends Activity {
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
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(Constants.MAX_REP_SELECT);
        numberPicker.setWrapSelectorWheel(false);

        Intent i = getIntent();
        int lastReps = i.getExtras().getInt(Constants.LAST_REP);
        int doneToday = i.getExtras().getInt(Constants.DONE_TODAY);
        int repsRemaining = i.getExtras().getInt(Constants.REPS_REMAINING);


        TextView pushupsToday = (TextView) findViewById(R.id.pushupsToday);
        pushupsToday.setText(String.valueOf(doneToday));
        pushupsOwed.setText(String.valueOf(repsRemaining));

        numberPicker.setValue(getIntent().getExtras().getInt(Constants.LAST_REP));

            /* set progress bar */
//            Handler progressBarHandler = new Handler();
//            final int finalPushupsLoggedToday = pushupsLoggedToday;
//            progressBarHandler.post(new Runnable() {
//                public void run() {
//                    progressBar.setProgress(finalPushupsLoggedToday);
//                    progressBar.setMax(neededToday);
//                }
//            });


        Button buttonSubmit = (Button) findViewById(R.id.button_submit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                submitSet();
                                            }
                                        }
        );

        initializeFields();

    }

    private void submitSet() {
        LoggedSet set = new LoggedSet();
        int reps = numberPicker.getValue();
        set.setReps(reps);
        set.setDateTime(DateTime.now());

        try {
            long returnRecords = helper.insertPushupSet(set);
            Intent intentData = new Intent();
            intentData.putExtra(Constants.REPS, reps);
            setResult(Constants.LOG_SUCCESS, intentData);
        } catch (Exception e) {
            Log.e(TAG, "An error occurred adding the reps. " + e.getMessage());
            e.printStackTrace();
            setResult(Constants.LOG_FAIL);
        }

        finish();
    }


    private void initializeFields() {
        TextView headerDate = (TextView) findViewById(R.id.header_date);
        headerDate.setText(DateTime.now().toString(DateHelper.humanFormat));

    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
