package com.example.wordnest.ui.history;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.wordnest.R;
import com.example.wordnest.WordDetailsActivity;
import com.example.wordnest.db.HistoryDatabaseHelper;
import com.example.wordnest.ui.history.HistoryItem;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private ListView listView;
    private ImageButton clearButton;
    private HistoryDatabaseHelper dbHelper;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        listView = root.findViewById(R.id.list_history);
        clearButton = root.findViewById(R.id.button_clear_history);
        dbHelper = new HistoryDatabaseHelper(getContext());

        historyList = new ArrayList<>(dbHelper.getAllWordsWithTime());
        adapter = new HistoryAdapter(getContext(), historyList);
        listView.setAdapter(adapter);

        // On item click → go to WordDetailsActivity
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String word = historyList.get(position).word;
            Intent intent = new Intent(getActivity(), WordDetailsActivity.class);
            intent.putExtra("word", word);
            startActivity(intent);
        });

        // Clear history
        clearButton.setOnClickListener(v -> {
            dbHelper.clearHistory();
            historyList.clear();
            adapter.notifyDataSetChanged();
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        historyList.clear();
        historyList.addAll(dbHelper.getAllWordsWithTime());
        adapter.notifyDataSetChanged();
    }
}
