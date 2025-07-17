package com.son.bookhaven.data.adapters; // Assuming this is your adapter package

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;
import com.son.bookhaven.R;
import com.son.bookhaven.data.model.Author;
import com.son.bookhaven.data.model.BookImage;
import com.son.bookhaven.data.model.BookVariant;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ExploreBookAdapter extends RecyclerView.Adapter<ExploreBookAdapter.ViewHolder> {

    private List<BookVariant> variants;
    private final NumberFormat currencyFormatter;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BookVariant variant);
    }

    public ExploreBookAdapter(List<BookVariant> variants, OnItemClickListener listener) {
        this.variants = variants;
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")); // Or your desired locale
        currencyFormatter.setGroupingUsed(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_explore_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookVariant variant = variants.get(position);

        // Safely set title
        holder.textViewBookTitle.setText(variant.getTitle() != null ? variant.getTitle() : "Unknown Title");

        // Safely format price
        if (variant.getPrice() != null) {
            holder.textViewBookPrice.setText(currencyFormatter.format(variant.getPrice()));
        } else {
            holder.textViewBookPrice.setText("N/A");
        }

        // Get author names, handle multiple authors with null checks
        String authorNames = "Unknown Author";
        if (variant.getAuthors() != null && !variant.getAuthors().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Author author : variant.getAuthors()) {
                if (author != null && author.getAuthorName() != null) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(author.getAuthorName());
                }
            }
            if (sb.length() > 0) {
                authorNames = sb.toString();
            }
        }
        holder.textViewBookAuthor.setText(authorNames);

        // Safely load book cover image
        if (variant.getBookImages() != null && !variant.getBookImages().isEmpty()) {
            BookImage bookImage = variant.getBookImages().stream()
                    .filter(img -> img != null && img.getImageUrl() != null && !img.getImageUrl().isEmpty())
                    .findFirst()
                    .orElse(null);

            if (bookImage != null && bookImage.getImageUrl() != null) {
                // Use Glide to load the image
                Glide.with(holder.imageViewBookCover.getContext())
                        .load(bookImage.getImageUrl())
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_book_placeholder)
                        .into(holder.imageViewBookCover);
            } else {
                holder.imageViewBookCover.setImageResource(R.drawable.ic_book_placeholder);
            }
        } else {
            holder.imageViewBookCover.setImageResource(R.drawable.ic_book_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(variant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return variants.size();
    }

    public void updateBookVariants(List<BookVariant> newVariants) {
        this.variants = newVariants;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewBookCover;
        MaterialTextView textViewBookTitle;
        MaterialTextView textViewBookAuthor;
        MaterialTextView textViewBookPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewBookCover = itemView.findViewById(R.id.imageViewBookCover);
            textViewBookTitle = itemView.findViewById(R.id.textViewBookTitle);
            textViewBookAuthor = itemView.findViewById(R.id.textViewBookAuthor);
            textViewBookPrice = itemView.findViewById(R.id.textViewBookPrice);
        }
    }
}
