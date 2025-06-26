package com.son.bookhaven.data.adapters; // Adjust package as necessary

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Removed ImageView import as it's no longer used
// import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.son.bookhaven.R; // Assuming R is generated for your project, adjust if needed
import com.son.bookhaven.data.model.CartItem;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private List<CartItem> cartItems;
    private NumberFormat currencyFormatter;

    public CartItemAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US); // Or your desired locale
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_item_checkout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.textViewBookTitle.setText(item.getBook().getTitle());
        // Accessing the first author's name from the Book's authors set (if available)
        // If there are multiple authors, you might want to join them or pick the primary one.
        String authorName = "";
        if (item.getBook().getAuthors() != null && !item.getBook().getAuthors().isEmpty()) {
            // Get the first author (example, adjust logic for multiple authors if needed)
            authorName = item.getBook().getAuthors().iterator().next().getAuthorName();
        }
        holder.textViewBookAuthor.setText(authorName);
        holder.textViewQuantity.setText(String.format(Locale.US, "Qty: %d", item.getQuantity())); // Format quantity

        BigDecimal itemTotalPrice = item.getTotalPrice();
        holder.textViewItemPrice.setText(currencyFormatter.format(itemTotalPrice));

        // Image loading logic removed as the ImageView is no longer in the layout
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // ImageView for book image removed
        // ImageView imageViewBook;
        MaterialTextView textViewBookTitle;
        MaterialTextView textViewBookAuthor;
        MaterialTextView textViewQuantity;
        MaterialTextView textViewItemPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // ImageView initialization removed
            // imageViewBook = itemView.findViewById(R.id.imageViewBook);
            textViewBookTitle = itemView.findViewById(R.id.textViewBookTitle);
            textViewBookAuthor = itemView.findViewById(R.id.textViewBookAuthor);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewItemPrice = itemView.findViewById(R.id.textViewItemPrice);
        }
    }
}
