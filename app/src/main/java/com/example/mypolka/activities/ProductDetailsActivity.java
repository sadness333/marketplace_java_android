package com.example.mypolka.activities;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mypolka.R;
import com.example.mypolka.Text2ImageAPI;
import com.example.mypolka.Utils;
import com.example.mypolka.adapters.AdapterCart;
import com.example.mypolka.adapters.AdapterImageSlider;
import com.example.mypolka.databinding.ActivityProductDetailsBinding;
import com.example.mypolka.models.ModelImageSlider;
import com.example.mypolka.models.ModelProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProductDetailsActivity extends AppCompatActivity {

    private ActivityProductDetailsBinding binding;


    private static final String TAG = "PRODUCT_DETAILS_TAG";

    private FirebaseAuth firebaseAuth;
    private Text2ImageAPI text2ImageAPI;


    private String productId = "";

    private ArrayList<ModelImageSlider> imageSliderArrayList;
    private int quantity = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productId = getIntent().getStringExtra("productId");

        firebaseAuth = FirebaseAuth.getInstance();

        binding.quantityTextView.setText(String.valueOf(quantity));

        loadProductDetails();
        loadProductImages();



        binding.cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.addToCart(productId, Integer.parseInt(binding.quantityTextView.getText().toString()),Integer.parseInt(binding.priceTv.getText().toString()));
                Utils.toast(ProductDetailsActivity.this, "Успешное добавление в корзину");
            }
        });

        binding.minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity > 1) {
                    quantity--;
                    binding.quantityTextView.setText(String.valueOf(quantity));
                }
            }
        });

        binding.plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity++;
                binding.quantityTextView.setText(String.valueOf(quantity));
            }
        });

        binding.aiProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(ProductDetailsActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_image);

                ImageView imageView = dialog.findViewById(R.id.imageView);

                TextView textView = dialog.findViewById(R.id.textView);
                textView.setText("Пожалуйста, подождите...");

                text2ImageAPI = new Text2ImageAPI();
                text2ImageAPI.generateImage(binding.titleTv.getText().toString().trim(), textView, imageView);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setLayout(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                );
                dialog.show();
            }
        });




        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private  void loadProductDetails(){
        Log.d(TAG, "loadProductDetails: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            ModelProduct modelProduct = snapshot.getValue(ModelProduct.class);

                            String title = modelProduct.getTitle();
                            String description = modelProduct.getDescription();
                            String price = modelProduct.getPrice();
                            String category = modelProduct.getCategory();

                            binding.titleTv.setText(title);
                            binding.descriptionTv.setText(description);
                            binding.priceTv.setText(price);
                            binding.categoryTv.setText(category);
                        } catch (Exception e){
                            Log.e(TAG, "onDataChange: ", e);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void loadProductImages(){
        imageSliderArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.child(productId).child("Images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        imageSliderArrayList.clear();

                        for(DataSnapshot ds: snapshot.getChildren()){

                            ModelImageSlider modelImageSlider = ds.getValue(ModelImageSlider.class);

                            imageSliderArrayList.add(modelImageSlider);
                        }

                        AdapterImageSlider adapterImageSlider = new AdapterImageSlider(ProductDetailsActivity.this, imageSliderArrayList);
                        binding.imageSliderVp.setAdapter(adapterImageSlider);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
