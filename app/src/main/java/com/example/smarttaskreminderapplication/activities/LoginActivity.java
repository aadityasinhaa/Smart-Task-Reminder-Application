package com.example.smarttaskreminderapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smarttaskreminderapplication.R;
import com.example.smarttaskreminderapplication.database.DatabaseHelper;
import com.example.smarttaskreminderapplication.models.User;
import com.example.smarttaskreminderapplication.utils.SessionManager;

/**
 * LoginActivity handles user authentication.
 * <p>
 * Input validation:
 * - Email: non-empty, valid email regex
 * - Password: non-empty, minimum 6 characters
 * - "Remember Me" checkbox (stored in session automatically)
 * <p>
 * On success:
 * - Creates user session via SessionManager
 * - Stores userId and userName for the session
 * - Navigates to HomeActivity
 * <p>
 * Links:
 * - "Register" button navigates to RegisterActivity
 * - "Forgot Password?" placeholder (not implemented in demo)
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoToRegister;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoToRegister = findViewById(R.id.btn_go_to_register);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    /**
     * Validate inputs and attempt to authenticate user.
     */
    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            etEmail.setError("Email required");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email required");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password required");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Min 6 characters");
            return;
        }

        // Check credentials in database
        User user = dbHelper.validateUser(email, password);
        if (user != null) {
            // Login successful -> create session
            sessionManager.createLoginSession(user.getId(), user.getName());
            Toast.makeText(this, "Welcome " + user.getName(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        } else {
            // Invalid credentials
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If already logged in from another tab, redirect
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }
}
