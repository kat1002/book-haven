package com.son.bookhaven.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView; // Make sure to import ImageView for ivEditProfilePicture
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.son.bookhaven.R;
import com.son.bookhaven.data.model.User;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditProfileFragment extends Fragment {

    private static final String ARG_USER = "user_data";
    private static final String TAG = "EditProfileFragment";

    private ShapeableImageView ivProfilePicture;
    private ImageView ivEditProfilePicture;
    private FrameLayout flProfilePictureContainer;
    private TextInputEditText etFullName, etEmail, etPhoneNumber;
    private TextInputLayout tilFullName, tilEmail, tilPhoneNumber;
    private MaterialButton btnSaveChanges, btnCancel, btnChangePassword;
    private MaterialToolbar toolbar;

    private User currentUser;
    private Uri currentPhotoUri; // To store URI of image captured by camera or selected from gallery

    // Activity Result Launchers
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> pickImageFromGalleryLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;

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

        // Initialize ActivityResultLaunchers
        setupActivityResultLaunchers();
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
        flProfilePictureContainer = view.findViewById(R.id.fl_profile_picture_container);
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        ivEditProfilePicture = view.findViewById(R.id.iv_edit_profile_picture);
        tilFullName = view.findViewById(R.id.til_full_name);
        etFullName = view.findViewById(R.id.et_full_name);
        tilEmail = view.findViewById(R.id.til_email);
        etEmail = view.findViewById(R.id.et_email);
        tilPhoneNumber = view.findViewById(R.id.til_phone_number);
        etPhoneNumber = view.findViewById(R.id.et_phone_number);
        btnSaveChanges = view.findViewById(R.id.btn_save_changes);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
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

            // Load profile image using Glide if a URL is available
            if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(currentUser.getProfileImageUrl())
                        .placeholder(R.drawable.ic_default_avatar) // Show default while loading
                        .error(R.drawable.ic_default_avatar)     // Show default if loading fails
                        .into(ivProfilePicture);
            } else {
                ivProfilePicture.setImageResource(R.drawable.ic_default_avatar);
            }
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

        // Make both the container and the icon clickable
        flProfilePictureContainer.setOnClickListener(v -> handleChangeProfilePicture());
        ivEditProfilePicture.setOnClickListener(v -> handleChangeProfilePicture());
    }

    private void saveChanges() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPhoneNumber.setError(null);

        String newFullName = etFullName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String newPhoneNumber = etPhoneNumber.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(newFullName)) {
            tilFullName.setError("Full Name cannot be empty");
            isValid = false;
        }

        if (TextUtils.isEmpty(newEmail) || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            tilEmail.setError("Enter a valid email address");
            isValid = false;
        }

        if (TextUtils.isEmpty(newPhoneNumber) || newPhoneNumber.length() < 7) {
            tilPhoneNumber.setError("Enter a valid phone number");
            isValid = false;
        }

        if (isValid) {
            if (currentUser == null) {
                currentUser = new User();
            }
            currentUser.setFullName(newFullName);
            currentUser.setEmail(newEmail);
            currentUser.setPhone(newPhoneNumber);

            // If a new photo was picked/taken, you'd update its URI/URL here
            if (currentPhotoUri != null) {
                // In a real app, you would upload this URI to your server
                // and then set the returned URL to currentUser.setProfileImageUrl()
                currentUser.setProfileImageUrl(currentPhotoUri.toString()); // For now, just save the URI
                Log.d(TAG, "New profile image URI: " + currentPhotoUri.toString());
            }

            Toast.makeText(getContext(), "Profile changes saved!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Saved Profile: " + currentUser.getFullName() + ", " + currentUser.getEmail());

            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }

        } else {
            Toast.makeText(getContext(), "Please correct the errors.", Toast.LENGTH_SHORT).show();
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

    // --- Image Picker/Camera Logic ---

    private void setupActivityResultLaunchers() {
        // Launcher for requesting permissions
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Log.d(TAG, "Permission Granted, retrying operation.");
                // Depending on which permission was just granted, try to launch the respective intent
                // This requires more sophisticated handling if you request multiple permissions
                // For simplicity, we'll re-check within the picker logic.
            } else {
                Toast.makeText(getContext(), "Permission denied. Cannot perform action.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Permission denied.");
            }
        });

        // Launcher for picking image from gallery
        pickImageFromGalleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    currentPhotoUri = imageUri;
                    Glide.with(this).load(currentPhotoUri).into(ivProfilePicture);
                    Log.d(TAG, "Image selected from gallery: " + currentPhotoUri.toString());
                }
            } else {
                Log.d(TAG, "Image selection cancelled or failed.");
            }
        });

        // Launcher for taking a picture with the camera
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success) {
                if (currentPhotoUri != null) {
                    Glide.with(this).load(currentPhotoUri).into(ivProfilePicture);
                    Log.d(TAG, "Image captured by camera: " + currentPhotoUri.toString());
                }
            } else {
                Log.d(TAG, "Picture capture cancelled or failed.");
                currentPhotoUri = null; // Clear URI if capture failed
            }
        });
    }

    private void handleChangeProfilePicture() {
        // Show a dialog to choose between gallery and camera
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Change Profile Picture")
                .setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) { // Take Photo
                        dispatchTakePictureIntent();
                    } else { // Choose from Gallery
                        dispatchPickImageFromGalleryIntent();
                    }
                })
                .show();
    }

    private void dispatchTakePictureIntent() {
        if (getContext() == null) return;

        // Determine which permission to check based on Android version
        String cameraPermission = Manifest.permission.CAMERA;

        if (ContextCompat.checkSelfPermission(getContext(), cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, proceed with camera intent
            launchCameraIntent();
        } else {
            // Request camera permission
            requestPermissionLauncher.launch(cameraPermission);
        }
    }

    private void launchCameraIntent() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getApplicationContext().getPackageName() + ".fileprovider", // Must match authority in manifest
                        photoFile
                );
                takePictureLauncher.launch(currentPhotoUri);
            }
        } catch (IOException ex) {
            Log.e(TAG, "Error creating image file: " + ex.getMessage(), ex);
            Toast.makeText(getContext(), "Error creating file for photo.", Toast.LENGTH_SHORT).show();
            currentPhotoUri = null;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            Toast.makeText(getContext(), "Cannot access external storage.", Toast.LENGTH_SHORT).show();
            return null;
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    private void dispatchPickImageFromGalleryIntent() {
        if (getContext() == null) return;

        // Determine which permission to check based on Android version
        String readPermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readPermission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            readPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }


        if (ContextCompat.checkSelfPermission(getContext(), readPermission) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, proceed with gallery intent
            launchGalleryIntent();
        } else {
            // Request gallery permission
            requestPermissionLauncher.launch(readPermission);
        }
    }

    private void launchGalleryIntent() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK);
        pickPhotoIntent.setType("image/*");
        pickImageFromGalleryLauncher.launch(pickPhotoIntent);
    }
}