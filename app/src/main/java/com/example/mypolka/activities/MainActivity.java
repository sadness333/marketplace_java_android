package com.example.mypolka.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.mypolka.databinding.ActivityMainBinding;
import com.example.mypolka.fragments.CartFragment;
import com.example.mypolka.fragments.MyAdsFragment;
import com.example.mypolka.R;
import com.example.mypolka.Utils;
import com.example.mypolka.fragments.AccountFragment;
import com.example.mypolka.fragments.HomeFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private FirebaseAuth fireBaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fireBaseAuth = FirebaseAuth.getInstance();

        if (fireBaseAuth.getCurrentUser() == null) {
            startLoginOptions();
        }

        showHomeFragment();

        binding.bottomNV.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int itemID = menuItem.getItemId();
                if (itemID == R.id.menu_home) {
                    showHomeFragment();
                    return true;
                }
                else if (itemID == R.id.menu_my_ads) {
                    if (fireBaseAuth.getCurrentUser() == null){
                        Utils.toast(MainActivity.this, "Требуется авторизация...");
                        startLoginOptions();

                        return false;
                    }
                    else {
                        showMyAdsFragment();
                        return true;
                    }
                }
                else if (itemID == R.id.menu_sell) {
                    if (fireBaseAuth.getCurrentUser() == null){
                        Utils.toast(MainActivity.this, "Требуется авторизация...");
                        startLoginOptions();

                        return false;
                    }
                    else {
                        showCartFragment();
                        return true;
                    }
                }
                else if (itemID == R.id.menu_account) {
                    if (fireBaseAuth.getCurrentUser() == null){
                        Utils.toast(MainActivity.this, "Требуется авторизация...");
                        startLoginOptions();

                        return false;
                    }
                    else {
                        showAccountFragment();
                        return true;
                    }
                }
                else {
                    return false;

                }
            }
        });
    }

    private void showHomeFragment() {
        binding.toolbarTitleTV.setText("Главная");

        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsRL.getId(), fragment, "HomeFragment");
        fragmentTransaction.commit();

    }
    private void showMyAdsFragment() {
        binding.toolbarTitleTV.setText("Избранное");

        MyAdsFragment fragment = new MyAdsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsRL.getId(), fragment, "MyAdsFragment");
        fragmentTransaction.commit();
    }

    private void showCartFragment() {
        binding.toolbarTitleTV.setText("Корзина");

        CartFragment fragment = new CartFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsRL.getId(), fragment, "CartFragment");
        fragmentTransaction.commit();
    }

    private void showAccountFragment() {
        binding.toolbarTitleTV.setText("Профиль");

        AccountFragment fragment = new AccountFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsRL.getId(), fragment, "AccountFragment");
        fragmentTransaction.commit();
    }

    private void startLoginOptions() {
        startActivity(new Intent(this, LoginOptionsActivity.class));
    }
}