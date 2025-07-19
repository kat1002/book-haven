package com.son.bookhaven.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.snackbar.Snackbar;
import com.son.bookhaven.R;
import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.apiHelper.BookVariantApiService;
import com.son.bookhaven.data.adapters.ExploreBookAdapter;
import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.response.BookVariantResponse;
import com.son.bookhaven.data.model.BookVariant;
import com.son.bookhaven.data.model.LanguageCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreFragment extends Fragment implements ExploreBookAdapter.OnItemClickListener,
        FilterBottomSheetDialogFragment.FilterApplyListener {

    private static final String TAG = "ExploreFragment";

    private SearchBar searchBar;
    private SearchView searchView;
    private MaterialButton btnFilter;
    private MaterialButton btnCart;
    private RecyclerView recyclerViewExploreBooks;
    private ExploreBookAdapter exploreBookAdapter;
    private RecyclerView rvExploreSearchResults;
    private ExploreBookAdapter searchResultsAdapter;

    private List<BookVariant> allBookVariants = new ArrayList<>();
    private List<BookVariant> filteredBookVariants = new ArrayList<>();

    // API service
    private BookVariantApiService bookVariantApiService;

    // Current filter state
    private Double currentFilterMinPrice = null;
    private Double currentFilterMaxPrice = null;
    private Integer currentFilterAuthorId = null;
    private Integer currentFilterCategoryId = null;
    private Integer currentFilterPublisherId = null;
    private LanguageCode currentFilterLanguage = null;
    private boolean isLoading = false;

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        bookVariantApiService = ApiClient.getClient().create(BookVariantApiService.class);

        // Initialize UI components
        searchBar = view.findViewById(R.id.search_bar_explore);
        searchView = view.findViewById(R.id.search_view_explore);
        btnFilter = view.findViewById(R.id.btn_filter);
      //  btnCart = view.findViewById(R.id.btn_cart_explore);
        recyclerViewExploreBooks = view.findViewById(R.id.recyclerViewExploreBooks);
        rvExploreSearchResults = view.findViewById(R.id.rv_explore_search_results);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup search functionality
        setupSearch();

        // Setup filter button
        setupFilterButton();

        // Load books
        loadBookVariants();

        return view;
    }

    private void setupRecyclerView() {
        // Setup main explore recyclerview
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerViewExploreBooks.setLayoutManager(layoutManager);
        exploreBookAdapter = new ExploreBookAdapter(new ArrayList<>(), this);
        recyclerViewExploreBooks.setAdapter(exploreBookAdapter);

        // Setup search results recyclerview
        GridLayoutManager searchLayoutManager = new GridLayoutManager(getContext(), 2);
        rvExploreSearchResults.setLayoutManager(searchLayoutManager);
        searchResultsAdapter = new ExploreBookAdapter(new ArrayList<>(), this);
        rvExploreSearchResults.setAdapter(searchResultsAdapter);
    }

    private void setupSearch() {
        searchBar.setOnClickListener(v -> searchView.show());
        searchView.setupWithSearchBar(searchBar);

        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });
    }

    private void setupFilterButton() {
        btnFilter.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void showFilterBottomSheet() {
        FilterBottomSheetDialogFragment filterBottomSheet = new FilterBottomSheetDialogFragment();
        filterBottomSheet.setFilterApplyListener(this);
        filterBottomSheet.show(getChildFragmentManager(), filterBottomSheet.getTag());
    }

    private void loadBookVariants() {
        isLoading = true;

        bookVariantApiService.getAllVariants().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookVariantResponse>>> call,
                                   Response<ApiResponse<List<BookVariantResponse>>> response) {
                isLoading = false;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<BookVariantResponse> bookVariantResponses = response.body().getData();
                    allBookVariants = convertToBookVariantModels(bookVariantResponses);
                    filteredBookVariants = new ArrayList<>(allBookVariants);
                    exploreBookAdapter.updateBookVariants(filteredBookVariants);
                } else {
                    showError("Failed to load books");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookVariantResponse>>> call, Throwable t) {
                isLoading = false;
                Log.e(TAG, "Error loading book variants", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private List<BookVariant> convertToBookVariantModels(List<BookVariantResponse> responses) {
        List<BookVariant> variants = new ArrayList<>();

        for (BookVariantResponse response : responses) {
            BookVariant variant = new BookVariant();
            variant.setVariantId(response.getVariantId());
            variant.setBookId(response.getBookId());
            variant.setTitle(response.getTitle());
            variant.setDescription(response.getDescription());
            variant.setIsbn(response.getIsbn());
            variant.setPrice(response.getPrice());
            variant.setStock(response.getStock());
            variant.setCategoryId(response.getCategory().getCategoryId());
            variant.setPublisherId(response.getPublisher().getPublisherId());
            variant.setPublicationYear(response.getPublicationYear());

            // Convert language code string to enum
            if (response.getLanguage() != null) {
                try {
                    variant.setLanguage(LanguageCode.valueOf(response.getLanguage()));
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid language code: " + response.getLanguage());
                }
            }

            // Set created/updated dates
            if (response.getCreatedAt() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                    variant.setCreatedAt(LocalDateTime.parse(response.getCreatedAt(), formatter));
                } catch (Exception e) {
                    Log.e(TAG, "Date parsing error", e);
                }
            }

            // Set related entities
            variant.setCategory(response.getCategory());
            variant.setPublisher(response.getPublisher());
            variant.setAuthors(new HashSet<>(response.getAuthors()));
            variant.setBookImages(response.getImages());

            variants.add(variant);
        }

        return variants;
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchResultsAdapter.updateBookVariants(new ArrayList<>());
            return;
        }

        // Search across all book variants
        List<BookVariant> searchResults = new ArrayList<>();

        for (BookVariant variant : allBookVariants) {
            if (variant.getTitle().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)) ||
                    (variant.getIsbn() != null && variant.getIsbn().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) ||
                    (variant.getDescription() != null && variant.getDescription().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) ||
                    (variant.getAuthors() != null && variant.getAuthors().stream().anyMatch(
                            author -> author.getAuthorName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))))) {

                searchResults.add(variant);
            }
        }

        searchResultsAdapter.updateBookVariants(searchResults);
    }

    @Override
    public void onFilterApplied(Double minPrice, Double maxPrice, Integer authorId, Integer categoryId,
                                Integer publisherId, String languageCode) {
        // Update filter state
        this.currentFilterMinPrice = minPrice;
        this.currentFilterMaxPrice = maxPrice;
        this.currentFilterAuthorId = authorId;
        this.currentFilterCategoryId = categoryId;
        this.currentFilterPublisherId = publisherId;

        // Convert language code string to enum if provided
        if (languageCode != null && !languageCode.isEmpty()) {
            try {
                this.currentFilterLanguage = LanguageCode.valueOf(languageCode);
            } catch (IllegalArgumentException e) {
                this.currentFilterLanguage = null;
                Log.e(TAG, "Invalid language code: " + languageCode, e);
            }
        } else {
            this.currentFilterLanguage = null;
        }

        // Apply filters
        applyFilters();

        // Log applied filters for debugging
        Log.d(TAG, "Filters applied: minPrice=" + minPrice + ", maxPrice=" + maxPrice +
                ", authorId=" + authorId + ", categoryId=" + categoryId +
                ", publisherId=" + publisherId + ", language=" + languageCode);
    }

    private void applyFilters() {
        // Start with all book variants
        List<BookVariant> result = new ArrayList<>();

        // For debugging
        int totalCount = allBookVariants.size();
        int matchedCount = 0;

        // Check each variant against the filters
        for (BookVariant variant : allBookVariants) {
            // Check if this variant matches all active filters
            boolean variantMatches = true;

            // Price range filter
            if (currentFilterMinPrice != null && (variant.getPrice() == null ||
                    variant.getPrice().compareTo(BigDecimal.valueOf(currentFilterMinPrice)) < 0)) {
                variantMatches = false;
            }

            if (currentFilterMaxPrice != null && (variant.getPrice() == null ||
                    variant.getPrice().compareTo(BigDecimal.valueOf(currentFilterMaxPrice)) > 0)) {
                variantMatches = false;
            }

            // Author filter
            if (currentFilterAuthorId != null && (variant.getAuthors() == null ||
                    variant.getAuthors().isEmpty() ||
                    variant.getAuthors().stream().noneMatch(a -> currentFilterAuthorId.equals(a.getAuthorId())))) {
                variantMatches = false;
            }

            // Category filter
            if (currentFilterCategoryId != null &&
                    currentFilterCategoryId != variant.getCategoryId()) {
                variantMatches = false;
            }

            // Publisher filter
            if (currentFilterPublisherId != null &&
                    currentFilterPublisherId != variant.getPublisherId()) {
                variantMatches = false;
            }

            // Language filter - null-safe comparison
            if (currentFilterLanguage != null &&
                    (variant.getLanguage() == null ||
                            !currentFilterLanguage.equals(variant.getLanguage()))) {
                variantMatches = false;
            }

            if (variantMatches) {
                result.add(variant);
                matchedCount++;
            }
        }

        // Log filter results for debugging
        Log.d(TAG, "Filter results: " + matchedCount + " out of " + totalCount + " books matched");

        // Update the filtered variants list and refresh the adapter
        filteredBookVariants = result;
        exploreBookAdapter.updateBookVariants(filteredBookVariants);

        // Show count of results
        showResultCount(filteredBookVariants.size());
    }

    private void showResultCount(int count) {
        if (getView() != null) {
            Snackbar.make(getView(), count + " books found", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(BookVariant variant) {
        // Navigate to book detail fragment
        navigateToBookDetail(variant);
    }

    private void navigateToBookDetail(BookVariant variant) {
        BookDetailFragment bookDetailFragment = BookDetailFragment.newInstance(String.valueOf(variant.getBookId()),
                String.valueOf(variant.getVariantId()));

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frame_layout, bookDetailFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
