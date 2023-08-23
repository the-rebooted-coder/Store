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
    LottieAnimationView lottieAnimationView2, emptyWorld;
    TextView placeHolder, emptyPlaceholder;
    DatabaseReference foodDbAdd;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    private View loadingView;

    // Add a member variable for storing the list of image URLs
    private List<String> imageUrls;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v3 = inflater.inflate(R.layout.fragment_home, container, false);
        loadingView = inflater.inflate(R.layout.loading_layout, container, false);
        container.addView(loadingView);
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Initialize Firebase Database
        foodDbAdd = FirebaseDatabase.getInstance().getReference("SecureVault/SecureVault");
        emptyWorld = v3.findViewById(R.id.emptyWorld);
        emptyPlaceholder = v3.findViewById(R.id.emptyPlaceholder);
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
                    try {
                        foodList.clear();
                        imageUrls.clear();
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
                        if (imageUrls.isEmpty()) {
                            // Display a toast message indicating no images
                            myListView.setVisibility(View.GONE); // Hide the ListView
                            emptyPlaceholder.setVisibility(View.VISIBLE);
                            emptyWorld.setVisibility(View.VISIBLE);
                        } else {
                            ListAdapter adapter = new ListAdapter(getActivity(), foodList);
                            myListView.setAdapter(adapter);
                            myListView.setVisibility(View.VISIBLE);
                            emptyPlaceholder.setVisibility(View.GONE);
                            emptyWorld.setVisibility(View.GONE);
                            // Start displaying images here
                            startDisplayingImages(imageUrls);

                            // Remove the loading UI if it was added
                            ViewGroup container = (ViewGroup) getView();
                            if (container != null && loadingView != null) {
                                container.removeView(loadingView);
                            }
                        }

                    } catch (Exception e) {
                        // DO NOT REMOVE THIS EMPTY CATCH
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
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
                if (haveNetwork()) {
                    // Refresh data from Firebase
                    Toast.makeText(getContext(), "Refreshing", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "No Internet", Toast.LENGTH_SHORT).show();
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