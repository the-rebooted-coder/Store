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

import java.util.Calendar;

public class Landing extends AppCompatActivity {
    private Vibrator vibrator;
    private SharedPreferences sharedPreferencesNotif;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        sharedPreferencesNotif = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
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