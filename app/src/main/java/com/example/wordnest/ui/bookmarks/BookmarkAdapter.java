package com.example.wordnest.ui.bookmarks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordnest.R;
import com.example.wordnest.db.BookmarksDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

    private final List<Bookmark> items = new ArrayList<>();
    private final Context context;
    private final BookmarksDatabaseHelper dbHelper;
    private OnBookmarkActionListener listener;

    public interface OnBookmarkActionListener {
        void onWordClicked(Bookmark bookmark);
        void onBookmarkDeleted();
    }

    public BookmarkAdapter(Context context, OnBookmarkActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.dbHelper = new BookmarksDatabaseHelper(context);
    }

    public void setItems(List<Bookmark> bookmarks) {
        items.clear();
        if (bookmarks != null) items.addAll(bookmarks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bookmark, parent, false);
        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        Bookmark bookmark = items.get(position);

        holder.tvWord.setText(bookmark.getWord());
        holder.tvSavedOn.setText("Saved on " + bookmark.getTimestamp());

        // Click listener for viewing word details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onWordClicked(bookmark);
        });

        // Delete bookmark
        holder.imgDelete.setOnClickListener(v -> {
            boolean deleted = dbHelper.removeBookmarkById(bookmark.getId());
            if (deleted) {
                Toast.makeText(context, "Deleted " + bookmark.getWord(), Toast.LENGTH_SHORT).show();
                items.remove(position);
                notifyItemRemoved(position);
                if (listener != null) listener.onBookmarkDeleted();
            } else {
                Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class BookmarkViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvSavedOn;
        ImageView imgBookmarkIcon, imgDelete;

        public BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tv_word);
            tvSavedOn = itemView.findViewById(R.id.tv_saved_on);
            imgBookmarkIcon = itemView.findViewById(R.id.img_bookmark_icon);
            imgDelete = itemView.findViewById(R.id.img_delete);
        }
    }
}
