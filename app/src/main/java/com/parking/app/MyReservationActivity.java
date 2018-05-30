/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;



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

public class MyReservationActivity extends AppCompatActivity {

    String ipAddress;

    ParkingSpot parkingSpot;
    Reservation reservation;
    String sessionid;
    String previousActivity;

    TextView spotTextView;
    TextView typeTextView;
    TextView fromTextView;
    TextView toTextView;
    TextView accessTextView;
    TextView hoursTextView;
    TextView cancelTextView;
    TextView renterTextView;
    TextView vehicleTextView;
    TextView totalPriceTextView;
    Spinner reasonSpinner;
    Button cancelButton;
    LinearLayout reasonLinearLayout;

    String[] reasons = {"Select Reason", "No longer need the spot", "Spot inaccurately represented", "Spot is fraudulent", "Spot Owner", "Other"};
    ArrayAdapter<CharSequence> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("My Reservation");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        reservation = bundle.getParcelable("reservation");
        sessionid = bundle.getString("sessionid");
        previousActivity = bundle.getString("previousActivity");
        ipAddress = bundle.getString("ipAddress");

        spotTextView = (TextView) findViewById(R.id.spot_address);
        typeTextView = (TextView) findViewById(R.id.spot_type);
        fromTextView = (TextView) findViewById(R.id.from_field);
        toTextView = (TextView) findViewById(R.id.to_field);
        accessTextView = (TextView) findViewById(R.id.access_type);
        hoursTextView = (TextView) findViewById(R.id.hours_field);
        renterTextView = (TextView) findViewById(R.id.name);
        vehicleTextView = (TextView) findViewById(R.id.vehicle);
        totalPriceTextView = (TextView) findViewById(R.id.total_price);
        cancelTextView = (TextView) findViewById(R.id.initial_cancel);
        reasonSpinner = (Spinner) findViewById(R.id.cancel_reason);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        reasonLinearLayout = (LinearLayout) findViewById(R.id.reason_linear_layout);

        spotTextView.setText(reservation.getSpotAddress());
        typeTextView.setText(reservation.getReservationType());

        if (reservation.getReservationType().equals("Monthly")) {
            fromTextView.setText(reservation.getStart().split("/")[1] + "/" + reservation.getStart().split("/")[2] + "/" + reservation.getStart().split("/")[0]);

            if (reservation.getEnd().equals("none")) {
                toTextView.setText("Recurring Monthly");
            } else {
                toTextView.setText(reservation.getEnd().split("/")[1] + "/" + reservation.getEnd().split("/")[2] + "/" + reservation.getEnd().split("/")[0]);
            }
        } else if (reservation.getReservationType().equals("Daily")) {
            fromTextView.setText(reservation.getStart().split("/")[1] + "/" + reservation.getStart().split("/")[2] + "/" + reservation.getStart().split("/")[0]);
            toTextView.setText(reservation.getEnd().split("/")[1] + "/" + reservation.getEnd().split("/")[2] + "/" + reservation.getEnd().split("/")[0]);
        } else if (reservation.getReservationType().equals("Hourly")) {
            String startDate = reservation.getStart().split("/")[1] + "/" + reservation.getStart().split("/")[2] + "/" + reservation.getStart().split("/")[0];
            Spanned startTime = Html.fromHtml(startDate + ": <b>" + convert24to12(reservation.getStart().split("/")[3] + "/" + reservation.getStart().split("/")[4]) + "</b>");
            Spanned endTime = Html.fromHtml(startDate + ": <b>" + convert24to12(reservation.getEnd().split("/")[3] + "/" + reservation.getEnd().split("/")[4]) + "</b>");

            fromTextView.setText(startTime);
            toTextView.setText(endTime);
        }

        accessTextView.setText(reservation.getAccess());
        hoursTextView.setText(generateHoursStringFromJSON(reservation.getHours()));
        renterTextView.setText(reservation.getRenterName());
        vehicleTextView.setText(reservation.getRenterVehicle());

        DecimalFormat df = new DecimalFormat("0.00");
        String price = "$" + df.format(reservation.getTotalPrice());
        totalPriceTextView.setText(price);

        spinnerAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, reasons);
        reasonSpinner.setAdapter(spinnerAdapter);

        reasonLinearLayout.setVisibility(View.INVISIBLE);
        cancelButton.setVisibility(View.INVISIBLE);


        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialCancel();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelReservation();
            }
        });

    }

    public void initialCancel() {
        reasonLinearLayout.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
    }

    public void cancelReservation() {
        if (reasonSpinner.getSelectedItem().toString().equals("Select Reason")) {
            Toast.makeText(this, "Please select a reason", Toast.LENGTH_SHORT).show();
            return;
        }

        CancelReservationTask cancelReservationTask = new CancelReservationTask(this);
        cancelReservationTask.execute();
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

    private class CancelReservationTask extends AsyncTask<Integer, Void, String> {

        Context ctx;
        CancelReservationTask(Context ctx) { this.ctx = ctx; }
        String reason = "";

        @Override
        protected void onPreExecute() {
            this.reason = reasonSpinner.getSelectedItem().toString();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            String url_string = null;

            if (previousActivity.equals("MyReservationsFragment")) {
                url_string = "https://" + ipAddress + "/";
            }

            try {
                URL url = new URL(url_string);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String data = URLEncoder.encode("spotID", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(reservation.getSpotID()), "UTF-8") + "&" +
                        URLEncoder.encode("sessionid", "UTF-8") + "=" + URLEncoder.encode(sessionid, "UTF-8") + "&" +
                        URLEncoder.encode("reservationID", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(reservation.getReservationID()), "UTF-8") + "&" +
                        URLEncoder.encode("reason", "UTF-8") + "=" + URLEncoder.encode(this.reason, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                String response = "";
                String line = "";

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                while ((line=bufferedReader.readLine()) != null) {
                    response += line;
                }
                bufferedReader.close();
                inputStream.close();

                if (!response.equals("")) {
                    return response;
                } else {
                    return "Error reaching server.";
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Could not reach server.";
            } catch (IOException e) {
                e.printStackTrace();
                return "Could not reach server.";
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String response) {
            if (!response.equals("Error reaching server.") && !response.equals("Could not reach server.")) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("Success")) {
                        String message = jsonObject.getString("message");
                        AlertDialog.Builder builder = new AlertDialog.Builder(MyReservationActivity.this);
                        builder.setMessage(message)
                                .setTitle("Status")
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
                            Toast.makeText(MyReservationActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MyReservationActivity.this, "Error reaching server", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MyReservationActivity.this, response, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
