package com.onesilicondiode.store;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeFragment extends Fragment {
    public static final String UI_MODE = "uiMode";
    private static final String TEXT_FILE_URL = "https://the-rebooted-coder.github.io/Store/store-update.txt";
    private static final String APK_DOWNLOAD_URL = "https://the-rebooted-coder.github.io/Store/Store.apk";
    private static final String UPDATE_CHANGELOG = "https://the-rebooted-coder.github.io/Store/update_changelog.txt";
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    LottieAnimationView emptyWorld;
    TextView emptyPlaceholder;
    DatabaseReference foodDbAdd;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    private FloatingActionButton showMore, fabUpload, fabReload, fabUpdate;
    private boolean isFABMenuOpen = false;
    // Add a member variable for storing the list of image URLs
    private List<String> imageUrls;
    private ActivityResultLauncher<String> requestPermissionLauncher;


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
        fabUpdate = v3.findViewById(R.id.fabUpdate);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                initiateApkDownload();
            } else {
                // Permission denied, show a message or take appropriate action
                if (getView() != null) {
                    Snackbar.make(getView(), "Storage Permission Denied, Can't Download Update 😔", Snackbar.LENGTH_LONG).show();
                }
            }
        });
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
        fabUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable()) {
                    Animation animation = new AlphaAnimation(1f, 0f);
                    animation.setDuration(500); // Duration in milliseconds
                    animation.setFillAfter(true);
                    // Set an animation listener to make the button invisible when the animation is done
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            fabUpdate.setVisibility(View.GONE);
                            fabReload.setVisibility(View.GONE);
                            fabUpload.setVisibility(View.GONE);
                        }
                    });
                    performReadTextFile();
                }
            }
        });
        showMore.setOnClickListener(view -> toggleFABMenu());
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

    private void performReadTextFile() {
        // Create a new thread to perform the file reading task
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(new ReadTextFileTask());

        // Handle the result when the task is completed
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final String result = future.get();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handleReadTextFileResult(result);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("");
        }
        reader.close();
        return stringBuilder.toString();
    }

    private void handleReadTextFileResult(String result) {
        if (containsNumberGreaterThanZero(result)) {
            new FetchTextTask().execute(UPDATE_CHANGELOG);
        } else {
            if (getView() != null) {
                Snackbar snackbar = Snackbar.make(getView(), "Already on the latest version - duh 🤷‍♂️", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
    }

    private void initiateApkDownload() {
        // Create a download request for the APK file
        if (getContext() != null) {
            DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(APK_DOWNLOAD_URL);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setTitle("Store Update");
            request.setDescription("Please Wait...");
            File destinationDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Vault");
            destinationDirectory.mkdirs();
            request.setDestinationUri(Uri.fromFile(new File(destinationDirectory, "Vault-Update.apk")));
            downloadManager.enqueue(request);
            Toast.makeText(getContext(), "Download started, check notification for progress 🚀", Toast.LENGTH_LONG).show();
        }
    }

    private String readChangelog(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        reader.close();
        return stringBuilder.toString();
    }

    private boolean hasWriteExternalStoragePermission() {
        // Check if the app has write external storage permission
        return ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestWriteExternalStoragePermission() {
        // Request write external storage permission
        requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private boolean containsNumberGreaterThanZero(String text) {
        // This method checks if the provided text contains any number greater than 0
        // You can modify this method based on the structure of your online text file
        // For this example, we'll check for any numeric value greater than 0
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group());
            if (value > 2) {
                return true;
            }
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        if (getActivity() != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            }
        }
        return false;
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

        // Animate the "Update" FAB
        fabUpdate.setVisibility(View.VISIBLE);
        fabUpdate.animate().translationY(0);
        fabUpdate.animate().alpha(1.0f);
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

        // Animate the "Update" FAB
        fabUpdate.animate().translationY(0);
        fabUpdate.animate().alpha(0.0f);
        fabUpdate.setVisibility(View.GONE);
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
                        editor.putString("uiMode", "Light");
                        editor.apply();
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        Toast.makeText(getContext(), "Already in Light Mode ☀️", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        break;
                    default:
                        Toast.makeText(getContext(), "Choose a theme for Store", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(), "Already in Dark Mode \uD83C\uDF19", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        SharedPreferences.Editor editor = getContext().getSharedPreferences(UI_MODE, MODE_PRIVATE).edit();
                        editor.putString("uiMode", "Dark");
                        editor.apply();
                        break;
                    default:
                        Toast.makeText(getContext(), "Choose a theme", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss(); // Dismiss the dialog
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private class FetchTextTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                result = readChangelog(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result;
        }
        @Override
        protected void onPostExecute(String changelog) {
            super.onPostExecute(changelog);
            // Show a dialog with the changelog and an "Okay" button
            if (!changelog.isEmpty() && getActivity() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("What's New in Store");
                builder.setIcon(R.drawable.update_icon);
                builder.setMessage(changelog+"\n\nTap on Begin to Start the Update Download."); // Use the changelog text as the message
                builder.setPositiveButton("BEGIN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Dismiss the dialog
                        if (hasWriteExternalStoragePermission()) {
                            // Initiate APK download now
                            initiateApkDownload();
                        } else {
                            // Permission not granted, request it
                            requestWriteExternalStoragePermission();
                        }
                    }
                });
                builder.show();
            }
        }
    }

    private class ReadTextFileTask implements Callable<String> {

        @Override
        public String call() {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(TEXT_FILE_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                result = readStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result;
        }
    }
}