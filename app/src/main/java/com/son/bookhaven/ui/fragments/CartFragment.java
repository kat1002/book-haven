package com.son.bookhaven.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.son.bookhaven.data.adapters.CartAdapter;
import com.son.bookhaven.data.model.Author;
import com.son.bookhaven.data.model.Book;
import com.son.bookhaven.data.model.BookImage;
import com.son.bookhaven.data.model.CartItem;
import com.son.bookhaven.data.model.LanguageCode;
import com.son.bookhaven.databinding.FragmentCartBinding; // Make sure ViewBinding is enabled and package correct

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding; // For ViewBinding
    private CartAdapter cartAdapter;
    private final List<CartItem> cartItemsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cartAdapter = new CartAdapter(
                (item, newQuantity) -> {
                    // your onQuantityChange logic
                    // This callback is triggered when quantity changes in the adapter's view.
                    // You should update the item in the *fragment's* `cartItemsList` here
                    // and then potentially call updateCartTotals and notify the adapter again
                    // or just notifyItemChanged on the adapter directly if only quantity changed.
                    item.setQuantity(newQuantity); // Update the item in the *fragment's* `cartItemsList`
                    // To reflect this change immediately in the UI and totals:
                    updateCartTotals();
                    // Option 1: Re-send the entire list (simpler, but less efficient for single item change)
                    // cartAdapter.updateData(cartItemsList);
                    // Option 2: Notify specific item changed (more efficient)
                    int index = cartItemsList.indexOf(item); // Find the item's position in fragment's list
                    if (index != -1) {
                        binding.rvCartItems.getAdapter().notifyItemChanged(index);
                    }
                },
                (item, isChecked) -> {
                    // your onCheckedChange logic
                    // Update the item in the *fragment's* `cartItemsList` here
                    item.setChecked(isChecked);
                    updateCartTotals();
                    // No specific notifyItemChanged usually needed for checkbox unless view needs full rebind
                    // or just let updateCartTotals refresh the whole UI.
                }
        );

        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCartItems.setAdapter(cartAdapter);

        // --- Load Data ---
        loadCartItems();

        // --- Button Listeners ---
        binding.btnUncheckAll.setOnClickListener(v -> {
            for (CartItem item : cartItemsList) {
                item.setChecked(false);
            }
            cartAdapter.notifyDataSetChanged(); // Refresh all items
            updateCartTotals();
        });

        binding.btnSelectAll.setOnClickListener(v -> {
            for (CartItem item : cartItemsList) {
                item.setChecked(true);
            }
            cartAdapter.notifyDataSetChanged(); // Refresh all items
            updateCartTotals();
        });

        binding.btnCheckout.setOnClickListener(v -> {
            // Handle "Proceed to Checkout" logic here
            // You can get selected items:
            // List<CartItem> selectedItems = cartItemsList.stream()
            //     .filter(CartItem::isChecked)
            //     .collect(Collectors.toList());
            // Then pass them to your next screen or process them.
            // Example: Toast.makeText(getContext(), "Proceeding to checkout with " + selectedItems.size() + " items", Toast.LENGTH_SHORT).show();
        });

        // Initial calculation
        updateCartTotals();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Release the binding when the view is destroyed
    }

    private void loadCartItems() {

        Log.d("LoadCartItems", "Loading cart items...");

        // This is where you'd fetch your actual cart data, e.g., from a database or API
        // For now, populate with dummy data using the Book and CartItem structure:
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book1.setCreatedAt(LocalDateTime.now().minusDays(30));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book1.setUpdatedAt(LocalDateTime.now().minusDays(5));
        }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book2.setCreatedAt(LocalDateTime.now().minusDays(25));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book2.setUpdatedAt(LocalDateTime.now().minusDays(3));
        }

        // Add authors for book2
        Set<Author> authors2 = new HashSet<>();
        Author author2 = new Author();
        author2.setAuthorId(2);
        author2.setAuthorName("Alex Chen");
        authors2.add(author2);
        book2.setAuthors(authors2);

        book2.setBookImages(bookImages1);

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book3.setCreatedAt(LocalDateTime.now().minusDays(20));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            book3.setUpdatedAt(LocalDateTime.now().minusDays(2));
        }

        // Add authors for book3
        Set<Author> authors3 = new HashSet<>();
        Author author3 = new Author();
        author3.setAuthorId(3);
        author3.setAuthorName("Maria Rodriguez");
        authors3.add(author3);
        book3.setAuthors(authors3);

        book3.setBookImages(bookImages1);

        cartItemsList.add(new CartItem(book1, 2, true));
        cartItemsList.add(new CartItem(book2, 1, true));
        cartItemsList.add(new CartItem(book3, 3, false));

        Log.d("LoadCartItems", "Loaded " + cartItemsList.size() + " items.");

        cartAdapter.updateData(cartItemsList);
    }

    private void updateCartTotals() {
        BigDecimal subtotal = BigDecimal.ZERO; // Initialize with BigDecimal.ZERO
        for (CartItem item : cartItemsList) {
            if (item.isChecked()) {
                subtotal = subtotal.add(item.getTotalPrice()); // Use BigDecimal.add()
            }
        }
        binding.subtotalText.setText(String.format(Locale.US, "$%.2f", subtotal));
        binding.totalText.setText(String.format(Locale.US, "$%.2f", subtotal));

        // You can add logic for discount, shipping, etc., here if needed
        // binding.discountLayout.setVisibility(discount > 0 ? View.VISIBLE : View.GONE);
        // binding.discountText.setText(String.format(Locale.US, "-$%.2f", discount));
    }
}