package com.son.bookhaven.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.son.bookhaven.R;
import com.son.bookhaven.utils.ApiClient;
import com.son.bookhaven.services.AccountApiService;
import com.son.bookhaven.utils.TokenManager;
import com.son.bookhaven.data.dto.request.ChangePasswordRequest;
import com.son.bookhaven.data.dto.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends Fragment {

    private static final String TAG = "ChangePasswordFragment";

    private MaterialToolbar toolbar;
    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmNewPassword;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private MaterialButton btnChangePasswordSubmit;
    private CircularProgressIndicator progressIndicator;

    private TokenManager tokenManager;

    private AccountApiService apiService;

    // Regex for strong password:
    // At least 8 characters long
    // Contains at least one uppercase letter (A-Z)
    // Contains at least one lowercase letter (a-z)
    // Contains at least one digit (0-9)
//    private static final Pattern PASSWORD_PATTERN =
//            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,32}$");

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        initViews(view);
        setupApiService();
        setupToolbar();
        setupClickListeners();
        setupRealtimeValidation();

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
        progressIndicator = view.findViewById(R.id.progress_indicator);

        tokenManager = new TokenManager(requireContext());
    }

    private void setupApiService() {
        apiService = ApiClient.getClient().create(AccountApiService.class);
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

    private boolean validateCurrentPassword(String password) {
        if (TextUtils.isEmpty(password)) {
            tilCurrentPassword.setError("Mật khẩu hiện tại không được để trống");
            return false;
        } else {
            tilCurrentPassword.setError(null);
            return true;
        }
    }

    private boolean validateNewPassword(String password) {
        String currentPassword = etCurrentPassword.getText() != null ?
                etCurrentPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(password)) {
            tilNewPassword.setError("Mật khẩu mới không được để trống");
            return false;
        } else if (password.length() < 6) {
            tilNewPassword.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
            return false;
        } else if (password.equals(currentPassword) && !TextUtils.isEmpty(currentPassword)) {
            tilNewPassword.setError("Mật khẩu mới không thể giống mật khẩu hiện tại");
            return false;
        } else {
            tilNewPassword.setError(null);
            return true;
        }
    }

    private boolean validateConfirmNewPassword(String confirmPassword) {
        String newPassword = etNewPassword.getText().toString().trim();
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmNewPassword.setError("Xác nhận mật khẩu không được để trống");
            return false;
        } else if (!newPassword.equals(confirmPassword)) {
            tilConfirmNewPassword.setError("Mật khẩu xác nhận không khớp");
            return false;
        } else {
            tilConfirmNewPassword.setError(null);
            return true;
        }
    }

    private void handleChangePassword() {
        // Final validation
        boolean isCurrentPasswordValid = validateCurrentPassword(etCurrentPassword.getText().toString().trim());
        boolean isNewPasswordValid = validateNewPassword(etNewPassword.getText().toString().trim());
        boolean isConfirmNewPasswordValid = validateConfirmNewPassword(etConfirmNewPassword.getText().toString().trim());

        boolean allFieldsValid = isCurrentPasswordValid && isNewPasswordValid && isConfirmNewPasswordValid;

        if (!allFieldsValid) {
            showErrorMessage("Vui lòng sửa các lỗi trước khi tiếp tục");
            return;
        }

        // Show loading state
        setLoadingState(true);

        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        // Create request
        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword);

        // Get auth token
        String authToken = tokenManager.getToken();

        // Make API call
        Call<ApiResponse> call = apiService.changePassword("Bearer " + authToken, request);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                setLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    showSuccessMessage(apiResponse.getMessage());

                    // Clear all fields after successful password change
                    etCurrentPassword.setText("");
                    etNewPassword.setText("");
                    etConfirmNewPassword.setText("");

                    // Navigate back after showing success message
                    etCurrentPassword.postDelayed(() -> {
                        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                            getParentFragmentManager().popBackStack();
                        } else {
                            requireActivity().onBackPressed();
                        }
                    }, 2000);

                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                setLoadingState(false);
                Log.e(TAG, "Network error during change password request", t);
                showErrorMessage("Không thể kết nối đến server. Vui lòng kiểm tra kết nối internet và thử lại.");
            }
        });
    }

    private void handleApiError(Response<ApiResponse> response) {
        try {
            String errorMessage = "Có lỗi xảy ra. Vui lòng thử lại.";

            if (response.code() == 400) {
                errorMessage = "Mật khẩu hiện tại không đúng";
                tilCurrentPassword.setError("Mật khẩu hiện tại không đúng");
            } else if (response.code() == 401) {
                errorMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
                // Handle session expiry - navigate to login
                handleSessionExpired();
                return;
            }

            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e(TAG, "API error response: " + errorBody);

            }

            showErrorMessage(errorMessage);

        } catch (Exception e) {
            Log.e(TAG, "Error handling API error response", e);
            showErrorMessage("Có lỗi xảy ra. Vui lòng thử lại.");
        }
    }

    private void handleSessionExpired() {
        // Clear stored auth token
        tokenManager.clearUserData();

        // Navigate to login screen
        // You'll need to implement this based on your navigation structure
        Toast.makeText(requireContext(), "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();

    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            btnChangePasswordSubmit.setEnabled(false);
            btnChangePasswordSubmit.setText("Đang xử lý...");
            progressIndicator.setVisibility(View.VISIBLE);
            etCurrentPassword.setEnabled(false);
            etNewPassword.setEnabled(false);
            etConfirmNewPassword.setEnabled(false);
        } else {
            btnChangePasswordSubmit.setEnabled(true);
            btnChangePasswordSubmit.setText("Đổi mật khẩu");
            progressIndicator.setVisibility(View.GONE);
            etCurrentPassword.setEnabled(true);
            etNewPassword.setEnabled(true);
            etConfirmNewPassword.setEnabled(true);
        }
    }

    private void showSuccessMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            message = "Đổi mật khẩu thành công!";
        }

        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.md_theme_primary, null))
                .setTextColor(getResources().getColor(android.R.color.white, null))
                .show();
    }

    private void showErrorMessage(String message) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.md_theme_error, null))
                .setTextColor(getResources().getColor(android.R.color.white, null))
                .show();
    }
}