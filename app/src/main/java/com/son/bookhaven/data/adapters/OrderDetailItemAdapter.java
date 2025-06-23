package com.son.bookhaven.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.son.bookhaven.R;
import com.son.bookhaven.data.model.OrderDetail;

import java.util.List;
import java.util.Locale;

public class OrderDetailItemAdapter extends RecyclerView.Adapter<OrderDetailItemAdapter.OrderDetailItemViewHolder> {

    private List<OrderDetail> orderDetailsList;

    public OrderDetailItemAdapter(List<OrderDetail> orderDetailsList) {
        this.orderDetailsList = orderDetailsList;
    }

    @NonNull
    @Override
    public OrderDetailItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail_product, parent, false);
        return new OrderDetailItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailItemViewHolder holder, int position) {
        OrderDetail item = orderDetailsList.get(position);

        holder.tvProductName.setText(item.getBookName());
        holder.tvProductQuantity.setText("x" + item.getQuantity());
        holder.tvPricePerUnit.setText(String.format(Locale.getDefault(), "$%.2f / item", item.getPricePerUnit()));
        holder.tvItemSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", item.getSubTotal()));
    }

    @Override
    public int getItemCount() {
        return orderDetailsList.size();
    }

    public void updateOrderDetails(List<OrderDetail> newOrderDetails) {
        this.orderDetailsList.clear();
        this.orderDetailsList.addAll(newOrderDetails);
        notifyDataSetChanged();
    }

    static class OrderDetailItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvProductQuantity;
        TextView tvPricePerUnit;
        TextView tvItemSubtotal;

        public OrderDetailItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductQuantity = itemView.findViewById(R.id.tv_product_quantity);
            tvPricePerUnit = itemView.findViewById(R.id.tv_price_per_unit);
            tvItemSubtotal = itemView.findViewById(R.id.tv_item_subtotal);
        }
    }
}