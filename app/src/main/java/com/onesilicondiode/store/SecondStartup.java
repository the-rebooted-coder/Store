package com.onesilicondiode.store;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.transition.Explode;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class SecondStartup extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String FIRST_LAUNCH_KEY = "firstLaunch";
    MaterialButton finalLaunch;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        setContentView(R.layout.activity_second_startup);
        getWindow().setExitTransition(new Explode());
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        finalLaunch = findViewById(R.id.finalLaunch);
        finalLaunch.setOnClickListener(view -> {
            Intent intent = new Intent(SecondStartup.this, SignUp.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            vibrate();
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(FIRST_LAUNCH_KEY, false);
            editor.apply();
            finish();
        });
        TextView wavingHandTextView = findViewById(R.id.wavingHandTextView);
        Animation waveAnimation = AnimationUtils.loadAnimation(this, R.anim.emoji_wave);
        wavingHandTextView.setOnClickListener(view -> {
            vibrate();
            wavingHandTextView.startAnimation(waveAnimation);
        });
        wavingHandTextView.startAnimation(waveAnimation);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void vibrate() {
        long[] pattern = {10, 0, 11, 1, 16, 2, 11, 3, 10, 5, 0, 6, 0, 7, 11, 9, 14, 10, 13, 10, 11, 11, 0, 11, 11, 11, 11};

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, -1);
            vibrator.vibrate(vibrationEffect);
        } else {
            // For versions lower than Oreo
            vibrator.vibrate(pattern, -1);
        }
    }
}