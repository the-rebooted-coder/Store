package com.onesilicondiode.store;

import static android.content.Context.MODE_PRIVATE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    public static final String UI_MODE = "uiMode";
    LottieAnimationView emptyWorld;
    TextView emptyPlaceholder;
    DatabaseReference foodDbAdd;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    private ExtendedFloatingActionButton fabUpload;

    private List<String> imageUrls;
    private boolean isFabMenuOpen = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v3 = inflater.inflate(R.layout.fragment_home, container, false);
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        fabUpload = v3.findViewById(R.id.fabUpload);
        // Initialize Firebase Database
        foodDbAdd = FirebaseDatabase.getInstance().getReference("SecureVault/SecureVault");
        emptyWorld = v3.findViewById(R.id.emptyWorld);
        emptyPlaceholder = v3.findViewById(R.id.emptyPlaceholder);
        GridView myListView;
        List<SecureVaultModel> foodList;
        myListView = v3.findViewById(R.id.myGridView);
        foodList = new ArrayList<>();
        imageUrls = new ArrayList<>(); // Initialize the imageUrls list
        fabUpload.setOnClickListener(view -> {
            BottomSheetUpload bottomSheetFragment = new BottomSheetUpload();
            bottomSheetFragment.show(getParentFragmentManager(), bottomSheetFragment.getTag());
        });
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

    // Add a method to start displaying images
    private void startDisplayingImages(List<String> urls) {
        for (String url : urls) {
            ImageView imageView = new ImageView(getActivity());
            Glide.with(getContext())
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }
    }

    private void showReloadDialog() {
        int nightModeFlags =
                getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Choose Theme for Store")
                .setPositiveButton("LIGHT", (dialog, which) -> {
                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            SharedPreferences.Editor editor = requireContext().getSharedPreferences(UI_MODE, MODE_PRIVATE).edit();
                            editor.putString("uiMode", "Light");
                            editor.apply();
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                            Toast.makeText(requireContext(), "Already in Light Mode ☀️", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(requireContext(), "Choose a theme for Store", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("DARK", (dialog, which) -> {
                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            Toast.makeText(requireContext(), "Already in Dark Mode \uD83C\uDF19", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            SharedPreferences.Editor editor = requireContext().getSharedPreferences(UI_MODE, MODE_PRIVATE).edit();
                            editor.putString("uiMode", "Dark");
                            editor.apply();
                            break;
                        default:
                            Toast.makeText(requireContext(), "Choose a theme", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        animateFabIn();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isFabMenuOpen) {
            animateFabOut();
        }
    }

    private void animateFabIn() {
        if (!isFabMenuOpen) {
            isFabMenuOpen = true;
            fabUpload.setVisibility(View.VISIBLE);
            fabUpload.setScaleX(0f);
            fabUpload.setScaleY(0f);
            fabUpload.setAlpha(0f);
            fabUpload.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            // Animation completed
                        }
                    })
                    .start();
        }
    }

    private void animateFabOut() {
        if (isFabMenuOpen) {
            isFabMenuOpen = false;
            fabUpload.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            // Animation completed
                            fabUpload.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }
    }
}