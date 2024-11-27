package com.example.mypolka.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mypolka.adapters.AdapterProduct;
import com.example.mypolka.databinding.FragmentMyAdsBinding;
import com.example.mypolka.models.ModelProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MyAdsFragment extends Fragment {

    private FragmentMyAdsBinding binding;

    private Context mContext;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelProduct> productArrayList;

    private AdapterProduct adapterProduct;

    public MyAdsFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMyAdsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        loadProducts();


    }

    private void loadProducts() {
        productArrayList = new ArrayList<>();

        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Users");
        favRef.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productArrayList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()){

                            String productId = ""+ ds.child("productId").getValue();

                            DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Products");
                            productRef.child(productId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            try {
                                                ModelProduct modelProduct = snapshot.getValue(ModelProduct.class);
                                                productArrayList.add(modelProduct);
                                            }catch (Exception e){}
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                adapterProduct = new AdapterProduct(mContext, productArrayList);
                                binding.productRv.setAdapter(adapterProduct);
                            }
                        },100);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}