package com.example.mypolka.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mypolka.R;
import com.example.mypolka.RvListenerCategory;
import com.example.mypolka.Utils;
import com.example.mypolka.adapters.AdapterCategory;
import com.example.mypolka.adapters.AdapterProduct;
import com.example.mypolka.databinding.FragmentHomeBinding;
import com.example.mypolka.models.ModelCategory;
import com.example.mypolka.models.ModelProduct;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private static final String TAG = "HOME_TAG";

    private Context mContext;

    private ArrayList<ModelProduct> productArrayList;
    private AdapterProduct adapterProduct;

    private SharedPreferences locationSp;




    @Override
    public void onAttach(@NonNull Context context){
        mContext = context;
        super.onAttach(context);
    }

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,@Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);



        loadCategories();

        loadProducts("Всё");


        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "onTextChanged: Query: "+charSequence);

                try {
                    if (adapterProduct != null) {
                        String query = charSequence.toString();
                        adapterProduct.getFilter().filter(query);
                    } else {
                        Log.e(TAG, "AdapterProduct is null");
                    }
                } catch (Exception e){
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }


    private void loadCategories(){
    ArrayList<ModelCategory> categoryArrayList = new ArrayList<>();

    ModelCategory modelCategoryAll = new ModelCategory("Всё", R.drawable.ic_category_all);
    categoryArrayList.add(modelCategoryAll);


    for(int i = 0; i< Utils.categories.length; i++){
        ModelCategory modelCategory = new ModelCategory(Utils.categories[i],Utils.categoryIcons[i]);
        categoryArrayList.add(modelCategory);
    }

    AdapterCategory adapterCategory = new AdapterCategory(mContext, categoryArrayList, new RvListenerCategory() {
        @Override
        public void onCategoryClick(ModelCategory modelCategory) {
            loadProducts(modelCategory.getCategory());
        }
    });
    binding.categoriesRv.setAdapter(adapterCategory);
    }

    private void loadProducts(String category){
        Log.d(TAG, "loadProducts: Category" +category);

        productArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelProduct modelProduct = ds.getValue(ModelProduct.class);

                    if(category.equals("Всё")){
                        productArrayList.add(modelProduct);
                    } else {
                        if (modelProduct.getCategory().equals(category)){
                            productArrayList.add(modelProduct);
                        }
                    }
                }
                adapterProduct = new AdapterProduct(mContext, productArrayList);
                binding.adsRv.setAdapter(adapterProduct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}