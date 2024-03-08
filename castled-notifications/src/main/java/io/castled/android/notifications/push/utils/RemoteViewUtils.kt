package io.castled.android.notifications.push.utils

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL


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

        suspend fun getRemoteViewBitmapFrom(imageUrl: String?): Bitmap? =
            withContext(Dispatchers.IO) {
                try {
                    val url = URL(imageUrl)
                    val connection = url.openConnection()
                    connection.connectTimeout = 10000 // 10 seconds
                    connection.readTimeout = 15000 // 15 seconds
                    val bitmapOptions = BitmapFactory.Options()
                    bitmapOptions.inSampleSize = 4
                    /* inSampleSize with a value superior to one, shrink the image. With an inSampleSize = 4
                     ,we get an image that is 1/4 of the width/height of the image
                    The total Bitmap memory used by the RemoteViews object cannot exceed that required to
                     fill the screen 1.5 times,
                    ie. (screen width x screen height x 4 x 1.5) bytes.*/

                    connection.getInputStream().use { inputStream ->
                        return@withContext BitmapFactory.decodeStream(
                            inputStream,
                            null,
                            bitmapOptions
                        )
                    }
                } catch (e: Exception) { // Catch general exceptions
                    // CastledNotificationBuilder.logger.debug("Bitmap fetch failed, reason: ${e.message}")
                }
                return@withContext null
            }

    }

}