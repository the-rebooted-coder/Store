package com.onesilicondiode.store;

import static android.content.Context.VIBRATOR_SERVICE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Journal extends Fragment implements JournalAdapter.OnItemClickListener {
    private ExtendedFloatingActionButton fab;
    private FloatingActionButton calendarView;
    private RecyclerView journalRecyclerView;
    private FirebaseUser currentUser;
    private DatabaseReference journalDatabase;
    private Vibrator vibrator;
    private JournalAdapter journalAdapter;
    private boolean isFabMenuOpen = false;
    private List<JournalEntry> journalEntries = new ArrayList<>();
    private int streak = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal, container, false);

        fab = view.findViewById(R.id.fab);
        calendarView = view.findViewById(R.id.selectDateButton);
        journalRecyclerView = view.findViewById(R.id.journalRecyclerView);

        // Initialize Firebase Database reference
        journalDatabase = FirebaseDatabase.getInstance().getReference().child("JournalEntries");
        calendarView.setOnClickListener(v -> showDatePickerDialog());
        // Get the current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (getActivity() != null) {
            vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
        }
        // Set up RecyclerView and Adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        journalRecyclerView.setLayoutManager(layoutManager);
        journalAdapter = new JournalAdapter(getContext(), journalEntries, this);
        journalRecyclerView.setAdapter(journalAdapter);

        // Set an onClickListener for the FAB to add a new journal entry
        fab.setOnClickListener(v -> {
            vibrateToEnter();
            checkIfEntryExistsForToday();
        });

        // Retrieve and display journal entries for the current user
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userJournalRef = journalDatabase.child(userId);

            userJournalRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    journalEntries.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        JournalEntry entry = snapshot.getValue(JournalEntry.class); // Ensure correct mapping
                        if (entry != null) {
                            journalEntries.add(entry);
                        }
                    }

                    // Sort entries by date (newest first)
                    Collections.sort(journalEntries, Collections.reverseOrder());

                    // Notify the adapter that the data has changed
                    journalAdapter.notifyDataSetChanged();
                    if (journalEntries.isEmpty()) {
                        TextView noEntriesTextView = view.findViewById(R.id.noEntriesTextView);
                        ImageView noEntriesImageView = view.findViewById(R.id.noEntriesImageView);
                        noEntriesTextView.setVisibility(View.VISIBLE);
                        noEntriesImageView.setVisibility(View.VISIBLE);
                    } else {
                        TextView noEntriesTextView = view.findViewById(R.id.noEntriesTextView);
                        ImageView noEntriesImageView = view.findViewById(R.id.noEntriesImageView);
                        noEntriesTextView.setVisibility(View.GONE);
                        noEntriesImageView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors here
                }
            });
        }

        return view;
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
    private void checkAndUpdateStreak() {
        // Get the current user's journal entries
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userJournalRef = journalDatabase.child(userId);

            userJournalRef.orderByChild("date").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        DataSnapshot latestEntrySnapshot = dataSnapshot.getChildren().iterator().next();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                        // Get the date string from the latest entry
                        String latestDateString = latestEntrySnapshot.child("date").getValue(String.class);
                        String formattedCurrentDateString = dateFormat.format(new Date());

                        try {
                            // Parse the date strings into Date objects
                            Date latestDate = dateFormat.parse(latestDateString);
                            Date formattedCurrentDate = dateFormat.parse(formattedCurrentDateString);

                            if (latestDate != null && formattedCurrentDate != null) {
                                Calendar latestCalendar = Calendar.getInstance();
                                latestCalendar.setTime(latestDate);
                                Calendar currentCalendar = Calendar.getInstance();
                                currentCalendar.setTime(formattedCurrentDate);

                                // Check if the latest entry's date is one day before the current date
                                if (isConsecutiveDates(latestCalendar, currentCalendar)) {
                                    streak++;
                                    Toast.makeText(getContext(), streak, Toast.LENGTH_SHORT).show();
                                } else {
                                    // Streak is broken, reset it
                                    streak = 1;
                                    Toast.makeText(getContext(), streak, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // No entries exist, start a new streak
                        streak = 1;
                        Toast.makeText(getContext(), "Shuru Karein ? "+streak, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors here
                }
            });
        }
    }

    // Helper method to check if two dates are consecutive
    private boolean isConsecutiveDates(Calendar date1, Calendar date2) {
        date1.add(Calendar.DAY_OF_YEAR, 1);
        return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)
                && date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR);
    }
    private void animateFabIn() {
        if (!isFabMenuOpen) {
            isFabMenuOpen = true;
            fab.setVisibility(View.VISIBLE);
            calendarView.setVisibility(View.VISIBLE);
            fab.setScaleX(0f);
            fab.setScaleY(0f);
            fab.setAlpha(0f);
            calendarView.setScaleX(0f);
            calendarView.setScaleY(0f);
            calendarView.setAlpha(0f);
            fab.animate()
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
            calendarView.animate()
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
            fab.animate()
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
                            fab.setVisibility(View.GONE);
                        }
                    })
                    .start();
            calendarView.animate()
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
                            calendarView.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }
    }

    // Inside your Journal Fragment class
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        // Define the builder for the MaterialDatePicker
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("View Historical Journal");
        builder.setSelection(calendar.getTimeInMillis());

        MaterialDatePicker<Long> materialDatePicker = builder.build();

        // Set up the listener for when a date is selected
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            // Convert the selected date to a string
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date selectedDate = new Date(selection);
            String selectedDateString = dateFormat.format(selectedDate);

            // Retrieve the journal entry for the selected date from your database
            retrieveJournalEntryForDate(selectedDateString);
        });

        // Show the MaterialDatePicker
        materialDatePicker.show(getChildFragmentManager(), "DATE_PICKER_TAG");
    }

    // Modify the getJournalEntryForDate method
    private void getJournalEntryForDate(String selectedDate, JournalEntryCallback callback) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userJournalRef = journalDatabase.child(userId);

            // Create a query to search for the journal entry with the selected date
            Query query = userJournalRef.orderByChild("date");

            // Add a ValueEventListener to listen for the query result
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Iterate through the result to find entries with matching dates
                        for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                            JournalEntry entry = entrySnapshot.getValue(JournalEntry.class);
                            if (entry != null) {
                                String entryDate = entry.getDate(); // Get the timestamp from the entry

                                // Parse the entryDate into a Date object
                                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
                                try {
                                    Date parsedDate = dateFormat.parse(entryDate);
                                    if (parsedDate != null) {
                                        // Format the parsedDate to extract only the date part
                                        String entryDateWithoutTime = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(parsedDate);

                                        // Compare the date part of the entry with the selected date
                                        if (selectedDate.equals(entryDateWithoutTime)) {
                                            // Pass the found entry to the callback
                                            callback.onJournalEntryLoaded(entry);
                                            return; // Exit the loop after finding the entry
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    // If no entry is found for the selected date, pass null to the callback
                    callback.onJournalEntryLoaded(null);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors here
                }
            });
        }
    }

    private void retrieveJournalEntryForDate(String selectedDate) {
        getJournalEntryForDate(selectedDate, new JournalEntryCallback() {
            @Override
            public void onJournalEntryLoaded(JournalEntry entry) {
                // Display the journal entry using a Bottom Sheet Fragment
                showJournalEntryBottomSheet(entry);
            }
        });
    }

    private void showJournalEntryBottomSheet(JournalEntry entry) {
        if (entry != null) {
            String title = entry.getTitle();
            String content = entry.getContent();
            String date = entry.getDate();
            vibrateToEnter();
            JournalEntryBottomSheet bottomSheet = new JournalEntryBottomSheet(title, content, date);
            bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
        } else {
            // Handle the case where no journal entry is found for the selected date
            Toast.makeText(requireContext(), "No entry found for the selected date", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteEntryDialog(JournalEntry entryToDelete) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Delete Journal Entry");
        builder.setMessage("Do you want to delete this journal entry?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Delete the entry from Firebase
            if (currentUser != null) {
                String userId = currentUser.getUid();
                DatabaseReference userJournalRef = journalDatabase.child(userId);
                userJournalRef.child(entryToDelete.getKey()).removeValue();
                Toast.makeText(getContext(), "Entry Deleted", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        builder.setNegativeButton("No Thanks", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.create().show();
    }

    private void showAddEntryDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Add a New Journal Entry");
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_entry, null);

        TextInputEditText titleEditText = dialogView.findViewById(R.id.titleEditText);
        TextInputEditText contentEditText = dialogView.findViewById(R.id.contentEditText);
        contentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Check if the last character is not a newline, and the user pressed Enter
                if (i2 > 0 && charSequence.charAt(i2 - 1) != '\n' && charSequence.charAt(i2 - 1) == '\n') {
                    // Append a newline character
                    contentEditText.append("\n");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not needed
            }
        });
        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = titleEditText.getText().toString().trim();
            String content = contentEditText.getText().toString().trim();
            if (!title.isEmpty() && !content.isEmpty()) {
                // Create a new journal entry with the current date and time
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
                String formattedDate = dateFormat.format(currentDate); // Format the date as a string

                // Generate a unique key for the entry
                String entryKey = journalDatabase.push().getKey();

                JournalEntry entry = new JournalEntry(title, content, formattedDate, entryKey);

                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    DatabaseReference userJournalRef = journalDatabase.child(userId);
                    if (entryKey != null) {
                        userJournalRef.child(entryKey).setValue(entry);
                    }
                }
            } else if (title.isEmpty()) {
                Toast.makeText(getContext(), "Put some title for the day", Toast.LENGTH_SHORT).show();
            } else if (content.isEmpty()) {
                Toast.makeText(getContext(), "Your day couldn't be this boring 💤", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.create().show();
    }

    @Override
    public void onDeleteClicked(JournalEntry entryToDelete) {
        // Handle the delete action here
        vibrate();
        showDeleteEntryDialog(entryToDelete);
    }

    private void vibrate() {
        long[] pattern = {5, 0, 5, 0, 5, 1, 5, 1, 5, 2, 5, 2, 5, 3, 5, 4, 5, 4, 5, 5, 5, 6, 5, 6, 5, 7, 5, 8, 5, 8, 5, 9, 5, 10, 5, 10, 5, 11, 5, 11, 5, 12, 5, 13, 5, 13, 5, 14, 5, 14, 5, 15, 5, 15, 5, 16, 5, 16, 5, 17, 5};
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, -1);
            vibrator.vibrate(vibrationEffect);
        } else {
            // For versions lower than Oreo
            vibrator.vibrate(pattern, -1);
        }
    }

    private void vibrateToEnter() {
        long[] pattern = {11, 8, 0, 7, 10, 7, 0, 7, 16, 8, 0, 9, 16, 10, 10, 10, 10, 9, 0, 9, 11, 8, 15, 7, 10, 6, 18, 6, 13, 6, 0, 8, 14, 9, 14, 12, 0, 14, 11, 17, 15, 19, 16, 22, 10, 23, 12, 25, 10};
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, -1);
            vibrator.vibrate(vibrationEffect);
        } else {
            // For versions lower than Oreo
            vibrator.vibrate(pattern, -1);
        }
    }

    private void editEntryVibrate() {
        long[] pattern = {21, 0, 25, 5, 21, 9, 19, 12, 25, 15, 26, 18, 21, 21, 20, 24, 0};
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, -1);
            vibrator.vibrate(vibrationEffect);
        } else {
            // For versions lower than Oreo
            vibrator.vibrate(pattern, -1);
        }
    }

    private void checkIfEntryExistsForToday() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userJournalRef = journalDatabase.child(userId);

            userJournalRef.orderByChild("date").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        DataSnapshot latestEntrySnapshot = dataSnapshot.getChildren().iterator().next();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

                        // Get the date string from the latest entry
                        String latestDateString = latestEntrySnapshot.child("date").getValue(String.class);
                        String formattedCurrentDateString = dateFormat.format(new Date());

                        try {
                            // Parse the date strings into Date objects
                            Date latestDate = dateFormat.parse(latestDateString);
                            Date formattedCurrentDate = dateFormat.parse(formattedCurrentDateString);

                            if (latestDate != null && formattedCurrentDate != null) {
                                // Format the dates to only include the date part (MMM dd, yyyy)
                                String latestDateFormatted = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(latestDate);
                                String currentDayFormatted = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(formattedCurrentDate);

                                // Check if the latest entry's date is the same as the current date
                                if (latestDateFormatted.equals(currentDayFormatted)) {
                                    // An entry for today already exists, show a message to the user
                                    showMessage("You've already made an entry for today. Please come back later.", journalEntries.isEmpty() ? null : journalEntries.get(0));
                                } else {
                                    // No entry for today, allow the user to add a new entry
                                    showAddEntryDialog();
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // No entries exist, allow the user to add a new entry
                        showAddEntryDialog();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors here
                }
            });
        }
    }

    private void showMessage(String message, JournalEntry existingEntry) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Done for the day!");
        builder.setMessage(message);
        builder.setNegativeButton("Edit Today's Journal", (dialog, which) -> {
            dialog.dismiss();
            editEntryVibrate();
            // Pass the existing entry to the edit entry method
            editEntry(existingEntry);
        });
        builder.setPositiveButton("Okay!", (dialog, which) -> {
            dialog.dismiss();
            vibrate();
        });

        builder.create().show();
    }

    private void editEntry(JournalEntry existingEntry) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Edit Journal Entry");

        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_entry, null);

        TextInputLayout titleLayout = dialogView.findViewById(R.id.titleLayout);
        TextInputLayout contentLayout = dialogView.findViewById(R.id.contentLayout);

        TextInputEditText titleEditText = dialogView.findViewById(R.id.titleEditText);
        TextInputEditText contentEditText = dialogView.findViewById(R.id.contentEditText);

        // Set the existing entry data in the EditText fields
        titleEditText.setText(existingEntry.getTitle());
        contentEditText.setText(existingEntry.getContent());

        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = titleEditText.getText().toString().trim();
            String content = contentEditText.getText().toString().trim();
            if (!title.isEmpty() && !content.isEmpty()) {
                // Update the existing entry with the new data
                existingEntry.setTitle(title);
                existingEntry.setContent(content);

                // Save the updated entry to Firebase
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    DatabaseReference userJournalRef = journalDatabase.child(userId);
                    userJournalRef.child(existingEntry.getKey()).setValue(existingEntry);
                }
            } else if (title.isEmpty()) {
                titleLayout.setError("Title is Required");
            } else if (content.isEmpty()) {
                contentLayout.setError("Content is Required");
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.create().show();
    }

    public interface JournalEntryCallback {
        void onJournalEntryLoaded(JournalEntry entry);
    }
}