package com.onesilicondiode.store;

public class SecureVaultModel {
    private String key;
    private String imageUrl;
    private String userId;

    public SecureVaultModel() {
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUserId() { // Getter for userId
        return userId;
    }

    public void setUserId(String userId) { // Setter for userId
        this.userId = userId;
    }
}