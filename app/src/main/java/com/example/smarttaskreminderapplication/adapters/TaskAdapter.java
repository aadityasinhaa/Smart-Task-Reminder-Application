package com.example.smarttaskreminderapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smarttaskreminderapplication.R;
import com.example.smarttaskreminderapplication.models.Task;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * TaskAdapter bridges the task data (List<Task>) with the RecyclerView.
 * <p>
 * Key concepts:
 * - ViewHolder pattern: holds references to item views for efficient recycling
 * - DiffUtil could be added for better updates (simplified here)
 * - Supports click listeners:
 *   - Checkbox: toggle completion status
 *   - Edit button: open edit screen
 *   - Card click: view details (optional)
 *
 * Integrates with DatabaseHelper for persistence operations.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener listener;
    private DatabaseHelper dbHelper;

    /**
     * Interface for task interaction callbacks.
     */
    public interface OnTaskClickListener {
        void onTaskClicked(Task task);
        void onTaskDeleted(Task task);
        void onTaskStatusChanged(Task task, boolean isCompleted);
    }

    /**
     * Constructor.
     *
     * @param taskList Initial list of tasks
     * @param listener Callback implementation
     * @param dbHelper DatabaseHelper for CRUD operations
     */
    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener, DatabaseHelper dbHelper) {
        this.taskList = new ArrayList<>(taskList);
        this.listener = listener;
        this.dbHelper = dbHelper;
    }

    /**
     * Inflate item_task.xml and create ViewHolder.
     */
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    /**
     * Bind task data to views in ViewHolder.
     * Sets priority color, formatted date/time, completion state.
     *
     * @param holder   ViewHolder to bind to
     * @param position Position in list
     */
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task, listener);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    /**
     * Update the adapter's dataset and refresh the view.
     *
     * @param newTasks New list of tasks
     */
    public void updateTasks(List<Task> newTasks) {
        taskList.clear();
        taskList.addAll(newTasks);
        notifyDataSetChanged();
    }

    /**
     * Remove a task from the list (used by swipe-to-delete).
     *
     * @param position Position to remove
     */
    public void deleteTask(int position) {
        Task deletedTask = taskList.get(position);
        // Delete from database
        dbHelper.deleteTask(deletedTask.getId());
        // Remove from list and notify adapter
        taskList.remove(position);
        notifyItemRemoved(position);
        // Notify listener (for Undo Snackbar)
        if (listener != null) {
            listener.onTaskDeleted(deletedTask);
        }
    }

    /**
     * ViewHolder holds references to views in item_task.xml.
     * Uses efficient view lookup via findViewById.
     */
    class TaskViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvTitle, tvDescription, tvDate, tvTime, tvPriority;
        private CheckBox checkboxCompleted;
        private ImageButton btnEdit;
        private View priorityIndicator;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_task);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDescription = itemView.findViewById(R.id.tv_task_desc);
            tvDate = itemView.findViewById(R.id.tv_task_date);
            tvTime = itemView.findViewById(R.id.tv_task_time);
            tvPriority = itemView.findViewById(R.id.tv_priority);
            checkboxCompleted = itemView.findViewById(R.id.checkbox_completed);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            priorityIndicator = itemView.findViewById(R.id.view_priority_indicator);
        }

        /**
         * Bind task data to UI elements.
         * Sets click listeners for checkbox, edit button, and card.
         */
        void bind(final Task task, final OnTaskClickListener listener) {
            tvTitle.setText(task.getTitle());

            // Show description or placeholder if empty
            String desc = task.getDescription();
            if (desc == null || desc.trim().isEmpty()) {
                tvDescription.setText("No description");
                tvDescription.setTextColor(itemView.getResources().getColor(R.color.text_secondary));
            } else {
                tvDescription.setText(desc);
                tvDescription.setTextColor(itemView.getResources().getColor(R.color.text_primary));
            }

            // Format date and time
            tvDate.setText(formatDate(task.getDate()));
            tvTime.setText(formatTime(task.getTime()));

            // Set priority label and color
            tvPriority.setText(task.getPriority());
            int priorityColor = getPriorityColor(task.getPriority());
            priorityIndicator.setBackgroundColor(itemView.getResources().getColor(priorityColor));

            // Checkbox state
            checkboxCompleted.setChecked(task.isCompleted());
            // Dim text if completed
            tvTitle.setAlpha(task.isCompleted() ? 0.6f : 1.0f);

            // Click listeners
            checkboxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Update database
                dbHelper.updateTaskStatus(task.getId(), isChecked);
                // Notify listener
                listener.onTaskStatusChanged(task, isChecked);
            });

            btnEdit.setOnClickListener(v -> {
                // Launch edit screen
                Context context = v.getContext();
                Intent intent = new Intent(context, com.example.smarttaskreminderapplication.activities.AddTaskActivity.class);
                intent.putExtra("TASK_ID", task.getId());
                intent.putExtra("TASK_TITLE", task.getTitle());
                intent.putExtra("TASK_DESC", task.getDescription());
                intent.putExtra("TASK_DATE", task.getDate());
                intent.putExtra("TASK_TIME", task.getTime());
                intent.putExtra("TASK_PRIORITY", task.getPriority());
                intent.putExtra("TASK_HAS_REMINDER", task.hasReminder());
                intent.putExtra("IS_EDIT_MODE", true);
                context.startActivity(intent);
            });

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClicked(task);
                }
            });
        }

        /**
         * Format date string (yyyy-MM-dd) to readable format.
         *
         * @param dateStr Date string in database format
         * @return Formatted date like "14 May 2026"
         */
        private String formatDate(String dateStr) {
            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = input.parse(dateStr);
                SimpleDateFormat output = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
                return output.format(date);
            } catch (Exception e) {
                return dateStr;  // Fallback to raw string
            }
        }

        /**
         * Format time string (HH:mm) to 12-hour format with AM/PM.
         *
         * @param timeStr Time in 24-hour format
         * @return Formatted time like "2:30 PM"
         */
        private String formatTime(String timeStr) {
            try {
                SimpleDateFormat input = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date time = input.parse(timeStr);
                SimpleDateFormat output = new SimpleDateFormat("h:mm a", Locale.getDefault());
                return output.format(time);
            } catch (Exception e) {
                return timeStr;
            }
        }

        /**
         * Map priority string to color resource.
         *
         * @param priority "High", "Medium", or "Low"
         * @return Color resource ID
         */
        private int getPriorityColor(String priority) {
            switch (priority) {
                case "High":   return R.color.priority_high;
                case "Medium":  return R.color.priority_medium;
                case "Low":    return R.color.priority_low;
                default:       return R.color.priority_medium;
            }
        }
    }
}
