package com.onesilicondiode.store;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    LottieAnimationView emptyWorld;
    TextView emptyPlaceholder;
    DatabaseReference foodDbAdd;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    private FloatingActionButton showMore;
    private FloatingActionButton fabUpload;
    private FloatingActionButton fabReload;
    private boolean isFABMenuOpen = false;
    // Add a member variable for storing the list of image URLs
    private List<String> imageUrls;
    public static final String UI_MODE = "uiMode";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v3 = inflater.inflate(R.layout.fragment_home, container, false);
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        showMore = v3.findViewById(R.id.more);
        fabUpload = v3.findViewById(R.id.fabUpload);
        fabReload = v3.findViewById(R.id.fabReload);
        // Initialize Firebase Database
        foodDbAdd = FirebaseDatabase.getInstance().getReference("SecureVault/SecureVault");
        emptyWorld = v3.findViewById(R.id.emptyWorld);
        emptyPlaceholder = v3.findViewById(R.id.emptyPlaceholder);
        GridView myListView;
        List<SecureVaultModel> foodList;
        myListView = v3.findViewById(R.id.myGridView);
        foodList = new ArrayList<>();
        imageUrls = new ArrayList<>(); // Initialize the imageUrls list
        fabUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new ShareFragment()) // Replace with the ShareFragment
                        .addToBackStack(null) // Add to back stack to handle navigation
                        .commit();
            }
        });
        showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFABMenu();
            }
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
    private void toggleFABMenu() {
        if (isFABMenuOpen) {
            // Close the FAB menu with animation
            closeFABMenu();
        } else {
            // Open the FAB menu with animation
            openFABMenu();
        }
    }
    private void openFABMenu() {
        isFABMenuOpen = true;

        // Animate the "Upload" FAB
        fabUpload.setVisibility(View.VISIBLE);
        fabUpload.animate().translationY(0);
        fabUpload.animate().alpha(1.0f);

        // Animate the "Reload" FAB
        fabReload.setVisibility(View.VISIBLE);
        fabReload.animate().translationY(0);
        fabReload.animate().alpha(1.0f);
    }
    private void closeFABMenu() {
        isFABMenuOpen = false;

        // Animate the "Upload" FAB
        fabUpload.animate().translationY(0);
        fabUpload.animate().alpha(0.0f);
        fabUpload.setVisibility(View.GONE);

        // Animate the "Reload" FAB
        fabReload.animate().translationY(0);
        fabReload.animate().alpha(0.0f);
        fabReload.setVisibility(View.GONE);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fabReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReloadDialog();
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

    private void showReloadDialog() {
        int nightModeFlags =
                this.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Theme for Store"); // Set the dialog title
        // Set the positive button and its click listener
        builder.setPositiveButton("LIGHT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        SharedPreferences.Editor editor = getContext().getSharedPreferences(UI_MODE, MODE_PRIVATE).edit();
                        editor.putString("uiMode","Light");
                        editor.apply();
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        Toast.makeText(getContext(),"Already in Light Mode ☀️",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        break;
                    default:
                        Toast.makeText(getContext(),"Choose a theme for Store",Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss(); // Dismiss the dialog
            }
        });

        // Set the negative button and its click listener
        builder.setNegativeButton("DARK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        Toast.makeText(getContext(),"Already in Dark Mode \uD83C\uDF19",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        SharedPreferences.Editor editor = getContext().getSharedPreferences(UI_MODE, MODE_PRIVATE).edit();
                        editor.putString("uiMode","Dark");
                        editor.apply();
                        break;
                    default:
                        Toast.makeText(getContext(),"Choose a theme",Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss(); // Dismiss the dialog
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}