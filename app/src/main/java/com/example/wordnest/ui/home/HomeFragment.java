package com.example.wordnest.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wordnest.WordDetailsActivity;
import com.example.wordnest.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Search button click
        binding.buttonSearch.setOnClickListener(v -> {
            String word = binding.editTextSearch.getText().toString().trim();
            if (!word.isEmpty()) {
                Intent intent = new Intent(getActivity(), WordDetailsActivity.class);
                intent.putExtra("word", word); // pass the word to WordDetailsActivity
                startActivity(intent);
            }
        });

        // Word of the Day "View Details" button click
        binding.buttonViewDetails.setOnClickListener(v -> {
            String wordOfDay = binding.textWordOfDay.getText().toString().trim();
            if (!wordOfDay.isEmpty()) {
                Intent intent = new Intent(getActivity(), WordDetailsActivity.class);
                intent.putExtra("word", wordOfDay);
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            binding.editTextSearch.setText(""); // Reset the search bar when returning
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
