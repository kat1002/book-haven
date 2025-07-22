package com.son.bookhaven.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.son.bookhaven.R;
import com.son.bookhaven.utils.ApiClient;
import com.son.bookhaven.services.AuthorApiService;
import com.son.bookhaven.services.CategoryApiService;
import com.son.bookhaven.services.PublisherApiService;
import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.response.AuthorResponse;
import com.son.bookhaven.data.dto.response.CategoryResponse;
import com.son.bookhaven.data.dto.response.PublisherResponse;
import com.son.bookhaven.data.model.LanguageCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilterBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private static final String TAG = "FilterBottomSheet";

    public interface FilterApplyListener {
        void onFilterApplied(Double minPrice, Double maxPrice, Integer authorId,
                             Integer categoryId, Integer publisherId, String language);
    }

    @Setter
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
    private ProgressBar progressBar;

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

    private CategoryApiService categoryApiService;
    private AuthorApiService authorApiService;
    private PublisherApiService publisherApiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_options, container, false);

        categoryApiService = ApiClient.getClient().create(CategoryApiService.class);
        authorApiService = ApiClient.getClient().create(AuthorApiService.class);
        publisherApiService = ApiClient.getClient().create(PublisherApiService.class);

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
        showLoading(true);

        // Fetch data and populate filters
        fetchCategories();
        populateLanguages();
        fetchAuthors();
        fetchPublishers();

        // Set click listeners
        buttonClearFilters.setOnClickListener(v -> clearFilters());
        buttonApplyFilters.setOnClickListener(v -> applyFilters());
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void fetchCategories() {
        if (chipGroupCategories == null) return;

        categoryApiService.getAllCategories().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CategoryResponse>>> call, Response<ApiResponse<List<CategoryResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<CategoryResponse> categories = response.body().getData();
                    populateCategoriesChips(categories);
                } else {
                    Log.e(TAG, "Failed to fetch categories: " + response.message());
                    // Fallback to sample data
                    populateSampleCategories();
                }
                checkAllDataLoaded();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CategoryResponse>>> call, Throwable t) {
                Log.e(TAG, "Error fetching categories", t);
                // Fallback to sample data
                populateSampleCategories();
                checkAllDataLoaded();
            }
        });
    }

    private void populateCategoriesChips(List<CategoryResponse> categories) {
        if (chipGroupCategories == null || !isAdded()) return;

        // Clear existing chips
        chipGroupCategories.removeAllViews();
        categoryMap.clear();

        for (CategoryResponse category : categories) {
            categoryMap.put(category.getCategoryName(), category.getCategoryId());

            Chip chip = new Chip(requireContext());
            chip.setText(category.getCategoryName());
            chip.setCheckable(true);
            chip.setClickable(true);

            // If this is the currently selected category, check it
            if (selectedCategoryId != null && selectedCategoryId.equals(category.getCategoryId())) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategoryId = category.getCategoryId();
                } else if (selectedCategoryId != null && selectedCategoryId.equals(category.getCategoryId())) {
                    selectedCategoryId = null;
                }
            });

            chipGroupCategories.addView(chip);
        }
    }

    private void populateSampleCategories() {
        if (chipGroupCategories == null || !isAdded()) return;

        // Clear existing chips
        chipGroupCategories.removeAllViews();
        categoryMap.clear();

        String[] categories = {"Fiction", "Non-Fiction", "Biography", "Science", "History", "Technology", "Children"};

        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            int categoryId = i + 1; // Assign IDs starting from 1
            categoryMap.put(category, categoryId);

            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setClickable(true);

            // Add null check here to prevent NullPointerException
            if (selectedCategoryId != null && categoryId == selectedCategoryId) {
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

        // Get language values from the LanguageCode enum
        LanguageCode[] languageCodes = LanguageCode.values();

        for (LanguageCode languageCode : languageCodes) {
            Chip chip = new Chip(requireContext());
            chip.setText(languageCode.name());
            chip.setCheckable(true);
            chip.setClickable(true);

            // If this is the currently selected language, check it
            if (languageCode.name().equals(selectedLanguage)) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedLanguage = languageCode.name();
                } else if (languageCode.name().equals(selectedLanguage)) {
                    selectedLanguage = null;
                }
            });

            chipGroupLanguages.addView(chip);
        }

        checkAllDataLoaded();
    }

    private void fetchAuthors() {
        authorApiService.getAllAuthors().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AuthorResponse>>> call, Response<ApiResponse<List<AuthorResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<AuthorResponse> authors = response.body().getData();
                    populateAuthorsDropdown(authors);
                } else {
                    Log.e(TAG, "Failed to fetch authors: " + response.message());
                    // Fallback to sample data
                    populateSampleAuthors();
                }
                checkAllDataLoaded();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AuthorResponse>>> call, Throwable t) {
                Log.e(TAG, "Error fetching authors", t);
                // Fallback to sample data
                populateSampleAuthors();
                checkAllDataLoaded();
            }
        });
    }

    private void populateAuthorsDropdown(List<AuthorResponse> authors) {
        if (dropdownAuthors == null || !isAdded()) return;

        List<String> authorNames = new ArrayList<>();
        authorNames.add("All Authors");
        authorMap.clear();

        for (AuthorResponse author : authors) {
            authorNames.add(author.getAuthorName());
            authorMap.put(author.getAuthorName(), author.getAuthorId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,
                authorNames
        );

        dropdownAuthors.setAdapter(adapter);
        dropdownAuthors.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) { // "All Authors"
                selectedAuthorId = null;
            } else {
                selectedAuthorId = authorMap.get(authorNames.get(position));
            }
        });

        // Set initial selection
        if (selectedAuthorId != null) {
            for (Map.Entry<String, Integer> entry : authorMap.entrySet()) {
                if (entry.getValue().equals(selectedAuthorId)) {
                    dropdownAuthors.setText(entry.getKey(), false);
                    break;
                }
            }
        } else {
            dropdownAuthors.setText(authorNames.get(0), false);
        }
    }

    private void populateSampleAuthors() {
        if (dropdownAuthors == null || !isAdded()) return;

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

    private void fetchPublishers() {
        publisherApiService.getAllPublishers().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PublisherResponse>>> call, Response<ApiResponse<List<PublisherResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<PublisherResponse> publishers = response.body().getData();
                    populatePublishersDropdown(publishers);
                } else {
                    Log.e(TAG, "Failed to fetch publishers: " + response.message());
                    // Fallback to sample data
                    populateSamplePublishers();
                }
                checkAllDataLoaded();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PublisherResponse>>> call, Throwable t) {
                Log.e(TAG, "Error fetching publishers", t);
                // Fallback to sample data
                populateSamplePublishers();
                checkAllDataLoaded();
            }
        });
    }

    private void populatePublishersDropdown(List<PublisherResponse> publishers) {
        if (dropdownPublishers == null || !isAdded()) return;

        List<String> publisherNames = new ArrayList<>();
        publisherNames.add("All Publishers");
        publisherMap.clear();

        for (PublisherResponse publisher : publishers) {
            publisherNames.add(publisher.getPublisherName());
            publisherMap.put(publisher.getPublisherName(), publisher.getPublisherId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,
                publisherNames
        );

        dropdownPublishers.setAdapter(adapter);
        dropdownPublishers.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) { // "All Publishers"
                selectedPublisherId = null;
            } else {
                selectedPublisherId = publisherMap.get(publisherNames.get(position));
            }
        });

        // Set initial selection
        if (selectedPublisherId != null) {
            for (Map.Entry<String, Integer> entry : publisherMap.entrySet()) {
                if (entry.getValue().equals(selectedPublisherId)) {
                    dropdownPublishers.setText(entry.getKey(), false);
                    break;
                }
            }
        } else {
            dropdownPublishers.setText(publisherNames.get(0), false);
        }
    }

    private void populateSamplePublishers() {
        if (dropdownPublishers == null || !isAdded()) return;

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

    private void checkAllDataLoaded() {
        // Simple counter to track API calls completion
        // In a more complex app, you might use a more sophisticated approach
        if (chipGroupCategories != null && chipGroupCategories.getChildCount() > 0 &&
                chipGroupLanguages != null && chipGroupLanguages.getChildCount() > 0 &&
                dropdownAuthors != null && dropdownAuthors.getAdapter() != null &&
                dropdownPublishers != null && dropdownPublishers.getAdapter() != null) {

            showLoading(false);
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
