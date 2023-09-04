package com.onesilicondiode.store;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class BottomSheetCrash extends BottomSheetDialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate your bottom sheet layout here
        View view = inflater.inflate(R.layout.bottom_sheet_layout, container, false);
        TextInputLayout inputLayoutPassword = view.findViewById(R.id.inputLayoutPassword);
        TextInputEditText inputPassword = view.findViewById(R.id.inputPassword);
        Button unlockButton = view.findViewById(R.id.unlockApp);
        Animation shakeAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.shake);
        unlockButton.setOnClickListener(v -> {
            // Get the entered PIN
            String enteredPin = inputPassword.getText().toString();

            // Check if the entered PIN is correct (e.g., "2908")
            if (enteredPin.equals("2908")) {
                // PIN is correct, navigate to Landing Activity
                Intent intent = new Intent(getActivity(), Landing.class);
                startActivity(intent);
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
}