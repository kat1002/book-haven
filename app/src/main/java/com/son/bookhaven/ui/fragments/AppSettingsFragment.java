package com.son.bookhaven.ui.fragments; // Adjust your package name

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.son.bookhaven.R; // Make sure your R file is correctly imported

public class AppSettingsFragment extends Fragment {

    private MaterialToolbar toolbar;
    private MaterialSwitch switchDarkMode;
    private TextView tvVersion;
    private TextView tvPrivacyPolicy;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    public AppSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_settings, container, false);

        initViews(view);
        setupToolbar();
        loadSettings();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_settings);
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        tvVersion = view.findViewById(R.id.tv_version);
        tvPrivacyPolicy = view.findViewById(R.id.tv_privacy_policy);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                // Pop the fragment from the back stack or finish the activity
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    requireActivity().onBackPressed();
                }
            });
        }
    }

    private void loadSettings() {
        boolean darkModeEnabled = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        switchDarkMode.setChecked(darkModeEnabled);
    }

    private void setupListeners() {
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            // Apply dark mode immediately (requires restarting the activity for full effect or re-creating theme)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            // Recreate activity to apply theme change
            // This might not be ideal user experience for a simple toggle.
            // A better approach would be to handle theme changes dynamically.
            // requireActivity().recreate(); // Uncomment if you want immediate full theme change
            Toast.makeText(getContext(), "Dark Mode " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });

        tvPrivacyPolicy.setOnClickListener(v -> {
            // Open Privacy Policy in a browser or new Fragment
            Toast.makeText(getContext(), "Privacy Policy Clicked", Toast.LENGTH_SHORT).show();
            // Example: Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://your.privacy.policy.url"));
            // startActivity(browserIntent);
        });
    }
}