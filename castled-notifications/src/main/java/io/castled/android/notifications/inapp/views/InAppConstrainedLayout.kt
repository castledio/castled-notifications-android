package io.castled.android.notifications.inapp.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.RelativeLayout
import io.castled.android.notifications.R

class InAppConstrainedLayout(context: Context, attrs: AttributeSet) :
    RelativeLayout(context, attrs) {

    private val minWidthDp = 100F // Replace with your minimum width value
    private val minHeightDp = 100F // Replace with your minimum height value
    private val maxWidthDp = 480F // Replace with your maximum width value
    private val maxHeightDp = 750F // Replace with your maximum height value

    // Convert dp to pixels
    private val minWidthPx = minWidthDp.dpToPx(context).toInt()
    private val minHeightPx = minHeightDp.dpToPx(context).toInt()
    private val maxWidthPx = maxWidthDp.dpToPx(context).toInt()
    private val maxHeightPx = maxHeightDp.dpToPx(context).toInt()
    private var ignoreDimension = false

    init {

        attrs?.let {
            val typedArray: TypedArray =
                context.obtainStyledAttributes(it, R.styleable.CustomConstriants)
            ignoreDimension =
                typedArray.getBoolean(R.styleable.CustomConstriants_ignoreDimensions, false)
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val measuredWidth = measuredWidth.coerceIn(minWidthPx, maxWidthPx)
        val measuredHeight = measuredHeight.coerceIn(minHeightPx, maxHeightPx)
        if (!ignoreDimension)
            setMeasuredDimension(measuredWidth, measuredHeight)
    }

}