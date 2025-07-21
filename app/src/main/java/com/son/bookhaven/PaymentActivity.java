package com.son.bookhaven;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.apiHelper.OrderService;
import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.OrderResponse;
import com.son.bookhaven.ui.fragments.OrderConfirmationFragment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private String paymentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize views
        webView = findViewById(R.id.payment_webview);
        progressBar = findViewById(R.id.progress_bar);
        toolbar = findViewById(R.id.toolbar);

        // Set up toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Payment");

        toolbar.setNavigationOnClickListener(v -> {
            // Show confirmation dialog before allowing user to cancel payment
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Cancel Payment")
                    .setMessage("Are you sure you want to cancel this payment?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        finish(); // Close activity and return to previous screen
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

        // Get payment URL from intent
        paymentUrl = getIntent().getStringExtra("payment_url");
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            finish();
            return;
        }

        // Configure WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.d("PaymentActivity", "URL loading: " + url);

                // Handle custom bookhaven URL scheme
                if (url.startsWith("bookhaven://payment/")) {
                    Uri uri = Uri.parse(url);
                    List<String> pathSegments = uri.getPathSegments();
                    Log.d("PaymentActivity", "Full URI: " + uri.toString());
                    Log.d("PaymentActivity", "Path segments: " + pathSegments.toString());
                    // Extract order ID from path segment (bookhaven://payment/cancel/12)
                    int orderId = 0;
                    if (pathSegments.size() >= 2) {
                        try {
                            orderId = Integer.parseInt(pathSegments.get(1));
                            Log.d("PaymentActivity", "Extracted orderId from path: " + orderId);
                        } catch (NumberFormatException e) {
                            Log.e("PaymentActivity", "Invalid orderId in URL path", e);
                        }
                    }

                    // Determine payment status
                    boolean isSuccess = url.contains("/success/");
                    boolean isCancelled = url.contains("/cancel/");

                    // Extract payment code from PayOS parameters
                    String paymentCode = uri.getQueryParameter("orderCode");
                    Log.d("PaymentActivity", "PayOS payment code: " + paymentCode);

                    // Get status from PayOS parameters
                    String status = uri.getQueryParameter("status");
                    Log.d("PaymentActivity", "PayOS status: " + status);

                    if (orderId > 0) {
                        fetchOrderDetailsAndComplete(orderId, paymentCode, isSuccess);
                    } else {
                        handlePaymentCompletion(isSuccess, 0, paymentCode, 0);
                    }
                    return true;
                }

                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });

        // Load payment URL
        webView.loadUrl(paymentUrl);
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog before allowing user to go back
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancel Payment")
                .setMessage("Are you sure you want to cancel this payment?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void fetchOrderDetailsAndComplete(int orderId, String paymentCode, boolean isSuccess) {
        Log.d("PaymentActivity", "Fetching order details for orderId: " + orderId);
        progressBar.setVisibility(View.VISIBLE);

        // Create API service
        OrderService orderService = ApiClient.getAuthenticatedClient(this).create(OrderService.class);

        // Call API to get order details
        orderService.getOrderById(orderId).enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<OrderResponse>> call, Response<ApiResponse<OrderResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    OrderResponse orderDetails = response.body().getData();

                    if (orderDetails != null) {
                        // Process with complete order details - access public fields directly
                        handlePaymentCompletion(
                                isSuccess,
                                orderDetails.orderId,
                                paymentCode,
                                orderDetails.discountedPrice,
                                orderDetails.recipientName,
                                orderDetails.phoneNumber,
                                orderDetails.city,
                                orderDetails.district,
                                orderDetails.ward,
                                orderDetails.street
                        );
                    } else {
                        // Fallback if order details are null
                        handlePaymentCompletion(isSuccess, orderId, paymentCode, 0);
                    }
                } else {
                    // API error fallback
                    Log.e("PaymentActivity", "Failed to get order details: " +
                            (response.body() != null ? response.body().getMessage() : "Unknown error"));
                    handlePaymentCompletion(isSuccess, orderId, paymentCode, 0);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OrderResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("PaymentActivity", "Network error fetching order details", t);
                // Fallback to basic info
                handlePaymentCompletion(isSuccess, orderId, paymentCode, 0);
            }
        });
    }

    private void handlePaymentCompletion(boolean success, int orderId, String paymentCode, double totalAmount) {
        handlePaymentCompletion(success, orderId, paymentCode, totalAmount, "", "", "", "", "", "");
    }

    private void handlePaymentCompletion(boolean success, int orderId, String paymentCode, double totalAmount,
                                         String recipientName, String phoneNumber, String city,
                                         String district, String ward, String street) {
        Log.d("PaymentActivity", "Payment completed with success=" + success + ", orderId=" + orderId);

        // Clear payment in progress flag
        SharedPreferences prefs = getSharedPreferences("payment_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("payment_in_progress", false);
        editor.apply();

        // Return to main activity with payment result and order info
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("payment_completed", true);
        intent.putExtra("payment_success", success);
        intent.putExtra("order_id", orderId);
        intent.putExtra("payment_code", paymentCode);
        intent.putExtra("payment_method", OrderConfirmationFragment.PAYMENT_PAYOS);
        intent.putExtra("is_payment_completed", success);
        intent.putExtra("total_amount", totalAmount);

        // Add address info if available
        if (recipientName != null && !recipientName.isEmpty()) {
            intent.putExtra("recipient_name", recipientName);
            intent.putExtra("phone_number", phoneNumber);
            intent.putExtra("city", city);
            intent.putExtra("district", district);
            intent.putExtra("ward", ward);
            intent.putExtra("street", street);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


}