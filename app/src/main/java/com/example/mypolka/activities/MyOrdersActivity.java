package com.example.mypolka.activities;

import android.graphics.ColorSpace;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypolka.R;
import com.example.mypolka.adapters.AdapterOrder;
import com.example.mypolka.databinding.ActivityForgotPasswordBinding;
import com.example.mypolka.databinding.ActivityMyOrdersBinding;
import com.example.mypolka.models.ModelOrder;
import com.example.mypolka.models.ModelOrderItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyOrdersActivity extends AppCompatActivity {

    private ActivityMyOrdersBinding binding;
    private RecyclerView recyclerView;
    private AdapterOrder adapterOrder;
    private List<ModelOrder> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recyclerView = findViewById(R.id.orderRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();

        adapterOrder = new AdapterOrder(this, orderList);
        recyclerView.setAdapter(adapterOrder);

        loadOrdersFromDatabase();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void loadOrdersFromDatabase() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String orderId = dataSnapshot.getKey();
                    String userId = dataSnapshot.child("userId").getValue(String.class);

                    if (userId.equals(currentUserId)) {
                        String address = dataSnapshot.child("address").getValue(String.class);
                        double totalPrice = dataSnapshot.child("totalPrice").getValue(Double.class);
                        String paymentMethod = dataSnapshot.child("paymentMethod").getValue(String.class);

                        List<ModelOrderItem> orderItems = new ArrayList<>();
                        DataSnapshot orderItemsSnapshot = dataSnapshot.child("orderItems");
                        for (DataSnapshot itemSnapshot : orderItemsSnapshot.getChildren()) {
                            String productId = itemSnapshot.getKey();
                            int quantity = itemSnapshot.child("quantity").getValue(Integer.class);
                            ModelOrderItem orderItem = new ModelOrderItem(productId, quantity);
                            orderItems.add(orderItem);
                        }

                        ModelOrder order = new ModelOrder(orderId, userId, address, totalPrice, paymentMethod, orderItems);
                        orderList.add(order);
                    }
                }

                adapterOrder.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyOrdersActivity.this, "Ошибка загрузки заказов", Toast.LENGTH_SHORT).show();
            }
        });
    }
}