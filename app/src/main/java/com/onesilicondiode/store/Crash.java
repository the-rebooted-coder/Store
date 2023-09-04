package com.onesilicondiode.store;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class Crash extends AppCompatActivity {
    private MaterialButton showFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        showFrag = findViewById(R.id.showFrag);
        showFrag.setOnClickListener(view -> showBottomSheet());

    }

    private void showBottomSheet() {
        BottomSheetCrash bottomSheetFragment = new BottomSheetCrash();
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }
}