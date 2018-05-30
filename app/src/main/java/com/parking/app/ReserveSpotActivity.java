/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import com.braintreepayments.api.dropin.DropInActivity;
//import com.braintreepayments.api.dropin.DropInRequest;
//import com.braintreepayments.api.dropin.DropInResult;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReserveSpotActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    String ipAddress;
    String sessionid;
    String spotID;
    ParkingSpot parkingSpot;
    String filterType;

    TextView imageNumberTextView;
    TextView spotAddressTextView;
    TextView spotDetailsTextView;
    TextView spotHoursTextView;
    Button additionalHoursButton;
    Spinner reserveTypeSpinner;
    TextView monthlyStartTextView;
    TextView startDateEditText;
    TextView endDateEditText;
    Spinner hourlyStartTimeSpinner;
    Spinner hourlyEndTimeSpinner;
    Button requestReservationButton;
    TextView totalPriceTextView;
    TextView agreementsTextView;

    LinearLayout monthlyReservationLinearLayout;
    LinearLayout monthlyReservationRecurringLinearLayout;
    CheckBox monthlyRecurringCheckBox;
    TextView monthlyRecurringTextView;
    LinearLayout dailyStartReservationLinearLayout;
    LinearLayout dailyEndReservationLinearLayout;
    LinearLayout hourlyStartReservationLinearLayout;
    LinearLayout hourlyEndReservationLinearLayout;

    String reservationTypes[];
    ArrayAdapter<CharSequence> spinnerAdapter;

    String datePicker;
    String timePicker;

    CaldroidFragment caldroidFragment;

    List<Reservation> reservations = new ArrayList<Reservation>();
    ArrayList<Date> unavailableDates = new ArrayList<Date>();
    ArrayList<Date> unavailableMonthlyDates = new ArrayList<Date>();
    ArrayList<Date> unavailableDailyDates = new ArrayList<Date>();
    ArrayList<Date> unavailableHourlyDates = new ArrayList<Date>();
    ArrayList<Date> selectedDates = new ArrayList<Date>();
    List<Bitmap> bitmaps = new ArrayList<>();

    private ViewPager viewPager;
    private CustomSwipeAdapter pagerAdapter;
    Integer imagePosition = 0;

    Date minimumMonthlyDate = new Date();
    Date maxAvailableDate = null;

    Date startDailyDate;
    Date endDailyDate;
    Boolean originalReservationTypeSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
            Set the content view
            Create and enable the toolbar
            Enable the back button
         */
        setContentView(R.layout.activity_reserve_spot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
            Set title of screen
         */
        setTitle("Reserve Spot");

        /*
            Initialize layout
         */
        imageNumberTextView = (TextView) findViewById(R.id.image_number);
        spotAddressTextView = (TextView) findViewById(R.id.spot_address);
        spotDetailsTextView = (TextView) findViewById(R.id.spot_details);
        spotHoursTextView = (TextView) findViewById(R.id.spot_hours);
        additionalHoursButton = (Button) findViewById(R.id.additional_hours_button);
        reserveTypeSpinner = (Spinner) findViewById(R.id.reservation_type);
        monthlyStartTextView = (TextView) findViewById(R.id.monthly_start_date);
        startDateEditText = (TextView) findViewById(R.id.start_date);
        endDateEditText = (TextView) findViewById(R.id.end_date);
        hourlyStartTimeSpinner= (Spinner) findViewById(R.id.hourly_start_time);
        hourlyEndTimeSpinner = (Spinner) findViewById(R.id.hourly_end_time);
        requestReservationButton = (Button) findViewById(R.id.request_reservation_button);
        totalPriceTextView = (TextView) findViewById(R.id.total_price);
        monthlyReservationLinearLayout = (LinearLayout) findViewById(R.id.reservation_monthly_linear_layout);
        monthlyReservationRecurringLinearLayout = (LinearLayout) findViewById(R.id.reservation_monthly_recurring_layout);
        monthlyRecurringCheckBox = (CheckBox) findViewById(R.id.recurring_checkbox);
        monthlyRecurringTextView = (TextView) findViewById(R.id.recurring_text_view);
        dailyStartReservationLinearLayout = (LinearLayout) findViewById(R.id.reservation_daily_start_linear_layout);
        dailyEndReservationLinearLayout = (LinearLayout) findViewById(R.id.reservation_daily_end_linear_layout);
        hourlyStartReservationLinearLayout = (LinearLayout) findViewById(R.id.reservation_hourly_start_linear_layout);
        hourlyEndReservationLinearLayout = (LinearLayout) findViewById(R.id.reservation_hourly_end_linear_layout);
        agreementsTextView = (TextView) findViewById(R.id.agreements);
        agreementsTextView.setMovementMethod(LinkMovementMethod.getInstance());

        /*
            Initialize a date format to be used in the rest of the OnCreate method
         */
        final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        /*
            Initialize the Caldroid Fragment (Calendar), set listeners, and push the fragment onto the layout
         */
        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, false);
        caldroidFragment.setArguments(args);
        caldroidFragment.setCaldroidListener(new CaldroidListener() {

            @Override
            public void onChangeMonth(int month, int year) {
                super.onChangeMonth(month, year);

                caldroidFragment.clearDisableDates();
                caldroidFragment.refreshView();

                addUnavailableWeekdaysToUnavailableDatesArray(month, year);
            }

            /*
                            Handle date selection
                         */
            @Override
            public void onSelectDate(Date date, View view) {
                Calendar c = Calendar.getInstance();

                /*
                    Date selection handling will vary based on what ReservationType is selected
                 */
                switch (reserveTypeSpinner.getSelectedItem().toString()) {
                    /*
                        If monthly ReservationType is selected, check if the startDailyDate is null. If it's not, make it null so that is is ready to be re-used.
                        If the maxAvailableDate is not null, then monthly reservations are not available. -> Set the monthlyStartText to "Monthly Unavailable", otherwise initialize the
                            monthlyStartTextView TextView to show the date selected.
                     */
                    case "Monthly":
                        c.setTime(date);
                        if (startDailyDate != null) {
                            caldroidFragment.setBackgroundDrawableForDate(getResources().getDrawable(R.color.colorWhite), startDailyDate);
                            startDailyDate = null;
                            selectedDates.clear();
                        }
                        if (endDailyDate != null) {
                            caldroidFragment.setBackgroundDrawableForDate(getResources().getDrawable(R.color.colorWhite), endDailyDate);
                            endDailyDate = null;
                            selectedDates.clear();
                        }

                        if (maxAvailableDate != null) {
                            monthlyStartTextView.setText("Monthly Unavailable");
                        } else {
                            monthlyStartTextView.setText(pad(String.valueOf((c.get(Calendar.MONTH) + 1))) + "/" + pad(String.valueOf(c.get(Calendar.DAY_OF_MONTH))) + "/" + c.get(Calendar.YEAR));
                        }
                        startDailyDate = c.getTime();
                        selectedDates.add(startDailyDate);
                        caldroidFragment.setBackgroundDrawableForDate(getResources().getDrawable(R.color.colorPrimary), startDailyDate);
                        caldroidFragment.refreshView();
                        updateMonthlyRecurring();
                        updateTotal();
                        break;
                    case "Daily":
                        c.setTime(date);
                        if (startDailyDate == null) {
                            startDailyDate = c.getTime();
                            caldroidFragment.setBackgroundDrawableForDate(getResources().getDrawable(R.color.colorPrimary), startDailyDate);
                            startDateEditText.setText(pad(String.valueOf((c.get(Calendar.MONTH) + 1))) + "/" + pad(String.valueOf(c.get(Calendar.DAY_OF_MONTH))) + "/" + c.get(Calendar.YEAR));
                            caldroidFragment.refreshView();
                            selectedDates.add(startDailyDate);
                            updateTotal();
                        } else if (endDailyDate == null) {
                            endDailyDate = c.getTime();

                            if (endDailyDate.getTime() < startDailyDate.getTime()) {
                                endDailyDate = null;
                                displayMessage("End date must be after start date");
                                break;
                            }
                            caldroidFragment.setBackgroundDrawableForDate(getResources().getDrawable(R.color.colorPrimary), endDailyDate);
                            endDateEditText.setText(pad(String.valueOf((c.get(Calendar.MONTH) + 1))) + "/" + pad(String.valueOf(c.get(Calendar.DAY_OF_MONTH))) + "/" + c.get(Calendar.YEAR));
                            try {
                                Date startDailyDate = sdf.parse(startDateEditText.getText().toString());

                                Date endDailyDate;
                                Calendar endDateCalendar = Calendar.getInstance();
                                endDateCalendar.setTime(date);
                                endDailyDate = endDateCalendar.getTime();

                                Calendar selectDatesCalendar = Calendar.getInstance();
                                selectDatesCalendar.setTime(startDailyDate);
                                startDailyDate = selectDatesCalendar.getTime();

                                while(startDailyDate.getTime() <= endDailyDate.getTime()) {
                                    caldroidFragment.setBackgroundDrawableForDate(getResources().getDrawable(R.color.colorPrimary), startDailyDate);
                                    selectedDates.add(startDailyDate);
                                    selectDatesCalendar.add(Calendar.DATE, 1);
                                    startDailyDate = selectDatesCalendar.getTime();
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            caldroidFragment.refreshView();
                            updateTotal();
                        } else {

                            selectedDates.clear();
                            try {
                                Date startDailyDate = sdf.parse(startDateEditText.getText().toString());
                                Date endDailyDate = sdf.parse(endDateEditText.getText().toString());
                                Calendar dailyCalendar = Calendar.getInstance();
                                dailyCalendar.setTime(startDailyDate);

                                while (startDailyDate.getTime() <= endDailyDate.getTime()) {
                                    caldroidFragment.setBackgroundDrawableForDate(getResources().getDrawable(R.color.colorWhite), startDailyDate);
                                    dailyCalendar.add(Calendar.DATE, 1);
                                    startDailyDate = dailyCalendar.getTime();
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            caldroidFragment.refreshView();
                            startDailyDate = null;
                            endDailyDate = null;
                            startDateEditText.setText("");
                            endDateEditText.setText("");
                            //startDailyDate = c.getTime();
                            //caldroidFragment.setBackgroundDrawableForDate(getResources().getDrawable(R.color.colorPrimary), startDailyDate);
                            //startDateEditText.setText(pad(String.valueOf((c.get(Calendar.MONTH) + 1))) + "/" + pad(String.valueOf(c.get(Calendar.DAY_OF_MONTH))) + "/" + c.get(Calendar.YEAR));
                        }
                        updateTotal();
                        break;
                    case "Hourly":
                        updateTotal();
                        break;
                    default:
                        break;
                }
            }
        });
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar_view, caldroidFragment);
        t.commit();
        //caldroidFragment.setDisableDates(unavailableDates);

        reserveTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Date date = new Date();
                Calendar cr = Calendar.getInstance();
                cr.setTime(date);
                date = cr.getTime();

                if (endDailyDate != null) {
                    try {
                        Date startDailyDate = sdf.parse(startDateEditText.getText().toString());
                        Date endDailyDate = sdf.parse(endDateEditText.getText().toString());
                        Calendar dailyCalendar = Calendar.getInstance();
                        dailyCalendar.setTime(startDailyDate);

                        while (startDailyDate.getTime() <= endDailyDate.getTime()) {
                            caldroidFragment.clearBackgroundDrawableForDate(startDailyDate);
                            dailyCalendar.add(Calendar.DATE, 1);
                            startDailyDate = dailyCalendar.getTime();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    caldroidFragment.refreshView();
                }

                if (startDailyDate != null) {
                    caldroidFragment.clearBackgroundDrawableForDate(startDailyDate);
                    startDailyDate = null;
                }

                caldroidFragment.clearSelectedDates();
                caldroidFragment.clearBackgroundDrawableForDate(date);
                caldroidFragment.refreshView();

                String price = "$0.00";
                totalPriceTextView.setText(price);
                monthlyStartTextView.setText("");
                startDateEditText.setText("");
                endDateEditText.setText("");

                int month = caldroidFragment.getMonth();
                int year = caldroidFragment.getYear();

                addUnavailableWeekdaysToUnavailableDatesArray(month, year);

                switch (parent.getItemAtPosition(position).toString()) {
                    case "Monthly":
                        if (originalReservationTypeSelected) {
                            displayMessage("Check above for monthly reservation hours.");
                        }
                        originalReservationTypeSelected = true;
                        if (maxAvailableDate != null) {
                            monthlyStartTextView.setText("Monthly Unavailable");
                        }
                        dailyStartReservationLinearLayout.setVisibility(View.GONE);
                        dailyEndReservationLinearLayout.setVisibility(View.GONE);
                        hourlyStartReservationLinearLayout.setVisibility(View.GONE);
                        hourlyEndReservationLinearLayout.setVisibility(View.GONE);
                        monthlyReservationLinearLayout.setVisibility(View.VISIBLE);
                        monthlyReservationRecurringLinearLayout.setVisibility(View.VISIBLE);
                        spotHoursTextView.setText("Monthly Hours \n\n" + generateHoursStringFromJSON(parkingSpot.getMonthlyHours()));
                        return;
                    case "Daily":
                        if (originalReservationTypeSelected) {
                            displayMessage("Check above for daily reservation hours.");
                        }
                        originalReservationTypeSelected = true;
                        monthlyReservationLinearLayout.setVisibility(View.GONE);
                        monthlyReservationRecurringLinearLayout.setVisibility(View.GONE);
                        hourlyStartReservationLinearLayout.setVisibility(View.GONE);
                        hourlyEndReservationLinearLayout.setVisibility(View.GONE);
                        dailyStartReservationLinearLayout.setVisibility(View.VISIBLE);
                        dailyEndReservationLinearLayout.setVisibility(View.VISIBLE);
                        spotHoursTextView.setText("Daily Hours \n\n" + generateHoursStringFromJSON(parkingSpot.getDailyHours()));
                        return;
                    case "Hourly":
                        if (originalReservationTypeSelected) {
                            displayMessage("Check above for hourly reservation hours.");
                        }
                        originalReservationTypeSelected = true;
                        caldroidFragment.setSelectedDate(date);
                        caldroidFragment.setBackgroundDrawableForDate(getResources().getDrawable(R.color.colorPrimary), date);
                        caldroidFragment.refreshView();
                        monthlyReservationLinearLayout.setVisibility(View.GONE);
                        monthlyReservationRecurringLinearLayout.setVisibility(View.GONE);
                        dailyStartReservationLinearLayout.setVisibility(View.GONE);
                        dailyEndReservationLinearLayout.setVisibility(View.GONE);
                        hourlyStartReservationLinearLayout.setVisibility(View.VISIBLE);
                        hourlyEndReservationLinearLayout.setVisibility(View.VISIBLE);
                        spotHoursTextView.setText("Hourly Hours \n\n" + generateHoursStringFromJSON(parkingSpot.getHourlyHours()));
                        return;
                    default:
                        return;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker = "start";
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getSupportFragmentManager(), "date_picker");
            }
        });

        endDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker = "end";
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getSupportFragmentManager(), "date_picker");
            }
        });

        monthlyStartTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker = "monthly_start";
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getSupportFragmentManager(), "date_picker");
            }
        });

        monthlyRecurringCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateMonthlyRecurring();
            }
        });

        hourlyStartTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTotal();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        hourlyEndTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTotal();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        additionalHoursButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putString("monthlyHours", generateHoursStringFromJSON( (parkingSpot.getAvailableMonthly() == 1) ? parkingSpot.getMonthlyHours() : "" ));
                bundle.putString("dailyHours", generateHoursStringFromJSON( (parkingSpot.getAvailableDaily() == 1) ? parkingSpot.getDailyHours() : "" ));
                bundle.putString("hourlyHours", generateHoursStringFromJSON( (parkingSpot.getAvailableHourly() == 1) ? parkingSpot.getHourlyHours() : "" ));

                Intent intent = new Intent(ReserveSpotActivity.this, AvailableHoursActivity.class);
                intent.putExtra("bundle", bundle);
                startActivity(intent);
            }
        });

        requestReservationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestReservation();
            }
        });

        //totalPriceTextView.setText("150.00");

        // Initialize View Pager and Pager Adapter
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        pagerAdapter = new CustomSwipeAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                imagePosition = position;

                Integer pos = position;
                Integer count;
                count = pagerAdapter.getCount();

                String imageNumbers = (position + 1) + " of " + count;
                imageNumberTextView.setText(imageNumbers);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        spotID = bundle.getString("spotID");
        sessionid = bundle.getString("sessionid");
        ipAddress = bundle.getString("ipAddress");
        filterType = bundle.getString("filterType");

        loadSpotDetails();
    }

    public void addUnavailableWeekdaysToUnavailableDatesArray(int month, int year) {

        if (parkingSpot == null) {
            return;
        }
        if (reserveTypeSpinner.getSelectedItem() == null) {
            return;
        }
        String jsonMonthly = parkingSpot.getMonthlyHours().replace("&quot;", "\"");
        String jsonDaily = parkingSpot.getDailyHours().replace("&quot;", "\"");
        String jsonHourly = parkingSpot.getHourlyHours().replace("&quot;", "\"");
        JSONObject jsonMonthlyObject = null;
        JSONObject jsonDailyObject = null;
        JSONObject jsonHourlyObject = null;

        Calendar c = Calendar.getInstance();
        Date todayDate = new Date();
        c.setTime(todayDate);
        String todaysDate = pad(c.get(Calendar.MONTH) + 1) + "/" + pad(c.get(Calendar.DAY_OF_MONTH)) + "/" + c.get(Calendar.YEAR);

        try {
            jsonMonthlyObject = new JSONObject(jsonMonthly);
            jsonDailyObject = new JSONObject(jsonDaily);
            jsonHourlyObject = new JSONObject(jsonHourly);


            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                Date today = sdf.parse(todaysDate);

                Date firstDayOfMonth = sdf.parse(pad(month)+"/01/"+year);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(firstDayOfMonth);
                Integer weekday = calendar.get(Calendar.DAY_OF_WEEK);
                int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.add(Calendar.DAY_OF_MONTH, -(weekday - 1));
                firstDayOfMonth = calendar.getTime();
                Date lastDayOfMonth = sdf.parse(pad(month)+"/"+pad(lastDay)+"/"+year);
                calendar.setTime(lastDayOfMonth);
                weekday = calendar.get(Calendar.DAY_OF_WEEK);
                calendar.add(Calendar.DAY_OF_MONTH, 11);
                lastDayOfMonth = calendar.getTime();

                calendar.setTime(firstDayOfMonth);

                while (firstDayOfMonth.getTime() <= lastDayOfMonth.getTime()) {

                    Integer dayOfWeek = firstDayOfMonth.getDay();
                    String day = "";
                    switch (dayOfWeek) {
                        case 0:
                            day = "sunday";
                            break;
                        case 1:
                            day = "monday";
                            break;
                        case 2:
                            day = "tuesday";
                            break;
                        case 3:
                            day = "wednesday";
                            break;
                        case 4:
                            day = "thursday";
                            break;
                        case 5:
                            day = "friday";
                            break;
                        case 6:
                            day = "saturday";
                            break;
                    }

                    switch (reserveTypeSpinner.getSelectedItem().toString()) {
                        case "Monthly":
                            String todaySwitchMonthly = jsonMonthlyObject.getString(day + "Switch");
                            if (!todaySwitchMonthly.equals("true")) {
                                if (!unavailableMonthlyDates.contains(firstDayOfMonth)) {
                                    unavailableMonthlyDates.add(firstDayOfMonth);
                                }
                            }
                            if (firstDayOfMonth.getTime() == today.getTime()) {
                                String availabilityHourFrom = jsonMonthlyObject.getString(day + "From");
                                String availabilityHourTo = jsonMonthlyObject.getString(day + "To");

                                if (availabilityHourFrom == null) {
                                    availabilityHourFrom = "12:00 AM";
                                }
                                if (availabilityHourTo == null) {
                                    availabilityHourTo = "11:59 PM";
                                }
                                int hour = c.get(Calendar.HOUR_OF_DAY);
                                int minute = c.get(Calendar.MINUTE);

                                Integer currentTime = Integer.valueOf(pad(hour) + "" + pad(minute));
                                Integer integerFrom = Integer.valueOf(convert12to24(availabilityHourFrom).replace("/", ""));
                                Integer integerTo = Integer.valueOf(convert12to24(availabilityHourTo).replace("/", ""));

                                if (currentTime < integerFrom || currentTime > integerTo) {
                                    if (!unavailableMonthlyDates.contains(firstDayOfMonth)) {
                                        unavailableMonthlyDates.add(firstDayOfMonth);
                                    }
                                }
                            }
                            if (!unavailableMonthlyDates.contains(firstDayOfMonth) && !selectedDates.contains(firstDayOfMonth)) {
                                caldroidFragment.clearBackgroundDrawableForDate(firstDayOfMonth);
                            }
                            caldroidFragment.setDisableDates(unavailableMonthlyDates);
                            break;
                        case "Daily":
                            String todaySwitchDaily = jsonDailyObject.getString(day + "Switch");
                            if (!todaySwitchDaily.equals("true")) {
                                if (!unavailableDailyDates.contains(firstDayOfMonth)) {
                                    unavailableDailyDates.add(firstDayOfMonth);
                                }
                            }
                            if (firstDayOfMonth.getTime() == today.getTime()) {
                                String availabilityHourFrom = jsonDailyObject.getString(day + "From");
                                String availabilityHourTo = jsonDailyObject.getString(day + "To");

                                if (availabilityHourFrom == null) {
                                    availabilityHourFrom = "12:00 AM";
                                }
                                if (availabilityHourTo == null) {
                                    availabilityHourTo = "11:59 PM";
                                }
                                int hour = c.get(Calendar.HOUR_OF_DAY);
                                int minute = c.get(Calendar.MINUTE);

                                Integer currentTime = Integer.valueOf(pad(hour) + "" + pad(minute));
                                Integer integerFrom = Integer.valueOf(convert12to24(availabilityHourFrom).replace("/", ""));
                                Integer integerTo = Integer.valueOf(convert12to24(availabilityHourTo).replace("/", ""));

                                if (currentTime < integerFrom || currentTime > integerTo) {
                                    if (!unavailableDailyDates.contains(firstDayOfMonth)) {
                                        unavailableDailyDates.add(firstDayOfMonth);
                                    }
                                }
                            }
                            if (!unavailableDailyDates.contains(firstDayOfMonth) && !selectedDates.contains(firstDayOfMonth)) {
                                caldroidFragment.clearBackgroundDrawableForDate(firstDayOfMonth);
                            }
                            caldroidFragment.setDisableDates(unavailableDailyDates);
                            break;
                        case "Hourly":
                            String todaySwitchHourly = jsonHourlyObject.getString(day + "Switch");
                            if (!todaySwitchHourly.equals("true")) {
                                if (!unavailableHourlyDates.contains(firstDayOfMonth)) {
                                    unavailableHourlyDates.add(firstDayOfMonth);
                                }
                            }
                            if (firstDayOfMonth.getTime() == today.getTime()) {
                                String availabilityHourFrom = jsonHourlyObject.getString(day + "From");
                                String availabilityHourTo = jsonHourlyObject.getString(day + "To");

                                if (availabilityHourFrom == null) {
                                    availabilityHourFrom = "12:00 AM";
                                }
                                if (availabilityHourTo == null) {
                                    availabilityHourTo = "11:59 PM";
                                }
                                int hour = c.get(Calendar.HOUR_OF_DAY);
                                int minute = c.get(Calendar.MINUTE);

                                Integer currentTime = Integer.valueOf(pad(hour) + "" + pad(minute));
                                Integer integerFrom = Integer.valueOf(convert12to24(availabilityHourFrom).replace("/", ""));
                                Integer integerTo = Integer.valueOf(convert12to24(availabilityHourTo).replace("/", ""));

                                if (currentTime < integerFrom || currentTime > integerTo) {
                                    if (!unavailableHourlyDates.contains(firstDayOfMonth)) {
                                        unavailableHourlyDates.add(firstDayOfMonth);
                                    }
                                }
                            }
                            if (!unavailableHourlyDates.contains(firstDayOfMonth) && !selectedDates.contains(firstDayOfMonth)) {
                                caldroidFragment.clearBackgroundDrawableForDate(firstDayOfMonth);
                            }
                            caldroidFragment.setDisableDates(unavailableHourlyDates);
                            break;
                    }

                    calendar.add(Calendar.DATE, 1);
                    firstDayOfMonth = calendar.getTime();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //caldroidFragment.setDisableDates(unavailableDates);
        caldroidFragment.refreshView();
    }

    public void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void updateMonthlyRecurring() {
        if (monthlyRecurringCheckBox.isChecked()) {
            monthlyRecurringTextView.setText("");
        } else {
            if (!monthlyStartTextView.getText().equals("")) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                Calendar c = Calendar.getInstance();
                Date startDate = null;
                try {
                    startDate = sdf.parse(monthlyStartTextView.getText().toString());
                    c.setTime(startDate);
                    c.add(Calendar.DATE, 30);
                    monthlyRecurringTextView.setText("Ends " + (c.get(Calendar.MONTH) + 1) + "/" + pad(String.valueOf(c.get(Calendar.DAY_OF_MONTH))) + "/" + c.get(Calendar.YEAR));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void requestReservation() {

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        Date today = new Date();
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(today);
        String todayDate = pad(String.valueOf((todayCalendar.get(Calendar.MONTH) + 1))) + "/" + pad(String.valueOf(todayCalendar.get(Calendar.DAY_OF_MONTH))) + "/" + todayCalendar.get(Calendar.YEAR);
        Date todayDateFormatted = null;
        try {
            todayDateFormatted = sdf.parse(todayDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (reserveTypeSpinner.getSelectedItem().toString().equals("Monthly")) {
            if (monthlyStartTextView.getText().equals("")) {
                displayMessage("Select a start date");
                return;
            }

            Date startDate;

            try {
                startDate = sdf.parse(monthlyStartTextView.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
                displayMessage("Incorrect dates");
                return;
            }

            if (startDate.getTime() <= todayDateFormatted.getTime()) {
                displayMessage("Monthly reservations must be after the current date.");
                return;
            }

            if (maxAvailableDate != null) {
                displayMessage("Monthly reservations are unavailable.");
                return;
            }

            Calendar c2 = Calendar.getInstance();
            c2.setTime(startDate);
            int startMonth = c2.get(Calendar.MONTH) + 1;
            int startYear = c2.get(Calendar.YEAR);
            addUnavailableWeekdaysToUnavailableDatesArray(startMonth, startYear);

            if (unavailableMonthlyDates.contains(startDate)) {
                displayMessage("The selected date is unavailable");
                return;
            }

            if (startDate.getTime() < minimumMonthlyDate.getTime()) {
                Calendar c = Calendar.getInstance();
                c.setTime(minimumMonthlyDate);
                c.add(Calendar.DATE, 1);

                displayMessage("First available monthly reservation is on " + pad(String.valueOf((c.get(Calendar.MONTH) + 1))) + "/" + pad(String.valueOf(c.get(Calendar.DAY_OF_MONTH))) + "/" + c.get(Calendar.YEAR));
                return;
            }

        } else if (reserveTypeSpinner.getSelectedItem().toString().equals("Daily")) {

            if (startDateEditText.getText().equals("")) {
                displayMessage("Select a start date");
                return;
            }

            if (endDateEditText.getText().equals("")) {
                displayMessage("Select an end date");
                return;
            }

            Date startDate;
            Date endDate;

            try {
                startDate = sdf.parse(startDateEditText.getText().toString());
                endDate = sdf.parse(endDateEditText.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
                displayMessage("Incorrect dates");
                return;
            }

            if (startDate.getTime() > endDate.getTime()) {
                displayMessage("Start date must be before end date.");
                return;
            }

            if (startDate.getTime() <= todayDateFormatted.getTime() || endDate.getTime() <= todayDateFormatted.getTime()) {
                displayMessage("Daily reservations must be after the current date.");
                return;
            }

            if (maxAvailableDate != null) {
                if (endDate.getTime() > maxAvailableDate.getTime()) {
                    displayMessage("One or more of the dates selected is unavailable");
                    return;
                }
            }

            Calendar c = Calendar.getInstance();
            c.setTime(startDate);

            Calendar unavailableCalendar = Calendar.getInstance();
            String unavailableDateString;

            // Add any unexpected unavailable dates to the UnavailableDates array. (If the user uses the dropdown instead of clicking on the calendar
            int month = c.get(Calendar.MONTH) + 1;
            int year = c.get(Calendar.YEAR);
            addUnavailableWeekdaysToUnavailableDatesArray(month, year);
            c.setTime(endDate);
            month = c.get(Calendar.MONTH) + 1;
            year = c.get(Calendar.YEAR);
            addUnavailableWeekdaysToUnavailableDatesArray(month, year);
            c.setTime(startDate);

            // Check unavailableDailyDates array for current reservations
            while (startDate.getTime() <= endDate.getTime()) {
                String startDateString = c.get(Calendar.YEAR) + "/" + pad(String.valueOf((c.get(Calendar.MONTH) + 1))) + "/" + pad(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
                for (int i = 0; i < unavailableDailyDates.size(); i++) {
                    unavailableCalendar.setTime(unavailableDailyDates.get(i));
                    unavailableDateString = unavailableCalendar.get(Calendar.YEAR) + "/" + pad(String.valueOf((unavailableCalendar.get(Calendar.MONTH) + 1))) + "/" + pad(String.valueOf(unavailableCalendar.get(Calendar.DAY_OF_MONTH)));

                    if (startDateString.equals(unavailableDateString)) {
                        displayMessage("One or more of the dates selected is unavailable");
                        return;
                    }
                }
                c.add(Calendar.DATE, 1);
                startDate = c.getTime();
            }

        } else if (reserveTypeSpinner.getSelectedItem().toString().equals("Hourly")) {
            Integer startHour = Integer.parseInt(convert12to24(hourlyStartTimeSpinner.getSelectedItem().toString().split(", ")[1]).replace("/",""));
            Integer endHour = Integer.parseInt(convert12to24(hourlyEndTimeSpinner.getSelectedItem().toString().split(", ")[1]).replace("/",""));

            if (unavailableHourlyDates.contains(todayDateFormatted)) {
                displayMessage("One or more of the dates selected is unavailable");
                return;
            }

            if (hourlyStartTimeSpinner.getSelectedItem().equals("") || hourlyEndTimeSpinner.getSelectedItem().equals("")) {
                displayMessage("Start and end hour must be selected");
                return;
            }

            if (endHour <= startHour ) {
                displayMessage("End time must be after start time.");
                return;
            }
        }

        RequestReservationTask requestReservationTask = new RequestReservationTask(this);
        requestReservationTask.execute();

    }

    public void loadSpotDetails() {
        LoadSpotTask loadSpotTask = new LoadSpotTask(this);
        loadSpotTask.execute();
    }

    public String generateHoursStringFromJSON(String jsonString) {

        if (jsonString.equals("")) {
            return "M: Not Available" +
                    "\nT: Not Available" +
                    "\nW: Not Available" +
                    "\nTh: Not Available" +
                    "\nF: Not Available" +
                    "\nSa: Not Available" +
                    "\nSu: Not Available";

        }

        String json = jsonString.replace("&quot;", "\"");
        String hours = "";

        try {
            JSONObject jsonObject = new JSONObject(json);
            String mondaySwitch = jsonObject.getString("mondaySwitch");
            String tuesdaySwitch = jsonObject.getString("tuesdaySwitch");
            String wednesdaySwitch = jsonObject.getString("wednesdaySwitch");
            String thursdaySwitch = jsonObject.getString("thursdaySwitch");
            String fridaySwitch = jsonObject.getString("fridaySwitch");
            String saturdaySwitch = jsonObject.getString("saturdaySwitch");
            String sundaySwitch = jsonObject.getString("sundaySwitch");

            if (mondaySwitch.equals("true")) {
                hours += "M: " + jsonObject.getString("mondayFrom") + " - " + jsonObject.getString("mondayTo");
            } else {
                hours += "M: Not Available";
            }
            if (tuesdaySwitch.equals("true")) {
                hours += "\nT: " + jsonObject.getString("tuesdayFrom") + " - " + jsonObject.getString("tuesdayTo");
            } else {
                hours += "\nT: Not Available";
            }
            if (wednesdaySwitch.equals("true")) {
                hours += "\nW: " + jsonObject.getString("wednesdayFrom") + " - " + jsonObject.getString("wednesdayTo");
            } else {
                hours += "\nW: Not Available";
            }
            if (thursdaySwitch.equals("true")) {
                hours += "\nTh: " + jsonObject.getString("thursdayFrom") + " - " + jsonObject.getString("thursdayTo");
            } else {
                hours += "\nTh: Not Available";
            }
            if (fridaySwitch.equals("true")) {
                hours += "\nF: " + jsonObject.getString("fridayFrom") + " - " + jsonObject.getString("fridayTo");
            } else {
                hours += "\nF: Not Available";
            }
            if (saturdaySwitch.equals("true")) {
                hours += "\nSa: " + jsonObject.getString("saturdayFrom") + " - " + jsonObject.getString("saturdayTo");
            } else {
                hours += "\nSa: Not Available";
            }
            if (sundaySwitch.equals("true")) {
                hours += "\nSu: " + jsonObject.getString("sundayFrom") + " - " + jsonObject.getString("sundayTo");
            } else {
                hours += "\nSu: Not Available";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return hours;
    }

    public Bitmap decodeSampledBitmapFromResource(InputStream inputStream1, InputStream inputStream2, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream1, null, options);

        // Calculate inSampleSize
        int inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inSampleSize = inSampleSize;
        return BitmapFactory.decodeStream(inputStream2, null, options2);
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if (datePicker.equals("start")) {
            startDateEditText.setText(pad((month+1))+"/"+pad(dayOfMonth)+"/"+year);
        } else if (datePicker.equals("end")) {
            endDateEditText.setText(pad((month+1))+"/"+pad(dayOfMonth)+"/"+year);
        } else if (datePicker.equals("monthly_start")) {
            if (maxAvailableDate != null) {
                monthlyStartTextView.setText("Monthly Unavailable");
            } else {
                if (!monthlyStartTextView.getText().toString().equals("")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    Date startDate = null;
                    try {
                        Calendar c2 = Calendar.getInstance();
                        startDate = sdf.parse(monthlyStartTextView.getText().toString());
                        c2.setTime(startDate);
                        startDate = c2.getTime();
                        caldroidFragment.clearBackgroundDrawableForDate(startDate);
                        caldroidFragment.clearSelectedDates();
                        caldroidFragment.refreshView();
                        caldroidFragment.setBackgroundDrawableForDate(getResources().getDrawable(R.color.colorPrimary), startDate);
                        caldroidFragment.refreshView();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                monthlyStartTextView.setText(pad((month+1))+"/"+pad(dayOfMonth)+"/"+year);
                updateMonthlyRecurring();
            }
        }
        updateTotal();
    }

    private String convert24to12(String time) {
        if (Integer.parseInt(time.split("/")[0]) < 13) {
            if (time.split("/")[0] == "00") {
                return "01:" + time.split("/")[1] + " AM";
            }
            return time.replace("/",":") + " AM";
        } else {
            return (Integer.parseInt(time.split("/")[0]) - 12) + ":" + time.split("/")[1] + " PM";
        }
    }

    private class LoadSpotTask extends AsyncTask<Void, Void, String> {

        Context ctx;
        LoadSpotTask(Context ctx) { this.ctx = ctx; }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Calendar c = Calendar.getInstance();
            minimumMonthlyDate = c.getTime();
        }

        @Override
        protected String doInBackground(Void... params) {
            String url_string = "https://" + ipAddress + "/";
            try {
                URL url = new URL(url_string);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String data = URLEncoder.encode("spotID", "UTF-8") + "=" + URLEncoder.encode(spotID, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                String response = "";
                String line = "";

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                while((line = bufferedReader.readLine()) != null) {
                    response += line;
                }
                bufferedReader.close();
                inputStream.close();

                if (!response.equals("")) {

                    String result = "";

                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");

                    if (status.equals("Success")) {

                        JSONObject jsonData = jsonObject.getJSONObject("data");
                        parkingSpot = new ParkingSpot(jsonData);

                        result = "Success";
                    } else {
                        result = "Error reaching server";
                    }

                    String reservationStatus = jsonObject.getString("reservationsStatus");

                    if (reservationStatus.equals("Success")) {

                        JSONArray reservationsArray = jsonObject.getJSONArray("reservationsData");
                        for (int i = 0; i < reservationsArray.length(); i++) {
                            JSONObject reservationJSONObject = reservationsArray.getJSONObject(i);
                            Reservation reservation = new Reservation(reservationJSONObject);
                            reservations.add(reservation);
                        }
                    }

                    return result;

                } else {
                    return "Error reaching server";
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Could not reach server";
            } catch (IOException e) {
                e.printStackTrace();
                return "Could not reach server";
            } catch (JSONException e) {
                e.printStackTrace();
                return "Could not reach server";
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String response) {
            if (response.equals("Success")) {

                /*
                    Initialze Parking Spot details
                 */
                spotAddressTextView.setText(parkingSpot.getSpotAddress());
                DownloadImageTask downloadImageTask = new DownloadImageTask(ReserveSpotActivity.this);
                downloadImageTask.execute();
                String prices = "Prices - ";
                Integer numOfReservationTypes = 0;
                Integer position = 0;
                if (parkingSpot.getAvailableMonthly() == 1) {
                    spotHoursTextView.setText("Monthly Reservation Hours \n\n" + generateHoursStringFromJSON(parkingSpot.getMonthlyHours()));
                    prices += "\nMonthly: $" + String.format(Locale.US, "%.2f", parkingSpot.getMonthlyPrice());
                }
                if (parkingSpot.getAvailableDaily() == 1) {
                    spotHoursTextView.setText("Daily Reservation Hours \n\n" + generateHoursStringFromJSON(parkingSpot.getDailyHours()));
                    prices += "\nDaily: $" + String.format(Locale.US, "%.2f", parkingSpot.getDailyPrice());
                }
                if (parkingSpot.getAvailableHourly() == 1) {
                    spotHoursTextView.setText("Hourly Reservation Hours \n\n" + generateHoursStringFromJSON(parkingSpot.getHourlyHours()));
                    prices += "\nHourly: $" + String.format(Locale.US, "%.2f", parkingSpot.getHourlyPrice());
                }
                if (parkingSpot.getAvailableMonthly() == 1) {
                    numOfReservationTypes++;
                }
                if (parkingSpot.getAvailableDaily() == 1) {
                    numOfReservationTypes++;
                }
                if (parkingSpot.getAvailableHourly() == 1) {
                    numOfReservationTypes++;
                }
                reservationTypes = new String[numOfReservationTypes];
                if (parkingSpot.getAvailableMonthly() == 1) {
                    reservationTypes[position] = "Monthly";
                    position++;
                }
                if (parkingSpot.getAvailableDaily() == 1) {
                    reservationTypes[position] = "Daily";
                    position++;
                }
                if (parkingSpot.getAvailableHourly() == 1) {
                    reservationTypes[position] = "Hourly";
                }
                spinnerAdapter = new ArrayAdapter<CharSequence>(ReserveSpotActivity.this, android.R.layout.simple_spinner_dropdown_item, reservationTypes);
                reserveTypeSpinner.setAdapter(spinnerAdapter);
                int reservationIndex = 0;
                for (int i = 0; i < reservationTypes.length; i++) {
                    if (reservationTypes[i].equals("Monthly") && filterType.equals("Monthly")) {
                        reservationIndex = i;
                    } else if (reservationTypes[i].equals("Daily") && filterType.equals("Daily")) {
                        reservationIndex = i;
                    } else if (reservationTypes[i].equals("Hourly") && filterType.equals("Hourly")) {
                        reservationIndex = i;
                    }
                }
                reserveTypeSpinner.setSelection(reservationIndex);
                spotDetailsTextView.setText(
                        "Spot Details \n\nType: " + parkingSpot.getSpotType() + "\n" +
                                "Access: " + parkingSpot.getAccessType() + "\n" +
                                prices + "\n" +
                                "Rating: " + parkingSpot.getSpotRating()
                );

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

                /*
                    Set unavailable reservation dates and update the caldroidFragment Calendar based
                    on the current reservations returned by the server.
                 */
                for (int i = 0; i < reservations.size(); i++) {

                    if (reservations.get(i).getReservationType().equals("Monthly")) {
                        String start = reservations.get(i).getStart();
                        String end = reservations.get(i).getEnd();

                        if (end.equals("none")) {
                            try {
                                maxAvailableDate = sdf.parse(start);
                                Calendar c = Calendar.getInstance();
                                c.setTime(maxAvailableDate);
                                c.add(Calendar.DATE, -1);
                                maxAvailableDate = c.getTime();
                                caldroidFragment.setMaxDate(maxAvailableDate);
                                monthlyStartTextView.setText("Monthly Unavailable");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {

                            try {
                                Date startDate = sdf.parse(start);
                                Date endDate = sdf.parse(end);
                                Calendar c = Calendar.getInstance();
                                c.setTime(startDate);

                                // Set minimumMonthlyDate
                                Calendar endDateCalendar = Calendar.getInstance();
                                endDateCalendar.setTime(endDate);
                                endDateCalendar.add(Calendar.DATE, 1);
                                Date lastDailyEndDate = endDateCalendar.getTime();

                                if (lastDailyEndDate.getTime() > minimumMonthlyDate.getTime()) {
                                    minimumMonthlyDate = lastDailyEndDate;
                                }

                                // Set unavailable dates
                                while (startDate.getTime() <= endDate.getTime()) {
                                    unavailableDates.add(startDate);
                                    c.add(Calendar.DATE, 1);
                                    startDate = c.getTime();
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (reservations.get(i).getReservationType().equals("Daily")) {

                        String start = reservations.get(i).getStart();
                        String end = reservations.get(i).getEnd();

                        try {
                            Date startDate = sdf.parse(start);
                            Date endDate = sdf.parse(end);
                            Calendar c = Calendar.getInstance();
                            c.setTime(startDate);

                            // Set minimumMonthlyDate
                            Calendar endDateCalendar = Calendar.getInstance();
                            endDateCalendar.setTime(endDate);
                            endDateCalendar.add(Calendar.DATE, 1);
                            Date lastDailyEndDate = endDateCalendar.getTime();

                            if (lastDailyEndDate.getTime() > minimumMonthlyDate.getTime()) {
                                minimumMonthlyDate = lastDailyEndDate;
                            }

                            // Set unavailable dates
                            while (startDate.getTime() <= endDate.getTime()) {
                                unavailableDates.add(startDate);
                                c.add(Calendar.DATE, 1);
                                startDate = c.getTime();
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else if (reservations.get(i).getReservationType().equals("Hourly")) {

                    }

                }

                reloadHourlyReservatonType();

                /*
                    Add unavailable weeekdays to the unavailableDates array
                 */
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                int month = calendar.get(Calendar.MONTH) + 1;
                int year = calendar.get(Calendar.YEAR);

                for (int i = 0; i < unavailableDates.size(); i++) {
                    unavailableMonthlyDates.add(unavailableDates.get(i));
                    unavailableDailyDates.add(unavailableDates.get(i));
                    unavailableHourlyDates.add(unavailableDates.get(i));
                }

                addUnavailableWeekdaysToUnavailableDatesArray(month, year);

                switch (reserveTypeSpinner.getSelectedItem().toString()) {
                    case "Monthly":
                        caldroidFragment.setDisableDates(unavailableMonthlyDates);
                        break;
                    case "Daily":
                        caldroidFragment.setDisableDates(unavailableDailyDates);
                        break;
                    case "Hourly":
                        caldroidFragment.setDisableDates(unavailableHourlyDates);
                        break;
                }
                //caldroidFragment.setDisableDates(unavailableDates);

                caldroidFragment.refreshView();

            } else {
                Toast.makeText(ReserveSpotActivity.this, response, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
        Reloads the hourly reservation availability picker based on the current reservations returned from the server.
     */
    public void reloadHourlyReservatonType() {
        Integer firstAvailableTimeFound = null;

        Date today = new Date();
        Integer dayOfWeek = today.getDay();
        String day = "";

        switch (dayOfWeek) {
            case 0:
                day = "sunday";
                break;
            case 1:
                day = "monday";
                break;
            case 2:
                day = "tuesday";
                break;
            case 3:
                day = "wednesday";
                break;
            case 4:
                day = "thursday";
                break;
            case 5:
                day = "friday";
                break;
            case 6:
                day = "saturday";
                break;
        }

        String json = parkingSpot.getHourlyHours().replace("&quot;", "\"");
        JSONObject jsonObject = null;
        String availabilityHourFrom = null;
        String availabilityHourTo = null;
        Boolean todaySwitchTrue = false;

        try {
            jsonObject = new JSONObject(json);
            String todaySwitch = jsonObject.getString(day + "Switch");
            if (todaySwitch.equals("true")) {
                todaySwitchTrue = true;
                availabilityHourFrom = jsonObject.getString(day + "From");
                availabilityHourTo = jsonObject.getString(day + "To");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Deal with hourly reservations
        List<CharSequence> availableTimes = new ArrayList<CharSequence>();

        final List<Boolean> availabilityHash = new ArrayList<Boolean>();

        int i = 0;
        while (i < 49) {
            String hours;
            String minutes;
            String ampm;

            // Set minutes
            if (i == 48) {
                minutes = "59";
            } else if ((i % 2) > 0) {
                minutes = "30";
            } else {
                minutes = "00";
            }

            // Set hours
            if (i == 48) {
                hours = "11";
            } else if (i == 0 || i == 1) {
                hours = "12";
            } else if (i < 26) {
                hours = pad(String.valueOf(Double.valueOf(Math.floor(i / 2)).intValue()));
            } else {
                hours = pad(String.valueOf(Double.valueOf(Math.floor(i/2) - 12).intValue()));
            }

            // Set ampm
            if (i < 24) {
                ampm = "AM";
            } else {
                ampm = "PM";
            }

            availableTimes.add("today, " + hours + ":" + minutes + " " + ampm);

            Boolean timeIsAvailable = null;
            if (todaySwitchTrue) {
                timeIsAvailable = timeIsAvailable(hours, minutes, ampm, availabilityHourFrom, availabilityHourTo);
            } else {
                timeIsAvailable = false;
            }


            availabilityHash.add(timeIsAvailable);

            if (firstAvailableTimeFound == null) {
                if (timeIsAvailable) {
                    firstAvailableTimeFound = i;
                }
            }


            if (availabilityHash.size() == 1) {
                hourlyStartTimeSpinner.setSelection(i);
                hourlyEndTimeSpinner.setSelection(i);
            }

            i++;
        }

        ArrayAdapter<CharSequence> availableTimesAdapter = new ArrayAdapter<CharSequence>(ReserveSpotActivity.this, android.R.layout.simple_spinner_dropdown_item, availableTimes) {
            @Override
            public boolean isEnabled(int position) {
                if (!availabilityHash.get(position)) {
                    return false;
                }
                return true;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View myView = super.getDropDownView(position, convertView, parent);
                TextView mTextView = (TextView) myView;

                if (!availabilityHash.get(position)) {
                    mTextView.setTextColor(Color.GRAY);
                    mTextView.setBackgroundColor(Color.LTGRAY);
                } else {
                    mTextView.setTextColor(Color.BLACK);
                    mTextView.setBackgroundColor(Color.WHITE);
                }

                return mTextView;
            }
        };

        hourlyStartTimeSpinner.setAdapter(availableTimesAdapter);
        hourlyEndTimeSpinner.setAdapter(availableTimesAdapter);

        if (firstAvailableTimeFound != null) {
            hourlyStartTimeSpinner.setSelection(firstAvailableTimeFound);
            hourlyEndTimeSpinner.setSelection(firstAvailableTimeFound);
        } else {
            hourlyStartTimeSpinner.setEnabled(false);
            hourlyEndTimeSpinner.setEnabled(false);
        }
    }

    private Boolean timeIsAvailable(String hours, String minutes, String ampm, String availabilityHourFrom, String availabilityHourTo) {

        Date todaysDate = new Date();
        Integer requestedTime = Integer.valueOf(convert12to24(hours + ":" + minutes + " " + ampm).replace("/",""));
        Integer currentTime = Integer.valueOf(pad(String.valueOf(todaysDate.getHours())) + "" + pad(String.valueOf(todaysDate.getMinutes())));
        Integer availableFrom = Integer.valueOf(convert12to24(availabilityHourFrom).replace("/",""));
        Integer availableTo = Integer.valueOf(convert12to24(availabilityHourTo).replace("/",""));

        Calendar cal = Calendar.getInstance();
        cal.setTime(todaysDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date todaysDateClear;

        try {
            todaysDateClear = sdf.parse(cal.get(Calendar.YEAR) + "/" + pad(cal.get(Calendar.MONTH) + 1) + "/" + pad(cal.get(Calendar.DAY_OF_MONTH)));
            //todaysDateClear = sdf.parse(todaysDate.getYear() + "/" + todaysDate.getMonth() + 1 + "/" + todaysDate.getDate());
            if (unavailableDates.contains(todaysDateClear)) {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (requestedTime < currentTime) {
            return false;
        }

        if (requestedTime < availableFrom || requestedTime > availableTo) {
            return false;
        }



        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getReservationType().equals("Hourly")) {
                Integer reservationStart = Integer.valueOf(reservations.get(i).getStart().split("/")[3] + "" + reservations.get(i).getStart().split("/")[4]);
                Integer reservationEnd = Integer.valueOf(reservations.get(i).getEnd().split("/")[3] + "" + reservations.get(i).getEnd().split("/")[4]);

                disableDailyReservationIfHourlyReservationEndsAfterCurrentTime(reservationEnd, currentTime);

                if (requestedTime >= reservationStart && requestedTime <= reservationEnd) {
                    return false;
                }
            } else if (reservations.get(i).getReservationType().equals("Daily")) {
                if (unavailableDates.contains(todaysDate)) {
                    return false;
                }
            } else if (reservations.get(i).getReservationType().equals("Monthly")) {
                if (reservations.get(i).getEnd().equals("none")) {
                    Integer currentDate = Integer.valueOf(todaysDate.getYear() + "" + todaysDate.getMonth() + "" + todaysDate.getDate());
                    Integer reservationStart = Integer.valueOf(reservations.get(i).getStart().split("/")[0] + reservations.get(i).getStart().split("/")[1] + reservations.get(i).getStart().split("/")[2]);

                    if (reservationStart <= currentDate) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void disableDailyReservationIfHourlyReservationEndsAfterCurrentTime(Integer reservationEnd, Integer currentTime) {
        Date today = new Date();

        if (reservationEnd > currentTime) {
            unavailableDates.add(today);
        }
    }

    /*
        Parameters: "hours:minutes AMPM"
        Returns: "hours/minutes" in 24 hour format
     */
    private String convert12to24(String time) {
        if (time.split(" ")[1].equals("AM")) {
            if (time.split(" ")[0].split(":")[0].equals("12")) {
                return ("00:" + time.split(" ")[0].split(":")[1]).replace(":","/");
            }
            return time.split(" ")[0].replace(":","/");
        } else {
            if (time.split(" ")[0].split(":")[0].equals("12")) {
                return time.split(" ")[0].replace(":","/");
            }
            return ((Integer.valueOf(time.split(" ")[0].split(":")[0]) + 12) + ":" + time.split(" ")[0].split(":")[1]).replace(":","/");
        }
    }



    private class DownloadImageTask extends AsyncTask<String, Void, String> {
        Context ctx;
        String photos;

        DownloadImageTask(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... urls) {

            this.photos = parkingSpot.getPhotos();

            String[] urls_string = photos.split(",");

            for (int i = 0; i < urls_string.length; i++) {
                try {
                    InputStream inputStream1 = new java.net.URL("https://" + ipAddress + "" + urls_string[i]).openStream();
                    InputStream inputStream2 = new java.net.URL("https://" + ipAddress + "" + urls_string[i]).openStream();
                    bitmaps.add(decodeSampledBitmapFromResource(inputStream1, inputStream2, 200, 200));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return "Done.";
        }

        protected void onPostExecute(String result) {
            viewPager.setCurrentItem(0, true);
            for (int i = 0; i < bitmaps.size(); i++) {
                pagerAdapter.addImageResource(viewPager, bitmaps.get(i));
            }

            String imageNumbers = "1 of " + bitmaps.size();
            imageNumberTextView.setText(imageNumbers);
        }
    }

    private void updateTotal() {
        String reservationType = reserveTypeSpinner.getSelectedItem().toString();
        DecimalFormat df = new DecimalFormat("0.00");
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        Date today = new Date();
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(today);
        String todayDate = pad(String.valueOf((todayCalendar.get(Calendar.MONTH) + 1))) + "/" + pad(String.valueOf(todayCalendar.get(Calendar.DAY_OF_MONTH))) + "/" + todayCalendar.get(Calendar.YEAR);

        if (reservationType.equals("Monthly")) {
            if (!monthlyStartTextView.getText().equals("")) {
                String price = "$" + df.format(parkingSpot.getMonthlyPrice() + 0.30);
                totalPriceTextView.setText(price);
            }
        } else if (reservationType.equals("Daily")) {
            if (startDateEditText.getText().equals("") || endDateEditText.getText().equals("")) {
                String price = "$0.00";
                totalPriceTextView.setText(price);
            } else {
                Integer numOfDays = 0;
                try {
                    Date startDate = sdf.parse(startDateEditText.getText().toString());
                    Date endDate = sdf.parse(endDateEditText.getText().toString());
                    Calendar c = Calendar.getInstance();
                    c.setTime(startDate);

                    while (startDate.getTime() <= endDate.getTime()) {
                        numOfDays++;
                        c.add(Calendar.DATE, 1);
                        startDate = c.getTime();
                    }

                    String price = "$" + df.format(parkingSpot.getDailyPrice() * numOfDays + 0.30);
                    totalPriceTextView.setText(price);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } else if (reservationType.equals("Hourly")) {
            Double startHour = Double.valueOf((convert12to24(hourlyStartTimeSpinner.getSelectedItem().toString().split(", ")[1]).replace("/",".")).replace("30","50"));
            Double endHour = Double.valueOf((convert12to24(hourlyEndTimeSpinner.getSelectedItem().toString().split(", ")[1]).replace("/",".")).replace("30","50"));
            String price;

            if (startHour == 23.59) {
                startHour = 24.00;
            }
            if (endHour == 23.59) {
                endHour = 24.00;
            }

            if (startHour == endHour || endHour < startHour) {
                price = "$0.00";
                totalPriceTextView.setText(price);
                return;
            }

            price = "$" + df.format(((endHour - startHour) * parkingSpot.getHourlyPrice()) + 0.30);
            totalPriceTextView.setText(price);
        }
    }

    /*
        Parameters: an integer in the form of a integer
        Returns: an integer in the form of a String with a leading 0 if the original integer had only a length of 1
     */
    private String pad(Integer integer) {
        if (integer.toString().length() < 2) {
            return "0" + integer.toString();
        }
        return integer.toString();
    }

    /*
        Parameters: an integer in the form of a String
        Returns: an integer in the form of a String with a leading 0 if the original integer had only a length of 1
     */
    private String pad(String text) {
        if (text.length() < 2) {
            return "0" + text;
        }
        return text;
    }

    private class RequestReservationTask extends AsyncTask<Void, Void, String> {

        Context ctx;
        String start;
        String end;
        String address;
        String reservationType;
        String price;
        String hours;
        String access;

        AlertDialog pendingDialog;

        RequestReservationTask(Context ctx) { this.ctx = ctx; }

        @Override
        protected void onPreExecute() {
            /*
                Testing
             */

//            onBraintreeSubmit();

            /*
                End Testing
             */

            AlertDialog.Builder builder = new AlertDialog.Builder(ReserveSpotActivity.this);
            builder.setMessage("Please wait...")
                    .setTitle("Status")
                    .setCancelable(false);
            pendingDialog = builder.create();
            pendingDialog.show();

            reservationType = reserveTypeSpinner.getSelectedItem().toString();
            if (reservationType.equals("Monthly")) {
                start = monthlyStartTextView.getText().toString().split("/")[2] + "/" + pad(monthlyStartTextView.getText().toString().split("/")[0]) + "/" + pad(monthlyStartTextView.getText().toString().split("/")[1]);
                if (monthlyRecurringCheckBox.isChecked()) {
                    end = "none";
                } else {
                    end = monthlyRecurringTextView.getText().toString().split("Ends ")[1].split("/")[2] + "/" + pad(monthlyRecurringTextView.getText().toString().split("Ends ")[1].split("/")[0]) + "/" + pad(monthlyRecurringTextView.getText().toString().split("Ends ")[1].split("/")[1]);
                }
            } else if (reservationType.equals("Daily")) {
                start = startDateEditText.getText().toString().split("/")[2] + "/" + pad(startDateEditText.getText().toString().split("/")[0]) + "/" + pad(startDateEditText.getText().toString().split("/")[1]);
                end = endDateEditText.getText().toString().split("/")[2] + "/" + pad(endDateEditText.getText().toString().split("/")[0]) + "/" + pad(endDateEditText.getText().toString().split("/")[1]);
            } else if (reservationType.equals("Hourly")) {
                Date today = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(today);

                String todayString = c.get(Calendar.YEAR) + "/" + pad(String.valueOf((c.get(Calendar.MONTH) + 1))) + "/" + pad(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
                start = todayString + "/" + convert12to24(hourlyStartTimeSpinner.getSelectedItem().toString().split(", ")[1]);
                end = todayString + "/" + convert12to24(hourlyEndTimeSpinner.getSelectedItem().toString().split(", ")[1]);
            }

            address = parkingSpot.getSpotAddress();

            price = totalPriceTextView.getText().toString().split("\\$")[1];
            switch (reservationType) {
                case "Monthly":
                    this.hours = parkingSpot.getMonthlyHours().replace("&quot;", "\"");
                    break;
                case "Daily":
                    this.hours = parkingSpot.getDailyHours().replace("&quot;", "\"");
                    break;
                case "Hourly":
                    this.hours = parkingSpot.getHourlyHours().replace("&quot;", "\"");
                    break;
            }
            access = parkingSpot.getAccessType();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {

            String url_string = "https://" + ipAddress + "/";
            try {
                URL url = new URL(url_string);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String data = URLEncoder.encode("spotID", "UTF-8") + "=" + URLEncoder.encode(parkingSpot.getSpotID().toString(), "UTF-8") + "&" +
                        URLEncoder.encode("sessionid", "UTF-8") + "=" + URLEncoder.encode(sessionid, "UTF-8") + "&" +
                        URLEncoder.encode("start", "UTF-8") + "=" + URLEncoder.encode(start, "UTF-8") + "&" +
                        URLEncoder.encode("end", "UTF-8") + "=" + URLEncoder.encode(end, "UTF-8") + "&" +
                        URLEncoder.encode("totalPrice", "UTF-8") + "=" + URLEncoder.encode(price, "UTF-8") + "&" +
                        URLEncoder.encode("spotAddress", "UTF-8") + "=" + URLEncoder.encode(parkingSpot.getSpotAddress(), "UTF-8") + "&" +
                        URLEncoder.encode("reservationType", "UTF-8") + "=" + URLEncoder.encode(reservationType, "UTF-8") + "&" +
                        URLEncoder.encode("access", "UTF-8") + "=" + URLEncoder.encode(access, "UTF-8") + "&" +
                        URLEncoder.encode("hours", "UTF-8") + "=" + URLEncoder.encode(hours, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                String response = "";
                String line = "";

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                while((line = bufferedReader.readLine()) != null) {
                    response += line;
                }
                bufferedReader.close();
                inputStream.close();

                if (!response.equals("")) {

                    return response;
                } else {
                    return "Error reaching server";
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Could not reach server";
            } catch (IOException e) {
                e.printStackTrace();
                return "Could not reach server";
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String response) {

            pendingDialog.dismiss();

            if (!response.equals("Error reaching server") && !response.equals("Could not reach server")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");

                    if (status.equals("Success")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ReserveSpotActivity.this);
                        builder.setMessage("Reservation requested!")
                                .setTitle("Status")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    } else {
                        String message = jsonObject.getString("message");
                        if (message.equals("Please log in.")) {
                            LogoutUtility logoutUtility = new LogoutUtility(ctx, sessionid);
                            logoutUtility.execute();
                        } else {
                            Toast.makeText(ReserveSpotActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ReserveSpotActivity.this, "Error reaching server", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ReserveSpotActivity.this, response, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
