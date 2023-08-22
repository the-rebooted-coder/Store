package com.onesilicondiode.store;

public class SecureVaultModel {
    private String key;
    private String imageUrl;


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

}
