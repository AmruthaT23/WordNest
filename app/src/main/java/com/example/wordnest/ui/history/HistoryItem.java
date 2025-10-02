package com.example.wordnest.ui.history;

public class HistoryItem {
    public int id;
    public String word;
    public String timestamp;

    public HistoryItem(int id, String word, String timestamp) {
        this.id = id;
        this.word = word;
        this.timestamp = timestamp;
    }
}
