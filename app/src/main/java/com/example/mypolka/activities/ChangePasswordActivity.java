package com.example.mypolka.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mypolka.Utils;
import com.example.mypolka.databinding.ActivityChangePasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ChangePasswordActivity extends AppCompatActivity {


    private ActivityChangePasswordBinding binding;
    private static final String TAG = "CHANGE_PASS_TAG";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста подождите...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }

    private String currentPassword = "";
    private String newPassword = "";
    private String confirmNewPassword = "";

    private void validateData(){
        Log.d(TAG, "validateData: ");

        currentPassword = binding.currentPasswordEt.getText().toString();
        newPassword = binding.newPasswordEt.getText().toString();
        confirmNewPassword = binding.newPasswordEt.getText().toString();


        Log.d(TAG,"validateData: currentPassword: "+currentPassword);
        Log.d(TAG,"validateData: newPassword: "+newPassword);
        Log.d(TAG,"validateData: confirmNewPassword: "+confirmNewPassword);


        if (currentPassword.isEmpty()){
            binding.currentPasswordEt.setError("Введите новый пароль !");
            binding.currentPasswordEt.requestFocus();
        }else if (newPassword.isEmpty()) {

            binding.newPasswordEt.setError("Введите новый пароль!");
            binding.newPasswordEt.requestFocus();
        } else if (confirmNewPassword.isEmpty()){
            binding.confirmNewPasswordEt.setError("Введите еще раз новый пароль!");
            binding.confirmNewPasswordEt.requestFocus();
        }else if (!newPassword.equals(confirmNewPassword)){

            binding.confirmNewPasswordEt.setError("Пароль некорректный!");
            binding.confirmNewPasswordEt.requestFocus();
        }else {
            authenticateUserForUpdatePassword();
        }


        }
    private void authenticateUserForUpdatePassword(){
        Log.d(TAG, "authenticateUserForUpdatePassword: ");


        progressDialog.setMessage("Аутентификация пользователя");
        progressDialog.show();

        AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);
        firebaseUser.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        updatePassword();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ",e);
                        progressDialog.dismiss();
                        Utils.toast(ChangePasswordActivity.this, "Ошибка аутентификации: "+e.getMessage());
                    }
                });
    }
    private void updatePassword() {
        Log.d(TAG, "updatePassword: ");

        progressDialog.setMessage("Обновление пароля");
        progressDialog.show();

        firebaseUser.updatePassword(newPassword)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        progressDialog.dismiss();
                        Utils.toast(ChangePasswordActivity.this,"Пароль обновлен!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(ChangePasswordActivity.this,"Ошибка смена пароля: "+e.getMessage());
                    }
                });
    }
}