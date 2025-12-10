package com.example.wordnest.ui.bookmarks;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordnest.R;
import com.example.wordnest.WordDetailsActivity;
import com.example.wordnest.db.BookmarksDatabaseHelper;

import java.util.List;

public class BookmarksFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private TextView wordCountText;
    private BookmarkAdapter adapter; // ✅ singular name
    private BookmarksDatabaseHelper dbHelper;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_bookmarks, container, false);

        recyclerView = root.findViewById(R.id.bookmarksRecycler);
        emptyState = root.findViewById(R.id.empty_state);
        wordCountText = root.findViewById(R.id.btn_clear_all);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dbHelper = new BookmarksDatabaseHelper(getContext());

        loadBookmarks();
        return root;
    }

    private void loadBookmarks() {
        List<Bookmark> bookmarks = dbHelper.getAllBookmarks();

        if (bookmarks.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        // ✅ Corrected Adapter
        adapter = new BookmarkAdapter(getContext(), new BookmarkAdapter.OnBookmarkActionListener() {
            @Override
            public void onWordClicked(Bookmark bookmark) {
                // Open Word Details
                Intent intent = new Intent(getContext(), WordDetailsActivity.class);
                intent.putExtra("word", bookmark.getWord());
                startActivity(intent);
            }

            @Override
            public void onBookmarkDeleted() {
                loadBookmarks(); // Refresh after deletion
            }
        });

        adapter.setItems(bookmarks);
        recyclerView.setAdapter(adapter);

        updateWordCount(bookmarks.size());
    }

    private void updateWordCount(int count) {
        if (wordCountText != null) {
            if (count == 0) {
                wordCountText.setText("No words");
            } else if (count == 1) {
                wordCountText.setText("1 word");
            } else {
                wordCountText.setText(count + " words");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookmarks(); // Refresh every time you reopen the tab
    }
}
