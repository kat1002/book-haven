package com.son.bookhaven.ui.fragments; // Adjust package as necessary

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
import java.util.List;

public class FilterBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface FilterApplyListener {
        void onFilterApplied(String category, BigDecimal minPrice, BigDecimal maxPrice);
    }

    private FilterApplyListener filterApplyListener;

    private ChipGroup chipGroupCategories;
    private TextInputEditText textInputEditTextMinPrice;
    private TextInputEditText textInputEditTextMaxPrice;
    private TextInputLayout textInputLayoutMinPrice;
    private TextInputLayout textInputLayoutMaxPrice;
    private MaterialButton buttonClearFilters;
    private MaterialButton buttonApplyFilters;

    private String selectedCategory;
    private BigDecimal currentMinPrice;
    private BigDecimal currentMaxPrice;

    // Constructor to set initial filter values
    public static FilterBottomSheetDialogFragment newInstance(String currentCategory, BigDecimal minPrice, BigDecimal maxPrice) {
        FilterBottomSheetDialogFragment fragment = new FilterBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString("currentCategory", currentCategory);
        if (minPrice != null) args.putString("minPrice", minPrice.toPlainString());
        if (maxPrice != null) args.putString("maxPrice", maxPrice.toPlainString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedCategory = getArguments().getString("currentCategory");
            String minPriceStr = getArguments().getString("minPrice");
            String maxPriceStr = getArguments().getString("maxPrice");
            if (minPriceStr != null) currentMinPrice = new BigDecimal(minPriceStr);
            if (maxPriceStr != null) currentMaxPrice = new BigDecimal(maxPriceStr);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_options, container, false);

        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        textInputEditTextMinPrice = view.findViewById(R.id.textInputEditTextMinPrice);
        textInputEditTextMaxPrice = view.findViewById(R.id.textInputEditTextMaxPrice);
        textInputLayoutMinPrice = view.findViewById(R.id.textInputLayoutMinPrice);
        textInputLayoutMaxPrice = view.findViewById(R.id.textInputLayoutMaxPrice);
        buttonClearFilters = view.findViewById(R.id.buttonClearFilters);
        buttonApplyFilters = view.findViewById(R.id.buttonApplyFilters);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Populate Categories
        populateCategories();

        // Set initial price range if available
        if (currentMinPrice != null) {
            textInputEditTextMinPrice.setText(currentMinPrice.toPlainString());
        }
        if (currentMaxPrice != null) {
            textInputEditTextMaxPrice.setText(currentMaxPrice.toPlainString());
        }

        buttonClearFilters.setOnClickListener(v -> {
            clearFilters();
        });

        buttonApplyFilters.setOnClickListener(v -> {
            applyFilters();
        });
    }

    public void setFilterApplyListener(FilterApplyListener listener) {
        this.filterApplyListener = listener;
    }

    private void populateCategories() {
        // Example categories from your strings.xml
        List<String> categories = Arrays.asList(
                getString(R.string.category_fiction),
                getString(R.string.category_non_fiction),
                getString(R.string.category_science),
                getString(R.string.category_history),
                getString(R.string.category_fantasy),
                getString(R.string.category_thriller),
                getString(R.string.category_romance),
                getString(R.string.category_biography),
                getString(R.string.category_programming)
        );

        for (String categoryName : categories) {
            Chip chip = (Chip) LayoutInflater.from(getContext()).inflate(R.layout.single_chip_layout, chipGroupCategories, false);
            chip.setText(categoryName);
            chipGroupCategories.addView(chip);

            // Pre-select if it was the current category
            if (selectedCategory != null && selectedCategory.equals(categoryName)) {
                chip.setChecked(true);
            }
        }
    }

    private void clearFilters() {
        chipGroupCategories.clearCheck();
        textInputEditTextMinPrice.setText("");
        textInputEditTextMaxPrice.setText("");
        textInputLayoutMinPrice.setError(null);
        textInputLayoutMaxPrice.setError(null);

        selectedCategory = null;
        currentMinPrice = null;
        currentMaxPrice = null;

        // Immediately apply cleared filters
        if (filterApplyListener != null) {
            filterApplyListener.onFilterApplied(null, null, null);
        }
        dismiss(); // Dismiss the bottom sheet
    }

    private void applyFilters() {
        // Validate price inputs
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        boolean isValid = true;

        textInputLayoutMinPrice.setError(null);
        textInputLayoutMaxPrice.setError(null);

        String minPriceStr = textInputEditTextMinPrice.getText().toString().trim();
        String maxPriceStr = textInputEditTextMaxPrice.getText().toString().trim();

        if (!minPriceStr.isEmpty()) {
            try {
                minPrice = new BigDecimal(minPriceStr);
                if (minPrice.compareTo(BigDecimal.ZERO) < 0) {
                    textInputLayoutMinPrice.setError("Price cannot be negative.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                textInputLayoutMinPrice.setError("Invalid number.");
                isValid = false;
            }
        }

        if (!maxPriceStr.isEmpty()) {
            try {
                maxPrice = new BigDecimal(maxPriceStr);
                if (maxPrice.compareTo(BigDecimal.ZERO) < 0) {
                    textInputLayoutMaxPrice.setError("Price cannot be negative.");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                textInputLayoutMaxPrice.setError("Invalid number.");
                isValid = false;
            }
        }

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            textInputLayoutMaxPrice.setError("Max price cannot be less than min price.");
            isValid = false;
        }

        if (!isValid) {
            return; // Don't apply filters if validation fails
        }

        // Get selected category
        int checkedChipId = chipGroupCategories.getCheckedChipId();
        String category = null;
        if (checkedChipId != View.NO_ID) {
            Chip checkedChip = chipGroupCategories.findViewById(checkedChipId);
            category = checkedChip.getText().toString();
        }

        if (filterApplyListener != null) {
            filterApplyListener.onFilterApplied(category, minPrice, maxPrice);
        }
        dismiss(); // Dismiss the bottom sheet
    }
}
