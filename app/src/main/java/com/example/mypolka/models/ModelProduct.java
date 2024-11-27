package com.example.mypolka.models;

public class ModelProduct {
    String uid;
    String id;
    String category;
    String price;
    String title;
    String description;
    long timestamp;

    boolean favorite;

    public ModelProduct() {

    }

    public ModelProduct(String uid, String id, String category, String price, String title, String description, long timestamp, boolean favorite) {
        this.uid = uid;
        this.id = id;
        this.category = category;
        this.price = price;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;

        this.favorite = favorite;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
