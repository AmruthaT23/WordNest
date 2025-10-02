package com.example.wordnest;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordnest.db.HistoryDatabaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

public class WordDetailsActivity extends AppCompatActivity {

    private TextView textWord, textPhonetic, textPartOfSpeech;
    private LinearLayout layoutDefinitions, layoutExamples, layoutSynonyms, layoutAntonyms;
    private Button buttonBookmark;

    private HistoryDatabaseHelper dbHelper; // database helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_details);

        // Set the activity title
        setTitle("Word Details");

        // Enable back button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize DB helper
        dbHelper = new HistoryDatabaseHelper(this);

        // Bind views
        textWord = findViewById(R.id.text_word);
        textPhonetic = findViewById(R.id.text_phonetic);
        textPartOfSpeech = findViewById(R.id.text_part_of_speech);

        layoutDefinitions = findViewById(R.id.layout_definitions);
        layoutExamples = findViewById(R.id.layout_examples);
        layoutSynonyms = findViewById(R.id.layout_synonyms);
        layoutAntonyms = findViewById(R.id.layout_antonyms);

        buttonBookmark = findViewById(R.id.button_add_bookmark);

        // Get word from intent
        String word = getIntent().getStringExtra("word");
        if (word != null && !word.isEmpty()) {
            textWord.setText(word);

            // Save word to history
            dbHelper.insertWord(word);

            // Fetch details
            fetchWordDetails(word);
        }

        buttonBookmark.setOnClickListener(v ->
                Toast.makeText(this, "Bookmark feature coming soon!", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Closes this activity and returns to the previous one
        return true;
    }

    private void fetchWordDetails(String word) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + word);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    runOnUiThread(() ->
                            Toast.makeText(WordDetailsActivity.this, "Word not found!", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                runOnUiThread(() -> parseAndDisplay(response.toString()));

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(WordDetailsActivity.this, "Error fetching word details", Toast.LENGTH_SHORT).show()
                );
                e.printStackTrace();
            }
        }).start();
    }

    private void parseAndDisplay(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            JSONObject firstObj = jsonArray.getJSONObject(0);

            // Word and Phonetic
            textWord.setText(firstObj.getString("word"));
            textWord.setTextColor(0xFF7650FF);
            textWord.setTypeface(Typeface.SERIF, Typeface.BOLD);

            String phonetic = firstObj.has("phonetic") ? firstObj.getString("phonetic") : "";
            textPhonetic.setText(phonetic);
            textPhonetic.setTextSize(16);
            textPhonetic.setTypeface(Typeface.SERIF, Typeface.ITALIC);
            textPhonetic.setTextColor(0xFF444444);

            JSONArray meanings = firstObj.getJSONArray("meanings");
            if (meanings.length() > 0) {
                String pos = meanings.getJSONObject(0).getString("partOfSpeech");
                textPartOfSpeech.setText(pos);
            }

            // Clear previous content
            layoutDefinitions.removeAllViews();
            layoutExamples.removeAllViews();
            layoutSynonyms.removeAllViews();
            layoutAntonyms.removeAllViews();

            boolean exampleAdded = false;
            int definitionCount = 0;
            int maxDefinitions = 3; // Max 3 definitions

            HashSet<String> synonymsSet = new HashSet<>();
            HashSet<String> antonymsSet = new HashSet<>();

            for (int i = 0; i < meanings.length() && definitionCount < maxDefinitions; i++) {
                JSONObject meaningObj = meanings.getJSONObject(i);
                JSONArray definitions = meaningObj.getJSONArray("definitions");

                for (int j = 0; j < definitions.length() && definitionCount < maxDefinitions; j++) {
                    JSONObject defObj = definitions.getJSONObject(j);

                    // Definition
                    String def = defObj.getString("definition");
                    TextView defText = new TextView(this);
                    defText.setText("• " + def);
                    defText.setTextSize(16);
                    defText.setTextColor(0xFF000000);
                    defText.setTypeface(Typeface.SERIF);
                    defText.setPadding(0, 6, 0, 6);
                    layoutDefinitions.addView(defText);
                    definitionCount++;

                    // Only one example
                    if (!exampleAdded && defObj.has("example")) {
                        String example = defObj.getString("example");
                        if (!example.isEmpty()) {
                            TextView exLabel = new TextView(this);
                            exLabel.setText("Example:");
                            exLabel.setTextSize(16);
                            exLabel.setTextColor(0xFF7650FF);
                            exLabel.setTypeface(Typeface.SERIF, Typeface.BOLD);
                            layoutExamples.addView(exLabel);

                            TextView exText = new TextView(this);
                            exText.setText(example);
                            exText.setTextSize(16);
                            exText.setTextColor(0xFF333333);
                            exText.setTypeface(Typeface.SERIF);
                            exText.setPadding(0, 0, 0, 8);
                            layoutExamples.addView(exText);

                            exampleAdded = true;
                        }
                    }
                }

                // Collect synonyms
                if (meaningObj.has("synonyms")) {
                    JSONArray synArray = meaningObj.getJSONArray("synonyms");
                    for (int k = 0; k < synArray.length() && synonymsSet.size() < 5; k++) {
                        synonymsSet.add(synArray.getString(k));
                    }
                }

                // Collect antonyms
                if (meaningObj.has("antonyms")) {
                    JSONArray antArray = meaningObj.getJSONArray("antonyms");
                    for (int k = 0; k < antArray.length() && antonymsSet.size() < 5; k++) {
                        antonymsSet.add(antArray.getString(k));
                    }
                }
            }

            // Display synonyms
            TextView synLabel = new TextView(this);
            synLabel.setText("Synonyms:");
            synLabel.setTextSize(16);
            synLabel.setTextColor(0xFF7650FF);
            synLabel.setTypeface(Typeface.SERIF, Typeface.BOLD);
            layoutSynonyms.addView(synLabel);

            TextView synText = new TextView(this);
            if (!synonymsSet.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                int count = 0;
                for (String s : synonymsSet) {
                    sb.append(s);
                    count++;
                    if (count < synonymsSet.size()) sb.append(", ");
                }
                synText.setText(sb.toString());
            } else synText.setText("Not available");
            synText.setTextSize(16);
            synText.setTextColor(0xFF000000);
            synText.setTypeface(Typeface.SERIF);
            layoutSynonyms.addView(synText);

            // Display antonyms
            TextView antLabel = new TextView(this);
            antLabel.setText("Antonyms:");
            antLabel.setTextSize(16);
            antLabel.setTextColor(0xFF7650FF);
            antLabel.setTypeface(Typeface.SERIF, Typeface.BOLD);
            layoutAntonyms.addView(antLabel);

            TextView antText = new TextView(this);
            if (!antonymsSet.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                int count = 0;
                for (String s : antonymsSet) {
                    sb.append(s);
                    count++;
                    if (count < antonymsSet.size()) sb.append(", ");
                }
                antText.setText(sb.toString());
            } else antText.setText("Not available");
            antText.setTextSize(16);
            antText.setTextColor(0xFF000000);
            antText.setTypeface(Typeface.SERIF);
            layoutAntonyms.addView(antText);

        } catch (Exception e) {
            Toast.makeText(this, "Word not found!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
