package com.onesilicondiode.store;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    AlertDialog progressDialog;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v2 = inflater.inflate(R.layout.fragment_share, container, false);
        progressDialog = getDialogProgressBar().create();
        share = v2.findViewById(R.id.shareFood);
        foodImage = v2.findViewById(R.id.foodImage);
        foodDbAdd = FirebaseDatabase.getInstance().getReference().child("SecureVault");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        foodImage.setOnClickListener(v -> selectImage());
        share.setOnClickListener(v -> {
          if (filePath != null){
                    vibrateDevice();
                    Food food1 = new Food();
                    DatabaseReference specimenReference = foodDbAdd.child("SecureVault").push();
                    food1.setImageUrl("");
                    specimenReference.setValue(food1);
                    String key = specimenReference.getKey();
                    food1.setKey(key);
                    StorageReference ref
                            = storageReference
                            .child(
                                    "secureVault/"
                                            + filePath.getLastPathSegment());
                    ref.putFile(filePath)
                            .addOnProgressListener(snapshot -> {
                                progressDialog.setCancelable(false);
                                progressDialog.show();

                            })
                            .addOnSuccessListener(
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
                            .addOnFailureListener(e -> Toast.makeText(getActivity().getApplicationContext(),
                                    "Image Upload Failed " + e.getMessage(),
                                    Toast.LENGTH_SHORT)
                                    .show());
            }
            else {
                final Handler handler = new Handler();
                handler.postDelayed(() -> vibrateDevice(), 100);
                vibrateDeviceThird();
                Toast.makeText(getActivity().getApplicationContext(),"Image is required",Toast.LENGTH_SHORT).show();
            }
        });
        return v2;
    }
    public AlertDialog.Builder getDialogProgressBar() {

        if (builder == null) {
            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Storing in Vault ✨\n");
            builder.setMessage("Please Wait!");
            final ProgressBar progressBar = new ProgressBar(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            progressBar.setLayoutParams(lp);
            builder.setView(progressBar);
        }
        return builder;
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
        Toast.makeText(getContext(),"Pick an image",Toast.LENGTH_SHORT).show();
        final Handler handler = new Handler();
        handler.postDelayed(() -> vibrateDevice(), 100);
    }
    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data)
    {

        super.onActivityResult(requestCode,
                resultCode,
                data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

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
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
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