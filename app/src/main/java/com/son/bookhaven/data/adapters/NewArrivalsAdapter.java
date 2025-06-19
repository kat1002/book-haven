package com.son.bookhaven.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.son.bookhaven.R;
import com.son.bookhaven.data.model.Book;

import java.util.List;

public class NewArrivalsAdapter extends RecyclerView.Adapter<NewArrivalsAdapter.NewArrivalViewHolder> {

    private List<Book> books;
    private OnBookClickListener onBookClickListener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
        void onAddToCartClick(Book book);
    }

    public NewArrivalsAdapter(List<Book> books) {
        this.books = books;
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.onBookClickListener = listener;
    }

    public void updateBooks(List<Book> newBooks) {
        this.books = newBooks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewArrivalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_new_arrival_book, parent, false);
        return new NewArrivalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewArrivalViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return books != null ? books.size() : 0;
    }

    class NewArrivalViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBookCover;
        private MaterialTextView tvBookTitle;
        private MaterialTextView tvAuthorName;
        private MaterialTextView tvRating;
        private MaterialTextView tvPrice;
        private MaterialButton btnAddToCart;
        private LinearLayout ratingLayout;

        public NewArrivalViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.iv_book_cover);
            tvBookTitle = itemView.findViewById(R.id.tv_book_title);
            tvAuthorName = itemView.findViewById(R.id.tv_author_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onBookClickListener != null) {
                    onBookClickListener.onBookClick(books.get(position));
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Book book = books.get(position);
                    if (onBookClickListener != null) {
                        onBookClickListener.onAddToCartClick(book);
                    } else {
                        // Default behavior if no listener is set
                        Toast.makeText(v.getContext(),
                                "Added " + book.getTitle() + " to cart",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        public void bind(Book book) {
            tvBookTitle.setText(book.getTitle());
            tvAuthorName.setText(book.getAuthors().stream().findFirst().orElse(null).getAuthorName());
            tvPrice.setText(book.getPrice().toString());

            // Load book cover image
            loadBookCover(book.getBookImages().stream().findFirst().orElse(null).getImageUrl());
        }

        private void setupStarRating(float rating) {
            // Get the parent view of the rating layout to find star ImageViews
            View parentView = itemView;

            // Example implementation:
            int fullStars = (int) rating;
            boolean hasHalfStar = (rating % 1) >= 0.5;

            // You can implement star display logic here
            // For now, just showing the rating in text format
        }

        private void loadBookCover(String coverImage) {
            // For now, set a placeholder. Replace with actual image loading logic
            // Example with Glide:
            // Glide.with(ivBookCover.getContext())
            //     .load(coverImage)
            //     .placeholder(R.drawable.book_placeholder)
            //     .error(R.drawable.book_placeholder)
            //     .into(ivBookCover);

            // For demonstration, set a placeholder
            ivBookCover.setImageResource(R.drawable.ic_book_placeholder);
        }
    }
}