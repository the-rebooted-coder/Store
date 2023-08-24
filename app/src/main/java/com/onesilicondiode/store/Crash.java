package com.onesilicondiode.store;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Crash extends AppCompatActivity {

    private int titleClickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        builder.setCancelable(false); // Prevent the dialog from being canceled by pressing outside

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}