package com.example.mypolka.adapters;
import static android.app.PendingIntent.getActivity;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mypolka.R;
import com.example.mypolka.Utils;
import com.example.mypolka.activities.ProductDetailsActivity;
import com.example.mypolka.databinding.FragmentCartBinding;
import com.example.mypolka.fragments.CartFragment;
import com.example.mypolka.models.ModelCart;
import com.example.mypolka.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterCart extends RecyclerView.Adapter<AdapterCart.CartViewHolder> {

    private Context mContext;
    private List<ModelCart> cartList;
    private DatabaseReference productsRef;
    private CartListener cartListener;

    public interface CartListener {
        void onItemRemoved(String productId);
    }

    public AdapterCart(Context context, List<ModelCart> cartList, CartListener listener) {
        this.mContext = context;
        this.cartList = cartList;
        this.productsRef = FirebaseDatabase.getInstance().getReference().child("Products");
        this.cartListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ModelCart modelCart = cartList.get(position);

        productsRef.child(modelCart.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String title = snapshot.child("title").getValue(String.class);
                    String priceString = snapshot.child("price").getValue(String.class);

                    if (priceString != null && !priceString.isEmpty()) {
                        try {
                            double price = Double.parseDouble(priceString);
                            holder.productNameTextView.setText(title);
                            holder.productPriceTextView.setText(String.format("%.2f ₽", price));
                        } catch (NumberFormatException e) {
                            Toast.makeText(mContext, "Ошибка при преобразовании цены", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(mContext, "Цена отсутствует или неверного формата", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });

        String productId = modelCart.getProductId();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Products");
        reference.child(productId).child("Images").limitToFirst(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){

                            String imageUrl = "" + ds.child("imageUrl").getValue();

                            try {
                                Glide.with(mContext)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_image_grey)
                                        .into(holder.shapeableImageView);
                            } catch (Exception e){

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.quantityTextView.setText(String.valueOf(modelCart.getQuantity()));
        checkFavoriteProduct(modelCart.getProductId(), holder.favBtnImageButton);

        holder.deleteProductImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String productId = modelCart.getProductId();
                Utils.removeFromCart(productId); // Удаление из Firebase
                cartList.remove(position); // Удаление из списка
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartList.size());
                cartListener.onItemRemoved(productId); // Уведомление об удалении товара
            }
        });

    }

    private void checkFavoriteProduct(String productId, ImageButton favBtnImageButton) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            String currentUserId = firebaseAuth.getCurrentUser().getUid();
            DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("Favorites");

            favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(productId).exists()) {
                        favBtnImageButton.setImageResource(R.drawable.ic_fav_yes);
                    } else {
                        favBtnImageButton.setImageResource(R.drawable.ic_fav_no);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(mContext, "Ошибка загрузки избранного", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    @Override
    public int getItemCount() {
        return cartList == null ? 0 : cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {

        TextView productNameTextView;
        TextView productPriceTextView;
        TextView quantityTextView;
        ImageButton favBtnImageButton;
        ImageButton deleteProductImageButton;


        ShapeableImageView shapeableImageView;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            productNameTextView = itemView.findViewById(R.id.productName);
            productPriceTextView = itemView.findViewById(R.id.productPrice);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            shapeableImageView = itemView.findViewById(R.id.productImage);
            favBtnImageButton = itemView.findViewById(R.id.favBtnn);
            deleteProductImageButton = itemView.findViewById(R.id.deleteProduct);


        }
    }
}
