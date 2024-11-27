package com.example.mypolka.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypolka.adapters.AdapterCart;
import com.example.mypolka.databinding.FragmentCartBinding;
import com.example.mypolka.models.ModelCart;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CartFragment extends Fragment {

    private Context mContext;
    private FragmentCartBinding binding;
    private RecyclerView recyclerView;
    private AdapterCart adapterCart;
    private List<ModelCart> cartList;

    private boolean isCardPaymentSelected = false;
    private boolean isCashPaymentSelected = false;
    private Double totalPrice = 0.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getContext();
        recyclerView = binding.cartRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        cartList = new ArrayList<>();

        adapterCart = new AdapterCart(mContext, cartList, new AdapterCart.CartListener() {
            @Override
            public void onItemRemoved(String productId) {
                recalculateTotalPrice();
            }
        });
        recyclerView.setAdapter(adapterCart);

        loadCartData();

        binding.cardPaymentToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCardPaymentSelected) {
                    binding.cardPaymentToggleButton.setChecked(true);
                    isCardPaymentSelected = true;

                    binding.cashPaymentToggleButton.setChecked(false);
                    isCashPaymentSelected = false;
                } else {
                    binding.cardPaymentToggleButton.setChecked(false);
                    isCardPaymentSelected = false;
                }
            }
        });

        binding.cashPaymentToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCashPaymentSelected) {
                    binding.cashPaymentToggleButton.setChecked(true);
                    isCashPaymentSelected = true;

                    binding.cardPaymentToggleButton.setChecked(false);
                    isCardPaymentSelected = false;
                } else {
                    binding.cashPaymentToggleButton.setChecked(false);
                    isCashPaymentSelected = false;
                }
            }
        });

        binding.placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCardPaymentSelected) {
                    createOrder();
                } else if (isCashPaymentSelected) {
                    createOrder();
                } else {
                    Toast.makeText(mContext, "Выберите способ оплаты", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadCartData() {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("Cart");

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                totalPrice = 0.0;

                for (DataSnapshot cartSnapshot : snapshot.getChildren()) {
                    String productId = cartSnapshot.getKey();
                    int quantity = cartSnapshot.child("quantity").getValue(Integer.class);
                    int fullPrice = cartSnapshot.child("fullPrice").getValue(Integer.class);

                    totalPrice += fullPrice * quantity;

                    ModelCart modelCart = new ModelCart(productId, quantity, fullPrice);
                    cartList.add(modelCart);
                }

                binding.totalPriceValueTextView.setText(String.format("%.2f ₽", totalPrice));
                adapterCart.notifyDataSetChanged();

                if (cartList.isEmpty()) {
                    Toast.makeText(mContext, "Ваша корзина пуста", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOrder() {
        String city = binding.cityEditText.getText().toString().trim();
        String street = binding.streetEditText.getText().toString().trim();
        String house = binding.houseEditText.getText().toString().trim();
        String apartment = binding.apartmentEditText.getText().toString().trim();

        if (city.isEmpty() || street.isEmpty() || house.isEmpty() || apartment.isEmpty()) {
            Toast.makeText(mContext, "Введите адрес доставки", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> orderData = new HashMap<>();
        orderData.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        orderData.put("address", city + ", " + street + ", " + house + ", кв. " + apartment);
        orderData.put("totalPrice", totalPrice);

        if (isCardPaymentSelected) {
            orderData.put("paymentMethod", "Карта");
        } else if (isCashPaymentSelected) {
            orderData.put("paymentMethod", "Наличные");
        }

        List<HashMap<String, Object>> orderItemsList = new ArrayList<>();
        for (ModelCart cartItem : cartList) {
            HashMap<String, Object> orderItemData = new HashMap<>();
            orderItemData.put("productId", cartItem.getProductId());
            orderItemData.put("quantity", cartItem.getQuantity());
            orderItemsList.add(orderItemData);
        }
        orderData.put("orderItems", orderItemsList);

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        String orderId = ordersRef.push().getKey();
        if (orderId != null) {
            ordersRef.child(orderId).setValue(orderData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(mContext, "Заказ успешно оформлен", Toast.LENGTH_SHORT).show();

                            clearCart();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mContext, "Ошибка оформления заказа", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void clearCart() {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Cart");
        cartRef.removeValue();

        loadCartData();

    }





    private void recalculateTotalPrice() {
        totalPrice = 0.0;
        for (ModelCart cartItem : cartList) {
            totalPrice += cartItem.getFullPrice();
        }
        binding.totalPriceValueTextView.setText(String.format("%.2f", totalPrice));
    }
}
