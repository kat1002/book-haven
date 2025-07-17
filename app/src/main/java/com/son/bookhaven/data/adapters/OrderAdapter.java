package com.son.bookhaven.data.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.son.bookhaven.R;
import com.son.bookhaven.data.dto.OrderResponse;

import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<OrderResponse> orderList;
    private OnOrderClickListener listener;
    private Context context; // Needed for getColor

    public interface OnOrderClickListener {
        void onOrderClick(OrderResponse order);
    }

    public OrderAdapter(Context context, List<OrderResponse> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderResponse order = orderList.get(position);

        holder.tvOrderId.setText("Order #" + order.getOrderId());
        // Format date string (already a string from API)
        if (order.getOrderDate() != null) {
            // Parse the date string and format it nicely
            try {
                String dateStr = order.getOrderDate();
                // Remove the time part if it exists and show just the date
                if (dateStr.contains("T")) {
                    dateStr = dateStr.substring(0, dateStr.indexOf("T"));
                }
                holder.tvOrderDate.setText(dateStr);
            } catch (Exception e) {
                holder.tvOrderDate.setText("N/A");
            }
        } else {
            holder.tvOrderDate.setText("N/A");
        }

        // Get total items from order details
        if (order.getOrderDetails() != null) {
            holder.tvTotalItems.setText(order.getOrderDetails().size() + " items");
        } else {
            holder.tvTotalItems.setText("0 items"); // Or hide this view
        }

        // Display total amount (consider formatting for currency)
        holder.tvTotalAmount.setText(String.format(Locale.getDefault(), "$%.2f", order.getTotalAmount()));

        // Order Status
        holder.tvOrderStatus.setText(order.getStatus());
        setOrderStatusBackground(holder.tvOrderStatus, order.getStatus());

        // Delivery Address Summary
        String deliveryAddress = order.getStreet() + ", " + order.getWard() + ", " + order.getDistrict() + ", " + order.getCity();
        holder.tvDeliveryAddressSummary.setText("Delivered to: " + deliveryAddress);
        holder.tvDeliveryAddressSummary.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateOrders(List<OrderResponse> newOrders) {
        this.orderList.clear();
        this.orderList.addAll(newOrders);
        notifyDataSetChanged();
    }

    // Helper to set status background color dynamically
    private void setOrderStatusBackground(TextView textView, String status) {
        int color;
        switch (status.toLowerCase(Locale.getDefault())) {
            case "delivered":
                color = context.getResources().getColor(R.color.md_theme_tertiary, null); // Green
                break;
            case "pending":
                color = context.getResources().getColor(R.color.md_theme_secondary, null); // Orange/Amber
                break;
            case "shipped":
                color = context.getResources().getColor(R.color.md_theme_primary, null); // Blue
                break;
            case "cancelled":
                color = context.getResources().getColor(R.color.md_theme_error, null); // Red
                break;
            default:
                color = Color.GRAY; // Default gray
                break;
        }
        // Use the same drawable shape but change its color
        GradientDrawable drawable = (GradientDrawable) textView.getBackground().mutate();
        drawable.setColor(color);
        textView.setBackground(drawable);
        textView.setTextColor(context.getResources().getColor(android.R.color.white, null)); // Ensure text is white
    }


    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId;
        TextView tvOrderDate;
        TextView tvTotalItems;
        TextView tvTotalAmount;
        TextView tvOrderStatus;
        TextView tvDeliveryAddressSummary;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvTotalItems = itemView.findViewById(R.id.tv_total_items);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvDeliveryAddressSummary = itemView.findViewById(R.id.tv_delivery_address_summary);
        }
    }
}