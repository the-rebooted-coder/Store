package com.onesilicondiode.store;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swipebutton_library.SwipeButton;
import com.gauravbhola.ripplepulsebackground.RipplePulseLayout;

public class FirstTime extends AppCompatActivity {

    TextView appName;
    SwipeButton startApp;
    RipplePulseLayout mRipplePulseLayout;
    private Vibrator vibrator;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            vibEffect();
            handler.postDelayed(this, 4000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        ImageView sharedImageView = findViewById(R.id.sharedImageView);
        appName = findViewById(R.id.appName);
        startApp = findViewById(R.id.startButton);
        mRipplePulseLayout = findViewById(R.id.layout_ripplepulse);
        startPulse();
        handler.postDelayed(runnable, 4000);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            appName.startAnimation(fadeIn);
            fadeIn.setDuration(1200);
            appName.setVisibility(View.VISIBLE);
        }, 2400);
        handler.postDelayed(() -> {
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            startApp.startAnimation(fadeIn);
            fadeIn.setDuration(1200);
            startApp.setVisibility(View.VISIBLE);
        }, 2500);
        startApp.setOnActiveListener(() -> {
            vibrate();
            getWindow().setSharedElementsUseOverlay(true);
            Intent intent = new Intent(FirstTime.this, SecondStartup.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(FirstTime.this, sharedImageView, "imageTransition");
            startActivity(intent, options.toBundle());
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
    private void startPulse() {
        mRipplePulseLayout.startRippleAnimation();
    }

    private void vibEffect(){
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(28, VibrationEffect.DEFAULT_AMPLITUDE));
        }else {
            vibrator.vibrate(28);
        }
    }
    private void vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            vibrator.vibrate(
                    VibrationEffect.startComposition()
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, 0.3f)
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 0.3f)
                            .compose());
        } else {
            long[] pattern = {5, 0, 5, 0, 5, 1, 5, 1, 5, 2, 5, 2, 5, 3, 5, 4, 5, 4, 5, 5, 5, 6, 5, 6, 5, 7, 5, 8, 5, 8, 5, 9, 5, 10};

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