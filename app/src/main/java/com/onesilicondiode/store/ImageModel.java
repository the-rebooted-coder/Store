package com.onesilicondiode.store;

public class ImageModel {
    private String imageUrl;

    public ImageModel() {
        // Default constructor required for Firebase
    }

    public ImageModel(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
