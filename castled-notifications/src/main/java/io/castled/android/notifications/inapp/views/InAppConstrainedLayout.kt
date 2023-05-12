package io.castled.android.notifications.inapp.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

class InAppConstrainedLayout(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private val minWidthDp = 100F // Replace with your minimum width value
    private val minHeightDp = 100F // Replace with your minimum height value
    private val maxWidthDp = 480F // Replace with your maximum width value
    private val maxHeightDp = 750F // Replace with your maximum height value

    // Convert dp to pixels
    private val minWidthPx = minWidthDp.dpToPx(context).toInt()
    private val minHeightPx = minHeightDp.dpToPx(context).toInt()
    private val maxWidthPx = maxWidthDp.dpToPx(context).toInt()
    private val maxHeightPx = maxHeightDp.dpToPx(context).toInt()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val measuredWidth = measuredWidth.coerceIn(minWidthPx, maxWidthPx)
        val measuredHeight = measuredHeight.coerceIn(minHeightPx, maxHeightPx)

        setMeasuredDimension(measuredWidth, measuredHeight)
    }
}