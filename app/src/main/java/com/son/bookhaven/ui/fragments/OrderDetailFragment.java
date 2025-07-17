package com.son.bookhaven.ui.fragments;

import android.os.Build;
import android.os.Bundle;
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
import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.apiHelper.OrderService;
import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.OrderResponse;
import com.son.bookhaven.data.dto.OrderDetailResponse;
import com.son.bookhaven.data.adapters.OrderDetailItemAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private OrderService orderService;

    // Order Summary
    private TextView tvDetailOrderId, tvDetailOrderDate, tvDetailStatus,
            tvDetailPaymentMethod, tvDetailVoucherCode, tvDetailNote;

    // Delivery Information
    private TextView tvDetailRecipientName, tvDetailRecipientPhone, tvDetailDeliveryAddress;

    // Order Items RecyclerView
    private RecyclerView rvOrderItems;
    private OrderDetailItemAdapter orderDetailItemAdapter;
    private List<OrderDetailResponse> orderDetailsList;
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
        initializeApiService();
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

    private void initializeApiService() {
        orderService = ApiClient.getClient().create(OrderService.class);
    }

    private void loadOrderDetail(int id) {
        showLoadingState();

        Call<ApiResponse<OrderResponse>> call = orderService.getOrderById(id);
        call.enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<OrderResponse>> call, 
                                 @NonNull Response<ApiResponse<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OrderResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        OrderResponse orderResponse = apiResponse.getData();
                        displayOrderDetails(orderResponse);
                        showContentState();
                        Log.d(TAG, "Order details loaded for ID: " + id);
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Failed to load order details.";
                        showErrorState(errorMessage);
                        Log.e(TAG, "API Error: " + errorMessage);
                    }
                } else {
                    showErrorState("Failed to load order details. Please try again.");
                    Log.e(TAG, "HTTP Error: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<OrderResponse>> call, @NonNull Throwable t) {
                showErrorState("Network error. Please check your connection and try again.");
                Log.e(TAG, "Network Error: ", t);
            }
        });
    }

    private void displayOrderDetails(OrderResponse order) {
        // Order Summary
        tvDetailOrderId.setText(getString(R.string.order_id_format, order.getOrderId()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tvDetailOrderDate.setText(getString(R.string.order_date_format,
                    formatDateTime(LocalDateTime.parse(order.getOrderDate()))));
        }
        tvDetailStatus.setText(getString(R.string.order_status_format, order.getStatus()));
        tvDetailPaymentMethod.setText(getString(R.string.payment_method_format, getPaymentMethodString((byte) order.getPaymentMethod())));

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
        // Build full delivery address from components
        String fullAddress = buildDeliveryAddress(order.getStreet(), order.getWard(), order.getDistrict(), order.getCity());
        
        if (order.getRecipientName() != null && !order.getRecipientName().isEmpty()) {
            tvDetailRecipientName.setText(getString(R.string.recipient_name_format, order.getRecipientName()));
        } else {
            tvDetailRecipientName.setText(getString(R.string.recipient_name_format, "N/A"));
        }
        
        if (order.getPhoneNumber() != null && !order.getPhoneNumber().isEmpty()) {
            tvDetailRecipientPhone.setText(getString(R.string.recipient_phone_format, order.getPhoneNumber()));
        } else {
            tvDetailRecipientPhone.setText(getString(R.string.recipient_phone_format, "N/A"));
        }
        
        if (fullAddress != null && !fullAddress.isEmpty()) {
            tvDetailDeliveryAddress.setText(getString(R.string.delivery_address_format, fullAddress));
        } else {
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

    private String buildDeliveryAddress(String street, String ward, String district, String city) {
        StringBuilder address = new StringBuilder();
        
        if (street != null && !street.trim().isEmpty()) {
            address.append(street.trim());
        }
        
        if (ward != null && !ward.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(ward.trim());
        }
        
        if (district != null && !district.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(district.trim());
        }
        
        if (city != null && !city.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(city.trim());
        }
        
        return address.length() > 0 ? address.toString() : null;
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

    
}