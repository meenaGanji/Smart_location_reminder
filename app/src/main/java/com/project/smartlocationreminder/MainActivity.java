package com.project.smartlocationreminder;

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
    GeofencingClient geofencingClient;
    com.google.android.gms.location.LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    boolean isLAlreadyZoom;
    GeoFire geoFire;
    DatabaseReference mRef;
    int radius = 100;
    GeoQuery geoQuery;


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

        FirebaseApp.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire = new GeoFire(mRef);

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

        if (fusedLocationProviderClient != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }

        if (isPermissionGranted) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

            mMap.setMyLocationEnabled(true);
        } else {
            requestPermission();
        }
        AddMarkerOnMap();
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


    private void AddMarkerOnMap() {
        if (mMap != null) {
            //add new marker list,clear previous one
            mMap.clear();

            //before start new querry ,clear previous one
            if (geoQuery != null) {
                geoQuery.removeAllListeners();
            }
            for (int i = 0; i < list.size(); i++) {
                AddMarker(list.get(i));
            }
        }
    }

    private void AddMarker(Reminder reminder) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(new LatLng(reminder.getLatitude(), reminder.getLongitude()));
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
        mMap.addMarker(new MarkerOptions().title("Me").position(new LatLng(reminder.getLatitude(), reminder.getLongitude())).snippet(reminder.getLocation_id()));

        float rad = radius / 1000f;
        geoQuery = geoFire.queryAtLocation(new GeoLocation(reminder.getLatitude(), reminder.getLongitude()), rad);
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                PerformNotification(reminder, location);
            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    private void PerformNotification(Reminder reminder, GeoLocation location) {

        String channelId = "some_channel_id";

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(reminder.getTitle())
                        .setContentText("Date: " + reminder.getDate() + "\n" + "Time: " + reminder.getTime())
                        .setAutoCancel(true)
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    reminder.getTitle(),
                    NotificationManager.IMPORTANCE_DEFAULT);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }

        assert notificationManager != null;
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        AddMarkerOnMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadReminders();
        if (mMap != null) {
            mMap.clear();
            AddMarkerOnMap();
        }
    }


    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

            buildLocationRequest();
            buildLocationCallBack();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        isPermissionGranted = true;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        buildLocationRequest();
        buildLocationCallBack();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        SaveSeekBarValue(i);
        GetSeekBarValue();
        AddMarkerOnMap();
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

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (mMap != null) {
                    geoFire.setLocation("Me", new GeoLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (!isLAlreadyZoom) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()), 20));
                                isLAlreadyZoom = true;
                            }
                        }
                    });
                }
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new com.google.android.gms.location.LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }
}