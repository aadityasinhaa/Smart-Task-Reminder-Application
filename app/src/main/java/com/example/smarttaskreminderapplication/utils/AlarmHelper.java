package com.example.smarttaskreminderapplication.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.example.smarttaskreminderapplication.models.Task;
import com.example.smarttaskreminderapplication.receivers.AlarmReceiver;

import java.util.Calendar;

/**
 * AlarmHelper simplifies scheduling and canceling task reminder alarms.
 * <p>
 * Key Android concepts:
 * - AlarmManager: System service for triggering actions at specific times
 * - setExactAndAllowWhileIdle: Fires alarm at exact time even in Doze mode (API 23+)
 * - RTC_WAKEUP: Wakes device from sleep to deliver alarm
 * - PendingIntent: Wraps an Intent to be executed later by the system
 * <p>
 * Alarms are scheduled for each task's date+time when hasReminder = true.
 * BootReceiver re-registers all alarms after device reboot.
 */
public class AlarmHelper {
    private Context context;
    private AlarmManager alarmManager;

    /**
     * Constructor gets AlarmManager system service.
     *
     * @param context Application or activity context
     */
    public AlarmHelper(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Schedule an exact alarm for a task reminder.
     * <p>
     * Parses the task's date (yyyy-MM-dd) and time (HH:mm) into a Calendar,
     * then schedules AlarmManager to fire at that exact moment.
     *
     * @param task Task containing reminder date/time
     * @return true if alarm scheduled successfully, false if time is in the past
     */
    public boolean scheduleTaskAlarm(Task task) {
        if (!task.hasReminder()) {
            return false;
        }

        // Parse date and time strings into Calendar
        String[] dateParts = task.getDate().split("-");
        String[] timeParts = task.getTime().split(":");

        if (dateParts.length < 3 || timeParts.length < 2) {
            return false; // Invalid format
        }

        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1;  // Calendar months are 0-indexed
        int day = Integer.parseInt(dateParts[2]);
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long alarmTimeMillis = calendar.getTimeInMillis();
        long currentTime = System.currentTimeMillis();

        // Don't schedule alarm for past times
        if (alarmTimeMillis <= currentTime) {
            return false;
        }

        // Create explicit intent for AlarmReceiver
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("TASK_ID", task.getId());
        intent.putExtra("TASK_TITLE", task.getTitle());

        // Generate unique PendingIntent using task ID as request code
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId(),  // Unique request code per task
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule exact alarm that works even in Doze mode
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            // Android 12+ requires exact alarm permission
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeMillis,
                        pendingIntent
                );
            } else {
                // Fallback to non-exact if permission denied
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeMillis,
                        pendingIntent
                );
            }
        } else {
            // Pre-Android 12
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeMillis,
                    pendingIntent
            );
        }

        return true;
    }

    /**
     * Cancel a scheduled alarm for a specific task.
     *
     * @param task Task whose alarm should be cancelled
     */
    public void cancelAlarm(Task task) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    /**
     * Cancel all pending alarms (e.g., on logout).
     * Note: This cancels ALL alarms for this app, not just user-specific ones.
     * In a multi-user app, you'd need more granular control.
     */
    public void cancelAllAlarms() {
        // Clear all pending intents matching this app's AlarmReceiver
        // This is a broad cancellation; use with caution
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,  // Request code 0 matches none with specific IDs
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
