package com.son.bookhaven.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.appbar.MaterialToolbar; // Import MaterialToolbar

import com.son.bookhaven.R;
import com.son.bookhaven.data.model.Book;

import java.util.Locale;
import java.util.stream.Collectors;

// If you use Glide/Coil, add their imports:
// import com.bumptech.glide.Glide; // For Glide
// import coil.load; // For Coil (Kotlin extension)

public class BookDetailFragment extends Fragment {

    private static final String ARG_BOOK = "book";

    private ShapeableImageView ivBookCover;
    private MaterialTextView tvBookTitle, tvBookAuthor, tvBookPrice, tvBookDescription, tvBookIsbn, tvBookPublicationYear, tvBookLanguage;
    private MaterialToolbar toolbar;

    private ExtendedFloatingActionButton fabAddToCart;
    private ExtendedFloatingActionButton fabBuyNow;

    private Book book; // To hold the received book object

    public BookDetailFragment() {
        // Required empty public constructor
    }

    // Use this factory method to create a new instance of
    // this fragment using the provided parameters.
    public static BookDetailFragment newInstance(Book book) {
        BookDetailFragment fragment = new BookDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BOOK, book); // Pass the Book object as Serializable
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = (Book) getArguments().getSerializable(ARG_BOOK); // Retrieve the Book object
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_detail, container, false);

        initViews(view);
        setupToolbar();

        if (book != null) {
            displayBookDetails(book);
        } else {
            // Handle case where book data is not passed
            Log.e("BookDetailFragment", "No book data received.");
            Toast.makeText(getContext(), "Book details not available.", Toast.LENGTH_SHORT).show();
            // Optionally, navigate back or show an error state
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        }

        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_book_detail);
        ivBookCover = view.findViewById(R.id.iv_book_cover);
        tvBookTitle = view.findViewById(R.id.tv_book_title);
        tvBookAuthor = view.findViewById(R.id.tv_book_author);
        tvBookPrice = view.findViewById(R.id.tv_book_price);
        tvBookDescription = view.findViewById(R.id.tv_book_description);
        fabAddToCart = view.findViewById(R.id.fab_add_to_cart);
        fabBuyNow = view.findViewById(R.id.fab_buy_now);
        tvBookIsbn = view.findViewById(R.id.tv_book_isbn);
        tvBookPublicationYear = view.findViewById(R.id.tv_book_publication_year);
        tvBookLanguage = view.findViewById(R.id.tv_book_language);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setTitle(book != null ? book.getTitle() : "Book Details");
            toolbar.setNavigationOnClickListener(v -> {
                // Handle back button click
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    // Fallback if no back stack, though usually should not happen
                    requireActivity().onBackPressed();
                }
            });
        }
    }

    private void displayBookDetails(Book book) {
        tvBookTitle.setText(book.getTitle());

        // Format authors
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            String authors = book.getAuthors().stream()
                    .map(author -> author.getAuthorName())
                    .collect(Collectors.joining(", "));
            tvBookAuthor.setText("by " + authors);
        } else {
            tvBookAuthor.setText("Unknown Author");
        }

        tvBookPrice.setText(String.format(Locale.US, "$%.2f", book.getPrice()));

        // Placeholder description (as your Book model might not have a full description field)
        // If your Book model has a 'description' field, use it:
        // tvBookDescription.setText(book.getDescription());
        tvBookDescription.setText("This is a sample description for the book \"" + book.getTitle() + "\". A full, engaging summary would go here in a real application, describing the plot, themes, and what makes the book unique.");

        tvBookIsbn.setText(String.format("ISBN: %s", book.getIsbn()));
        tvBookPublicationYear.setText(String.format("Published: %d", book.getPublicationYear()));
        tvBookLanguage.setText(String.format("Language: %s", book.getLanguage().name()));


        // Load image using Glide/Coil or a placeholder
        if (book.getBookImages() != null && !book.getBookImages().isEmpty() && book.getBookImages().get(0).getImageUrl() != null) {
            String imageUrl = book.getBookImages().get(0).getImageUrl();
            // --- Use Glide or Coil here for real image loading ---
            // Example with Glide:
            // Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_book_placeholder).into(ivBookCover);
            // Example with Coil (Kotlin):
            // ivBookCover.load(imageUrl) { placeholder(R.drawable.ic_book_placeholder) }
            // ---------------------------------------------------
            // For now, use placeholder image:
            ivBookCover.setImageResource(R.drawable.ic_book_placeholder);
        } else {
            ivBookCover.setImageResource(R.drawable.ic_book_placeholder);
        }

        fabAddToCart.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Add to Cart clicked (Floating)", Toast.LENGTH_SHORT).show();
            // Add to cart logic here
        });

        fabBuyNow.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Buy Now clicked (Floating)", Toast.LENGTH_SHORT).show();
            // Buy now logic here
        });
    }
}