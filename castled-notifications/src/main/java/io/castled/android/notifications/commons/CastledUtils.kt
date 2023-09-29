package io.castled.android.notifications.commons

import android.content.Context
import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.view.WindowMetrics

internal object CastledUtils {

    fun getScreenSize(context: Context): Point {
        val outMetrics = DisplayMetrics()
        val statusBarHeight = getStatusBarHeight(context)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics: WindowMetrics =
                context.getSystemService(WindowManager::class.java).currentWindowMetrics
            Point(metrics.bounds.width(), metrics.bounds.height() - statusBarHeight)

        } else {
            @Suppress("DEPRECATION") val windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display: Display = windowManager.defaultDisplay
            @Suppress("DEPRECATION") display.getMetrics(outMetrics)

            // Reduce the status bar height from the screen height

            Point(display.width, display.height - statusBarHeight)

        }
    }

    fun getStatusBarHeight(context: Context): Int {
        return try {
            var result = 0
            val resourceId =
                context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = context.resources.getDimensionPixelSize(resourceId)
            }
            result
        } catch (e: Exception) {
            0
        }

    }

    fun getCurrentOrientation(context: Context): Int {
        val config = context.resources.configuration
        return config.orientation
    }

    fun changeBackgroundColorAndBorderColor(
        view: View,
        newBackgroundColor: Int,
        newBorderColor: Int
    ) {
        val currentBackground = view.background
        if (currentBackground is GradientDrawable) {
            currentBackground.setColor(newBackgroundColor)
            currentBackground.setStroke(
                1, // Use the existing border width
                newBorderColor // Set the new border color
            )

            // Set the modified drawable as the background of the view
            view.background = currentBackground
        }
    }
}