package com.project.smartlocationreminder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class AddReminderActivity extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap mMap;
    boolean isPermissionGranted;
    EditText searchEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);
        InitMemberVariable();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        requestPermission();
    }

    private void InitMemberVariable() {
        searchEt = findViewById(R.id.searchEt);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        if (isPermissionGranted) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestPermission();
        }
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                new AddLocationRemindDialog(AddReminderActivity.this).Save(latLng);
            }
        });
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(AddReminderActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddReminderActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(AddReminderActivity.this)
                        .setTitle("Permission Required")
                        .setMessage("Location permission required!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(AddReminderActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        111);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(AddReminderActivity.this,
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
}