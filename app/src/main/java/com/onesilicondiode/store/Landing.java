
package com.onesilicondiode.store;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class Landing extends AppCompatActivity {
    AnimatedBottomBar animatedBottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
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
                    fragment = new HomeFragment();
                } else if (newTab.getId() == R.id.share) {
                    fragment = new ShareFragment();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
            }

            @Override
            public void onTabReselected(int i, @NotNull AnimatedBottomBar.Tab tab) {
                //DO NOTHING
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
        builder.setPositiveButton("GOTCHA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveFirstTimePreference();
            }
        });
        builder.setCancelable(false);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void saveFirstTimePreference() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Set the "firstTime" preference to true
        preferences.edit().putBoolean("firstTime", true).apply();
    }
}