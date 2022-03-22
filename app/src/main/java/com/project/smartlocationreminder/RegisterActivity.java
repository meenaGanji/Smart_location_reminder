package com.project.smartlocationreminder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    TextView alreadyHaveAccount;
    EditText emailEt, passwordEt, passwordEt2;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        passwordEt2 = findViewById(R.id.passwordEt2);
        btnRegister = findViewById(R.id.btnRegister);
        alreadyHaveAccount.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.alreadyHaveAccount) {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        }
        if (view.getId() == R.id.btnRegister) {
            RegisterUser();
        }
    }

    private void RegisterUser() {
        String email = emailEt.getText().toString();
        String password = passwordEt.getText().toString();
        String cPassword = passwordEt2.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Select any Email!", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Select any Password!", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(cPassword)) {
            Toast.makeText(RegisterActivity.this, "Both password not matched!", Toast.LENGTH_SHORT).show();
        } else {
            ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
            progressDialog.setMessage("Register! Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
}