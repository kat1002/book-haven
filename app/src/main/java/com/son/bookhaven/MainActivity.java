package com.son.bookhaven;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MenuItem;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import com.google.android.material.snackbar.Snackbar;
import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.apiHelper.CartApiService;
import com.son.bookhaven.data.dto.response.CartItemResponse;
import com.son.bookhaven.ui.fragments.HomeFragment;
import com.son.bookhaven.ui.fragments.ExploreFragment;
import com.son.bookhaven.ui.fragments.CartFragment;
import com.son.bookhaven.ui.fragments.OrderConfirmationFragment;
import com.son.bookhaven.ui.fragments.OrderHistoryFragment;
import com.son.bookhaven.ui.fragments.ProfileFragment;
import com.son.bookhaven.ui.fragments.SignUpFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private MaterialToolbar toolbar;
    private final HomeFragment homeFragment = new HomeFragment();
    private final ExploreFragment exploreFragment = new ExploreFragment();
    private final CartFragment cartFragment = new CartFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();
    private final SignUpFragment signUpFragment = new SignUpFragment();
    private BadgeDrawable cartBadge;
    private int cartItemCount = 3; // Updated to match your cart layout (3 items)

    private long lastCartFetchTime = 0;
    private static final long CART_FETCH_COOLDOWN = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "onCreate called with intent: " + getIntent());
        boolean hasPaymentCompleted = getIntent().getBooleanExtra("payment_completed", false);
        Log.d("MainActivity", "payment_completed: " + hasPaymentCompleted);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set default fragment
        replaceFragment(signUpFragment);

        setupCartBadge();

        // Update badge to show current cart count
        updateCartBadge(cartItemCount);

        // Set up bottom navigation listener
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    replaceFragment(homeFragment);
                    updateToolbarTitle("BookHaven");
                    return true;
                } else if (itemId == R.id.nav_explore) {
                    replaceFragment(exploreFragment);
                    updateToolbarTitle("Explore");
                    return true;
                } else if (itemId == R.id.nav_cart) {
                    replaceFragment(cartFragment);
                    updateToolbarTitle("Shopping Cart");
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    replaceFragment(profileFragment);
                    updateToolbarTitle("Profile");
                    return true;
                }
                return false;
            }
        });

        if (getIntent().getBooleanExtra("payment_completed", false)) {
            boolean paymentSuccess = getIntent().getBooleanExtra("payment_success", false);
            if (paymentSuccess) {
                // Clear cart
                clearCart();

                // Navigate to OrderHistoryFragment
                replaceFragment(new OrderHistoryFragment());

                // Show success message
                View rootView = findViewById(android.R.id.content);
                Snackbar.make(rootView, "Payment completed successfully", Snackbar.LENGTH_LONG).show();
            } else {
                // Show failure message
                // Handle payment failure
                View rootView = findViewById(android.R.id.content);
                Snackbar.make(rootView, "Payment was not completed", Snackbar.LENGTH_LONG).show();
            }
        }
        if (hasPaymentCompleted) {
            boolean paymentSuccess = getIntent().getBooleanExtra("payment_success", false);
            Log.d("MainActivity", "Payment success: " + paymentSuccess);

            // Clear cart
            clearCart();

            // Always navigate to OrderHistoryFragment regardless of success/failure
            replaceFragment(new OrderHistoryFragment());

            // Show appropriate message
            View rootView = findViewById(android.R.id.content);
            Snackbar.make(rootView,
                    paymentSuccess ? "Payment completed successfully" : "Payment was not completed",
                    Snackbar.LENGTH_LONG).show();
        }
        if (hasPaymentCompleted) {
            boolean paymentSuccess = getIntent().getBooleanExtra("payment_success", false);
            Log.d("MainActivity", "Payment success: " + paymentSuccess);

            // Clear cart
            clearCart();

            // Navigate to OrderConfirmationFragment instead of OrderHistoryFragment
            OrderConfirmationFragment confirmationFragment = new OrderConfirmationFragment();

            // Pass all payment data to the fragment
            Bundle args = new Bundle();
            args.putInt("order_id", getIntent().getIntExtra("order_id", 0));
            args.putString("payment_code", getIntent().getStringExtra("payment_code"));
            args.putInt("payment_method", getIntent().getIntExtra("payment_method", OrderConfirmationFragment.PAYMENT_PAYOS));
            args.putBoolean("is_payment_completed", paymentSuccess);
            args.putString("order_date", new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN")).format(new Date()));
            args.putDouble("total_amount", getIntent().getDoubleExtra("total_amount", 0.0));

            // Add any address info if available
            args.putString("recipient_name", getIntent().getStringExtra("recipient_name"));
            args.putString("phone_number", getIntent().getStringExtra("phone_number"));
            args.putString("city", getIntent().getStringExtra("city"));
            args.putString("district", getIntent().getStringExtra("district"));
            args.putString("ward", getIntent().getStringExtra("ward"));
            args.putString("street", getIntent().getStringExtra("street"));

            confirmationFragment.setArguments(args);

            replaceFragment(confirmationFragment);

            // Set the correct tab selected in bottom navigation
            //bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            updateToolbarTitle("Order Confirmation");
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void updateToolbarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    private void setupCartBadge() {
        // Create badge for cart item
        cartBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_cart);

        // Configure badge appearance
        if (cartBadge != null) {
            // Use standard colors for better visibility
            cartBadge.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            cartBadge.setBadgeTextColor(ContextCompat.getColor(this, android.R.color.white));

            // Set badge position (optional)
            cartBadge.setHorizontalOffset(8);
            cartBadge.setVerticalOffset(8);

            // Set max character count for large numbers
            cartBadge.setMaxCharacterCount(3);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Update cart badge when returning to the app
        updateCartBadge(0); // 0 will trigger API fetch

        // Check if there was a payment in progress that we need to handle
        SharedPreferences prefs = getSharedPreferences("payment_prefs", Context.MODE_PRIVATE);
        boolean paymentInProgress = prefs.getBoolean("payment_in_progress", false);

        if (paymentInProgress) {
            // Clear the flag
            prefs.edit().putBoolean("payment_in_progress", false).apply();

            // Show dialog to check payment status
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Payment Status")
                    .setMessage("Did you complete your payment?")
                    .setPositiveButton("Yes, payment completed", (dialog, which) -> {
                        // Clear cart
                        clearCart();

                        // Navigate to OrderHistoryFragment
                        replaceFragment(new OrderHistoryFragment());
                    })
                    .setNegativeButton("No, payment cancelled", (dialog, which) -> {
                        // Do nothing, stay on current screen
                        dialog.dismiss();
                    })
                    .setCancelable(false)
                    .show();
        }
    }
    public void updateCartBadge(int initialCount) {
        long currentTime = System.currentTimeMillis();
        if (initialCount > 0 && (currentTime - lastCartFetchTime < CART_FETCH_COOLDOWN)) {
            updateBadgeVisibility(initialCount);
            return;
        }
        // Use this context instead of requireContext() which is for fragments
        CartApiService apiService = ApiClient.getAuthenticatedClient(this).create(CartApiService.class);
        Call<List<CartItemResponse>> call = apiService.getCart();
        lastCartFetchTime = currentTime;
        call.enqueue(new Callback<List<CartItemResponse>>() {
            @Override
            public void onResponse(Call<List<CartItemResponse>> call, Response<List<CartItemResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CartItemResponse> cartItems = response.body();
                    int itemCount = cartItems.size();
                    Log.d("LoadCartItems", "Loaded " + itemCount + " items from API.");

                    // Update the badge with the actual count from the API
                    if (cartBadge != null) {
                        if (itemCount > 0) {
                            cartBadge.setNumber(itemCount);
                            cartBadge.setVisible(true);
                        } else {
                            cartBadge.setVisible(false);
                        }
                    }

                    // Update the local count variable
                    cartItemCount = itemCount;
                } else {
                    Log.e("LoadCartItems", "Failed to load cart. Response code: " + response.code());
                    // Fallback to the provided count
                    updateBadgeVisibility(initialCount);
                }
            }

            @Override
            public void onFailure(Call<List<CartItemResponse>> call, Throwable t) {
                Log.e("LoadCartItems", "Error: " + t.getMessage());
                // Fallback to the provided count on error
                updateBadgeVisibility(initialCount);
            }
        });
    }
    private void updateBadgeVisibility(int count) {
        if (cartBadge != null) {
            if (count > 0) {
                cartBadge.setNumber(count);
                cartBadge.setVisible(true);
            } else {
                cartBadge.setVisible(false);
            }
        }
        cartItemCount = count;
    }
    // Call this method when items are added to cart
    public void addToCart() {
        cartItemCount++;
        updateCartBadge(cartItemCount);
    }

    // Call this method when items are removed from cart
    public void removeFromCart() {
        if (cartItemCount > 0) {
            cartItemCount--;
            updateCartBadge(cartItemCount);
        }
    }

    // Call this to clear cart
    public void clearCart() {
        cartItemCount = 0;
        updateCartBadge(cartItemCount);
    }
}