package com.example.smarttaskreminderapplication.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.core.app.NotificationCompat;
import com.example.smarttaskreminderapplication.R;
import com.example.smarttaskreminderapplication.activities.TaskListActivity;

/**
 * NotificationHelper creates and manages Android notification channels and builds notifications.
 * <p>
 * Key concepts:
 * - NotificationChannel: Required for Android 8.0+ (API 26) to group notifications
 * - NotificationCompat: Backward-compatible notification builder
 * - PendingIntent: Intent that fires when user taps notification
 * - setExactAndAllowWhileIdle: AlarmManager method for precise timing even in Doze mode
 */
public class NotificationHelper {
    private Context context;
    private NotificationManager notificationManager;

    // Channel constants
    public static final String CHANNEL_ID = "task_reminder_channel";
    public static final String CHANNEL_NAME = "Task Reminders";
    public static final String CHANNEL_DESC = "Notifications for upcoming task deadlines";

    // Notification IDs
    private static final int NOTIFICATION_ID_BASE = 1000;
    private static int notificationIdCounter = NOTIFICATION_ID_BASE;

    /**
     * Constructor initializes NotificationManager.
     *
     * @param context Application context
     */
    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Create the notification channel (required for API 26+).
     * Should be called once at app startup (e.g., in SplashActivity).
     */
    public void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Set importance to HIGH to show heads-up notification
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500}); // Vibrate pattern

            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Build and send a task reminder notification.
     *
     * @param taskId   Unique task ID (for notification tag)
     * @param title    Task title
     * @param message  Notification message
     */
    public void sendTaskReminderNotification(int taskId, String title, String message) {
        // Intent to open TaskListActivity when notification tapped
        Intent intent = new Intent(context, TaskListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("TASK_ID", taskId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                taskId,  // Unique request code per task
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification using NotificationCompat for backward compatibility
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)  // Status bar icon
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // Show as heads-up
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)  // Dismiss when tapped
                .setVibrate(new long[]{0, 500, 200, 500})
                .setColor(context.getResources().getColor(R.color.primary_500));

        // Show notification with unique ID per task
        notificationManager.notify(taskId, builder.build());
    }

    /**
     * Cancel a specific task's notification.
     *
     * @param taskId Task ID (used as notification ID)
     */
    public void cancelNotification(int taskId) {
        notificationManager.cancel(taskId);
    }

    /**
     * Clear all notifications posted by this app.
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    /**
     * Generate unique notification ID (incrementing).
     * Useful when scheduling multiple reminders.
     *
     * @return Unique notification ID
     */
    public static int getNextNotificationId() {
        return notificationIdCounter++;
    }
}
