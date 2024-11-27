package com.example.mypolka.adapters;

import android.content.Context;
import android.graphics.ColorSpace;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypolka.R;
import com.example.mypolka.models.ModelOrder;
import com.example.mypolka.models.ModelOrderItem;
import com.example.mypolka.models.ModelProduct;

import java.util.List;

public class AdapterOrder extends RecyclerView.Adapter<AdapterOrder.OrderViewHolder> {
    private Context mContext;
    private List<ModelOrder> orderList;

    public AdapterOrder(Context context, List<ModelOrder> orders) {
        this.mContext = context;
        this.orderList = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        ModelOrder order = orderList.get(position);

        holder.orderIdTextView.setText("Order ID: " + order.getOrderId());
        holder.addressTextView.setText("Адрес: " + order.getAddress());
        holder.totalPriceTextView.setText("Сумма заказа: " + order.getTotalPrice() + " ₽");

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView;
        TextView addressTextView;
        TextView totalPriceTextView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            totalPriceTextView = itemView.findViewById(R.id.totalPriceTextView);
        }
    }
}
