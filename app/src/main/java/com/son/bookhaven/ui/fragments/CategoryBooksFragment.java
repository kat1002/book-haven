package com.son.bookhaven.ui.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.son.bookhaven.R;
import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.apiHelper.BookVariantApiService;
import com.son.bookhaven.data.adapters.NewArrivalsAdapter;
import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.response.BookVariantResponse;
import com.son.bookhaven.data.model.BookVariant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryBooksFragment extends Fragment {
    private static final String TAG = "CategoryBooksFragment";
    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";

    private int categoryId;
    private String categoryName;

    private TextView tvCategoryTitle;
    private RecyclerView rvCategoryBooks;
    private NewArrivalsAdapter categoryBooksAdapter;
    private BookVariantApiService bookVariantApiService;

    public static CategoryBooksFragment newInstance(int categoryId, String categoryName) {
        CategoryBooksFragment fragment = new CategoryBooksFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getInt(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_books, container, false);

        // Initialize API service
        bookVariantApiService = ApiClient.getClient().create(BookVariantApiService.class);

        // Initialize views
        tvCategoryTitle = view.findViewById(R.id.tv_category_title);
        rvCategoryBooks = view.findViewById(R.id.rv_category_books);

        // Set category name
        tvCategoryTitle.setText(categoryName);

        // Setup RecyclerView
        setupRecyclerView();

        // Load books by category
        loadBooksByCategory();

        return view;
    }

    private void setupRecyclerView() {
        // Use GridLayoutManager with 3 columns instead of LinearLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvCategoryBooks.setLayoutManager(gridLayoutManager);

        // Add spacing between grid items
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        rvCategoryBooks.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        categoryBooksAdapter = new NewArrivalsAdapter(new ArrayList<>());
        categoryBooksAdapter.setOnBookClickListener(new NewArrivalsAdapter.OnBookClickListener() {
            @Override
            public void onBookVariantClick(BookVariant variant) {
                // Navigate to book detail
                navigateToBookDetail(variant);
            }

            @Override
            public void onAddToCartClick(BookVariant variant) {
                // Check if item has stock before adding to cart
                if (variant.getStock() <= 0) {
                    Toast.makeText(getContext(), "Sorry, this item is out of stock",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(getContext(), "Added " + variant.getTitle() + " to cart",
                        Toast.LENGTH_SHORT).show();
                // Add to cart logic
            }
        });
        rvCategoryBooks.setPadding(16, 16, 16, 16);
        rvCategoryBooks.setClipToPadding(false);
        rvCategoryBooks.setAdapter(categoryBooksAdapter);
    }

    /**
     * RecyclerView item decoration - give equal margin around grid items
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }

    private void loadBooksByCategory() {
        bookVariantApiService.getVariantsByCategoryId(categoryId).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookVariantResponse>>> call,
                                   Response<ApiResponse<List<BookVariantResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<BookVariantResponse> variantResponses = response.body().getData();
                    List<BookVariant> variants = convertToBookVariantModels(variantResponses);
                    categoryBooksAdapter.updateBookVariants(variants);
                } else {
                    // Handle API error
                    String errorMsg = "Failed to load books";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    showError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookVariantResponse>>> call, Throwable t) {
                Log.e(TAG, "Error loading book variants for category", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private List<BookVariant> convertToBookVariantModels(List<BookVariantResponse> variantResponses) {
        // Use the same conversion method from HomeFragment
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

            // Set related entities
            variant.setCategory(response.getCategory());
            variant.setPublisher(response.getPublisher());
            variant.setAuthors(new HashSet<>(response.getAuthors()));
            variant.setBookImages(response.getImages());

            variants.add(variant);
        }

        return variants;
    }

    private void navigateToBookDetail(BookVariant variant) {
        // Same as in HomeFragment
        BookDetailFragment bookDetailFragment = BookDetailFragment.newInstance(
                String.valueOf(variant.getBookId()),
                String.valueOf(variant.getVariantId()));

        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, bookDetailFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}