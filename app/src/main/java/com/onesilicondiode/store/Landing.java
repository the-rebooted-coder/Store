package com.onesilicondiode.store;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class Landing extends AppCompatActivity {
    AnimatedBottomBar animatedBottomBar;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        boolean isFirstTime = isFirstTimeOpen();
        if (isFirstTime) {
            // Show an alert dialog for the first-time users
            showFirstTimeAlertDialog();
        }
        animatedBottomBar = findViewById(R.id.bottomNavigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();
        animatedBottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {
            @Override
            public void onTabSelected(int lastIndex, @Nullable AnimatedBottomBar.Tab lastTab, int newIndex, @NotNull AnimatedBottomBar.Tab newTab) {
                Fragment fragment = null;
                if (newTab.getId() == R.id.bhojan) {
                    vibrate();
                    fragment = new HomeFragment();
                }
                else if (newTab.getId() == R.id.journal) {
                    vibrate();
                    fragment = new Journal();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
            }

            @Override
            public void onTabReselected(int i, @NotNull AnimatedBottomBar.Tab tab) {
                if (tab.getId() == R.id.bhojan) {
                    // If the Home button is reselected, reopen the Home Fragment
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();
                }
            }
        });
    }

    private boolean isFirstTimeOpen() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        // Check if the "firstTime" preference exists
        return !preferences.contains("firstTime");
    }

    private void showFirstTimeAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hey, here's what the Store app is all about 👋");
        builder.setMessage("We have public images, then we sometimes have not so public images, which we don't want the world to see, to address this, 'Store' comes in way 🚀.\n\nYou can keep storing images that you want, irrespective of their size, and then delete them away from your phone.\n\nSimple.\n\nAll of them are right there when you open this app 🥂.\n\nOh, and by the way, the next time when you open this app, it will simply crash 💥, don't worry, it's programmed to do so, just to keep other people away from accessing your mild data, just use the fingerprint sensor to unlock the app, or tap the Crash message 4 times to manually enter PIN.🎉!");
        builder.setPositiveButton("GOTCHA", (dialog, which) -> saveFirstTimePreference());
        builder.setCancelable(false);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // Check if the current fragment is the ShareFragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof ShareFragment) {
            // Replace the ShareFragment with the HomeFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new HomeFragment())
                    .commit();
        } else {
            // If not on ShareFragment, proceed with default back button behavior
            super.onBackPressed();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

    private void saveFirstTimePreference() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Set the "firstTime" preference to true
        preferences.edit().putBoolean("firstTime", true).apply();
    }
    private void vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            vibrator.vibrate(
                    VibrationEffect.startComposition()
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, 0.3f)
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 0.3f)
                            .compose());
        } else {
            long[] pattern = {5,0,5,0,5,1,5,1,5,2,5,2,5,3,5,4,5,4,5,5,5,6,5,6,5,7,5,8,5,8,5,9,5,10,5,10,5,11,5,11,5,12,5,13,5,13,5,14,5,14,5,15,5,15,5,16,5,16,5,17,5,17,5,17,5,18,5,18,5,19,5,19,5,19,5,20,5,20,5,20,5,21,5,21,5,21,5,22,5,22,5,22,5,22,5,23,5,23,5,23,5,23,5,23,5,24,5,24,5,24,5,24,5,24,5,24,5,24,5,24,5,25,5,25,5,25,5,25,5,25,5,25,5,25,5,25,5,25,5,25,5};

            VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, -1);
            vibrator.vibrate(vibrationEffect);
        }
    }
}