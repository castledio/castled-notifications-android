package io.castled.android.notifications.inapp.views

import android.content.Context
import android.util.TypedValue


fun Float.dpToPx(context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        context.resources.displayMetrics
    )
}