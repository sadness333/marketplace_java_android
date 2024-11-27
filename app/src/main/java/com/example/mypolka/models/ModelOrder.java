package com.example.mypolka.models;

import java.util.List;

public class ModelOrder {
    private String orderId;
    private String userId;
    private String address;
    private double totalPrice;
    private String paymentMethod;
    private List<ModelOrderItem> orderItems;

    public ModelOrder() {
    }

    public ModelOrder(String orderId, String userId, String address, double totalPrice, String paymentMethod, List<ModelOrderItem> orderItems) {
        this.orderId = orderId;
        this.userId = userId;
        this.address = address;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.orderItems = orderItems;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<ModelOrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<ModelOrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}
