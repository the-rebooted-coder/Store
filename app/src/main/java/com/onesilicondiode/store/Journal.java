package com.onesilicondiode.store;

import static android.content.Context.VIBRATOR_SERVICE;

import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Journal extends Fragment implements JournalAdapter.OnItemClickListener {
    private FloatingActionButton fab;
    private RecyclerView journalRecyclerView;
    private FirebaseUser currentUser;
    private DatabaseReference journalDatabase;
    private Vibrator vibrator;
    private JournalAdapter journalAdapter;
    private List<JournalEntry> journalEntries = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal, container, false);

        fab = view.findViewById(R.id.fab);
        journalRecyclerView = view.findViewById(R.id.journalRecyclerView);

        // Initialize Firebase Database reference
        journalDatabase = FirebaseDatabase.getInstance().getReference().child("JournalEntries");

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
            showAddEntryDialog();
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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors here
                }
            });
        }

        return view;
    }

    private void showDeleteEntryDialog(JournalEntry entryToDelete) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Delete Journal Entry");
        builder.setMessage("Are you sure you want to delete this journal entry?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Delete the entry from Firebase
            if (currentUser != null) {
                String userId = currentUser.getUid();
                DatabaseReference userJournalRef = journalDatabase.child(userId);
                userJournalRef.child(entryToDelete.getKey()).removeValue(); // Assuming each entry has a unique key
            }
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.create().show();
    }

    private void showAddEntryDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Add a New Journal Entry");

        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_entry, null);

        TextInputLayout titleLayout = dialogView.findViewById(R.id.titleLayout);
        TextInputLayout contentLayout = dialogView.findViewById(R.id.contentLayout);

        TextInputEditText titleEditText = dialogView.findViewById(R.id.titleEditText);
        TextInputEditText contentEditText = dialogView.findViewById(R.id.contentEditText);

        builder.setView(dialogView);

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

                JournalEntry entry = new JournalEntry(title, content, formattedDate, entryKey); // Pass the key

                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    DatabaseReference userJournalRef = journalDatabase.child(userId);
                    if (entryKey != null) {
                        userJournalRef.child(entryKey).setValue(entry);
                    }
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

    @Override
    public void onDeleteClicked(JournalEntry entryToDelete) {
        // Handle the delete action here
        vibrate();
        showDeleteEntryDialog(entryToDelete);
    }

    private void vibrate() {
        long[] pattern = {5, 0, 5, 0, 5, 1, 5, 1, 5, 2, 5, 2, 5, 3, 5, 4, 5, 4, 5, 5, 5, 6, 5, 6, 5, 7, 5, 8, 5, 8, 5, 9, 5, 10, 5, 10, 5, 11, 5, 11, 5, 12, 5, 13, 5, 13, 5, 14, 5, 14, 5, 15, 5, 15, 5, 16, 5, 16, 5, 17, 5, 17, 5, 17, 5, 18, 5, 18, 5, 19, 5, 19, 5, 19, 5, 20, 5, 20, 5, 20, 5, 21, 5, 21, 5, 21, 5, 22, 5, 22, 5, 22, 5, 22, 5, 23, 5, 23, 5, 23, 5, 23, 5, 23, 5, 24, 5, 24, 5, 24, 5, 24, 5, 24, 5, 24, 5, 24, 5, 24, 5, 25, 5, 25, 5, 25, 5, 25, 5, 25, 5, 25, 5, 25, 5, 25, 5, 25, 5, 25, 5};
        VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, -1);
        vibrator.vibrate(vibrationEffect);
    }
    private void vibrateToEnter() {
        long[] pattern = {11,8,0,7,10,7,0,7,16,8,0,9,16,10,10,10,10,9,0,9,11,8,15,7,10,6,18,6,13,6,0,8,14,9,14,12,0,14,11,17,15,19,16,22,10,23,12,25,10};
        VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, -1);
        vibrator.vibrate(vibrationEffect);
    }
}