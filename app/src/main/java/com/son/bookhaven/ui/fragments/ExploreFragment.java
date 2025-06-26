package com.son.bookhaven.ui.fragments; // A new package for explore UI

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.snackbar.Snackbar;
import com.son.bookhaven.R;
import com.son.bookhaven.data.adapters.ExploreBookAdapter;
import com.son.bookhaven.data.model.Author;
import com.son.bookhaven.data.model.Book;
import com.son.bookhaven.data.model.BookImage;
import com.son.bookhaven.data.model.LanguageCode;
import com.son.bookhaven.data.model.Publisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection; // Added import
import java.util.Collections; // Added import
import java.util.Comparator; // Added import
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors; // Added import for Java 8 stream operations

public class ExploreFragment extends Fragment implements ExploreBookAdapter.OnItemClickListener,
        FilterBottomSheetDialogFragment.FilterApplyListener { // Implemented FilterApplyListener

    private SearchBar searchBar;
    private SearchView searchView;
    private MaterialButton btnFilter;
    private MaterialButton btnCart;
    private RecyclerView recyclerViewExploreBooks;
    private ExploreBookAdapter exploreBookAdapter;
    private RecyclerView rvExploreSearchResults; // For search view results

    private List<Book> allBooks; // To hold all books for display and search
    private List<Book> filteredBooks; // Books after applying filters (excluding search)

    // Current filter state
    private String currentFilterCategory = null;
    private BigDecimal currentFilterMinPrice = null;
    private BigDecimal currentFilterMaxPrice = null;


    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        // Initialize UI components
        searchBar = view.findViewById(R.id.search_bar_explore);
        searchView = view.findViewById(R.id.search_view_explore);
        btnFilter = view.findViewById(R.id.btn_filter);
        btnCart = view.findViewById(R.id.btn_cart_explore);
        recyclerViewExploreBooks = view.findViewById(R.id.recyclerViewExploreBooks);
        rvExploreSearchResults = searchView.findViewById(R.id.rv_explore_search_results); // Get the RecyclerView from SearchView

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Populate dummy data
        allBooks = generateDummyBooks();
        filteredBooks = new ArrayList<>(allBooks); // Initially, filtered books are all books

        // Setup RecyclerView for 2-column display
        recyclerViewExploreBooks.setLayoutManager(new GridLayoutManager(getContext(), 2));
        exploreBookAdapter = new ExploreBookAdapter(filteredBooks, this);
        recyclerViewExploreBooks.setAdapter(exploreBookAdapter);

        // Setup SearchBar and SearchView
        searchView.setupWithSearchBar(searchBar);

        // Setup RecyclerView for search results within SearchView
        rvExploreSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
        // The search results adapter will be created and updated dynamically

        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Not used */ }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Perform search on filteredBooks (which already has category/price filters applied)
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { /* Not used */ }
        });


        // Setup filter button
        btnFilter.setOnClickListener(v -> {
            showFilterBottomSheet();
        });

        // Setup cart button
        btnCart.setOnClickListener(v -> {
            Snackbar.make(view, "Navigate to Cart", Snackbar.LENGTH_SHORT).show();
            // TODO: Implement navigation to cart fragment/activity
        });

        // Initially apply filters (if any saved state or defaults)
        applyFiltersToBooks();
    }

    /**
     * Shows the filter bottom sheet dialog.
     */
    private void showFilterBottomSheet() {
        FilterBottomSheetDialogFragment filterBottomSheet = FilterBottomSheetDialogFragment.newInstance(
                currentFilterCategory, currentFilterMinPrice, currentFilterMaxPrice
        );
        filterBottomSheet.setFilterApplyListener(this); // Set this fragment as the listener
        filterBottomSheet.show(getParentFragmentManager(), filterBottomSheet.getTag());
    }

    /**
     * Callback from FilterBottomSheetDialogFragment when filters are applied.
     * @param category The selected category, or null if no category selected.
     * @param minPrice The minimum price, or null if not set.
     * @param maxPrice The maximum price, or null if not set.
     */
    @Override
    public void onFilterApplied(String category, BigDecimal minPrice, BigDecimal maxPrice) {
        currentFilterCategory = category;
        currentFilterMinPrice = minPrice;
        currentFilterMaxPrice = maxPrice;
        applyFiltersToBooks();
        // Clear search view if filters are applied to show full filtered list
        searchView.setText(""); // This will trigger onTextChanged and clear search results
    }

    /**
     * Applies the current filter criteria to the `allBooks` list and updates `filteredBooks`.
     */
    private void applyFiltersToBooks() {
        List<Book> tempFilteredList = new ArrayList<>();

        for (Book book : allBooks) {
            boolean matchesCategory = true;
            if (currentFilterCategory != null && !currentFilterCategory.isEmpty()) {
                // Check if any of the book's authors match the category name for simplicity
                // In a real app, 'category' would be a proper Category object linked to the book
                boolean foundCategory = false;
                if (book.getAuthors() != null) { // For this dummy data, we are mapping category to author for simplicity
                    for (Author author : book.getAuthors()) {
                        if (currentFilterCategory.equalsIgnoreCase(getString(R.string.category_fiction)) && Arrays.asList("Matt Haig", "Colleen Hoover", "Delia Owens", "George Orwell", "Jane Austen", "Harper Lee", "F. Scott Fitzgerald", "Kristin Hannah").contains(author.getAuthorName())) {
                            foundCategory = true; break;
                        } else if (currentFilterCategory.equalsIgnoreCase(getString(R.string.category_non_fiction)) && Arrays.asList("James Clear", "Tara Westover", "Michelle Obama", "Walter Isaacson").contains(author.getAuthorName())) {
                            foundCategory = true; break;
                        } else if (currentFilterCategory.equalsIgnoreCase(getString(R.string.category_science)) && Arrays.asList("Carl Sagan", "Stephen Hawking").contains(author.getAuthorName())) {
                            foundCategory = true; break;
                        } else if (currentFilterCategory.equalsIgnoreCase(getString(R.string.category_history)) && Arrays.asList("Yuval Noah Harari", "Erik Larson").contains(author.getAuthorName())) {
                            foundCategory = true; break;
                        } else if (currentFilterCategory.equalsIgnoreCase(getString(R.string.category_fantasy)) && Arrays.asList("J.R.R. Tolkien", "Brandon Sanderson").contains(author.getAuthorName())) {
                            foundCategory = true; break;
                        } else if (currentFilterCategory.equalsIgnoreCase(getString(R.string.category_thriller)) && Arrays.asList("Stephen King", "Alex Michaelides", "Lucy Folk", "Gillian Flynn").contains(author.getAuthorName())) {
                            foundCategory = true; break;
                        } else if (currentFilterCategory.equalsIgnoreCase(getString(R.string.category_romance)) && Arrays.asList("Helen Hoang", "Casey McQuiston").contains(author.getAuthorName())) {
                            foundCategory = true; break;
                        } else if (currentFilterCategory.equalsIgnoreCase(getString(R.string.category_biography)) && Arrays.asList("Michelle Obama", "Walter Isaacson").contains(author.getAuthorName())) {
                            foundCategory = true; break;
                        } else if (currentFilterCategory.equalsIgnoreCase(getString(R.string.category_programming)) && Arrays.asList("Robert C. Martin", "Martin Fowler").contains(author.getAuthorName())) {
                            foundCategory = true; break;
                        }
                    }
                }
                matchesCategory = foundCategory;
            }

            boolean matchesPrice = true;
            if (currentFilterMinPrice != null && book.getPrice().compareTo(currentFilterMinPrice) < 0) {
                matchesPrice = false;
            }
            if (currentFilterMaxPrice != null && book.getPrice().compareTo(currentFilterMaxPrice) > 0) {
                matchesPrice = false;
            }

            if (matchesCategory && matchesPrice) {
                tempFilteredList.add(book);
            }
        }

        filteredBooks.clear();
        filteredBooks.addAll(tempFilteredList);
        exploreBookAdapter.notifyDataSetChanged();

        // Update the search results view as well if search view is active
        // This will happen automatically via text watcher if search text is not empty
        // but if it is empty, this ensures the main list is updated correctly.
        if (searchView.isShowing()) {
            performSearch(searchView.getText().toString());
        }

        Snackbar.make(requireView(), String.format(Locale.getDefault(), getString(R.string.filter_results_count), filteredBooks.size()), Snackbar.LENGTH_SHORT).show();
    }


    /**
     * Performs search on the `filteredBooks` list and updates the search results RecyclerView.
     * @param query The search query.
     */
    private void performSearch(String query) {
        List<Book> searchResults = new ArrayList<>();
        if (query.isEmpty()) {
            // If search query is empty, show the currently filtered books in main RecyclerView
            // and clear search results RecyclerView
            recyclerViewExploreBooks.setVisibility(View.VISIBLE);
            rvExploreSearchResults.setVisibility(View.GONE);
            exploreBookAdapter.notifyDataSetChanged(); // Re-notify main adapter to ensure it's up-to-date
            return;
        }

        for (Book book : filteredBooks) { // Search within the already filtered books
            boolean matchesTitle = book.getTitle() != null && book.getTitle().toLowerCase().contains(query.toLowerCase());
            boolean matchesAuthor = false;
            if (book.getAuthors() != null) {
                for (Author author : book.getAuthors()) {
                    if (author.getAuthorName() != null && author.getAuthorName().toLowerCase().contains(query.toLowerCase())) {
                        matchesAuthor = true;
                        break;
                    }
                }
            }
            if (matchesTitle || matchesAuthor) {
                searchResults.add(book);
            }
        }

        // Update search results RecyclerView
        ExploreBookAdapter searchResultsAdapter = new ExploreBookAdapter(searchResults, this);
        rvExploreSearchResults.setAdapter(searchResultsAdapter);
        recyclerViewExploreBooks.setVisibility(View.GONE); // Hide main RecyclerView
        rvExploreSearchResults.setVisibility(View.VISIBLE); // Show search results RecyclerView
    }


    @Override
    public void onItemClick(Book book) {
        if (getContext() != null) {
            Snackbar.make(requireView(), "Clicked on: " + book.getTitle(), Snackbar.LENGTH_SHORT).show();
            // Implement navigation to book detail screen
        }
    }

    /**
     * Generates dummy book items for demonstration purposes.
     * Includes diverse authors and categories for filtering example.
     * @return A list of dummy Book objects.
     */
    private List<Book> generateDummyBooks() {
        List<Book> dummyBooks = new ArrayList<>();

        // Create some dummy authors and publishers
        Author mattHaig = new Author("Matt Haig");
        Author colleenHoover = new Author("Colleen Hoover");
        Author deliaOwens = new Author("Delia Owens");
        Author stephenKing = new Author("Stephen King"); // Thriller
        Author georgeOrwell = new Author("George Orwell"); // Fiction
        Author janeAusten = new Author("Jane Austen"); // Fiction
        Author harperLee = new Author("Harper Lee"); // Fiction
        Author fScottFitzgerald = new Author("F. Scott Fitzgerald"); // Fiction
        Author jamesClear = new Author("James Clear"); // Non-Fiction
        Author taraWestover = new Author("Tara Westover"); // Non-Fiction
        Author carlSagan = new Author("Carl Sagan"); // Science
        Author stephenHawking = new Author("Stephen Hawking"); // Science
        Author yuvalNoahHarari = new Author("Yuval Noah Harari"); // History
        Author erikLarson = new Author("Erik Larson"); // History
        Author jrrTolkien = new Author("J.R.R. Tolkien"); // Fantasy
        Author brandonSanderson = new Author("Brandon Sanderson"); // Fantasy
        Author alexMichaelides = new Author("Alex Michaelides"); // Thriller
        Author helenHoang = new Author("Helen Hoang"); // Romance
        Author caseyMcQuiston = new Author("Casey McQuiston"); // Romance
        Author michelleObama = new Author("Michelle Obama"); // Biography
        Author walterIsaacson = new Author("Walter Isaacson"); // Biography
        Author robertCMartin = new Author("Robert C. Martin"); // Programming
        Author martinFowler = new Author("Martin Fowler"); // Programming


        Publisher publisher1 = new Publisher("Canongate Books");
        Publisher publisher2 = new Publisher("Atria Books");
        Publisher publisher3 = new Publisher("G.P. Putnam's Sons");
        Publisher publisher4 = new Publisher("Scribner");
        Publisher publisher5 = new Publisher("Secker & Warburg");
        Publisher publisher6 = new Publisher("Thomas Egerton");


        // Dummy Books with more varied data for filtering
        dummyBooks.add(createBook(1, "The Midnight Library", new BigDecimal("15.99"), new HashSet<>(Arrays.asList(mattHaig)), publisher1)); // Fiction
        dummyBooks.add(createBook(2, "It Ends with Us", new BigDecimal("12.50"), new HashSet<>(Arrays.asList(colleenHoover)), publisher2)); // Romance
        dummyBooks.add(createBook(3, "Where the Crawdads Sing", new BigDecimal("14.00"), new HashSet<>(Arrays.asList(deliaOwens)), publisher3)); // Fiction
        dummyBooks.add(createBook(4, "The Silent Patient", new BigDecimal("11.99"), new HashSet<>(Arrays.asList(alexMichaelides)), publisher4)); // Thriller
        dummyBooks.add(createBook(5, "1984", new BigDecimal("9.99"), new HashSet<>(Arrays.asList(georgeOrwell)), publisher5)); // Fiction
        dummyBooks.add(createBook(6, "Atomic Habits", new BigDecimal("17.50"), new HashSet<>(Arrays.asList(jamesClear)), publisher6)); // Non-Fiction
        dummyBooks.add(createBook(7, "Educated", new BigDecimal("11.00"), new HashSet<>(Arrays.asList(taraWestover)), publisher1)); // Non-Fiction
        dummyBooks.add(createBook(8, "Cosmos", new BigDecimal("22.00"), new HashSet<>(Arrays.asList(carlSagan)), publisher2)); // Science
        dummyBooks.add(createBook(9, "A Brief History of Time", new BigDecimal("13.00"), new HashSet<>(Arrays.asList(stephenHawking)), publisher3)); // Science
        dummyBooks.add(createBook(10, "Sapiens: A Brief History of Humankind", new BigDecimal("20.00"), new HashSet<>(Arrays.asList(yuvalNoahHarari)), publisher4)); // History
        dummyBooks.add(createBook(11, "The Hobbit", new BigDecimal("10.99"), new HashSet<>(Arrays.asList(jrrTolkien)), publisher5)); // Fantasy
        dummyBooks.add(createBook(12, "The Way of Kings", new BigDecimal("25.00"), new HashSet<>(Arrays.asList(brandonSanderson)), publisher6)); // Fantasy
        dummyBooks.add(createBook(13, "The Guest List", new BigDecimal("10.00"), new HashSet<>(Arrays.asList(brandonSanderson)), publisher1)); // Thriller
        dummyBooks.add(createBook(14, "Red, White & Royal Blue", new BigDecimal("13.00"), new HashSet<>(Arrays.asList(caseyMcQuiston)), publisher2)); // Romance
        dummyBooks.add(createBook(15, "Becoming", new BigDecimal("19.00"), new HashSet<>(Arrays.asList(michelleObama)), publisher3)); // Biography
        dummyBooks.add(createBook(16, "Steve Jobs", new BigDecimal("16.50"), new HashSet<>(Arrays.asList(walterIsaacson)), publisher4)); // Biography
        dummyBooks.add(createBook(17, "Clean Code", new BigDecimal("35.00"), new HashSet<>(Arrays.asList(robertCMartin)), publisher5)); // Programming
        dummyBooks.add(createBook(18, "Refactoring", new BigDecimal("32.00"), new HashSet<>(Arrays.asList(martinFowler)), publisher6)); // Programming
        dummyBooks.add(createBook(19, "The Nightingale", new BigDecimal("14.00"), new HashSet<>(Arrays.asList(brandonSanderson)), publisher1)); // Fiction
        dummyBooks.add(createBook(20, "Gone Girl", new BigDecimal("11.50"), new HashSet<>(Arrays.asList(brandonSanderson)), publisher2)); // Thriller


        return dummyBooks;
    }

    // Helper method to create a Book instance with dummy data
    private Book createBook(int id, String title, BigDecimal price, Set<Author> authors, Publisher publisher) {
        Book book = new Book();
        book.setBookId(id);
        book.setTitle(title);
        book.setPrice(price);
        book.setAuthors(authors);
        book.setPublisher(publisher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book.setCreatedAt(LocalDateTime.now());
            book.setUpdatedAt(LocalDateTime.now());
        }
        List<BookImage> bookImages = new ArrayList<>();
        BookImage bookImage = new BookImage();
        bookImage.setBookImageId(id);
        bookImage.setImageUrl("https://picsum.photos/200/300?random=" + id);
        bookImages.add(bookImage);
        book.setBookImages(bookImages);
        book.setLanguage(LanguageCode.English);
        book.setCategoryId(1); // Default category ID
        book.setPublisherId(publisher.hashCode()); // Simple publisher ID
        book.setPublicationYear(2023);
        book.setIsbn("ISBN" + id);
        return book;
    }
}
