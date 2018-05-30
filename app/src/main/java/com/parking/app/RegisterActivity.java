/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class RegisterActivity extends AppCompatActivity {

    String ipAddress = "";

    EditText emailEditText;
    EditText passwordEditText;
    EditText confirmPasswordEditText;
    EditText firstNameEditText;
    EditText lastNameEditText;
    EditText vehicleMakeEditText;
    EditText vehicleModelEditText;
    EditText vehicleColorEditText;
    EditText vehicleYearEditText;
    Button signUpButton;
    TextView alreadyHaveAccountTextView;
    TextView agreementsTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = (EditText) findViewById(R.id.email);
        passwordEditText = (EditText) findViewById(R.id.password);
        confirmPasswordEditText = (EditText) findViewById(R.id.confirm_password);
        firstNameEditText = (EditText) findViewById(R.id.first_name);
        lastNameEditText = (EditText) findViewById(R.id.last_name);
        vehicleMakeEditText = (EditText) findViewById(R.id.vehicle_make);
        vehicleModelEditText = (EditText) findViewById(R.id.vehicle_model);
        vehicleColorEditText = (EditText) findViewById(R.id.vehicle_color);
        vehicleYearEditText = (EditText) findViewById(R.id.vehicle_year);
        signUpButton = (Button) findViewById(R.id.sign_up_button);
        alreadyHaveAccountTextView = (TextView) findViewById(R.id.already_have_account_textview);
        agreementsTextView = (TextView) findViewById(R.id.agreements);
        agreementsTextView.setMovementMethod(LinkMovementMethod.getInstance());

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        alreadyHaveAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void signUp() {

        if (emailEditText.getText().toString().equals("") || passwordEditText.getText().toString().equals("") ||
                confirmPasswordEditText.getText().toString().equals("") || firstNameEditText.getText().toString().equals("") ||
                lastNameEditText.getText().toString().equals("")) {
            displayToast("Missing required field.");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString().replace(" ", "")).matches()) {
            displayToast("Not a valid email");
            return;
        }

        if (!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())) {
            displayToast("Password and Confirm Password do not match");
            return;
        }

//        if (vehicleMakeEditText.getText().toString().equals("") || vehicleModelEditText.getText().toString().equals("") ||
//                vehicleColorEditText.getText().toString().equals("") || vehicleYearEditText.getText().toString().equals("")) {
//            displayToast("All vehicle fields are required");
//            return;
//        }

        BackgroundTask backgroundTask = new BackgroundTask(this);
        backgroundTask.execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private class BackgroundTask extends AsyncTask<Void, Void, String> {

        Context ctx;
        BackgroundTask(Context ctx) { this.ctx = ctx; }

        String email;
        String password;
        String firstName;
        String lastName;
        String vehicleMake;
        String vehicleModel;
        String vehicleColor;
        String vehicleYear;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            email = emailEditText.getText().toString().replace(" ", "");
            password = passwordEditText.getText().toString();
            firstName = firstNameEditText.getText().toString();
            lastName = lastNameEditText.getText().toString();
            vehicleMake = vehicleMakeEditText.getText().toString();
            vehicleModel = vehicleModelEditText.getText().toString();
            vehicleColor = vehicleColorEditText.getText().toString();
            vehicleYear = vehicleYearEditText.getText().toString();
        }

        @Override
        protected String doInBackground(Void... params) {
            String url_string = "https://" + ipAddress + "/";

            try {
                URL url = new URL(url_string);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String data = URLEncoder.encode("email", "UTF-8")+"="+URLEncoder.encode(email, "UTF-8") + "&" +
                        URLEncoder.encode("password", "UTF-8")+"="+URLEncoder.encode(password, "UTF-8") + "&" +
                        URLEncoder.encode("firstName", "UTF-8")+"="+URLEncoder.encode(firstName, "UTF-8") + "&" +
                        URLEncoder.encode("lastName", "UTF-8")+"="+URLEncoder.encode(lastName, "UTF-8") + "&" +
                        URLEncoder.encode("vehicleMake", "UTF-8")+"="+URLEncoder.encode(vehicleMake, "UTF-8") + "&" +
                        URLEncoder.encode("vehicleModel", "UTF-8")+"="+URLEncoder.encode(vehicleModel, "UTF-8") + "&" +
                        URLEncoder.encode("vehicleColor", "UTF-8")+"="+URLEncoder.encode(vehicleColor, "UTF-8") + "&" +
                        URLEncoder.encode("vehicleYear", "UTF-8")+"="+URLEncoder.encode(vehicleYear, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                String response = "";
                String line = "";
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                while ( (line = bufferedReader.readLine()) != null ) {
                    response += line;
                }

                if (!response.equals("")) {
                    return response;
                } else {
                    return "Error reaching server.";
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
            Log.e("response", "response: " + response);
            if (!response.equals("Could not reach server.") && !response.equals("Error reaching server.")) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    String message = jsonObject.getString("message");

                    if (status.equals("Success")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                        builder.setMessage(message)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    } else {
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else {
                Toast.makeText(RegisterActivity.this, response, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
