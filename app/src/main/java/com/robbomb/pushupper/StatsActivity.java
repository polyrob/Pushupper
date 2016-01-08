package com.robbomb.pushupper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.robbomb.pushupper.model.PushupData;

public class StatsActivity extends AppCompatActivity {

    private static final String TAG = "StatsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        PushupData data = (PushupData) getIntent().getSerializableExtra(Constants.DATA);

        Log.i(TAG, data.getDayOneDate().toString());
    }
}
