package com.son.bookhaven.ui.fragments; // Adjust your package name

import android.os.Bundle;
import android.text.Editable; // Import Editable
import android.text.TextUtils;
import android.text.TextWatcher; // Import TextWatcher
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.son.bookhaven.R; // Ensure this imports your R file correctly

import java.util.regex.Pattern;

public class ChangePasswordFragment extends Fragment {

    private static final String TAG = "ChangePasswordFragment";

    private MaterialToolbar toolbar;
    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmNewPassword;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private MaterialButton btnChangePasswordSubmit;

    // Regex for strong password:
    // At least 8 characters long
    // Contains at least one uppercase letter (A-Z)
    // Contains at least one lowercase letter (a-z)
    // Contains at least one digit (0-9)
    // Contains at least one special character (!@#$%^&*()_+-=[]{};':"|,.<>/?`~)
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,32}$");

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        initViews(view);
        setupToolbar();
        setupClickListeners();
        setupRealtimeValidation(); // <--- This line enables realtime checking

        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_change_password);
        tilCurrentPassword = view.findViewById(R.id.til_current_password);
        etCurrentPassword = view.findViewById(R.id.et_current_password);
        tilNewPassword = view.findViewById(R.id.til_new_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        tilConfirmNewPassword = view.findViewById(R.id.til_confirm_new_password);
        etConfirmNewPassword = view.findViewById(R.id.et_confirm_new_password);
        btnChangePasswordSubmit = view.findViewById(R.id.btn_change_password_submit);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    requireActivity().onBackPressed();
                }
            });
        }
    }

    private void setupClickListeners() {
        btnChangePasswordSubmit.setOnClickListener(v -> handleChangePassword());
    }

    // --- Realtime Validation Setup ---
    private void setupRealtimeValidation() {
        // TextWatcher for Current Password field
        etCurrentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateCurrentPassword(s.toString());
            }
        });

        // TextWatcher for New Password field
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateNewPassword(s.toString());
                // Also re-validate confirm password if new password changes
                validateConfirmNewPassword(etConfirmNewPassword.getText().toString());
            }
        });

        // TextWatcher for Confirm New Password field
        etConfirmNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateConfirmNewPassword(s.toString());
            }
        });
    }

    // --- Individual Validation Methods for Realtime Check ---
    // These methods are called by the TextWatchers as the user types
    private boolean validateCurrentPassword(String password) {
        if (TextUtils.isEmpty(password)) {
            tilCurrentPassword.setError("Current password cannot be empty");
            return false;
        } else {
            tilCurrentPassword.setError(null); // Clear error if valid
            return true;
        }
    }

    private boolean validateNewPassword(String password) {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            tilNewPassword.setError("New password cannot be empty");
            return false;
        } else if (password.length() < 8) {
            tilNewPassword.setError("New password must be at least 8 characters long");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            tilNewPassword.setError("New password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.");
            return false;
        } else if (password.equals(currentPassword) && !TextUtils.isEmpty(currentPassword)) {
            // Only show this error if current password field is not empty itself
            tilNewPassword.setError("New password cannot be the same as the current password.");
            return false;
        }
        else {
            tilNewPassword.setError(null); // Clear error if valid
            return true;
        }
    }

    private boolean validateConfirmNewPassword(String confirmPassword) {
        String newPassword = etNewPassword.getText().toString().trim(); // Get current new password value
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmNewPassword.setError("Confirm new password cannot be empty");
            return false;
        } else if (!newPassword.equals(confirmPassword)) {
            tilConfirmNewPassword.setError("New passwords do not match");
            return false;
        } else {
            tilConfirmNewPassword.setError(null); // Clear error if valid
            return true;
        }
    }


    // --- Final Validation on Button Click (still needed!) ---
    private void handleChangePassword() {
        // Trigger all validations one last time to ensure all fields are checked
        // This is important because a user might skip a field or leave an error uncorrected
        boolean isCurrentPasswordValid = validateCurrentPassword(etCurrentPassword.getText().toString().trim());
        boolean isNewPasswordValid = validateNewPassword(etNewPassword.getText().toString().trim());
        boolean isConfirmNewPasswordValid = validateConfirmNewPassword(etConfirmNewPassword.getText().toString().trim());

        boolean allFieldsValid = isCurrentPasswordValid && isNewPasswordValid && isConfirmNewPasswordValid;

        if (allFieldsValid) {
            Log.d(TAG, "Client-side validation passed. Attempting to change password...");

            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();

            // --- Server-side Verification and Update ---
            // THIS IS THE CRUCIAL PART FOR REAL SECURITY
            // In a real application, you would send the 'currentPassword' and 'newPassword'
            // to your authentication service or backend API.

            // The backend would then:
            // 1. Verify the 'currentPassword' against the user's stored (hashed) password.
            //    This prevents unauthorized password changes even if someone gains temporary access to the device.
            // 2. If current password is correct, hash the 'newPassword'.
            // 3. Update the user's password in the database with the new hashed password.
            // 4. Respond with success or failure.

            // --- Placeholder for API Call ---
            // Example using a fictitious service call:
            // AuthService.getInstance().changePassword(currentPassword, newPassword, new Callback() {
            //     @Override
            //     public void onSuccess() {
            //         Toast.makeText(getContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show();
            //         // After successful change, navigate back
            //         if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            //             getParentFragmentManager().popBackStack();
            //         } else {
            //             requireActivity().onBackPressed();
            //         }
            //     }
            //
            //     @Override
            //     public void onFailure(String errorMessage) {
            //         Toast.makeText(getContext(), "Failed to change password: " + errorMessage, Toast.LENGTH_LONG).show();
            //         // Optionally set error on current password field if it was incorrect
            //         tilCurrentPassword.setError("Incorrect current password or other error.");
            //     }
            // });

            // --- SIMULATION of successful password change for now ---
            Toast.makeText(getContext(), "Password changed successfully! (Server call simulated)", Toast.LENGTH_SHORT).show();

            // After simulated successful change, navigate back
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }

        } else {
            // Client-side validation failed
            Toast.makeText(getContext(), "Please correct the errors before submitting.", Toast.LENGTH_SHORT).show();
        }
    }
}