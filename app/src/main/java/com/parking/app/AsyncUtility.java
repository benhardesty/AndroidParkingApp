/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.content.Context;
import android.os.AsyncTask;

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


public class AsyncUtility extends AsyncTask<Void, Void, String> {

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public AsyncResponse delegate = null;

    Context ctx;
    String url;
    String params;

    public AsyncUtility(Context ctx, String url, String params, AsyncResponse delegate) {
        this.ctx = ctx; this.url = url; this.params = params; this.delegate = delegate;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            URL url = new URL(this.url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String data = this.params;
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
            return "Error reaching server.";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reaching server.";
        }
    }

    @Override
    protected void onPostExecute(String response) {
        delegate.processFinish(response);
    }
}
