package com.example.wordnest.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wordnest.R;
import com.example.wordnest.WordDetailsActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Random;

public class HomeFragment extends Fragment {

    private EditText editTextSearch;
    private Button buttonSearch, buttonViewDetails;
    private TextView textWordOfDay, textWordType, textWordMeanings;
    private String currentWordOfDay = "";

    private final String[] wordList = {
            "serendipity", "tranquil", "ephemeral", "melancholy", "euphoria",
            "resilience", "benevolent", "solitude", "eloquent", "nostalgia",
            "harmony", "luminous", "whimsical", "inspire", "zenith"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Bind views
        editTextSearch = root.findViewById(R.id.editText_search);
        buttonSearch = root.findViewById(R.id.button_search);
        textWordOfDay = root.findViewById(R.id.text_word_of_day);
        textWordType = root.findViewById(R.id.text_word_type);
        textWordMeanings = root.findViewById(R.id.text_word_meanings);
        buttonViewDetails = root.findViewById(R.id.button_view_details);

        // Handle Search button click
        buttonSearch.setOnClickListener(v -> {
            String searchWord = editTextSearch.getText().toString().trim();
            if (searchWord.isEmpty()) {
                Toast.makeText(getContext(), "Enter a word to search", Toast.LENGTH_SHORT).show();
            } else {
                openWordDetails(searchWord);
            }
        });

        // Handle View Details click for Word of the Day
        buttonViewDetails.setOnClickListener(v -> {
            if (!currentWordOfDay.isEmpty()) {
                openWordDetails(currentWordOfDay);
            } else {
                Toast.makeText(getContext(), "Word not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });

        // Load Word of the Day
        loadWordOfTheDay();

        return root;
    }

    private void loadWordOfTheDay() {
        SharedPreferences prefs = requireContext().getSharedPreferences("WordNestPrefs", Context.MODE_PRIVATE);
        String savedWord = prefs.getString("wordOfDay", null);
        long savedDate = prefs.getLong("wordOfDayDate", 0);

        long today = getTodayDateCode();

        if (savedWord != null && savedDate == today) {
            // Already saved for today
            currentWordOfDay = savedWord;
            fetchWordDetails(savedWord);
        } else {
            // Generate a new random word
            String newWord = getRandomWord();
            currentWordOfDay = newWord;
            prefs.edit().putString("wordOfDay", newWord).putLong("wordOfDayDate", today).apply();
            fetchWordDetails(newWord);
        }
    }

    private long getTodayDateCode() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) * 10000L +
                (calendar.get(Calendar.MONTH) + 1) * 100 +
                calendar.get(Calendar.DAY_OF_MONTH);
    }

    private String getRandomWord() {
        Random random = new Random();
        return wordList[random.nextInt(wordList.length)];
    }

    private void fetchWordDetails(String word) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + word);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    showError("Unable to fetch word details");
                    return;
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();

                parseAndDisplay(response.toString());

            } catch (Exception e) {
                e.printStackTrace();
                showError("Error fetching details");
            }
        }).start();
    }

    private void parseAndDisplay(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            JSONObject firstObj = jsonArray.getJSONObject(0);
            String word = firstObj.getString("word");
            JSONArray meanings = firstObj.getJSONArray("meanings");

            StringBuilder meaningsText = new StringBuilder();
            String pos = "";

            if (meanings.length() > 0) {
                JSONObject firstMeaning = meanings.getJSONObject(0);
                pos = firstMeaning.getString("partOfSpeech");

                JSONArray definitions = firstMeaning.getJSONArray("definitions");
                for (int i = 0; i < Math.min(2, definitions.length()); i++) {
                    String def = definitions.getJSONObject(i).getString("definition");
                    meaningsText.append("• ").append(def).append("\n");
                }
            }

            String finalPos = pos;
            new Handler(Looper.getMainLooper()).post(() -> {
                textWordOfDay.setText(word);
                textWordType.setText(finalPos.isEmpty() ? "—" : finalPos);
                textWordMeanings.setText(meaningsText.toString().trim());
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error parsing details");
        }
    }

    private void showError(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }

    private void openWordDetails(String word) {
        Intent intent = new Intent(getContext(), WordDetailsActivity.class);
        intent.putExtra("word", word);
        startActivity(intent);
    }
}
