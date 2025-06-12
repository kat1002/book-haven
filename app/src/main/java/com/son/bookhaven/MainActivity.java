package com.son.bookhaven;

import android.view.MenuItem;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment = new HomeFragment();
    private ExploreFragment exploreFragment = new ExploreFragment();
    private CartFragment cartFragment = new CartFragment();
    private ProfileFragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set default fragment
        replaceFragment(homeFragment);

        // Set up bottom navigation listener
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    replaceFragment(homeFragment);
                    return true;
                } else if (itemId == R.id.nav_explore) {
                    replaceFragment(exploreFragment);
                    return true;
                } else if (itemId == R.id.nav_cart) {
                    replaceFragment(cartFragment);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    replaceFragment(profileFragment);
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
        fragmentTransaction.commit();
    }
}