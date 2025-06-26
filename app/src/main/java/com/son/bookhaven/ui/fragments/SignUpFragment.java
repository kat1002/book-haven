package com.son.bookhaven.ui.fragments; // Adjust your package name

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        initViews(view);
        setupClickListeners();

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
            Log.d(TAG, "Attempting sign up for: " + email);
            // In a real app, you would send this to your registration service
            // (e.g., Firebase Auth, your backend API)

            // Simulate a successful registration
            Toast.makeText(getContext(), "Registration successful! Welcome, " + fullName, Toast.LENGTH_LONG).show();

            // After successful sign up, you might automatically log them in
            // and navigate to the main screen, or navigate to the login screen.
            navigateToLogin(); // Go back to login screen
        } else {
            Toast.makeText(getContext(), "Please correct the errors and try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToLogin() {
        FragmentManager fragmentManager = getParentFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(); // Go back to the previous fragment (LoginFragment)
        } else {
            // If SignUpFragment is the root, replace it with LoginFragment
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, new LoginFragment())
                    .commit();
        }
    }
}