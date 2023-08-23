package com.onesilicondiode.store;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.joooonho.SelectableRoundedImageView;
import com.pedromassango.doubleclick.DoubleClick;
import com.pedromassango.doubleclick.DoubleClickListener;

import java.util.List;

public class ListAdapter extends ArrayAdapter<SecureVaultModel> {
    private final Activity mContext;
    private List<SecureVaultModel> foodList;

    public ListAdapter(Activity mContext, List<SecureVaultModel> foodList){
        super(mContext,R.layout.list_item,foodList);
        this.mContext = mContext;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater =  mContext.getLayoutInflater();
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
                Toast.makeText(getContext(), "Viewing", Toast.LENGTH_SHORT).show();

                // Show the image in a full-screen dialog when double-clicked
                showImageFullScreenDialog(url);
            }
        }));

        Glide.with(getContext())
                .load(url)
                .override(500, 500)
         //       .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(foodImage);
        return listItemView;
    }

    private void vibrateDeviceSecond() {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(32, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    private void vibrateDevice() {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    // Method to show the image in a full-screen dialog
    private void showImageFullScreenDialog(String imageUrl) {
        final Dialog dialog = new Dialog(mContext, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.full_screen_image_dialog);

        ImageView fullScreenImageView = dialog.findViewById(R.id.fullScreenImageView);

        Glide.with(mContext)
                .load(imageUrl)
                .into(fullScreenImageView);

        fullScreenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}