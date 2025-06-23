package com.son.bookhaven.ui.fragments; // Adjust your package name as needed

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.son.bookhaven.R; // Ensure this imports your R file correctly
import com.son.bookhaven.data.model.User; // Assuming you have this User model

// If you're loading profile images from a URL, you'll need Glide or Coil
// import com.bumptech.glide.Glide; // For Glide

public class ProfileFragment extends Fragment {

    private MaterialToolbar toolbar;
    private ShapeableImageView profileImage;
    private TextView profileName, profileEmail;
    private MaterialButton btnEditProfile, btnLogOut;
    private LinearLayout layoutOrderHistory,
            layoutAppSettings, layoutPrivacySecurity, layoutHelpSupport;

    // Dummy User for demonstration. In a real app, you'd fetch this from a ViewModel or repository.
    private User currentUser;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false); // Make sure this matches your XML file name

        initViews(view);
        setupToolbar();
        populateProfileData(); // Populate user details
        setupClickListeners(); // Set up click handlers for all interactive elements

        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_cart); // Note: Your toolbar ID is toolbar_cart
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnLogOut = view.findViewById(R.id.btn_log_out);

        layoutOrderHistory = view.findViewById(R.id.layout_order_history);
        layoutAppSettings = view.findViewById(R.id.layout_app_settings);
        layoutPrivacySecurity = view.findViewById(R.id.layout_privacy_security);
        layoutHelpSupport = view.findViewById(R.id.layout_help_support);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            // If you want a back button, uncomment and set navigationIcon
            // toolbar.setNavigationIcon(R.drawable.ic_arrow_back); // Ensure you have this drawable
            // toolbar.setNavigationOnClickListener(v -> {
            //     if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            //         getParentFragmentManager().popBackStack();
            //     } else {
            //         requireActivity().onBackPressed();
            //     }
            // });
        }
    }

    private void populateProfileData() {
        // For demonstration, create a dummy user
        // In a real application, you would fetch the actual user data
        // from your authentication system, a database, or a ViewModel.
        currentUser = new User("Sabrina Aryan", "sabrina.aryan@example.com", "0987654321", "https://example.com/your_profile_image.jpg");
        // Or fetch from a global place:
        // currentUser = UserManager.getInstance().getCurrentUser(); // Example of a singleton user manager

        if (currentUser != null) {
            profileName.setText(currentUser.getFullName());
            profileEmail.setText(currentUser.getEmail());

            // Load profile image (using Glide as an example)
            // if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
            //     Glide.with(this)
            //          .load(currentUser.getProfileImageUrl())
            //          .placeholder(R.drawable.ic_default_avatar) // Show default while loading
            //          .error(R.drawable.ic_default_avatar)     // Show default if loading fails
            //          .into(profileImage);
            // } else {
            //     profileImage.setImageResource(R.drawable.ic_default_avatar);
            // }
            profileImage.setImageResource(R.drawable.ic_default_avatar); // Using placeholder for now
        } else {
            // Handle case where no user is logged in or data not available
            profileName.setText("Guest User");
            profileEmail.setText("Not logged in");
            profileImage.setImageResource(R.drawable.ic_default_avatar);
            btnEditProfile.setVisibility(View.GONE); // Hide edit button for guest
            btnLogOut.setText("Log In"); // Change logout to login
            Log.w("ProfileFragment", "No user data available.");
        }
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> navigateToEditProfile());
        btnLogOut.setOnClickListener(v -> handleLogout());

        layoutOrderHistory.setOnClickListener(v -> navigateToOrderHistory());
        layoutAppSettings.setOnClickListener(v -> navigateToAppSettings());
        layoutPrivacySecurity.setOnClickListener(v -> showToast("Privacy & Security clicked"));
        layoutHelpSupport.setOnClickListener(v -> showToast("Help & Support clicked"));
    }

    private void navigateToAppSettings(){
        // Get the FragmentManager
        FragmentManager fragmentManager = getParentFragmentManager();
        // Start a FragmentTransaction
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Create a new instance of OrderHistoryFragment
        AppSettingsFragment appSettingsFragment = new AppSettingsFragment();

        fragmentTransaction.replace(R.id.frame_layout, appSettingsFragment);

        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }

    private void navigateToOrderHistory(){
            // Get the FragmentManager
            FragmentManager fragmentManager = getParentFragmentManager();
            // Start a FragmentTransaction
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // Create a new instance of OrderHistoryFragment
            OrderHistoryFragment orderHistoryFragment = new OrderHistoryFragment();

            fragmentTransaction.replace(R.id.frame_layout, orderHistoryFragment);

            fragmentTransaction.addToBackStack(null);

            fragmentTransaction.commit();
    }

    private void navigateToEditProfile() {
        if (currentUser != null) {
            // Create an instance of EditProfileFragment
            EditProfileFragment editProfileFragment = EditProfileFragment.newInstance(currentUser);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.replace(R.id.frame_layout, editProfileFragment);

            fragmentTransaction.addToBackStack(null);

            fragmentTransaction.commit();
        } else {
            Toast.makeText(getContext(), "Please log in to edit profile.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogout() {
        Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();
        Log.d("ProfileFragment", "User logged out.");
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        // Here you would implement actual navigation or logic for each setting item
    }
}