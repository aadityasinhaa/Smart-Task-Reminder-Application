package com.example.smarttaskreminderapplication.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.example.smarttaskreminderapplication.database.DatabaseHelper;
import com.example.smarttaskreminderapplication.models.Task;
import com.example.smarttaskreminderapplication.utils.AlarmHelper;
import com.example.smarttaskreminderapplication.utils.SessionManager;
import java.util.List;

/**
 * BootReceiver listens for BOOT_COMPLETED system broadcast.
 * <p>
 * When the device restarts, all previously scheduled alarms are cleared by the system.
 * This receiver re-schedules alarms for all pending task reminders.
 * <p>
 * Manifest requirements:
 * - RECEIVE_BOOT_COMPLETED permission
 * - android:enabled="true"
 * - Intent filter with android.intent.action.BOOT_COMPLETED
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Device just booted. Re-schedule all task alarms.
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            AlarmHelper alarmHelper = new AlarmHelper(context);

            // Get logged-in user's ID from persistent session storage
            SharedPreferences prefs = context.getSharedPreferences(SessionManager.PREF_NAME, Context.MODE_PRIVATE);
            int userId = prefs.getInt(SessionManager.KEY_USER_ID, -1);

            if (userId != -1) {
                List<Task> tasks = dbHelper.getTasksWithReminders(userId);
                for (Task task : tasks) {
                    alarmHelper.scheduleTaskAlarm(task);
                }
            }
        }
    }
}
