package com.onesilicondiode.store;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.pedromassango.doubleclick.DoubleClick;
import com.pedromassango.doubleclick.DoubleClickListener;

import java.io.File;
import android.Manifest;
import java.util.List;

public class ListAdapter extends ArrayAdapter<SecureVaultModel> {
    private final Activity mContext;
    private List<SecureVaultModel> foodList;

    public ListAdapter(Activity mContext, List<SecureVaultModel> foodList) {
        super(mContext, R.layout.list_item, foodList);
        this.mContext = mContext;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = mContext.getLayoutInflater();
        View listItemView = inflater.inflate(R.layout.list_item, null, true);
        ImageView foodImage = listItemView.findViewById(R.id.imageLoader);
        SecureVaultModel food = foodList.get(position);
        String url = food.getImageUrl();

        foodImage.setOnClickListener(new DoubleClick(new DoubleClickListener() {
            @Override
            public void onSingleClick(View view) {
                vibrateDeviceSecond();
                Toast.makeText(getContext(), "Tap twice to view fullscreen", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDoubleClick(View view) {
                vibrateDeviceSecond();
                int splash_screen_time_out = 360;
                new Handler().postDelayed(() -> {
                    vibrateDevice();
                }, splash_screen_time_out);

                // Show the image in a full-screen dialog when double-clicked
                showImageFullScreenDialog(url);
            }
        }));

        Glide.with(getContext())
                .load(url)
                .override(500, 500)
                .fitCenter()
                .into(foodImage);
        return listItemView;
    }

    private void vibrateDeviceSecond() {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(32, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(32);
        }
    }

    private void vibrateDevice() {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(30);
        }
    }

    // Method to show the image in a full-screen dialog
    private void showImageFullScreenDialog(String imageUrl) {
        final Dialog dialog = new Dialog(mContext, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.full_screen_image_dialog);

        ImageView fullScreenImageView = dialog.findViewById(R.id.fullScreenImageView);
        Button downloadButton = dialog.findViewById(R.id.downloadButton);

        Glide.with(mContext)
                .load(imageUrl)
                .into(fullScreenImageView);

        // Handle the download button click event
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for permission to write to external storage
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted, request it from the user
                    ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                    Toast.makeText(mContext, "Tap Re-Save Again", Toast.LENGTH_LONG).show();
                } else {
                    // Permission is granted, proceed with download
                    initiateDownload(imageUrl);
                    // Dismiss the dialog after initiating the download
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    // Request code for write external storage permission
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private void initiateDownload(String imageUrl) {
        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(imageUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle("Saving Memories");
        request.setDescription("Downloading image...");

        // Set the destination directory for the downloaded image
        File destinationDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Memories");
        destinationDirectory.mkdirs();
        request.setDestinationUri(Uri.fromFile(new File(destinationDirectory, "memories-offloaded.jpg")));

        downloadManager.enqueue(request);

        // Show a toast to inform the user about the download
        Toast.makeText(mContext, "Download started...", Toast.LENGTH_SHORT).show();
    }
}