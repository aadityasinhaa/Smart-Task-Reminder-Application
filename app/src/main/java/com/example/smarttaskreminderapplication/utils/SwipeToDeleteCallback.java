package com.example.smarttaskreminderapplication.utils;

import android.graphics.Canvas;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * SwipeToDeleteCallback enables swipe-to-delete functionality for RecyclerView.
 * <p>
 * Integrated with TaskAdapter to allow users to swipe task cards left to delete.
 * Shows a red background with delete icon during swipe gesture.
 * <p>
 * Usage:
 * ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new SwipeToDeleteCallback(taskAdapter);
 * ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
 * itemTouchHelper.attachToRecyclerView(recyclerView);
 */
public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final TaskAdapter adapter;

    /**
     * Constructor configures swipe directions (left only).
     *
     * @param adapter TaskAdapter instance to notify of deletions
     */
    public SwipeToDeleteCallback(TaskAdapter adapter) {
        // 0 = no drag directions, ItemTouchHelper.LEFT = swipe left only
        super(0, ItemTouchHelper.LEFT);
        this.adapter = adapter;
    }

    /**
     * Called when user swipes an item. We delegate to adapter's delete method.
     *
     * @param viewHolder  ViewHolder of swiped item
     * @param direction   Swipe direction (LEFT or RIGHT)
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // Notify adapter to delete this item
        adapter.deleteTask(viewHolder.getAdapterPosition());
    }

    /**
     * Draws the red background and delete icon during swipe.
     *
     * @param c      Canvas to draw on
     * @param recyclerView RecyclerView
     * @param viewHolder Swiped ViewHolder
     * @param dX     Horizontal displacement
     * @param dY     Vertical displacement
     * @param actionState Current action state (swipe or drag)
     * @param isCurrentlyActive Whether view is currently being dragged/swiped
     */
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        // Let the superclass handle the default swipe animation
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        // Custom background/icon drawing could be added here (e.g., red background)
    }

    /**
     * Enable or disable swipe movement for a given ViewHolder.
     * All items are swipeable.
     *
     * @param recyclerView RecyclerView
     * @param viewHolder   ViewHolder to check
     * @return Always true (all items swipeable)
     */
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        // We don't support drag-and-drop reordering, only swipe-to-delete
        return false;
    }

    /**
     * Get swipe direction movement flags (left only).
     *
     * @param recyclerView RecyclerView
     * @param viewHolder   ViewHolder
     * @return Swipe direction flags
     */
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT);
    }
}
