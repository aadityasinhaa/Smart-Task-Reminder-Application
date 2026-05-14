package com.example.smarttaskreminderapplication.activities;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smarttaskreminderapplication.R;
import com.example.smarttaskreminderapplication.database.DatabaseHelper;
import com.example.smarttaskreminderapplication.utils.SessionManager;
import java.util.Map;

/**
 * StatisticsActivity visualizes task completion statistics.
 * <p>
 * Displays:
 * - Overall completion ratio (progress bar)
 * - Percentage text (e.g., "65% Completed")
 * - Breakdown by priority (counts for High/Medium/Low)
 *
 * Data fetched from DatabaseHelper statistics methods.
 */
public class StatisticsActivity extends AppCompatActivity {

    private TextView tvTotalTasks, tvCompletedCount, tvPendingCount;
    private TextView tvCompletionPercent;
    private ProgressBar progressOverall;
    private TextView tvHighCount, tvMediumCount, tvLowCount;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Statistics");
        }

        // Initialize views
        tvTotalTasks = findViewById(R.id.tv_total_count);
        tvCompletedCount = findViewById(R.id.tv_completed_count);
        tvPendingCount = findViewById(R.id.tv_pending_count);
        tvCompletionPercent = findViewById(R.id.tv_completion_percent);
        progressOverall = findViewById(R.id.progress_completion);
        tvHighCount = findViewById(R.id.tv_high_count);
        tvMediumCount = findViewById(R.id.tv_medium_count);
        tvLowCount = findViewById(R.id.tv_low_count);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        loadStatistics();
    }

    /**
     * Compute and display all stats.
     */
    private void loadStatistics() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            finish();
            return;
        }

        // Get overall stats
        int[] stats = dbHelper.getTaskStats(userId);
        int total = stats[0];
        int completed = stats[1];
        int pending = stats[2];

        // Update text views
        tvTotalTasks.setText(String.valueOf(total));
        tvCompletedCount.setText(String.valueOf(completed));
        tvPendingCount.setText(String.valueOf(pending));

        // Update progress bar (max 100)
        int percent = total > 0 ? (completed * 100) / total : 0;
        progressOverall.setProgress(percent);
        tvCompletionPercent.setText(percent + "% Completed");

        // Get priority breakdown
        Map<String, Integer> priorityStats = dbHelper.getPriorityStats(userId);
        tvHighCount.setText(String.valueOf(priorityStats.getOrDefault("High", 0)));
        tvMediumCount.setText(String.valueOf(priorityStats.getOrDefault("Medium", 0)));
        tvLowCount.setText(String.valueOf(priorityStats.getOrDefault("Low", 0)));
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
