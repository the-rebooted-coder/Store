package com.onesilicondiode.store;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;

public class Landing extends AppCompatActivity {
    private Vibrator vibrator;
    private SharedPreferences sharedPreferencesNotif;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        sharedPreferencesNotif = this.getSharedPreferences("MyPrefsEnabled", Context.MODE_PRIVATE);
        boolean isNotificationEnabled = sharedPreferencesNotif.getBoolean("notificationEnabled", true);
        if(isNotificationEnabled){
            scheduleDailyNotification();
        }
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        boolean isFirstTime = isFirstTimeOpen();
        if (isFirstTime) {
            // Show an alert dialog for the first-time users
            showFirstTimeAlertDialog();
        }
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            if (item.getItemId() == R.id.bhojan) {
                vibrate();
                fragment = new HomeFragment();
            } else if (item.getItemId() == R.id.journal) {
                vibrate();
                fragment = new Journal();
            } else if (item.getItemId() == R.id.preferences) {
                vibrate();
                fragment = new Settings();
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
            return true;
        });
    }
    private void scheduleDailyNotification() {
        // Create an intent for the notification broadcast receiver
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // Set the time for the daily notification
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 45);
        calendar.set(Calendar.SECOND, 0);

        // Schedule the notification
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, // Repeat daily
                    pendingIntent
            );
        }
    }
    private boolean isFirstTimeOpen() {
        SharedPreferences preferences = getSharedPreferences("MyPrefsEnabled", Context.MODE_PRIVATE);
        // Check if the "firstTime" preference exists
        return !preferences.contains("firstTime");
    }

    private void showFirstTimeAlertDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Here's What's new in Store! 👋")
                .setMessage("The name!\n\nStoring things is now all better with a new welcoming name, say hello to 'Memories', your personal private storage and digital journal, where you can put stuff which is only meant to cherish later, all by yourself.\n\nThere's 'Journal', that let's you add 10-line entries about your day, with a title.\n\nSay goodbye to keeping those diaries, take memories with you, right in your pocket!\n\nOh, and there's a whole new preferences panel to tweak memories as per your taste!\n\nDo know, this is a beta version, so you might expect some bumps along the ride 🚗!")
                .setPositiveButton("Got it!", (dialog, which) -> saveFirstTimePreference())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void saveFirstTimePreference() {
        vibrate();
        SharedPreferences preferences = getSharedPreferences("MyPrefsEnabled", Context.MODE_PRIVATE);
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
            long[] pattern = {5, 0, 5, 0, 5, 1, 5, 1, 5, 2, 5, 2, 5, 3, 5, 4, 5, 4, 5, 5, 5, 6, 5, 6, 5};
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, -1);
                vibrator.vibrate(vibrationEffect);
            } else {
                // For versions lower than Oreo
                vibrator.vibrate(pattern, -1);
            }
        }
    }
}