/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String ipAddress;
    String sessionid;
    String isUserLoggedInFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        isUserLoggedInFileName = "isUserLoggedIn";
        Intent intent = getIntent();
        sessionid = intent.getStringExtra("sessionid");
        Bundle bundle = intent.getBundleExtra("bundle");
        ipAddress = bundle.getString("ipAddress");

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, new MapFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        FragmentManager fragmentManager = getSupportFragmentManager();

        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new MapFragment()).commit();
        } else if (id == R.id.nav_my_reservations) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new MyReservationsFragment()).commit();
        } else if (id == R.id.nav_my_pending_requests) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new MyPendingRequestsFragment()).commit();
        } else if (id == R.id.nav_payments) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new PaymentsFragment()).commit();
        } else if (id == R.id.nav_logout) {
            LogoutUtility logoutUtility = new LogoutUtility(this, sessionid);
            logoutUtility.execute();
        } else {
            Toast.makeText(this, "Fragment not published to GitHub.", Toast.LENGTH_SHORT).show();
        }

        /*
         * Fragments not published to GitHub
        else if (id == R.id.nav_gallery) {
            // fragmentManager.beginTransaction().replace(R.id.content_frame, new ListSpotFragment()).commit();
        } else if (id == R.id.nav_slideshow) {
            // fragmentManager.beginTransaction().replace(R.id.content_frame, new MyListedSpotsFragment()).commit();
        } else if (id == R.id.nav_manage) {
            // fragmentManager.beginTransaction().replace(R.id.content_frame, new RequestsPendingMyResponseFragment()).commit();
        }   else if (id == R.id.nav_transaction_history) {
            // fragmentManager.beginTransaction().replace(R.id.content_frame, new TransactionsHistoryFragment()).commit();
        } else if (id == R.id.nav_account) {
            // fragmentManager.beginTransaction().replace(R.id.content_frame, new AccountFragment()).commit();
        } else if (id == R.id.nav_owner_transactions_history) {
            // fragmentManager.beginTransaction().replace(R.id.content_frame, new OwnerTransactionsHistoryFragment()).commit();
        }   else if (id == R.id.nav_get_paid) {
            // fragmentManager.beginTransaction().replace(R.id.content_frame, new GetPaidFragment()).commit();
        } else if (id == R.id.nav_contact_us) {
            // fragmentManager.beginTransaction().replace(R.id.content_frame, new ContactUsFragment()).commit();
        }
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
