package com.example.smarttaskreminderapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager handles user session persistence using SharedPreferences.
 * <p>
 * Stores:
 * - User login status
 * - User ID and name
 * - Dark mode preference
 * - Notification toggle setting
 * <p>
 * Session persists across app restarts until logout.
 */
public class SessionManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    // SharedPreferences file name
    private static final String PREF_NAME = "SmartTaskReminderPrefs";

    // Keys
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notificationsEnabled";

    /**
     * Constructor initializes SharedPreferences.
     *
     * @param context Application context
     */
    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Save user login session.
     *
     * @param userId   User ID
     * @param userName User display name
     */
    public void createLoginSession(int userId, String userName) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    /**
     * Check if user is currently logged in.
     *
     * @return true if session exists, false otherwise
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get logged-in user's ID.
     *
     * @return User ID or -1 if not logged in
     */
    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    /**
     * Get logged-in user's display name.
     *
     * @return User name or empty string if not logged in
     */
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    /**
     * End the current session (logout).
     * Clears all stored session data.
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    /**
     * Update stored user name (after settings change).
     *
     * @param userName New display name
     */
    public void updateUserName(String userName) {
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    // ==================== DARK MODE ====================

    /**
     * Check if dark mode is enabled.
     *
     * @return true if dark mode on, false otherwise
     */
    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }

    /**
     * Toggle dark mode preference.
     *
     * @param isDarkMode true to enable dark mode
     */
    public void setDarkMode(boolean isDarkMode) {
        editor.putBoolean(KEY_DARK_MODE, isDarkMode);
        editor.apply();
    }

    // ==================== NOTIFICATIONS ====================

    /**
     * Check if notifications are globally enabled.
     *
     * @return true if notifications allowed, false otherwise
     */
    public boolean areNotificationsEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    /**
     * Toggle global notifications setting.
     *
     * @param enabled true to enable notifications
     */
    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();
    }
}
