package com.onesilicondiode.store;

import static android.content.Context.CONNECTIVITY_SERVICE;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    LottieAnimationView lottieAnimationView2;
    TextView placeHolder;
    DatabaseReference foodDbAdd = FirebaseDatabase.getInstance().getReference("SecureVault/SecureVault");

    // Add a member variable for storing the list of image URLs
    private List<String> imageUrls;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v3 = inflater.inflate(R.layout.fragment_home, container, false);
        lottieAnimationView2 = v3.findViewById(R.id.animation_view_here2);
        placeHolder = v3.findViewById(R.id.placeHolderText);
            ListView myListView;
            List<SecureVaultModel> foodList;
            myListView = v3.findViewById(R.id.myListView);
            foodList = new ArrayList<>();
            imageUrls = new ArrayList<>(); // Initialize the imageUrls list

            foodDbAdd.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    myListView.setVisibility(View.INVISIBLE);
                    try {
                        foodList.clear();
                        imageUrls.clear(); // Clear the existing URLs

                        for (DataSnapshot foodDatastamp : snapshot.getChildren()) {
                            SecureVaultModel food = foodDatastamp.getValue(SecureVaultModel.class);
                            try {
                                foodList.add(food);
                                // Add the image URL to the list
                                imageUrls.add(food.getImageUrl());
                            } catch (NullPointerException e) {
                                //DO NOT REMOVE THIS EMPTY CATCH
                            }
                        }

                        ListAdapter adapter = new ListAdapter(getActivity(), foodList);
                        myListView.setAdapter(adapter);

                        // Start displaying images here
                        startDisplayingImages(imageUrls);
                    } catch (Exception e) {
                        //DO NOT REMOVE THIS EMPTY CATCH
                    }
                    int splash_screen_time_out = 1500;
                    new Handler().postDelayed(() -> {
                        lottieAnimationView2.setVisibility(View.GONE);
                        myListView.setVisibility(View.VISIBLE);
                        placeHolder.setVisibility(View.GONE);
                    }, splash_screen_time_out);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        return v3;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fabReload = view.findViewById(R.id.fabReload);
        fabReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle FAB click here
                if(haveNetwork()){
                    FirebaseDatabase.getInstance().goOffline();
                    FirebaseDatabase.getInstance().goOnline();
                    startDisplayingImages(imageUrls);
                    Toast.makeText(getContext(),"Refreshing",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getContext(),"No Internet",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    // Add a method to start displaying images
    private void startDisplayingImages(List<String> urls) {
        for (String url : urls) {
            ImageView imageView = new ImageView(getActivity());
            Glide.with(getContext())
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
            // Add the ImageView to your layout where you want to display the images
        }
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
}

