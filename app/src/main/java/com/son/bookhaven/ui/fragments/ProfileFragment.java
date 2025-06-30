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
import androidx.constraintlayout.widget.ConstraintLayout; // Import ConstraintLayout
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
    private MaterialButton btnEditProfile, btnLogOut, btnSignIn, btnSignUp; // Added btnSignIn, btnSignUp
    private LinearLayout layoutOrderHistory, layoutAppSettings, layoutPrivacySecurity, layoutHelpSupport;
    private ConstraintLayout groupLoggedInProfile; // Added reference to the logged-in profile group
    private LinearLayout layoutAuthButtons; // Added reference to the auth buttons layout

    // Dummy User for demonstration. In a real app, you'd fetch this from a ViewModel or repository.
    private User currentUser; // This will determine if the user is logged in or not

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false); // Make sure this matches your XML file name

        initViews(view);
        setupToolbar();
        checkLoginStatusAndPopulateUI(); // Checks login status and populates UI accordingly
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

        groupLoggedInProfile = view.findViewById(R.id.group_logged_in_profile); // Initialize
        layoutAuthButtons = view.findViewById(R.id.layout_auth_buttons);       // Initialize
        btnSignIn = view.findViewById(R.id.btn_sign_in);                       // Initialize
        btnSignUp = view.findViewById(R.id.btn_sign_up);                       // Initialize
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

    private void checkLoginStatusAndPopulateUI() {
        // This is where you would get the actual login status from your auth system
        // For demonstration:
        // Set currentUser to null for logged out state, or to a User object for logged in state
        // For testing logged-out state:
        // currentUser = null;
        // For testing logged-in state:
        currentUser = new User("Sabrina Aryan", "sabrina.aryan@example.com", "0987654321", "https://picsum.photos/96"); // Dummy URL

        if (currentUser != null) {
            // User is logged in
            groupLoggedInProfile.setVisibility(View.VISIBLE);
            btnLogOut.setVisibility(View.VISIBLE);
            layoutAuthButtons.setVisibility(View.GONE);

            profileName.setText(currentUser.getFullName());
            profileEmail.setText(currentUser.getEmail());

            // Load profile image (using Glide as an example, otherwise use a default drawable)
            // if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
            //     Glide.with(this)
            //          .load(currentUser.getProfileImageUrl())
            //          .placeholder(R.drawable.ic_default_avatar)
            //          .error(R.drawable.ic_default_avatar)
            //          .into(profileImage);
            // } else {
            //     profileImage.setImageResource(R.drawable.ic_default_avatar);
            // }
            profileImage.setImageResource(R.drawable.ic_default_avatar); // Placeholder for now
            // If you have a real image URL, you would load it here.
            // Example for dynamic URL placeholder if you don't use Glide/Picasso and need a quick test:
            // if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
            //     // Note: Direct URL loading needs a library like Glide/Picasso for production
            //     // This is just a conceptual placeholder
            //     profileImage.setImageDrawable(null); // Clear previous image
            //     // You'd need an async task or library to load from URL properly
            // } else {
            //     profileImage.setImageResource(R.drawable.ic_default_avatar);
            // }


            // Enable/disable logged-in specific options
            layoutOrderHistory.setEnabled(true);
            layoutOrderHistory.setClickable(true);
            // ... other logged-in specific options
        } else {
            // User is NOT logged in
            groupLoggedInProfile.setVisibility(View.GONE);
            btnLogOut.setVisibility(View.GONE); // No logout button if not logged in
            layoutAuthButtons.setVisibility(View.VISIBLE);

            // You might want to disable options that require login
            layoutOrderHistory.setEnabled(false);
            layoutOrderHistory.setClickable(false);
            // ... other options
            Log.d("ProfileFragment", "User not logged in. Showing auth buttons.");
        }
    }

    private void setupClickListeners() {
        // Click listeners for logged-in state (if visible)
        btnEditProfile.setOnClickListener(v -> {
            if (currentUser != null) {
                navigateToEditProfile();
            } else {
                Toast.makeText(getContext(), R.string.not_logged_in_message, Toast.LENGTH_SHORT).show();
            }
        });
        btnLogOut.setOnClickListener(v -> handleLogout());

        // Click listeners for not-logged-in state (if visible)
        btnSignIn.setOnClickListener(v -> navigateToSignIn());
        btnSignUp.setOnClickListener(v -> navigateToSignUp());


        // General settings/support options (might be available even if not logged in,
        // or their logic might adapt if clicked when not logged in)
        layoutOrderHistory.setOnClickListener(v -> {
            if (currentUser != null) {
                navigateToOrderHistory();
            } else {
                Toast.makeText(getContext(), R.string.not_logged_in_message, Toast.LENGTH_SHORT).show();
            }
        });
        layoutAppSettings.setOnClickListener(v -> navigateToAppSettings());
        layoutPrivacySecurity.setOnClickListener(v -> showToast("Privacy & Security clicked"));
        layoutHelpSupport.setOnClickListener(v -> showToast("Help & Support clicked"));
    }

    private void navigateToAppSettings(){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        AppSettingsFragment appSettingsFragment = new AppSettingsFragment();
        fragmentTransaction.replace(R.id.frame_layout, appSettingsFragment); // Replace with your actual fragment container ID
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void navigateToOrderHistory(){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        OrderHistoryFragment orderHistoryFragment = new OrderHistoryFragment();
        fragmentTransaction.replace(R.id.frame_layout, orderHistoryFragment); // Replace with your actual fragment container ID
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void navigateToEditProfile() {
        EditProfileFragment editProfileFragment = EditProfileFragment.newInstance(currentUser);
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, editProfileFragment); // Replace with your actual fragment container ID
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void navigateToSignIn() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LoginFragment loginFragment = new LoginFragment(); // Assuming you have a LoginFragment
        fragmentTransaction.replace(R.id.frame_layout, loginFragment); // Replace with your actual fragment container ID
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        Toast.makeText(getContext(), "Navigate to Sign In", Toast.LENGTH_SHORT).show();
    }

    private void navigateToSignUp() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SignUpFragment registerFragment = new SignUpFragment(); // Assuming you have a RegisterFragment
        fragmentTransaction.replace(R.id.frame_layout, registerFragment); // Replace with your actual fragment container ID
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        Toast.makeText(getContext(), "Navigate to Sign Up", Toast.LENGTH_SHORT).show();
    }

    private void handleLogout() {
        // In a real app, you would clear user session/token here
        currentUser = null; // Clear current user for demonstration
        checkLoginStatusAndPopulateUI(); // Update UI after logout
        Toast.makeText(getContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show();
        Log.d("ProfileFragment", "User logged out.");
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}