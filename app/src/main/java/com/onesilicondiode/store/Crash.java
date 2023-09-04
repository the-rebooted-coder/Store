package com.onesilicondiode.store;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class Crash extends AppCompatActivity {
    private MaterialButton showFrag;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        check();
        setContentView(R.layout.activity_crash);
        showFrag = findViewById(R.id.showFrag);
        showFrag.setOnClickListener(view -> showBottomSheet());

    }

    private void check() {
        boolean isSecureAppEnabled = sharedPreferences.getBoolean("secureAppEnabled", false);

        if (!isSecureAppEnabled) {
            // Secure app is disabled, start the Land activity
            startActivity(new Intent(this, Landing.class));
            finish();
        }
    }

    private void showBottomSheet() {
        BottomSheetCrash bottomSheetFragment = new BottomSheetCrash();
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }
}