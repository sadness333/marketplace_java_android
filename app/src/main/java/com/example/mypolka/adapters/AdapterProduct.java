package com.example.mypolka.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mypolka.FilterProduct;
import com.example.mypolka.R;
import com.example.mypolka.Utils;
import com.example.mypolka.activities.ProductDetailsActivity;
import com.example.mypolka.databinding.RowProductBinding;
import com.example.mypolka.models.ModelProduct;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterProduct extends RecyclerView.Adapter<AdapterProduct.HolderProduct> implements Filterable {
    private FirebaseAuth firebaseAuth;
    private Context context;
    public ArrayList<ModelProduct> productArrayList;

    private ArrayList<ModelProduct> filterList;
    private FilterProduct filter;

    private RowProductBinding binding;
    private static final String TAG = "ADAPTER_PRODUCT_TAG";

    public AdapterProduct(){


    }

    public AdapterProduct(Context context, ArrayList<ModelProduct> productArrayList){
        this.context = context;
        this.productArrayList = productArrayList;
        this.filterList = productArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderProduct onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowProductBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderProduct(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProduct holder, int position) {
        ModelProduct modelProduct = productArrayList.get(position);

        String title = modelProduct.getTitle();
        String description = modelProduct.getDescription();
        String price = modelProduct.getPrice() + " ₽";
        long timestamp = modelProduct.getTimestamp();
        String formattedDate = Utils.formatTimestampDate(timestamp);

        loadProductFirstImage(modelProduct, holder);

        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorite(modelProduct, holder);
        }

        holder.titleTv.setText(title);
        holder.priceTv.setText(price);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProductDetailsActivity.class);
                intent.putExtra("productId", modelProduct.getId());
                context.startActivity(intent);
            }
        });

        holder.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean favorite = modelProduct.isFavorite();

                if (favorite){
                    Utils.removeFromFavorite(context, modelProduct.getId());
                } else {

                    Utils.addToFavorite(context, modelProduct.getId());
                }
            }
        });
    }

    private void checkIsFavorite(ModelProduct modelProduct, HolderProduct holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(modelProduct.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean favorite = snapshot.exists();

                        modelProduct.setFavorite(favorite);

                        if (favorite){
                            holder.favBtn.setImageResource(R.drawable.ic_fav_yes);
                        } else {
                            holder.favBtn.setImageResource(R.drawable.ic_fav_no);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadProductFirstImage(ModelProduct modelProduct, HolderProduct holder){
        Log.d(TAG,"loadProductFirstImage");

        String productId = modelProduct.getId();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Products");
        reference.child(productId).child("Images").limitToFirst(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){

                            String imageUrl = "" + ds.child("imageUrl").getValue();
                            Log.d(TAG, "onDataChange: imageUrl" + imageUrl);

                            try {
                                Glide.with(context)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_image_grey)
                                        .into(holder.imageIv);
                            } catch (Exception e){
                                Log.e(TAG, "onDataChange ", e);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return productArrayList.size();
    }

    @Override
    public Filter getFilter() {

        if(filter == null){
            filter = new FilterProduct(this, filterList);
        }
        return filter;
    }

    class HolderProduct extends RecyclerView.ViewHolder{
        ShapeableImageView imageIv;
        TextView titleTv, priceTv;
        ImageButton favBtn;

        public HolderProduct(@NonNull View itemView){
            super(itemView);

            imageIv = binding.imageIv;
            titleTv = binding.titleTv;
            favBtn = binding.favBtn;
            priceTv = binding.priceTv;

        }
    }

}
