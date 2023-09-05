package com.onesilicondiode.store;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.VIBRATOR_SERVICE;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Random;

public class Settings extends Fragment {
    public static final String UI_MODE = "uiMode";
    private final ActivityResultLauncher<String> requestNotificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    // Permission granted, perform your action
                    Toast.makeText(getContext(), "Permission denied, cannot post notification to keep SafeCharge working 😔", Toast.LENGTH_LONG).show();

                }
            });
    private TextView accountHolderName;
    private TextView accountHolderEmail;
    private MaterialButton signOutBtn, editPin;
    private ImageView userAccImage;
    private FirebaseAuth mAuth;
    private Vibrator vibrator;
    private MaterialSwitch secureAppSwitch, appAppearance, notification;
    private SharedPreferences sharedPreferences, sharedPreferencesTheme, sharedPreferencesNotif;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        sharedPreferencesNotif = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        if (getActivity() != null) {
            vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
        }
        accountHolderName = rootView.findViewById(R.id.accountHolderName);
        accountHolderEmail = rootView.findViewById(R.id.accountHolderEmail);
        editPin = rootView.findViewById(R.id.editPin);
        appAppearance = rootView.findViewById(R.id.appAppearance);
        signOutBtn = rootView.findViewById(R.id.signOutBtn);
        userAccImage = rootView.findViewById(R.id.userAccImage);
        secureAppSwitch = rootView.findViewById(R.id.secureApp);
        notification = rootView.findViewById(R.id.notifications);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission();
        }
        boolean isNotificationEnabled = sharedPreferencesNotif.getBoolean("notificationEnabled", true);
        notification.setChecked(isNotificationEnabled);
        notification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            vibrate();
            SharedPreferences.Editor editor = sharedPreferencesNotif.edit();
            editor.putBoolean("notificationEnabled", isChecked);
            editor.apply();
            if (isChecked) {
                // Schedule the daily notification if it's enabled
                scheduleDailyNotification();
            } else {
                // Cancel the scheduled notification if it's disabled
                cancelDailyNotification();
            }
        });
        sharedPreferencesTheme = requireContext().getSharedPreferences(UI_MODE, MODE_PRIVATE);
        String uiMode = sharedPreferencesTheme.getString("uiMode", "Light");
        appAppearance.setChecked("Dark".equals(uiMode));
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        ObjectAnimator slideDownAnimation = (ObjectAnimator) AnimatorInflater.loadAnimator(requireContext(), R.animator.slide_down);
        boolean isSecureAppEnabled = sharedPreferences.getBoolean("secureAppEnabled", false);
        secureAppSwitch.setChecked(isSecureAppEnabled);
        editPin.setVisibility(isSecureAppEnabled ? View.VISIBLE : View.GONE);
        appAppearance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferences.Editor editor = requireContext().getSharedPreferences(UI_MODE, MODE_PRIVATE).edit();
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putString("uiMode", "Dark");
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putString("uiMode", "Light");
                }
                editor.apply();
            }
        });

        secureAppSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Check if a PIN is already set
                String savedPin = sharedPreferences.getString("appPin", "");
                if (savedPin.isEmpty()) {
                    showSetPinDialog();
                    vibrate();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("secureAppEnabled", true); // Set secureAppEnabled to true
                    editor.commit();
                }
                editPin.setVisibility(View.VISIBLE);
                slideDownAnimation.setTarget(editPin);
                slideDownAnimation.start();
            } else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("secureAppEnabled", false); // Set secureAppEnabled to true
                editor.commit();
                editPin.setVisibility(View.GONE);
            }
        });
        editPin.setOnClickListener(view -> {
            vibrate();
            showSetPinDialog();
        });
        if (user != null) {
            String displayName = user.getDisplayName();
            String email = user.getEmail();

            // Set the user's name and email in the TextViews
            accountHolderName.setText(displayName);
            accountHolderEmail.setText(email);

            // Load and set the user's profile picture using Glide or any other image loading library
            String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
            if (photoUrl != null) {
                Glide.with(requireContext())
                        .load(photoUrl)
                        .into(userAccImage);
            }
        }
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the user
                vibrate();
                signOut();
            }
        });


        return rootView;
    }

    private void scheduleDailyNotification() {
        // Create an intent for the notification broadcast receiver
        Intent notificationIntent = new Intent(requireContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // Set the time for the daily notification
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 45);
        calendar.set(Calendar.SECOND, 0);

        // Schedule the notification
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, // Repeat daily
                    pendingIntent
            );
        }
    }

    private void checkNotificationPermission() {
        String permission = Manifest.permission.POST_NOTIFICATIONS;
        if (getContext() != null) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Voila, you'll be reminded 🎉!", Toast.LENGTH_SHORT).show();
            } else if (shouldShowRequestPermissionRationale(permission)) {
                // Permission denied previously, show rationale dialog
                showPermissionRationaleDialog();
            } else {
                // Request notification permission
                showPermissionRationaleDialog();
            }
        }
    }

    private void showPermissionRationaleDialog() {
        View customView = getLayoutInflater().inflate(R.layout.custom_alert_dialog, null);
        if (getContext() != null) {
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle("Store is better with Notifications!")
                    .setMessage(R.string.one_time_reminder)
                    .setView(customView)
                    .setPositiveButton("Continue", (dialog, which) -> {
                        vibrate();
                        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
                    })
                    .setNegativeButton("No Thanks", (dialog, which) -> {
                        // Handle if the user cancels the request
                        Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        }

    }

    private void cancelDailyNotification() {
        // Create an intent for the notification broadcast receiver
        Intent notificationIntent = new Intent(requireContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Cancel the scheduled notification
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void vibrate() {
        long[] pattern = {23, 0, 17, 4, 15, 11, 19, 15, 18, 13, 16, 8, 20, 2, 0, 0, 14, 0, 14, 5, 0, 17, 16};
        VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, -1);
        vibrator.vibrate(vibrationEffect);
    }

    private void showSetPinDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_pin, null);

        TextInputEditText pinEditText = dialogView.findViewById(R.id.pinEditText);
        Button setPinButton = dialogView.findViewById(R.id.setPinButton);

        builder.setView(dialogView);
        AlertDialog setPinDialog = builder.create();
        setPinDialog.setCancelable(false);
        setPinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredPin = pinEditText.getText().toString();
                if (isValidPin(enteredPin)) {
                    // Save the PIN to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("appPin", enteredPin);
                    editor.putBoolean("secureAppEnabled", true); // Set secureAppEnabled to true
                    editor.apply();
                    setPinDialog.dismiss();
                    secureAppSwitch.setChecked(true);
                } else {
                    // Show an error message if the PIN is invalid
                    Toast.makeText(requireContext(), "Invalid PIN. Please enter a 4-digit PIN.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        setPinDialog.show();
    }


    private boolean isValidPin(String pin) {
        // Validate the PIN (4 digits)
        return pin.length() == 4 && pin.matches("\\d{4}");
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        googleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                // Sign-out successful, redirect to SignUp class
                Intent intent = new Intent(getActivity(), SplashScreen.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            } else {
                Toast.makeText(getContext(), "Logout Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}