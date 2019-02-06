/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;


public class LoginActivity extends AppCompatActivity {

    String ipAddress = "";

    Button signInButton;
    Button signUpButton;
    EditText userEmail;
    EditText userPassword;
    LinearLayout mainLayout;
    ImageView checkSessionImage;
    String isUserLoggedInFileName;
    LinearLayout forgotPasswordLinearLayout;
    TextView forgotPasswordButtonInitial;
    Button forgotPasswordButton;
    EditText forgotPasswordEmail;
    TextView agreementsTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkSessionImage = (ImageView) findViewById(R.id.checkSessionImage);
        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        mainLayout.setVisibility(View.GONE);

        isUserLoggedInFileName = "isUserLoggedIn";

        // Check if the user was logged on prior to the app last closing. If the user was logged on, check if the user's
        // session is still valid and direct them either to the login page or to the home page.
        try {
            String response = "";
            String line = "";
            FileInputStream inputStream = openFileInput(isUserLoggedInFileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
            while ((line = bufferedReader.readLine()) != null) {
                response += line;
            }
            bufferedReader.close();
            inputStream.close();

            if (!response.equals("")) {
                String[] loginData = response.split(",");
                String loggedIn = loginData[0];
                String sessionid = loginData[1];

                if (loggedIn.equals("true")) {

                    CheckSession checkSession = new CheckSession(this, sessionid);
                    checkSession.execute();

                } else {
                    checkSessionImage.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            checkSessionImage.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            checkSessionImage.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
            checkSessionImage.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
        }

        // Initialize views
        signInButton = (Button) findViewById(R.id.sign_in_button);
        signUpButton = (Button) findViewById(R.id.sign_up_button);
        userEmail = (EditText) findViewById(R.id.user_email);
        userPassword = (EditText) findViewById(R.id.user_password);
        forgotPasswordButtonInitial = (TextView) findViewById(R.id.forgot_password_initial_button);
        forgotPasswordLinearLayout = (LinearLayout) findViewById(R.id.forgot_password_linear_layout);
        forgotPasswordButton = (Button) findViewById(R.id.forgot_password_button);
        forgotPasswordEmail = (EditText) findViewById(R.id.forgot_password_email);
        agreementsTextView = (TextView) findViewById(R.id.agreements);
        agreementsTextView.setMovementMethod(LinkMovementMethod.getInstance());


        /*
         * Set on click listeners
         */
        forgotPasswordButtonInitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPasswordLinearLayout.setVisibility(View.VISIBLE);
            }
        });

        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (forgotPasswordEmail.getText().toString().equals("")) {
                    Toast.makeText(LoginActivity.this, "Please enter an email address.", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(forgotPasswordEmail.getText().toString(), "UTF-8");
                    AsyncUtility asyncUtility = new AsyncUtility(LoginActivity.this, "https://" + ipAddress + "", data, new AsyncUtility.AsyncResponse() {
                        @Override
                        public void processFinish(String output) {
                            if (!output.equals("Error reaching server.")) {

                                try {
                                    JSONObject jsonObject = new JSONObject(output);
                                    String status = jsonObject.getString("status");
                                    String message = jsonObject.getString("message");

                                    if (status.equals("Success")) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                        builder.setMessage(message)
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        forgotPasswordLinearLayout.setVisibility(View.GONE);
                                                    }
                                                });
                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(LoginActivity.this, "Error reaching server",
                                            Toast.LENGTH_LONG).show();
                                }


                            } else {
                                Toast.makeText(LoginActivity.this, output, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    asyncUtility.execute();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackgroundTask loginTask = new BackgroundTask(LoginActivity.this);
                loginTask.execute();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // Attempt to log the user in with the provided username and password.
    private class BackgroundTask extends AsyncTask<Void, Void, String> {

        Context ctx;
        String email;
        String password;
        String isUserLoggedIn;

        BackgroundTask(Context ctx){
            this.ctx = ctx;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            email = userEmail.getText().toString();
            password = userPassword.getText().toString();
        }

        @Override
        protected String doInBackground(Void... params) {
            String url_string = "https://" + ipAddress + "";

            try {
                URL url = new URL(url_string);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") + "&" +
                        URLEncoder.encode("application", "UTF-8") + "=" + URLEncoder.encode("android", "UTF-8") + "&" +
                        URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                String response = "";
                String line = "";
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                if(!response.equals("")) {

                    return response;
                } else {
                    return "Error reaching server.";
                }

            } catch (MalformedURLException e) {
                Log.e("error:", e.toString());
                e.printStackTrace();
                return "Could not reach server.";
            } catch (ProtocolException e) {
                Log.e("error:", e.toString());
                e.printStackTrace();
                return "Could not reach server.";
            } catch (IOException e) {
                Log.e("error:", e.toString());
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

            if (!response.equals("Could not reach server.") && !response.equals("Error reaching server.")) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    String message = jsonObject.getString("message");

                    if (status.equals("Success")) {

                        String sessionid = jsonObject.getString("sessionID");
                        isUserLoggedIn = "true," + sessionid;
                        FileOutputStream fileOutputStream = openFileOutput(isUserLoggedInFileName,
                                Context.MODE_PRIVATE);
                        fileOutputStream.write(isUserLoggedIn.getBytes());
                        fileOutputStream.close();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("sessionid", sessionid);

                        Bundle bundle = new Bundle();
                        bundle.putString("ipAddress", ipAddress);
                        intent.putExtra("bundle", bundle);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Check if the user has a valid session on the server.
    private class CheckSession extends AsyncTask<Void, Void, String> {

        Context ctx;
        String sessionid;
        String isUserLoggedInFileName;

        CheckSession(Context ctx, String sessionid){
            this.ctx = ctx;
            this.sessionid = sessionid;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.isUserLoggedInFileName = "isUserLoggedIn";
        }

        @Override
        protected String doInBackground(Void... params) {
            String url_string = "https://" + ipAddress + "";

            try {
                URL url = new URL(url_string);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String data = URLEncoder.encode("sessionid", "UTF-8") + "=" + URLEncoder.encode(this.sessionid, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                String response = "";
                String line = "";
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                Log.e("response", "response 0: " + response);
                if(!response.equals("")) {

                    return response;
                } else {
                    return "Error reaching server.";
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Could not reach server.";
            } catch (ProtocolException e) {
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
            if (!response.equals("Could not reach server.") && !response.equals("Error reaching server.")) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");

                    // If the user's session is active on the server, sign them in to the app and
                    // end the login activity.
                    if (status.equals("Success")) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("ipAddress", ipAddress);

                        intent.putExtra("sessionid", sessionid);
                        intent.putExtra("bundle", bundle);
                        startActivity(intent);
                        finish();
                    } else {
                        String isUserLoggedIn = "false,0";
                        try {
                            FileOutputStream fileOutputStream = ctx.openFileOutput(isUserLoggedInFileName,
                                    Context.MODE_PRIVATE);
                            fileOutputStream.write(isUserLoggedIn.getBytes());
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        checkSessionImage.setVisibility(View.GONE);
                        mainLayout.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                    String isUserLoggedIn = "false,0";
                    try {
                        FileOutputStream fileOutputStream = ctx.openFileOutput(isUserLoggedInFileName,
                                Context.MODE_PRIVATE);
                        fileOutputStream.write(isUserLoggedIn.getBytes());
                        fileOutputStream.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    checkSessionImage.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);
                }

            } else {
                checkSessionImage.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
                Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
