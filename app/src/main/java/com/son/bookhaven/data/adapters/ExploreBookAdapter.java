package com.son.bookhaven.data.adapters; // Assuming this is your adapter package

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.son.bookhaven.R; // Ensure R is correctly imported
import com.son.bookhaven.data.model.Book;
import com.son.bookhaven.data.model.Author;
// Assuming you have a utility for image loading like Glide or Picasso,
// otherwise you'll need to manually set images or provide a placeholder.
// import com.bumptech.glide.Glide; // Example if using Glide

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ExploreBookAdapter extends RecyclerView.Adapter<ExploreBookAdapter.ViewHolder> {

    private List<Book> books;
    private NumberFormat currencyFormatter;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    public ExploreBookAdapter(List<Book> books, OnItemClickListener listener) {
        this.books = books;
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US); // Or your desired locale
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
        Book book = books.get(position);

        holder.textViewBookTitle.setText(book.getTitle());
        holder.textViewBookPrice.setText(currencyFormatter.format(book.getPrice()));

        // Get author names, handle multiple authors
        String authorNames = "";
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Author author : book.getAuthors()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(author.getAuthorName()); // Assuming Author has getAuthorName()
            }
            authorNames = sb.toString();
        }
        holder.textViewBookAuthor.setText(authorNames);

        // Load book cover image
        // If you have a real image URL, use a library like Glide or Picasso here.
        // For example, using Glide:
        // if (book.getBookImages() != null && !book.getBookImages().isEmpty()) {
        //     Glide.with(holder.imageViewBookCover.getContext())
        //          .load(book.getBookImages().get(0).getImageUrl()) // Assuming first image is cover
        //          .placeholder(R.drawable.placeholder_book) // Your placeholder drawable
        //          .error(R.drawable.error_book) // Your error drawable
        //          .into(holder.imageViewBookCover);
        // } else {
        //     holder.imageViewBookCover.setImageResource(R.drawable.placeholder_book);
        // }
        // For now, setting a static placeholder
        holder.imageViewBookCover.setImageResource(R.drawable.ic_book_placeholder);


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
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
