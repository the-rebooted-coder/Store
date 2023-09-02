package com.onesilicondiode.store;

public class JournalEntry {
    private String title;
    private String content;
    private String date; // Store the date as a String

    public JournalEntry() {
        // Default constructor required for Firebase
    }

    public JournalEntry(String title, String content, String date) {
        this.title = title;
        this.content = content;
        this.date = date;
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
}