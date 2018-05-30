/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;



public class AvailableHoursActivity extends AppCompatActivity {

    TextView monthlyHoursTextView;
    TextView dailyHoursTextView;
    TextView hourlyHoursTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_hours);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Available Hours");

        monthlyHoursTextView = (TextView) findViewById(R.id.monthly_hours);
        dailyHoursTextView = (TextView) findViewById(R.id.daily_hours);
        hourlyHoursTextView = (TextView) findViewById(R.id.hourly_hours);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        monthlyHoursTextView.setText(bundle.getString("monthlyHours"));
        dailyHoursTextView.setText(bundle.getString("dailyHours"));
        hourlyHoursTextView.setText(bundle.getString("hourlyHours"));

    }

    // Up button pressed -> goes back to previous activity or fragment
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
