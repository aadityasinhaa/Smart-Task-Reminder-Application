package com.example.smarttaskreminderapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.smarttaskreminderapplication.R;
import com.example.smarttaskreminderapplication.database.DatabaseHelper;
import com.example.smarttaskreminderapplication.utils.SessionManager;

/**
 * HomeActivity is the main dashboard after login.
 * <p>
 * Displays:
 * - Welcome message with user's name
 * - Three stat cards: Total Tasks, Completed, Pending
 * - Floating Action Button (FAB) to add new task
 * - Navigation to TaskList, Statistics, Settings
 * <p>
 * Stats are fetched from DatabaseHelper getTaskStats() method.
 */
public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalTasks, tvCompletedTasks, tvPendingTasks;
    private CardView cardTaskList, cardStatistics, cardSettings;
    private Button btnAddTask;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        tvWelcome = findViewById(R.id.tv_welcome);
        tvTotalTasks = findViewById(R.id.tv_total_tasks);
        tvCompletedTasks = findViewById(R.id.tv_completed_tasks);
        tvPendingTasks = findViewById(R.id.tv_pending_tasks);
        cardTaskList = findViewById(R.id.card_task_list);
        cardStatistics = findViewById(R.id.card_statistics);
        cardSettings = findViewById(R.id.card_settings);
        btnAddTask = findViewById(R.id.btn_add_task);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Set welcome message
        String userName = sessionManager.getUserName();
        tvWelcome.setText("Welcome, " + userName + "!");

        // Load task statistics
        loadStats();

        // Navigation listeners
        btnAddTask.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AddTaskActivity.class));
        });

        cardTaskList.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, TaskListActivity.class));
        });

        cardStatistics.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, StatisticsActivity.class));
        });

        cardSettings.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        });
    }

    /**
     * Fetch and display task statistics for current user.
     * Called in onCreate and onResume to refresh data.
     */
    private void loadStats() {
        int userId = sessionManager.getUserId();
        if (userId != -1) {
            int[] stats = dbHelper.getTaskStats(userId);
            tvTotalTasks.setText(String.valueOf(stats[0]));
            tvCompletedTasks.setText(String.valueOf(stats[1]));
            tvPendingTasks.setText(String.valueOf(stats[2]));
        } else {
            // Not logged in? Shouldn't happen, but handle gracefully
            tvTotalTasks.setText("0");
            tvCompletedTasks.setText("0");
            tvPendingTasks.setText("0");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh stats when returning to this activity
        loadStats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connection to avoid leaks
        dbHelper.close();
    }
}
