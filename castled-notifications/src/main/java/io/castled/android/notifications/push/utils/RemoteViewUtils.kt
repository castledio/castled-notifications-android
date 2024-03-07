package io.castled.android.notifications.push.utils

import android.content.res.ColorStateList
import android.os.Build
import android.widget.RemoteViews


internal class RemoteViewUtils {
    companion object {
        internal fun setRemoteViewBackgroundColor(
            remoteView: RemoteViews?,
            viewId: Int,
            color: Int
        ) {
            try {
                remoteView?.setInt(
                    viewId, "setBackgroundColor",
                    color
                );
            } catch (_: Exception) {
            }
        }

        internal fun setProgressViewTintColor(
            remoteView: RemoteViews?,
            viewId: Int,
            color: Int
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    remoteView?.setColorStateList(
                        viewId,
                        "setProgressTintList",
                        ColorStateList.valueOf(color)
                    );
                } catch (_: Exception) {
                }
            }
        }

        internal fun setProgressViewBackgroundColor(
            remoteView: RemoteViews?,
            viewId: Int,
            color: Int
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    remoteView?.setColorStateList(
                        viewId,
                        "setProgressBackgroundTintList",
                        ColorStateList.valueOf(color)
                    );
                } catch (_: Exception) {
                }
            }
        }

        internal fun setProgressViewProgress(
            remoteView: RemoteViews?,
            viewId: Int,
            value: Int
        ) {
            try {
                remoteView?.setInt(
                    viewId,
                    "setProgress",
                    value
                )
            } catch (_: Exception) {
            }
        }

    }

}