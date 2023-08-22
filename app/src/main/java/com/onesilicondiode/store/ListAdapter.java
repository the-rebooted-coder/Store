package com.onesilicondiode.store;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.joooonho.SelectableRoundedImageView;
import com.pedromassango.doubleclick.DoubleClick;
import com.pedromassango.doubleclick.DoubleClickListener;

import java.util.List;

public class ListAdapter extends ArrayAdapter {
    private final Activity mContext;
    List<Food> foodList;
    Button moreDetails;

    public ListAdapter(Activity mContext, List<Food> foodList){
        super(mContext,R.layout.list_item,foodList);
        this.mContext = mContext;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater =  mContext.getLayoutInflater();
        View listItemView = inflater.inflate(R.layout.list_item,null,true);
        moreDetails = listItemView.findViewById(R.id.moreDetails);
        SelectableRoundedImageView foodImage = listItemView.findViewById(R.id.imageLoader);
        Food food = foodList.get(position);
        String url = food.getImageUrl();
        moreDetails.setOnClickListener(new DoubleClick(new DoubleClickListener() {
            @Override
            public void onSingleClick(View view) {
                vibrateDeviceSecond();
                Toast.makeText(getContext(),"Tap twice to view",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDoubleClick(View view) {
                vibrateDeviceSecond();
                int splash_screen_time_out = 360;
                new Handler().postDelayed(() -> {
                    vibrateDevice();
                }, splash_screen_time_out);
                Toast.makeText(getContext(),"Viewing ",Toast.LENGTH_SHORT).show();
            }
        }));
        Glide.with(getContext())
                .load(url)
                .override(400,500)
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
}