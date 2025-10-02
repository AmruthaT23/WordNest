package com.example.wordnest.ui.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.wordnest.R;
import com.example.wordnest.ui.history.HistoryItem;

import java.util.List;

public class HistoryAdapter extends BaseAdapter {
    private Context context;
    private List<HistoryItem> historyList;

    public HistoryAdapter(Context context, List<HistoryItem> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @Override
    public int getCount() {
        return historyList.size();
    }

    @Override
    public Object getItem(int position) {
        return historyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return historyList.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false);
        }

        HistoryItem item = historyList.get(position);

        TextView textWord = convertView.findViewById(R.id.text_word);
        TextView textTimestamp = convertView.findViewById(R.id.text_timestamp);

        textWord.setText(item.word);
        textTimestamp.setText(item.timestamp);

        return convertView;
    }
}
