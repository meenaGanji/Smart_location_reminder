package com.project.smartlocationreminder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.LocusId;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    TextView createNewAccountTv;
 
    EditText emailEt, passwordEt;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        createNewAccountTv = findViewById(R.id.createNewAccountTv);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        btnLogin = findViewById(R.id.btnLogin);
        createNewAccountTv.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        
       if( FirebaseAuth.getInstance().getCurrentUser()!=null)
       {
           startActivity(new Intent(LoginActivity.this,MainActivity.class));
           finish();
       }

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.createNewAccountTv) {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        } if (view.getId() == R.id.btnLogin) {
           LoginUser();
        }
    }

    private void LoginUser() {
        String email = emailEt.getText().toString();
        String password = passwordEt.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Select any Email!", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Select any Password!", Toast.LENGTH_SHORT).show();
        } else {
            ProgressDialog progressDialog=new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Login! Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                        finish();
                    }else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }
}