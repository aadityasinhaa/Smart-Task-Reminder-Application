package com.example.smarttaskreminderapplication.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.smarttaskreminderapplication.R;
import com.example.smarttaskreminderapplication.database.DatabaseHelper;
import com.example.smarttaskreminderapplication.utils.SessionManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * SettingsActivity provides user preferences and account controls.
 * <p>
 * Options:
 * 1. Change Display Name: Modal dialog to edit username
 * 2. Notifications Toggle: Enable/disable all task reminder notifications
 * 3. Dark Mode Toggle: Switch between light/dark theme
 * 4. Clear All Tasks: Deletes all tasks for current user (with confirmation)
 * 5. Logout: Ends session and returns to LoginActivity
 *
 * Preferences persist via SharedPreferences through SessionManager.
 */
public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchNotifications, switchDarkMode;
    private Button btnChangeName, btnClearTasks, btnLogout;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        // Initialize views
        switchNotifications = findViewById(R.id.switch_notifications);
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        btnChangeName = findViewById(R.id.btn_change_name);
        btnClearTasks = findViewById(R.id.btn_clear_tasks);
        btnLogout = findViewById(R.id.btn_logout);

        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        // Load saved preferences
        switchNotifications.setChecked(sessionManager.areNotificationsEnabled());
        switchDarkMode.setChecked(sessionManager.isDarkMode());
        applyDarkMode(sessionManager.isDarkMode());

        // Notification toggle listener
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setNotificationsEnabled(isChecked);
            Toast.makeText(this, "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        // Dark mode toggle listener
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setDarkMode(isChecked);
            applyDarkMode(isChecked);
            Toast.makeText(this, "Dark mode " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        // Change name button
        btnChangeName.setOnClickListener(v -> showChangeNameDialog());

        // Clear all tasks button
        btnClearTasks.setOnClickListener(v -> showClearTasksConfirmation());

        // Logout button
        btnLogout.setOnClickListener(v -> {
            // Cancel any pending alarms first
            int userId = sessionManager.getUserId();
            dbHelper.deleteAllTasksForUser(userId);  // Optional: clear tasks on logout
            sessionManager.logout();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            finishAffinity();  // Close all activities
        });
    }

    /**
     * Show AlertDialog to edit display name.
     */
    private void showChangeNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Display Name");

        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_name, null);
        final EditText etNewName = dialogView.findViewById(R.id.et_new_name);
        etNewName.setText(sessionManager.getUserName());
        builder.setView(dialogView);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = etNewName.getText().toString().trim();
            if (!newName.isEmpty()) {
                // Update database
                int userId = sessionManager.getUserId();
                dbHelper.updateUserName(userId, newName);
                // Update session
                sessionManager.updateUserName(newName);
                Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Show confirmation dialog before deleting all tasks.
     */
    private void showClearTasksConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Tasks")
                .setMessage("Are you sure you want to delete all tasks? This cannot be undone.")
                .setPositiveButton("Yes, Delete All", (dialog, which) -> {
                    int userId = sessionManager.getUserId();
                    int deleted = dbHelper.deleteAllTasksForUser(userId);
                    Toast.makeText(this, deleted + " tasks deleted", Toast.LENGTH_SHORT).show();
                    // Refresh HomeActivity stats if needed
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Apply dark mode using AppCompatDelegate.
     *
     * @param enable true for dark mode, false for light
     */
    private void applyDarkMode(boolean enable) {
        if (enable) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        // Recreate activity to apply theme
        recreate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
