package com.son.bookhaven.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.son.bookhaven.MainActivity;
import com.son.bookhaven.R;
import com.son.bookhaven.utils.ApiClient;
import com.son.bookhaven.services.OrderService;
import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.request.UpdateOrderRequest;
import com.son.bookhaven.data.dto.response.PaymentLinkInformation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderConfirmationFragment extends Fragment {

    private static final String TAG = "OrderConfirmationFrag";
    private static final int STATUS_PendingPayment = 0;
    private static final int STATUS_Delivering = 1;
    private static final int STATUS_SDelivered = 2;
    private static final int STATUS_CANCELLED = 3;
    public static final int PAYMENT_PAYOS = 0;
    public static final int PAYMENT_COD = 1;

    // View bindings
    private MaterialToolbar toolbar;
    private ImageView imageViewStatus;
    private MaterialTextView textViewStatusTitle;
    private MaterialTextView textViewStatusMessage;
    private MaterialTextView textViewOrderId;
    private MaterialTextView textViewOrderDate;
    private MaterialTextView textViewPaymentMethod;
    private MaterialTextView textViewPaymentStatus;
    private MaterialTextView textViewTotalAmount;
    private MaterialButton buttonViewOrderHistory;
    private MaterialButton buttonContinueShopping;
    private View loadingOverlay;

    // Order data
    private int orderId;
    private String paymentCode;
    private int paymentMethod;
    private boolean isPaymentCompleted;
    private String orderDate;
    private double totalAmount;
    private String recipientName;
    private String phoneNumber;
    private String city;
    private String district;
    private String ward;
    private String street;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_confirmation, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        toolbar = view.findViewById(R.id.toolbar);
        imageViewStatus = view.findViewById(R.id.imageViewStatus);
        textViewStatusTitle = view.findViewById(R.id.textViewStatusTitle);
        textViewStatusMessage = view.findViewById(R.id.textViewStatusMessage);
        textViewOrderId = view.findViewById(R.id.textViewOrderId);
        textViewOrderDate = view.findViewById(R.id.textViewOrderDate);
        textViewPaymentMethod = view.findViewById(R.id.textViewPaymentMethod);
        textViewPaymentStatus = view.findViewById(R.id.textViewPaymentStatus);
        textViewTotalAmount = view.findViewById(R.id.textViewTotalAmount);
        buttonViewOrderHistory = view.findViewById(R.id.buttonViewOrderHistory);
        buttonContinueShopping = view.findViewById(R.id.buttonContinueShopping);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        // Get arguments
        if (getArguments() != null) {
            orderId = getArguments().getInt("order_id", 0);
            paymentCode = getArguments().getString("payment_code", "");
            paymentMethod = getArguments().getInt("payment_method", PAYMENT_COD);
            isPaymentCompleted = getArguments().getBoolean("is_payment_completed", false);
            orderDate = getArguments().getString("order_date", getCurrentDate());
            totalAmount = getArguments().getDouble("total_amount", 0.0);

            // Address info for order update if needed
            recipientName = getArguments().getString("recipient_name", "");
            phoneNumber = getArguments().getString("phone_number", "");
            city = getArguments().getString("city", "");
            district = getArguments().getString("district", "");
            ward = getArguments().getString("ward", "");
            street = getArguments().getString("street", "");
        }

        setupUI();
        setupListeners();

        // If using PayOS, check payment status
        if (paymentMethod == PAYMENT_PAYOS && !paymentCode.isEmpty()) {
            checkPaymentStatus(paymentCode);
        }
    }

    private void setupUI() {
        // Set up toolbar
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                // Navigate to home instead of going back to payment
                ((MainActivity) getActivity()).replaceFragment(new HomeFragment());
            }
        });

        // Update order details
        textViewOrderId.setText("#" + orderId);
        textViewOrderDate.setText(orderDate);
        textViewPaymentMethod.setText(paymentMethod == PAYMENT_PAYOS ? "PayOS Online" : "Cash on Delivery");
        textViewTotalAmount.setText(String.format(new Locale("vi", "VN"), "%,d đ", (int) totalAmount));

        // Set initial UI based on payment method and status
        if (paymentMethod == PAYMENT_PAYOS) {
            if (isPaymentCompleted) {
                showSuccessState();
            } else {
                showCancelState();
            }
        } else {
            // COD order
            showCodOrderState();
        }
    }

    private void setupListeners() {
        MainActivity mainActivity = (MainActivity) getActivity();
        buttonViewOrderHistory.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {

                mainActivity.replaceFragment(new OrderHistoryFragment());

                // Cast to BottomNavigationView before calling getMenu()
                ((BottomNavigationView) mainActivity.findViewById(R.id.bottom_navigation))
                        .getMenu().findItem(R.id.nav_profile).setChecked(true);
            }
        });

        buttonContinueShopping.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                mainActivity.replaceFragment(new HomeFragment());

                ((BottomNavigationView) mainActivity.findViewById(R.id.bottom_navigation))
                        .getMenu().findItem(R.id.nav_home).setChecked(true);
            }
        });
    }

    private void checkPaymentStatus(String paymentCode) {
        showLoading(true);

        OrderService orderService = ApiClient.getAuthenticatedClient(requireContext()).create(OrderService.class);
        orderService.checkPaymentStatus(paymentCode).enqueue(new Callback<ApiResponse<PaymentLinkInformation>>() {
            @Override
            public void onResponse(Call<ApiResponse<PaymentLinkInformation>> call, Response<ApiResponse<PaymentLinkInformation>> response) {
                if (!isAdded()) {
                    Log.d(TAG, "Fragment not attached. Skipping UI updates.");
                    return;
                }

                showLoading(false);

                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String isPaid = response.body().getData().getStatus();
                        Log.d(TAG, "Fragment not attached. Skipping UI updates.");


                        if (isPaid.equals("PAID")) {
                            showSuccessState();
                            updateOrderStatus(STATUS_Delivering);
                        } else {
                            showCancelState();
                            updateOrderStatus(STATUS_CANCELLED);
                        }
                    } else {
                        Log.e(TAG, "Payment status check failed not get status : " + (response.body() != null ? response.body().getMessage() : "No response"));
                        showCancelState();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing payment response", e);
                    showCancelState();
                    if (isAdded() && getView() != null) {
                        Snackbar.make(getView(), "Error checking payment status", Snackbar.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PaymentLinkInformation>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Payment status check failed", t);
            }
        });
    }

    private void updateOrderStatus(int status) {


        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setOrderId(orderId);
        request.setStatus(status);
        request.setRecipientName(recipientName);
        request.setPhoneNumber(phoneNumber);
        request.setCity(city);
        request.setDistrict(district);
        request.setWard(ward);
        request.setStreet(street);

        OrderService orderService = ApiClient.getAuthenticatedClient(requireContext()).create(OrderService.class);
        orderService.updateOrderStatus(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {


                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "Order status updated: " + status);
                } else {
                    Log.e(TAG, "Failed to update order status: " + (response.body() != null ? response.body().getMessage() : "Unknown error"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {

                Log.e(TAG, "Order status update failed", t);
            }
        });
    }

    private void showSuccessState() {
        imageViewStatus.setImageResource(R.drawable.ic_success);
        textViewStatusTitle.setText("Order Completed");
        textViewStatusMessage.setText("Thank you for your order. Your payment was successful.");
        textViewPaymentStatus.setText("Completed");
    }

    private void showCancelState() {
        imageViewStatus.setImageResource(R.drawable.ic_error);
        textViewStatusTitle.setText("Payment Cancelled");
        textViewStatusMessage.setText("You have canceled your payment. If this was a mistake, please contact support.");
        textViewPaymentStatus.setText("Cancelled");
    }

    private void showProcessingState() {
        imageViewStatus.setImageResource(R.drawable.ic_pending);
        textViewStatusTitle.setText("Payment Processing");
        textViewStatusMessage.setText("Your payment is being processed. We will update you once it's completed.");
        textViewPaymentStatus.setText("Processing");
    }

    private void showCodOrderState() {
        imageViewStatus.setImageResource(R.drawable.ic_success);
        textViewStatusTitle.setText("Order Placed");
        textViewStatusMessage.setText("Thank you for your order. Payment will be collected upon delivery.");
        textViewPaymentStatus.setText("Pending (COD)");
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null && isAdded()) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
        return sdf.format(new Date());
    }
}