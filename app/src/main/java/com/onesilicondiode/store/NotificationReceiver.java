package com.onesilicondiode.store;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "MyNotificationChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferencesNotif = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isNotificationEnabled = sharedPreferencesNotif.getBoolean("notificationEnabled", true);

        if (isNotificationEnabled) {
            createNotificationChannel(context);
            showNotification(context);
        }
    }

    private void createNotificationChannel(Context context) {
        CharSequence name = "Journal Entry Reminders";
        String description = "Channel for Journal Entry";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.subtle), null);
        channel.setDescription(description);
        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @SuppressLint("MissingPermission")
    private void showNotification(Context context) {
        // Create an intent to open the Journal Fragment
        Intent intent = new Intent(context, SplashScreen.class);// Add a flag to indicate opening the Journal Fragment
        // Create a PendingIntent for the intent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Daily Journal Reminder")
                .setContentText("Don't forget to write your journal entry today!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // Set the PendingIntent
                .setAutoCancel(true); // Automatically dismiss the notification when tapped

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
