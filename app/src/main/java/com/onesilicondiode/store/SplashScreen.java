package com.onesilicondiode.store;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.FirebaseDatabase;

public class SplashScreen extends AppCompatActivity {
    String name;
    public static final String UI_MODE = "uiMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences(UI_MODE, MODE_PRIVATE);
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            Log.e("FireBaseErr",e.toString());
        }
        name = prefs.getString("uiMode", "Light");
        applyUI();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        one();
        fireSplashScreen();
    }

    private void applyUI() {
        if (name.equals("Dark")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else if (name.equals("Light")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    private void one() {
        int feelUI = 860;
        two();
        new Handler().postDelayed(this::vibrateDevice, feelUI);
    }
    private void two() {
        int feelUItwo = 920;
        three();
        new Handler().postDelayed(this::vibrateDeviceSecond, feelUItwo);
    }
    private void three() {
        int feelUIthree = 980;
        four();
        new Handler().postDelayed(this::vibrateDeviceThird, feelUIthree);
    }
    private void four() {
        int feelUIfour = 1040;
        five();
        new Handler().postDelayed(this::vibrateDeviceFourth, feelUIfour);
    }
    private void five() {
        int feelUIfive = 1100;
        six();
        new Handler().postDelayed(this::vibrateDeviceFifth, feelUIfive);
    }
    private void six() {
        int feelUIsix = 1160;
        seven();
        new Handler().postDelayed(this::vibrateDeviceSixth, feelUIsix);
    }
    private void seven() {
        int feelUIseven = 1220;
        new Handler().postDelayed(this::vibrateDeviceSeventh, feelUIseven);
    }

    private void fireSplashScreen() {
        int splash_screen_time_out = 2000;
        new Handler().postDelayed(() -> {
            check();
        }, splash_screen_time_out);
    }

    private void check() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (getIntent() != null && getIntent().hasExtra("key_from_my_app")) {
            Toast.makeText(this,"Launching from Anumi, Bypassing Lock Screen",Toast.LENGTH_LONG).show();
            int splash_screen_time_out = 300;
            new Handler().postDelayed(() -> {
                Intent i = new Intent(SplashScreen.this, Landing.class);
                startActivity(i);
                finish();
            }, splash_screen_time_out);
        }
        else if (account != null) {
            //User Signed In, Proceeding to Landing
            int splash_screen_time_out = 300;
            new Handler().postDelayed(() -> {
                Intent i = new Intent(SplashScreen.this, Crash.class);
                startActivity(i);
                finish();
            }, splash_screen_time_out);
        }
        else {
            //Newbie
            int splash_screen_time_out = 1000;
            new Handler().postDelayed(() -> {
                Intent i = new Intent(SplashScreen.this, SignUp.class);
                startActivity(i);
                finish();
            }, splash_screen_time_out);
        }
    }

    private void vibrateDeviceSecond() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(32, VibrationEffect.DEFAULT_AMPLITUDE));
    }
    private void vibrateDeviceThird() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(36, VibrationEffect.DEFAULT_AMPLITUDE));
    }
    private void vibrateDeviceFourth() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
    }
    private void vibrateDeviceFifth() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE));
    }
    private void vibrateDeviceSixth() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
    }
    private void vibrateDeviceSeventh() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(52, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibrator.vibrate(44);
        }
    }
    private void vibrateDevice() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(28, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibrator.vibrate(25);
        }
    }
}