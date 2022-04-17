package com.project.smartlocationreminder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class LocationService extends Service {

    FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    boolean isLAlreadyZoom;
    DatabaseReference mRef;
    GeoFire geoFire;
   public static GeoQuery geoQuery;
    List<Reminder> list;
    Context context;
    List<Reminder> listCurrentNotification;

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.context = this;
        listCurrentNotification = new ArrayList<>();
        mRef = FirebaseDatabase.getInstance().getReference("MyLocation").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        geoFire = new GeoFire(mRef);
        LoadReminders();
        AddMarkerOnMap();

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;


    }

    private void LoadReminders() {
        list = new ArrayList<>();
        list.clear();
        list = new MyDb(this).getReminderList();
    }

    private void AddMarkerOnMap() {
        if (MainActivity.mMap != null) {
            //add new marker list,clear previous one
            MainActivity.mMap.clear();

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
        circleOptions.radius(MainActivity.radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        MainActivity.mMap.addCircle(circleOptions);
        MainActivity.mMap.addMarker(new MarkerOptions().title("Me").position(new LatLng(reminder.getLatitude(), reminder.getLongitude())).snippet(reminder.getLocation_id()));

        float rad = MainActivity.radius / 1000f;
        geoQuery = geoFire.queryAtLocation(new GeoLocation(reminder.getLatitude(), reminder.getLongitude()), rad);
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {

                if (!isNotificationAlreadyExist(reminder)) {
                    listCurrentNotification.add(reminder);
                    PerformNotification(reminder, location);
                }
            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

                for (int i = 0; i < listCurrentNotification.size(); i++) {
                    if (listCurrentNotification.get(i).getLocation_id().equals(reminder.getLocation_id())) {
                        listCurrentNotification.remove(i);
                    }
                }
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

    private boolean isNotificationAlreadyExist(Reminder reminder) {
        for (int i = 0; i < listCurrentNotification.size(); i++) {
            if (listCurrentNotification.get(i).getLocation_id().equals(reminder.getLocation_id())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Notification();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            createNotificationChanel();
        else startForeground(
                1,
                new Notification()
        );

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setMaxWaitTime(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {

                if (MainActivity.mMap != null) {
                    geoFire.setLocation("Me", new GeoLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (!isLAlreadyZoom) {
                                MainActivity.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()), 20));
                                isLAlreadyZoom = true;
                            }
                        }
                    });
                }
            }
        };
        startLocationUpdates();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChanel() {
        String notificationChannelId = "Location channel id";
        String channelName = "Background Service";

        NotificationChannel chan = new NotificationChannel(
                notificationChannelId,
                channelName,
                NotificationManager.IMPORTANCE_NONE
        );
        chan.setLightColor(Color.RED);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = getSystemService(NotificationManager.class);

        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, notificationChannelId);

        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Location updates:")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }


    private void PerformNotification(Reminder reminder, GeoLocation location) {
        Intent notificationIntent = new Intent(context, ListReminderActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        String channelId = "some_channel_id";
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(reminder.getTitle())
                        .setContentText("Date: " + reminder.getDate() + "\n" + "Time: " + reminder.getTime())
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent)
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

//        assert notificationManager != null;
        notificationManager.notify(reminder.getLocation_id(), 0, notificationBuilder.build());
        // AddMarkerOnMap();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
