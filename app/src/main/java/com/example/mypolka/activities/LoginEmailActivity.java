package com.example.mypolka.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mypolka.Utils;
import com.example.mypolka.databinding.ActivityLoginEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginEmailActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private static final String TAG = "LOGIN_TAG";

    private FirebaseAuth firebaseAuth;
    private ActivityLoginEmailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста подождите...");
        progressDialog.setCanceledOnTouchOutside(false);
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginEmailActivity.this, RegisterEmailActivity.class));
            }
        });

        binding.forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginEmailActivity.this, ForgotPasswordActivity.class));
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }
    private String email, password;

    private void validateData(){
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString();

        Log.d(TAG, "validateData: email: "+email);
        Log.d(TAG, "validateData: password: "+password);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.setError("Неправильная почта");
            binding.emailEt.requestFocus();
        } else if (password.isEmpty()) {
            binding.passwordEt.setError("Введите пароль");
            binding.passwordEt.requestFocus();
        }
        else {
            loginUser();
        }
    }

    private void loginUser() {
        progressDialog.setMessage("Авторизация");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onSuccess: Logged In...");
                        progressDialog.dismiss();

                        startActivity(new Intent(LoginEmailActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        Utils.toast(LoginEmailActivity.this, "Failed due to "+e.getMessage());
                        progressDialog.dismiss();
                    }
                });

    }
}
