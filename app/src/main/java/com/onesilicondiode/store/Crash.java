package com.onesilicondiode.store;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class Crash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showBottomSheet();
    }

    private void showBottomSheet() {
        BottomSheetCrash bottomSheetFragment = new BottomSheetCrash();
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }
}