package com.example.wordnest.ui.bookmarks;

public class Bookmark {
    private int id;
    private String word;
    private String timestamp;

    public Bookmark(int id, String word, String timestamp) {
        this.id = id;
        this.word = word;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getWord() {
        return word;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
