/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;



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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyReservationsFragment extends Fragment {

    String ipAddress;

    private static View view;
    ListView listView;
    MainActivity mainActivity;
    TextView noDataTextView;

    ArrayAdapter<String> reservationsAdapter;
    List<Reservation> reservations = new ArrayList<Reservation>();
    String reservationsAddresses[];

    List<Map<String, String>> reservationsList = new ArrayList<Map<String, String>>();
    SimpleAdapter simpleAdapter;

    public MyReservationsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        reservations.clear();
        reservationsList.clear();
        simpleAdapter = new SimpleAdapter(getActivity(), reservationsList, android.R.layout.simple_list_item_2, new String[]{"Address", "Dates"}, new int[]{android.R.id.text1, android.R.id.text2});
        listView.setAdapter(simpleAdapter);
        BackgroundTask backgroundTask = new BackgroundTask(getActivity());
        backgroundTask.execute();
        super.onResume();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        getActivity().setTitle("My Reservations");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }

        try {
            view = inflater.inflate(R.layout.fragment_my_reservations, container, false);
        } catch (InflateException e) {

        }

        listView = (ListView) view.findViewById(R.id.list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("reservation", reservations.get(position));
                bundle.putString("sessionid", mainActivity.sessionid.toString());
                bundle.putString("previousActivity", "MyReservationsFragment");
                bundle.putString("ipAddress", ipAddress);

                Intent intent = new Intent(getActivity(), MyReservationActivity.class);
                intent.putExtra("bundle", bundle);
                startActivity(intent);
            }
        });

        noDataTextView = (TextView) view.findViewById(R.id.no_data_text_view);

        mainActivity = (MainActivity) getActivity();
        ipAddress = mainActivity.ipAddress;

        return view;
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

    private class BackgroundTask extends AsyncTask<Void, Void, String> {

        Context ctx;
        BackgroundTask(Context ctx) { this.ctx = ctx; }
        String sessionid;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.sessionid = mainActivity.sessionid.toString();
            noDataTextView.setText("");
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

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("Success")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject reservationObject = jsonArray.getJSONObject(i);
                                Reservation reservation = new Reservation(reservationObject);
                                reservations.add(reservation);
                            }
                            reservationsAddresses = new String[reservations.size()];

                            reservationsList.clear();

                            for (int i = 0; i < reservations.size(); i++) {
                                Map<String, String> pair = new HashMap<String, String>(2);
                                String start = reservations.get(i).getStart();
                                String end = reservations.get(i).getEnd();

                                String reservationDates = "";

                                String reservationType = reservations.get(i).getReservationType();
                                switch (reservationType) {
                                    case "Monthly":
                                        reservationDates = "Monthly: Starting " + start.split("/")[1] + "/" + start.split("/")[2] + "/" + start.split("/")[0];
                                        pair.put("Address", reservations.get(i).getSpotAddress());
                                        pair.put("Dates", reservationDates);
                                        reservationsList.add(pair);
                                        break;
                                    case "Daily":
                                        reservationDates = "Daily: " + start.split("/")[1] + "/" + start.split("/")[2] + "/" + start.split("/")[0] + " - " + end.split("/")[1] + "/" + end.split("/")[2] + "/" + end.split("/")[0];
                                        pair.put("Address", reservations.get(i).getSpotAddress());
                                        pair.put("Dates", reservationDates);
                                        reservationsList.add(pair);
                                        break;
                                    case "Hourly":
                                        String startTime = convert24to12(start.split("/")[3] + "/" + start.split("/")[4]);
                                        String endTime = convert24to12(end.split("/")[3] + "/" + end.split("/")[4]);

                                        Date todaysDate = new Date();
                                        Integer currentTime = Integer.valueOf(pad(String.valueOf(todaysDate.getHours())) + "" + pad(String.valueOf(todaysDate.getMinutes())));
                                        Integer startTimeInteger = Integer.valueOf(start.split("/")[3] + "" + start.split("/")[4]);
                                        Integer endTimeInteger = Integer.valueOf(end.split("/")[3] + "" + end.split("/")[4]);

                                        if (endTimeInteger <= currentTime) {
                                            reservations.remove(i);
                                            i--;
                                            break;
                                        }

                                        reservationDates = "Hourly: " + start.split("/")[1] + "/" + start.split("/")[2] + "/" + start.split("/")[0] + " between " + startTime + " - " + endTime;
                                        pair.put("Address", reservations.get(i).getSpotAddress());
                                        pair.put("Dates", reservationDates);
                                        reservationsList.add(pair);
                                        break;
                                    default:
                                        break;
                                }
                            }

                            simpleAdapter = new SimpleAdapter(getActivity(), reservationsList, android.R.layout.simple_list_item_2, new String[]{"Address", "Dates"}, new int[]{android.R.id.text1, android.R.id.text2});

                            return "Success";
                        } else {
                            return jsonObject.getString("message");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        return "Error reaching server.";
                    }
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
            if (response.equals("Success")) {

                listView.setAdapter(simpleAdapter);
            } else if (response.equals("Please log in.")) {
                LogoutUtility logoutUtility = new LogoutUtility(ctx, sessionid);
                logoutUtility.execute();
            } else {
                noDataTextView.setText("No reservations");
            }
        }
    }

}
