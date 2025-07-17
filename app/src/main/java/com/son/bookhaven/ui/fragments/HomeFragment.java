package com.son.bookhaven.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.son.bookhaven.R;
import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.apiHelper.BookVariantApiService;
import com.son.bookhaven.apiHelper.CategoryApiService;
import com.son.bookhaven.data.adapters.CategoryAdapter;
import com.son.bookhaven.data.adapters.FeaturedBooksAdapter;
import com.son.bookhaven.data.adapters.NewArrivalsAdapter;
import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.response.BookVariantResponse;
import com.son.bookhaven.data.dto.response.CategoryResponse;
import com.son.bookhaven.data.model.Author;
import com.son.bookhaven.data.model.BookVariant;
import com.son.bookhaven.data.model.LanguageCode;
import com.son.bookhaven.data.model.Publisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView rvFeaturedBooks, rvNewArrivals, rvSearchResults;
    private FeaturedBooksAdapter featuredBooksAdapter;
    private NewArrivalsAdapter newArrivalsAdapter;
    private NewArrivalsAdapter searchResultsAdapter;
    private MaterialButton btnCart;
    private SearchBar searchBar;
    private SearchView searchView;

    // Store all book variants
    private List<BookVariant> allBookVariants = new ArrayList<>();
    // Store only lowest price variants
    private List<BookVariant> lowestPriceVariants = new ArrayList<>();

    private BookVariantApiService bookVariantApiService;

    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private CategoryApiService categoryApiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        bookVariantApiService = ApiClient.getClient().create(BookVariantApiService.class);
        categoryApiService = ApiClient.getClient().create(CategoryApiService.class);

        initViews(view);
        setupRecyclerViews();
        setupClickListeners();
        setupSearchBarAndSearchView();

        // Load book variants from the API
        loadBookVariantsFromApi();
        loadCategoriesFromApi();

        return view;
    }

    private void initViews(View view) {
        rvCategories = view.findViewById(R.id.rv_categories);
        rvFeaturedBooks = view.findViewById(R.id.rv_featured_books);
        rvNewArrivals = view.findViewById(R.id.rv_new_arrivals);
        btnCart = view.findViewById(R.id.btn_cart);
        searchBar = view.findViewById(R.id.search_bar);
        searchView = view.findViewById(R.id.search_view);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
    }

    private void setupRecyclerViews() {
        setupCategoriesRecyclerView();
        setupFeaturedBooksRecyclerView();
        setupNewArrivalsRecyclerView();
        setupSearchResultsRecyclerView();
    }

    private void setupCategoriesRecyclerView() {
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(new ArrayList<>());
        // Navigate to category detail showing all book variants in this category
        categoryAdapter.setOnCategoryClickListener(this::navigateToCategoryBooks);
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupFeaturedBooksRecyclerView() {
        rvFeaturedBooks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        featuredBooksAdapter = new FeaturedBooksAdapter(new ArrayList<>());
        featuredBooksAdapter.setOnBookClickListener(new FeaturedBooksAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(BookVariant variant) {
                // Navigate to book variant details
                navigateToBookDetail(variant);
            }

            @Override
            public void onAddToCartClick(BookVariant variant) {
                Toast.makeText(getContext(), "Added " + variant.getTitle() + " to cart", Toast.LENGTH_SHORT).show();
                // Add to cart logic
            }
        });
        rvFeaturedBooks.setAdapter(featuredBooksAdapter);
    }

    private void setupNewArrivalsRecyclerView() {
        rvNewArrivals.setLayoutManager(new LinearLayoutManager(getContext()));
        newArrivalsAdapter = new NewArrivalsAdapter(new ArrayList<>());
        newArrivalsAdapter.setOnBookClickListener(new NewArrivalsAdapter.OnBookClickListener() {
            @Override
            public void onBookVariantClick(BookVariant variant) {
                // Navigate to book variant details
                navigateToBookDetail(variant);
            }

            @Override
            public void onAddToCartClick(BookVariant variant) {
                Toast.makeText(getContext(), "Added " + variant.getTitle() + " to cart", Toast.LENGTH_SHORT).show();
                // Add to cart logic
            }
        });
        rvNewArrivals.setAdapter(newArrivalsAdapter);
    }

    private void setupSearchResultsRecyclerView() {
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsAdapter = new NewArrivalsAdapter(new ArrayList<>());
        searchResultsAdapter.setOnBookClickListener(new NewArrivalsAdapter.OnBookClickListener() {
            @Override
            public void onBookVariantClick(BookVariant variant) {
                // Navigate to book variant details from search results
                navigateToBookDetail(variant);
                searchView.hide(); // Hide search view after selection
            }

            @Override
            public void onAddToCartClick(BookVariant variant) {
                Toast.makeText(getContext(), "Added " + variant.getTitle() + " to cart from search", Toast.LENGTH_SHORT).show();
                // Add to cart logic
            }
        });
        rvSearchResults.setAdapter(searchResultsAdapter);
    }

    private void setupClickListeners() {
        btnCart.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Cart clicked", Toast.LENGTH_SHORT).show();
            // Navigate to cart fragment
        });
    }

    private void setupSearchBarAndSearchView() {
        searchBar.setOnClickListener(v -> {
            searchView.show(); // Show the SearchView when SearchBar is clicked
        });

        searchView.setupWithSearchBar(searchBar); // Link SearchView to SearchBar

        searchView.getEditText().addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString()); // Perform search as text changes
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // Not used
            }
        });

        searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getAction() == android.view.KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {
                performSearch(searchView.getText().toString()); // Perform search on submit
                searchView.hide(); // Hide the search view after submission
                return true;
            }
            return false;
        });

        searchView.getToolbar();
        searchView.getToolbar().setNavigationOnClickListener(v -> {
            searchView.hide();
        });
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchResultsAdapter.updateBookVariants(new ArrayList<>());
            return;
        }

        // Search across all book variants
        List<BookVariant> searchResults = allBookVariants.stream()
                .filter(variant ->
                        variant.getTitle().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)) ||
                                (variant.getIsbn() != null && variant.getIsbn().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) ||
                                (variant.getDescription() != null && variant.getDescription().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) ||
                                (variant.getAuthors() != null && variant.getAuthors().stream().anyMatch(
                                        author -> author.getAuthorName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))))
                .collect(Collectors.toList());

        searchResultsAdapter.updateBookVariants(searchResults);
    }

    private void loadCategoriesFromApi() {
        categoryApiService.getAllCategories().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CategoryResponse>>> call,
                                   Response<ApiResponse<List<CategoryResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<CategoryResponse> categories = response.body().getData();
                    updateCategories(categories);
                } else {
                    // Handle API error
                    String errorMsg = "Failed to load categories";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    showError(errorMsg);
                    loadSampleCategoriesIfNeeded();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CategoryResponse>>> call, Throwable t) {
                Log.e(TAG, "Error loading categories", t);
                showError("Network error: " + t.getMessage());
                loadSampleCategoriesIfNeeded();
            }
        });
    }

    private void updateCategories(List<CategoryResponse> categories) {
        if (categoryAdapter != null && categories != null) {
            categoryAdapter.updateCategories(categories);
        }
    }

    private void loadSampleCategoriesIfNeeded() {
        List<CategoryResponse> sampleCategories = getSampleCategories();
        updateCategories(sampleCategories);
    }

    private List<CategoryResponse> getSampleCategories() {
        List<CategoryResponse> categories = new ArrayList<>();

        CategoryResponse fiction = new CategoryResponse();
        fiction.setCategoryId(1);
        fiction.setCategoryName("Fiction");
        fiction.setDescription("Fiction books");

        CategoryResponse nonFiction = new CategoryResponse();
        nonFiction.setCategoryId(2);
        nonFiction.setCategoryName("Non-Fiction");
        nonFiction.setDescription("Non-fiction books");

        CategoryResponse scienceFiction = new CategoryResponse();
        scienceFiction.setCategoryId(3);
        scienceFiction.setCategoryName("Science Fiction");
        scienceFiction.setDescription("Sci-fi books");

        CategoryResponse fantasy = new CategoryResponse();
        fantasy.setCategoryId(4);
        fantasy.setCategoryName("Fantasy");
        fantasy.setDescription("Fantasy books");

        categories.add(fiction);
        categories.add(nonFiction);
        categories.add(scienceFiction);
        categories.add(fantasy);

        return categories;
    }

    private void navigateToCategoryBooks(CategoryResponse category) {
        // Create a new fragment to display books by category
        CategoryBooksFragment categoryBooksFragment = CategoryBooksFragment.newInstance(
                category.getCategoryId(), category.getCategoryName());

        // Get the FragmentManager and start a transaction
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the current fragment with CategoryBooksFragment
        fragmentTransaction.replace(R.id.frame_layout, categoryBooksFragment);

        // Add the transaction to the back stack so the user can navigate back
        fragmentTransaction.addToBackStack(null);

        // Commit the transaction
        fragmentTransaction.commit();
    }

    private void loadBookVariantsFromApi() {
        // Show loading indicator if you have one

        // Call the API to get all book variants
        bookVariantApiService.getAllVariants().enqueue(new Callback<ApiResponse<List<BookVariantResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookVariantResponse>>> call, Response<ApiResponse<List<BookVariantResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<BookVariantResponse> variantResponses = response.body().getData();
                    allBookVariants = convertToBookVariantModels(variantResponses);

                    // Process to get only lowest price variants per book ID
                    lowestPriceVariants = getLowestPriceVariantPerBook(allBookVariants);

                    // Update UI with fetched book variants
                    updateFeaturedBooks();
                    updateNewArrivals();
                } else {
                    // Handle API error
                    String errorMsg = "Failed to load books";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    showError(errorMsg);
                    loadSampleDataIfNeeded();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookVariantResponse>>> call, Throwable t) {
                Log.e(TAG, "Error loading book variants", t);
                showError("Network error: " + t.getMessage());
                loadSampleDataIfNeeded();
            }
        });
    }

    private List<BookVariant> getLowestPriceVariantPerBook(List<BookVariant> allVariants) {
        Map<Integer, BookVariant> lowestPriceMap = new HashMap<>();

        for (BookVariant variant : allVariants) {
            int bookId = variant.getBookId();
            if (!lowestPriceMap.containsKey(bookId) ||
                    variant.getPrice().compareTo(lowestPriceMap.get(bookId).getPrice()) < 0) {
                lowestPriceMap.put(bookId, variant);
            }
        }

        return new ArrayList<>(lowestPriceMap.values());
    }

    private List<BookVariant> convertToBookVariantModels(List<BookVariantResponse> variantResponses) {
        List<BookVariant> variants = new ArrayList<>();

        for (BookVariantResponse response : variantResponses) {
            BookVariant variant = new BookVariant();
            variant.setVariantId(response.getVariantId());
            variant.setTitle(response.getTitle());
            variant.setDescription(response.getDescription());
            variant.setBookId(response.getBookId());
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
                    // Try ISO_DATE_TIME format first
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                    variant.setCreatedAt(LocalDateTime.parse(response.getCreatedAt(), formatter));
                } catch (Exception e) {
                    try {
                        // If that fails, try a more flexible pattern that matches your API response
                        DateTimeFormatter alternateFormatter =
                                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]");
                        variant.setCreatedAt(LocalDateTime.parse(response.getCreatedAt(), alternateFormatter));
                    } catch (Exception e2) {
                        // If all parsing fails, set a default date (now)
                        Log.e(TAG, "Date parsing error: " + response.getCreatedAt(), e2);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            variant.setCreatedAt(LocalDateTime.now());
                        }
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // If createdAt is null or empty, set current time
                variant.setCreatedAt(LocalDateTime.now());
            }

            if (response.getUpdatedAt() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                    variant.setUpdatedAt(LocalDateTime.parse(response.getUpdatedAt(), formatter));
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

    // Method to navigate to BookVariantDetailFragment
    private void navigateToBookDetail(BookVariant variant) {
        // Create an instance of BookVariantDetailFragment using the factory method
        BookDetailFragment bookDetailFragment = BookDetailFragment.newInstance(String.valueOf(variant.getBookId()),
                String.valueOf(variant.getVariantId()));

        // Get the FragmentManager and start a transaction
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the current fragment (HomeFragment) with BookVariantDetailFragment
        fragmentTransaction.replace(R.id.frame_layout, bookDetailFragment);

        // Add the transaction to the back stack so the user can navigate back
        fragmentTransaction.addToBackStack(null);

        // Commit the transaction
        fragmentTransaction.commit();
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads sample book variant data as a fallback when API calls fail.
     */
    private void loadSampleDataIfNeeded() {
        // Only load sample data if we don't have any variants loaded yet
        if (allBookVariants == null || allBookVariants.isEmpty()) {
            allBookVariants = getSampleBookVariants();
            lowestPriceVariants = getLowestPriceVariantPerBook(allBookVariants);

            // Update UI with sample data
            updateFeaturedBooks();
            updateNewArrivals();

            // Show a message that we're using sample data
            if (getContext() != null) {
                Toast.makeText(getContext(), "Showing sample data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Creates a list of sample book variants to display when offline or when API fails.
     */
    private List<BookVariant> getSampleBookVariants() {
        List<BookVariant> sampleVariants = new ArrayList<>();

        // Sample book variants for book ID 1
        BookVariant variant1A = new BookVariant();
        variant1A.setVariantId(101);
        variant1A.setTitle("The Great Gatsby (Hardcover)");
        variant1A.setDescription("A classic novel about American society in the 1920s");
        variant1A.setBookId(1);
        variant1A.setIsbn("9780743273565");
        variant1A.setPrice(new BigDecimal("14.99"));
        variant1A.setStock(50);
        variant1A.setCategoryId(1);
        variant1A.setPublisherId(1);
        variant1A.setPublicationYear(1925);
        variant1A.setLanguage(LanguageCode.English);

        Publisher publisher1 = new Publisher();
        publisher1.setPublisherId(1);
        publisher1.setPublisherName("Scribner");
        variant1A.setPublisher(publisher1);

        Author author1 = new Author();
        author1.setAuthorId(1);
        author1.setAuthorName("F. Scott Fitzgerald");
        Set<Author> authors1 = new HashSet<>();
        authors1.add(author1);
        variant1A.setAuthors(authors1);

        BookVariant variant1B = new BookVariant();
        variant1B.setVariantId(102);
        variant1B.setTitle("The Great Gatsby (Paperback)");
        variant1B.setDescription("A classic novel about American society in the 1920s");
        variant1B.setBookId(1);
        variant1B.setIsbn("9780743273566");
        variant1B.setPrice(new BigDecimal("9.99"));
        variant1B.setStock(100);
        variant1B.setCategoryId(1);
        variant1B.setPublisherId(1);
        variant1B.setPublicationYear(1925);
        variant1B.setLanguage(LanguageCode.English);
        variant1B.setPublisher(publisher1);
        variant1B.setAuthors(authors1);

        // Add both variants to the list
        sampleVariants.add(variant1A);
        sampleVariants.add(variant1B);

        // Sample book variants for book ID 2
        BookVariant variant2A = new BookVariant();
        variant2A.setVariantId(201);
        variant2A.setTitle("To Kill a Mockingbird (Hardcover)");
        variant2A.setDescription("Harper Lee's Pulitzer Prize-winning masterwork");
        variant2A.setBookId(2);
        variant2A.setIsbn("9780061120084");
        variant2A.setPrice(new BigDecimal("16.99"));
        variant2A.setStock(40);
        variant2A.setCategoryId(1);
        variant2A.setPublisherId(2);
        variant2A.setPublicationYear(1960);
        variant2A.setLanguage(LanguageCode.English);

        Publisher publisher2 = new Publisher();
        publisher2.setPublisherId(2);
        publisher2.setPublisherName("HarperCollins");
        variant2A.setPublisher(publisher2);

        Author author2 = new Author();
        author2.setAuthorId(2);
        author2.setAuthorName("Harper Lee");
        Set<Author> authors2 = new HashSet<>();
        authors2.add(author2);
        variant2A.setAuthors(authors2);

        BookVariant variant2B = new BookVariant();
        variant2B.setVariantId(202);
        variant2B.setTitle("To Kill a Mockingbird (Paperback)");
        variant2B.setDescription("Harper Lee's Pulitzer Prize-winning masterwork");
        variant2B.setBookId(2);
        variant2B.setIsbn("9780061120085");
        variant2B.setPrice(new BigDecimal("10.99"));
        variant2B.setStock(85);
        variant2B.setCategoryId(1);
        variant2B.setPublisherId(2);
        variant2B.setPublicationYear(1960);
        variant2B.setLanguage(LanguageCode.English);
        variant2B.setPublisher(publisher2);
        variant2B.setAuthors(authors2);

        // Add both variants to the list
        sampleVariants.add(variant2A);
        sampleVariants.add(variant2B);

        return sampleVariants;
    }

    private void updateFeaturedBooks() {
        if (featuredBooksAdapter != null && !lowestPriceVariants.isEmpty()) {
            // Get up to 5 variants for featured display
            int maxFeatured = Math.min(lowestPriceVariants.size(), 5);
            List<BookVariant> featuredVariants = lowestPriceVariants.subList(0, maxFeatured);
            featuredBooksAdapter.updateBookVariants(featuredVariants);
        }
    }

    private void updateNewArrivals() {
        if (newArrivalsAdapter != null && !lowestPriceVariants.isEmpty()) {
            // For new arrivals, get the most recently created variants
            List<BookVariant> newVariants = new ArrayList<>(lowestPriceVariants);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                newVariants.sort((a, b) -> {
                    if (a.getCreatedAt() == null || b.getCreatedAt() == null) {
                        return 0;
                    }
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                });
            }

            int maxNew = Math.min(newVariants.size(), 10);
            List<BookVariant> recentVariants = newVariants.subList(0, maxNew);
            newArrivalsAdapter.updateBookVariants(recentVariants);
        }
    }
}
