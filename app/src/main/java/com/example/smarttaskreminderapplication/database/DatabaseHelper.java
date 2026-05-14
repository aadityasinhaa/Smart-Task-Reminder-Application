package com.example.smarttaskreminderapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.smarttaskreminderapplication.models.Task;
import com.example.smarttaskreminderapplication.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper manages SQLite database operations for the Smart Task Reminder app.
 * <p>
 * Handles two tables:
 * - users: stores registered users (id, name, email, password)
 * - tasks: stores task data linked to users via userId foreign key
 * <p>
 * Key concepts demonstrated:
 * - SQLiteOpenHelper lifecycle (onCreate, onUpgrade)
 * - CRUD operations (Create, Read, Update, Delete)
 * - Cursor usage for querying results
 * - ContentValues for inserting/updating rows
 * - Foreign key relationships between tables
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Configuration
    private static final String DATABASE_NAME = "SmartTaskReminder.db";
    private static final int DATABASE_VERSION = 1;

    // Users Table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD = "password";

    // Tasks Table
    public static final String TABLE_TASKS = "tasks";
    public static final String COL_TASK_ID = "id";
    public static final String COL_TASK_TITLE = "title";
    public static final String COL_TASK_DESC = "description";
    public static final String COL_TASK_DATE = "date";
    public static final String COL_TASK_TIME = "time";
    public static final String COL_TASK_PRIORITY = "priority";
    public static final String COL_TASK_COMPLETED = "isCompleted";
    public static final String COL_TASK_HAS_REMINDER = "hasReminder";
    public static final String COL_TASK_USER_ID = "userId";

    /**
     * Constructor initializes database and creates tables on first run.
     *
     * @param context Application or activity context
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when database is created for the first time.
     * Creates users and tasks tables with appropriate columns and constraints.
     *
     * @param db SQLiteDatabase instance being created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_USER_NAME + " TEXT NOT NULL,"
                + COL_USER_EMAIL + " TEXT UNIQUE NOT NULL,"
                + COL_USER_PASSWORD + " TEXT NOT NULL"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create tasks table with foreign key to users
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_TASK_TITLE + " TEXT NOT NULL,"
                + COL_TASK_DESC + " TEXT,"
                + COL_TASK_DATE + " TEXT NOT NULL,"
                + COL_TASK_TIME + " TEXT NOT NULL,"
                + COL_TASK_PRIORITY + " TEXT NOT NULL,"
                + COL_TASK_COMPLETED + " INTEGER DEFAULT 0,"
                + COL_TASK_HAS_REMINDER + " INTEGER DEFAULT 0,"
                + COL_TASK_USER_ID + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + COL_TASK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_TASKS_TABLE);
    }

    /**
     * Called when database version is incremented.
     * Handles schema migrations (add columns, create new tables, etc.).
     *
     * @param db         SQLiteDatabase instance
     * @param oldVersion Previous database version
     * @param newVersion New database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple upgrade strategy: drop and recreate (for demo app)
        // In production, you would migrate data properly
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ==================== USER OPERATIONS ====================

    /**
     * Insert a new user into the database.
     *
     * @param user User object to insert
     * @return row ID of newly inserted user, or -1 if error (e.g., email already exists)
     */
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, user.getName());
        values.put(COL_USER_EMAIL, user.getEmail());
        values.put(COL_USER_PASSWORD, user.getPassword());

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    /**
     * Validate user credentials for login.
     *
     * @param email    User email
     * @param password User password
     * @return User object if credentials match, null otherwise
     */
    public User validateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COL_USER_ID, COL_USER_NAME, COL_USER_EMAIL, COL_USER_PASSWORD};
        String selection = COL_USER_EMAIL + " = ? AND " + COL_USER_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        User user = null;

        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD))
            );
            cursor.close();
        }

        db.close();
        return user;
    }

    /**
     * Check if an email is already registered.
     *
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean isEmailRegistered(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COL_USER_ID};
        String selection = COL_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }

    /**
     * Update user's name.
     *
     * @param userId User ID
     * @param name   New display name
     * @return number of rows affected (1 if successful)
     */
    public int updateUserName(int userId, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);

        int rows = db.update(TABLE_USERS, values, COL_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();
        return rows;
    }

    // ==================== TASK OPERATIONS ====================

    /**
     * Insert a new task.
     *
     * @param task Task object to insert
     * @return row ID of new task, or -1 on error
     */
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_TITLE, task.getTitle());
        values.put(COL_TASK_DESC, task.getDescription());
        values.put(COL_TASK_DATE, task.getDate());
        values.put(COL_TASK_TIME, task.getTime());
        values.put(COL_TASK_PRIORITY, task.getPriority());
        values.put(COL_TASK_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COL_TASK_HAS_REMINDER, task.hasReminder() ? 1 : 0);
        values.put(COL_TASK_USER_ID, task.getUserId());

        long result = db.insert(TABLE_TASKS, null, values);
        db.close();
        return result;
    }

    /**
     * Retrieve a single task by ID.
     *
     * @param taskId Task ID
     * @return Task object or null if not found
     */
    public Task getTask(int taskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                COL_TASK_ID, COL_TASK_TITLE, COL_TASK_DESC, COL_TASK_DATE,
                COL_TASK_TIME, COL_TASK_PRIORITY, COL_TASK_COMPLETED,
                COL_TASK_HAS_REMINDER, COL_TASK_USER_ID
        };
        String selection = COL_TASK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(taskId)};

        Cursor cursor = db.query(TABLE_TASKS, columns, selection, selectionArgs, null, null, null);
        Task task = null;

        if (cursor != null && cursor.moveToFirst()) {
            task = new Task(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESC)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TIME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_PRIORITY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_HAS_REMINDER)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_USER_ID))
            );
            cursor.close();
        }

        db.close();
        return task;
    }

    /**
     * Get all tasks for a specific user, sorted by date/time ascending (nearest first).
     *
     * @param userId User ID
     * @return List of Task objects (may be empty)
     */
    public List<Task> getAllTasksForUser(int userId) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_TASKS
                + " WHERE " + COL_TASK_USER_ID + " = ?"
                + " ORDER BY " + COL_TASK_DATE + " ASC, " + COL_TASK_TIME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_PRIORITY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_HAS_REMINDER)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_USER_ID))
                );
                taskList.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return taskList;
    }

    /**
     * Update an existing task.
     *
     * @param task Task object with updated values (must have valid ID)
     * @return number of rows affected
     */
    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_TITLE, task.getTitle());
        values.put(COL_TASK_DESC, task.getDescription());
        values.put(COL_TASK_DATE, task.getDate());
        values.put(COL_TASK_TIME, task.getTime());
        values.put(COL_TASK_PRIORITY, task.getPriority());
        values.put(COL_TASK_HAS_REMINDER, task.hasReminder() ? 1 : 0);
        values.put(COL_TASK_USER_ID, task.getUserId());

        int rows = db.update(TABLE_TASKS, values, COL_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        return rows;
    }

    /**
     * Update task completion status.
     *
     * @param taskId      Task ID
     * @param isCompleted New completion status
     * @return number of rows affected
     */
    public int updateTaskStatus(int taskId, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_COMPLETED, isCompleted ? 1 : 0);

        int rows = db.update(TABLE_TASKS, values, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rows;
    }

    /**
     * Delete a task by ID.
     *
     * @param taskId Task ID to delete
     * @return number of rows affected (1 if successful)
     */
    public int deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_TASKS, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rows;
    }

    /**
     * Delete all tasks for a specific user (used on logout or bulk delete).
     *
     * @param userId User ID
     * @return number of rows deleted
     */
    public int deleteAllTasksForUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_TASKS, COL_TASK_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();
        return rows;
    }

    /**
     * Get task statistics for a user.
     *
     * @param userId User ID
     * @return int array: [total, completed, pending]
     */
    public int[] getTaskStats(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int[] stats = new int[3]; // [total, completed, pending]

        // Total tasks
        Cursor totalCursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COL_TASK_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
        if (totalCursor != null && totalCursor.moveToFirst()) {
            stats[0] = totalCursor.getInt(0);
            totalCursor.close();
        }

        // Completed tasks
        Cursor completedCursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COL_TASK_USER_ID + " = ? AND " + COL_TASK_COMPLETED + " = 1",
                new String[]{String.valueOf(userId)}
        );
        if (completedCursor != null && completedCursor.moveToFirst()) {
            stats[1] = completedCursor.getInt(0);
            completedCursor.close();
        }

        // Pending tasks
        stats[2] = stats[0] - stats[1];

        db.close();
        return stats;
    }

    /**
     * Get task count by priority for a user.
     *
     * @param userId User ID
     * @return Map with priority as key and count as value
     */
    public java.util.Map<String, Integer> getPriorityStats(int userId) {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] priorities = {"High", "Medium", "Low"};
        for (String priority : priorities) {
            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COL_TASK_USER_ID + " = ? AND " + COL_TASK_PRIORITY + " = ?",
                    new String[]{String.valueOf(userId), priority}
            );
            if (cursor != null && cursor.moveToFirst()) {
                stats.put(priority, cursor.getInt(0));
                cursor.close();
            }
        }

        db.close();
        return stats;
    }

    /**
     * Find tasks matching a search query (title or description).
     *
     * @param userId User ID
     * @param query  Search string
     * @return List of matching tasks
     */
    public List<Task> searchTasks(int userId, String query) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String searchPattern = "%" + query.toLowerCase() + "%";
        String[] columns = {
                COL_TASK_ID, COL_TASK_TITLE, COL_TASK_DESC, COL_TASK_DATE,
                COL_TASK_TIME, COL_TASK_PRIORITY, COL_TASK_COMPLETED,
                COL_TASK_HAS_REMINDER, COL_TASK_USER_ID
        };
        String selection = COL_TASK_USER_ID + " = ? AND (LOWER(" + COL_TASK_TITLE + ") LIKE ? OR LOWER(" + COL_TASK_DESC + ") LIKE ?)";
        String[] selectionArgs = {String.valueOf(userId), searchPattern, searchPattern};

        Cursor cursor = db.query(TABLE_TASKS, columns, selection, selectionArgs, null, null,
                COL_TASK_DATE + " ASC, " + COL_TASK_TIME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_PRIORITY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_HAS_REMINDER)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_USER_ID))
                );
                taskList.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return taskList;
    }

    /**
     * Get tasks filtered by completion status.
     *
     * @param userId     User ID
     * @param completed  true for completed, false for pending
     * @return Filtered task list
     */
    public List<Task> getTasksByStatus(int userId, boolean completed) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COL_TASK_ID, COL_TASK_TITLE, COL_TASK_DESC, COL_TASK_DATE,
                COL_TASK_TIME, COL_TASK_PRIORITY, COL_TASK_COMPLETED,
                COL_TASK_HAS_REMINDER, COL_TASK_USER_ID
        };
        String selection = COL_TASK_USER_ID + " = ? AND " + COL_TASK_COMPLETED + " = ?";
        String[] selectionArgs = {String.valueOf(userId), completed ? "1" : "0"};

        Cursor cursor = db.query(TABLE_TASKS, columns, selection, selectionArgs, null, null,
                COL_TASK_DATE + " ASC, " + COL_TASK_TIME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_PRIORITY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_HAS_REMINDER)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_USER_ID))
                );
                taskList.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return taskList;
    }

    /**
     * Get tasks filtered by priority.
     *
     * @param userId   User ID
     * @param priority Priority level ("High", "Medium", "Low")
     * @return Filtered task list
     */
    public List<Task> getTasksByPriority(int userId, String priority) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COL_TASK_ID, COL_TASK_TITLE, COL_TASK_DESC, COL_TASK_DATE,
                COL_TASK_TIME, COL_TASK_PRIORITY, COL_TASK_COMPLETED,
                COL_TASK_HAS_REMINDER, COL_TASK_USER_ID
        };
        String selection = COL_TASK_USER_ID + " = ? AND " + COL_TASK_PRIORITY + " = ?";
        String[] selectionArgs = {String.valueOf(userId), priority};

        Cursor cursor = db.query(TABLE_TASKS, columns, selection, selectionArgs, null, null,
                COL_TASK_DATE + " ASC, " + COL_TASK_TIME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_PRIORITY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_HAS_REMINDER)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_USER_ID))
                );
                taskList.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return taskList;
    }

    /**
     * Get tasks that have reminders enabled (for alarm scheduling).
     *
     * @param userId User ID
     * @return List of tasks with reminders
     */
    public List<Task> getTasksWithReminders(int userId) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COL_TASK_ID, COL_TASK_TITLE, COL_TASK_DESC, COL_TASK_DATE,
                COL_TASK_TIME, COL_TASK_PRIORITY, COL_TASK_COMPLETED,
                COL_TASK_HAS_REMINDER, COL_TASK_USER_ID
        };
        String selection = COL_TASK_USER_ID + " = ? AND " + COL_TASK_HAS_REMINDER + " = 1 AND " + COL_TASK_COMPLETED + " = 0";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(TABLE_TASKS, columns, selection, selectionArgs, null, null,
                COL_TASK_DATE + " ASC, " + COL_TASK_TIME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_PRIORITY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_HAS_REMINDER)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_USER_ID))
                );
                taskList.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return taskList;
    }
}
