/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

// View used to manager a user's payment methods.
public class ManagePaymentActivity extends AppCompatActivity {

    String ipAddress;
    String sessionid;

    Card card;
    TextView cardStringTextView;
    Button makeDefaultButton;
    Button deleteButton;
    Boolean buttonsAvailable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_payment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Manage Payment Method");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cardStringTextView = (TextView) findViewById(R.id.card_string);
        makeDefaultButton  = (Button) findViewById(R.id.make_card_default);
        deleteButton = (Button) findViewById(R.id.delete_button);

        makeDefaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeCardDefault();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCard();
            }
        });

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        sessionid = bundle.getString("sessionid");
        ipAddress = bundle.getString("ipAddress");
        card = bundle.getParcelable("card");

        cardStringTextView.setText(card.getCardString());
    }

    // Update a user's default credit card.
    void makeCardDefault() {
        if (!buttonsAvailable)
        {
            return;
        }

        buttonsAvailable = false;

        final AlertDialog pendingDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(ManagePaymentActivity.this);
        builder.setMessage("Please wait...")
                .setTitle("Status")
                .setCancelable(false);
        pendingDialog = builder.create();
        pendingDialog.show();

        try {
            String data = URLEncoder.encode("sessionid", "UTF-8") + "=" + URLEncoder.encode(sessionid, "UTF-8") + "&" +
                    URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(card.getToken(), "UTF-8");
            AsyncUtility asyncUtility = new AsyncUtility(this, "https://"+ipAddress+"/", data, new AsyncUtility.AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    if (!output.equals("Error reaching server.")) {

                        try {
                            JSONObject jsonObject = new JSONObject(output);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");

                            if (status.equals("Success")) {
                                pendingDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(ManagePaymentActivity.this);
                                builder.setMessage(message)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                                buttonsAvailable = true;
                            } else {
                                if (message.equals("Please log in.")) {
                                    LogoutUtility logoutUtility = new LogoutUtility(ManagePaymentActivity.this, sessionid);
                                    logoutUtility.execute();
                                } else {
                                    pendingDialog.dismiss();
                                    Toast.makeText(ManagePaymentActivity.this, message, Toast.LENGTH_LONG).show();
                                    buttonsAvailable = true;
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            pendingDialog.dismiss();
                            Toast.makeText(ManagePaymentActivity.this, "Error reaching server", Toast.LENGTH_LONG).show();
                            buttonsAvailable = true;
                        }


                    } else {
                        pendingDialog.dismiss();
                        Toast.makeText(ManagePaymentActivity.this, output, Toast.LENGTH_SHORT).show();
                        buttonsAvailable = true;
                    }
                }
            });
            asyncUtility.execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            pendingDialog.dismiss();
            buttonsAvailable = true;
        }
    }

    // Delete a user's credit card.
    void deleteCard() {
        if (!buttonsAvailable)
        {
            return;
        }

        buttonsAvailable = false;

        final AlertDialog pendingDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(ManagePaymentActivity.this);
        builder.setMessage("Please wait...")
                .setTitle("Status")
                .setCancelable(false);
        pendingDialog = builder.create();
        pendingDialog.show();

        try {
            String data = URLEncoder.encode("sessionid", "UTF-8") + "=" + URLEncoder.encode(sessionid, "UTF-8") + "&" +
                    URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(card.getToken(), "UTF-8");
            AsyncUtility asyncUtility = new AsyncUtility(this, "https://"+ipAddress+"/", data, new AsyncUtility.AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    if (!output.equals("Error reaching server.")) {

                        try {
                            JSONObject jsonObject = new JSONObject(output);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");

                            if (status.equals("Success")) {
                                pendingDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(ManagePaymentActivity.this);
                                builder.setMessage(message)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                                buttonsAvailable = true;
                            } else {
                                if (message.equals("Please log in.")) {
                                    LogoutUtility logoutUtility = new LogoutUtility(ManagePaymentActivity.this, sessionid);
                                    logoutUtility.execute();
                                } else {
                                    pendingDialog.dismiss();
                                    Toast.makeText(ManagePaymentActivity.this, message, Toast.LENGTH_LONG).show();
                                    buttonsAvailable = true;
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            pendingDialog.dismiss();
                            Toast.makeText(ManagePaymentActivity.this, "Error reaching server", Toast.LENGTH_LONG).show();
                            buttonsAvailable = true;
                        }


                    } else {
                        pendingDialog.dismiss();
                        Toast.makeText(ManagePaymentActivity.this, output, Toast.LENGTH_SHORT).show();
                        buttonsAvailable = true;
                    }
                }
            });
            asyncUtility.execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            pendingDialog.dismiss();
            buttonsAvailable = true;
        }
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
