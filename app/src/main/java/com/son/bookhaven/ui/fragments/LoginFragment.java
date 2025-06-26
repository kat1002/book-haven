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
import com.son.bookhaven.R; // Ensure this imports your R file correctly

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private TextInputLayout tilEmailUsername;
    private TextInputEditText etEmailUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword;
    private MaterialButton btnSignUp;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        initViews(view);
        setupClickListeners();

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
            Log.d(TAG, "Attempting login with: " + emailOrUsername);
            // In a real app, you would send this to your authentication service
            // (e.g., Firebase Auth, your backend API)

            // Simulate a successful login
            Toast.makeText(getContext(), "Login successful for " + emailOrUsername, Toast.LENGTH_SHORT).show();

            // After successful login, you would typically navigate to the main screen
            // or perform a user session setup.
            // Example:
            // Intent intent = new Intent(requireActivity(), MainActivity.class);
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            // startActivity(intent);
            // requireActivity().finish();

            // For fragment-based navigation, just pop back or replace with main fragment
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack(); // Go back to previous fragment
            } else {
                // If this is the root, replace with your main app content fragment
                // Example:
                // getParentFragmentManager().beginTransaction()
                //     .replace(R.id.fragment_container, new HomeFragment()) // Replace with your main fragment
                //     .commit();
            }

        } else {
            Toast.makeText(getContext(), "Please correct the errors.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleForgotPassword() {
        Toast.makeText(getContext(), "Forgot Password clicked! (Navigate to password reset screen)", Toast.LENGTH_LONG).show();
        // Example: Navigate to a ForgotPasswordFragment
        // FragmentManager fragmentManager = getParentFragmentManager();
        // fragmentManager.beginTransaction()
        //     .replace(R.id.fragment_container, new ForgotPasswordFragment())
        //     .addToBackStack(null)
        //     .commit();
    }

    private void handleSignUp() {
        Toast.makeText(getContext(), "Sign Up clicked! (Navigate to registration screen)", Toast.LENGTH_LONG).show();
        // Example: Navigate to a RegisterFragment
        // FragmentManager fragmentManager = getParentFragmentManager();
        // fragmentManager.beginTransaction()
        //     .replace(R.id.fragment_container, new RegisterFragment())
        //     .addToBackStack(null)
        //     .commit();
    }
}