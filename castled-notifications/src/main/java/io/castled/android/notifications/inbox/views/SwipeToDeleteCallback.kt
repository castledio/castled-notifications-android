import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.castled.android.notifications.R
import io.castled.android.notifications.inbox.views.CastledInboxRecycleViewAdapter

internal class SwipeToDeleteCallback(private var adapter: CastledInboxRecycleViewAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {

        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        when (direction) {
            ItemTouchHelper.LEFT -> {
                // Handle swipe-to-delete here
                adapter.deleteItem(position)
            }
        }
    }

    // Add custom icons and backgrounds for delete and edit actions
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        val itemView = viewHolder.itemView
        val deleteIcon = ContextCompat.getDrawable(
            adapter.context,
            R.drawable.castled_swipe_delete_filled
        )

        val deleteBackground = ColorDrawable(Color.RED)
        val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
        val iconBottom = iconTop + deleteIcon.intrinsicHeight

        // Swiping to the left (delete action)
        if (dX < 0) {
            val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            deleteBackground.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            deleteBackground.draw(c)
            deleteIcon.draw(c)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}