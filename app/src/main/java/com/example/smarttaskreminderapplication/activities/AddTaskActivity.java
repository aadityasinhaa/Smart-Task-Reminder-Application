package com.example.smarttaskreminderapplication.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smarttaskreminderapplication.R;
import com.example.smarttaskreminderapplication.database.DatabaseHelper;
import com.example.smarttaskreminderapplication.models.Task;
import com.example.smarttaskreminderapplication.utils.AlarmHelper;
import com.example.smarttaskreminderapplication.utils.NotificationHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * AddTaskActivity handles creating and editing tasks.
 * <p>
 * Features:
 * - TextInputLayout fields for title and description
 * - DatePickerDialog and TimePickerDialog for due date/time
 * - Spinner for priority (High/Medium/Low)
 * - SwitchMaterial for enabling/disabling reminder alarm
 * - Save button inserts or updates task in SQLite
 * - Schedules alarm if reminder enabled (via AlarmHelper)
 *
 * Edit mode:
 * - If TASK_ID extra is present, loads existing task data for editing.
 * - Otherwise creates new task.
 */
public class AddTaskActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private TextView tvDate, tvTime;
    private Spinner spinnerPriority;
    private SwitchMaterial checkboxReminder;
    private Button btnSave, btnCancel;

    private String selectedDate = "";
    private String selectedTime = "";
    private String selectedPriority = "Medium";  // Default

    private DatabaseHelper dbHelper;
    private AlarmHelper alarmHelper;
    private NotificationHelper notificationHelper;

    private boolean isEditMode = false;
    private int editingTaskId = -1;

    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize views
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        tvDate = findViewById(R.id.tv_date);
        tvTime = findViewById(R.id.tv_time);
        spinnerPriority = findViewById(R.id.spinner_priority);
        checkboxReminder = findViewById(R.id.checkbox_reminder);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        dbHelper = new DatabaseHelper(this);
        alarmHelper = new AlarmHelper(this);
        notificationHelper = new NotificationHelper(this);
        notificationHelper.createNotificationChannel();

        // Setup priority spinner
        setupPrioritySpinner();

        // Setup date/time pickers
        setupDateTimePickers();

        // Check if editing existing task
        handleIntentExtras();

        // Button click listeners
        btnSave.setOnClickListener(v -> saveTask());
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * Initialize priority spinner with High/Medium/Low options.
     */
    private void setupPrioritySpinner() {
        String[] priorities = {"Low", "Medium", "High"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);

        spinnerPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPriority = priorities[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPriority = "Medium";
            }
        });
    }

    /**
     * Set OnClickListeners for date/time TextViews to show pickers.
     */
    private void setupDateTimePickers() {
        tvDate.setOnClickListener(v -> showDatePicker());
        tvTime.setOnClickListener(v -> showTimePicker());
    }

    /**
     * Show DatePickerDialog and update selectedDate TextView.
     */
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedDate = dateFormat.format(calendar.getTime());
                    tvDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Show TimePickerDialog and update selectedTime TextView.
     */
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    selectedTime = timeFormat.format(calendar.getTime());
                    tvTime.setText(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true  // 24-hour format
        );
        timePickerDialog.show();
    }

    /**
     * Check if this activity is in edit mode (task ID passed via Intent).
     * If so, load task data into form fields.
     */
    private void handleIntentExtras() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("TASK_ID")) {
            isEditMode = true;
            editingTaskId = intent.getIntExtra("TASK_ID", -1);

            // Pre-fill form with task data
            etTitle.setText(intent.getStringExtra("TASK_TITLE"));
            etDescription.setText(intent.getStringExtra("TASK_DESC"));
            tvDate.setText(intent.getStringExtra("TASK_DATE"));
            tvTime.setText(intent.getStringExtra("TASK_TIME"));
            selectedPriority = intent.getStringExtra("TASK_PRIORITY");
            checkboxReminder.setChecked(intent.getBooleanExtra("TASK_HAS_REMINDER", false));

            // Set spinner to existing priority
            setSpinnerToPriority(selectedPriority);
        }
    }

    /**
     * Set spinner selection based on priority string.
     */
    private void setSpinnerToPriority(String priority) {
        String[] priorities = {"Low", "Medium", "High"};
        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i].equals(priority)) {
                spinnerPriority.setSelection(i);
                break;
            }
        }
    }

    /**
     * Validate form and insert/update task in database.
     */
    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String date = tvDate.getText().toString().trim();
        String time = tvTime.getText().toString().trim();
        boolean hasReminder = checkboxReminder.isChecked();

        // Validation
        if (title.isEmpty()) {
            etTitle.setError("Title required");
            return;
        }
        if (date.isEmpty() || date.equals("Select Date")) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (time.isEmpty() || time.equals("Select Time")) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = new SessionManager(this).getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (isEditMode) {
            // Update existing task
            Task task = new Task(editingTaskId, title, description, date, time,
                    selectedPriority, false, hasReminder, userId);
            dbHelper.updateTask(task);

            // Cancel old alarm if reminder was changed
            Task oldTask = dbHelper.getTask(editingTaskId);
            if (oldTask.hasReminder()) {
                alarmHelper.cancelAlarm(oldTask);
            }

            // Schedule new alarm if enabled
            if (hasReminder) {
                alarmHelper.scheduleTaskAlarm(task);
            }

            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
        } else {
            // Create new task
            Task task = new Task(title, description, date, time, selectedPriority, hasReminder, userId);
            long taskId = dbHelper.addTask(task);

            if (taskId != -1) {
                // Get the created task with its auto-generated ID
                Task createdTask = dbHelper.getTask((int) taskId);
                if (createdTask != null && hasReminder) {
                    alarmHelper.scheduleTaskAlarm(createdTask);
                }
                Toast.makeText(this, "Task created", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to create task", Toast.LENGTH_SHORT).show();
            }
        }

        // Return to previous screen
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
