package com.son.bookhaven.ui.fragments; // Assuming a package for authentication UI

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.son.bookhaven.R;

public class ForgotPasswordFragment extends Fragment {

    private MaterialToolbar toolbar;
    private TextInputEditText textInputEditTextEmail;
    private TextInputLayout textInputLayoutEmail;
    private MaterialButton buttonResetPassword;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        // Initialize views
        toolbar = view.findViewById(R.id.toolbar);
        textInputEditTextEmail = view.findViewById(R.id.textInputEditTextEmail);
        textInputLayoutEmail = view.findViewById(R.id.textInputLayoutEmail);
        buttonResetPassword = view.findViewById(R.id.buttonResetPassword);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up toolbar navigation (e.g., back button)
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed(); // Go back to previous fragment/activity
            }
        });

        buttonResetPassword.setOnClickListener(v -> {
            resetPassword();
        });
    }

    private void resetPassword() {
        // Clear previous error
        textInputLayoutEmail.setError(null);

        String email = textInputEditTextEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            textInputLayoutEmail.setError(getString(R.string.error_email_required));
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayoutEmail.setError(getString(R.string.error_invalid_email));
            return;
        }

        // TODO: Implement actual password reset logic here (e.g., API call to your backend)
        // For demonstration purposes, we'll just show a Snackbar.
        Snackbar.make(requireView(), getString(R.string.password_reset_sent, email), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.dismiss), v -> {
                })
                .show();

        // In a real application, after a successful reset request, you might
        // navigate back to the login screen or show a confirmation screen.
        // For example:
        // if (getActivity() != null) {
        //     getActivity().onBackPressed();
        // }
    }
}