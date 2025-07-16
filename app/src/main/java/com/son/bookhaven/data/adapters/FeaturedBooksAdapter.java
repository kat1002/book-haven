package com.son.bookhaven.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.son.bookhaven.R;
import com.son.bookhaven.data.model.BookImage;
import com.son.bookhaven.data.model.BookVariant;

import java.util.List;

public class FeaturedBooksAdapter extends RecyclerView.Adapter<FeaturedBooksAdapter.FeaturedBookViewHolder> {

    private List<BookVariant> bookVariants;
    private OnBookClickListener onBookClickListener;

    public interface OnBookClickListener {
        void onBookClick(BookVariant variant);

        void onAddToCartClick(BookVariant variant);
    }

    public FeaturedBooksAdapter(List<BookVariant> bookVariants) {
        this.bookVariants = bookVariants;
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.onBookClickListener = listener;
    }

    public void updateBookVariants(List<BookVariant> newBookVariants) {
        this.bookVariants = newBookVariants;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeaturedBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_featured_book, parent, false);
        return new FeaturedBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedBookViewHolder holder, int position) {
        BookVariant variant = bookVariants.get(position);
        holder.bind(variant);
    }

    @Override
    public int getItemCount() {
        return bookVariants != null ? bookVariants.size() : 0;
    }

    class FeaturedBookViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBookCover;
        private MaterialTextView tvBookTitle;
        private MaterialTextView tvAuthorName;
        private MaterialTextView tvPrice;
        private MaterialButton btnAddToCart;

        public FeaturedBookViewHolder(@NonNull View itemView) {
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
                    onBookClickListener.onBookClick(bookVariants.get(position));
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    BookVariant variant = bookVariants.get(position);
                    if (onBookClickListener != null) {
                        onBookClickListener.onAddToCartClick(variant);
                    } else {
                        // Default behavior if no listener is set
                        Toast.makeText(v.getContext(),
                                "Added " + variant.getTitle() + " to cart",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        public void bind(BookVariant variant) {
            tvBookTitle.setText(variant.getTitle());

            // Safely get author name if available
            if (variant.getAuthors() != null && !variant.getAuthors().isEmpty()) {
                tvAuthorName.setText(variant.getAuthors().stream().findFirst().orElse(null).getAuthorName());
            } else {
                tvAuthorName.setText("Unknown Author");
            }

            // Safely display price if available
            if (variant.getPrice() != null) {
                tvPrice.setText(variant.getPrice().toString());
            } else {
                tvPrice.setText("N/A");
            }

            // Safely load book cover image
            String imageUrl = null;
            if (variant.getBookImages() != null && !variant.getBookImages().isEmpty()) {
                BookImage bookImage = variant.getBookImages().stream().findFirst().orElse(null);
                if (bookImage != null) {
                    imageUrl = bookImage.getImageUrl();
                }
            }

            loadBookCover(imageUrl);
        }

        private void loadBookCover(String coverImageUrl) {
            // For demonstration, set a placeholder
//            ivBookCover.setImageResource(R.drawable.ic_book_placeholder);

            // When you implement image loading, use this pattern:
            if (coverImageUrl != null && !coverImageUrl.isEmpty()) {
                Glide.with(ivBookCover.getContext())
                        .load(coverImageUrl)
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_book_placeholder)
                        .into(ivBookCover);
            } else {
                ivBookCover.setImageResource(R.drawable.ic_book_placeholder);
            }
        }
    }
}