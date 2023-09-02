package com.onesilicondiode.store;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

public class Journal extends Fragment {
    private FloatingActionButton fab;
    private RecyclerView journalRecyclerView;
    private JournalAdapter journalAdapter;
    private DatabaseReference journalDatabase;
    private FirebaseUser currentUser;
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

        // Set up RecyclerView and Adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        journalRecyclerView.setLayoutManager(layoutManager);
        journalAdapter = new JournalAdapter(getContext(), journalEntries);
        journalRecyclerView.setAdapter(journalAdapter);

        // Set an onClickListener for the FAB to add a new journal entry
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddEntryDialog();
            }
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

    // Show an AlertDialog to add a new journal entry
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

                JournalEntry entry = new JournalEntry(title, content, formattedDate); // Store the formatted date as a string

                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    DatabaseReference userJournalRef = journalDatabase.child(userId);
                    String entryKey = userJournalRef.push().getKey();
                    if (entryKey != null) {
                        userJournalRef.child(entryKey).setValue(entry);
                    }
                }
            }
            else if (title.isEmpty()){
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
}