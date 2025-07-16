package com.son.bookhaven.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.son.bookhaven.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface FilterApplyListener {
        void onFilterApplied(Double minPrice, Double maxPrice, Integer authorId,
                             Integer categoryId, Integer publisherId, String language);
    }

    private FilterApplyListener filterApplyListener;

    private ChipGroup chipGroupCategories;
    private ChipGroup chipGroupLanguages;
    private TextInputEditText textInputEditTextMinPrice;
    private TextInputEditText textInputEditTextMaxPrice;
    private AutoCompleteTextView dropdownAuthors;
    private AutoCompleteTextView dropdownPublishers;
    private TextInputLayout textInputLayoutMinPrice;
    private TextInputLayout textInputLayoutMaxPrice;
    private MaterialButton buttonClearFilters;
    private MaterialButton buttonApplyFilters;

    // Current filter values
    private Integer selectedCategoryId;
    private Integer selectedAuthorId;
    private Integer selectedPublisherId;
    private String selectedLanguage;
    private Double currentMinPrice;
    private Double currentMaxPrice;

    // Maps to store IDs for dropdown selections
    private Map<String, Integer> authorMap = new HashMap<>();
    private Map<String, Integer> publisherMap = new HashMap<>();
    private Map<String, Integer> categoryMap = new HashMap<>();

    public void setFilterApplyListener(FilterApplyListener listener) {
        this.filterApplyListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_options, container, false);

        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        chipGroupLanguages = view.findViewById(R.id.chipGroupLanguages);
        textInputEditTextMinPrice = view.findViewById(R.id.textInputEditTextMinPrice);
        textInputEditTextMaxPrice = view.findViewById(R.id.textInputEditTextMaxPrice);
        textInputLayoutMinPrice = view.findViewById(R.id.textInputLayoutMinPrice);
        textInputLayoutMaxPrice = view.findViewById(R.id.textInputLayoutMaxPrice);
        dropdownAuthors = view.findViewById(R.id.dropdownAuthors);
        dropdownPublishers = view.findViewById(R.id.dropdownPublishers);
        buttonClearFilters = view.findViewById(R.id.buttonClearFilters);
        buttonApplyFilters = view.findViewById(R.id.buttonApplyFilters);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Populate filters
        populateCategories();
        populateLanguages();
        populateAuthors();
        populatePublishers();

        // Set click listeners
        buttonClearFilters.setOnClickListener(v -> clearFilters());
        buttonApplyFilters.setOnClickListener(v -> applyFilters());
    }

    private void populateCategories() {
        if (chipGroupCategories == null) return;

        // Clear existing chips
        chipGroupCategories.removeAllViews();

        // Sample categories - in a real app, these would come from an API
        String[] categories = {"Fiction", "Non-Fiction", "Biography", "Science", "History", "Technology", "Children"};
        categoryMap.clear();

        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            int categoryId = i + 1; // Assign IDs starting from 1
            categoryMap.put(category, categoryId);

            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setClickable(true);

            // If this is the currently selected category, check it
            if (categoryId == selectedCategoryId) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategoryId = categoryMap.get(category);
                } else if (selectedCategoryId != null && selectedCategoryId.equals(categoryMap.get(category))) {
                    selectedCategoryId = null;
                }
            });

            chipGroupCategories.addView(chip);
        }
    }

    private void populateLanguages() {
        if (chipGroupLanguages == null) return;

        // Clear existing chips
        chipGroupLanguages.removeAllViews();

        // Sample languages - in a real app, these would come from an API or enum
        String[] languages = {"English", "Spanish", "French", "German", "Chinese", "Japanese"};

        for (String language : languages) {
            Chip chip = new Chip(requireContext());
            chip.setText(language);
            chip.setCheckable(true);
            chip.setClickable(true);

            // If this is the currently selected language, check it
            if (language.equals(selectedLanguage)) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedLanguage = language;
                } else if (language.equals(selectedLanguage)) {
                    selectedLanguage = null;
                }
            });

            chipGroupLanguages.addView(chip);
        }
    }

    private void populateAuthors() {
        if (dropdownAuthors == null) return;

        // Sample authors - in a real app, these would come from an API
        String[] authors = {"All Authors", "Sarah Johnson", "Alex Chen", "Maria Rodriguez", "David Kim",
                "Michael Blake", "Lisa Wong", "Emma Thompson", "Roberto Martinez"};
        authorMap.clear();

        for (int i = 0; i < authors.length; i++) {
            // Skip "All Authors" for ID mapping (index 0)
            if (i > 0) {
                authorMap.put(authors[i], i);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,
                authors
        );

        dropdownAuthors.setAdapter(adapter);
        dropdownAuthors.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) { // "All Authors"
                selectedAuthorId = null;
            } else {
                selectedAuthorId = position;
            }
        });

        // Set initial selection if available
        if (selectedAuthorId != null) {
            for (Map.Entry<String, Integer> entry : authorMap.entrySet()) {
                if (entry.getValue().equals(selectedAuthorId)) {
                    dropdownAuthors.setText(entry.getKey(), false);
                    break;
                }
            }
        } else {
            dropdownAuthors.setText(authors[0], false);
        }
    }

    private void populatePublishers() {
        if (dropdownPublishers == null) return;

        // Sample publishers - in a real app, these would come from an API
        String[] publishers = {"All Publishers", "Random House", "Penguin Books", "HarperCollins",
                "Simon & Schuster", "Macmillan", "Hachette Book Group"};
        publisherMap.clear();

        for (int i = 0; i < publishers.length; i++) {
            // Skip "All Publishers" for ID mapping (index 0)
            if (i > 0) {
                publisherMap.put(publishers[i], i + 100); // Use offset for publisher IDs
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,
                publishers
        );

        dropdownPublishers.setAdapter(adapter);
        dropdownPublishers.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) { // "All Publishers"
                selectedPublisherId = null;
            } else {
                selectedPublisherId = publisherMap.get(publishers[position]);
            }
        });

        // Set initial selection if available
        if (selectedPublisherId != null) {
            for (Map.Entry<String, Integer> entry : publisherMap.entrySet()) {
                if (entry.getValue().equals(selectedPublisherId)) {
                    dropdownPublishers.setText(entry.getKey(), false);
                    break;
                }
            }
        } else {
            dropdownPublishers.setText(publishers[0], false);
        }
    }

    private void clearFilters() {
        // Clear all filter selections
        selectedCategoryId = null;
        selectedAuthorId = null;
        selectedPublisherId = null;
        selectedLanguage = null;
        currentMinPrice = null;
        currentMaxPrice = null;

        // Clear UI elements
        textInputEditTextMinPrice.setText("");
        textInputEditTextMaxPrice.setText("");

        // Reset category chips
        for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategories.getChildAt(i);
            chip.setChecked(false);
        }

        // Reset language chips
        for (int i = 0; i < chipGroupLanguages.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupLanguages.getChildAt(i);
            chip.setChecked(false);
        }

        // Reset dropdowns
        dropdownAuthors.setText("All Authors", false);
        dropdownPublishers.setText("All Publishers", false);
    }

    private void applyFilters() {
        // Get price range from input fields
        String minPriceStr = textInputEditTextMinPrice.getText().toString();
        String maxPriceStr = textInputEditTextMaxPrice.getText().toString();

        if (!minPriceStr.isEmpty()) {
            try {
                currentMinPrice = Double.parseDouble(minPriceStr);
            } catch (NumberFormatException e) {
                textInputLayoutMinPrice.setError("Invalid number");
                return;
            }
        } else {
            currentMinPrice = null;
            textInputLayoutMinPrice.setError(null);
        }

        if (!maxPriceStr.isEmpty()) {
            try {
                currentMaxPrice = Double.parseDouble(maxPriceStr);
            } catch (NumberFormatException e) {
                textInputLayoutMaxPrice.setError("Invalid number");
                return;
            }
        } else {
            currentMaxPrice = null;
            textInputLayoutMaxPrice.setError(null);
        }

        // Validate that min price is less than max price if both are provided
        if (currentMinPrice != null && currentMaxPrice != null) {
            if (currentMinPrice > currentMaxPrice) {
                textInputLayoutMinPrice.setError("Min price must be less than max price");
                return;
            } else {
                textInputLayoutMinPrice.setError(null);
                textInputLayoutMaxPrice.setError(null);
            }
        }

        // Call the listener with filter values
        if (filterApplyListener != null) {
            filterApplyListener.onFilterApplied(
                    currentMinPrice,
                    currentMaxPrice,
                    selectedAuthorId,
                    selectedCategoryId,
                    selectedPublisherId,
                    selectedLanguage
            );
        }

        // Dismiss the dialog
        dismiss();
    }
}
