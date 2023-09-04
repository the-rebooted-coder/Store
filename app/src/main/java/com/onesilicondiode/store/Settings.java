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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Settings extends Fragment {
    private TextView accountHolderName;
    private TextView accountHolderEmail;
    private MaterialButton signOutBtn;

    private ImageView userAccImage;
    private FirebaseAuth mAuth;
    private Vibrator vibrator;
    private MaterialSwitch secureAppSwitch;
    private SharedPreferences sharedPreferences;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        accountHolderName = rootView.findViewById(R.id.accountHolderName);
        accountHolderEmail = rootView.findViewById(R.id.accountHolderEmail);
        signOutBtn = rootView.findViewById(R.id.signOutBtn);
        userAccImage = rootView.findViewById(R.id.userAccImage);
        secureAppSwitch = rootView.findViewById(R.id.secureApp);
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        if (getActivity() != null) {
            vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
        }
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        boolean isSecureAppEnabled = sharedPreferences.getBoolean("secureAppEnabled", false);
        secureAppSwitch.setChecked(isSecureAppEnabled);
        secureAppSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the switch state in SharedPreferences
                sharedPreferences.edit().putBoolean("secureAppEnabled", isChecked).apply();
            }
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

    private void vibrate() {
        long[] pattern = {23, 0, 17, 4, 15, 11, 19, 15, 18, 13, 16, 8, 20, 2, 0, 0, 14, 0, 14, 5, 0, 17, 16};
        VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, -1);
        vibrator.vibrate(vibrationEffect);
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