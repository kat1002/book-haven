package com.son.bookhaven.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.pranathicodes.letteravatar.AvatarCreator;
import com.son.bookhaven.R;
import com.son.bookhaven.apiHelper.AccountApiService;
import com.son.bookhaven.apiHelper.ApiClient;
import com.son.bookhaven.authService.TokenManager;
import com.son.bookhaven.data.dto.request.UserInfoUpdateRequest;
import com.son.bookhaven.data.dto.response.ErrorResponse;
import com.son.bookhaven.data.dto.response.UpdateInfoResponse;
import com.son.bookhaven.data.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends Fragment {

    private static final String ARG_USER = "user_data";
    private static final String TAG = "EditProfileFragment";

    private ShapeableImageView ivProfilePicture;
    private TextInputEditText etFullName, etEmail, etPhoneNumber;
    private TextInputLayout tilFullName, tilEmail, tilPhoneNumber;
    private MaterialButton btnSaveChanges, btnCancel, btnChangePassword;
    private MaterialToolbar toolbar;

    private User currentUser;
    private AccountApiService accountApiService;
    private TokenManager tokenManager;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    public static EditProfileFragment newInstance(User user) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUser = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        initViews(view);
        setupToolbar();
        populateProfileData();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_edit_profile);
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        tilFullName = view.findViewById(R.id.til_full_name);
        etFullName = view.findViewById(R.id.et_full_name);
        tilEmail = view.findViewById(R.id.til_email);
        etEmail = view.findViewById(R.id.et_email);
        tilPhoneNumber = view.findViewById(R.id.til_phone_number);
        etPhoneNumber = view.findViewById(R.id.et_phone_number);
        btnSaveChanges = view.findViewById(R.id.btn_save_changes);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnChangePassword = view.findViewById(R.id.btn_change_password);

        // Initialize API service and TokenManager
        accountApiService = ApiClient.getAuthenticatedClient(requireContext()).create(AccountApiService.class);
        tokenManager = new TokenManager(requireContext());
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

    private void populateProfileData() {
        if (currentUser != null) {
            etFullName.setText(currentUser.getFullName());
            etEmail.setText(currentUser.getEmail());
            etPhoneNumber.setText(currentUser.getPhone());

            // Make email field read-only since it's not editable via API
            etEmail.setEnabled(false);
            etEmail.setFocusable(false);

            // Generate avatar with user's name
            generateAvatar(currentUser.getFullName());
        } else {
            Log.w(TAG, "No user data provided to EditProfileFragment. Fields will be empty.");
            Toast.makeText(getContext(), "User data not loaded.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        btnSaveChanges.setOnClickListener(v -> saveChanges());
        btnCancel.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });
        btnChangePassword.setOnClickListener(v -> handleChangePassword());

        // Profile picture editing removed - avatar is auto-generated from name
    }

    private void saveChanges() {
        // Clear previous errors
        tilFullName.setError(null);
        tilPhoneNumber.setError(null);

        String newFullName = etFullName.getText().toString().trim();
        String newPhoneNumber = etPhoneNumber.getText().toString().trim();

        // Validate inputs
        if (!isValidInput(newFullName, newPhoneNumber)) {
            return;
        }

        // Check if there are actual changes
        if (!hasChanges(newFullName, newPhoneNumber)) {
            Toast.makeText(getContext(), "No changes to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading and call API
        showLoading(true);
        updateUserInfo(newFullName, newPhoneNumber);
    }

    private boolean isValidInput(String fullName, String phoneNumber) {
        boolean isValid = true;

        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full Name cannot be empty");
            isValid = false;
        } else if (fullName.length() < 2) {
            tilFullName.setError("Full Name must be at least 2 characters");
            isValid = false;
        } else if (fullName.length() > 50) {
            tilFullName.setError("Full Name cannot exceed 50 characters");
            isValid = false;
        } else if (!fullName.matches("^[a-zA-Z\\s]+$")) {
            tilFullName.setError("Full Name can only contain letters and spaces");
            isValid = false;
        }

        // Validate phone number
        if (TextUtils.isEmpty(phoneNumber)) {
            tilPhoneNumber.setError("Phone number cannot be empty");
            isValid = false;
        } else if (phoneNumber.length() < 7) {
            tilPhoneNumber.setError("Phone number must be at least 7 digits");
            isValid = false;
        } else if (phoneNumber.length() > 15) {
            tilPhoneNumber.setError("Phone number cannot exceed 15 digits");
            isValid = false;
        } else if (!phoneNumber.matches("^[+]?[0-9\\s\\-()]+$")) {
            tilPhoneNumber.setError("Enter a valid phone number");
            isValid = false;
        }

        return isValid;
    }

    private boolean hasChanges(String newFullName, String newPhoneNumber) {
        if (currentUser == null) {
            return true; // Treat as changes if no current user data
        }

        String currentFullName = currentUser.getFullName() != null ? currentUser.getFullName().trim() : "";
        String currentPhone = currentUser.getPhone() != null ? currentUser.getPhone().trim() : "";

        return !newFullName.equals(currentFullName) || !newPhoneNumber.equals(currentPhone);
    }

    private void updateUserInfo(String fullName, String phoneNumber) {
        UserInfoUpdateRequest request = new UserInfoUpdateRequest(fullName, phoneNumber);

        Call<UpdateInfoResponse> call = accountApiService.updateUserInfo(request);
        call.enqueue(new Callback<UpdateInfoResponse>() {
            @Override
            public void onResponse(Call<UpdateInfoResponse> call, Response<UpdateInfoResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Update was successful
                    handleUpdateSuccess(fullName, phoneNumber, response.body().getMessage());
                } else {
                    // Handle error response
                    handleUpdateError(response);
                }
            }

            @Override
            public void onFailure(Call<UpdateInfoResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Failed to update user info", t);
                Toast.makeText(getContext(), "Network error. Please check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleUpdateSuccess(String newFullName, String newPhoneNumber, String message) {
        // Update local user data
        if (currentUser != null) {
            currentUser.setFullName(newFullName);
            currentUser.setPhone(newPhoneNumber);
        }

        // Update TokenManager with new user data
        tokenManager.updateUserData(currentUser);

        // Regenerate avatar if name has changed
        generateAvatar(newFullName);

        // Show success message
        Toast.makeText(getContext(), message != null ? message : "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Profile updated successfully: " + newFullName + ", " + newPhoneNumber);

        // Navigate back
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else {
            requireActivity().onBackPressed();
        }
    }

    private void handleUpdateError(Response<UpdateInfoResponse> response) {
        String errorMessage = "Failed to update profile";

        if (response.errorBody() != null) {
            try {
                String errorBody = response.errorBody().string();
                if (response.code() == 400) {
                    // Bad request - likely the API returned "Failed to update user information"
                    errorMessage = errorBody.replace("\"", ""); // Remove quotes if present
                } else {
                    ErrorResponse errorResponse = new Gson().fromJson(errorBody, ErrorResponse.class);
                    if (errorResponse != null && errorResponse.getMessage() != null) {
                        errorMessage = errorResponse.getMessage();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing error response", e);
            }
        }

        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Update failed: " + response.code() + " - " + errorMessage);
    }

    private void showLoading(boolean isLoading) {
        btnSaveChanges.setEnabled(!isLoading);
        btnCancel.setEnabled(!isLoading);
        btnChangePassword.setEnabled(!isLoading);

        if (isLoading) {
            btnSaveChanges.setText("Updating...");
        } else {
            btnSaveChanges.setText("Save Changes");
        }
    }

    private void handleChangePassword() {
        // Navigate to ChangePasswordFragment
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment(); // No arguments needed for this basic version

        fragmentTransaction.replace(R.id.frame_layout, changePasswordFragment); // R.id.fragment_container is your host FrameLayout/FragmentContainerView
        fragmentTransaction.addToBackStack(null); // Add to back stack to allow back navigation
        fragmentTransaction.commit();
    }

    private void generateAvatar(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            // Fallback to default avatar if no name is available
            ivProfilePicture.setImageResource(R.drawable.ic_default_avatar);
            return;
        }

        try {
            // Extract first letter of the name
            char firstLetter = fullName.charAt(0);

            // Create avatar with the first letter
            AvatarCreator avatarCreator = new AvatarCreator(requireContext());
            ivProfilePicture.setImageBitmap(avatarCreator
                    .setLetter(firstLetter)
                    .setTextSize(40)
                    .setAvatarSize(200)
                    .build());
        } catch (Exception e) {
            Log.e(TAG, "Error generating avatar", e);
            // Fallback to default avatar on error
            ivProfilePicture.setImageResource(R.drawable.ic_default_avatar);
        }
    }

}