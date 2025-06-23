package com.son.bookhaven.data.adapters;

import android.content.Context;
import android.graphics.Color; // For programmatic status color
import android.graphics.drawable.GradientDrawable; // For programmatic status color
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.son.bookhaven.R;
import com.son.bookhaven.data.model.Order;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private OnOrderClickListener listener;
    private Context context; // Needed for getColor

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
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
        Order order = orderList.get(position);

        holder.tvOrderId.setText("Order #" + order.getOderId());
        // Format LocalDateTime to a readable date string
        if (order.getOrderDate() != null) {
            DateTimeFormatter formatter = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault());
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                holder.tvOrderDate.setText(order.getOrderDate().format(formatter));
            }
        } else {
            holder.tvOrderDate.setText("N/A");
        }

        // Assuming you have a way to get total items (e.g., from order.orderDetails.size())
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
        if (order.getDeliveryAddress() != null) {
            holder.tvDeliveryAddressSummary.setText("Delivered to: " + order.getDeliveryAddress());
            holder.tvDeliveryAddressSummary.setVisibility(View.VISIBLE);
        } else {
            holder.tvDeliveryAddressSummary.setVisibility(View.GONE);
        }


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

    public void updateOrders(List<Order> newOrders) {
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