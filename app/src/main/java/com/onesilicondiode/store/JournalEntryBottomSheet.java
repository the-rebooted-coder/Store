package com.onesilicondiode.store;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class JournalEntryBottomSheet extends BottomSheetDialogFragment {

    private String title;
    private String content;
    private String date;

    public JournalEntryBottomSheet(String title, String content, String date) {
        this.title = title;
        this.content = content;
        this.date = date;
    }

    public JournalEntryBottomSheet() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journal_entry_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView titleTextView = view.findViewById(R.id.titleTextView);
        TextView contentTextView = view.findViewById(R.id.contentTextView);
        TextView dateTextView = view.findViewById(R.id.dateTextView);

        titleTextView.setText(title);
        contentTextView.setText(content);
        dateTextView.setText(date);
    }
}