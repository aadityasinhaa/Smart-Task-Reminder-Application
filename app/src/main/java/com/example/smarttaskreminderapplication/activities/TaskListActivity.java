package com.example.smarttaskreminderapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smarttaskreminderapplication.R;
import com.example.smarttaskreminderapplication.adapters.TaskAdapter;
import com.example.smarttaskreminderapplication.database.DatabaseHelper;
import com.example.smarttaskreminderapplication.models.Task;
import com.example.smarttaskreminderapplication.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskListActivity displays all tasks for the logged-in user in a RecyclerView.
 * <p>
 * Features:
 * 1. SearchView toolbar: real-time search by title/description
 * 2. Filter chips: All / Completed / Pending / High / Medium / Low priority
 * 3. Swipe-to-delete (via SwipeToDeleteCallback)
 * 4. RecyclerView with TaskAdapter showing task cards
 * 5. Empty state handling (when no tasks match filter)
 *
 * Data is loaded from DatabaseHelper and filtered in real-time.
 */
public class TaskListActivity extends AppCompatActivity
        implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private SearchView searchView;
    private ChipGroup filterChipGroup;

    private List<Task> fullTaskList;  // Unfiltered master copy
    private List<Task> filteredList;  // Currently displayed

    private String currentFilter = "ALL";  // ALL, COMPLETED, PENDING, High, Medium, Low
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        // Enable back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Tasks");
        }

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view_tasks);
        filterChipGroup = findViewById(R.id.chip_group_filter);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Load all tasks
        loadTasks();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(filteredList, this, dbHelper);
        recyclerView.setAdapter(adapter);

        // Attach swipe-to-delete
        setupSwipeToDelete();

        // Setup filter chip listener
        setupFilterChips();
    }

    /**
     * Fetch all tasks for current user from database.
     */
    private void loadTasks() {
        int userId = sessionManager.getUserId();
        fullTaskList = dbHelper.getAllTasksForUser(userId);
        applyFilters();
    }

    /**
     * Apply current search query and filter chip selection.
     */
    private void applyFilters() {
        filteredList = new ArrayList<>();

        for (Task task : fullTaskList) {
            // Apply status/priority filter
            boolean matchesFilter = matchesFilter(task);
            if (!matchesFilter) continue;

            // Apply search query
            boolean matchesSearch = matchesSearch(task);
            if (!matchesSearch) continue;

            filteredList.add(task);
        }

        adapter.updateTasks(filteredList);
    }

    /**
     * Check if task matches selected filter chip.
     */
    private boolean matchesFilter(Task task) {
        switch (currentFilter) {
            case "ALL":      return true;
            case "COMPLETED": return task.isCompleted();
            case "PENDING":  return !task.isCompleted();
            case "High":     return task.getPriority().equals("High");
            case "Medium":   return task.getPriority().equals("Medium");
            case "Low":      return task.getPriority().equals("Low");
            default:         return true;
        }
    }

    /**
     * Check if task matches search query (title or description).
     */
    private boolean matchesSearch(Task task) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return true;
        }
        String query = searchQuery.toLowerCase();
        return task.getTitle().toLowerCase().contains(query)
                || task.getDescription().toLowerCase().contains(query);
    }

    /**
     * Configure chip group with click listener.
     */
    private void setupFilterChips() {
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                currentFilter = "ALL";
            } else if (checkedId == R.id.chip_completed) {
                currentFilter = "COMPLETED";
            } else if (checkedId == R.id.chip_pending) {
                currentFilter = "PENDING";
            } else if (checkedId == R.id.chip_high) {
                currentFilter = "High";
            } else if (checkedId == R.id.chip_medium) {
                currentFilter = "Medium";
            } else if (checkedId == R.id.chip_low) {
                currentFilter = "Low";
            }
            applyFilters();
        });
    }

    /**
     * Setup ItemTouchHelper for swipe-to-delete.
     */
    private void setupSwipeToDelete() {
        SwipeToDeleteCallback swipeCallback = new SwipeToDeleteCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeCallback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Inflate menu with SearchView.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_list, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search tasks...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText;
                applyFilters();
                return true;
            }
        });

        return true;
    }

    /**
     * Handle back button in toolbar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ==================== TaskAdapter Callbacks ====================

    @Override
    public void onTaskClicked(Task task) {
        // Optional: show details dialog or open edit screen
        Intent intent = new Intent(this, AddTaskActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        intent.putExtra("TASK_TITLE", task.getTitle());
        intent.putExtra("TASK_DESC", task.getDescription());
        intent.putExtra("TASK_DATE", task.getDate());
        intent.putExtra("TASK_TIME", task.getTime());
        intent.putExtra("TASK_PRIORITY", task.getPriority());
        intent.putExtra("TASK_HAS_REMINDER", task.hasReminder());
        intent.putExtra("IS_EDIT_MODE", true);
        startActivity(intent);
    }

    @Override
    public void onTaskDeleted(Task task) {
        // Undo could be implemented here with Snackbar
        Toast.makeText(this, task.getTitle() + " deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskStatusChanged(Task task, boolean isCompleted) {
        // Immediately update UI (adapter already updated checkbox)
        String msg = task.getTitle() + (isCompleted ? " completed" : " marked pending");
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh task list when returning from AddTaskActivity
        loadTasks();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
