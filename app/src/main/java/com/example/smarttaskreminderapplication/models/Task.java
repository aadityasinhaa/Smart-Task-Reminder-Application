package com.example.smarttaskreminderapplication.models;

/**
 * Task model class representing a single task/reminder.
 * Contains all task details including title, description, date/time, priority, and completion status.
 * Used by DatabaseHelper and TaskAdapter for SQLite operations and RecyclerView display.
 */
public class Task {
    private int id;
    private String title;
    private String description;
    private String date;  // Format: yyyy-MM-dd
    private String time;  // Format: HH:mm
    private String priority;  // "High", "Medium", "Low"
    private boolean isCompleted;
    private boolean hasReminder;
    private int userId;  // Foreign key linking to User

    /**
     * Default constructor for database operations.
     */
    public Task() {
    }

    /**
     * Constructor for creating a new task (without ID, auto-incremented).
     *
     * @param title       Task title (required)
     * @param description Task description (optional)
     * @param date        Due date in yyyy-MM-dd format
     * @param time        Due time in HH:mm format (24-hour)
     * @param priority    Priority level: "High", "Medium", or "Low"
     * @param hasReminder true if task should trigger notification alarm
     * @param userId      ID of the user who owns this task
     */
    public Task(String title, String description, String date, String time, String priority, boolean hasReminder, int userId) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.priority = priority;
        this.hasReminder = hasReminder;
        this.userId = userId;
        this.isCompleted = false;
    }

    /**
     * Full constructor including ID and completion status (for database queries).
     *
     * @param id           Unique task ID (auto-incremented)
     * @param title        Task title
     * @param description  Task description
     * @param date         Due date
     * @param time         Due time
     * @param priority     Priority level
     * @param isCompleted  Completion status
     * @param hasReminder  Reminder enabled flag
     * @param userId       Owner user ID
     */
    public Task(int id, String title, String description, String date, String time, String priority, boolean isCompleted, boolean hasReminder, int userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.hasReminder = hasReminder;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean hasReminder() {
        return hasReminder;
    }

    public void setHasReminder(boolean hasReminder) {
        this.hasReminder = hasReminder;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
