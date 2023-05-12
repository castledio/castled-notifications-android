package io.castled.android.notifications.inapp.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import io.castled.android.notifications.R

class RounderCornerImageView(context: Context, attrs: AttributeSet) :
    androidx.appcompat.widget.AppCompatImageView(context, attrs) {

    private val path = Path()

    private var cornerRadius = 0f
        set(value) {
            field = value
            invalidate()
        }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RounderCornerImageView)
        cornerRadius = typedArray.getDimension(R.styleable.RounderCornerImageView_cornerRadius, 0f)
        typedArray.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        path.addRoundRect(rect, floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0f, 0f, 0f, 0f), Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.clipPath(path)
        super.onDraw(canvas)
    }
}