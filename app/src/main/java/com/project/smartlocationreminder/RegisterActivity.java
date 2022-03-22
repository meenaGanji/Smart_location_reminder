package com.project.smartlocationreminder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    TextView alreadyHaveAccount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        alreadyHaveAccount=findViewById(R.id.alreadyHaveAccount);



        alreadyHaveAccount.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.alreadyHaveAccount)
        {
            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
        }
    }
}