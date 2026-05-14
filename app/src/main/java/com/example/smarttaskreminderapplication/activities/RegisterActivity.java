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

/**
 * RegisterActivity allows new users to create an account.
 * <p>
 * Input validation:
 * - Name: non-empty
 * - Email: non-empty, valid format, must be unique (not already registered)
 * - Password: minimum 6 characters
 * - Confirm Password: must match password
 * <p>
 * On success:
 * - Inserts user into SQLite database (users table)
 * - Redirects to LoginActivity
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister, btnGoToLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        btnGoToLogin = findViewById(R.id.btn_go_to_login);

        dbHelper = new DatabaseHelper(this);

        btnRegister.setOnClickListener(v -> attemptRegistration());
        btnGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    /**
     * Validate all inputs and create new user.
     */
    private void attemptRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etName.setError("Name required");
            return;
        }
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
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords don't match");
            return;
        }

        // Check if email already registered
        if (dbHelper.isEmailRegistered(email)) {
            etEmail.setError("Email already registered");
            return;
        }

        // Create user object and insert into DB
        User newUser = new User(name, email, password);
        long userId = dbHelper.addUser(newUser);

        if (userId != -1) {
            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
