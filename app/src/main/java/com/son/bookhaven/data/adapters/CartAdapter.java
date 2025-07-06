package com.son.bookhaven.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.son.bookhaven.R;
import com.son.bookhaven.data.dto.response.CartItemResponse;

import java.util.ArrayList; // <--- IMPORT ArrayList
import java.util.List;
import java.util.Locale;
import android.util.Log;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    // --- CRITICAL CHANGE 1: Initialize cartItems here as a new, mutable list ---
    private final List<CartItemResponse> cartItems = new ArrayList<>();

    private final OnQuantityChangeListener onQuantityChangeListener;
    private final OnCheckedChangeListener onCheckedChangeListener;

    // Define interfaces for callbacks
    public interface OnQuantityChangeListener {
        void onQuantityChange(CartItemResponse item, int newQuantity);
    }

    public interface OnCheckedChangeListener {
        void onCheckedChange(CartItemResponse item, boolean isChecked);
    }

    // --- CRITICAL CHANGE 2: Modify constructor - it no longer needs to receive the list ---
    public CartAdapter(OnQuantityChangeListener onQuantityChangeListener,
                       OnCheckedChangeListener onCheckedChangeListener) {
        // 'this.cartItems' is already initialized above as a new ArrayList
        this.onQuantityChangeListener = onQuantityChangeListener;
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_book, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItemResponse item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    // Call this method to update the data in the adapter
    public void updateData(List<CartItemResponse> newItems) {
        Log.d("CartAdapter", "Updating data with " + newItems.size() + " items.");

        for (int i = 0; i < newItems.size(); i++) {
            CartItemResponse item = newItems.get(i);
            Log.d("CartAdapter", "  Incoming item " + i + ": " + item.getTitle() + " (Qty: " + item.getQuantity() + ")");
        }

        cartItems.clear(); // Clears the adapter's *internal* list
        cartItems.addAll(newItems); // Adds a *copy* of the elements from newItems to the adapter's internal list

        Log.d("CartAdapter", "Updated data with " + cartItems.size() + " items.");
        notifyDataSetChanged();
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        final MaterialCheckBox checkboxItem;
        final ShapeableImageView itemImage;
        final MaterialTextView itemName;
        final MaterialTextView itemPrice;
        final MaterialButton btnDecrement;
        final MaterialTextView tvQuantity;
        final MaterialButton btnIncrement;
        final MaterialTextView itemTotalPrice;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxItem = itemView.findViewById(R.id.checkbox_item);
            itemImage = itemView.findViewById(R.id.item_image);
            itemName = itemView.findViewById(R.id.item_name);
            itemPrice = itemView.findViewById(R.id.item_price);
            btnDecrement = itemView.findViewById(R.id.btn_decrement);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnIncrement = itemView.findViewById(R.id.btn_increment);
            itemTotalPrice = itemView.findViewById(R.id.item_total_price);
        }

        public void bind(CartItemResponse item) {
            checkboxItem.setChecked(item.getIsSelected());
            itemName.setText(item.getTitle());
            itemPrice.setText(String.format(Locale.US, "$%.2f", item.getPrice()));
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            itemTotalPrice.setText(String.format(Locale.US, "$%.2f", item.getTotalPrice()));

            itemImage.setImageResource(R.drawable.ic_book_placeholder);

            // Listeners
            checkboxItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.onCheckedChange(item, isChecked);
                }
            });

            btnDecrement.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    if (onQuantityChangeListener != null) {
                        onQuantityChangeListener.onQuantityChange(item, item.getQuantity() - 1);
                    }
                }
            });

            btnIncrement.setOnClickListener(v -> {
                if (onQuantityChangeListener != null) {
                    onQuantityChangeListener.onQuantityChange(item, item.getQuantity() + 1);
                }
            });
        }
    }
}