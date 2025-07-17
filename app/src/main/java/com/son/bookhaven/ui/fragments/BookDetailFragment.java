package com.son.bookhaven.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.son.bookhaven.MainActivity;
import com.son.bookhaven.R;
import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.apiHelper.BookVariantApiService;
import com.son.bookhaven.apiHelper.CartApiService;
import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.request.CartItemUpdateRequest;
import com.son.bookhaven.data.dto.response.BookVariantResponse;
import com.son.bookhaven.data.model.Author;
import com.son.bookhaven.data.model.BookImage;
import com.son.bookhaven.data.model.BookVariant;
import com.son.bookhaven.data.model.LanguageCode;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailFragment extends Fragment {

    private static final String ARG_BOOK_ID = "book_id";
    private static final String ARG_VARIANT_ID = "variant_id";
    private static final String TAG = "BookDetailFragment";

    private ShapeableImageView ivBookCover;
    private MaterialTextView tvBookTitle, tvBookAuthor, tvBookPrice, tvBookDescription, tvBookIsbn, tvBookPublicationYear, tvBookLanguage;
    private MaterialToolbar toolbar;
    private Spinner variantSpinner;

    private ExtendedFloatingActionButton fabAddToCart;
    private ExtendedFloatingActionButton fabBuyNow;

    private String bookId; // To store the book ID
    private String variantId; // To store the initial variant ID
    private List<BookVariant> bookVariants = new ArrayList<>(); // To store all variants of the book
    private BookVariant selectedVariant; // Currently selected variant
    private BookVariantApiService bookVariantApiService;
    private final NumberFormat currencyFormatter;

    public BookDetailFragment() {
        // Required empty public constructor
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")); // Or your desired locale
        currencyFormatter.setGroupingUsed(true);
    }

    /**
     * Use this factory method to create a new instance of the fragment.
     *
     * @param bookId The ID of the book to display
     * @return A new instance of BookDetailFragment
     */
    public static BookDetailFragment newInstance(String bookId) {
        BookDetailFragment fragment = new BookDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BOOK_ID, bookId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Use this factory method when you want to show a specific variant.
     *
     * @param bookId    The book ID
     * @param variantId The specific variant ID to display initially
     * @return A new instance of BookDetailFragment
     */
    public static BookDetailFragment newInstance(String bookId, String variantId) {
        BookDetailFragment fragment = new BookDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BOOK_ID, bookId);
        args.putString(ARG_VARIANT_ID, variantId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookId = getArguments().getString(ARG_BOOK_ID);
            variantId = getArguments().getString(ARG_VARIANT_ID);
        }

        // Initialize API service
        bookVariantApiService = ApiClient.getClient().create(BookVariantApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_detail, container, false);

        initializeViews(view);
        setupToolbar();
        setupButtons();

        // Load book variants
        if (bookId != null) {
            loadBookVariants(bookId, variantId);
        } else {
            showError("Book ID is missing");
        }

        return view;
    }

    private void initializeViews(View view) {
        // Initialize all view components
        ivBookCover = view.findViewById(R.id.iv_book_cover);
        tvBookTitle = view.findViewById(R.id.tv_book_title);
        tvBookAuthor = view.findViewById(R.id.tv_book_author);
        tvBookPrice = view.findViewById(R.id.tv_book_price);
        tvBookDescription = view.findViewById(R.id.tv_book_description);
        tvBookIsbn = view.findViewById(R.id.tv_book_isbn);
        tvBookPublicationYear = view.findViewById(R.id.tv_book_publication_year);
        tvBookLanguage = view.findViewById(R.id.tv_book_language);
        toolbar = view.findViewById(R.id.toolbar_book_detail);
        fabAddToCart = view.findViewById(R.id.fab_add_to_cart);
        fabBuyNow = view.findViewById(R.id.fab_buy_now);
        variantSpinner = view.findViewById(R.id.spinner_variants);
    }

    private void setupVariantSpinner() {
        if (variantSpinner == null || bookVariants == null || bookVariants.isEmpty()) {
            return;
        }

        List<String> variantNames = new ArrayList<>();
        for (BookVariant variant : bookVariants) {
            // Create a descriptive name for each variant
            String variantName = variant.getTitle();
            if (variant.getLanguage() != null) {
                variantName += " (" + variant.getLanguage().toString() + ")";
            }
            variantNames.add(variantName);
        }

        // Create adapter and set it to the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,
                variantNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        variantSpinner.setAdapter(adapter);

        // Handle variant selection changes
        variantSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < bookVariants.size()) {
                    selectedVariant = bookVariants.get(position);
                    displayBookInfo(); // Update displayed info when variant changes
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                // Navigate back to previous fragment
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }
    }

    private void displayBookInfo() {
        if (selectedVariant == null) {
            return;
        }

        // Set book details from the selected variant
        tvBookTitle.setText(selectedVariant.getTitle());

        // Display author names
        if (selectedVariant.getAuthors() != null && !selectedVariant.getAuthors().isEmpty()) {
            String authors = selectedVariant.getAuthors().stream()
                    .map(Author::getAuthorName)
                    .collect(Collectors.joining(", "));
            tvBookAuthor.setText(getString(R.string.by_author, authors));
        } else {
            tvBookAuthor.setText(R.string.unknown_author);
        }

        // Set price
        if (selectedVariant.getPrice() != null) {
            tvBookPrice.setText(currencyFormatter.format(selectedVariant.getPrice()));
        } else {
            tvBookPrice.setText(R.string.price_not_available);
        }

        // Set description
        if (selectedVariant.getDescription() != null && !selectedVariant.getDescription().isEmpty()) {
            tvBookDescription.setText(selectedVariant.getDescription());
        } else {
            tvBookDescription.setText(R.string.no_description_available);
        }

        // Set ISBN
        if (selectedVariant.getIsbn() != null && !selectedVariant.getIsbn().isEmpty()) {
            tvBookIsbn.setText(getString(R.string.isbn_format, selectedVariant.getIsbn()));
        } else {
            tvBookIsbn.setText(R.string.isbn_not_available);
        }

        // Set publication year
        int year = selectedVariant.getPublicationYear();
        if (year > 0) {
            tvBookPublicationYear.setText(getString(R.string.published_year, String.valueOf(year)));
        } else {
            tvBookPublicationYear.setText(R.string.year_not_available);
        }

        // Set language
        if (selectedVariant.getLanguage() != null) {
            tvBookLanguage.setText(getString(R.string.language_format, selectedVariant.getLanguage().toString()));
        } else {
            tvBookLanguage.setText(R.string.language_not_available);
        }

        // Set book cover image
        if (selectedVariant.getBookImages() != null && !selectedVariant.getBookImages().isEmpty()) {
            BookImage coverImage = selectedVariant.getBookImages().get(0);
            // Load image with Glide
            Glide.with(requireContext())
                    .load(coverImage.getImageUrl())
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .centerCrop()
                    .into(ivBookCover);
        } else {
            // Set placeholder image
            ivBookCover.setImageResource(R.drawable.ic_book_placeholder);
        }

        // Update button states based on stock
        boolean isInStock = selectedVariant.getStock() > 0;
        fabAddToCart.setEnabled(isInStock);
        fabBuyNow.setVisibility(isInStock ? View.VISIBLE : View.GONE);
        if (!isInStock) {
            fabAddToCart.setText(R.string.out_of_stock);
        }
    }

    private void setupButtons() {
        fabAddToCart.setOnClickListener(v -> {
            if (selectedVariant != null) {
                Snackbar.make(v, "Adding " + selectedVariant.getTitle() + " to cart", Snackbar.LENGTH_SHORT).show();
                addToCart(selectedVariant);
            }
        });

        fabBuyNow.setOnClickListener(v -> {
            if (selectedVariant != null) {
                Snackbar.make(v, "Adding " + selectedVariant.getTitle() + " to cart", Snackbar.LENGTH_SHORT).show();
                addToCart(selectedVariant);
                navigateToCart();
            }
        });
    }

    private void navigateToCart() {
        // Create a new instance of CartFragment
        CartFragment cartFragment = new CartFragment();

        // Get the FragmentManager and start a transaction
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the current fragment with CartFragment
        fragmentTransaction.replace(R.id.frame_layout, cartFragment);

        // Add the transaction to the back stack so the user can navigate back
        fragmentTransaction.addToBackStack(null);

        // Commit the transaction
        fragmentTransaction.commit();
    }

    private void showLoading(boolean isLoading) {
        // TODO: Implement loading indicator
    }

    private void addToCart(BookVariant variant) {
        // Show loading indicator if needed
        CartApiService cartApiService = ApiClient.getAuthenticatedClient(requireContext()).create(CartApiService.class);

        CartItemUpdateRequest request = new CartItemUpdateRequest(
                variant.getVariantId(),
                1  // Adding 1 item to cart
        );

        cartApiService.updateCartItemQuantity(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {

                    // Update the cart badge in MainActivity
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateCartBadge(0); // This will trigger a refresh
                    }
                } else {
                    // Show error message
                    Snackbar.make(getView(), "Failed to add item to cart", Snackbar.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to add to cart. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Show network error
                Snackbar.make(getView(), "Network error. Please try again.", Snackbar.LENGTH_SHORT).show();
                Log.e(TAG, "Error adding to cart: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBookVariants(String bookId, String initialVariantId) {
        // Show loading state
        showLoading(true);

        // Call API to get all variants for this book ID
        bookVariantApiService.getVariantsByBookId(bookId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<BookVariantResponse>>> call,
                                   @NonNull Response<ApiResponse<List<BookVariantResponse>>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<BookVariantResponse> variantResponses = response.body().getData();

                    if (variantResponses != null && !variantResponses.isEmpty()) {
                        Log.d(TAG, "First variant data: " + variantResponses.get(0).toString());
                        // Convert responses to model objects
                        bookVariants = convertToBookVariants(variantResponses);

                        // Setup spinner with the variants
                        setupVariantSpinner();

                        // Select the initial variant if provided, otherwise use the first one
                        if (initialVariantId != null) {
                            selectVariant(initialVariantId);
                        } else {
                            selectedVariant = bookVariants.get(0);
                            displayBookInfo();
                        }
                    } else {
                        showError("No variants found for this book");
                    }
                } else {
                    showError("Failed to load book variants");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<BookVariantResponse>>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "API call failed", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private List<BookVariant> convertToBookVariants(List<BookVariantResponse> responses) {
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
            variant.setLanguage(LanguageCode.valueOf(response.getLanguage()));
            variant.setCategory(response.getCategory());
            variant.setPublisher(response.getPublisher());
            variant.setAuthors(response.getAuthors());
            variant.setBookImages(response.getImages());

            variants.add(variant);
        }

        return variants;
    }

    private void selectVariant(String variantId) {
        int variantIdInt;
        try {
            variantIdInt = Integer.parseInt(variantId);

            for (int i = 0; i < bookVariants.size(); i++) {
                if (bookVariants.get(i).getVariantId() == variantIdInt) {
                    selectedVariant = bookVariants.get(i);
                    variantSpinner.setSelection(i);
                    displayBookInfo();
                    return;
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid variant ID format: " + variantId, e);
        }

        // If variant not found or invalid, use the first one
        if (!bookVariants.isEmpty()) {
            selectedVariant = bookVariants.get(0);
            variantSpinner.setSelection(0);
            displayBookInfo();
        }
    }
}