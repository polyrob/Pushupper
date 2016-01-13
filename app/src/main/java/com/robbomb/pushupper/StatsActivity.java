package com.robbomb.pushupper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.robbomb.pushupper.model.AppData;

public class StatsActivity extends Activity {

    private static final String TAG = "StatsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        AppData data = (AppData) getIntent().getSerializableExtra(Constants.DATA);
    }
}

