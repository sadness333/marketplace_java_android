package com.example.mypolka.models;
public class ModelCart {


    private String productId;
    private int quantity;

    private int fullPrice;

    public ModelCart() {
    }

    public ModelCart(String productId, int quantity, int fullPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.fullPrice = fullPrice;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getFullPrice() {
        return fullPrice ;
    }

    public void setFullPrice(int fullPrice) {
        this.fullPrice = fullPrice;
    }
}
