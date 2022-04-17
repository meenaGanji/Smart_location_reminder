package com.project.smartlocationreminder;

import static com.project.smartlocationreminder.LocationService.geoQuery;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SeekBar.OnSeekBarChangeListener, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    public static GoogleMap mMap;
    public static int radius = 100;
    boolean isPermissionGrantedFine;
    boolean isPermissionGrantedBackground;
    SeekBar seekBar;
    TextView radiusValue;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView userEmail;
    ImageView homeMenu;
    FloatingActionButton fabAddLocation;
    FloatingActionButton fabDeleteLocation;

    String selectedReminderId;
    String selectedReminderTitle;

    private static int MY_FINE_LOCATION_REQUEST = 99;
    private static int MY_BACKGROUND_LOCATION_REQUEST = 100;

    LocationService mLocationService = new LocationService();
    Intent mServiceIntent;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitMemberVariable();
        seekBar.setOnSeekBarChangeListener(this);
        GetSeekBarValue();

        requestPermission();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        stopServiceFunc();

        statusCheck();
        homeMenu.setOnClickListener(this);
        fabAddLocation.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, AddReminderActivity.class)));
        fabDeleteLocation.setOnClickListener(view -> DeleteReminder());

        FirebaseApp.getInstance();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void requestPermission() {
        //check fine location and background PERMISSION
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }

            stopServiceFunc();
            starServiceFunc();
        } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            isPermissionGrantedFine = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestBackgroundLocationPermission();
                }
            }
        } else {
            requestFineLocationPermission();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestBackgroundLocationPermission();
                }
            }
        }
    }


    private void DeleteReminder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Alert!!!");
        builder.setMessage("Are you sure you want to delete?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new MyDb(MainActivity.this).deleteReminder(selectedReminderId);
                geoQuery.removeAllListeners();
                stopServiceFunc();
                starServiceFunc();
                fabDeleteLocation.setVisibility(View.GONE);
            }
        }).setNegativeButton("No", null);
        builder.create();
        builder.show();

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void InitMemberVariable() {

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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        requestPermission();

        mMap.setOnMapClickListener(latLng -> {
            fabDeleteLocation.setVisibility(View.GONE);
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                selectedReminderId = marker.getSnippet();
                selectedReminderTitle = marker.getTitle();
                fabDeleteLocation.setVisibility(View.VISIBLE);
                marker.hideInfoWindow();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            mMap.clear();
            stopServiceFunc();
            requestPermission();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_FINE_LOCATION_REQUEST) {
            isPermissionGrantedFine = true;
            if (mMap != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
            }
            requestBackgroundLocationPermission();

        } else if (requestCode == MY_BACKGROUND_LOCATION_REQUEST) {
            if (isPermissionGrantedFine) {
                isPermissionGrantedBackground = true;
                stopServiceFunc();
                starServiceFunc();
            }
            //  Toast.makeText(this, "Background location Permission Granted", Toast.LENGTH_LONG).show();
        }


    }

    @SuppressLint("MissingPermission")
    private void starServiceFunc() {
        mLocationService = new LocationService();
        mServiceIntent = new Intent(this, mLocationService.getClass());
        if (!Util.isMyServiceRunning(mLocationService.getClass(), this)) {
            startService(mServiceIntent);
            //  Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        }
    }


    private void requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                MY_BACKGROUND_LOCATION_REQUEST);
    }

    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, MY_FINE_LOCATION_REQUEST);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        SaveSeekBarValue(i);
        GetSeekBarValue();
        if (mMap != null) {
            mMap.clear();
        }
        stopServiceFunc();
        requestPermission();
        // AddMarkerOnMap();
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
        radius = seekBarValue;
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

    private void stopServiceFunc() {
        mLocationService = new LocationService();
        mServiceIntent = new Intent(this, mLocationService.getClass());
        if (Util.isMyServiceRunning(mLocationService.getClass(), this)) {
            stopService(mServiceIntent);
        }
    }
}