package com.example.mypolka;

import android.content.Context;
import android.widget.Toast;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;

import com.example.mypolka.activities.ProductDetailsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
public class Utils {

    private DatabaseReference priceRef;


            public static final String[] categories = {
                    "Телефоны",
                    "Компьютеры/Ноутбуки",
                    "Электроника",
                    "Домашнии декорации",
                    "Мода и красота",
                    "Книги",
                    "Спорт",
                    "Для животных",
                    "Агрокультура",
            };

            public static final int[] categoryIcons= {
                    R.drawable.ic_category_mobilies,
                    R.drawable.ic_category_computer,
                    R.drawable.ic_category_electronics,
                    R.drawable.ic_category_furniture,
                    R.drawable.ic_category_fashion,
                    R.drawable.ic_category_books,
                    R.drawable.ic_category_sports,
                    R.drawable.ic_category_animals,
                    R.drawable.ic_category_agriculture,

            };
            public static void toast(Context context, String message) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }


            public static long getTimestamp() {
                return System.currentTimeMillis();
            }

            public static String formatTimestampDate(Long timestamp) {
                Calendar calendar = Calendar.getInstance(Locale.ENGLISH);

                calendar.setTimeInMillis(timestamp);

                String date = DateFormat.format("dd/MM/yyyy", calendar).toString();

                return date;


            }


    public static void addToFavorite(Context context, String productId){
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                if(firebaseAuth.getCurrentUser() != null) {
                    long timestamp = Utils.getTimestamp();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("productId", productId);
                    hashMap.put("timestamp", timestamp);

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.child(firebaseAuth.getUid()).child("Favorites").child(productId)
                            .setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Utils.toast(context, "Ошибка при добавлении");
                                }
                            });
                } else{
                    Utils.toast(context, "Вы не авторизованы");
                }
    }

    public static void removeFromFavorite(Context context, String productId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(productId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.toast(context, "Ошибка при удалении из избранного");
                        }
                    });
        } else{
            Utils.toast(context, "Вы не авторизованы");


        }
    }



    public static void addToCart(String productId, int quantity, int fullPrices) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(firebaseAuth.getUid())
                    .child("Cart")
                    .child(productId);

            cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int currentQuantity = snapshot.child("quantity").getValue(Integer.class);
                        int updatedQuantity = currentQuantity + quantity;
                        int currentFullPrice = snapshot.child("fullPrice").getValue(Integer.class);
                        int fullPrice = currentFullPrice/currentQuantity;
                        int updatedFullPrice = fullPrice * updatedQuantity;

                        cartRef.child("quantity").setValue(updatedQuantity)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                        cartRef.child("fullPrice").setValue(updatedFullPrice)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                    } else {

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("productId", productId);
                        hashMap.put("quantity", quantity);
                        hashMap.put("fullPrice", fullPrices);

                        cartRef.setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
        }
    }


    public static void removeFromCart(String productId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(firebaseAuth.getUid())
                    .child("Cart")
                    .child(productId);

            cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        cartRef.removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                    } else {
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
        }
    }

}
