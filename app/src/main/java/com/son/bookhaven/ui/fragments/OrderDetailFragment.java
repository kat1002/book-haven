package com.son.bookhaven.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.son.bookhaven.R;
import com.son.bookhaven.data.model.Order;
import com.son.bookhaven.data.model.OrderDetail;
import com.son.bookhaven.data.adapters.OrderDetailItemAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class OrderDetailFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";
    private static final String TAG = "OrderDetailFragment";

    private MaterialToolbar toolbar;
    private ProgressBar progressBar;

    // Order Summary
    private TextView tvDetailOrderId, tvDetailOrderDate, tvDetailStatus,
            tvDetailPaymentMethod, tvDetailVoucherCode, tvDetailNote;

    // Delivery Information
    private TextView tvDetailRecipientName, tvDetailRecipientPhone, tvDetailDeliveryAddress;

    // Order Items RecyclerView
    private RecyclerView rvOrderItems;
    private OrderDetailItemAdapter orderDetailItemAdapter;
    private List<OrderDetail> orderDetailsList;
    private View contentContainer;

    // Total Amounts
    private TextView tvDetailTotalAmount, tvDetailDiscountedPrice, tvDetailFinalPayable;

    // Timestamps
    private TextView tvDetailCreatedAt, tvDetailUpdatedAt;

    private int orderId; // The ID of the order to display

    public OrderDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param orderId Parameter 1.
     * @return A new instance of fragment OrderDetailFragment.
     */
    public static OrderDetailFragment newInstance(int orderId) {
        OrderDetailFragment fragment = new OrderDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getInt(ARG_ORDER_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_detail, container, false);

        initViews(view); // Pass the inflated view to initViews
        setupToolbar();
        setupOrderItemsRecyclerView();
        loadOrderDetail(orderId); // Load the specific order's details

        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_order_detail);
        progressBar = view.findViewById(R.id.progress_bar_detail);
        // Corrected: Initialize contentContainer from the fragment's root view
        contentContainer = view.findViewById(R.id.fragment_order_detail_content_container);

        // Order Summary
        tvDetailOrderId = view.findViewById(R.id.tv_detail_order_id);
        tvDetailOrderDate = view.findViewById(R.id.tv_detail_order_date);
        tvDetailStatus = view.findViewById(R.id.tv_detail_status);
        tvDetailPaymentMethod = view.findViewById(R.id.tv_detail_payment_method);
        tvDetailVoucherCode = view.findViewById(R.id.tv_detail_voucher_code);
        tvDetailNote = view.findViewById(R.id.tv_detail_note);

        // Delivery Information
        tvDetailRecipientName = view.findViewById(R.id.tv_detail_recipient_name);
        tvDetailRecipientPhone = view.findViewById(R.id.tv_detail_recipient_phone);
        tvDetailDeliveryAddress = view.findViewById(R.id.tv_detail_delivery_address);

        // Order Items RecyclerView
        rvOrderItems = view.findViewById(R.id.rv_order_items);

        // Total Amounts
        tvDetailTotalAmount = view.findViewById(R.id.tv_detail_total_amount);
        tvDetailDiscountedPrice = view.findViewById(R.id.tv_detail_discounted_price);
        tvDetailFinalPayable = view.findViewById(R.id.tv_detail_final_payable);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    requireActivity().onBackPressed();
                }
            });
        }
    }

    private void setupOrderItemsRecyclerView() {
        orderDetailsList = new ArrayList<>();
        orderDetailItemAdapter = new OrderDetailItemAdapter(orderDetailsList);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderItems.setAdapter(orderDetailItemAdapter);
    }

    private void loadOrderDetail(int id) {
        showLoadingState();

        // Simulate fetching order details from a backend/database
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Order order = getDummyOrderById(id); // Get a dummy order

            if (order != null) {
                displayOrderDetails(order);
                showContentState();
                Log.d(TAG, "Order details loaded for ID: " + id);
            } else {
                showErrorState("Order not found.");
                Log.e(TAG, "Order with ID " + id + " not found.");
            }
        }, 800); // Simulate network delay
    }

    private void displayOrderDetails(Order order) {
        // Order Summary
        tvDetailOrderId.setText(getString(R.string.order_id_format, order.getOderId()));
        tvDetailOrderDate.setText(getString(R.string.order_date_format,
                formatDateTime(order.getOrderDate())));
        tvDetailStatus.setText(getString(R.string.order_status_format, order.getStatus()));
        tvDetailPaymentMethod.setText(getString(R.string.payment_method_format, getPaymentMethodString(order.getPaymentMethod())));

        if (order.getVoucherCode() != null && !order.getVoucherCode().isEmpty()) {
            tvDetailVoucherCode.setVisibility(View.VISIBLE);
            tvDetailVoucherCode.setText(getString(R.string.voucher_code_format, order.getVoucherCode()));
        } else {
            tvDetailVoucherCode.setVisibility(View.GONE);
        }

        if (order.getNote() != null && !order.getNote().isEmpty()) {
            tvDetailNote.setVisibility(View.VISIBLE);
            tvDetailNote.setText(getString(R.string.note_format, order.getNote()));
        } else {
            tvDetailNote.setVisibility(View.GONE);
        }

        // Delivery Information
        // Assume Order has an OrderDeliveryAddress object
        if (order.getDeliveryAddress() != null) {
            tvDetailRecipientName.setText(getString(R.string.recipient_name_format, order.getRecipientName()));
            tvDetailRecipientPhone.setText(getString(R.string.recipient_phone_format, order.getPhone()));
            tvDetailDeliveryAddress.setText(getString(R.string.delivery_address_format, order.getDeliveryAddress()));
        } else {
            // Hide delivery info section or show "Not available"
            tvDetailRecipientName.setText(getString(R.string.recipient_name_format, "N/A"));
            tvDetailRecipientPhone.setText(getString(R.string.recipient_phone_format, "N/A"));
            tvDetailDeliveryAddress.setText(getString(R.string.delivery_address_format, "N/A"));
        }


        // Order Items
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            orderDetailItemAdapter.updateOrderDetails(order.getOrderDetails());
            rvOrderItems.setVisibility(View.VISIBLE);
        } else {
            rvOrderItems.setVisibility(View.GONE);
            // Optionally, show a text indicating "No items in this order"
        }

        // Total Amounts
        tvDetailTotalAmount.setText(String.format(Locale.getDefault(), "$%.2f", order.getTotalAmount()));
        tvDetailDiscountedPrice.setText(String.format(Locale.getDefault(), "-$%.2f", order.getDiscountedPrice()));
        double finalPayable = order.getTotalAmount() - order.getDiscountedPrice();
        tvDetailFinalPayable.setText(String.format(Locale.getDefault(), "$%.2f", finalPayable));

    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd,yyyy HH:mm a", Locale.getDefault());
            return dateTime.format(formatter);
        } else {
            // Fallback for older Android versions (pre-API 26)
            // You might need to use SimpleDateFormat and convert LocalDateTime to Date if supporting older APIs extensively
            return dateTime.toString(); // Simple string conversion for older APIs
        }
    }

    private String getPaymentMethodString(byte method) {
        switch (method) {
            case 0: return "Cash on Delivery";
            case 1: return "Credit Card";
            case 2: return "Bank Transfer";
            // Add more cases as per your payment method byte codes
            default: return "Unknown";
        }
    }

    private void showLoadingState() {
        progressBar.setVisibility(View.VISIBLE);
        // Corrected: Use the contentContainer member variable
        if (contentContainer != null) {
            contentContainer.setVisibility(View.GONE);
        }
    }

    private void showContentState() {
        progressBar.setVisibility(View.GONE);
        // Corrected: Use the contentContainer member variable
        if (contentContainer != null) {
            contentContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorState(String message) {
        progressBar.setVisibility(View.GONE);
        // Corrected: Use the contentContainer member variable
        if (contentContainer != null) {
            contentContainer.setVisibility(View.GONE); // Ensure content is hidden on error
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        // Optionally, pop the fragment or show a retry button
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else {
            requireActivity().onBackPressed();
        }
    }

    // --- Dummy Data Retrieval (replace with real API/DB call) ---
    private Order getDummyOrderById(int id) {
        // This simulates fetching a single order. In a real app, this would be
        // an API call to get specific order details.
        // For now, we'll return a hardcoded order based on ID.
        if (id == 123456) {

            Order order = new Order();
            order.setCity("Thành phố Hà Nội");
            order.setDistrict("Quận Ba Đình");
            order.setWard("Phường Trúc Bạch");
            order.setStreet("asdfasdf");
            order.setOderId(123456);
            order.setUserId(1); // Dummy user ID
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                order.setOrderDate(LocalDateTime.now().minusDays(10).minusHours(2));
            }
            order.setTotalAmount(175.00);
            order.setDiscountedPrice(25.00); // 175.00 - 25.00 = 150.00
            order.setStatus("Delivered");
            order.setNote("Please deliver after 2 PM.");
            order.setPaymentMethod((byte) 0); // 0 = Cash on Delivery
            order.setVoucherCode("SUMMER2025");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                order.setCreatedAt(LocalDateTime.now().minusDays(10).minusHours(3));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                order.setUpdatedAt(LocalDateTime.now().minusDays(9));
            }

            // Order Details (Products)
            List<OrderDetail> details = new ArrayList<>();
            details.add(new OrderDetail(1, 123456, 101, "The Great Gatsby", 2, 12.50));
            details.add(new OrderDetail(2, 123456, 102, "To Kill a Mockingbird", 1, 25.00));
            details.add(new OrderDetail(3, 123456, 103, "1984", 3, 15.00)); // Total 3 items, price 45
            order.setOrderDetails(details);

            // Calculate total for dummy data if not already done by constructor/setter in OrderDetail
            double calculatedTotal = details.stream().mapToDouble(OrderDetail::getSubTotal).sum();
            // order.setTotalAmount(calculatedTotal); // Ensure consistency if data comes from different sources

            return order;
        } else if (id == 123457) {

            Order order = new Order();
            order.setCity("Thành phố Hồ Chí Minh");
            order.setDistrict("Quận 1");
            order.setStreet("Phường Bến Nghé");
            order.setOderId(123457);
            order.setUserId(1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                order.setOrderDate(LocalDateTime.now().minusDays(5).minusHours(1));
            }
            order.setTotalAmount(75.50);
            order.setDiscountedPrice(0.00);
            order.setStatus("Shipped");
            order.setPaymentMethod((byte) 1); // 1 = Credit Card
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                order.setCreatedAt(LocalDateTime.now().minusDays(5).minusHours(2));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                order.setUpdatedAt(LocalDateTime.now().minusDays(4));
            }
            List<OrderDetail> details = new ArrayList<>();
            details.add(new OrderDetail(4, 123457, 201, "The Hobbit", 1, 30.00));
            details.add(new OrderDetail(5, 123457, 202, "Lord of the Rings", 1, 45.50));
            order.setOrderDetails(details);
            return order;
        }
        return null; // Order not found
    }
}