package com.onesilicondiode.store;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static android.content.Context.CONNECTIVITY_SERVICE;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShareFragment extends Fragment {
    private final int PICK_IMAGE_REQUEST = 22;
    MaterialButton share;
    DatabaseReference foodDbAdd;
    ImageView foodImage;
    FirebaseStorage storage;
    StorageReference storageReference;
    private Uri filePath;
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
            if (haveNetwork()) {
                if (filePath != null) {
                    vibrateDevice();
                    SecureVaultModel food1 = new SecureVaultModel();

                    // Obtain the current user's UID from Firebase Authentication
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        food1.setUserId(userId); // Set the user's UID

                        DatabaseReference specimenReference = foodDbAdd.child("SecureVault").push();
                        food1.setImageUrl("");
                        specimenReference.setValue(food1);
                        String key = specimenReference.getKey();
                        food1.setKey(key);

                        // Compress the selected image to WebP format
                        try {
                            Bitmap selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            selectedImage.compress(Bitmap.CompressFormat.WEBP, 90, byteArrayOutputStream);
                            byte[] webpData = byteArrayOutputStream.toByteArray();
                            filePath = saveWebPImage(webpData);

                            // Now, you have the compressed image in filePath
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        StorageReference ref = storageReference.child("secureVault/" + filePath.getLastPathSegment());
                        UploadTask uploadTask = ref.putFile(filePath);
                        share.setEnabled(false);

                        // Set up a progress listener for the upload task
                        uploadTask.addOnProgressListener(snapshot -> {
                            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                            // Update the progressText in your custom ProgressDialog
                            progressText.setText("Encrypting: " + (int) progress + "%");
                            // Update the ProgressBar
                            progressBar.setProgress((int) progress);
                        });

                        uploadTask.addOnSuccessListener(
                                        taskSnapshot -> {
                                            Task<Uri> downloadUrl = ref.getDownloadUrl();
                                            downloadUrl.addOnSuccessListener(uri -> {
                                                progressDialog.dismiss();
                                                vibrateWithFeel();
                                                Toast.makeText(getActivity().getApplicationContext(), "Stored in Vault Successfully!", Toast.LENGTH_SHORT).show();
                                                final Handler handler = new Handler();
                                                handler.postDelayed(() -> vibrateDeviceThird(), 100);
                                                final Handler handler2 = new Handler();
                                                handler2.postDelayed(() -> vibrateDevice(), 300);
                                                String imageReference = uri.toString();
                                                foodDbAdd.child("SecureVault").child(food1.getKey()).child("imageUrl").setValue(imageReference);
                                                food1.setImageUrl(imageReference);
                                                share.setEnabled(true);
                                            });
                                        })
                                .addOnFailureListener(e -> {
                                    share.setEnabled(true);
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
                        // Handle the case where the user is not authenticated
                        Toast.makeText(getActivity().getApplicationContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    final Handler handler = new Handler();
                    handler.postDelayed(() -> vibrateDevice(), 100);
                    vibrateDeviceThird();
                    Toast.makeText(getActivity().getApplicationContext(), "Tap on the image-lock above and select an image first!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "No Internet 😔", Toast.LENGTH_SHORT).show();
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
            filePath = data.getData();
            try {
                // Display the selected image
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(32, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            // For versions lower than Oreo
            vibrator.vibrate(32);
        }
    }

    private void vibrateWithFeel() {
        long[] pattern = {5, 0, 5, 0, 5, 1, 5, 1, 5, 2, 5, 2, 5, 3, 5, 4, 5, 4, 5, 5, 5, 6, 5, 6, 5};
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, -1);
            vibrator.vibrate(vibrationEffect);
        } else {
            // For versions lower than Oreo
            vibrator.vibrate(pattern, -1);
        }
    }

    private void vibrateDeviceThird() {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(36, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    private boolean haveNetwork() {
        boolean have_WIFI = false;
        boolean have_MobileData = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo info : networkInfos) {
            if (info.getTypeName().equalsIgnoreCase("WIFI"))
                if (info.isConnected())
                    have_WIFI = true;
            if (info.getTypeName().equalsIgnoreCase("MOBILE"))
                if (info.isConnected())
                    have_MobileData = true;
        }
        return have_MobileData || have_WIFI;
    }

    private Uri saveWebPImage(byte[] webpData) {
        try {
            // Generate a unique filename using a timestamp
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filename = "compressed_image_" + timestamp + ".webp";
            FileOutputStream outputStream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(webpData);
            outputStream.close();
            return Uri.fromFile(getActivity().getFileStreamPath(filename));
        } catch (IOException e) {
            Log.e(TAG, "Error saving WebP image: " + e.getMessage());
            return null;
        }
    }
}