package com.onesilicondiode.store;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder> {
    private List<JournalEntry> journalEntries;
    private OnItemClickListener onItemClickListener;
    private Context context;
    private Vibrator vibrator;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public JournalAdapter(Context context, List<JournalEntry> journalEntries, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.journalEntries = journalEntries;
        this.onItemClickListener = onItemClickListener;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE); // Initialize vibrator here
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
            holder.itemView.setOnLongClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onDeleteClicked(entry);
                }
                return true;
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBottomSheet(entry.getTitle(), entry.getContent(), entry.getDate());
                }
            });
        }
    }

    private void showBottomSheet(String title, String content, String date) {
        bottomSheetVibrate();
        JournalEntryBottomSheet bottomSheet = new JournalEntryBottomSheet(title, content, date);
        bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), bottomSheet.getTag());
    }

    @Override
    public int getItemCount() {
        return journalEntries.size();
    }

    private void bottomSheetVibrate() {
        long[] pattern = {21, 0, 25, 5, 21, 9, 19, 12, 25, 15, 26, 18, 21, 21, 20, 24, 0};
        VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, -1);
        vibrator.vibrate(vibrationEffect);
    }

    public interface OnItemClickListener {
        void onDeleteClicked(JournalEntry entry);
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