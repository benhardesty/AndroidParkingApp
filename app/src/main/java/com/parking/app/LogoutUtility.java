/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


public class LogoutUtility extends AsyncTask<Void, Void, String> {

    Context ctx;
    String isUserLoggedInFileName;
    String sessionid;

    LogoutUtility(Context ctx, String sessionid) { this.ctx = ctx; this.sessionid = sessionid; }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.isUserLoggedInFileName = "isUserLoggedIn";
    }

    @Override
    protected String doInBackground(Void... params) {
        String url_string = "https://";
        try {
            URL url = new URL(url_string);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String data = URLEncoder.encode("sessionid", "UTF-8")+"="+URLEncoder.encode(sessionid, "UTF-8");
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
        if (!response.equals("Error reaching server.")) {
            try {
                JSONObject responseJSON = new JSONObject(response);
                String status = responseJSON.getString("status");
                if (status.equals("Success")) {
                    String isUserLoggedIn = "false,0";
                    try {
                        FileOutputStream fileOutputStream = ctx.openFileOutput(isUserLoggedInFileName, Context.MODE_PRIVATE);
                        fileOutputStream.write(isUserLoggedIn.getBytes());
                        fileOutputStream.close();

                        Intent intent = new Intent(ctx, LoginActivity.class);
                        ctx.startActivity(intent);
                        ((Activity) ctx).finish();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    String message = responseJSON.getString("message");
                    if (message.equals("Please log in.")) {
                        String isUserLoggedIn = "false,0";
                        try {
                            FileOutputStream fileOutputStream = ctx.openFileOutput(isUserLoggedInFileName, Context.MODE_PRIVATE);
                            fileOutputStream.write(isUserLoggedIn.getBytes());
                            fileOutputStream.close();

                            Intent intent = new Intent(ctx, LoginActivity.class);
                            ctx.startActivity(intent);
                            ((Activity) ctx).finish();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                String message = "Error reaching server.";
                Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ctx, response, Toast.LENGTH_SHORT).show();
        }
    }

}
