package com.onesilicondiode.store;

import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder> {
    private List<JournalEntry> journalEntries;
    private OnItemClickListener onItemClickListener;

    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public JournalAdapter(Context context, List<JournalEntry> journalEntries, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.journalEntries = journalEntries;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journal_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JournalEntry entry = journalEntries.get(position);
        if (entry != null) {
            holder.titleTextView.setText(entry.getTitle());
            holder.contentTextView.setText(entry.getContent());

            // Parse the stored date string into a Date object
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            try {
                Date date = dateFormat.parse(entry.getDate());

                // Format the date for display
                SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
                String formattedDate = displayDateFormat.format(date);

                holder.dateTextView.setText(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onDeleteClicked(entry);
                    }
                    return true;
                }
            });
        }
    }
    public interface OnItemClickListener {
        void onDeleteClicked(JournalEntry entry);
    }

    @Override
    public int getItemCount() {
        return journalEntries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentTextView;
        TextView dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}