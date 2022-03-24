package com.project.smartlocationreminder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.security.Permissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SeekBar.OnSeekBarChangeListener, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    boolean isPermissionGranted;
    SeekBar seekBar;
    TextView radiusValue;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView userEmail;
    ImageView homeMenu;
    FloatingActionButton fabAddLocation;
    FloatingActionButton fabDeleteLocation;
    List<Reminder> list;
    String selectedReminderId;
    String selectedReminderTitle;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitMemberVaribale();
        seekBar.setOnSeekBarChangeListener(this);
        GetSeekBarValue();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        requestPermission();
        homeMenu.setOnClickListener(this);
        fabAddLocation.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, AddReminderActivity.class)));
        fabDeleteLocation.setOnClickListener(view -> DeleteReminder());

        navigationView.setNavigationItemSelectedListener(this);
        statusCheck();
        LoadReminders();

    }

    private void DeleteReminder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Alert!!!");
        builder.setMessage("Are you sure you want to delete?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new MyDb(MainActivity.this).deleteReminder(selectedReminderId);
                LoadReminders();
                AddMarkerOnMap();
                fabDeleteLocation.setVisibility(View.GONE);
            }
        }).setNegativeButton("No", null);
        builder.create();
        builder.show();

    }

    private void LoadReminders() {
        list = new ArrayList<>();
        list.clear();
        list = new MyDb(MainActivity.this).getReminderList();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void InitMemberVaribale() {

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_View);
        homeMenu = findViewById(R.id.homeMenu);
        View view = navigationView.inflateHeaderView(R.layout.header_layout);

        seekBar = view.findViewById(R.id.seekBar);
        radiusValue = view.findViewById(R.id.radiusValue);
        userEmail = view.findViewById(R.id.userEmail);

        userEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        seekBar.setMin(1);
        seekBar.setMax(500);

        fabAddLocation = findViewById(R.id.addLocationReminder);
        fabDeleteLocation = findViewById(R.id.deleteLocationReminder);
        fabDeleteLocation.setVisibility(View.GONE);

    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        if (isPermissionGranted) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestPermission();
        }
        AddMarkerOnMap();
        mMap.setOnMapClickListener(latLng -> {
            fabDeleteLocation.setVisibility(View.GONE);
        });
        mMap.setOnMarkerClickListener(marker -> {
            marker.hideInfoWindow();
            selectedReminderId = marker.getSnippet();
            selectedReminderTitle = marker.getTitle();
            fabDeleteLocation.setVisibility(View.VISIBLE);
            return true;
        });
    }

    private void AddMarkerOnMap() {
        mMap.clear();
        for (int i = 0; i < list.size(); i++) {
            AddMarker(list.get(i));
            addCircle(list.get(i));
        }
    }

    private void AddMarker(Reminder reminder) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(reminder.getTitle());
        markerOptions.position(new LatLng(reminder.getLatitude(), reminder.getLongitude()));
        markerOptions.snippet(reminder.getLocation_id());
        mMap.addMarker(markerOptions);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            AddMarkerOnMap();
        }
    }

    private void addCircle(Reminder reminder) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(new LatLng(reminder.getLatitude(), reminder.getLongitude()));
        circleOptions.radius(200);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Permission Required")
                        .setMessage("Location permission required!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        111);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        111);
            }
        } else {
            isPermissionGranted = true;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        isPermissionGranted = true;
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        SaveSeekBarValue(i);
        GetSeekBarValue();
    }

    private void SaveSeekBarValue(int value) {
        SharedPreferences.Editor editor = getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE).edit();
        editor.putInt("seekBar", value);
        editor.apply();
    }

    private void GetSeekBarValue() {
        SharedPreferences prefs = getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE);
        int seekBarValue = prefs.getInt("seekBar", 0);
        radiusValue.setText(seekBarValue + "m");
        seekBar.setProgress(seekBarValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.homeMenu) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addLocationReminder) {
            startActivity(new Intent(MainActivity.this, AddReminderActivity.class));
        }
        if (item.getItemId() == R.id.listReminder) {
            startActivity(new Intent(MainActivity.this, ListReminderActivity.class));
        }
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        return false;
    }


//    protected void enableLocationSettings() {
//        LocationRequest locationRequest = LocationRequest.create()
//                .setInterval(10 * 1000)
//                .setFastestInterval(2 * 1000)
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
//                .addLocationRequest(locationRequest);
//
//        LocationServices
//                .getSettingsClient(this)
//                .checkLocationSettings(builder.build())
//                .addOnSuccessListener(this, (LocationSettingsResponse response) -> {
//                    // startUpdatingLocation(...);
//                })
//                .addOnFailureListener(this, ex -> {
//                    if (ex instanceof ResolvableApiException) {
//                        // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
//                        try {
//                            // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
//                            ResolvableApiException resolvable = (ResolvableApiException) ex;
//                            resolvable.startResolutionForResult(TrackingListActivity.this, REQUEST_CODE_CHECK_SETTINGS);
//                        } catch (IntentSender.SendIntentException sendEx) {
//                            // Ignore the error.
//                        }
//                    }
//                });
//    }
}