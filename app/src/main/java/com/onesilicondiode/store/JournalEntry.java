package com.onesilicondiode.store;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JournalEntry implements Comparable<JournalEntry> {
    private String title;
    private String content;
    private String date;
    private String key; // Add a key field

    public JournalEntry() {
        // Default constructor required for Firebase
    }

    public JournalEntry(String title, String content, String formattedDate, String key) {
        this.title = title;
        this.content = content;
        this.date = formattedDate;
        this.key = key;
    }

    // Getter method for the key
    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }
    @Override
    public int compareTo(JournalEntry other) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        try {
            Date thisDate = dateFormat.parse(this.date);
            Date otherDate = dateFormat.parse(other.date);
            // Compare based on date (newest first), without reversing the order
            return thisDate.compareTo(otherDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}