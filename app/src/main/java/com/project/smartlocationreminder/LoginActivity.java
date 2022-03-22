package com.project.smartlocationreminder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.LocusId;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    TextView createNewAccountTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        createNewAccountTv = findViewById(R.id.createNewAccountTv);
        createNewAccountTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.createNewAccountTv) {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        }
    }
}