package com.onesilicondiode.store;

import static android.content.Context.VIBRATOR_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BottomSheetCrash extends BottomSheetDialogFragment {
    String savedPin;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private Vibrator vibrator;
    private MaterialButton useBiometric;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor = Executors.newSingleThreadExecutor();
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricCallback());
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Store")
                .setSubtitle("Use your fingerprint")
                .setNegativeButtonText("Go Back")
                .build();

        // Initialize sharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate your bottom sheet layout here
        View view = inflater.inflate(R.layout.bottom_sheet_layout, container, false);
        TextInputLayout inputLayoutPassword = view.findViewById(R.id.inputLayoutPassword);
        TextInputEditText inputPassword = view.findViewById(R.id.inputPassword);
        savedPin = sharedPreferences.getString("appPin", "");
        useBiometric = view.findViewById(R.id.useBiometric);
        if (getActivity() != null) {
            vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
        }
        Button unlockButton = view.findViewById(R.id.unlockApp);
        Animation shakeAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.shake);
        useBiometric.setOnClickListener(v -> {
            vibrate();
            biometricPrompt.authenticate(promptInfo);
        });
        unlockButton.setOnClickListener(v -> {
            // Get the entered PIN
            String enteredPin = inputPassword.getText().toString();

            if (savedPin.equals(enteredPin)) {
                Intent intent = new Intent(getActivity(), Landing.class);
                startActivity(intent);
                vibrate();
                if (getActivity() != null) {
                    getActivity().finish();
                }

                dismiss();
            } else {
                inputPassword.setError("Wrong PIN");
                inputLayoutPassword.startAnimation(shakeAnimation);
                Toast.makeText(getContext(), "Incorrect PIN", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void vibrate() {
        long[] pattern = {23, 0, 17, 4, 15, 11, 19, 15, 18, 13, 16, 8, 20, 2, 0, 0, 14, 0, 14, 5, 0, 17, 16};
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, -1);
            vibrator.vibrate(vibrationEffect);
        } else {
            // For versions lower than Oreo
            vibrator.vibrate(pattern, -1);
        }
    }

    private class BiometricCallback extends BiometricPrompt.AuthenticationCallback {
        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            // Authentication succeeded, start the Landing Activity
            Intent intent = new Intent(getActivity(), Landing.class);
            startActivity(intent);
            vibrate();
            if (getActivity() != null) {
                getActivity().finish();
            }
            dismiss();
        }
    }
}