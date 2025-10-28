package com.example.wordnest.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.wordnest.ui.bookmarks.Bookmark;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookmarksDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bookmarks.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "bookmarks";
    public static final String COL_ID = "id";
    public static final String COL_WORD = "word";
    public static final String COL_TIMESTAMP = "timestamp";

    public BookmarksDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COL_WORD + " TEXT NOT NULL UNIQUE, "
                        + COL_TIMESTAMP + " TEXT"
                        + ");";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addBookmark(String word) {
        if (word == null || word.trim().isEmpty()) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WORD, word.trim());
        String timestamp = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        values.put(COL_TIMESTAMP, timestamp);

        long id = -1;
        try {
            id = db.insertOrThrow(TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return id != -1;
    }

    public List<Bookmark> getAllBookmarks() {
        List<Bookmark> bookmarks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " DESC";
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                    String word = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORD));
                    String ts = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP));
                    bookmarks.add(new Bookmark(id, word, ts));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return bookmarks;
    }

    public void clearAllBookmarks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    public boolean isBookmarked(String word) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.query(TABLE_NAME, new String[]{COL_ID}, COL_WORD + "=?", new String[]{word}, null, null, null);
            exists = (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return exists;
    }

    // --- REMOVE METHOD ADDED ---
    public boolean removeBookmarkById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = 0;
        try {
            rows = db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return rows > 0;
    }
}
