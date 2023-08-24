package com.onesilicondiode.store;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Crash extends AppCompatActivity {
    private FingerprintManagerCompat fingerprintManager;
    private int titleClickCount = 0;
    private static final String KEY_NAME = "my_key_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fingerprintManager = FingerprintManagerCompat.from(this);
        if (fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()) {
            authenticateWithFingerprint();
        } else {
            Toast.makeText(this,"Fingerprint not available, tap the title 4 times to enter pin manually",Toast.LENGTH_LONG).show();
        }
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Initialize the titleClickCount to 0
        titleClickCount = 0;

        // Find the title TextView in the custom dialog layout
        TextView titleTextView = dialogView.findViewById(R.id.title_text_view);

        // Set a click listener on the title TextView
        titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                titleClickCount++;

                // Check if the title is clicked twice
                if (titleClickCount == 2) {
                    showPinCodeDialog();
                }
            }
        });

        builder.setPositiveButton("Open App Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
               recreate();
            }
        });

        // Set the dialog to not be cancelable (user can't dismiss it)
        builder.setCancelable(false);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void authenticateWithFingerprint() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            keyGenerator.init(builder.build());
            SecretKey secretKey = keyGenerator.generateKey();

            Cipher cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            FingerprintManagerCompat.CryptoObject cryptoObject =
                    new FingerprintManagerCompat.CryptoObject(cipher);

            fingerprintManager.authenticate(cryptoObject, 0, null,
                    new FingerprintManagerCompat.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(
                                @NonNull FingerprintManagerCompat.AuthenticationResult result) {
                            // Fingerprint authentication successful, navigate to the next screen
                            Intent intent = new Intent(Crash.this, Landing.class);
                            startActivity(intent);
                            finish(); // Close the current activity
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, CharSequence errString) {
                            // Handle authentication errors
                        }
                    }, null);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception
        }
    }
    private void showPinCodeDialog() {
        // Create a dialog to enter the pin code
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter PIN Code");

        // Set an EditText for the user to enter the PIN
        final EditText pinEditText = new EditText(this);
        builder.setView(pinEditText);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String enteredPin = pinEditText.getText().toString();
                if (enteredPin.equals("2908")) {
                    // If the PIN matches, start another activity
                    Intent intent = new Intent(Crash.this, Landing.class);
                    startActivity(intent);
                    finish(); // Close the current activity
                } else {
                    Toast.makeText(Crash.this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Close the dialog
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}