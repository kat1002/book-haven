package com.son.bookhaven;

import android.view.MenuItem;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import com.son.bookhaven.ui.fragments.HomeFragment;
import com.son.bookhaven.ui.fragments.ExploreFragment;
import com.son.bookhaven.ui.fragments.CartFragment;
import com.son.bookhaven.ui.fragments.ProfileFragment;
import com.son.bookhaven.ui.fragments.SignUpFragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }

    private void replaceFragment(Fragment fragment) {
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

    public void updateCartBadge(int itemCount) {
        cartItemCount = itemCount;

        if (cartBadge != null) {
            if (itemCount > 0) {
                cartBadge.setNumber(itemCount);
                cartBadge.setVisible(true);
            } else {
                cartBadge.setVisible(false);
            }
        }
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