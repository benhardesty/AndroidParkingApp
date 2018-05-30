/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import org.json.JSONArray;
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
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapLoadedCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {

    String ipAddress = null;

    LocationRequest locationRequest;
    Boolean mRequestingLocationUpdates = false;
    Marker placeMarker;
    private static View view;
    GoogleApiClient mGoogleApiClient;
    private GoogleMap gMap;
    TextView monthlyButton;
    TextView dailyButton;
    TextView hourlyButton;
    TextView allSpotsButton;

    String filterType;
    Double minPrice = 0.00;
    Double maxPrice = 750.00;

    List<SpotMarker> spotMarkers = new ArrayList<SpotMarker>();

    Location mLastLocation;
    SupportMapFragment sMapFragment;
    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;

    LatLng usersLastLocation;
    Float usersLastZoomLevel;

    TextView autocompleteWidget;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;
    ImageView removeSearchedPlace;

    MainActivity mainActivity;

    private CrystalRangeSeekbar rangeSeekbar;
    TextView tvPrices;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle("Parking App");

        mainActivity = (MainActivity) getActivity();
        ipAddress = mainActivity.ipAddress;

        // Initialize GoogleApiClient, used for location services, etc.
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(15);

        // Initialize Map Fragment
        sMapFragment = SupportMapFragment.newInstance();
        sMapFragment.getMapAsync(this);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().add(R.id.map, sMapFragment).commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }

        try {
            view = inflater.inflate(R.layout.fragment_map, container, false);
        } catch (InflateException e) {

        }

        // get seekbar from view
        rangeSeekbar = (CrystalRangeSeekbar) view.findViewById(R.id.rangeSeekbar1);
        rangeSeekbar.setRotation(-90);
        rangeSeekbar.setMinStartValue(Float.valueOf(String.valueOf(minPrice)));
        rangeSeekbar.setMaxStartValue(Float.valueOf(String.valueOf(maxPrice)));
        rangeSeekbar.apply();

        // get min and max text view
        tvPrices = (TextView) view.findViewById(R.id.prices);
        tvPrices.setText(String.valueOf(minPrice) + " - " + String.valueOf(maxPrice));

        // Set final status listener (prices selected)
        rangeSeekbar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                tvPrices.setText(String.valueOf(minValue) + " - " + String.valueOf(maxValue));
                setMaxAndMinPrice(minValue, maxValue);
            }
        });

        // Set change listener (change values while scrolling)
        rangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                tvPrices.setText("$" + String.valueOf(minValue) + " - $" + String.valueOf(maxValue));
            }
        });

        // Initialize Google Search Autocomplete Widget
        autocompleteWidget = (TextView) view.findViewById(R.id.map_fragment_autocomplete_widget);
        autocompleteWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        autocompleteWidget.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (autocompleteWidget.getText() != "Search") {
                    removeSearchedPlace.setVisibility(View.VISIBLE);
                } else {
                    removeSearchedPlace.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Initialize button to remove a place market on the map
        removeSearchedPlace = (ImageView) view.findViewById(R.id.remove_searched_place);
        removeSearchedPlace.setVisibility(View.INVISIBLE);
        removeSearchedPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autocompleteWidget.setText("Search");
                if (placeMarker != null) {
                    placeMarker.remove();
                }
                removeSearchedPlace.setVisibility(View.INVISIBLE);
            }
        });

        // My Location Button click listener
        ImageView MyLocationButton = (ImageView) view.findViewById(R.id.my_location_button);
        MyLocationButton.setOnClickListener(new ImageView.OnClickListener(){
            @Override
            public void onClick(View v) {
                MyLocation(v);
            }
        });

        monthlyButton = (TextView) view.findViewById(R.id.monthly_button);
        dailyButton = (TextView) view.findViewById(R.id.daily_button);
        hourlyButton = (TextView) view.findViewById(R.id.hourly_button);
        allSpotsButton = (TextView) view.findViewById(R.id.all_spots_button);

        filterType = "All";
        allSpotsButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_dark));

        monthlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Change seekbar range according to spot type so larger/smaller values are easier to select
                rangeSeekbar.setMaxValue(750);
                rangeSeekbar.setMinStartValue(Float.valueOf(String.valueOf(minPrice)));
                rangeSeekbar.setMaxStartValue(Float.valueOf(String.valueOf(maxPrice)));
                rangeSeekbar.apply();

                filterType = "Monthly";
                monthlyButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_dark));
                dailyButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_light));
                hourlyButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_light));
                allSpotsButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_light));
                LoadSpots();
            }
        });

        dailyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Change seekbar range according to spot type so larger/smaller values are easier to select
                rangeSeekbar.setMaxValue(150);
                if (maxPrice > 150) {
                    maxPrice = 150.00;
                }
                rangeSeekbar.setMinStartValue(Float.valueOf(String.valueOf(minPrice)));
                rangeSeekbar.setMaxStartValue(Float.valueOf(String.valueOf(maxPrice)));
                rangeSeekbar.apply();

                filterType = "Daily";
                monthlyButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_light));
                dailyButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_dark));
                hourlyButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_light));
                allSpotsButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_light));
                LoadSpots();
            }
        });

        hourlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Change seekbar range according to spot type so larger/smaller values are easier to select
                rangeSeekbar.setMaxValue(75);
                if (maxPrice > 75) {
                    maxPrice = 75.00;
                }
                rangeSeekbar.setMinStartValue(Float.valueOf(String.valueOf(minPrice)));
                rangeSeekbar.setMaxStartValue(Float.valueOf(String.valueOf(maxPrice)));
                rangeSeekbar.apply();

                filterType = "Hourly";
                monthlyButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_light));
                dailyButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_light));
                hourlyButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_dark));
                allSpotsButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_light));
                LoadSpots();
            }
        });

        allSpotsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Change seekbar range according to spot type so larger/smaller values are easier to select
                rangeSeekbar.setMaxValue(750);
                rangeSeekbar.setMinStartValue(Float.valueOf(String.valueOf(minPrice)));
                rangeSeekbar.setMaxStartValue(Float.valueOf(String.valueOf(maxPrice)));
                rangeSeekbar.apply();

                filterType = "All";
                monthlyButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_light));
                dailyButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_light));
                hourlyButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_light));
                allSpotsButton.setBackground(getActivity().getResources().getDrawable(R.drawable.rounded_corners_gray_opacity_dark));
                LoadSpots();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        // Connect to GoogleApiClient
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        // Disconnect from GoogleApiClient
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        setUsersLastLocationOnMap();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    protected void startLocationUpdates() {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            mRequestingLocationUpdates = true;
        }
    }

    private void setMaxAndMinPrice(Number minValue, Number maxValue) {
        minPrice = Double.valueOf(String.valueOf(minValue));
        maxPrice = Double.valueOf(String.valueOf(maxValue));
        LoadSpots();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Initialize Google Map

        gMap = googleMap;
        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setMapToolbarEnabled(false);
        gMap.getUiSettings().setMyLocationButtonEnabled(false);

        LatLng location;
        CameraPosition cameraPosition;

        // If user has granted location permission, enable My Location
        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);

            // If user's last location is known, zoom to it
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation != null) {
                location = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

            } else { // If user's last location is not known, zoom to either the last location the user had on the map or to over San Francisco\

                if (getUsersLastLocationOnMap()) // If the user has opened the map before, get the last location that the map was at
                {
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(usersLastLocation, usersLastZoomLevel));
                }
                else // If the user has never used the map, initialize the map over San Francisco
                {
                    location = new LatLng(37.7749, -122.4194);
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
                }
            }
        } else { // If user has not granted location permission, request it

            if (getUsersLastLocationOnMap()) // If the user has opened the map before, get the last location that the map was at
            {
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(usersLastLocation, usersLastZoomLevel));
            }
            else // If the user has never used the map, initialize the map over San Francisco
            {
                location = new LatLng(37.7749, -122.4194);
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
            }

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }

        gMap.setOnMapLoadedCallback(this);
        gMap.setOnCameraIdleListener(this);
        gMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);

                if (placeMarker != null) {
                    placeMarker.remove();
                }

                LatLng latlng = place.getLatLng();
                CameraPosition cameraPosition = CameraPosition.fromLatLngZoom(latlng, 15);
                gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                placeMarker = gMap.addMarker(new MarkerOptions().position(latlng));

                autocompleteWidget.setText(place.getAddress().toString());
            }
        }
    }

    /*
     * RETURN: Whether or not the user's last location is known
     */
    public boolean getUsersLastLocationOnMap() {
        Boolean lastLocationFound = false;

        String locationFoundFileName = "lastLocationFound";

        try {
            String response = "";
            String line = "";
            FileInputStream inputStream = getActivity().openFileInput(locationFoundFileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
            while ((line = bufferedReader.readLine()) != null) {
                response += line;
            }
            bufferedReader.close();
            inputStream.close();

            if (!response.equals("")) {
                String[] locationData = response.split(",");
                String locationFound = locationData[0];
                String latitude = locationData[1];
                String longitude = locationData[2];
                String zoom = locationData[3];

                if (locationFound.equals("true")) {
                    Double lat = Double.valueOf(latitude);
                    Double lng = Double.valueOf(longitude);
                    usersLastZoomLevel = Float.valueOf(zoom);
                    usersLastLocation = new LatLng(lat, lng);
                    lastLocationFound = true;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lastLocationFound;
    }

    /*
     * Set the user's last location on the map
     */
    public void setUsersLastLocationOnMap()
    {
        String locationFoundFileName = "lastLocationFound";

        try {

            String lat = String.valueOf(gMap.getCameraPosition().target.latitude);
            String lng = String.valueOf(gMap.getCameraPosition().target.longitude);
            String zoom = String.valueOf(gMap.getCameraPosition().zoom);
            String lastLocationData = "true," + lat + "," + lng + "," + zoom;
            FileOutputStream fileOutputStream = getActivity().openFileOutput(locationFoundFileName, Context.MODE_PRIVATE);
            fileOutputStream.write(lastLocationData.getBytes());
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Update user's location
    */
    public void MyLocation(View view) {

        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Attempt to get lastest location
            Location tempLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            // If latest location is available, update the mLastLocation variable
            if(tempLocation != null) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }

            // If mLatestLocation is not null
            if(mLastLocation != null) {
                LatLng location = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                CameraPosition cameraPosition = CameraPosition.fromLatLngZoom(location, 15);
                gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            } else { // Otherwise alert the user that no location is found
                Toast.makeText(getActivity(), "Current location not found", Toast.LENGTH_SHORT).show();
            }

        } else { // If user has not granted location permission, request it
            Toast.makeText(getActivity(), "You must enable location permission to access your location", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }
    }

    /*
        Request permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {

            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {

                // Note: If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Enable My Location
                    gMap.setMyLocationEnabled(true);

                    // Search for user's last known location
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    // Zoom to user's last known location if found
                    if(mLastLocation != null) {
                        LatLng location = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        CameraPosition cameraPosition = CameraPosition.fromLatLngZoom(location, 15);
                        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                }

                return;
            }
        }
    }

    /*
        When GoogleApiClient is connected
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // Request location updates
        startLocationUpdates();

        // Variables to hold location and cameraPosition
        LatLng location;
        CameraPosition cameraPosition;

        // If user has granted location permission
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // If user's last location is known, zoom to it
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation != null && view == null) {
                location = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                cameraPosition = CameraPosition.fromLatLngZoom(location, 15);
                gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }

        } else if (view != null) { // If user has not granted location permission

            // Set default location to San Francisco
            location = new LatLng(37.7749, -122.4194);
            cameraPosition = CameraPosition.fromLatLngZoom(location, 10);
            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            // Request permission for user location
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        // Required by GoogleApiClient. Do nothing.
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Required by GoogleApiClient. Do nothing.
    }

    /*
        Handle location updates
     */
    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            mLastLocation = location;
        }
    }

    // When map finishes initial load, check for new spots
    @Override
    public void onMapLoaded() {
        LoadSpots();
    }

    // When user finishes moving map, check for new spots
    @Override
    public void onCameraIdle() {
        LoadSpots();
    }

    // Load new spots on the map
    public void LoadSpots() {
        LatLngBounds bounds = gMap.getProjection().getVisibleRegion().latLngBounds;
        double nelat = bounds.northeast.latitude;
        double swlat = bounds.southwest.latitude;
        BackgroundTask backgroundTask = new BackgroundTask(getActivity());
        backgroundTask.execute(nelat, swlat);
    }

    // When a spot market is clicked, load the spot details in a new view
    @Override
    public boolean onMarkerClick(Marker marker) {
        SpotMarker spotMarker = (SpotMarker) marker.getTag();
        Bundle bundle = new Bundle();
        bundle.putString("spotID", spotMarker.getID().toString());
        bundle.putString("sessionid", mainActivity.sessionid.toString());
        bundle.putString("ipAddress", ipAddress);
        bundle.putString("filterType", filterType);

        Intent intent = new Intent(getActivity(), ReserveSpotActivity.class);
        intent.putExtra("bundle", bundle);
        startActivity(intent);

        return false;
    }

    private class BackgroundTask extends AsyncTask<Double, Void, String> {

        Context ctx;
        List<Integer> indexToAddToMap = new ArrayList<Integer>();
        List<Integer> indexOfNewSpotsInSpotsArray = new ArrayList<Integer>();

        Double minSpotPrice;
        Double maxSpotPrice;

        BackgroundTask(Context ctx){
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Double... params) {
            double nelat = params[0];
            double swlat = params[1];
            String url_string = "https://" + ipAddress + "/";

            try {
                URL url = new URL(url_string);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String data = URLEncoder.encode("nelat", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(nelat), "UTF-8") + "&" +
                        URLEncoder.encode("swlat", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(swlat), "UTF-8") + "&" +
                        URLEncoder.encode("filterType", "UTF-8") + "=" + URLEncoder.encode(filterType, "UTF-8") + "&" +
                        URLEncoder.encode("max", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(maxPrice), "UTF-8") + "&" +
                        URLEncoder.encode("min", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(minPrice), "UTF-8");
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
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");

                    if (status.equals("Success")) {
                        JSONArray jsonData = jsonObject.getJSONArray("data");

                        for (int i = 0; i < jsonData.length(); i++) {
                            JSONObject spot = jsonData.getJSONObject(i);

                            SpotMarker spotMarker = new SpotMarker(spot);

                            if (spotMarkers.contains(spotMarker)) {
                                // If spotsArray already contains spot,

                                // Add the spot's index in the spotsMarkers list to the
                                // indexOfNewSpotsInSpotsArray
                                indexOfNewSpotsInSpotsArray.add(spotMarkers.indexOf(spotMarker));
                            } else {

                                // If spotMarkers doesn't contain spot, add the spot to the spotMarkers list
                                // and add the index of the newly added spot in the spotMarkers list to the
                                // indexToAddToMap list

                                spotMarkers.add(spotMarker);
                                int index = spotMarkers.indexOf(spotMarker);
                                indexToAddToMap.add(index);

                                // Add the spot's index in the spotMarkers to the
                                // indexOfNewSpotsInSpotsArray
                                indexOfNewSpotsInSpotsArray.add(index);

                            }
                        }

                        return "Success";
                    } else if (status.equals("error")) {
                        String message = jsonObject.getString("message");
                        if (message.equals("No data.")) {
                            return "Clear markers";
                        } else {
                            return "No data";
                        }
                    } else {
                        String message = "No data";
                        return message;
                    }
                } else {
                    return "Could not reach server.";
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Error reaching server.";
            } catch (IOException e) {
                e.printStackTrace();
                return "Could not reach server.";

            } catch (JSONException e) {
                e.printStackTrace();
                return "Error reaching server.";
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("Success")) {

                for (int i = 0; i < indexToAddToMap.size(); i++) {
                    spotMarkers.get(indexToAddToMap.get(i)).setMarker(gMap.addMarker(new MarkerOptions().position(spotMarkers.get(indexToAddToMap.get(i)).getLatLng()).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_parking))));
                    spotMarkers.get(indexToAddToMap.get(i)).setMarkerTag(spotMarkers.get(indexToAddToMap.get(i)));
                }

                int indexAdjustment = 0;

                String spotIDs = "(";

                for (int i = 0; i < spotMarkers.size(); i++) {

                    // Get spot IDs to update availability for spots
                    Integer ID = spotMarkers.get(i).getID();
                    if (i == spotMarkers.size() - 1) {
                        spotIDs = spotIDs + " '" + ID + "')";
                    } else if (i == 0) {
                        spotIDs = spotIDs + "'" + ID + "',";
                    } else {
                        spotIDs = spotIDs + " '" + ID + "',";
                    }

                    // If the index of the spot in spotMarkers is one of the indexes
                    // that was added to the IndexOfNewSpotsInSpotsArray, then it was one
                    // of the spots returned by the server - do nothing
                    if (indexOfNewSpotsInSpotsArray.contains(i + indexAdjustment)) {
                        // do nothing
                    } else {
                        // Otherwise, remove the spot
                        spotMarkers.get(i).removeMarker(spotMarkers.get(i));
                        spotMarkers.remove(i);
                        indexAdjustment++;
                        i--;
                    }
                }

                indexToAddToMap.clear();
                indexOfNewSpotsInSpotsArray.clear();

            } else {

                for (int i = 0; i < spotMarkers.size(); i++) {
                    spotMarkers.get(i).removeMarker(spotMarkers.get(i));
                    spotMarkers.remove(i);
                    i--;
                }

                indexToAddToMap.clear();
                indexOfNewSpotsInSpotsArray.clear();
            }
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

}
