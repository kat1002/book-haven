package com.son.bookhaven.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.son.bookhaven.R; // Ensure this imports your R file correctly
import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.apiHelper.AuthApiService;
import com.son.bookhaven.authService.TokenManager;
import com.son.bookhaven.data.dto.request.LoginRequest;
import com.son.bookhaven.data.dto.response.ErrorResponse;
import com.son.bookhaven.data.dto.response.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private TextInputLayout tilEmailUsername;
    private TextInputEditText etEmailUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword;
    private MaterialButton btnSignUp;

    private AuthApiService authApiService;
    private TokenManager tokenManager;

    public LoginFragment() {
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        initViews(view);
        setupClickListeners();

        // Initialize API service and token manager
        authApiService = ApiClient.getClient().create(AuthApiService.class);
        tokenManager = new TokenManager(requireContext());

        return view;
    }

    private void initViews(View view) {
        tilEmailUsername = view.findViewById(R.id.til_email_username);
        etEmailUsername = view.findViewById(R.id.et_email_username);
        tilPassword = view.findViewById(R.id.til_password);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        tvForgotPassword = view.findViewById(R.id.tv_forgot_password);
        btnSignUp = view.findViewById(R.id.btn_sign_up);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
        btnSignUp.setOnClickListener(v -> handleSignUp());
    }

    private void handleLogin() {
        // Clear previous errors
        tilEmailUsername.setError(null);
        tilPassword.setError(null);

        String emailOrUsername = etEmailUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(emailOrUsername)) {
            tilEmailUsername.setError("Email or Username cannot be empty");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password cannot be empty");
            isValid = false;
        }

        if (isValid) {
            // Disable login button to prevent multiple requests
            btnLogin.setEnabled(false);
            btnLogin.setText("Logging in...");

            // Create login request
            LoginRequest loginRequest = new LoginRequest(emailOrUsername, password);

            // Make API call
            Call<LoginResponse> call = authApiService.login(loginRequest);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    // Re-enable login button
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");

                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse loginResponse = response.body();

                        // Save token and user data
                        tokenManager.saveUserData(loginResponse.getToken(), loginResponse.getUser());

                        Toast.makeText(getContext(),
                                "Welcome back, " + loginResponse.getUser().getFullName() + "!",
                                Toast.LENGTH_SHORT).show();

                        Log.d(TAG, "Login successful for user: " + loginResponse.getUser().getFullName());

                        // Navigate to main screen
                        navigateToMainScreen();

                    } else {
                        // Handle error response
                        String errorMessage = "Login failed";
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                ErrorResponse errorResponse = new Gson().fromJson(errorBody, ErrorResponse.class);
                                errorMessage = errorResponse.getMessage();
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error response", e);
                            }
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    // Re-enable login button
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");

                    Log.e(TAG, "Login API call failed", t);
                    Toast.makeText(getContext(),
                            "Network error. Please check your connection.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Please correct the errors.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMainScreen() {
        if (getActivity() != null && isAdded()) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new ProfileFragment())
                    .commitAllowingStateLoss();
        }
    }

    private void handleForgotPassword() {
        Toast.makeText(getContext(), "Forgot Password clicked!", Toast.LENGTH_SHORT).show();
        // Navigate to forgot password fragment
    }

    private void handleSignUp() {
        Toast.makeText(getContext(), "Sign Up clicked!", Toast.LENGTH_SHORT).show();
        if (getActivity() != null && isAdded()) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new SignUpFragment())
                    .commitAllowingStateLoss();
        }
    }
}