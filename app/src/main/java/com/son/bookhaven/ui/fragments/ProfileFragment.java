package com.son.bookhaven.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;
import com.pranathicodes.letteravatar.AvatarCreator;
import com.son.bookhaven.R;
import com.son.bookhaven.apiHelper.AccountApiService;
import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.apiHelper.AuthApiService;
import com.son.bookhaven.authService.TokenManager;
import com.son.bookhaven.data.dto.response.ErrorResponse;
import com.son.bookhaven.data.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private MaterialToolbar toolbar;
    private ShapeableImageView profileImage;
    private TextView profileName, profileEmail;
    private MaterialButton btnEditProfile, btnLogOut, btnSignIn, btnSignUp;
    private LinearLayout layoutOrderHistory, layoutAppSettings, layoutPrivacySecurity, layoutHelpSupport;
    private ConstraintLayout groupLoggedInProfile;
    private LinearLayout layoutAuthButtons;
    private ProgressBar progressBar;

    // API service and token manager
    private AuthApiService authApiService;
    private AccountApiService accountApiService;
    private TokenManager tokenManager;

    // User data
    private User currentUser;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        showBottomNavigation();
    }

    private void showBottomNavigation() {
        if (getActivity() != null) {
            View bottomNavigation = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNavigation != null) {
                bottomNavigation.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        initApiService();
        setupToolbar();
        setupClickListeners();
        checkLoginStatusAndFetchUserData();

        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_cart);
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnLogOut = view.findViewById(R.id.btn_log_out);

        layoutOrderHistory = view.findViewById(R.id.layout_order_history);
        layoutAppSettings = view.findViewById(R.id.layout_app_settings);
        layoutPrivacySecurity = view.findViewById(R.id.layout_privacy_security);
        layoutHelpSupport = view.findViewById(R.id.layout_help_support);

        groupLoggedInProfile = view.findViewById(R.id.group_logged_in_profile);
        layoutAuthButtons = view.findViewById(R.id.layout_auth_buttons);
        btnSignIn = view.findViewById(R.id.btn_sign_in);
        btnSignUp = view.findViewById(R.id.btn_sign_up);

        // Progress bar (add this to your layout if not present)
        progressBar = view.findViewById(R.id.progress_bar);

        // Initialize TokenManager
        tokenManager = new TokenManager(requireContext());
    }

    private void initApiService() {
        authApiService = ApiClient.getClient().create(AuthApiService.class);
        accountApiService = ApiClient.getAuthenticatedClient(requireContext()).create(AccountApiService.class);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            // Toolbar setup if needed
        }
    }

    private void checkLoginStatusAndFetchUserData() {
        if (tokenManager.isLoggedIn()) {
            // User is logged in, fetch user data from API
            showLoadingState();
            fetchUserInfo();
        } else {
            // No token, show login/signup options
            showLoggedOutState();
        }
    }

    private void fetchUserInfo() {
        String token = tokenManager.getToken();
        if (token == null) {
            showLoggedOutState();
            return;
        }

        Call<User> call = accountApiService.getMyInfo();

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                hideLoadingState();

                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    // Update TokenManager with fresh user data
                    tokenManager.updateUserData(currentUser);
                    showLoggedInState();
                    Log.d(TAG, "User info fetched successfully");
                } else if (response.code() == 401) {
                    // Token is invalid/expired
                    handleTokenExpired();
                } else {
                    // Handle other errors
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                hideLoadingState();
                Log.e(TAG, "Failed to fetch user info", t);

                // Show cached user data if available
                if (tokenManager.isLoggedIn()) {
                    showLoggedInStateFromCache();
                    showToast("Using cached data. Please check your connection.");
                } else {
                    showLoggedOutState();
                    showToast("Network error. Please check your connection.");
                }
            }
        });
    }

    private void handleTokenExpired() {
        tokenManager.clearUserData();
        currentUser = null;
        showLoggedOutState();
        showToast("Session expired. Please login again.");
        Log.d(TAG, "Token expired, user logged out");
    }

    private void handleApiError(Response<User> response) {
        String errorMessage = "Failed to load user information";
        if (response.errorBody() != null) {
            try {
                String errorBody = response.errorBody().string();
                ErrorResponse errorResponse = new Gson().fromJson(errorBody, ErrorResponse.class);
                errorMessage = errorResponse.getMessage();
            } catch (Exception e) {
                Log.e(TAG, "Error parsing error response", e);
            }
        }

        // Show cached data if available
        if (tokenManager.isLoggedIn()) {
            showLoggedInStateFromCache();
            showToast("Using cached data. " + errorMessage);
        } else {
            showLoggedOutState();
            showToast(errorMessage);
        }
    }

    private void showLoadingState() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        groupLoggedInProfile.setVisibility(View.GONE);
        layoutAuthButtons.setVisibility(View.GONE);
        btnLogOut.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showLoggedInState() {
        groupLoggedInProfile.setVisibility(View.VISIBLE);
        btnLogOut.setVisibility(View.VISIBLE);
        layoutAuthButtons.setVisibility(View.GONE);

        if (currentUser != null) {
            profileName.setText(currentUser.getFullName());
            profileEmail.setText(currentUser.getEmail());

            // Generate avatar with user's name
            generateAvatar(currentUser.getFullName());
        }

        // Enable logged-in specific options
        enableLoggedInFeatures(true);
    }

    private void showLoggedInStateFromCache() {
        groupLoggedInProfile.setVisibility(View.VISIBLE);
        btnLogOut.setVisibility(View.VISIBLE);
        layoutAuthButtons.setVisibility(View.GONE);

        // Use cached data from TokenManager
        String fullName = tokenManager.getFullName();
        profileName.setText(fullName);
        profileEmail.setText(""); // Email not stored in cache

        // Generate avatar with cached user's name
        generateAvatar(fullName);

        // Enable logged-in specific options
        enableLoggedInFeatures(true);
    }

    private void showLoggedOutState() {
        groupLoggedInProfile.setVisibility(View.GONE);
        btnLogOut.setVisibility(View.GONE);
        layoutAuthButtons.setVisibility(View.VISIBLE);

        // Disable options that require login
        enableLoggedInFeatures(false);

        Log.d(TAG, "User not logged in. Showing auth buttons.");
    }

    private void generateAvatar(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            // Fallback to default avatar if no name is available
            profileImage.setImageResource(R.drawable.ic_default_avatar);
            return;
        }

        try {
            // Extract first letter of the name
            char firstLetter = fullName.charAt(0);

            // Create avatar with the first letter
            AvatarCreator avatarCreator = new AvatarCreator(requireContext());
            profileImage.setImageBitmap(avatarCreator
                    .setLetter(firstLetter)
                    .setTextSize(40)
                    .setAvatarSize(200)
                    .build());
        } catch (Exception e) {
            Log.e(TAG, "Error generating avatar", e);
            // Fallback to default avatar on error
            profileImage.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    private void enableLoggedInFeatures(boolean enabled) {
        layoutOrderHistory.setEnabled(enabled);
        layoutOrderHistory.setClickable(enabled);
        layoutOrderHistory.setAlpha(enabled ? 1.0f : 0.5f);

        btnEditProfile.setEnabled(enabled);
        btnEditProfile.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void setupClickListeners() {
        // Click listeners for logged-in state
        btnEditProfile.setOnClickListener(v -> {
            if (tokenManager.isLoggedIn()) {
                navigateToEditProfile();
            } else {
                showToast("Please login first");
            }
        });

        btnLogOut.setOnClickListener(v -> handleLogout());

        // Click listeners for not-logged-in state
        btnSignIn.setOnClickListener(v -> navigateToSignIn());
        btnSignUp.setOnClickListener(v -> navigateToSignUp());

        // General settings/support options
        layoutOrderHistory.setOnClickListener(v -> {
            if (tokenManager.isLoggedIn()) {
                navigateToOrderHistory();
            } else {
                showToast("Please login to view order history");
            }
        });

        layoutAppSettings.setOnClickListener(v -> navigateToAppSettings());
        layoutPrivacySecurity.setOnClickListener(v ->
                navigateToFragment(new PrivacySecurityFragment()));

        layoutHelpSupport.setOnClickListener(v ->
                navigateToFragment(new HelpSupportFragment()));

    }

    private void navigateToFragment(Fragment fragment) {
        if (getActivity() != null && isAdded()) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .commitAllowingStateLoss();
        }
    }


    // Navigation methods
    private void navigateToAppSettings() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        AppSettingsFragment appSettingsFragment = new AppSettingsFragment();
        fragmentTransaction.replace(R.id.frame_layout, appSettingsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void navigateToOrderHistory() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        OrderHistoryFragment orderHistoryFragment = new OrderHistoryFragment();
        fragmentTransaction.replace(R.id.frame_layout, orderHistoryFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void navigateToEditProfile() {
        if (currentUser != null) {
            EditProfileFragment editProfileFragment = EditProfileFragment.newInstance(currentUser);
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, editProfileFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            showToast("User data not available");
        }
    }

    private void navigateToSignIn() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LoginFragment loginFragment = new LoginFragment();
        fragmentTransaction.replace(R.id.frame_layout, loginFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void navigateToSignUp() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SignUpFragment registerFragment = new SignUpFragment();
        fragmentTransaction.replace(R.id.frame_layout, registerFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void handleLogout() {
        // Clear user session/token
        tokenManager.clearUserData();
        currentUser = null;

        // Update UI after logout
        showLoggedOutState();
        if (getActivity() != null && isAdded()) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new LoginFragment())
                    .commitAllowingStateLoss();
        }
        showToast("Logged out successfully.");
        Log.d(TAG, "User logged out successfully");
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Public method to refresh user data (can be called from other fragments after login)
    public void refreshUserData() {
        checkLoginStatusAndFetchUserData();
    }
}