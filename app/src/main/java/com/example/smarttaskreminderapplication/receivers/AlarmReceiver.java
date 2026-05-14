package com.example.smarttaskreminderapplication.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import com.example.smarttaskreminderapplication.utils.NotificationHelper;

/**
 * AlarmReceiver is triggered when a task reminder alarm fires.
 * <p>
 * Receives the explicit intent from AlarmManager, extracts task details,
 * and displays a notification via NotificationHelper.
 * <p>
 * Also cancels the notification if it's an alarm trigger (to avoid duplicates).
 */
public class AlarmReceiver extends BroadcastReceiver {

    /**
     * Called when the broadcast is received (alarm fires).
     *
     * @param context Context for accessing system services
     * @param intent  Intent containing extras (TASK_ID, TASK_TITLE)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.hasExtra("TASK_ID")) {
            int taskId = intent.getIntExtra("TASK_ID", 0);
            String title = intent.getStringExtra("TASK_TITLE");

            // Build and display notification
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.createNotificationChannel();  // Safe to call repeatedly
            notificationHelper.sendTaskReminderNotification(
                    taskId,
                    "Task Reminder",
                    "Time to complete: " + (title != null ? title : "Your task")
            );

            // Optional: Cancel notification after some delay or when user interacts
        }
    }
}
