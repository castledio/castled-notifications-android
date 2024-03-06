package io.castled.android.notifications.commons

import android.graphics.Color
import android.widget.RemoteViews


internal class ColorUtils {
    companion object {
        internal fun parseColor(colorStr: String, defaultColor: Int): Int {
            return try {
                Color.parseColor(colorStr)
            } catch (e: IllegalArgumentException) {
                defaultColor
            }
        }

        internal fun setRemoteViewBackgroundColor(
            remoteView: RemoteViews?,
            viewId: Int,
            color: Int
        ) {
            remoteView?.setInt(
                viewId, "setBackgroundColor",
                color
            );
        }
    }

}