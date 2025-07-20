package com.son.bookhaven.ui.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.son.bookhaven.apiHelper.AccountApiService;
import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.apiHelper.AuthApiService;
import com.son.bookhaven.authService.TokenManager;
import com.son.bookhaven.data.dto.ApiResponse;
import com.son.bookhaven.data.dto.request.ForgotPasswordRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
// Import your API service/repository classes here
// import com.son.bookhaven.api.AuthRepository;

public class ForgotPasswordFragment extends Fragment {

    private MaterialToolbar toolbar;
    private TextInputEditText textInputEditTextEmail;
    private TextInputLayout textInputLayoutEmail;
    private MaterialButton buttonResetPassword;

    private AccountApiService accountApiService;


    private boolean isRequestInProgress = false;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        // Initialize views
        toolbar = view.findViewById(R.id.toolbar);
        textInputEditTextEmail = view.findViewById(R.id.textInputEditTextEmail);
        textInputLayoutEmail = view.findViewById(R.id.textInputLayoutEmail);
        buttonResetPassword = view.findViewById(R.id.buttonResetPassword);

        accountApiService = ApiClient.getClient().create(AccountApiService.class);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null && !isRequestInProgress) {
                getActivity().onBackPressed();
            }
        });

        buttonResetPassword.setOnClickListener(v -> {
            if (!isRequestInProgress) {
                resetPassword();
            }
        });

        // Add text watcher to clear errors when user types
        textInputEditTextEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                textInputLayoutEmail.setError(null);
            }
        });
    }

    private void resetPassword() {
        textInputLayoutEmail.setError(null);

        String email = textInputEditTextEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            textInputLayoutEmail.setError(getString(R.string.error_email_required));
            textInputEditTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayoutEmail.setError(getString(R.string.error_invalid_email));
            textInputEditTextEmail.requestFocus();
            return;
        }

        setLoadingState(true);

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        Call<ApiResponse> call = accountApiService.forgotPassword(request);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                setLoadingState(false);
                if (response.isSuccessful() && response.body() != null) {
                    showSuccessMessage(response.body().getMessage());

                    // Optional: Clear email field
                    textInputEditTextEmail.setText("");

                    // Optional: Navigate back
                    textInputEditTextEmail.postDelayed(() -> {
                        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                            getParentFragmentManager().popBackStack();
                        } else {
                            requireActivity().onBackPressed();
                        }
                    }, 2000);
                } else {
                    Log.e(TAG,"error");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                setLoadingState(false);
                Log.e(TAG, "Error during forgot password", t);
                showErrorMessage("Không thể kết nối đến server. Vui lòng thử lại sau.");
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        isRequestInProgress = isLoading;
        buttonResetPassword.setEnabled(!isLoading);
        textInputEditTextEmail.setEnabled(!isLoading);

    }

    private void showSuccessMessage(String message) {
        String successMsg = TextUtils.isEmpty(message)
                ? getString(R.string.password_reset_sent)
                : message;

        Snackbar.make(requireView(), successMsg, Snackbar.LENGTH_LONG)
                .setAction(message, v -> {
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                })
                .show();
    }


    private void showErrorMessage(String message) {
        String errorMsg = TextUtils.isEmpty(message) ?
                getString(R.string.error_email_required) : message;

        Snackbar.make(requireView(), errorMsg, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.dismiss), v -> {})
                .show();
    }
}