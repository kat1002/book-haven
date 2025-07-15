package com.son.bookhaven.authService;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.son.bookhaven.data.dto.response.LoginResponse;

public class TokenManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_ROLE = "role";

    private SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            // Fallback to regular SharedPreferences if encryption fails
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public void saveUserData(String token, LoginResponse.User user) {
        sharedPreferences.edit()
                .putString(KEY_TOKEN, token)
                .putInt(KEY_USER_ID, user.getUserId())
                .putString(KEY_FULL_NAME, user.getFullName())
                .putString(KEY_ROLE, user.getRole())
                .apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clearUserData() {
        sharedPreferences.edit().clear().apply();
    }

    public String getFullName() {
        return sharedPreferences.getString(KEY_FULL_NAME, "");
    }

    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, "");
    }

    // Method to update user data from fetched User object
    public void updateUserData(com.son.bookhaven.data.model.User user) {
        sharedPreferences.edit()
                .putInt(KEY_USER_ID, user.getUserId())
                .putString(KEY_FULL_NAME, user.getFullName())
                .apply();
    }
}
