package com.son.bookhaven;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.son.bookhaven.ui.fragments.OrderHistoryFragment;

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
                Log.d("Log", "URL loading: " + url);

                // Check for success indicators in PayOS redirect URLs
                if (url.contains("success=true") || url.contains("status=success") ||
                        url.contains("return_url") || url.contains("callback")) {
                    Log.d("Log", "Payment successful: " + url);
                    handlePaymentCompletion(true);
                    return true;
                }
                // Check for failure/cancel indicators
                else if (url.contains("cancel=true") || url.contains("status=cancel") ||
                        url.contains("failure") || url.contains("error")) {
                    Log.d("Log", "Payment failed or canceled: " + url);
                    handlePaymentCompletion(false);
                    return true;
                }
                // If it's trying to use our custom scheme
                else if (url.startsWith("bookhaven://")) {
                    Log.d("Log", "Custom URL scheme detected: " + url);
                    // Extract success/failure from the URL if possible
                    boolean isSuccess = url.contains("success");
                    handlePaymentCompletion(isSuccess);
                    return true;
                }

                // Let WebView handle other URLs
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

    private void handlePaymentCompletion(boolean success) {
        Log.d("PaymentActivity", "Payment completed with success=" + success);

        // Clear payment in progress flag
        SharedPreferences prefs = getSharedPreferences("payment_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("payment_in_progress", false);
        editor.apply();

        // Return to main activity with payment result
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("payment_completed", true);
        intent.putExtra("payment_success", success);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


}