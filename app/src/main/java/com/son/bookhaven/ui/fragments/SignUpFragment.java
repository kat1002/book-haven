package com.son.bookhaven.ui.fragments; // Adjust your package name

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.son.bookhaven.R;
import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.apiHelper.AuthApiService;
import com.son.bookhaven.data.dto.request.RegisterRequest;
import com.son.bookhaven.data.dto.response.RegisterResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpFragment extends Fragment {

    private static final String TAG = "SignUpFragment";

    private TextInputLayout tilFullName;
    private TextInputEditText etFullName;
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnSignUp;
    private TextView tvLoginLink;
    private ProgressBar progressBar; // Add progress bar for loading state

    private AuthApiService apiService;

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        hideBottomNavigation();
    }

    @Override
    public void onPause() {
        super.onPause();
        showBottomNavigation();
    }

    private void hideBottomNavigation() {
        if (getActivity() != null) {
            View bottomNavigation = getActivity().findViewById(R.id.bottom_navigation); // Use your actual ID
            if (bottomNavigation != null) {
                bottomNavigation.setVisibility(View.GONE);
            }
        }
    }

    private void showBottomNavigation() {
        if (getActivity() != null) {
            View bottomNavigation = getActivity().findViewById(R.id.bottom_navigation); // Use your actual ID
            if (bottomNavigation != null) {
                bottomNavigation.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        initViews(view);
        setupClickListeners();
        initApiService();

        return view;
    }

    private void initViews(View view) {
        tilFullName = view.findViewById(R.id.til_full_name);
        etFullName = view.findViewById(R.id.et_full_name);
        tilEmail = view.findViewById(R.id.til_email);
        etEmail = view.findViewById(R.id.et_email);
        tilPassword = view.findViewById(R.id.til_password);
        etPassword = view.findViewById(R.id.et_password);
        tilConfirmPassword = view.findViewById(R.id.til_confirm_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnSignUp = view.findViewById(R.id.btn_sign_up);
        tvLoginLink = view.findViewById(R.id.tv_login_link);
        progressBar = view.findViewById(R.id.progress_bar); // Add this to your layout
    }

    private void initApiService() {
        apiService = ApiClient.getClient().create(AuthApiService.class);
    }

    private void setupClickListeners() {
        btnSignUp.setOnClickListener(v -> handleSignUp());
        tvLoginLink.setOnClickListener(v -> navigateToLogin());
    }

    private void handleSignUp() {
        // Clear previous errors
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full Name cannot be empty");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email cannot be empty");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email address");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password cannot be empty");
            isValid = false;
        } else if (password.length() < 6) { // Example: Minimum password length
            tilPassword.setError("Password must be at least 6 characters long");
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Confirm Password cannot be empty");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        if (isValid) {
            performSignUp(fullName, email, password);
        } else {
            Toast.makeText(getContext(), "Please correct the errors and try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void performSignUp(String fullName, String email, String password) {
        // Show loading state
        setLoadingState(true);

        // Create register request
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName(fullName);
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setRoleID(2); // Default role ID
        registerRequest.setReturnUrl(""); // Return URL is null

        Log.d(TAG, "Attempting sign up for: " + email);

        // Make API call
        Call<RegisterResponse> call = apiService.registerMobile(registerRequest);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                setLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    Toast.makeText(getContext(),
                            "Registration successful! Welcome, " + fullName,
                            Toast.LENGTH_LONG).show();

                    Log.d(TAG, "Registration successful: " + registerResponse.getMessage());

                    // Navigate to login screen after successful registration
                    navigateToLogin();
                } else {
                    // Handle error response
                    String errorMessage = "Registration failed. Please try again.";

                    if (response.errorBody() != null) {
                        try {
                            // Parse error response
                            String errorResponse = response.errorBody().string();
                            // You might want to parse this JSON to get the actual error message
                            Log.e(TAG, "Registration error: " + errorResponse);

                            // If your API returns a specific error message format, parse it here
                            // For now, we'll use a generic message
                            errorMessage = "Registration failed. Please check your details and try again.";
                        } catch (IOException e) {
                            Log.e(TAG, "Error parsing error response", e);
                        }
                    }

                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                setLoadingState(false);
                Log.e(TAG, "Registration API call failed", t);

                String errorMessage = "Network error. Please check your internet connection and try again.";
                if (t instanceof java.net.ConnectException) {
                    errorMessage = "Unable to connect to server. Please try again later.";
                }

                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        btnSignUp.setEnabled(!isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        if (isLoading) {
            btnSignUp.setText("Signing Up...");
        } else {
            btnSignUp.setText("Sign Up");
        }
    }

    private void navigateToLogin() {
//        FragmentManager fragmentManager = getParentFragmentManager();
//        if (fragmentManager.getBackStackEntryCount() > 0) {
//            fragmentManager.popBackStack(); // Go back to the previous fragment (LoginFragment)
//        } else {
//            // If SignUpFragment is the root, replace it with LoginFragment
//            fragmentManager.beginTransaction()
//                    .replace(R.id.frame_layout, new LoginFragment())
//                    .commit();
//        }
        if (getActivity() != null && isAdded()) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new LoginFragment())
                    .commitAllowingStateLoss();
        }
    }
}