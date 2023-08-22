package com.onesilicondiode.store;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class ShareFragment extends Fragment {

    MaterialButton share;
    DatabaseReference foodDbAdd;
    ImageView foodImage;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 22;
    FirebaseStorage storage;
    StorageReference storageReference;
    AlertDialog.Builder builder;
    private ProgressBar progressBar;
    private TextView progressText;
    private Dialog progressDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v2 = inflater.inflate(R.layout.fragment_share, container, false);
        progressDialog = createProgressDialog(getContext());
        share = v2.findViewById(R.id.shareFood);
        foodImage = v2.findViewById(R.id.foodImage);
        foodDbAdd = FirebaseDatabase.getInstance().getReference().child("SecureVault");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        foodImage.setOnClickListener(v -> selectImage());
        share.setOnClickListener(v -> {
            if (filePath != null) {
                vibrateDevice();
                SecureVaultModel food1 = new SecureVaultModel();
                DatabaseReference specimenReference = foodDbAdd.child("SecureVault").push();
                food1.setImageUrl("");
                specimenReference.setValue(food1);
                String key = specimenReference.getKey();
                food1.setKey(key);

                StorageReference ref = storageReference.child("secureVault/" + filePath.getLastPathSegment());
                UploadTask uploadTask = ref.putFile(filePath);

                // Set up a progress listener for the upload task
                uploadTask.addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    // Update the progressText in your custom ProgressDialog
                    progressText.setText("Uploading: " + (int) progress + "%");
                    // Update the ProgressBar
                    progressBar.setProgress((int) progress);
                });

                uploadTask.addOnSuccessListener(
                                taskSnapshot -> {
                                    Task<Uri> downloadUrl = ref.getDownloadUrl();
                                    downloadUrl.addOnSuccessListener(uri -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity().getApplicationContext(), "Food Details Shared Successfully!", Toast.LENGTH_SHORT).show();
                                        final Handler handler = new Handler();
                                        handler.postDelayed(() -> vibrateDeviceThird(), 100);
                                        final Handler handler2 = new Handler();
                                        handler2.postDelayed(() -> vibrateDevice(), 300);
                                        String imageReference = uri.toString();
                                        foodDbAdd.child("SecureVault").child(food1.getKey()).child("imageUrl").setValue(imageReference);
                                        food1.setImageUrl(imageReference);
                                    });
                                })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity().getApplicationContext(),
                                            "Image Upload Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        });

                progressDialog.setCancelable(false);
                progressDialog.show();
            } else {
                final Handler handler = new Handler();
                handler.postDelayed(() -> vibrateDevice(), 100);
                vibrateDeviceThird();
                Toast.makeText(getActivity().getApplicationContext(), "Image is required", Toast.LENGTH_SHORT).show();
            }
        });
        return v2;
    }

    public Dialog createProgressDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.progress_dialog);
        dialog.setCancelable(false); // Prevent user from dismissing it
        progressBar = dialog.findViewById(R.id.progressBar);
        progressText = dialog.findViewById(R.id.progressText);
        progressBar.setMax(100);
        return dialog;
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
        vibrateDeviceThird();
        // Show the custom ProgressDialog
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode,
                resultCode,
                data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            // Get the Uri of data
            try {
                filePath = data.getData();
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getActivity().getContentResolver(),
                                filePath);
                foodImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Dismiss the ProgressDialog when the fragment is destroyed
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void vibrateDevice() {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(32, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    private void vibrateDeviceThird() {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(36, VibrationEffect.DEFAULT_AMPLITUDE));
    }
}