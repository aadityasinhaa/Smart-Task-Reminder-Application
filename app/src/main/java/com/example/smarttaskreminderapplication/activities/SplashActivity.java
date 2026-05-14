package com.example.smarttaskreminderapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smarttaskreminderapplication.R;
import com.example.smarttaskreminderapplication.utils.SessionManager;

/**
 * SplashActivity displays the app's logo and name on launch.
 * <p>
 * Flow:
 * 1. Shows animated splash screen for 2 seconds
 * 2. Checks if user is logged in via SessionManager
 * 3. Routes to HomeActivity (if logged in) or LoginActivity (if not)
 * <p>
 * Design:
 * - Gradient background (splash_background.xml)
 * - Centered icon and text
 * - Fade-in animation (anim/fade_in.xml)
 */
public class SplashActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);

        // Delay 2 seconds, then navigate
        new Handler().postDelayed(() -> {
            if (sessionManager.isLoggedIn()) {
                // User already logged in -> Home
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            } else {
                // Not logged in -> Login
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();  // Close splash so back button won't return
        }, 2000);  // 2-second delay
    }
}
