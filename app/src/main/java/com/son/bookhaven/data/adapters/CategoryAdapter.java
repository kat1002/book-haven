package com.son.bookhaven.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.son.bookhaven.R;
import com.son.bookhaven.data.dto.response.CategoryResponse;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<CategoryResponse> categories;
    private OnCategoryClickListener listener;

    public CategoryAdapter(List<CategoryResponse> categories) {
        this.categories = categories;
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryResponse category);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryResponse category = categories.get(position);
        holder.tvCategoryName.setText(category.getCategoryName());

        holder.cardCategory.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<CategoryResponse> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardCategory;
        TextView tvCategoryName;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCategory = itemView.findViewById(R.id.card_category);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}