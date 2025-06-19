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
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.son.bookhaven.R;
import com.son.bookhaven.data.adapters.FeaturedBooksAdapter;
import com.son.bookhaven.data.adapters.NewArrivalsAdapter;
import com.son.bookhaven.data.model.Author;
import com.son.bookhaven.data.model.Book;
import com.son.bookhaven.data.model.BookImage;
import com.son.bookhaven.data.model.LanguageCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private RecyclerView rvFeaturedBooks, rvNewArrivals, rvSearchResults;
    private FeaturedBooksAdapter featuredBooksAdapter;
    private NewArrivalsAdapter newArrivalsAdapter;
    private NewArrivalsAdapter searchResultsAdapter; // This remains NewArrivalsAdapter as per previous change
    private MaterialButton btnCart;
    private MaterialCardView cardFiction, cardNonFiction, cardBestsellers, cardNewArrivals;
    private SearchBar searchBar;
    private SearchView searchView;

    private List<Book> allBooks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerViews(); // This will now call the smaller methods
        setupClickListeners();
        setupSearchBarAndSearchView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            loadSampleData();
        }

        return view;
    }

    private void initViews(View view) {
        rvFeaturedBooks = view.findViewById(R.id.rv_featured_books);
        rvNewArrivals = view.findViewById(R.id.rv_new_arrivals);
        btnCart = view.findViewById(R.id.btn_cart);
        cardFiction = view.findViewById(R.id.card_fiction);
        cardNonFiction = view.findViewById(R.id.card_non_fiction);
        cardBestsellers = view.findViewById(R.id.card_bestsellers);
        cardNewArrivals = view.findViewById(R.id.card_new_arrivals);
        searchBar = view.findViewById(R.id.search_bar);
        searchView = view.findViewById(R.id.search_view);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
    }

    // Refactored setupRecyclerViews method
    private void setupRecyclerViews() {
        setupFeaturedBooksRecyclerView();
        setupNewArrivalsRecyclerView();
        setupSearchResultsRecyclerView();
    }

    // New method for Featured Books RecyclerView setup
    private void setupFeaturedBooksRecyclerView() {
        rvFeaturedBooks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        featuredBooksAdapter = new FeaturedBooksAdapter(new ArrayList<>());
        featuredBooksAdapter.setOnBookClickListener(new FeaturedBooksAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(Book book) {
                Toast.makeText(getContext(), "Clicked: " + book.getTitle(), Toast.LENGTH_SHORT).show();
                // Navigate to book details
            }

            @Override
            public void onAddToCartClick(Book book) {
                Toast.makeText(getContext(), "Added " + book.getTitle() + " to cart", Toast.LENGTH_SHORT).show();
                // Add to cart logic
            }
        });
        rvFeaturedBooks.setAdapter(featuredBooksAdapter);
    }

    // New method for New Arrivals RecyclerView setup
    private void setupNewArrivalsRecyclerView() {
        rvNewArrivals.setLayoutManager(new LinearLayoutManager(getContext()));
        newArrivalsAdapter = new NewArrivalsAdapter(new ArrayList<>());
        newArrivalsAdapter.setOnBookClickListener(new NewArrivalsAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(Book book) {
                Toast.makeText(getContext(), "Clicked: " + book.getTitle(), Toast.LENGTH_SHORT).show();
                // Navigate to book details
            }

            @Override
            public void onAddToCartClick(Book book) {
                Toast.makeText(getContext(), "Added " + book.getTitle() + " to cart", Toast.LENGTH_SHORT).show();
                // Add to cart logic
            }
        });
        rvNewArrivals.setAdapter(newArrivalsAdapter);
    }

    // New method for Search Results RecyclerView setup
    private void setupSearchResultsRecyclerView() {
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsAdapter = new NewArrivalsAdapter(new ArrayList<>());
        searchResultsAdapter.setOnBookClickListener(new NewArrivalsAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(Book book) {
                Toast.makeText(getContext(), "Search Result Clicked: " + book.getTitle(), Toast.LENGTH_SHORT).show();
                // Navigate to book details from search results
                searchView.hide(); // Hide search view after selection
            }

            @Override
            public void onAddToCartClick(Book book) {
                Toast.makeText(getContext(), "Added " + book.getTitle() + " to cart from search", Toast.LENGTH_SHORT).show();
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

        cardFiction.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Fiction category clicked", Toast.LENGTH_SHORT).show();
            // Navigate to fiction books
        });

        cardNonFiction.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Non-Fiction category clicked", Toast.LENGTH_SHORT).show();
            // Navigate to non-fiction books
        });

        cardBestsellers.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Bestsellers clicked", Toast.LENGTH_SHORT).show();
            // Navigate to bestsellers
        });

        cardNewArrivals.setOnClickListener(v -> {
            Toast.makeText(getContext(), "New Arrivals clicked", Toast.LENGTH_SHORT).show();
            // Navigate to new arrivals
        });
    }

    private void setupSearchBarAndSearchView() {
        searchBar.setOnClickListener(v -> {
            searchView.show(); // Show the SearchView when SearchBar is clicked
        });

        searchView.setupWithSearchBar(searchBar); // Link SearchView to SearchBar

        // For Material SearchView, you listen to query changes via the EditText that it wraps.
        // The SearchView itself provides access to this EditText.
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

        // To handle the "submit" action (e.g., keyboard enter), you'd typically set an OnEditorActionListener
        // on the SearchView's internal EditText.
        searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getAction() == android.view.KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {
                performSearch(searchView.getText().toString()); // Perform search on submit
                searchView.hide(); // Hide the search view after submission
                return true; // Consume the event
            }
            return false;
        });

        if (searchView.getToolbar() != null) {
            searchView.getToolbar().setNavigationOnClickListener(v -> {
                searchView.hide(); // Hide the search view
            });
        }
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchResultsAdapter.updateBooks(new ArrayList<>()); // Clear results if query is empty
            return;
        }

        List<Book> filteredBooks = allBooks.stream()
                .filter(book -> book.getTitle().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)) ||
                        book.getAuthors().stream()
                                .anyMatch(author -> author.getAuthorName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))))
                .collect(Collectors.toList());

        searchResultsAdapter.updateBooks(filteredBooks);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadSampleData() {
        Log.d("HomeFragment", "Loading sample data...");

        // Sample Featured Books Data
        List<Book> featuredBooks = new ArrayList<>();

        // Book 1: The Silent Echo
        Book book1 = new Book();
        book1.setBookId(1);
        book1.setTitle("The Silent Echo");
        book1.setPublisherId(101);
        book1.setCategoryId(1);
        book1.setPublicationYear(2023);
        book1.setPrice(new BigDecimal("19.99"));
        book1.setIsbn("978-0-123456-78-9");
        book1.setLanguage(LanguageCode.English);
        book1.setCreatedAt(LocalDateTime.now().minusDays(30));
        book1.setUpdatedAt(LocalDateTime.now().minusDays(5));

        // Add authors for book1
        Set<Author> authors1 = new HashSet<>();
        Author author1 = new Author();
        author1.setAuthorId(1);
        author1.setAuthorName("Sarah Johnson");
        authors1.add(author1);
        book1.setAuthors(authors1);

        List<BookImage> bookImages1 = new ArrayList<>();
        BookImage bookImage1 = new BookImage();
        bookImage1.setBookImageId(1);
        bookImage1.setImageUrl("https://picsum.photos/200/300?random=1"); // Placeholder image
        bookImages1.add(bookImage1);
        book1.setBookImages(bookImages1);

        featuredBooks.add(book1);

        // Book 2: Digital Dreams
        Book book2 = new Book();
        book2.setBookId(2);
        book2.setTitle("Digital Dreams");
        book2.setPublisherId(102);
        book2.setCategoryId(2);
        book2.setPublicationYear(2024);
        book2.setPrice(new BigDecimal("24.99"));
        book2.setIsbn("978-0-234567-89-0");
        book2.setLanguage(LanguageCode.English);
        book2.setCreatedAt(LocalDateTime.now().minusDays(25));
        book2.setUpdatedAt(LocalDateTime.now().minusDays(3));

        // Add authors for book2
        Set<Author> authors2 = new HashSet<>();
        Author author2 = new Author();
        author2.setAuthorId(2);
        author2.setAuthorName("Alex Chen");
        authors2.add(author2);
        book2.setAuthors(authors2);

        book2.setBookImages(bookImages1);

        featuredBooks.add(book2);

        // Book 3: Ocean's Mystery
        Book book3 = new Book();
        book3.setBookId(3);
        book3.setTitle("Ocean's Mystery");
        book3.setPublisherId(103);
        book3.setCategoryId(3);
        book3.setPublicationYear(2023);
        book3.setPrice(new BigDecimal("21.99"));
        book3.setIsbn("978-0-345678-90-1");
        book3.setLanguage(LanguageCode.English);
        book3.setCreatedAt(LocalDateTime.now().minusDays(20));
        book3.setUpdatedAt(LocalDateTime.now().minusDays(2));

        // Add authors for book3
        Set<Author> authors3 = new HashSet<>();
        Author author3 = new Author();
        author3.setAuthorId(3);
        author3.setAuthorName("Maria Rodriguez");
        authors3.add(author3);
        book3.setAuthors(authors3);

        book3.setBookImages(bookImages1);
        featuredBooks.add(book3);

        // Book 4: City Lights
        Book book4 = new Book();
        book4.setBookId(4);
        book4.setTitle("City Lights");
        book4.setPublisherId(104);
        book4.setCategoryId(4);
        book4.setPublicationYear(2024);
        book4.setPrice(new BigDecimal("18.99"));
        book4.setIsbn("978-0-456789-01-2");
        book4.setLanguage(LanguageCode.English);
        book4.setCreatedAt(LocalDateTime.now().minusDays(15));
        book4.setUpdatedAt(LocalDateTime.now().minusDays(1));

        // Add authors for book4
        Set<Author> authors4 = new HashSet<>();
        Author author4 = new Author();
        author4.setAuthorId(4);
        author4.setAuthorName("David Kim");
        authors4.add(author4);
        book4.setAuthors(authors4);

        book4.setBookImages(bookImages1);
        featuredBooks.add(book4);

        // Sample New Arrivals Data
        List<Book> newArrivals = new ArrayList<>();

        // Book 5: The Last Secret
        Book book5 = new Book();
        book5.setBookId(5);
        book5.setTitle("The Last Secret");
        book5.setPublisherId(105);
        book5.setCategoryId(5);
        book5.setPublicationYear(2024);
        book5.setPrice(new BigDecimal("24.99"));
        book5.setIsbn("978-0-567890-12-3");
        book5.setLanguage(LanguageCode.English);
        book5.setCreatedAt(LocalDateTime.now().minusDays(10));
        book5.setUpdatedAt(LocalDateTime.now());

        // Add authors for book5
        Set<Author> authors5 = new HashSet<>();
        Author author5 = new Author();
        author5.setAuthorId(5);
        author5.setAuthorName("Michael Blake");
        authors5.add(author5);
        book5.setAuthors(authors5);

        book5.setBookImages(bookImages1);
        newArrivals.add(book5);

        // Book 6: Quantum Physics
        Book book6 = new Book();
        book6.setBookId(6);
        book6.setTitle("Quantum Physics");
        book6.setPublisherId(106);
        book6.setCategoryId(6);
        book6.setPublicationYear(2024);
        book6.setPrice(new BigDecimal("32.99"));
        book6.setIsbn("978-0-678901-23-4");
        book6.setLanguage(LanguageCode.English);
        book6.setCreatedAt(LocalDateTime.now().minusDays(8));
        book6.setUpdatedAt(LocalDateTime.now());

        // Add authors for book6
        Set<Author> authors6 = new HashSet<>();
        Author author6 = new Author();
        author6.setAuthorId(6);
        author6.setAuthorName("Dr. Lisa Wong");
        authors6.add(author6);
        book6.setAuthors(authors6);

        book6.setBookImages(bookImages1);
        newArrivals.add(book6);

        // Book 7: Modern Art
        Book book7 = new Book();
        book7.setBookId(7);
        book7.setTitle("Modern Art");
        book7.setPublisherId(107);
        book7.setCategoryId(7);
        book7.setPublicationYear(2024);
        book7.setPrice(new BigDecimal("28.99"));
        book7.setIsbn("978-0-789012-34-5");
        book7.setLanguage(LanguageCode.English);
        book7.setCreatedAt(LocalDateTime.now().minusDays(6));
        book7.setUpdatedAt(LocalDateTime.now());

        // Add authors for book7
        Set<Author> authors7 = new HashSet<>();
        Author author7 = new Author();
        author7.setAuthorId(7);
        author7.setAuthorName("Emma Thompson");
        authors7.add(author7);
        book7.setAuthors(authors7);

        book7.setBookImages(bookImages1);
        newArrivals.add(book7);

        // Book 8: Cooking Mastery
        Book book8 = new Book();
        book8.setBookId(8);
        book8.setTitle("Cooking Mastery");
        book8.setPublisherId(108);
        book8.setCategoryId(8);
        book8.setPublicationYear(2024);
        book8.setPrice(new BigDecimal("26.99"));
        book8.setIsbn("978-0-890123-45-6");
        book8.setLanguage(LanguageCode.English);
        book8.setCreatedAt(LocalDateTime.now().minusDays(4));
        book8.setUpdatedAt(LocalDateTime.now());

        // Add authors for book8
        Set<Author> authors8 = new HashSet<>();
        Author author8 = new Author();
        author8.setAuthorId(8);
        author8.setAuthorName("Chef Roberto");
        authors8.add(author8);
        book8.setAuthors(authors8);

        book8.setBookImages(bookImages1);
        newArrivals.add(book8);

        // Update adapters
        if (featuredBooksAdapter != null && newArrivalsAdapter != null) {
            featuredBooksAdapter.updateBooks(featuredBooks);
            newArrivalsAdapter.updateBooks(newArrivals);

            // Populate allBooks list for searching
            allBooks.clear();
            allBooks.addAll(featuredBooks);
            allBooks.addAll(newArrivals);
            // In a real app, you would fetch a comprehensive list of all books
            // from your data source to populate 'allBooks'.
        } else {
            Log.e("HomeFragment", "Adapters not initialized yet");
        }
    }
}