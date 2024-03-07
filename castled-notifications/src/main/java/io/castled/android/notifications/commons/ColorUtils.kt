package io.castled.android.notifications.commons

import android.graphics.Color


internal class ColorUtils {
    companion object {
        internal fun parseColor(colorStr: String, defaultColor: Int): Int {
            return try {
                Color.parseColor(colorStr)
            } catch (e: IllegalArgumentException) {
                defaultColor
            }
        }
    }

}