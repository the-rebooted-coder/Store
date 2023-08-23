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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    LottieAnimationView lottieAnimationView2;
    TextView placeHolder;
    DatabaseReference foodDbAdd;
    FirebaseAuth auth;
    FirebaseUser currentUser;

    // Add a member variable for storing the list of image URLs
    private List<String> imageUrls;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v3 = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Initialize Firebase Database
        foodDbAdd = FirebaseDatabase.getInstance().getReference("SecureVault/SecureVault");

        lottieAnimationView2 = v3.findViewById(R.id.animation_view_here2);
        placeHolder = v3.findViewById(R.id.placeHolderText);
        ListView myListView;
        List<SecureVaultModel> foodList;
        myListView = v3.findViewById(R.id.myListView);
        foodList = new ArrayList<>();
        imageUrls = new ArrayList<>(); // Initialize the imageUrls list

        // Check if a user is authenticated
        if (currentUser != null) {
            // Get the current user's UID
            String userId = currentUser.getUid();

            // Modify your query to filter data based on the user's UID
            Query query = foodDbAdd.orderByChild("userId").equalTo(userId);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    myListView.setVisibility(View.GONE);
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
                                // DO NOT REMOVE THIS EMPTY CATCH
                            }
                        }

                        ListAdapter adapter = new ListAdapter(getActivity(), foodList);
                        myListView.setAdapter(adapter);

                        // Start displaying images here
                        startDisplayingImages(imageUrls);
                    } catch (Exception e) {
                        // DO NOT REMOVE THIS EMPTY CATCH
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
        } else {
            // Handle the case where the user is not authenticated
            // You can display a message or redirect the user to login here
        }

        return v3;
    }

    // Add a method to start displaying images
    private void startDisplayingImages(List<String> urls) {
        for (String url : urls) {
            ImageView imageView = new ImageView(getActivity());
            Glide.with(getContext())
                    .load(url)
               //     .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
            // Add the ImageView to your layout where you want to display the images
        }
    }
}